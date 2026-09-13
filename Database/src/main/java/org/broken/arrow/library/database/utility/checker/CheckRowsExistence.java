package org.broken.arrow.library.database.utility.checker;

import com.mongodb.lang.NonNull;
import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuildContext;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.utility.BatchExecutor;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CheckRowsExistence<T> {
    private final Logging log = new Logging(CheckRowsExistence.class);
    private final String tempTableName = "temp_keys";

    private final Connection connection;
    private final List<T> rows;
    private final List<String> matchKeys;

    // Private constructor forces the use of the Builder
    private CheckRowsExistence(@Nonnull final Builder<T> builder) {
        this.connection = Objects.requireNonNull(builder.connection, "Connection cannot be null");
        this.rows = Objects.requireNonNull(builder.rows, "dataToProcess cannot be null");

        // If the developer provided keys explicitly, use them.
        // Otherwise, infer the "truth" directly from the provided data.
        if (builder.matchKeys != null && !builder.matchKeys.isEmpty()) {
            this.matchKeys = new ArrayList<>(builder.matchKeys);
        } else {
            this.matchKeys = inferKeysFromData(this.rows);
        }
        if (this.matchKeys.isEmpty()) {
            throw new IllegalArgumentException("Cannot determine match columns. Either explicitly provide them using matchOn() or ensure dataToProcess contains valid WriteContext columns.");
        }
    }

    public Map<Boolean, List<T>> partitionByCompositeKeys(@Nonnull final String targetTable) throws SQLException {
        if (rows.isEmpty()) {
            return new HashMap<>();
        }

        final Set<List<Object>> foundKeys = new HashSet<>();
        // Reusable key extractor strictly against our matchKeys
        final Function<T, List<Object>> extractKeyValues = row -> {
            DataWrapper dataWrapper = getDataWrapper(row);
            if (dataWrapper == null) return null;

            Map<String, Object> columnContext = dataWrapper.getWriteContext().getColumnContext();
            List<Object> keys = new ArrayList<>(matchKeys.size());

            for (String keyCol : matchKeys) {
                Object val = columnContext.get(keyCol);
                if (val == null && !columnContext.containsKey(keyCol)) {
                    return null; // Row missing a required key
                }
                keys.add(val);
            }
            return keys;
        };

        createTempTable(targetTable);

        try {
            insertData(extractKeyValues);
            final QueryBuilder checkMatch = buildJoinQuery(targetTable);

            try (Statement selectStmt = connection.createStatement();
                 ResultSet rs = selectStmt.executeQuery(checkMatch.build())) {
                while (rs.next()) {
                    List<Object> compositeKey = new ArrayList<>(matchKeys.size());
                    for (String col : matchKeys) {
                        compositeKey.add(rs.getObject(col));
                    }
                    foundKeys.add(compositeKey);
                }
            }
        } finally {
            final QueryBuilder dropTemp = new QueryBuilder();
            dropTemp.dropTable(tempTableName);
            try (Statement dropStmt = connection.createStatement()) {
                dropStmt.execute(dropTemp.build());
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> "Failed to drop temporary table: " + tempTableName);
            }
        }

        return rows.stream().collect(Collectors.partitioningBy(row -> {
            List<Object> rowKeyValues = extractKeyValues.apply(row);
            return rowKeyValues != null && foundKeys.contains(rowKeyValues);
        }));
    }

    /**
     * Extracts keys from the first valid DataWrapper in the batch.
     * This acts as the fallback "truth" when the dev doesn't explicitly provide matchKeys.
     *
     * @param dataToProcess the data to progress
     * @return A list of set columns.
     */
    private List<String> inferKeysFromData(List<T> dataToProcess) {
        for (T item : dataToProcess) {
            DataWrapper dataWrapper = getDataWrapper(item);
            if (dataWrapper != null) {
                Map<String, Object> ctx = dataWrapper.getWriteContext().getColumnContext();
                if (!ctx.isEmpty()) {
                    return new ArrayList<>(ctx.keySet());
                }
            }
        }
        return Collections.emptyList();
    }


    private void createTempTable(String targetTable) throws SQLException {
        final QueryBuilder createTemp = new QueryBuilder();
        createTemp.createTemporaryTable(tempTableName)
                .as()
                .select(c -> matchKeys.forEach(col -> c.add((TableColumn) TableColumn.of(col))))
                .from(targetTable)
                .where(w -> w.where("1").equal(0));

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTemp.build());
        }
    }

    private void insertData(@NonNull final Function<T, List<Object>> extractKeyValues) throws SQLException {
        final QueryBuilder insertTemp = new QueryBuilder();
        insertTemp.insertInto(tempTableName, insert -> {
            matchKeys.forEach(col -> insert.add(InsertBuilder.of(col, SqlArg.raw("?"))));
        });

        try (PreparedStatement insertStmt = connection.prepareStatement(insertTemp.build())) {
            for (T row : rows) {
                List<Object> keyValues = extractKeyValues.apply(row);
                if (keyValues == null) continue; // Skip incomplete records

                for (int i = 0; i < keyValues.size(); i++) {
                    insertStmt.setObject(i + 1, keyValues.get(i));
                }
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    @Nonnull
    private QueryBuilder buildJoinQuery(@NonNull final String targetTable) {
        final QueryBuilder checkMatch = new QueryBuilder();
        checkMatch.select(c -> matchKeys.forEach(col -> c.add(Column.of("temp." + col))))
                .from(targetTable, "target")
                .join(j -> j.innerJoin(tempTableName, "temp", ctx -> {
                    ConditionChainer<JoinBuildContext> currentJoinContext = ctx.on(Column.of("target." + matchKeys.get(0)))
                            .equal(Column.of("temp." + matchKeys.get(0)));

                    for (int i = 1; i < matchKeys.size(); i++) {
                        String colName = matchKeys.get(i);
                        currentJoinContext = currentJoinContext.and()
                                .on(Column.of("target." + colName))
                                .equal(Column.of("temp." + colName));
                    }
                    return currentJoinContext;
                }));
        return checkMatch;
    }

    private DataWrapper getDataWrapper(@Nullable final T item) {
        return (item instanceof DataWrapper) ? (DataWrapper) item : null;
    }

    // =========================================================================
    // THE BUILDER
    // =========================================================================
    public static <T> Builder<T> builder(Connection connection) {
        return new Builder<>(connection);
    }

    static class Builder<T> {
        private final Connection connection;
        private List<T> rows = new ArrayList<>();
        private List<String> matchKeys = new ArrayList<>();

        public Builder(Connection connection) {
            this.connection = connection;
        }

        public Builder<T> withData(@Nonnull List<T> dataToProcess) {
            this.rows = dataToProcess;
            return this;
        }

        /**
         * Optional: Explicitly define columns to match on using Varargs.
         */
        public Builder<T> matchOn(String... columns) {
            this.matchKeys = Arrays.asList(columns);
            return this;
        }

        /**
         * Optional: Explicitly define columns to match on using a List.
         */
        public Builder<T> matchOn(List<String> columns) {
            this.matchKeys = columns;
            return this;
        }

        public CheckRowsExistence<T> build() {
            return new CheckRowsExistence<>(this);
        }
    }
}


