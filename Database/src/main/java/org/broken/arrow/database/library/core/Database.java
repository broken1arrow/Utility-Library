package org.broken.arrow.database.library.core;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.SqlQueryBuilder;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.SqlQueryTable;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.core.databases.H2DB;
import org.broken.arrow.database.library.core.databases.MongoDB;
import org.broken.arrow.database.library.core.databases.MySQL;
import org.broken.arrow.database.library.core.databases.PostgreSQL;
import org.broken.arrow.database.library.core.databases.SQLite;
import org.broken.arrow.database.library.utility.BatchExecutor;
import org.broken.arrow.database.library.utility.BatchExecutorUnsafe;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;
import org.broken.arrow.database.library.utility.DatabaseType;
import org.broken.arrow.database.library.utility.SQLCommandPrefix;
import org.broken.arrow.database.library.utility.serialize.MethodReflectionUtils;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

@SuppressWarnings("unused")
public abstract class Database {
    private final Logging log = new Logging(Database.class);
    private final Map<String, SqlQueryTable> tablesCache = new HashMap<>();
    private final ConnectionSettings connectionSettings;
    private final MethodReflectionUtils methodReflectionUtils = new MethodReflectionUtils();
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

    public abstract boolean isHasCastException();

    /**
     * The batchUpdate method, override this method to self set the method.
     *
     * @param sqlComposer   list of instances that store the information for the command that will be executed.
     * @param tableWrappers the table wrapper involved in the execution of this event.
     * @deprecated should not be used as it have no use soon.
     */
    @Deprecated
    protected void batchUpdate(@Nonnull final List<SqlCommandComposer> sqlComposer, @Nonnull final TableWrapper... tableWrappers) {

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

    @Nullable
    public SqlQueryTable getTableFromName(String tableName) {
        return tablesCache.get(tableName);
    }


    /**
     * Create all needed tables if it not exist.
     */
    public void createTables() {
        if (tablesCache.isEmpty()) {
            log.log(() -> of("You don't have added any tables, so it can't check or create your tables."));
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
                log.log(throwable, () -> of("Fail to update columns in your table."));
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
    public abstract <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, String columnValue);


    /**
     * Remove all rows from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param values    the list of primary key values you want to remove from database.
     */
    public void removeAll(@Nonnull final String tableName, @Nonnull final List<String> values) {
        BatchExecutor batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
        }

        final SqlQueryTable table = this.getTableFromName(tableName);
        if (table == null) {
            this.log.log(Level.WARNING, () -> Logging.of("Could not find this table:'" + tableName + "' when attempting to remove your list of primary values. Did you register your table?"));
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
        BatchExecutor batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
        }
        final SqlQueryTable table = this.getTableFromName(tableName);

        if (table == null) {
            this.log.log(Level.WARNING, () -> Logging.of("Could not find this table:'" + tableName + "' when attempting to remove your list of primary values. Did you register your table?"));
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
        BatchExecutor batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
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
        BatchExecutor batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return false;
        }
        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
        }
        final SqlQueryTable table = this.getTableFromName(tableName);
        if (table == null) {
            log.log(() -> of("Could not find this table: '" + tableName + "'"));
            return false;
        }
        return batchExecutor.checkIfRowExist(tableName, primaryKeyValue, whereClause -> table.createWhereClauseFromPrimaryColumns(whereClause, "'" + primaryKeyValue + "'"));
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
        runSQLCommand(new SqlQueryBuilder.Builder(SQLCommandPrefix.DELETE, quotes + tableName + quotes)
                .selectColumns(' ')
                .from()
                .where(column + " < ?")
                .putColumnData(1, column, threshold)
                .build());
    }

    /**
     * This method enables you to set up and execute custom SQL commands on a database table. This method use the batch
     * to effectively run several commands at the same time. While this method uses preparedStatement and parameterized
     * queries to reduce SQL injection risks (if you do not turn it off with {@link #setSecureQuery(boolean)}),
     * it is crucial for you to follow safe practices:
     * <p>&nbsp;</p>
     * <p>
     * * Don't pass unsanitized user input directly into SQL commands. Always validate and sanitize
     * any user-provided values before using them in SQL commands.
     * </p>
     * <p>&nbsp;</p>
     * <p>
     * * You need to be aware of the potentially security risks of running your own custom SQL
     * commands, and only give access to trusted individuals only.
     * </p>
     *
     * @param sqlQueryBuilders The SQL command or commands you want to run
     */
    public void runSQLCommand(@Nonnull final SqlQueryBuilder... sqlQueryBuilders) {
        BatchExecutor batchExecutor;
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }
        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
        }
        batchExecutor.runSQLCommand(sqlQueryBuilders);
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

    protected void close(final PreparedStatement... preparedStatement) {
        if (preparedStatement == null) return;
        for (final PreparedStatement statement : preparedStatement)
            close(statement, null);
    }

    public void close(final PreparedStatement preparedStatement, final ResultSet resultSet) {
        try {
            if (preparedStatement != null) preparedStatement.close();
            if (resultSet != null) resultSet.close();
        } catch (final SQLException ex) {
            log.log(ex, () -> of("Fail to close preparedStatement."));
        }
    }

    /**
     * Update the table, if it missing a colum or columns.
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
            final String queryAllColumns = "SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + ";";
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

    protected void dropColumn(final List<String> existingColumns, final String tableName) throws SQLException {
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
    }

    private void createMissingColumns(Connection connection, SqlQueryTable queryTable, List<String> existingColumns) {
        if (existingColumns == null) return;
        if (connection == null) {
            log.log(Level.WARNING, () -> of("You must set the connection instance."));
            return;
        }

        for (final Column column : queryTable.getTable().getColumns()) {
            String columnName = column.getColumnName();
            if (removeColumns.contains(columnName) || existingColumns.contains(columnName.toLowerCase())) continue;

            try (final PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + this.getQuote() + queryTable.getTableName() + this.getQuote() + " ADD " + this.getQuote() + columnName + this.getQuote() + " " + ((TableColumn) column).getDataType() + ";")) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> of("Could not add this '" + columnName + "' missing column. To this table '" + queryTable.getTableName() + "'"));
            }
        }

    }

    protected void createMissingColumns(final Connection connection, final TableWrapper tableWrapper, final List<String> existingColumns) throws SQLException {
        if (existingColumns == null) return;
        if (connection == null) {
            log.log(Level.WARNING, () -> of("You must set the connection instance."));
            return;
        }

        for (final Entry<String, TableRow> entry : tableWrapper.getColumns().entrySet()) {
            String columnName = entry.getKey();
            TableRow tableRow = entry.getValue();
            if (removeColumns.contains(columnName) || existingColumns.contains(columnName.toLowerCase())) continue;

            try (final PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + this.getQuote() + tableWrapper.getTableName() + this.getQuote() + " ADD " + this.getQuote() + columnName + this.getQuote() + " " + tableRow.getDatatype() + ";")) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> of("Could not create this '" + columnName + "' missing column. To this table '" + tableWrapper.getTableName() + "'"));
            }
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
            log.log(() -> of("Something not working when try create this table: '" + tableQuery.getTableName() + "'"));
            final String finalTable = table;
            log.log(e, () -> of("With this command: " + finalTable));
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
            log.log(() -> of("Something not working when try create this table: '" + tableName + "'"));
            final String finalTable = table;
            log.log(e, () -> of("With this command: " + finalTable));
            return false;
        } finally {
            close(statement);
        }
    }

    @Nullable
    public Connection attemptToConnect() {
        Connection connection = this.connect();
        if (isHasCastException()) {
            this.printFailConnect();
            if (connection == null) {
                this.printFailToOpen();
                return null;
            }
            return connection;
        }
        return connection;
    }

    private void checkIfTableExist(@Nonnull final Connection connection, String tableName, String columName) {

        final String checkTableQuery = "SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + " WHERE " + this.getQuote() + columName + this.getQuote() + " = ?";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(checkTableQuery)) {
            preparedStatement.setString(1, "");
            final ResultSet resultSet = preparedStatement.executeQuery();
            close(preparedStatement, resultSet);

        } catch (final SQLException ex) {
            log.log(ex, () -> of("Unable to check if table is created."));
        }
    }


    protected String getColumnsFromTable(final List<Column> columns) {
        final StringBuilder columRow = new StringBuilder();
        for (final Column colum : columns) {
            columRow.append(colum.getColumnName()).append(" ");
        }
        return columRow.toString();
    }

    protected String textUtils(final List<String> columns) {
        return columns.toString().replace("[", "").replace("]", "");
    }

    public boolean isHasStartWriteToDb() {
        return false;
    }

    public Set<String> getRemoveColumns() {
        return removeColumns;
    }

    public void setRemoveColumns(final Set<String> removeColumns) {
        this.removeColumns = removeColumns;
    }

    /**
     * V of database set.
     *
     * @return the database type currently set.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    @Nonnull
    public Map<String, SqlQueryTable> getTables() {
        return tablesCache;
    }


    @Nullable
    public Connection openConnection() {
        return connect();
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException exception) {
            log.log(Level.WARNING, exception, () -> of("Something went wrong, when attempt to close connection."));
        }
    }

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
     * may return column names in uppercase, and this method uses {@link #getColumnName(TableWrapper, String)}
     * to correct the keys in the map based on the column names set in the TableWrapper.
     *
     * @param resultSet    The ResultSet object representing the cursor to retrieve the data set.
     * @param tableWrapper The TableWrapper instance representing the current table to get the column names.
     * @return A map containing the column values from the database, with corrected keys based on the
     * column names in the TableWrapper.
     * @throws SQLException If there is an issue reading data from the database.
     */
    public Map<String, Object> getDataFromDB(final ResultSet resultSet, final TableWrapper tableWrapper) throws SQLException {
        if (tableWrapper == null) return new HashMap<>();

        final ResultSetMetaData rsmd = resultSet.getMetaData();
        final int columnCount = rsmd.getColumnCount();
        final Map<String, Object> objectMap = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            objectMap.put(this.getColumnName(tableWrapper, rsmd.getColumnName(i)), resultSet.getObject(i));
        }
        return objectMap;
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
     * Retrieves the correct column name for the given table from the TableWrapper.
     * Some databases may return column names in uppercase, so this method allows you
     * to fetch the column name with the appropriate casing as set in the TableWrapper.
     *
     * @param tableWrapper The TableWrapper instance currently being processed.
     * @param columnName   The name of the column to retrieve.
     * @return The column name with the correct case as set in the TableWrapper. If there
     * is no matching column name in the TableWrapper, it returns the columnName
     * you set in the second argument.
     */
    public String getColumnName(final TableWrapper tableWrapper, String columnName) {
        TableRow primaryRow = tableWrapper.getPrimaryRow();
        if (primaryRow != null && primaryRow.getColumnName().equalsIgnoreCase(columnName))
            return primaryRow.getColumnName();

        if (!tableWrapper.getColumns().isEmpty())
            for (String column : tableWrapper.getColumns().keySet()) {
                if (column.equalsIgnoreCase(columnName)) return column;
            }
        // If no matching column name is found, return the original name from the columnName.
        return columnName;
    }

    public boolean isHikariAvailable(final String path) {
        try {
            Class.forName(path);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
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

    public void loadDriver(final String path) {
        try {
            Class.forName(path);
        } catch (ClassNotFoundException e) {
            log.log(() -> of("Could not load this driver: " + path));
        }
    }

    public MethodReflectionUtils getMethodReflectionUtils() {
        return methodReflectionUtils;
    }

    public void printPressesCount(int processedCount) {
        if (processedCount > 10_000)
            log.log(() -> of(("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE " + (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.")));
    }

    public void printFailToOpen() {
        log.log(Level.WARNING, () -> of("Could not open connection, check the logs for more details."));
    }

    public void printFailConnect() {
        log.log(Level.WARNING, () -> of("Previous attempt to connect have failed, so can't execute your sql command. Will do an attempt to connect again, if it will not work you get 'Could not open connection, check the logs for more details.' message."));
    }

    public void printFailFindTable(String tableName) {
        log.log(Level.WARNING, () -> of("Could not find table " + tableName));
    }

    public boolean checkIfNotNull(Object object) {
        return object != null;
    }

}
