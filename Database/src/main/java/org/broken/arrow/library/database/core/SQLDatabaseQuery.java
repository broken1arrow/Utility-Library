package org.broken.arrow.library.database.core;

import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.builders.wrappers.LoadSetup;
import org.broken.arrow.library.database.builders.wrappers.QueryLoader;
import org.broken.arrow.library.database.builders.wrappers.QuerySaver;
import org.broken.arrow.library.database.builders.wrappers.SaveSetup;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.QueryDefinition;
import org.broken.arrow.library.database.utility.BatchExecutor;
import org.broken.arrow.library.database.utility.BatchExecutorUnsafe;
import org.broken.arrow.library.database.utility.StatementContext;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

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

public abstract class SQLDatabaseQuery extends Database {
    private final Logging log = new Logging(Database.class);

    protected SQLDatabaseQuery(@Nonnull final ConnectionSettings connectionSettings) {
        super(connectionSettings);

    }

    @Override
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, String... columns) {
        final Connection connection = getDatabase().attemptToConnect();
        final BatchExecutor<DataWrapper> batchExecutor;

        if (connection == null) {
            getDatabase().printFailToOpen();
            return;
        }
        if (getDatabase().isSecureQuery())
            batchExecutor = new BatchExecutor<>(getDatabase(), connection, dataWrapperList);
        else {
            batchExecutor = new BatchExecutorUnsafe<>(getDatabase(), connection, dataWrapperList);
        }
        SqlQueryTable table = getDatabase().getTableFromName(tableName);

        if (table == null) {
            this.log.log(Level.WARNING, () -> "Could not find this table:'" + tableName + "' . Did you register your table?");
            return;
        }

        batchExecutor.saveAll(tableName, shallUpdate, table::createWhereClauseFromPrimaryColumns, columns);
    }

    @Override
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, String... columns) {
        final Connection connection = getDatabase().attemptToConnect();
        final BatchExecutor<DataWrapper> batchExecutor;

        if (connection == null) {
            getDatabase().printFailToOpen();
            return;
        }

        if (getDatabase().isSecureQuery())
            batchExecutor = new BatchExecutor<>(getDatabase(), connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe<>(getDatabase(), connection, new ArrayList<>());
        }
        SqlQueryTable table = getDatabase().getTableFromName(tableName);
        if (table == null) {
            this.log.log(Level.WARNING, () -> "Could not find this table:'" + tableName + "' . Did you register your table?");
            return;
        }
        if (table.getPrimaryColumns().isEmpty()) {
            this.log.log(Level.WARNING, () -> "Could not find any set where clause for this table:'" + tableName + "' . Did you set a primary key for at least 1 column?");
            return;
        }

        batchExecutor.save(tableName, dataWrapper, shallUpdate, where -> table.createWhereClauseFromPrimaryColumns(where, dataWrapper.getPrimaryValue()), columns);
    }

    @Nonnull
    @Override
    public <K, V extends ConfigurationSerializable> QuerySaver<K, V> save(@Nonnull final String tableName, @Nonnull final Map<K, V> cacheToSave,
                                                                          @Nonnull final Consumer<SaveSetup> saveSetup) {
        return new QuerySaver<>(this, tableName, cacheToSave, saveSetup);
    }

    @Override
    @Nullable
    public <T extends ConfigurationSerializable> List<LoadDataWrapper<T>> loadAll(@Nonnull final String tableName, @Nonnull final Class<T> clazz) {
        final SqlQueryTable table = getDatabase().getTableFromName(tableName);

        if (table == null) {
            getDatabase().printFailFindTable(tableName);
            return null;
        }

        final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
        final String selectRow = table.selectTable();
        this.executeQuery(QueryDefinition.of(selectRow), statementWrapper -> {
            try (ResultSet resultSet = statementWrapper.getContextResult().executeQuery()) {
                while (resultSet.next()) {
                    final Map<String, Object> dataFromDB = getDatabase().getDataFromDB(resultSet, table.getTable().getColumns());
                    final T deserialize = getDatabase().deSerialize(clazz, dataFromDB);
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
                log.log(Level.WARNING, e, () -> "Could not load all data for this table '" + tableName + "'. Check the stacktrace.");
            }
        });

        return loadDataWrappers;
    }

    @Override
    @Nullable
    public <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final String columnValue) {
        SqlQueryTable table = getDatabase().getTableFromName(tableName);
        if (table == null) {
            getDatabase().printFailFindTable(tableName);
            return null;
        }

        final Map<String, Object> dataFromDB = new HashMap<>();
        final SqlHandler sqlHandler = new SqlHandler(table.getTableName(), getDatabase());

        final WhereBuilder whereBuilder = WhereBuilder.of(new QueryBuilder().setGlobalEnableQueryPlaceholders(this.isSecureQuery()));
        table.createWhereClauseFromPrimaryColumns(whereBuilder, columnValue);
        Validate.checkBoolean(whereBuilder.isEmpty(), "Could not find any set where clause for this table:'" + tableName + "' . Did you set a primary key for at least 1 column?");

        final SqlQueryPair selectRow = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getTable().getColumns()), whereBuilder);

        this.executeQuery(QueryDefinition.of(selectRow.getQuery()), statementWrapper -> {
            PreparedStatement preparedStatement = statementWrapper.getContextResult();

            whereBuilder.getValues().forEach((index, value) -> {
                try {
                    preparedStatement.setObject(index, value);
                } catch (SQLException e) {
                    log.log(Level.WARNING, e, () -> "Failed to set where clause values. for this column value " + columnValue + ". Check the stacktrace.");
                }
            });
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    dataFromDB.putAll(getDatabase().getDataFromDB(resultSet, table.getTable().getColumns()));
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> "Could not load the data from " + columnValue + ". Check the stacktrace.");
            }
        });
        if (dataFromDB.isEmpty())
            return null;
        T deserialize = getDatabase().deSerialize(clazz, dataFromDB);
        List<ComparisonHandler<WhereBuilder>> comparisonHandlerList = whereBuilder.getConditionsList();
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


    @Nonnull
    @Override
    public <T extends ConfigurationSerializable> QueryLoader<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final Consumer<LoadSetup<T>> setup) {
        return new QueryLoader<>(this, tableName, clazz, setup);
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
            log.log(() -> "This query command is not set");
            return null;
        }

        final Connection connection = getDatabase().attemptToConnect();
        if (connection == null) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return function.apply(new StatementContext<>(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> "could not execute this command: " + query);
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
            log.log(() -> "This query command is not set");
            return;
        }
        Connection connection = getDatabase().attemptToConnect();
        if (connection == null) {
            return;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            consumer.accept(new StatementContext<>(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> "Could not execute this command: " + query);
        } finally {
            getDatabase().closeConnection(connection);
        }
    }

    public Database getDatabase() {
        return this;
    }

}
