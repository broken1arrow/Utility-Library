package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.SqlQueryBuilder;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.database.library.utility.BatchExecutor;
import org.broken.arrow.database.library.utility.BatchExecutorUnsafe;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;
import org.broken.arrow.database.library.utility.DatabaseType;
import org.broken.arrow.database.library.utility.PreparedStatementWrapper;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public abstract class Database {
    private final Logging log = new Logging(Database.class);
    private final Map<String, TableWrapper> tables = new HashMap<>();
    private Set<String> removeColumns = new HashSet<>();
    private final ConnectionSettings connectionSettings;
    private final MethodReflectionUtils methodReflectionUtils = new MethodReflectionUtils();

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
     * Create all needed tables if it not exist.
     */
    public void createTables() {
        Validate.checkBoolean(tables.isEmpty(), "The table is empty, add tables to the map before call this method");
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }
        try {
            createAllTablesIfNotExist(connection);
            try {
                for (final Entry<String, TableWrapper> entityTables : tables.entrySet()) {
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
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, String... columns) {
        Connection connection = this.attemptToConnect();
        BatchExecutor batchExecutor;
        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, dataWrapperList);
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, dataWrapperList);
        }
        batchExecutor.saveAll(tableName, shallUpdate, columns);
    }

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
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, String... columns) {

        Connection connection = this.attemptToConnect();
        BatchExecutor batchExecutor;
        if (this.secureQuery)
            batchExecutor = new BatchExecutor(this, connection, new ArrayList<>());
        else {
            batchExecutor = new BatchExecutorUnsafe(this, connection, new ArrayList<>());
        }
        batchExecutor.save(tableName, dataWrapper, shallUpdate, columns);
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
    public <T extends ConfigurationSerializable> List<LoadDataWrapper<T>> loadAll(@Nonnull final String tableName, @Nonnull final Class<T> clazz) {

        TableWrapper tableWrapper = this.getTable(tableName);
        if (tableWrapper == null) {
            this.printFailFindTable(tableName);
            return null;
        }

        final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Primary column should not be null");

        this.getPreparedStatement(sqlCommandComposer.selectTable(), statementWrapper -> {
            try (ResultSet resultSet = statementWrapper.getPreparedStatement().executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> dataFromDB = this.getDataFromDB(resultSet, tableWrapper);
                    T deserialize = this.methodReflectionUtils.invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
                    Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());
                    loadDataWrappers.add(new LoadDataWrapper<>(primaryValue, deserialize));
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Could not load all data for this table '" + tableName + "'. Check the stacktrace."));
            }
        });
        return loadDataWrappers;
    }

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
    public <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, String columnValue) {
        TableWrapper tableWrapper = this.getTable(tableName);

        if (tableWrapper == null) {
            this.printFailFindTable(tableName);
            return null;
        }

        Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
        String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
        Map<String, Object> dataFromDB = new HashMap<>();
        Validate.checkNotNull(columnValue, "Could not find column for " + primaryColumn + ". Because the column value is null.");

        final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);

        this.getPreparedStatement(sqlCommandComposer.selectRow(columnValue), statementWrapper -> {
            try (ResultSet resultSet = statementWrapper.getPreparedStatement().executeQuery()) {
                if (resultSet.next())
                    dataFromDB.putAll(this.getDataFromDB(resultSet, tableWrapper));
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> of("Could not load the data. Check the stacktrace."));
            }
        });
        if (dataFromDB.isEmpty())
            return null;
        T deserialize = this.methodReflectionUtils.invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
        Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());

        return new LoadDataWrapper<>(primaryValue, deserialize);
    }


    /**
     * Remove all rows from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param values    the list of primary key values you want to remove from database.
     */
    public void removeAll(final String tableName, final List<String> values) {
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

        batchExecutor.removeAll(tableName, values);
    }

    /**
     * Remove one row from specified database table.
     *
     * @param tableName name of the table you want to get data from.
     * @param value     the primary key value you want to remove from database.
     */
    public void remove(final String tableName, final String value) {
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

        batchExecutor.remove(tableName, value);
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
     * @param command  the V or SQL command you want to run.
     * @param function the function that will be applied to the command.
     * @param <T>      The type you want the method to return.
     * @return the value you set as the lambda should return or null if something did go wrong.
     */
    @Nullable
    public <T> T getPreparedStatement(@Nonnull final String command, final Function<PreparedStatementWrapper, T> function) {
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(command)) {
            return function.apply(new PreparedStatementWrapper(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> of("could not execute this command: " + command));
        } finally {
            this.closeConnection(connection);
        }
        return null;
    }

    /**
     * Executes a custom SQL command on the database and returns a {@link PreparedStatementWrapper}.
     * This method leverages prepared statements and parameterized queries to mitigate SQL injection risks
     * (unless disabled using {@link #setSecureQuery(boolean)}). It is essential to adhere to best practices for safe SQL execution:
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
     * @param command  the SQL command to be executed.
     * @param consumer a consumer to handle the result returned by the database.
     */
    public void getPreparedStatement(@Nonnull final String command, final Consumer<PreparedStatementWrapper> consumer) {
        Connection connection = this.attemptToConnect();
        if (connection == null) {
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(command)) {
            consumer.accept(new PreparedStatementWrapper(preparedStatement));
        } catch (SQLException e) {
            log.log(e, () -> of("Could not execute this command: " + command));
        } finally {
            this.closeConnection(connection);
        }
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
     * @param tableName the name of the table to search for the data.
     * @param primaryKeyValue the primary key value to look for in the table.
     * @return {@code true} if the key exists in the table, or {@code false} if the data is not found
     *         or a connection issue occurs.
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
        return batchExecutor.checkIfRowExist(tableName,  primaryKeyValue);
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
            log.log(ex, () -> of("Fail to close connection."));
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
            statement = connection.prepareStatement("SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + ";");
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
        final TableWrapper updatedTableColumns = tables.get(tableName);

        if (updatedTableColumns == null || updatedTableColumns.getColumns().isEmpty()) return;
        // Remove the columns we don't want anymore from the table's list of columns

        if (this.removeColumns != null) for (final String removed : this.removeColumns) {
            existingColumns.remove(removed);
        }
        final String columnsSeparated = getColumnsFromTable(updatedTableColumns.getColumns().values());

        PreparedStatement moveData = null;
        PreparedStatement alterTable = null;
        PreparedStatement createTable = null;
        PreparedStatement removeOldTable = null;
        try {    // Rename the old table, so we can remove old name and rename columns.
            alterTable = connection.prepareStatement("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");
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
        for (final Entry<String, TableWrapper> wrapperEntry : tables.entrySet()) {
            this.createTableIfNotExist(connection, wrapperEntry.getKey());
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
            TableWrapper wrapperEntry = this.getTable(tableName);
            if (wrapperEntry == null) {
                this.printFailFindTable(tableName);
                return false;
            }
            final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(wrapperEntry), this);
            table = sqlCommandComposer.createTable();
            statement = connection.prepareStatement(table);
            statement.executeUpdate();
            TableRow wrapper = wrapperEntry.getColumns().values().stream().findFirst().orElse(null);
            Validate.checkNotNull(wrapper, "Could not find a column for this table " + tableName);
            checkIfTableExist(connection, tableName, wrapper.getColumnName());
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
    private Connection attemptToConnect() {
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

        try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + " WHERE " + this.getQuote() + columName + this.getQuote() + " = ?");) {
            preparedStatement.setString(1, "");
            final ResultSet resultSet = preparedStatement.executeQuery();
            close(preparedStatement, resultSet);

        } catch (final SQLException ex) {
            log.log(ex, () -> of("Unable to retrieve connection."));
        }
    }


    protected String getColumnsFromTable(final Collection<TableRow> columns) {
        final StringBuilder columRow = new StringBuilder();
        for (final TableRow colum : columns) {
            columRow.append(colum).append(" ");
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
    public Map<String, TableWrapper> getTables() {
        return tables;
    }

    @Nullable
    public TableWrapper getTable(String tableName) {
        return tables.get(tableName);
    }

    /**
     * Remove the table.
     *
     * @param tableName The table you want to remove
     * @return true if it find the old table you want to remove.
     */
    public boolean removeTable(String tableName) {
        return tables.remove(tableName) != null;
    }

    public void addTable(TableWrapper tableWrapper) {
        tables.put(tableWrapper.getTableName(), tableWrapper);
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
        final ResultSetMetaData rsmd = resultSet.getMetaData();
        final int columnCount = rsmd.getColumnCount();
        final Map<String, Object> objectMap = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            objectMap.put(this.getColumnName(tableWrapper, rsmd.getColumnName(i)), resultSet.getObject(i));
        }
        return objectMap;
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
     * Retrieve the current maximum size of the connection pool.
     *
     * @return the maximum size of the connection pool, or a default value if not explicitly set.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
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
     * Retrieve the current idle timeout in milliseconds.
     *
     * @return the current idle timeout in milliseconds, or a default value if not explicitly set.
     */
    public long getIdleTimeout() {
        return idleTimeout;
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
