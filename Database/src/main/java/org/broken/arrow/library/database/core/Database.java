package org.broken.arrow.library.database.core;

import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.builders.wrappers.LoadSetup;
import org.broken.arrow.library.database.builders.wrappers.QueryLoader;
import org.broken.arrow.library.database.builders.wrappers.QuerySaver;
import org.broken.arrow.library.database.builders.wrappers.SaveSetup;
import org.broken.arrow.library.database.connection.HikariCP;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.AlterTable;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.core.databases.H2DB;
import org.broken.arrow.library.database.core.databases.MongoDB;
import org.broken.arrow.library.database.core.databases.MySQL;
import org.broken.arrow.library.database.core.databases.PostgreSQL;
import org.broken.arrow.library.database.core.databases.SQLite;

import org.broken.arrow.library.database.utility.BatchExecutor;
import org.broken.arrow.library.database.utility.BatchExecutorUnsafe;
import org.broken.arrow.library.database.utility.DatabaseCommandConfig;
import org.broken.arrow.library.database.utility.DatabaseType;
import org.broken.arrow.library.database.utility.PrimaryConstraintWrapper;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.serialize.utility.serialize.MethodReflectionUtils;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * The main database class that handle the general logic around database queries.
 */
@SuppressWarnings("unused")
public abstract class Database {
    private final Logging log = new Logging(Database.class);
    private final Map<String, SqlQueryTable> tablesCache = new HashMap<>();
    private final ConnectionSettings connectionSettings;
    private Set<String> removeColumns = new HashSet<>();
    private DatabaseType databaseType = null;
    private char quote = '`';
    private String characterSet = "";
    private boolean secureQuery = true;
    private int maximumPoolSize;
    private long connectionTimeout;
    private long idleTimeout;
    private long maxLifeTime;
    private int minimumIdle;
    private BiConsumer<String, PrimaryConstraintWrapper> handleConstraints;

    /**
     * The  database instance.
     *
     * @param connectionSettings the settings for the connection.
     */
    protected Database(ConnectionSettings connectionSettings) {
        if (this instanceof SQLite) {
            this.databaseType = DatabaseType.SQLITE;
        }
        if (this instanceof MySQL) {
            this.databaseType = DatabaseType.MYSQL;
        }
        if (this instanceof H2DB) {
            this.databaseType = DatabaseType.H2;
        }
        if (this instanceof PostgreSQL) {
            this.databaseType = DatabaseType.POSTGRESQL;
            quote = ' ';
        }
        if (this instanceof MongoDB) {
            this.databaseType = DatabaseType.MONGO_DB;
        }
        if (this.databaseType == null)
            this.databaseType = DatabaseType.UNKNOWN;

        this.connectionSettings = connectionSettings;
    }

    /**
     * The connection to the database.
     *
     * @return the connection.
     */
    public abstract Connection connect();

    /**
     * Checks if HikariCP is being used as the connection method to the database.
     *
     * @return {@code true} if HikariCP is being used, otherwise {@code false}.
     */
    public abstract boolean usingHikari();

    /**
     * Checks whether the last attempt to connect to the database has failed.
     *
     * @return {@code true} if the connection failed; {@code false} otherwise.
     */
    public abstract boolean hasConnectionFailed();

    /**
     * @return {@code true} if a connection exception was detected; {@code false} otherwise.
     * @deprecated This method is outdated and only checks for connection exceptions.
     * Use {@link #hasConnectionFailed()} instead for clearer semantics.
     * <p>
     * Checks whether a connection exception has occurred.
     */
    @Deprecated
    public boolean isHasCastException() {
        return false;
    }

    /**
     * Retrieves the configuration settings for a specific database type. This includes settings
     * such as {@code resultSetType}, {@code resultSetConcurrency}, and the type of command
     * to execute when performing data updates or inserts.
     *
     * @return the {@link DatabaseCommandConfig} instance containing the database configuration settings.
     */
    @Nonnull
    public abstract DatabaseCommandConfig databaseConfig();

    /**
     * Add the tables you want to create inside the database and
     * don't forget at least 1 primary key.
     *
     * @param callback the helper to create your sql query.
     */
    public void addTable(Function<QueryBuilder, CreateTableHandler> callback) {
        SqlQueryTable sqlQueryTable = new SqlQueryTable(callback);

        this.tablesCache.put(sqlQueryTable.getTableName(), sqlQueryTable);
    }

    /**
     * Retrieves the {@link SqlQueryTable} associated with the given table name.
     * <p>
     * The lookup is case-sensitive.
     *
     * @param tableName the exact name of the table (case-sensitive).
     * @return the {@link SqlQueryTable} for the specified table, or {@code null} if no such table exists.
     */
    @Nullable
    public SqlQueryTable getTableFromName(String tableName) {
        return tablesCache.get(tableName);
    }

    /**
     * Create all needed tables if it not exist.
     */
    public void createTables() {
        this.createTables(null);
    }

    /**
     * Create all needed tables if it not exist.
     *
     * @param handleConstraints Use this if you set now columns with constraints.
     */
    public void createTables(final BiConsumer<String, PrimaryConstraintWrapper> handleConstraints) {
        this.handleConstraints = handleConstraints;
        if (tablesCache.isEmpty()) {
            log.log(() -> "You don't have added any tables, so it can't check or create your tables.");
            return;
        }
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }
        try {
            createAllTablesIfNotExist(connection);
            try {
                for (final Entry<String, SqlQueryTable> entityTables : tablesCache.entrySet()) {
                    final List<String> columns = updateTableColumnsInDb(connection, entityTables.getKey());
                    this.createMissingColumns(connection, entityTables.getValue(), columns);
                }
            } catch (final SQLException throwable) {
                log.log(throwable, () -> "Fail to update columns in your table.");
            }
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Saves all rows to the specified database table, based on the provided primary key and associated data.
     * <p>&nbsp;</p>
     * <p>
     * Note: If you use this method it will replace the old data instead of update it.
     *
     * @param tableName       name of the table you want to update rows inside.
     * @param dataWrapperList List of data you want to cache to database.
     *                        Note: the primary value you set has to be linked to the primary key you want to update.
     */
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList) {
        this.saveAll(tableName, dataWrapperList, false);
    }

    /**
     * Saves all rows to the specified database table, based on the provided primary key and associated data.
     * <p>&nbsp;</p>
     * <p>
     * Note: If you use this method it will replace the old data instead of update it.
     *
     * @param tableName       name of the table you want to update rows inside.
     * @param dataWrapperList List of data you want to cache to database.
     *                        Note: the primary value you set has to be linked to the primary key you want to update.
     * @param columns         Set the columns you only wants to update in the database. It will not null and not empty,
     *                        updates the existing row with these columns if it exists.
     */
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, String... columns) {
        this.saveAll(tableName, dataWrapperList, true, columns);
    }


    /**
     * Saves all rows to the specified database table, based on the provided primary key and associated data.
     *
     * @param tableName       name of the table you want to update rows inside.
     * @param dataWrapperList List of data you want to cache to database.
     *                        Note: the primary value you set has to be linked to the primary key you want to update.
     * @param shallUpdate     Set to true if you want to update the row, other wise it will replace the old row.
     * @param columns         Set the columns you only wants to update in the database. It will not null and not empty,
     *                        updates the existing row with these columns if it exists.
     */
    public abstract void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, String... columns);

    /**
     * Saves a single row to the specified database table. The row is identified by the
     * provided primary key and associated data.
     * <p>&nbsp;</p>
     * <p>
     * Note: If you use this method it will replace the old data instead of update it.
     * </p>
     *
     * @param tableName   The name of the table to save the row to.
     * @param dataWrapper The wrapper with the set values, for primaryKey, primaryValue and serialize data.
     */
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper) {
        this.save(tableName, dataWrapper, false);
    }

    /**
     * Saves a single row to the specified database table. The row is identified by the
     * provided primary key and associated data.
     *
     * @param tableName   The name of the table to save the row to.
     * @param dataWrapper The wrapper with the set values, for primaryKey, primaryValue and serialize data.
     * @param columns     Set the columns you only wants to update in the database. It will not null and not empty,
     *                    updates the existing row with these columns if it exists.
     */
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, String... columns) {
        this.save(tableName, dataWrapper, true, columns);
    }

    /**
     * Saves a single row to the specified database table. The row is identified by the
     * provided primary key and associated data.
     *
     * @param tableName   The name of the table to save the row to.
     * @param dataWrapper The wrapper with the set values, for primaryKey, primaryValue and serialize data.
     * @param shallUpdate Set to true if you want to update the row, other wise it will replace the old row.
     * @param columns     Set the columns you only wants to update in the database. It will not null and not empty,
     *                    updates the existing row with these columns if it exists.
     */
    public abstract void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, String... columns);

    /**
     * Prepares an operation to save one or more rows to a SQL database table.
     * <p>
     * <b>Note:</b> This method currently supports only SQL databases.
     * It performs a <i>blocking</i> query, so it's strongly recommended to call
     * {@link QuerySaver#save()} in a separate thread to avoid blocking the main thread.
     * </p>
     *
     * <p>
     * You specify the target table and a map of values to save. The table does <b>not</b>
     * need to be registered in this library — no validation is performed against known tables.
     * </p>
     *
     * <p>
     * The values in the map must implement {@link ConfigurationSerializable}, and the keys are up to you.
     * These keys will be available inside the query logic through {@link QuerySaver#forEachQuery(Consumer)},
     * where you can configure the update logic and define the WHERE clause.
     * </p>
     *
     * <p>
     * You must call {@link QuerySaver#save()} after configuring your query logic using {@code forEachQuery}.
     * </p>
     *
     * @param tableName   The name of the target table. It must exist in the database, but does not need to be registered in this library.
     * @param cacheToSave A map of data to save. The values must implement {@link ConfigurationSerializable}.
     *                    The keys are available during query setup via {@link QuerySaver#forEachQuery(Consumer)}.
     * @param saveSetup   Provides access to define primary keys, WHERE clauses, and column filters for updates.
     * @param <K>         The type of keys used in your cache map.
     * @param <V>         The type of values, which must implement {@link ConfigurationSerializable}.
     * @return a {@link QuerySaver} instance used to configure and execute the query, and to access results if needed.
     * @throws UnsupportedOperationException if this database type does not support save operations.
     */
    @Nonnull
    public <K, V extends ConfigurationSerializable> QuerySaver<K, V> save(@Nonnull final String tableName, @Nonnull final Map<K, V> cacheToSave, @Nonnull final Consumer<SaveSetup> saveSetup) {
        throw new UnsupportedOperationException("This function is not implemented for this database type yet." + this);
    }

    /**
     * Load all rows from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param clazz     the class you have your static deserialize method.
     * @param <T>       the type of ConfigurationSerialize instance.
     * @return list of all data you have in the table.
     */
    @Nullable
    public abstract <T extends ConfigurationSerializable> List<LoadDataWrapper<T>> loadAll(@Nonnull final String tableName, @Nonnull final Class<T> clazz);

    /**
     * Loads a single row from the specified database table.
     *
     * @param tableName   the name of the table to retrieve data from.
     * @param clazz       the class containing the static deserialize method.
     * @param columnValue the primary key value used to find the data.
     * @param <T>         a class that extends {@code ConfigurationSerializable}.
     * @return the retrieved row from the table, or {@code null} if no data is found.
     */
    @Nullable
    public abstract <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final String columnValue);

    /**
     * Loads one or more rows from the specified database table.
     * <p>
     * This method performs a blocking database query, so it is strongly recommended
     * to invoke {@link QueryLoader#load()} in a separate thread to avoid blocking the main thread.
     * </p>
     * <p>
     * Use the {@link QueryLoader} instance to define how rows should be handled during loading
     * via {@link QueryLoader#forEachQuery(Consumer)}, or to retrieve the full list of loaded results afterward.
     * </p>
     *
     * @param tableName the name of the table to query.
     * @param clazz     the class type that must provide a static {@code deserialize} method.
     * @param setup     a consumer to configure optional filtering and query conditions.
     * @param <T>       the type that implements {@link ConfigurationSerializable}.
     * @return a {@link QueryLoader} instance for configuring, triggering, and retrieving query results.
     * @throws UnsupportedOperationException if this operation is not implemented for the current database type.
     */
    @Nonnull
    public <T extends ConfigurationSerializable> QueryLoader<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final Consumer<LoadSetup<T>> setup) {
        throw new UnsupportedOperationException("This function is not implemented for this database type yet." + this);
    }

    /**
     * Remove all rows from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param values    the list of primary key values you want to remove from database.
     */
    public void removeAll(@Nonnull final String tableName, @Nonnull final List<String> values) {
        final BatchExecutor<DataWrapper> batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor<>(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe<>(this, connection, new ArrayList<>());
        }

        final SqlQueryTable table = this.getTableFromName(tableName);
        if (table == null) {
            this.log.log(Level.WARNING, () -> "Could not find this table:'" + tableName + "' when attempting to remove your list of primary values. Did you register your table?");
            return;
        }
        batchExecutor.removeAll(tableName, values, table::createWhereClauseFromPrimaryColumns);
    }

    /**
     * Remove one row from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param value     the primary key value you want to remove from database.
     */
    public void remove(@Nonnull final String tableName, @Nonnull final String value) {
        BatchExecutor<DataWrapper> batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor<>(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe<>(this, connection, new ArrayList<>());
        }
        final SqlQueryTable table = this.getTableFromName(tableName);

        if (table == null) {
            this.log.log(Level.WARNING, () -> "Could not find this table:'" + tableName + "' when attempting to remove your list of primary values. Did you register your table?");
            return;
        }
        batchExecutor.remove(tableName, value, table::createWhereClauseFromPrimaryColumns);
    }

    /**
     * Drop the table.
     *
     * @param tableName the name of the table to drop.
     */
    public void dropTable(final String tableName) {
        BatchExecutor<DataWrapper> batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor<>(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe<>(this, connection, new ArrayList<>());
        }

        batchExecutor.dropTable(tableName);
    }

    /**
     * Checks if a row exists in the specified table.
     * <p>&nbsp;</p>
     * <p>It is recommended to use {@link #load(String, Class, String)} or {@link #loadAll(String, Class)}
     * if you also need to load the data, as a simple null check or checking if the list is empty is often sufficient.</p>
     * <p>&nbsp;</p>
     * <p>For save operations, running this check beforehand is unnecessary, as the SQL command
     * automatically determines whether the value exists and executes the appropriate command.</p>
     *
     * @param tableName       the name of the table to search for the data.
     * @param primaryKeyValue the primary key value to look for in the table.
     * @return {@code true} if the key exists in the table, or {@code false} if the data is not found
     * or a connection issue occurs.
     */
    public boolean doRowExist(@Nonnull String tableName, @Nonnull Object primaryKeyValue) {
        BatchExecutor<DataWrapper> batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return false;
        }
        if (this.secureQuery)
            batchExecutor = new BatchExecutor<>(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe<>(this, connection, new ArrayList<>());
        }
        final SqlQueryTable table = this.getTableFromName(tableName);
        if (table == null) {
            log.log(() -> "Could not find this table: '" + tableName + "'");
            return false;
        }
        return batchExecutor.checkIfRowExist(tableName, whereClause -> table.createWhereClauseFromPrimaryColumns(whereClause, "'" + primaryKeyValue + "'"));
    }

    /**
     * This method removes rows older than the set threshold.
     *
     * @param tableName the table to find the rows to remove.
     * @param column    the name of the column to remove.
     * @param quotes    The quotes around the table name.
     * @param threshold the threshold where it should start to remove and below.
     */
    public void removeBelowThreshold(@Nonnull final String tableName, @Nonnull final String column, char quotes, int threshold) {

    }


    /**
     * Get the type of quote, it currently uses around
     * the key and also the table name.
     *
     * @return the type quote currently set.
     */
    public char getQuote() {
        return quote;
    }

    /**
     * Set the quote around columns name and on the table name.
     * set to empty string if you not want any quotes. If you not
     * set this it will use a default quote.
     *
     * @param quote the quote around the columns name and on the table name.
     */
    public void setQuote(final char quote) {
        this.quote = quote;
    }

    /**
     * Retrieve the character set declaration used for defining
     * the character set of text-based columns in a table.
     *
     * @return the character set declaration.
     */
    public String getCharacterSet() {
        return this.characterSet;
    }

    /**
     * Sets the character set declaration parameter when creating a table for the first time.
     * Note: The character set declaration specifies the desired character set for text-based columns in a table.
     * Keep in mind that not all databases support this option; compatibility may vary.
     *
     * @param characterSet the desired character set for text-based columns (e.g., "utf8mb4", "UTF-8").
     */
    public void setCharacterSet(@Nonnull final String characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Try to close the connection.
     *
     * @param preparedStatement the database statement to close.
     */
    protected void close(final PreparedStatement... preparedStatement) {
        if (preparedStatement == null) return;
        for (final PreparedStatement statement : preparedStatement)
            close(statement, null);
    }

    /**
     * Try to close the connection.
     *
     * @param preparedStatement the database statement to close.
     * @param resultSet         the database result set to close.
     */
    public void close(final PreparedStatement preparedStatement, final ResultSet resultSet) {
        try {
            if (preparedStatement != null) preparedStatement.close();
            if (resultSet != null) resultSet.close();
        } catch (final SQLException ex) {
            log.log(ex, () -> "Fail to close preparedStatement.");
        }
    }

    /**
     * Update the table, if it missing a column or columns.
     *
     * @param connection the connection to the database where the changes should be added.
     * @param tableName  the table you want to check.
     * @return list of columns added in the database.
     * @throws SQLException if something going wrong.
     */
    protected List<String> updateTableColumnsInDb(final Connection connection, final String tableName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            final List<String> column = new ArrayList<>();
            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.select(ColumnManager.of().column("*").finish()).from(tableName);
            final String queryAllColumns = queryBuilder.build();
            statement = connection.prepareStatement(queryAllColumns);
            rs = statement.executeQuery();
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                column.add(rsmd.getColumnName(i).toLowerCase());
            }
            return column;
        } finally {
            close(statement, rs);
        }
    }

/*    protected void dropColumn(final List<String> existingColumns, final String tableName) throws SQLException {
        Connection connection = this.connect();
        if (connection == null) {
            this.printFailToOpen();
            return;
        }
        final SqlQueryTable updatedTableColumns = this.getTableFromName(tableName);

        if (updatedTableColumns == null || updatedTableColumns.getTable().getColumns().isEmpty()) return;
        // Remove the columns we don't want anymore from the table's list of columns

        if (this.removeColumns != null) for (final String removed : this.removeColumns) {
            existingColumns.remove(removed);
        }
        final String columnsSeparated = getColumnsFromTable(updatedTableColumns.getTable().getColumns());

        PreparedStatement moveData = null;
        PreparedStatement alterTable = null;
        PreparedStatement createTable = null;
        PreparedStatement removeOldTable = null;
        try {    // Rename the old table, so we can remove old name and rename columns.
            final String alterTableQuery = "ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;";
            alterTable = connection.prepareStatement(alterTableQuery);
            // Creating the table on its new format (no redundant columns)
            createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsSeparated + ");");

            alterTable.execute();
            createTable.execute();

            // Populating the table with the data
            moveData = connection.prepareStatement("INSERT INTO " + tableName + "(" + textUtils(existingColumns) + ") SELECT " + textUtils(existingColumns) + " FROM " + tableName + "_old;");
            moveData.execute();

            removeOldTable = connection.prepareStatement("DROP TABLE " + tableName + "_old;");
            removeOldTable.execute();
        } finally {
            close(moveData, alterTable, createTable, removeOldTable);
        }
    }*/

    private void createMissingColumns(final Connection connection, final SqlQueryTable queryTable, final List<String> existingColumns) {
        if (existingColumns == null) return;
        if (connection == null) {
            log.log(Level.WARNING, () -> "You must set the connection instance.");
            return;
        }
        final Set<String> newPrimaryKeys = new HashSet<>();
        final List<Column> columnsToAdd = new ArrayList<>();
        boolean failCreateColumns = false;

        for (final Column column : queryTable.getTable().getColumns()) {
            String columnName = column.getColumnName();
            if (removeColumns.contains(columnName) || existingColumns.contains(columnName.toLowerCase())) continue;
            Column tableColumn = column;

            if (column instanceof TableColumn) {
                final boolean isPrimaryKey = ((TableColumn) column).isPrimaryKey();
                if (isPrimaryKey) {
                    newPrimaryKeys.add(columnName);
                    tableColumn = new TableColumn(null, column.getColumnName(), ((TableColumn) column).getDataType());
                }
            }
            columnsToAdd.add(tableColumn);
        }


        if (!columnsToAdd.isEmpty()) {
            failCreateColumns = executeCreation(connection, queryTable, columnsToAdd, failCreateColumns);
        }
        if (failCreateColumns) {
            log.log(Level.SEVERE, () -> "Schema migration aborted for " + queryTable.getTableName() +
                    " due to previous errors. The constraints will not be set.");
            return;
        }

        this.preparePrimaryKeyMigration(connection, queryTable, newPrimaryKeys);
    }

    private boolean executeCreation(@Nonnull final Connection connection, @Nonnull final SqlQueryTable queryTable, @Nonnull final List<Column> columnsToAdd, @Nonnull boolean failCreateColumns) {
        if (this.databaseType == DatabaseType.SQLITE) {
            for (Column col : columnsToAdd) {
                final QueryBuilder queryBuilder = new QueryBuilder();
                final AlterTable alterBuilder = queryBuilder.alterTable(queryTable.getTableName());
                alterBuilder.add(col);
                final String query = queryBuilder.build();
                try (final PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.execute();
                    log.log(Level.FINE, () -> "Successfully added column " + col.getColumnName() + " to " + queryTable.getTableName());
                } catch (final SQLException throwable) {
                    log.log(throwable, () -> "Failed to add missing column " + col.getColumnName() + " . Query: '" + query + "'");
                    failCreateColumns = true;
                }
            }
        } else {
            final QueryBuilder queryBuilder = new QueryBuilder();
            final AlterTable alterBuilder = queryBuilder.alterTable(queryTable.getTableName());
            for (Column col : columnsToAdd) {
                alterBuilder.add(col);
            }
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
                log.log(Level.FINE, () -> "Successfully added " + columnsToAdd.size() + " columns to " + queryTable.getTableName());
            } catch (final SQLException throwable) {
                log.log(throwable, () -> "Failed to add missing columns in batch. Query: '" + query + "'");
                failCreateColumns = true;
            }
        }
        return failCreateColumns;
    }

    private void preparePrimaryKeyMigration(final Connection connection, final SqlQueryTable queryTable, final Set<String> newPrimaryKeys) {
        if (newPrimaryKeys.isEmpty()) {
            log.log(Level.FINE, () -> "No new primary key columns detected. Skipping primary key migration for table '" + queryTable.getTableName() + "'");
            return;
        }
        Validate.checkNotNull(this.handleConstraints, "Constraint handler not configured. You must provide a callback to define how constraints should be applied to newly created columns.");
        final PrimaryConstraintWrapper primaryWrapper = new PrimaryConstraintWrapper(this, queryTable);
        this.handleConstraints.accept(queryTable.getTableName(), primaryWrapper);

        final QueryBuilder builder = new QueryBuilder();
        builder.select(new ColumnManager().column("*").finish()).from(queryTable.getTableName());
        final String builtQuery = builder.build();
        try (final ResultSet resultSet = connection.prepareStatement(builtQuery).executeQuery()) {
            while (resultSet.next()) {
                final CreateTableHandler tableHandler = queryTable.getTable();
                final Map<String, Object> dataFromDB = getDataFromDB(resultSet, tableHandler.getColumns());
                primaryWrapper.loadMap(dataFromDB);
            }
        } catch (final SQLException throwable) {
            log.log(throwable, () -> "Failed to read existing rows while preparing primary key migration. The query '" + builtQuery + "' and this table '" + queryTable.getTableName() + "'");
        }
        Validate.checkBoolean(!primaryWrapper.isSet() && !primaryWrapper.isUnique(), "Primary key creation requested, but required values are missing. Provide values for all primary key columns or configure UNIQUE fallback instead.");

        boolean primaryMapValuesSet = saveDataToColumns(connection, queryTable, primaryWrapper);
        Validate.checkBoolean(!primaryMapValuesSet && !primaryWrapper.isUnique(), "Primary key creation failed. One or more primary columns contain null " +
                "values, and UNIQUE fallback is disabled. Either provide values for all primary columns or enable UNIQUE fallback.");

        boolean primaryValuesComplete = primaryWrapper.allPrimaryValuesPresent(newPrimaryKeys);
        Validate.checkBoolean(!primaryValuesComplete && !primaryWrapper.isUnique(), "Primary key creation failed. Not all columns marked as primary received values during migration. Ensure all PRIMARY KEY columns are populated, or enable UNIQUE fallback. ");

        this.setConstraints(connection, queryTable, newPrimaryKeys, primaryMapValuesSet && primaryValuesComplete);
    }

    private boolean saveDataToColumns(final Connection connection, final SqlQueryTable queryTable, final PrimaryConstraintWrapper primaryWrapper) {
        boolean primaryMapValuesSet = true;
        final Map<String, List<Map<Integer, Object>>> batchGroups = new LinkedHashMap<>();

        if (!primaryWrapper.getPrimaryWrappers().isEmpty()) {
            primaryMapValuesSet = setValuesToDatabase(queryTable, primaryWrapper, primaryMapValuesSet, batchGroups);
        }

        if (!batchGroups.isEmpty()) {
            for (Map.Entry<String, List<Map<Integer, Object>>> entry : batchGroups.entrySet()) {
                String sql = entry.getKey();
                List<Map<Integer, Object>> allRowsParams = entry.getValue();

                try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    for (Map<Integer, Object> rowParams : allRowsParams) {
                        for (Map.Entry<Integer, Object> param : rowParams.entrySet()) {
                            preparedStatement.setObject(param.getKey(), param.getValue());
                        }
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                } catch (final SQLException throwable) {
                    log.log(throwable, () -> "Failed to populate primary key values. SQL: '" + sql + "'. Table: '" + queryTable.getTableName() + "'");
                }
            }
        }

        return primaryMapValuesSet;
    }

    private boolean setValuesToDatabase(final SqlQueryTable queryTable, final PrimaryConstraintWrapper primaryWrapper, boolean primaryMapValuesSet, final Map<String, List<Map<Integer, Object>>> batchGroups) {
        for (DataWrapper.PrimaryWrapper primary : primaryWrapper.getPrimaryWrappers()) {
            if (primary == null) {
                log.log(Level.WARNING, () -> "A row for this table '" + queryTable.getTableName() + "' is not set.");
                continue;
            }

            final QueryBuilder saveBuilder = new QueryBuilder();
            final Map<String, Object> primaryKeys = primary.getPrimaryKeys();

            if (primaryKeys.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
                sendLogMessage(primaryWrapper, primaryKeys);
                primaryMapValuesSet = false;
            }
            Selector<ColumnBuilder<Column, Void>, Column> update = saveBuilder
                    .update(queryTable.getTableName())
                    .putAll(primaryWrapper.convert(primaryKeys))
                    .getSelector()
                    .where(whereBuilder -> {
                        if (primary.getWhereClause() == null)
                            return null;
                        return primary.getWhereClause().apply(whereBuilder);
                    });
            if (update.getWhereBuilder() == null) {
                log.log(Level.WARNING, () -> "Update skipped, no WHERE clause was provided. For this table '" + queryTable.getTableName() + "'" + ". Updates without a WHERE clause are not allowed for safety reasons.");
            } else {
                String sql = saveBuilder.build();
                batchGroups.computeIfAbsent(sql, k -> new ArrayList<>()).add(saveBuilder.getValues());
            }
        }
        return primaryMapValuesSet;
    }


    private void setConstraints(final Connection connection, final SqlQueryTable queryTable, final Set<String> newPrimaryKeys, final boolean primaryValuesComplete) {
        final List<Column> columnsToBeModified = new ArrayList<>();
        final List<Column> primaryColumns = queryTable.getPrimaryColumns();

        for (final Column column : primaryColumns) {
            boolean addingPrimary = false;
            if (primaryValuesComplete) {
                columnsToBeModified.add(column);
                addingPrimary = true;
            }
            if (addingPrimary || !newPrimaryKeys.contains(column.getColumnName())) {
                continue;
            }
            columnsToBeModified.add(column);
        }

        if (!columnsToBeModified.isEmpty() && databaseType == DatabaseType.SQLITE) {
            copyTable(connection, queryTable, columnsToBeModified);
            return;
        }
        final String tableName = queryTable.getTableName();

        if (!columnsToBeModified.isEmpty() && !primaryValuesComplete) {
            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.alterTable(tableName).setConstraints(modifyConstraints ->
                    modifyConstraints.addUnique(columnsToBeModified.stream().map(Column::getColumnName).toArray(String[]::new)));
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> getMessage("Failed to apply UNIQUE constraint during primary key migration. Columns '", tableName, columnsToBeModified));
            }
        }
        if (!columnsToBeModified.isEmpty() && primaryValuesComplete) {
            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.alterTable(tableName).setConstraints(modifyConstraints -> {
                modifyConstraints.dropPrimaryKey();
                modifyConstraints.addPrimaryKey(columnsToBeModified.stream().map(Column::getColumnName).toArray(String[]::new));
            });
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> getMessage("Failed to apply PRIMARY KEY constraint during migration. Columns ", tableName, columnsToBeModified));
            }
        }
    }

    private void copyTable(@Nonnull final Connection connection, @Nonnull final SqlQueryTable queryTable, @Nonnull final List<Column> columnsToBeModified) {
        boolean autoCommit = false;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            recreateTable(connection, queryTable, columnsToBeModified);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.log(ex, () -> "could not rollback the changes");
            }
            log.log(e, () -> "Failed to change the contains on the SSQLite database.");
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                log.log(e, () -> "Failed to set auto commit back on the SSQLite database.");
            }
        }
    }

    private void recreateTable(final Connection connection, final SqlQueryTable queryTable, final List<Column> columnsToBeModified) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final String tableName = queryTable.getTableName();
        final String temporaryTable = tableName + "_new";

        queryBuilder.createTable(temporaryTable).addAllColumns(queryTable.getColumns());
        final String query = queryBuilder.build();
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to create table during primary key migration. Columns ", tableName , columnsToBeModified));
        }

        final QueryBuilder queryInsertBuilder = new QueryBuilder();
        queryInsertBuilder.insertInto(temporaryTable, insertHandler -> {
            insertHandler.addAll(queryTable.getColumns()).getQueryModifier()
                    .select(columnBuilder ->
                            columnBuilder.addAll(queryTable.getColumns()))
                    .from(tableName);
        });
        final String insertQuery = queryInsertBuilder.build();
        try (final PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to create table during primary key migration. Query ", tableName, insertQuery));
        }
        //todo handle when the the the primary column is an index.
        // updateIndex(connection, tableName);

        final QueryBuilder queryDropBuilder = new QueryBuilder();
        queryDropBuilder.dropTable(tableName);
        final String dropQuery = queryDropBuilder.build();
        try (final PreparedStatement statement = connection.prepareStatement(dropQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to drop table during primary key migration. Query ", tableName, dropQuery));
        }

        final QueryBuilder queryAlterBuilder = new QueryBuilder();
        queryAlterBuilder.alterTable(temporaryTable).rename(tableName);
        final String alterQuery = queryAlterBuilder.build();
        try (final PreparedStatement statement = connection.prepareStatement(alterQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to alter table during primary key migration. Query ", tableName, alterQuery));
        }
    }

    private void updateIndex(final Connection connection, final String tableName) {

        final QueryBuilder incrementIndexBuilder = new QueryBuilder();
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", ""));
        columns.add(new Column("seq", ""));

        incrementIndexBuilder.insertOrReplaceInto("sqlite_sequence", insertHandler -> insertHandler.addAll(columns)
                .getQueryModifier()
                .select(columnBuilder -> {
                    columnBuilder.add(new Column("'" + tableName + "'", ""));
                    columnBuilder.add(new Column("id", "").setAggregation()
                            .withAggregation(CalcFunc.MAX).getColumn());
                })
                .from(tableName)
        );

        try (final PreparedStatement statement = connection.prepareStatement(incrementIndexBuilder.build())) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to update index during primary key migration. Query ", tableName,  incrementIndexBuilder));
        }
    }


    /**
     * Create all tables if it not exist yet, will only create a table if it not already exist.
     *
     * @param connection the connection to the database where the changes should be added.
     */
    protected void createAllTablesIfNotExist(final Connection connection) {
        if (tablesCache.isEmpty()) return;
        for (final Entry<String, SqlQueryTable> sqlQueryTable : tablesCache.entrySet()) {
            this.createTableIfNotExist(connection, sqlQueryTable);
        }
    }

    private void createTableIfNotExist(Connection connection, Entry<String, SqlQueryTable> sqlQueryTable) {
        PreparedStatement statement = null;
        String table = "";
        if (sqlQueryTable == null) {
            this.printFailFindTable("'table is null'");
            return;
        }

        final SqlQueryTable tableQuery = sqlQueryTable.getValue();
        try {
            if (tableQuery != null) {
                if (tableQuery.getTableName().isEmpty())
                    return;
                table = tableQuery.createTable();
                statement = connection.prepareStatement(table);
                statement.executeUpdate();
                Column column = tableQuery.getPrimaryColumns().stream().findFirst().orElse(null);
                Validate.checkNotNull(column, "Could not find a primary column for this table " + tableQuery.getTableName());
                checkIfTableExist(connection, tableQuery.getTableName(), column.getColumnName());
            }
        } catch (final SQLException e) {
            log.log(() -> "Something not working when try create this table: '" + tableQuery.getTableName() + "'");
            final String finalTable = table;
            log.log(e, () -> "With this command: " + finalTable);
        } finally {
            close(statement);
        }
    }


    /**
     * Create table if it not exist yet, will only create a table if it not already exist.
     *
     * @param connection the connection to the database where the changes should be added.
     * @param tableName  the name of the table to create.
     * @return true if it could check if the table exist or create now table.
     */
    protected boolean createTableIfNotExist(final Connection connection, final String tableName) {

        PreparedStatement statement = null;
        String table = "";
        try {

            final SqlQueryTable tableQuery = this.getTableFromName(tableName);
            if (tableQuery == null) {
                this.printFailFindTable(tableName);
                return false;
            }

            if (tableQuery.getTableName().isEmpty())
                return false;
            statement = connection.prepareStatement(tableQuery.createTable());
            statement.executeUpdate();
            Column column = tableQuery.getPrimaryColumns().stream().findFirst().orElse(null);
            Validate.checkNotNull(column, "Could not find a primary column for this table " + tableName);
            checkIfTableExist(connection, tableName, column.getColumnName());
            return true;

        } catch (final SQLException e) {
            log.log(() -> "Something not working when try create this table: '" + tableName + "'");
            final String finalTable = table;
            log.log(e, () -> "With this command: " + finalTable);
            return false;
        } finally {
            close(statement);
        }
    }

    /**
     * Trying to connect to the database.
     *
     * @return the connection {@code null} if it fails.
     */
    @Nullable
    public Connection attemptToConnect() {
        Connection connection = this.connect();
        if (hasConnectionFailed()) {
            this.printFailConnect();
            if (connection == null) {
                this.printFailToOpen();
                return null;
            }
            return connection;
        }
        return connection;
    }

    /**
     * Checks whether a given table exists and contains a specific column.
     * <p>
     * This method runs a simple {@code SELECT} query against the specified table
     * using the provided column name. It is primarily used as a lightweight
     * existence check — if the query fails, it is assumed that the table or column
     * does not exist.
     * <p>
     * Note: This method does not return a value; instead, it logs a warning if
     * the check fails. It is intended for internal validation rather than
     * production-grade existence checks.
     *
     * @param connection the active JDBC connection to use for the check (must not be {@code null}).
     * @param tableName  the name of the table to check.
     * @param columName  the name of the column to check within the table.
     */
    private void checkIfTableExist(@Nonnull final Connection connection, String tableName, String columName) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.select(ColumnManager.of().column("*").finish()).from(tableName).where(where -> where.where(columName).equal(""));
        final String checkTableQuery = queryBuilder.build();

        try (final PreparedStatement preparedStatement = connection.prepareStatement(checkTableQuery)) {
            preparedStatement.setString(1, "");
            final ResultSet resultSet = preparedStatement.executeQuery();
            close(preparedStatement, resultSet);

        } catch (final SQLException ex) {
            log.log(ex, () -> "Unable to check if table is created.");
        }
    }

    /**
     * Converts a list of {@link Column} objects into a single space-separated
     * string of column names.
     *
     * @param columns the list of columns to extract names from.
     * @return a string containing all column names separated by spaces.
     */
    protected String getColumnsFromTable(final List<Column> columns) {
        final StringBuilder columRow = new StringBuilder();
        for (final Column colum : columns) {
            columRow.append(colum.getColumnName()).append(" ");
        }
        return columRow.toString();
    }

    /**
     * Joins a list of strings into a single comma-separated string without
     * enclosing brackets.
     *
     * @param columns the list of strings to join.
     * @return the joined string, with elements separated by commas.
     */
    protected String textUtils(final List<String> columns) {
        return columns.toString().replace("[", "").replace("]", "");
    }

    /**
     * Indicates whether writing to the database has started.
     * <p>
     * This implementation always returns {@code false}, and should be overridden
     * if actual state tracking is needed.
     *
     * @return {@code false} by default.
     */
    public boolean isHasStartWriteToDb() {
        return false;
    }

    /**
     * Retrieves the set of columns scheduled for removal.
     *
     * @return the set of column names to be removed.
     */
    public Set<String> getRemoveColumns() {
        return removeColumns;
    }

    /**
     * Sets the collection of columns to be removed.
     *
     * @param removeColumns the set of column names to mark for removal.
     */
    public void setRemoveColumns(final Set<String> removeColumns) {
        this.removeColumns = removeColumns;
    }

    /**
     * Gets the database type currently configured for this utility.
     *
     * @return the active {@link DatabaseType}.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Retrieves the cached mapping of table names to their associated SQL query definitions.
     *
     * @return a map where keys are table names and values are {@link SqlQueryTable} objects.
     */
    @Nonnull
    public Map<String, SqlQueryTable> getTables() {
        return tablesCache;
    }

    /**
     * Opens a new database connection using the configured settings.
     *
     * @return a new {@link Connection} instance, or {@code null} if the connection could not be established.
     */
    @Nullable
    public Connection openConnection() {
        return connect();
    }

    /**
     * Closes the given database connection if it is not {@code null}.
     *
     * @param connection the connection to close.
     */
    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException exception) {
            log.log(Level.WARNING, exception, () -> "Something went wrong, when attempt to close connection.");
        }
    }

    /**
     * Determines whether secure query execution is enabled.
     *
     * @return {@code true} if secure query mode is active, otherwise {@code false}.
     */
    public boolean isSecureQuery() {
        return secureQuery;
    }

    /**
     * Sets whether to perform a secure batch update. When set to false, the class will not apply
     * measures to prevent SQL injection vulnerabilities during batch updates.
     * <p>
     * It is strongly recommended to leave this setting as true (secure) to ensure safe query execution.
     *
     * @param secureQuery Pass true to enable secure batch updates, or false to disable security measures.
     */
    public final void setSecureQuery(final boolean secureQuery) {
        this.secureQuery = secureQuery;
    }


    /**
     * Converts the data retrieved from the database and puts it into a map. Some databases
     * may return column names in uppercase, and this method uses {@link #getColumnName(List, String)} )}
     * to correct the keys in the map based on the column names set when you created the table.
     *
     * @param resultSet The ResultSet object representing the cursor to retrieve the data set.
     * @param columns   The list of columns you want to check against column names.
     * @return A map containing the column values from the database, with corrected keys based on the
     * column names in the TableWrapper.
     * @throws SQLException If there is an issue reading data from the database.
     */
    public Map<String, Object> getDataFromDB(final ResultSet resultSet, List<Column> columns) throws SQLException {
        final ResultSetMetaData rsmd = resultSet.getMetaData();
        final int columnCount = rsmd.getColumnCount();
        final Map<String, Object> objectMap = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            objectMap.put(this.getColumnName(columns, rsmd.getColumnName(i)), resultSet.getObject(i));
        }
        return objectMap;
    }

    /**
     * Retrieves the correct column name for the given table from the TableWrapper.
     * Some databases may return column names in uppercase, so this method allows you
     * to fetch the column name with the appropriate casing as set in the TableWrapper.
     *
     * @param columns    The list of columns to check for a match.
     * @param columnName The name of the column to retrieve.
     * @return The column name with the correct case as set in the TableWrapper. If there
     * is no matching column name in the TableWrapper, it returns the columnName
     * you set in the second argument.
     */
    private String getColumnName(List<Column> columns, String columnName) {
        if (!columns.isEmpty())
            for (Column column : columns) {
                if (column.getColumnName().equalsIgnoreCase(columnName)) return column.getColumnName();
            }

        return columnName;
    }

    /**
     * Checks if a database driver class exists using reflection.
     *
     * @param path the fully qualified class name of the driver to check.
     * @return {@code true} if the driver class is found; {@code false} otherwise.
     */
    public boolean isDriverFound(final String path) {
        try {
            Class.forName(path);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Attempts to load the database driver class.
     * <p>
     * If the driver is not found, logs a warning message.
     *
     * @param path the fully qualified class name of the driver to load.
     */
    public void loadDriver(final String path) {
        if (!isDriverFound(path))
            log.log(() -> "Could not load this driver: " + path);
    }

    /**
     * Retrieve the current maximum size of the connection pool.
     *
     * @return the maximum size of the connection pool, or a default value if not explicitly set.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Set the maximum size of the connection pool.
     * <p>
     * Note: Does currently only works with this connection pool {@link HikariCP}.
     *
     * @param poolSize the maximum size of the connection pool.
     */
    public void setMaximumPoolSize(final int poolSize) {
        this.maximumPoolSize = poolSize;
    }

    /**
     * Retrieve the current connection timeout in milliseconds.
     *
     * @return the current connection timeout in milliseconds, or a default value if not explicitly set.
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set the maximum time (in seconds) to wait for a connection to be established.
     * <p>
     * Note: Does currently only works with this connection pool {@link HikariCP}.
     *
     * @param timeout the maximum connection timeout in seconds.
     */
    public void setConnectionTimeout(final int timeout) {
        this.connectionTimeout = timeout * 1000L;
    }

    /**
     * Retrieve the current idle timeout in milliseconds.
     *
     * @return the current idle timeout in milliseconds, or a default value if not explicitly set.
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Set the maximum time (in seconds) a connection can remain idle before it is eligible for eviction.
     * <p>
     * Note: Does currently only works with this connection pool {@link HikariCP}.
     *
     * @param idleTimeout the timeout in seconds.
     */
    public void setIdleTimeout(final int idleTimeout) {
        this.idleTimeout = idleTimeout * 1000L;
    }

    /**
     * Retrieve the minimum number of idle connections the pool tries to maintain,
     * including both idle and in-use connections.
     *
     * @return the minimum number of connections in the pool.
     */
    public int getMinimumIdle() {
        return minimumIdle;
    }

    /**
     * The property controls the minimum number of idle connections that HikariCP tries to maintain in
     * the pool, including both idle and in-use connections. If the idle connections dip below this
     * value, HikariCP will make a best effort to restore them quickly and efficiently.
     * <p>
     * Note: Does currently only works with this connection pool {@link HikariCP}.
     *
     * @param minimumIdle the minimum number of idle connections in the pool to maintain
     */
    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    /**
     * This property controls the maximum lifetime of a connection in the pool. When a connection reaches
     * this timeout, even if recently used, it will be retired from the pool. An in-use connection will
     * never be retired, only when it is idle will it be removed.
     *
     * @return the maximum connection lifetime in milliseconds.
     */
    public long getMaxLifeTime() {
        return maxLifeTime;
    }

    /**
     * This property controls the maximum lifetime of a connection in the pool. When a connection reaches
     * this timeout, even if recently used, it will be retired from the pool. An in-use connection
     * will never be retired, only when it is idle will it be removed.
     * <p>
     * Note: Does currently only works with this connection pool {@link HikariCP}.
     *
     * @param maxLifeTime the maximum connection lifetime in seconds
     */
    public void setMaxLifeTime(long maxLifeTime) {
        this.maxLifeTime = maxLifeTime * 1000L;
    }

    /**
     * Retrieve the settings used to connect to the database.
     *
     * @return the settings that should be used to connect.
     */
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }


    /**
     * Deserializes the given data into an instance of the specified class.
     * <p>
     * The target class must implement {@link ConfigurationSerializable} and provide
     * either a static {@code deserialize(Map<String, Object>)} method or a static
     * {@code valueOf(Map<String, Object>)} method for reconstruction.
     *
     * @param <T>            the type of object to deserialize, which must extend {@link ConfigurationSerializable}.
     * @param clazz          the class type to instantiate (must extend {@link ConfigurationSerializable}).
     * @param serializedData the serialized key-value data to be used for deserialization.
     * @return an instance of {@code T} populated from the serialized data.
     */
    public <T extends ConfigurationSerializable> T deSerialize(@Nonnull final Class<T> clazz, @Nonnull final Map<String, Object> serializedData) {
        Method deserializeMethod = MethodReflectionUtils.getMethod(clazz, "deserialize", Map.class);
        if (deserializeMethod == null)
            deserializeMethod = MethodReflectionUtils.getMethod(clazz, "valueOf", Map.class);
        return MethodReflectionUtils.invokeStaticMethod(clazz, deserializeMethod, serializedData);
    }

    /**
     * Print a message when can't open connection
     */
    public void printFailToOpen() {
        log.log(Level.WARNING, () -> "Could not open connection, check the logs for more details.");
    }

    /**
     * Print a message when fail to connect.
     */
    public void printFailConnect() {
        log.log(Level.WARNING, () -> "Previous attempt to connect have failed, so can't execute your sql command. Will do an attempt to connect again, if it will not work you get 'Could not open connection, check the logs for more details.' message.");
    }

    /**
     * Print a message when can't find the table.
     *
     * @param tableName The name for the table it could not find.
     */
    public void printFailFindTable(String tableName) {
        log.log(Level.WARNING, () -> "Could not find table " + tableName);
    }

    private void sendLogMessage(final PrimaryConstraintWrapper primaryWrapper, final Map<String, Object> primaryKeys) {
        if (primaryWrapper.isUnique()) {
            log.log(Level.FINE, () -> "Primary key values are incomplete (null key or value detected). Provided values: '" + primaryKeys + "'. Primary key will not be created for this row. Unique constraint will be used instead, as configured.");
        } else {
            log.log(Level.FINE, () -> "Primary key values are incomplete (null key or value detected). Provided values: '" + primaryKeys + "' . Primary key cannot be created and UNIQUE fallback is disabled. Migration will be aborted.");
        }
    }

    private static String getMessage(final String message, final String tableName , final Object columnsToBeModified) {
        return message + "'" + columnsToBeModified + "'. To this table '" + tableName + "'";
    }

}
