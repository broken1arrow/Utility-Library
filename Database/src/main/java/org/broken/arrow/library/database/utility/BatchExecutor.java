package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.builders.wrappers.DatabaseQueryHandler;
import org.broken.arrow.library.database.builders.wrappers.SaveRecord;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.logging.Level;

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
     * @param shallUpdate true to update existing rows if they exist; false to insert only.
     * @param columns     optional list of columns to save or update.
     */
    public void saveAll(@Nonnull final String tableName, final boolean shallUpdate, String... columns) {
        final List<SqlQueryPair> queryList = new ArrayList<>();

        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        for (T dataToSave : this.dataToProcess) {
            if (!(dataToSave instanceof DataWrapper)) continue;

            final DataWrapper dataWrapper = (DataWrapper) dataToSave;

            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            final boolean columnsIsEmpty = columns == null || columns.length == 0;
            boolean canUpdateRow = false;
            final Object primaryValue = dataWrapper.getPrimaryValue();
            final DataWrapper.PrimaryWrapper primaryWrapper = dataWrapper.getPrimaryWrapper();
            final WhereClauseFunction whereClause = primaryWrapper.getWhereClause();

            if ((!columnsIsEmpty || shallUpdate)) {
                final SqlQueryPair query = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getPrimaryColumns()), true,
                        whereBuilder -> {
                            if (whereClause != null)
                                return whereClause.apply(whereBuilder);
                            return table.createWhereClauseFromPrimaryColumns(whereBuilder, primaryValue);
                        });
                canUpdateRow = this.checkIfRowExist(query, false);
            }
            sqlHandler.setQueryPlaceholders(this.database.isSecureQuery());
            final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, canUpdateRow ? columns : null));

            for (Column primary : table.getPrimaryColumns()) {
                Object value = primaryValue;
                if (value == null)
                    value = primaryWrapper.getPrimaryValue(primary.getColumnName());
                columnValueMap.put(primary, value);
            }
            queryList.add(this.databaseConfig.applyDatabaseCommand(sqlHandler, columnValueMap, wereClause -> {
                if (whereClause != null)
                    return whereClause.apply(wereClause);
                return table.createWhereClauseFromPrimaryColumns(wereClause, primaryValue);
                //return whereClauseFunc.apply(wereClause, primaryValue);
            }, canUpdateRow));
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
    public <K, V extends ConfigurationSerializable> void save(@Nonnull final String tableName, final boolean shallUpdate, final DatabaseQueryHandler<SaveRecord<K, V>> databaseQueryHandler) {
        final List<SqlQueryPair> queryList = new ArrayList<>();

        if (this.dataToProcess.isEmpty()) {
            this.log.log(Level.WARNING, () -> "No query is not set for this table: " + tableName + ". You must have at least 1 command to save data to the database.");
            return;
        }

        for (T dataToSave : this.dataToProcess) {
            final SaveRecord<K, V> saveRecord = getSaveRecord(dataToSave);
            final QueryBuilder queryBuilder = saveRecord != null ? saveRecord.getQueryBuilder() : null;
            if (saveRecord == null || queryBuilder == null || checkIfQuerySet(saveRecord, queryBuilder)) continue;

            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            final boolean columnsFilterSet = databaseQueryHandler.isFilterSet();
            boolean canUpdateRow = false;

            if ((!columnsFilterSet || shallUpdate)) {
                final SqlQueryPair wrappedQuery = sqlHandler.wrapQuery(queryBuilder);
                canUpdateRow = this.checkIfRowExist(wrappedQuery, false);
            }
            Map<Column, Object> toSave = this.getColumns(databaseQueryHandler, saveRecord, canUpdateRow);
            queryList.add(this.databaseConfig.applyDatabaseCommand(sqlHandler, toSave, saveRecord.getWhereClause(), canUpdateRow));
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
    public void save(final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, final String... columns) {
        final SqlQueryTable table = this.database.getTableFromName(tableName);

        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (!checkIfNotNull(dataWrapper)) return;

        final List<SqlQueryPair> queryList = new ArrayList<>();
        final SqlHandler sqlHandler = new SqlHandler(tableName, database);
        final boolean columnsIsEmpty = columns == null || columns.length == 0;
        boolean canUpdateRow = false;
        if ((!columnsIsEmpty || shallUpdate)) {
            final SqlQueryPair query = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getPrimaryColumns()), true, whereClause);
            canUpdateRow = this.checkIfRowExist(query, false);
        }
        sqlHandler.setQueryPlaceholders(this.database.isSecureQuery());
        final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, canUpdateRow ? columns : null));
        for (Column primary : table.getPrimaryColumns()) {
            columnValueMap.put(primary, dataWrapper.getPrimaryValue());
        }

        queryList.add(this.databaseConfig.applyDatabaseCommand(sqlHandler, columnValueMap, whereClause, canUpdateRow));
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

        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        final SqlHandler sqlHandler = new SqlHandler(tableName, database);
        List<SqlQueryPair> queryList = new ArrayList<>();
        for (String value : values) {
            queryList.add(sqlHandler.removeRow(where -> whereClause.apply(where, value)));
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
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        final SqlHandler sqlHandler = new SqlHandler(tableName, database);
        List<SqlQueryPair> queryList = new ArrayList<>();
        queryList.add(sqlHandler.removeRow(where -> whereClause.apply(where, value)));
        this.executeDatabaseTasks(queryList);
    }

    /**
     * Drops the entire table with the specified name.
     *
     * @param tableName the database table name to drop.
     */
    public void dropTable(String tableName) {
        final SqlQueryTable table = this.database.getTableFromName(tableName);

        if (table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        final SqlHandler sqlHandler = new SqlHandler(tableName, database);
        List<SqlQueryPair> queryList = new ArrayList<>();
        queryList.add(sqlHandler.dropTable());
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
    public boolean checkIfRowExist(@Nonnull String tableName, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (table == null) {
            this.printFailFindTable(tableName);
            return false;
        }
        final SqlHandler sqlHandler = new SqlHandler(tableName, database);
        final SqlQueryPair query = sqlHandler.selectRow(columnManger ->
                columnManger.addAll(table.getPrimaryColumns()), true, whereClause);
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

            rowWrapper.put(ColumnManager.of().column(name).getColumn(), entry.getValue());
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
    private boolean checkIfRowExist(@Nonnull final SqlQueryPair query, final boolean closeConnection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.getQuery())) {
            if (query.isSafeQuery()) {
                query.getValues().forEach((index, value) -> {
                    try {
                        preparedStatement.setObject(index, value);
                    } catch (SQLException e) {
                        log.log(Level.WARNING, e, () -> "Failed to set where clause values. for this query: " + query.getQuery() + ". Check the stacktrace.");
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
    protected void executeDatabaseTasks(List<SqlQueryPair> composerList) {
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
            for (SqlQueryPair sql : composerList) {
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

    private void setPreparedStatement(SqlQueryPair sql) throws SQLException {

        Map<Integer, Object> cachedDataByColumn = sql.getValues();
        try (PreparedStatement statement = connection.prepareStatement(sql.getQuery(), resultSetType, resultSetConcurrency)) {
            boolean valuesSet = false;

            if (!cachedDataByColumn.isEmpty()) {
                for (Map.Entry<Integer, Object> column : cachedDataByColumn.entrySet()) {
                    statement.setObject(column.getKey(), column.getValue());
                    valuesSet = true;
                }
            }
            if (valuesSet)
                statement.addBatch();
            statement.executeBatch();
        } catch (SQLException e) {
            failedSetValuesBatch(sql.getQuery(), e, cachedDataByColumn);
        } catch (ArrayIndexOutOfBoundsException exception) {
            log.log(Level.WARNING, () -> "Could not execute this batch: \"" + sql.getQuery() + "\" . Probably this is not an premed batch with placeholders, check so the query contains ? for all values.");
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

}
