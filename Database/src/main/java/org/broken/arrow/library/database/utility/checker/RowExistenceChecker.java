package org.broken.arrow.library.database.utility.checker;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuildContext;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.utlity.DataType;
import org.broken.arrow.library.database.utility.DatabaseType;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * An efficient batch processor that determines which rows from a dataset already exist in a target database table.
 *
 * <p>This class evaluates a batch of in-memory records against a database table by:
 * <ol>
 *     <li>Creating a lightweight temporary table matching the composite key structure.</li>
 *     <li>Inserting the composite key values of the provided rows into the temporary table in chunked batches.</li>
 *     <li>Performing an inner join between the temporary table and the target table to locate matching records.</li>
 * </ol>
 *
 * <p>The result is returned as an {@link ExistingRowKeys} set containing the matching composite keys, allowing fast lookup
 * via {@link ExistingRowKeys#contains(DataWrapper)}.
 *
 * @param <T> the type of the data items being processed, extending {@link DataWrapper}.
 */
public class RowExistenceChecker<T extends DataWrapper> {
    private static final Logging LOG = new Logging(RowExistenceChecker.class);
    private static final String TEMP_TABLE_NAME = "temp.table_";
    private static final String TEMP = "temp.";

    private final String table;
    private final DatabaseType databaseType;
    private final Connection connection;
    private final List<T> dataToProcess;
    private final List<String> matchKeys;
    private final int batchSize;

    /**
     * Private constructor forcing the use of the {@link RowExistenceBuilder}.
     *
     * @param table        the name of the database table to check for existing records
     * @param databaseType the type of database used
     * @param existenceBuilder      the configured builder containing connection, dataset, and matching criteria
     * @throws Validate.ValidateExceptions if matching keys cannot be determined from the builder or inferred from the dataset
     */
    private RowExistenceChecker(@Nonnull final String table, @Nonnull final DatabaseType databaseType, @Nonnull final RowExistenceBuilder<T> existenceBuilder) {
        Validate.checkBoolean(existenceBuilder.connection == null, "Connection must be set.");
        Validate.checkBoolean(existenceBuilder.dataToProcess == null || existenceBuilder.dataToProcess.isEmpty(), "Data to process can't be null or empty.");
        Validate.checkNotNull(databaseType, "You must set the database type.");

        this.table = table;
        this.databaseType = databaseType;
        this.connection = existenceBuilder.connection;
        this.dataToProcess = existenceBuilder.dataToProcess;
        this.batchSize = existenceBuilder.batchSize;
        final List<String> keys = existenceBuilder.matchKeys;

        if (keys != null && !keys.isEmpty()) {
            this.matchKeys = new ArrayList<>(keys);
        } else {
            this.matchKeys = inferKeysFromData(this.dataToProcess);
            LOG.log(Level.WARNING, () -> "You did not set columns to check, it uses all columns you set from the first row of data set in the list.");
        }
        if (this.matchKeys.isEmpty()) {
            throw new IllegalArgumentException("Cannot determine match columns. Either explicitly provide them using matchOn() or ensure dataToProcess contains valid WriteContext columns.");
        }
    }


    /**
     * Initializes a new builder for {@link RowExistenceChecker}.
     *
     * @param <T>          the type of data rows to be processed
     * @param table        the name of the database table to check for existing records
     * @param databaseType the type of database engine used
     * @param connection   the database connection to execute queries against
     * @return a new {@link RowExistenceBuilder} instance
     */
    public static <T extends DataWrapper> RowExistenceBuilder<T> make(@Nonnull final String table, @Nonnull final DatabaseType databaseType, @Nonnull final Connection connection) {
        return new RowExistenceBuilder<>(table, databaseType, connection);
    }

    /**
     * Evaluates the provided batch of rows against the target table to verify their existence.
     *
     * <p>This method safely manages the full lifecycle of the temporary table, ensuring it is
     * cleaned up even if a database operation fails.
     *
     * @return an {@link ExistingRowKeys} holding the set of composite keys that already exist in the target table
     * @throws SQLException if a database access error occurs during temporary table creation, batch insertion, or join execution
     */
    public ExistingRowKeysView findExisting() throws SQLException {
        final ExistingRowKeys existingRowKeys = new ExistingRowKeys(this.matchKeys);
        if (dataToProcess.isEmpty()) {
            return existingRowKeys;
        }
        @Nonnull final String targetTable = this.table;
        createTempTable(targetTable);

        try {
            this.insertData(targetTable);
            final QueryBuilder checkMatch = buildJoinQuery(targetTable);
            try (Statement selectStmt = connection.createStatement();
                 final ResultSet rs = selectStmt.executeQuery(checkMatch.build())) {
                while (rs.next()) {
                    final Map<String, Object> map = new HashMap<>(matchKeys.size());
                    matchKeys.forEach(col -> {
                        try {
                            map.put(col, rs.getObject(col));
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    existingRowKeys.add(new CompositeKey(map));
                }
            }
        } finally {
            final QueryBuilder dropTemp = new QueryBuilder(this.databaseType);
            dropTemp.dropTable(this.getTempTableName(targetTable));
            try (Statement dropStmt = connection.createStatement()) {
                dropStmt.execute(dropTemp.build());
            } catch (SQLException e) {
                LOG.log(Level.WARNING, e, () -> "Failed to drop temporary table: " + this.getTempTableName(targetTable));
            }
        }
        return existingRowKeys;
    }

    /**
     * Creates a key extractor function for mapping a {@link DataWrapper} instance
     * to its corresponding list of composite key values.
     *
     * @return a function extracting key column values from a row wrapper
     */
    @Nonnull
    private Function<T, List<Object>> getExtractKeyValues() {
        return rowData -> {
            if (rowData == null) return null;

            final Map<String, Object> columnContext = rowData.getWriteContext().getColumnContext();
            final List<Object> keys = new ArrayList<>(matchKeys.size());

            for (String keyCol : matchKeys) {
                Object val = columnContext.get(keyCol);
                if (val == null && !columnContext.containsKey(keyCol)) {
                    return null;
                }
                keys.add(val);
            }
            return keys;
        };
    }

    /**
     * Extracts column key names from the first non-empty {@link DataWrapper} in the batch.
     * This acts as the fallback mechanism when match keys are not explicitly configured on the builder.
     *
     * @param dataToProcess the collection of data rows being evaluated
     * @return a list of column names derived from the data context, or an empty list if none are found
     */
    private List<String> inferKeysFromData(List<T> dataToProcess) {
        for (T rowData : dataToProcess) {
            if (rowData != null) {
                Map<String, Object> ctx = rowData.getWriteContext().getColumnContext();
                if (!ctx.isEmpty()) {
                    return new ArrayList<>(ctx.keySet());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Creates an empty temporary table structured with only the columns required for composite key matching.
     *
     * @param targetTable the source table to derive matching schema context from
     * @throws SQLException if a database error occurs during table creation
     */
    private void createTempTable(String targetTable) throws SQLException {
        final QueryBuilder createTemp = new QueryBuilder(this.databaseType);
        createTemp.setGlobalEnableQueryPlaceholders(false);
        createTemp.createTemporaryTable(this.getTempTableName(targetTable))
                .as()
                .select(c -> matchKeys.forEach(col -> c.add(new TableColumn(col, DataType.varchar(64)))))
                .from(targetTable)
                .where(w -> w.where("1").equal(0));

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTemp.build());
        }
    }

    /**
     * Inserts the extracted composite key values into the temporary table using chunked batch execution.
     *
     * @param targetTable the target database table being checked against
     * @throws SQLException if batch insertion fails
     */
    private void insertData(final String targetTable) throws SQLException {
        final Function<T, List<Object>> extractKeyValues = this.getExtractKeyValues();
        final QueryBuilder insertTemp = new QueryBuilder(this.databaseType);
        insertTemp.insertInto(this.getTempTableName(targetTable), insert -> {
            matchKeys.forEach(col -> insert.add(InsertBuilder.of(col, SqlArg.raw("?"))));
        });
        final int batchSize = this.batchSize;
        int pendingInBatch = 0;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertTemp.build())) {
            for (T row : dataToProcess) {
                List<Object> keyValues = extractKeyValues.apply(row);
                if (keyValues == null) continue;

                for (int i = 0; i < keyValues.size(); i++) {
                    insertStmt.setObject(i + 1, keyValues.get(i));
                }
                insertStmt.addBatch();
                pendingInBatch++;
                if (pendingInBatch == batchSize) {
                    insertStmt.executeBatch();
                    pendingInBatch = 0;
                }
            }
            if (pendingInBatch > 0) {
                insertStmt.executeBatch();
            }
        }
    }

    /**
     * Generates a unique temporary table name based on the target table name.
     *
     * @param targetTable the name of the target database table
     * @return the fully qualified temporary table identifier
     */
    @Nonnull
    private String getTempTableName(@Nonnull final String targetTable) {
        return TEMP_TABLE_NAME + targetTable;
    }

    /**
     * Constructs the inner join query between the target table and the temporary key table
     * to identify existing records.
     *
     * @param targetTable the destination table being checked against
     * @return a {@link QueryBuilder} configured to execute the match query
     */
    @Nonnull
    private QueryBuilder buildJoinQuery(final String targetTable) {
        final QueryBuilder checkMatch = new QueryBuilder(this.databaseType);
        checkMatch.select(c -> matchKeys.forEach(col -> c.add(Column.of(TEMP + col))))
                .distinct(true)
                .from(targetTable, "target")
                .join(j -> j.innerJoin(this.getTempTableName(targetTable), "temp", ctx -> {
                    final String columnName = "target." + matchKeys.get(0);
                    final String tempColumnName = TEMP + matchKeys.get(0);
                    ConditionChainer<JoinBuildContext> currentJoinContext = ctx.on(Column.of(columnName)).equal(Column.of(tempColumnName))
						/*	.or()
							.on(columnName).isNull()
							.and()
							.on(tempColumnName).isNull()*/;

                    for (int i = 1; i < matchKeys.size(); i++) {
                        String colName = matchKeys.get(i);
                        currentJoinContext.and()
                                .on(Column.of("target." + colName)).equal(Column.of(TEMP + colName))
								/*.or()
								.on("target." + colName).isNull()
								.and()
								.on(TEMP + colName).isNull()*/;
                    }
                    return currentJoinContext;
                }));
        return checkMatch;
    }

    /**
     * Builder class for configuring and instantiating {@link RowExistenceChecker}.
     *
     * @param <T> the type of the data rows being processed
     */
    public static class RowExistenceBuilder<T extends DataWrapper> {
        private final String table;
        private final DatabaseType databaseType;
        private final Connection connection;
        private int batchSize = 2000;
        private List<T> dataToProcess = new ArrayList<>();
        private List<String> matchKeys = new ArrayList<>();

        /**
         * Constructs a new builder with the required target configuration.
         *
         * @param table        the name of the database table to check for existing records
         * @param databaseType the type of database engine used
         * @param connection   the database connection to execute queries against
         */
        public RowExistenceBuilder(@Nonnull final String table, @Nonnull final DatabaseType databaseType, @Nonnull final Connection connection) {
            this.table = table;
            this.databaseType = databaseType;
            this.connection = connection;
        }

        /**
         * Sets the dataset to be evaluated.
         *
         * @param dataToProcess a list of records to check for existence
         * @return this builder instance
         */
        public RowExistenceBuilder<T> withData(@Nonnull final List<T> dataToProcess) {
            this.dataToProcess = dataToProcess;
            return this;
        }

        /**
         * Explicitly defines the column names that form the composite key to match on, using Varargs.
         * If not provided, the class will attempt to infer these keys from the dataset.
         *
         * @param columns the names of the database columns to use for matching
         * @return this builder instance
         */
        public RowExistenceBuilder<T> matchOn(@Nonnull final String... columns) {
            this.matchKeys = Arrays.asList(columns);
            return this;
        }

        /**
         * Explicitly defines the column names that form the composite key to match on, using a List.
         * If not provided, the class will attempt to infer these keys from the dataset.
         *
         * @param columns a list of database column names to use for matching
         * @return this builder instance
         */
        public RowExistenceBuilder<T> matchOn(@Nonnull final List<String> columns) {
            this.matchKeys = columns;
            return this;
        }

        /**
         * Sets the batch size used when executing batch SQL statements against the target table.
         * If not set, it defaults to a batch size of {@code 2000}.
         * <p>
         * Controls how many rows are accumulated in memory before sending an
         * {@code executeBatch()} command to the database driver. Tuning this value can help balance
         * network round-trip overhead with database memory utilization.
         * </p>
         *
         * <ul>
         *   <li><b>Minimum value:</b> {@code 1} (Executes after every row, useful for debugging).</li>
         *   <li><b>Recommended default:</b> {@code 2000}.</li>
         *   <li><b>Large values:</b> Setting this higher than {@code 10000} may cause high memory
         *       usage on the client or hit database packet limits (e.g., maximum parameters per statement).</li>
         * </ul>
         *
         * @param batchSize the number of statements per executeBatch call, defaults to 1 if set to zero or below
         * @return this builder instance for method chaining
         */
        public RowExistenceBuilder<T> setBatchSize(final int batchSize) {
            if (batchSize > 10_000) {
                LOG.warn(() -> "Configured batchSize (" + batchSize + ") is unusually large. " +
                        "This may cause excessive memory consumption or exceed database driver packet limits.");
            }
            this.batchSize = Math.max(batchSize, 1);
            return this;
        }

        /**
         * Constructs the {@link RowExistenceChecker} processor and executes the row existence query.
         *
         * @return an {@link ExistingRowKeysView} holding existing record keys found in the database
         * @throws SQLException if database execution fails
         */
        public ExistingRowKeysView findExisting() throws SQLException {
            final RowExistenceChecker<T> rowExistenceChecker = new RowExistenceChecker<>(this.table, databaseType, this);
            return rowExistenceChecker.findExisting();
        }
    }
}


