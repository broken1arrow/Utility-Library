package org.broken.arrow.database.library.core;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.builders.tables.SqlQueryTable;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.QueryDefinition;
import org.broken.arrow.database.library.utility.BatchExecutor;
import org.broken.arrow.database.library.utility.BatchExecutorUnsafe;
import org.broken.arrow.database.library.utility.StatementContext;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public abstract class SQLDatabaseQuery extends Database {
    private final Logging log = new Logging(Database.class);

    public SQLDatabaseQuery(@Nonnull final ConnectionSettings connectionSettings) {
        super(connectionSettings);

    }

    @Override
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, String... columns) {
        final Connection connection = getDatabase().attemptToConnect();
        final BatchExecutor batchExecutor;

        if (connection == null) {
            getDatabase().printFailToOpen();
            return;
        }
        if (getDatabase().isSecureQuery())
            batchExecutor = new BatchExecutor(getDatabase(), connection, dataWrapperList);
        else {
            batchExecutor = new BatchExecutorUnsafe(getDatabase(), connection, dataWrapperList);
        }
        SqlQueryTable table = getDatabase().getTableFromName(tableName);
        Function<Object, WhereBuilder> whereClauseFunc = primaryValue -> table == null ? WhereBuilder.of() : table.createWhereClauseFromPrimaryColumns(true, primaryValue);

        batchExecutor.saveAll(tableName, shallUpdate, whereClauseFunc, columns);
    }

    @Override
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, String... columns) {
        final Connection connection = getDatabase().attemptToConnect();
        final BatchExecutor batchExecutor;

        if (connection == null) {
            getDatabase().printFailToOpen();
            return;
        }

        if (getDatabase().isSecureQuery())
            batchExecutor = new BatchExecutor(getDatabase(), connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(getDatabase(), connection, new ArrayList<>());
        }
        SqlQueryTable table = getDatabase().getTableFromName(tableName);
        final WhereBuilder whereClauseFromPrimaryColumns = table == null ? WhereBuilder.of() : table.createWhereClauseFromPrimaryColumns(true, dataWrapper.getPrimaryValue());
        if (whereClauseFromPrimaryColumns.isEmpty()) {
            this.log.log(Level.WARNING, () -> Logging.of("Could not find any set where clause for this table:'" + tableName + "' . Did you set a primary key for at least 1 column?"));
            return;
        }
        batchExecutor.save(tableName, dataWrapper, shallUpdate, where -> whereClauseFromPrimaryColumns, columns);
    }

    @Override
    @Nullable
    public <T extends ConfigurationSerializable> List<LoadDataWrapper<T>> loadAll(@Nonnull final String tableName, @Nonnull final Class<T> clazz) {
        final TableWrapper tableWrapper = getDatabase().getTable(tableName);
        final SqlQueryTable table = getDatabase().getTableFromName(tableName);

        if (tableWrapper == null && table == null) {
            getDatabase().printFailFindTable(tableName);
            return null;
        }

        if (table != null) {
            final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
            final String selectRow = table.selectTable();

            this.executeQuery(QueryDefinition.of(selectRow), statementWrapper -> {
                try (ResultSet resultSet = statementWrapper.getContextResult().executeQuery()) {
                    while (resultSet.next()) {
                        final Map<String, Object> dataFromDB = getDatabase().getDataFromDB(resultSet, table.getTable().getColumns());
                        final T deserialize = getDatabase().getMethodReflectionUtils().invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
                        final List<Column> primaryColumns = table.getTable().getPrimaryColumns();
                        final Map<String, Object> objectList = new HashMap<>();
                        if (!primaryColumns.isEmpty()) {
                            for (Column column : primaryColumns) {
                                Object primaryValue = dataFromDB.get(column.getColumnName());
                                objectList.put(column.getColumnName(), primaryValue);
                            }
                        }
                        loadDataWrappers.add(new LoadDataWrapper<>(objectList, deserialize));
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, e, () -> of("Could not load all data for this table '" + tableName + "'. Check the stacktrace."));
                }
            });
            return loadDataWrappers;
        }

        final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), getDatabase());
        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Primary column should not be null");

        this.executeQuery(QueryDefinition.of(sqlCommandComposer.selectTable()), statementWrapper -> {
            try (ResultSet resultSet = statementWrapper.getContextResult().executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> dataFromDB = getDatabase().getDataFromDB(resultSet, tableWrapper);
                    T deserialize = getDatabase().getMethodReflectionUtils().invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
                    Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());
                    loadDataWrappers.add(new LoadDataWrapper<>(primaryValue, deserialize));
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Could not load all data for this table '" + tableName + "'. Check the stacktrace."));
            }
        });
        return loadDataWrappers;
    }

    @Override
    @Nullable
    public <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, String columnValue) {
        TableWrapper tableWrapper = getDatabase().getTable(tableName);
        SqlQueryTable table = getDatabase().getTableFromName(tableName);
        if (tableWrapper == null && table == null) {
            getDatabase().printFailFindTable(tableName);
            return null;
        }

        if (table != null) {
            final Map<String, Object> dataFromDB = new HashMap<>();
            final SqlHandler sqlHandler = new SqlHandler(table.getTableName(), getDatabase());
            final WhereBuilder primaryColumns = table.createWhereClauseFromPrimaryColumns(true, columnValue);
            Validate.checkBoolean(primaryColumns.isEmpty(), "Could not find any set where clause for this table:'" + tableName + "' . Did you set a primary key for at least 1 column?");

            final SqlQueryPair selectRow = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getTable().getColumns()), primaryColumns);

            this.executeQuery(QueryDefinition.of(selectRow.getQuery()), statementWrapper -> {
                PreparedStatement preparedStatement = (PreparedStatement) statementWrapper.getContextResult();
                primaryColumns.getValues().forEach((index, value) -> {
                    try {
                        preparedStatement.setObject(index, value);
                    } catch (SQLException e) {
                        log.log(Level.WARNING, e, () -> of("Failed to set where clause values. for this column value " + columnValue + ". Check the stacktrace."));
                    }
                });
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next())
                        dataFromDB.putAll(getDatabase().getDataFromDB(resultSet, tableWrapper));
                } catch (SQLException e) {
                    log.log(Level.WARNING, e, () -> of("Could not load the data from " + columnValue + ". Check the stacktrace."));
                }
            });
            if (dataFromDB.isEmpty())
                return null;
            T deserialize = getDatabase().getMethodReflectionUtils().invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
            List<ComparisonHandler<?>> comparisonHandlerList = primaryColumns.getConditionsList();
            Map<String, Object> objectList = new HashMap<>();
            if (!comparisonHandlerList.isEmpty()) {
                for (ComparisonHandler<?> comparisonHandler : comparisonHandlerList) {
                    String column = comparisonHandler != null ? comparisonHandler.getColumn() : null;
                    Object primaryValue = dataFromDB.get(column);
                    objectList.put(column, primaryValue);
                }
            }
            return new LoadDataWrapper<>(objectList, deserialize);
        }

        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
        String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
        Map<String, Object> dataFromDB = new HashMap<>();
        Validate.checkNotNull(columnValue, "Could not find column for " + primaryColumn + ". Because the column value is null.");

        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), getDatabase());

        this.executeQuery(QueryDefinition.of(sqlCommandComposer.selectRow(columnValue)), statementWrapper -> {
            try (ResultSet resultSet = ((PreparedStatement) statementWrapper.getContextResult()).executeQuery()) {
                if (resultSet.next())
                    dataFromDB.putAll(getDatabase().getDataFromDB(resultSet, tableWrapper));
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Could not load the data from " + columnValue + ". Check the stacktrace."));
            }
        });
        if (dataFromDB.isEmpty())
            return null;
        T deserialize = getDatabase().getMethodReflectionUtils().invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
        Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());

        return new LoadDataWrapper<>(primaryValue, deserialize);
    }

    /**
     * This method enables you to set up and execute custom SQL commands on a database and returns PreparedStatement. While this method uses
     * preparedStatement and parameterized queries to reduce SQL injection risks (if you do not turn it off with
     * {@link #setSecureQuery(boolean)}), it is crucial for you to follow safe practices:
     * <p>&nbsp;</p>
     * <ul>
     *   <li>Do not pass unsanitized user input directly into SQL commands. Always validate and sanitize
     *       user-provided values before including them in SQL queries.</li>
     *   <li>Be cautious of security risks when executing custom SQL commands. Ensure that end-users cannot manipulate
     *       sensitive values or keys.</li>
     * </ul>
     * <p>&nbsp;</p>
     * Throws SQLException if a database access error occurs or this method is called on a
     * closed connection. Alternatively the command is not correctly setup.
     *
     * @param queryBuilder the query command you want to execute.
     * @param function     the function that will be applied to the command.
     * @param <T>          The type you want the method to return.
     * @return the value you set as the lambda should return or null if something did go wrong.
     */
    @Nullable
    public <T> T executeQuery(@Nonnull final QueryDefinition queryBuilder, final Function<StatementContext<PreparedStatement>, T> function) {
        final String query = queryBuilder.getQuery();

        if (query.isEmpty()) {
            log.log(() -> of("This query command is not set"));
            return null;
        }

        final Connection connection = getDatabase().attemptToConnect();
        if (connection == null) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return function.apply(new StatementContext<>(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> of("could not execute this command: " + query));
        } finally {
            getDatabase().closeConnection(connection);
        }
        return null;
    }

    /**
     * Executes a custom SQL command on the database and returns a {@link StatementContext}.
     * It is essential to adhere to best practices for safe SQL execution:
     *
     * <ul>
     *   <li>Do not pass unsanitized user input directly into SQL commands. Always validate and sanitize
     *       user-provided values before including them in SQL queries.</li>
     *   <li>Be cautious of security risks when executing custom SQL commands. Ensure that end-users cannot manipulate
     *       sensitive values or keys.</li>
     * </ul>
     *
     * <p>Throws {@code SQLException} if a database access error occurs, if the connection is closed, or if the command
     * is not properly set up.</p>
     *
     * @param queryBuilder the query command you want to execute.
     * @param consumer     a consumer to handle the result returned by the database.
     */

    public void executeQuery(@Nonnull final QueryDefinition queryBuilder, final Consumer<StatementContext<PreparedStatement>> consumer) {
        final String query = queryBuilder.getQuery();

        if (query.isEmpty()) {
            log.log(() -> of("This query command is not set"));
            return;
        }

        Connection connection = getDatabase().attemptToConnect();
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            consumer.accept(new StatementContext<>(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> of("Could not execute this command: " + query));
        } finally {
            getDatabase().closeConnection(connection);
        }
    }

    private Database getDatabase() {
        return this;
    }

}
