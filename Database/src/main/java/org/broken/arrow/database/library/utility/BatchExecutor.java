package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.RowDataWrapper;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.SqlQueryBuilder;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.builders.tables.SqlQueryTable;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public class BatchExecutor {
    protected final Database database;
    protected final List<DataWrapper> dataWrapperList;
    protected final Connection connection;
    protected final int resultSetType;
    protected final int resultSetConcurrency;
    private final Logging log = new Logging(BatchExecutor.class);
    private final DatabaseCommandConfig databaseConfig;
    private volatile boolean batchUpdateGoingOn;

    public BatchExecutor(@Nonnull final Database database, @Nonnull final Connection connection, @Nonnull final List<DataWrapper> dataWrapperList) {
        this.database = database;
        this.connection = connection;
        this.dataWrapperList = dataWrapperList;
        this.databaseConfig = this.database.databaseConfig();
        this.resultSetType = this.databaseConfig.getResultSetType();
        this.resultSetConcurrency = this.databaseConfig.getResultSetConcurrency();
    }

    public void saveAll(@Nonnull final String tableName, final boolean shallUpdate, final WhereClauseApplier whereClauseFunc, String... columns) {
        final List<SqlCommandComposer> composerList = new ArrayList<>();
        final List<SqlQueryPair> queryList = new ArrayList<>();

        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (whereClauseFunc == null) {
            this.log.log(Level.WARNING, () -> Logging.of("Where clause is not set for this table: " + tableName + ". You must register the table before attempting to save all data."));
            return;
        }

        for (DataWrapper dataWrapper : dataWrapperList) {

            if (table != null) {
                final SqlHandler sqlHandler = new SqlHandler(tableName, database);
                final boolean columnsIsEmpty = columns == null || columns.length == 0;
                boolean canUpdateRow = false;
                if ((!columnsIsEmpty || shallUpdate)) {
                    final String query = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getPrimaryColumns()), false, whereBuilder -> table.createWhereClauseFromPrimaryColumns(whereBuilder, dataWrapper.getPrimaryValue())).getQuery();
                    canUpdateRow = this.checkIfRowExist(query, false);
                }
                sqlHandler.setQueryPlaceholders(this.database.isSecureQuery());

                final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, columns));
                for (Column primary : table.getPrimaryColumns()) {
                    columnValueMap.put(primary, dataWrapper.getPrimaryValue());
                }

                queryList.add(this.databaseConfig.applyDatabaseCommand(sqlHandler, columnValueMap, wereClause -> whereClauseFunc.apply(wereClause, dataWrapper.getPrimaryValue()), canUpdateRow));
            } else {
                TableRow primaryRow = tableWrapper.getPrimaryRow();
                if (dataWrapper == null || primaryRow == null) continue;
                this.formatData(composerList, dataWrapper, shallUpdate, columns, tableWrapper);
            }
        }
        if (table != null) {
            this.executeDatabaseTasks(queryList);
        } else {
            this.executeDatabaseTask(composerList);
        }
    }

    public void save(final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, final String... columns) {
        final List<SqlCommandComposer> composerList = new ArrayList<>();
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (!checkIfNotNull(dataWrapper)) return;

        if (table != null) {
            final List<SqlQueryPair> queryList = new ArrayList<>();
            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            final boolean columnsIsEmpty = columns == null || columns.length == 0;
            boolean canUpdateRow = false;
            if ((!columnsIsEmpty || shallUpdate)) {
                final String query = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getPrimaryColumns()), false, whereClause).getQuery();
                canUpdateRow = this.checkIfRowExist(query, false);
            }
            sqlHandler.setQueryPlaceholders(this.database.isSecureQuery());
            final Map<Column, Object> columnValueMap = new HashMap<>(formatData(dataWrapper, columns));
            for (Column primary : table.getPrimaryColumns()) {
                columnValueMap.put(primary, dataWrapper.getPrimaryValue());
            }

            queryList.add(this.databaseConfig.applyDatabaseCommand(sqlHandler, columnValueMap, whereClause, canUpdateRow));
            this.executeDatabaseTasks(queryList);
        } else {
            this.formatData(composerList, dataWrapper, shallUpdate, columns, tableWrapper);
            this.executeDatabaseTask(composerList);
        }
    }

    public void removeAll(@Nonnull final String tableName, @Nonnull final List<String> values, @Nonnull final WhereClauseApplier whereClause) {
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (table != null) {
            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            List<SqlQueryPair> queryList = new ArrayList<>();
            for (String value : values) {
                queryList.add(sqlHandler.removeRow(where -> whereClause.apply(where, value)));
            }
            this.executeDatabaseTasks(queryList);
            return;
        }
        final List<SqlCommandComposer> columns = new ArrayList<>();
        for (String value : values) {
            final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);
            sqlCommandComposer.removeRow(value);
            columns.add(sqlCommandComposer);
        }
        this.executeDatabaseTask(columns);
    }

    public void remove(@Nonnull final String tableName, @Nonnull final String value, @Nonnull final WhereClauseApplier whereClause) {
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        if (table != null) {
            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            List<SqlQueryPair> queryList = new ArrayList<>();
            queryList.add(sqlHandler.removeRow(where -> whereClause.apply(where, value)));
            this.executeDatabaseTasks(queryList);
            return;
        }
        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);
        sqlCommandComposer.removeRow(value);
        this.executeDatabaseTask(Collections.singletonList(sqlCommandComposer));
    }

    public void dropTable(String tableName) {
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return;
        }

        if (table != null) {
            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            List<SqlQueryPair> queryList = new ArrayList<>();
            queryList.add(sqlHandler.dropTable());
            this.executeDatabaseTasks(queryList);
            return;
        }
        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);
        sqlCommandComposer.dropTable();
        this.executeDatabaseTask(Collections.singletonList(sqlCommandComposer));
    }

    public void runSQLCommand(@Nonnull final SqlQueryBuilder... sqlQueryBuilders) {
        if (!checkIfNotNull(sqlQueryBuilders)) return;

        final List<SqlCommandComposer> sqlComposer = new ArrayList<>();
        for (SqlQueryBuilder command : sqlQueryBuilders) {
            final TableWrapper tableWrapper = TableWrapper.of(command, TableRow.of("", ""));
            final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);
            sqlCommandComposer.executeCustomCommand();
            sqlComposer.add(sqlCommandComposer);
        }
        this.executeDatabaseTask(sqlComposer);
    }

    /**
     * Checks if a row exists in the specified table.
     * <p>&nbsp;</p>
     * <p>For save operations, running this check beforehand is unnecessary since the appropriate SQL
     * command will be used based on whether the value already exists.</p>
     *
     * @param tableName       the name of the table to search for the data.
     * @param primaryKeyValue the primary key value to look for in the table.
     * @param whereClause     witch rows to update.
     * @return {@code true} if the key exists in the table, or {@code false} if the data is not found
     * or a connection issue occurs.
     */
    public boolean checkIfRowExist(@Nonnull String tableName, @Nonnull Object primaryKeyValue, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        final SqlQueryTable table = this.database.getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            this.printFailFindTable(tableName);
            return false;
        }
        if (table != null) {
            final SqlHandler sqlHandler = new SqlHandler(tableName, database);
            final String query = sqlHandler.selectRow(columnManger ->
                            columnManger.addAll(table.getPrimaryColumns()), false, whereClause)
                    .getQuery();
            return this.checkIfRowExist(query, true);
        }

        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
        final String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
        Validate.checkNotNull(primaryKeyValue, "The value for the column " + primaryColumn + ". Is null.");

        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);
        return this.checkIfRowExist(sqlCommandComposer.selectRow((String) primaryKeyValue), true);
    }

    private void formatData(@Nonnull final List<SqlCommandComposer> composerList, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, final String[] columns, final TableWrapper tableWrapper) {
        final ConfigurationSerializable configuration = dataWrapper.getConfigurationSerialize();
        final RowWrapper rowWrapper = new RowDataWrapper(tableWrapper, dataWrapper.getPrimaryValue());

        for (Map.Entry<String, Object> entry : configuration.serialize().entrySet()) {
            TableRow column = tableWrapper.getColumn(entry.getKey());
            if (column == null) continue;
            rowWrapper.putColumn(entry.getKey(), entry.getValue());
        }
        composerList.add(this.getCommandComposer(rowWrapper, shallUpdate, columns));
    }

    private Map<Column, Object> formatData(@Nonnull final DataWrapper dataWrapper, final String[] columns) {
        final ConfigurationSerializable configuration = dataWrapper.getConfigurationSerialize();
        final Map<Column, Object> rowWrapper = new HashMap<>();
        for (Map.Entry<String, Object> entry : configuration.serialize().entrySet()) {
            if (columns != null && columns.length > 0 && !checkIfUpdateColumn(columns, entry.getKey())) continue;

            rowWrapper.put(ColumnManger.of().column(entry.getKey()).getColumn(), entry.getValue());
        }

        return rowWrapper;
    }

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
     * Retrieves a SqlCommandComposer instance.
     *
     * @param rowWrapper  The current row's column data.
     * @param shallUpdate Specifies whether the table should be updated.
     * @param columns     If not null and not empty, updates the existing row with these columns if it exists.
     * @return The SqlCommandComposer instance with the finish SQL command for either use for prepare V or not.
     */
    private SqlCommandComposer getCommandComposer(@Nonnull final RowWrapper rowWrapper, final boolean shallUpdate, String... columns) {
        final SqlCommandComposer commandComposer = new SqlCommandComposer(rowWrapper, this.database);
        commandComposer.setColumnsToUpdate(columns);

        final boolean columnsIsEmpty = columns == null || columns.length == 0;
        final String tableName = rowWrapper.getTableWrapper().getTableName();
        final TableWrapper tableWrapper = this.database.getTable(tableName);
        if (tableWrapper == null) {
            this.printFailFindTable(tableName);
            return commandComposer;
        }
        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
        final String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
        Validate.checkNotNull(rowWrapper.getPrimaryKeyValue(), "Could not find column for " + primaryColumn + ". Because the column value is null.");

        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this.database);

        boolean canUpdateRow = (!columnsIsEmpty || shallUpdate) && this.checkIfRowExist(sqlCommandComposer.selectRow(rowWrapper.getPrimaryKeyValue() + ""), false);
        this.databaseConfig.applyDatabaseCommand(commandComposer, rowWrapper.getPrimaryKeyValue(), canUpdateRow);
        return commandComposer;
    }

    /**
     * Checks if a row exists in the specified table.
     * <p>&nbsp;<p>
     * <p><strong>Important:</strong> If you use this method, you must either:</p>
     * <ul>
     *   <li>Set {@code closeConnection} to {@code true} to automatically close the connection.</li>
     *   <li>Manually close the connection after use if {@code closeConnection} is set to {@code false}.</li>
     * </ul>
     *
     * <p>For save operations, running this check beforehand is unnecessary, as the appropriate SQL
     * command will be used based on whether the value already exists.</p>
     *
     * @param sqlQuery        the query to run to check if the row exists.
     * @param closeConnection set to {@code true} to close the connection after the call,
     *                        or {@code false} if you plan to use it further.
     * @return {@code true} if the key exists in the table, or {@code false} if either a connection
     * issue occurs or the data is not found.
     */
    private boolean checkIfRowExist(@Nonnull String sqlQuery, boolean closeConnection) {

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            log.log(e, () -> of("Could not search for your the row with this query '" + sqlQuery + "' ."));
        }
        if (closeConnection) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Failed to close database connection."));
            }
        }

        return false;
    }

    protected void executeDatabaseTasks(List<SqlQueryPair> composerList) {
        batchUpdateGoingOn = true;
        final Connection databaseConnection = this.connection;
        final int processedCount = composerList.size();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (batchUpdateGoingOn) log.log(() -> of("Still executing, DO NOT SHUTDOWN YOUR SERVER."));
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
            log.log(Level.WARNING, e, () -> of("Error during batch execution. Rolling back changes."));
            try {
                databaseConnection.rollback();
            } catch (SQLException rollbackEx) {
                log.log(Level.SEVERE, rollbackEx, () -> of("Failed to rollback changes after error."));
            }
            this.batchUpdateGoingOn = false;
        } finally {
            try {
                databaseConnection.setAutoCommit(true);
            } catch (SQLException ex) {
                log.log(Level.WARNING, ex, () -> of("Could not reset auto-commit to true."));
            }
            try {
                databaseConnection.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Failed to close database connection."));
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
            log.log(Level.WARNING, () -> of("Could not execute this prepared batch: \"" + sql.getQuery() + "\""));
            log.log(e, () -> of("Values that could not be executed: '" + cachedDataByColumn.values() + "'"));
        } catch (ArrayIndexOutOfBoundsException exception) {
            log.log(Level.WARNING, () -> of("Could not execute this batch: \"" + sql.getQuery() + "\" . Probably this is not an premed batch with placeholders, check so the query contains ? for all values."));
            log.log(exception, () -> of("Values that could not be executed: '" + cachedDataByColumn.entrySet() + "'"));
        }
    }

    protected void executeDatabaseTask(List<SqlCommandComposer> composerList) {
        batchUpdateGoingOn = true;
        Connection databaseConnection = this.connection;
        final int processedCount = composerList.size();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (batchUpdateGoingOn) log.log(() -> of("Still executing, DO NOT SHUTDOWN YOUR SERVER."));
                else cancel();
            }
        }, 1000 * 30L, 1000 * 30L);
        if (processedCount > 10_000)
            this.printPressesCount(processedCount);
        try {
            databaseConnection.setAutoCommit(false);
            int batchSize = 1;
            for (SqlCommandComposer sql : composerList) {
                this.setPreparedStatement(sql);

                if (batchSize % 100 == 0)
                    databaseConnection.commit();
                batchSize++;
            }
            databaseConnection.commit();
        } catch (SQLException e) {
            log.log(Level.WARNING, e, () -> of("Error during batch execution. Rolling back changes."));
            try {
                databaseConnection.rollback();
            } catch (SQLException rollbackEx) {
                log.log(Level.SEVERE, rollbackEx, () -> of("Failed to rollback changes after error."));
            }
            this.batchUpdateGoingOn = false;
        } finally {
            try {
                databaseConnection.setAutoCommit(true);
            } catch (SQLException ex) {
                log.log(Level.WARNING, ex, () -> of("Could not reset auto-commit to true."));
            }
            try {
                databaseConnection.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Failed to close database connection."));
            } finally {
                this.batchUpdateGoingOn = false;
                timer.cancel();
            }
        }
    }

    private void setPreparedStatement(SqlCommandComposer sql) throws SQLException {

        Map<Integer, Object> cachedDataByColumn = sql.getCachedDataByColumn();
        try (PreparedStatement statement = connection.prepareStatement(sql.getPreparedSQLBatch(), resultSetType, resultSetConcurrency)) {
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
            log.log(Level.WARNING, () -> of("Could not execute this prepared batch: \"" + sql.getPreparedSQLBatch() + "\""));
            log.log(e, () -> of("Values that could not be executed: '" + cachedDataByColumn.values() + "'"));
        }
    }

    public void printPressesCount(int processedCount) {
        if (processedCount > 10_000)
            log.log(() -> of(("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE " + (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.")));
    }

    public boolean checkIfNotNull(Object object) {
        return object != null;
    }

    public void printFailFindTable(String tableName) {
        log.log(Level.WARNING, () -> of("Could not find table " + tableName));
    }
}
