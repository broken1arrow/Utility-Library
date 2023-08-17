package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.RowDataWrapper;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.log.Validate;
import org.broken.arrow.database.library.utility.DatabaseType;
import org.broken.arrow.database.library.utility.serialize.MethodReflectionUtils;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Database {

	protected Connection connection;
	private final MethodReflectionUtils methodReflectionUtils = new MethodReflectionUtils();
	protected boolean batchUpdateGoingOn = false;
	private final Map<String, TableWrapper> tables = new HashMap<>();
	protected boolean hasStartWriteToDb = false;
	private final DatabaseType databaseType;
	private Set<String> removeColumns = new HashSet<>();
	private char quote = '`';
	private String characterSet = "";
	private boolean secureQuery = true;

	public Database() {
		if (this instanceof SQLite) {
			this.databaseType = DatabaseType.SQLite;
			return;
		}
		if (this instanceof MySQL) {
			this.databaseType = DatabaseType.MySQL;
			return;
		}
		if (this instanceof H2DB) {
			this.databaseType = DatabaseType.H2;
			return;
		}
		if (this instanceof PostgreSQL) {
			this.databaseType = DatabaseType.PostgreSQL;
			quote = ' ';
			return;
		}
		if (this instanceof MongoDB) {
			this.databaseType = DatabaseType.MongoDB;
			return;
		}
		this.databaseType = DatabaseType.Unknown;
	}

	/**
	 * The connection to the database.
	 *
	 * @return the connection.
	 */
	public abstract Connection connect();

	/**
	 * The batchUpdate method, override this method to self set the {@link #batchUpdate(java.util.List, int, int)} method.
	 *
	 * @param sqlComposer   list of instances that store the information for the command that will be executed.
	 * @param tableWrappers the table wrapper involved in the execution of this event.
	 */
	protected abstract void batchUpdate(@Nonnull final List<SqlCommandComposer> sqlComposer, @Nonnull final TableWrapper... tableWrappers);

	/**
	 * Create all needed tables if it not exist.
	 */
	public void createTables() {
		Validate.checkBoolean(tables.isEmpty(), "The table is empty, add tables to the map before call this method");
		try {
			createAllTablesIfNotExist();
			try {
				for (final Entry<String, TableWrapper> entityTables : tables.entrySet()) {
					final List<String> columns = updateTableColumnsInDb(entityTables.getKey());
					this.createMissingColumns(entityTables.getValue(), columns);
				}
			} catch (final SQLException throwable) {
				throwable.printStackTrace();
			}
		} finally {
			closeConnection();
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
		final List<SqlCommandComposer> composerList = new ArrayList<>();

		final TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}

		for (DataWrapper dataWrapper : dataWrapperList) {
			if (dataWrapper == null) continue;

			TableRow primaryRow = tableWrapper.getPrimaryRow();
			if (primaryRow == null) continue;

			RowWrapper saveWrapper = new RowDataWrapper(tableWrapper, dataWrapper.getPrimaryValue());

			for (Entry<String, Object> entry : dataWrapper.getConfigurationSerialize().serialize().entrySet()) {
				TableRow column = tableWrapper.getColumn(entry.getKey());
				if (column == null) continue;
				saveWrapper.putColumn(entry.getKey(), entry.getValue());
			}
			composerList.add(this.getCommandComposer(saveWrapper, shallUpdate, columns));
		}
		this.batchUpdate(composerList, this.getTables().values().toArray(new TableWrapper[0]));
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
		final List<SqlCommandComposer> composerList = new ArrayList<>();
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		if (dataWrapper == null)
			return;

		ConfigurationSerializable configuration = dataWrapper.getConfigurationSerialize();
		RowWrapper rowWrapper = new RowDataWrapper(tableWrapper, dataWrapper.getPrimaryValue());

		for (Entry<String, Object> entry : configuration.serialize().entrySet()) {
			TableRow column = tableWrapper.getColumn(entry.getKey());
			if (column == null) continue;
			rowWrapper.putColumn(entry.getKey(), entry.getValue());
		}
		composerList.add(this.getCommandComposer(rowWrapper, shallUpdate, columns));
		this.batchUpdate(composerList, tableWrapper);
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
		final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return null;
		}
		if (!openConnection()) {
			LogMsg.warn("Could not open connection.");
			return null;
		}
		final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
		Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Primary column should not be null");

		try {
			preparedStatement = connection.prepareStatement(sqlCommandComposer.selectTable());
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Map<String, Object> dataFromDB = this.getDataFromDB(resultSet, tableWrapper);
				T deserialize = this.methodReflectionUtils.invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
				Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());
				if (primaryValue == null)
					LogMsg.warn("This table '" + tableName + "' with the primary key '" + tableWrapper.getPrimaryRow().getColumnName() + "' has null value. Please ensure that this is not a mistake.");
				loadDataWrappers.add(new LoadDataWrapper<>(primaryValue, deserialize));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(preparedStatement, resultSet);
		}
		return loadDataWrappers;
	}

	/**
	 * Load one row from specified database table.
	 *
	 * @param tableName   name of the table you want to get data from.
	 * @param clazz       the class you have your static deserialize method.
	 * @param columnValue the value of the primary key you want to find data from.
	 * @param <T>         Type of class that extends ConfigurationSerializable .
	 * @return one row you have in the table.
	 */
	@Nullable
	public <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, String columnValue) {
		TableWrapper tableWrapper = this.getTable(tableName);
		if (!openConnection()) {
			LogMsg.warn("Could not open connection.");
			return null;
		}
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return null;
		}
		Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
		String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
		Map<String, Object> dataFromDB = new HashMap<>();
		Validate.checkNotNull(columnValue, "Could not find column for " + primaryColumn + ". Because the column value is null.");

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
		try {
			preparedStatement = this.connection.prepareStatement(sqlCommandComposer.selectRow(columnValue));
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				dataFromDB.putAll(this.getDataFromDB(resultSet, tableWrapper));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(preparedStatement, resultSet);
		}
		T deserialize = this.methodReflectionUtils.invokeDeSerializeMethod(clazz, "deserialize", dataFromDB);
		Object primaryValue = dataFromDB.get(tableWrapper.getPrimaryRow().getColumnName());
		if (primaryValue == null)
			LogMsg.warn("This table '" + tableName + "' with the primary key '" + tableWrapper.getPrimaryRow().getColumnName() + "' has null value. Please ensure that this is not a mistake.");
		return new LoadDataWrapper<>(primaryValue, deserialize);
	}


	/**
	 * Remove all rows from specified database table.
	 *
	 * @param tableName name of the table you want to get data from.
	 * @param values    the list of primary key values you want to remove from database.
	 */
	public void removeAll(final String tableName, final List<String> values) {
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		List<SqlCommandComposer> columns = new ArrayList<>();
		for (String value : values) {
			final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
			sqlCommandComposer.removeRow(value);
			columns.add(sqlCommandComposer);
		}
		this.batchUpdate(columns, tableWrapper);
	}

	/**
	 * Remove one row from specified database table.
	 *
	 * @param tableName name of the table you want to get data from.
	 * @param value     the primary key value you want to remove from database.
	 */
	public void remove(final String tableName, final String value) {
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
		sqlCommandComposer.removeRow(value);
		this.batchUpdate(Collections.singletonList(sqlCommandComposer), tableWrapper);
	}

	/**
	 * Drop the table.
	 *
	 * @param tableName the name of the table to drop.
	 */
	public void dropTable(final String tableName) {
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
		sqlCommandComposer.dropTable();
		this.batchUpdate(Collections.singletonList(sqlCommandComposer), tableWrapper);
	}

	public boolean doRowExist(@Nonnull String tableName, @Nonnull Object primaryKeyValue) {
		TableWrapper tableWrapper = this.getTable(tableName);
		if (!openConnection()) {
			LogMsg.warn("Could not open connection.");
			return false;
		}
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return false;
		}
		Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Could not find  primary column for table " + tableName);
		String primaryColumn = tableWrapper.getPrimaryRow().getColumnName();
		Validate.checkNotNull(primaryKeyValue, "Could not find column for " + primaryColumn + ". Because the column value is null.");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(tableWrapper), this);
		try {
			preparedStatement = this.connection.prepareStatement(sqlCommandComposer.selectRow(primaryKeyValue + ""));
			resultSet = preparedStatement.executeQuery();
			return resultSet.next();
		} catch (SQLException e) {
			LogMsg.warn("Could not search for your the row with this value '" + primaryKeyValue + "' from this table '" + tableName + "'", e);
		} finally {
			this.close(preparedStatement, resultSet);
		}
		return false;
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

	protected void close(final PreparedStatement preparedStatement, final ResultSet resultSet) {
		try {
			if (preparedStatement != null)
				preparedStatement.close();
			if (resultSet != null)
				resultSet.close();
		} catch (final SQLException ex) {
			ex.printStackTrace();
			/*LogMsg.close(CCH.getInstance(), ex);*/
		}

	}

	/**
	 * Update the table, if it missing a colum or columns.
	 *
	 * @param tableName the table you want to check.
	 * @return list of columns added in the database.
	 * @throws SQLException if something going wrong.
	 */
	protected List<String> updateTableColumnsInDb(final String tableName) throws SQLException {
		if (!openConnection()) return new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {
			final List<String> column = new ArrayList<>();
			statement = this.connection.prepareStatement("SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + ";");
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

	protected void dropColumn(final String createTableCmd, final List<String> existingColumns, final String tableName) throws SQLException {

		final TableWrapper updatedTableColumns = tables.get(tableName);

		if (updatedTableColumns == null || updatedTableColumns.getColumns().isEmpty()) return;
		// Remove the columns we don't want anymore from the table's list of columns

		if (this.removeColumns != null)
			for (final String removed : this.removeColumns) {
				existingColumns.remove(removed);
			}
		final String columnsSeparated = getColumnsFromTable(updatedTableColumns.getColumns().values());
		if (!openConnection()) return;

		// Rename the old table, so we can remove old name and rename columns.
		final PreparedStatement alterTable = connection.prepareStatement("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");
		// Creating the table on its new format (no redundant columns)
		final PreparedStatement createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsSeparated + ");");

		alterTable.execute();
		createTable.execute();

		// Populating the table with the data
		final PreparedStatement moveData = connection.prepareStatement("INSERT INTO " + tableName + "(" + TextUtils(existingColumns) + ") SELECT "
				+ TextUtils(existingColumns) + " FROM " + tableName + "_old;");
		moveData.execute();

		final PreparedStatement removeOldTable = connection.prepareStatement("DROP TABLE " + tableName + "_old;");
		removeOldTable.execute();

		close(moveData, alterTable, createTable, removeOldTable);
	}

	protected void createMissingColumns(final TableWrapper tableWrapper, final List<String> existingColumns) throws SQLException {
		if (existingColumns == null) return;
		if (!openConnection()) return;

		for (final Entry<String, TableRow> entry : tableWrapper.getColumns().entrySet()) {
			String columnName = entry.getKey();
			TableRow tableRow = entry.getValue();
			if (removeColumns.contains(columnName)) continue;
			if (existingColumns.contains(columnName.toLowerCase())) continue;
			try {
				final PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + this.getQuote() + tableWrapper.getTableName() + this.getQuote() + " ADD " + this.getQuote() + columnName + this.getQuote() + " " + tableRow.getDatatype() + ";");
				statement.execute();
			} catch (final SQLException throwable) {
				throwable.printStackTrace();
			}
		}
	}

	/**
	 * Create all tables if it not exist yet, will only create a table if it not already exist.
	 */
	protected void createAllTablesIfNotExist() {
		//if (!openConnection()) return;
		for (final Entry<String, TableWrapper> wrapperEntry : tables.entrySet()) {
			this.createTableIfNotExist(wrapperEntry.getKey());
		}
	}


	/**
	 * Create table if it not exist yet, will only create a table if it not already exist.
	 *
	 * @param tableName the name of the table to create.
	 * @return true if it could check if the table exist or create now table.
	 */
	protected boolean createTableIfNotExist(final String tableName) {
		if (!openConnection()) {
			LogMsg.warn("Could not open connection.");
			return false;
		}
		PreparedStatement statement = null;
		String table = "";
		try {
			TableWrapper wrapperEntry = this.getTable(tableName);
			if (wrapperEntry == null) {
				LogMsg.warn("Could not find table " + tableName);
				return false;
			}
			final SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(new RowWrapper(wrapperEntry), this);
			table = sqlCommandComposer.createTable();
			statement = this.connection.prepareStatement(table);
			statement.executeUpdate();
			TableRow wrapper = wrapperEntry.getColumns().values().stream().findFirst().orElse(null);
			Validate.checkNotNull(wrapper, "Could not find a column for this table " + tableName);
			checkIfTableExist(tableName, wrapper.getColumnName());
			return true;
		} catch (final SQLException e) {
			LogMsg.warn("Something not working when try create this table: '" + tableName + "'");
			LogMsg.warn("With this command: " + table, e);
			//e.printStackTrace();
			return false;
		} finally {
			close(statement);
		}
	}

	private void checkIfTableExist(String tableName, String columName) {
		try {
			if (this.connection == null) return;

			final PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM " + this.getQuote() + tableName + this.getQuote() + " WHERE " + this.getQuote() + columName + this.getQuote() + " = ?");
			preparedStatement.setString(1, "");
			final ResultSet resultSet = preparedStatement.executeQuery();
			close(preparedStatement, resultSet);

		} catch (final SQLException ex) {
			LogMsg.warn("Unable to retrieve connection ", ex);
		}
	}


	protected String getColumnsFromTable(final Collection<TableRow> columns) {
		final StringBuilder columRow = new StringBuilder();
		for (final TableRow colum : columns) {
			columRow.append(colum).append(" ");
		}
		return columRow.toString();
	}

	/**
	 * Retrieves a SqlCommandComposer instance.
	 *
	 * @param rowWrapper  The current row's column data.
	 * @param shallUpdate Specifies whether the table should be updated.
	 * @param columns     If not null and not empty, updates the existing row with these columns if it exists.
	 * @return The SqlCommandComposer instance with the finish SQL command for either use for prepare statement or not.
	 */
	protected SqlCommandComposer getCommandComposer(@Nonnull final RowWrapper rowWrapper, final boolean shallUpdate, String... columns) {
		SqlCommandComposer commandComposer = new SqlCommandComposer(rowWrapper, this);
		commandComposer.setColumnsToUpdate(columns);
		boolean columnsIsEmpty = columns == null || columns.length == 0;

		if ((!columnsIsEmpty || shallUpdate) && this.doRowExist(rowWrapper.getTableWrapper().getTableName(), rowWrapper.getPrimaryKeyValue()))
			commandComposer.updateTable(rowWrapper.getPrimaryKeyValue());
		else
			commandComposer.replaceIntoTable();

		return commandComposer;
	}

	protected String TextUtils(final List<String> columns) {
		return columns.toString().replace("[", "").replace("]", "");
	}

	public boolean isHasStartWriteToDb() {
		return hasStartWriteToDb;
	}

	public Set<String> getRemoveColumns() {
		return removeColumns;
	}

	public void setRemoveColumns(final Set<String> removeColumns) {
		this.removeColumns = removeColumns;
	}

	/**
	 * Type of database set.
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

	public boolean openConnection() {
		if (isClosed())
			this.connection = connect();
		return this.connection != null;
	}

	public void closeConnection() {
		try {
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * This method check if connection is null or close.
	 *
	 * @return true if connection is closed or null.
	 */
	public boolean isClosed() {
		try {
			return this.connection == null || this.connection.isClosed();
		} catch (SQLException e) {
			LogMsg.warn("Could not check if the connection is closed ", e);
			return false;
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

		for (String column : tableWrapper.getColumns().keySet()) {
			if (column.equalsIgnoreCase(columnName))
				return column;
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

	public void loadDriver(final String path) {
		try {
			Class.forName(path);
		} catch (ClassNotFoundException e) {
			LogMsg.info("Could not load this driver: " + path);
		}
	}

	public MethodReflectionUtils getMethodReflectionUtils() {
		return methodReflectionUtils;
	}

	protected final void batchUpdate(@Nonnull final List<SqlCommandComposer> batchList, int resultSetType, int resultSetConcurrency) {
		final ArrayList<SqlCommandComposer> sqls = new ArrayList<>(batchList);
		if (!openConnection()) {
			LogMsg.warn("Could not open connection.");
			return;
		}
		if (sqls.size() == 0)
			return;

		if (this.secureQuery)
			this.executePrepareBatch(sqls, resultSetType, resultSetConcurrency);
		else
			this.executeBatch(sqls, resultSetType, resultSetConcurrency);
	}

	protected final void executePrepareBatch(@Nonnull final List<SqlCommandComposer> batchList, int resultSetType, int resultSetConcurrency) {
		batchUpdateGoingOn = true;
		final int processedCount = batchList.size();
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (batchUpdateGoingOn)
					LogMsg.warn("Still executing, DO NOT SHUTDOWN YOUR SERVER.");
				else
					cancel();
			}
		}, 1000 * 30, 1000 * 30);
		if (processedCount > 10_000) {
			LogMsg.warn("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE "
					+ (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.");
		}
		for (SqlCommandComposer sql : batchList) {

			try (PreparedStatement statement = connection.prepareStatement(sql.getPreparedSQLBatch(), resultSetType, resultSetConcurrency)) {
				// Populate the statement with data where the key is the row identifier (id).
				for (Entry<Integer, Object> column : sql.getColumns().entrySet()) {
					statement.setObject(column.getKey(), column.getValue());
				}
				// Adding the current set of parameters to the batch
				statement.addBatch();
				statement.executeBatch();
			} catch (SQLException e) {
				// Handle the exception for a specific statement
				LogMsg.warn("Could not execute this prepared batch: \"" + sql.getPreparedSQLBatch() + "\"");
				LogMsg.warn("Values that could not be executed: '" + sql.getColumns().values() + "'", e);
			} finally {
				batchUpdateGoingOn = false;
			}
		}
	}

	protected final void executeBatch(@Nonnull final List<SqlCommandComposer> batchOfSQL, int resultSetType, int resultSetConcurrency) {

		if (!hasStartWriteToDb)
			try {
				hasStartWriteToDb = true;
				final Statement statement = this.connection.createStatement(resultSetType, resultSetConcurrency);
				final int processedCount = batchOfSQL.size();

				// Prevent automatically sending db instructions
				this.connection.setAutoCommit(false);

				for (final SqlCommandComposer sql : batchOfSQL)
					statement.addBatch(sql.getQueryCommand());
				if (processedCount > 10_000)
					LogMsg.warn("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE "
							+ (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.");

				// Set the flag to start time notifications timer
				batchUpdateGoingOn = true;

				// Notify console that progress still is being made
				new Timer().scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						if (batchUpdateGoingOn)
							LogMsg.warn("Still executing, DO NOT SHUTDOWN YOUR SERVER.");
						else
							cancel();
					}
				}, 1000 * 30, 1000 * 30);
				// Execute
				statement.executeBatch();

				// This will block the thread
				this.connection.commit();

			} catch (final Throwable t) {
				t.printStackTrace();

			} finally {
				try {
					this.connection.setAutoCommit(true);
					this.closeConnection();

				} catch (final SQLException ex) {
					ex.printStackTrace();
				}
				hasStartWriteToDb = false;
				// Even in case of failure, cancel
				batchUpdateGoingOn = false;
			}
	}

	public abstract boolean isHasCastException();

}
