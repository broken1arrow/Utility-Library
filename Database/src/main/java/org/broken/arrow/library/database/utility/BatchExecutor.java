package org.broken.arrow.library.database.utility;

import com.mongodb.lang.NonNull;
import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.WriteContext;
import org.broken.arrow.library.database.builders.schema.TableQuery;
import org.broken.arrow.library.database.builders.wrappers.SqlQuery;
import org.broken.arrow.library.database.builders.schema.TableSchema;
import org.broken.arrow.library.database.builders.wrappers.handlers.DatabaseQueryHandler;
import org.broken.arrow.library.database.builders.wrappers.SaveRecord;
import org.broken.arrow.library.database.builders.wrappers.handlers.DatabaseQuerySaving;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuildContext;
import org.broken.arrow.library.database.construct.query.builder.column.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.database.utility.query.build.SqlResultRow;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles batch updates and operations on the database.
 *
 * @param <T> The type of data to be saved or processed.
 */
public class BatchExecutor<T> {

    /**
     * Database instance.
     */
    protected final Database database;
    /**
     * Data to process.
     */
    protected final List<T> dataToProcess;
    /**
     * Database connection.
     */
    protected final Connection connection;
    /**
     * Result set.
     */
    protected final int resultSetType;
    /**
     * Result set concurrency.
     */
    protected final int resultSetConcurrency;
    private final Logging log = new Logging(BatchExecutor.class);
    private final DatabaseCommandConfig databaseConfig;
    private volatile boolean batchUpdateGoingOn;

    /**
     * Creates a new BatchExecutor instance.
     *
     * @param database      the database instance to operate on.
     * @param connection    the database connection to use.
     * @param dataToProcess the list of data to be processed in batch.
     */
    public BatchExecutor(@Nonnull final Database database, @Nonnull final Connection connection, @Nonnull final List<T> dataToProcess) {
        this.database = database;
        this.connection = connection;
        this.dataToProcess = dataToProcess;
        this.databaseConfig = this.database.databaseConfig();
        this.resultSetType = this.databaseConfig.getResultSetType();
        this.resultSetConcurrency = this.databaseConfig.getResultSetConcurrency();
    }

    /**
     * Saves all items from the batch to the specified table, optionally updating existing rows.
     *
     * @param tableName   the database table name.
     * @param shallUpdate true to update existing rows if they exist, false to allow insert fallback.
     * @param columns     optional list of columns to save or update.
     */
    public void saveAll(@Nonnull final String tableName, final boolean shallUpdate, String... columns) {
        final List<SqlQuery> queryList = new ArrayList<>();

        final TableSchema table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        for (T dataToSave : this.dataToProcess) {
            if (!(dataToSave instanceof DataWrapper)) continue;

            final DataWrapper dataWrapper = (DataWrapper) dataToSave;

            final TableQuery tableQuery = new TableQuery(tableName);
            final boolean columnsIsEmpty = columns == null || columns.length == 0;
            boolean canUpdateRow = false;
            final Object legacyPrimaryValue = dataWrapper.getPrimaryValue();
            final WriteContext primaryWrapper = dataWrapper.getWriteContext();
            final WhereClauseFunction whereClauseCallback = primaryWrapper.getWhereClause();


            final Function<WhereBuilder, ConditionChainer<WhereBuilder>> finalWhereStrategy = whereBuilder -> {
                if (whereClauseCallback != null) {
                    return whereClauseCallback.apply(whereBuilder);
                }
                return table.createWhereClauseFromPrimaryColumns(whereBuilder, legacyPrimaryValue);
            };
            final boolean isManualInsertOrUpdate = !table.isAutoIncrementTable();
            final boolean hasUpdateIntent = !columnsIsEmpty || shallUpdate;
            final boolean primaryValueSet = primaryWrapper.getColumnContext().values().stream().noneMatch(Objects::isNull);

            if (isManualInsertOrUpdate && !primaryValueSet) {
                System.out.println("You must provide valid where clause if, it shall insert or update rows.");
            }

            if (primaryValueSet && hasUpdateIntent) {
                final SqlQuery query = tableQuery.selectRow(columnManger -> {
                    columnManger.addAll(table.getPrimaryColumnsWrapped());
                }, true, finalWhereStrategy);
                canUpdateRow = this.checkIfRowExist(query, false);
            }
            tableQuery.setQueryPlaceholders(this.database.isSecureQuery());
            final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, canUpdateRow ? columns : null));

            for (TableColumn primary : table.getPrimaryColumns()) {
                Object value = legacyPrimaryValue;
                if (value == null || value.toString().isEmpty())
                    value = primaryWrapper.getValue(primary.getColumnName());
                if (value == null) continue;
                columnValueMap.put(primary, value);
            }

            final SqlQuery queryPair = this.databaseConfig.applyDatabaseCommand(tableQuery, columnValueMap, finalWhereStrategy, canUpdateRow);
            final Consumer<SqlResultRow> generatedKeyCallback = dataWrapper.getGeneratedKeyCallback();
            if (generatedKeyCallback != null) {
                queryPair.setGeneratedKeyCallback(generatedKeyCallback);
            }
            queryList.add(queryPair);
        }
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Saves data items with a custom query handler, optionally updating existing rows.
     *
     * @param <K>                  the key type in the save record.
     * @param <V>                  the value type extending ConfigurationSerializable.
     * @param tableName            the database table name.
     * @param shallUpdate          true to update existing rows if they exist; false to insert only.
     * @param databaseQueryHandler handler to provide queries and filters for saving.
     */
    public <K, V extends ConfigurationSerializable> void save(@Nonnull final String tableName, final boolean shallUpdate, final DatabaseQuerySaving<SaveRecord<K, V>> databaseQueryHandler) {
        final List<SqlQuery> queryList = new ArrayList<>();

        if (this.dataToProcess.isEmpty()) {
            this.log.log(Level.WARNING, () -> "No query is not set for this table: " + tableName + ". You must have at least 1 command to save data to the database.");
            return;
        }

        for (T dataToSave : this.dataToProcess) {
            final SaveRecord<K, V> saveRecord = getSaveRecord(dataToSave);
            final QueryBuilder queryBuilder = saveRecord != null ? saveRecord.getQueryBuilder() : null;
            if (saveRecord == null || queryBuilder == null || checkIfQuerySet(saveRecord, queryBuilder)) continue;

            final TableQuery tableQuery = new TableQuery(tableName);
            final boolean columnsFilterSet = databaseQueryHandler.isFilterSet();
            boolean canUpdateRow = false;
            final boolean hasUpdateIntent = !columnsFilterSet || shallUpdate;

            if (hasUpdateIntent) {
                final SqlQuery wrappedQuery = tableQuery.wrapQuery(queryBuilder);
                canUpdateRow = this.checkIfRowExist(wrappedQuery, false);
            }
            final Map<Column, Object> toSave = this.getColumns(databaseQueryHandler, saveRecord, canUpdateRow);
            final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause = saveRecord.getWhereClause();
            final SqlQuery queryPair = this.databaseConfig.applyDatabaseCommand(tableQuery, toSave, whereClause, canUpdateRow);
            final Consumer<SqlResultRow> generatedKeyCallback = databaseQueryHandler.getGeneratedKeyCallback();
            if (generatedKeyCallback != null) {
                queryPair.setGeneratedKeyCallback(generatedKeyCallback);
            }
            queryList.add(queryPair);
        }
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Saves a single data wrapper instance to the specified table, optionally updating existing rows.
     *
     * @param tableName   the database table name.
     * @param dataWrapper the data wrapper instance to save.
     * @param shallUpdate true to update existing rows if they exist; false to insert only.
     * @param whereClause function to apply WHERE clauses for filtering rows.
     * @param columns     optional list of columns to save or update.
     */
    public void save(final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause, final String... columns) {
        final TableSchema table = this.database.getTableFromName(tableName);

        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (!checkIfNotNull(dataWrapper)) return;

        final List<SqlQuery> queryList = new ArrayList<>();
        final TableQuery tableQuery = new TableQuery(tableName);
        final boolean columnsIsEmpty = columns == null || columns.length == 0;
        boolean canUpdateRow = false;
        final Object primaryValue = dataWrapper.getPrimaryValue();
        final WriteContext primaryWrapper = dataWrapper.getWriteContext();
        final boolean isManualInsertOrUpdate = !table.isAutoIncrementTable();
        final boolean hasUpdateIntent = !columnsIsEmpty || shallUpdate;
        final boolean primaryValueSet = primaryWrapper.getColumnContext().values().stream().noneMatch(Objects::isNull);

        if (isManualInsertOrUpdate && !primaryValueSet) {
            System.out.println("You must provide where it shall insert or update rows.");
        }

        if (primaryValueSet && hasUpdateIntent) {
            final SqlQuery query = tableQuery.selectRow(columnManger -> columnManger.addAll(new ArrayList<>(table.getPrimaryColumns())), true, whereClause);
            canUpdateRow = this.checkIfRowExist(query, false);
        }
        tableQuery.setQueryPlaceholders(this.database.isSecureQuery());
        final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, canUpdateRow ? columns : null));

        for (TableColumn primary : table.getPrimaryColumns()) {
            Object value = primaryValue;
            if (value == null || value.toString().isEmpty()) {
                value = primaryWrapper.getValue(primary.getColumnName());
            }
            if (value == null) continue;
            columnValueMap.put(primary, value);
        }
        final SqlQuery queryPair = this.databaseConfig.applyDatabaseCommand(tableQuery, columnValueMap, whereClause, canUpdateRow);
        final Consumer<SqlResultRow> generatedKeyCallback = dataWrapper.getGeneratedKeyCallback();
        if (generatedKeyCallback != null) {
            queryPair.setGeneratedKeyCallback(generatedKeyCallback);
        }
        queryList.add(queryPair);
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Removes multiple rows from the specified table based on a list of values and a where clause.
     *
     * @param tableName   the database table name.
     * @param values      the list of values identifying rows to remove.
     * @param whereClause the where clause applier to select rows.
     */
    public void removeAll(@Nonnull final String tableName, @Nonnull final List<String> values, @Nonnull final WhereClauseApplier whereClause) {

        final TableSchema table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        final TableQuery tableQuery = new TableQuery(tableName);
        List<SqlQuery> queryList = new ArrayList<>();
        for (String value : values) {
            queryList.add(tableQuery.removeRow(where -> whereClause.apply(where, value)));
        }
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Removes a single row from the specified table based on a value and a where clause.
     *
     * @param tableName   the database table name.
     * @param value       the value identifying the row to remove.
     * @param whereClause the where clause applier to select the row.
     */
    public void remove(@Nonnull final String tableName, @Nonnull final String value, @Nonnull final WhereClauseApplier whereClause) {
        final TableSchema table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        final TableQuery tableQuery = new TableQuery(tableName);
        List<SqlQuery> queryList = new ArrayList<>();
        queryList.add(tableQuery.removeRow(where -> whereClause.apply(where, value)));
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Removes matching rows from the specified table based on a value and a where clause.
     *
     * @param tableName   the database table name.
     * @param whereClause the where clause applier to select the row.
     */
    public void remove(@Nonnull final String tableName, @Nonnull final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        final TableSchema table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        final TableQuery tableQuery = new TableQuery(tableName);
        List<SqlQuery> queryList = new ArrayList<>();
        queryList.add(tableQuery.removeRow(whereClause));
        this.executeDatabaseTasks(queryList);
    }


    /**
     * Drops the entire table with the specified name.
     *
     * @param tableName the database table name to drop.
     */
    public void dropTable(String tableName) {
        final TableSchema table = this.database.getTableFromName(tableName);

        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        final TableQuery tableQuery = new TableQuery(tableName);
        List<SqlQuery> queryList = new ArrayList<>();
        queryList.add(tableQuery.dropTable());
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Checks if a row exists in the specified table with a given primary key value and where clause.
     * <p>
     * For save operations, running this check beforehand is unnecessary since the appropriate SQL
     * command will be used based on whether the value already exists.
     * </p>
     *
     * @param tableName   the name of the table to search.
     * @param whereClause the where clause function to filter rows.
     * @return {@code true} if the row exists; {@code false} otherwise or if connection issues occur.
     */
    public boolean checkIfRowExist(@Nonnull String tableName, @Nonnull final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        final TableSchema table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return false;
        }
        final TableQuery tableQuery = new TableQuery(tableName);
        final SqlQuery query = tableQuery.selectRow(columnManger ->
                columnManger.addAll(table.getPrimaryColumnsWrapped()), true, whereClause);
        return this.checkIfRowExist(query, true);
    }

    /**
     * Formats the data from a DataWrapper into a map of columns to values, filtering by specified columns.
     *
     * @param dataWrapper the DataWrapper containing data to format.
     * @param columns     the list of columns to include; if null or empty, all columns are included.
     * @return a map of columns to their corresponding values.
     */
    private Map<Column, Object> formatData(@Nonnull final DataWrapper dataWrapper, final String[] columns) {
        final ConfigurationSerializable configuration = dataWrapper.getConfigurationSerialize();
        return this.formatData(configuration, null, columns);
    }

    /**
     * Formats the data from a ConfigurationSerializable object into a map of columns to values,
     * optionally filtered by columns and a database query handler.
     *
     * @param <K>                  key type of SaveRecord.
     * @param <V>                  value type extending ConfigurationSerializable.
     * @param configuration        the ConfigurationSerializable instance to format.
     * @param databaseQueryHandler the query handler for column filtering; may be null.
     * @param columns              the list of columns to include; if null or empty, all columns are included.
     * @return a map of columns to their corresponding values.
     */
    private <K, V extends ConfigurationSerializable> Map<Column, Object> formatData(final V configuration, final DatabaseQueryHandler<SaveRecord<K, V>> databaseQueryHandler, String[] columns) {
        final Map<Column, Object> rowWrapper = new HashMap<>();
        for (Map.Entry<String, Object> entry : configuration.serialize().entrySet()) {
            String name = entry.getKey();
            if (isFilteredOutColumn(databaseQueryHandler, columns, name)) continue;

            rowWrapper.put(Column.of(name), entry.getValue());
        }
        return rowWrapper;

    }

    /**
     * Checks if a column should be filtered out based on the database query handler and columns filter.
     *
     * @param <K>                  key type of SaveRecord.
     * @param <V>                  value type extending ConfigurationSerializable.
     * @param databaseQueryHandler the query handler; may be null.
     * @param columns              array of columns to include.
     * @param name                 the column name to check.
     * @return true if the column should be filtered out (excluded), false otherwise.
     */
    private <K, V extends ConfigurationSerializable> boolean isFilteredOutColumn(DatabaseQueryHandler<SaveRecord<K, V>> databaseQueryHandler, String[] columns, String name) {
        if (databaseQueryHandler != null && !databaseQueryHandler.containsFilteredColumn(name)) return true;

        return columns != null && columns.length > 0 && !checkIfUpdateColumn(columns, name);
    }

    /**
     * Checks if a specific column name is present in the provided list of columns to update.
     *
     * @param columns    the array of column names to update.
     * @param columnName the name of the column to check.
     * @return true if the column name is in the array; false otherwise.
     */
    private boolean checkIfUpdateColumn(final String[] columns, final String columnName) {
        if (columns == null || columns.length == 0)
            return false;

        for (String column : columns) {
            if (column.equals(columnName))
                return true;
        }
        return false;
    }

    /**
     * Checks if a row exists in the database using a provided SQL query.
     * <p>
     * <strong>Important:</strong> If you use this method, you must either:
     * <ul>
     *   <li>Set {@code closeConnection} to {@code true} to automatically close the connection.</li>
     *   <li>Manually close the connection after use if {@code closeConnection} is set to {@code false}.</li>
     * </ul>
     * <p>
     * For save operations, running this check beforehand is unnecessary, as the appropriate SQL
     * command will be used based on whether the value already exists.
     * </p>
     *
     * @param query           the SQL query pair used to check for existence.
     * @param closeConnection whether to close the database connection after the check.
     * @return {@code true} if the row exists, {@code false} if not found or an error occurs.
     */
    private boolean checkIfRowExist(@Nonnull final SqlQuery query, final boolean closeConnection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.getSql())) {
            if (query.isSafeQuery()) {
                query.getParameters().forEach((index, value) -> {
                    try {
                        preparedStatement.setObject(index, value);
                    } catch (SQLException e) {
                        log.log(Level.WARNING, e, () -> "Failed to set where clause values. for this query: " + query.getSql() + ". Check the stacktrace.");
                    }
                });
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            log.log(e, () -> "Could not search for your the row with this query '" + query + "' .");
        }
        if (closeConnection) {
            try {
                connection.close();
            } catch (SQLException e) {
                failedCloseConnection(e);
            }
        }

        return false;

    }

    /**
     * Executes a list of SQL queries as batch operations against the database.
     * Commits after every 100 queries and handles rollback on errors.
     *
     * @param composerList the list of SQL query pairs to execute.
     */
    protected void executeDatabaseTasks(List<SqlQuery> composerList) {
        batchUpdateGoingOn = true;
        final Connection databaseConnection = this.connection;
        final int processedCount = composerList.size();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (batchUpdateGoingOn) log.log(() -> "Still executing, DO NOT SHUTDOWN YOUR SERVER.");
                else cancel();
            }
        }, 1000 * 30L, 1000 * 30L);

        if (processedCount > 10_000)
            this.printPressesCount(processedCount);
        try {
            databaseConnection.setAutoCommit(false);
            int batchSize = 1;
            for (SqlQuery sql : composerList) {
                this.setPreparedStatement(sql);

                if (batchSize % 100 == 0)
                    databaseConnection.commit();
                batchSize++;
            }
            databaseConnection.commit();
        } catch (SQLException e) {
            log.log(Level.WARNING, e, () -> "Error during batch execution. Rolling back changes.");
            try {
                databaseConnection.rollback();
            } catch (SQLException rollbackEx) {
                log.log(Level.SEVERE, rollbackEx, () -> "Failed to rollback changes after error.");
            }
            this.batchUpdateGoingOn = false;
        } finally {
            try {
                databaseConnection.setAutoCommit(true);
            } catch (SQLException ex) {
                log.log(Level.WARNING, ex, () -> "Could not reset auto-commit to true.");
            }
            try {
                databaseConnection.close();
            } catch (SQLException e) {
                failedCloseConnection(e);
            } finally {
                this.batchUpdateGoingOn = false;
                timer.cancel();
            }
        }
    }

    private void setPreparedStatement(SqlQuery sql) throws SQLException {
        final Map<Integer, Object> cachedDataByColumn = sql.getParameters();
        Consumer<SqlResultRow> callback = sql.getGeneratedKeyCallback();
        int autoGeneratedKeys = callback != null ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;

        try (PreparedStatement statement = connection.prepareStatement(sql.getSql(), autoGeneratedKeys)) {
            boolean valuesSet = false;
            if (!cachedDataByColumn.isEmpty()) {
                for (Map.Entry<Integer, Object> column : cachedDataByColumn.entrySet()) {
                    statement.setObject(column.getKey(), column.getValue());
                    valuesSet = true;
                }
            }
           /* if (valuesSet)
                statement.addBatch();*/
            statement.executeUpdate();
            callbackGeneratedKeys(statement, callback);
        } catch (SQLException e) {
            failedSetValuesBatch(sql.getSql(), e, cachedDataByColumn);
        } catch (ArrayIndexOutOfBoundsException exception) {
            log.log(Level.WARNING, () -> "Could not execute this batch: \"" + sql.getSql() + "\" . Probably this is not an premed batch with placeholders, check so the query contains ? for all values.");
        }
    }

    private static void callbackGeneratedKeys(final PreparedStatement statement, final Consumer<SqlResultRow> callback) throws SQLException {
        if (callback == null) return;

        try (ResultSet rs = statement.getGeneratedKeys()) {
            if (!rs.next()) return;
            final SqlResultRow rowData = new SqlResultRow();
            final ResultSetMetaData metaData = rs.getMetaData();
            boolean foundExactAutoIncrement = false;

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                final String columnName = metaData.getColumnLabel(i);
                final Object value = rs.getObject(i);
                rowData.put(columnName, value);
                if (value instanceof Number) {
                    boolean isAutoInc = metaData.isAutoIncrement(i);
                    if (isAutoInc) {
                        rowData.put("generated_id", value);
                        foundExactAutoIncrement = true;
                    } else if (i == 1 && !foundExactAutoIncrement) {
                        rowData.put("generated_id", value);
                    }
                }
            }
            callback.accept(rowData);
        }
    }

    /**
     * Print the amount of items to process.
     *
     * @param processedCount the amount in the list.
     */
    protected void printPressesCount(int processedCount) {
        if (processedCount > 10_000)
            log.log(() -> ("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE " + (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed."));
    }

    private boolean checkIfNotNull(Object object) {
        return object != null;
    }

    private void failedSetValuesBatch(String sql, SQLException e, Map<Integer, Object> cachedDataByColumn) {
        log.log(Level.WARNING, () -> "Could not execute this prepared batch: \"" + sql + "\"");
        log.log(e, () -> "Values that could not be executed: '" + cachedDataByColumn.values() + "'");
    }

    private void failedCloseConnection(SQLException e) {
        log.log(Level.WARNING, e, () -> "Failed to close database connection.");
    }

    private void printFailFindTable(String tableName) {
        log.log(Level.WARNING, () -> "Could not find table " + tableName);
    }

    @Nullable
    private <K, V extends ConfigurationSerializable> SaveRecord<K, V> getSaveRecord(T dataToSave) {
        if (!(dataToSave instanceof SaveRecord<?, ?>)) {
            this.log.log(Level.WARNING, () -> "Failed to process this data as it is: '" + dataToSave + "' or not an instance of SaveContext");
            return null;
        }
        final SaveRecord<K, V> saveRecord = ((SaveRecord<?, ?>) dataToSave).isSaveContext(dataToSave);
        if (saveRecord == null) {
            this.log.log(Level.WARNING, () -> "Failed to process this: " + dataToSave + ". As it is a class mismatch for the saveContext class for the generic type.");
            return null;
        }
        return saveRecord;
    }

    private <K, V extends ConfigurationSerializable> boolean checkIfQuerySet(SaveRecord<K, V> saveRecord, QueryBuilder queryBuilder) {
        if (queryBuilder == null || saveRecord.getSelectData() == null) {
            this.log.log(Level.WARNING, () -> "Missing queryBuilder for key: " + saveRecord.getKey() + ". Did you forget to call setSelectCommand()?");
            return true;
        }
        if (!queryBuilder.isQuerySet()) {
            this.log.log(Level.WARNING, () -> "query is not correct setup: " + saveRecord.getKey() + ". It seams like you never chose the type of command to execute on the database.");
            return true;
        }

        if (saveRecord.getSelectData().getWhereBuilder().isEmpty()) {
            this.log.log(Level.WARNING, () -> "Missing where clause for key: " + saveRecord.getKey() + ". You must set it via setSelectCommand() to avoid replacing entire table.");
            return true;
        }
        return false;
    }

    private <K, V extends ConfigurationSerializable> @Nonnull Map<Column, Object> getColumns(DatabaseQueryHandler<SaveRecord<K, V>> databaseQueryHandler, SaveRecord<K, V> saveRecord, boolean canUpdateRow) {
        Map<Column, Object> toSave = formatData(saveRecord.getValue(), canUpdateRow ? databaseQueryHandler : null, new String[0]);
        if (!canUpdateRow) {
            if (saveRecord.getKeys().isEmpty())
                this.log.log(Level.WARNING, () -> "Primary key and/or foreign key values were not set. It will still attempt to save the data, which may result in " +
                        "certain columns being saved as null unless your ConfigurationSerializable implementation explicitly handles missing columns and values.");
            else
                toSave.putAll(saveRecord.getKeys());
        }
        return toSave;
    }


    @Nullable
    private DataWrapper getDataWrapper(T dataToSave) {
        if (!(dataToSave instanceof DataWrapper)) {
            this.log.log(Level.WARNING, () -> "Failed to process this save record as it is: '" + dataToSave.getClass() + "' or not an instance of DataWrapper");
            return null;
        }
        return (DataWrapper) dataToSave;
    }

    static class CheckRowsExistence<T> {
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
        private QueryBuilder buildJoinQuery(@NonNull final  String targetTable) {
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
    }

    // =========================================================================
    // THE BUILDER
    // =========================================================================

   private static <T> Builder<T> builder(Connection connection) {
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
