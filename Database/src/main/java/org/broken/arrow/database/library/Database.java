package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ColumnDataWrapper;
import org.broken.arrow.database.library.builders.ColumnWrapper;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandUtility;
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
	private String quote = "`";

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
			quote = "\"";
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
	 * @param batchList     list of commands that should be executed.
	 * @param tableWrappers the table wrapper involved in the execution of this event.
	 */
	protected abstract void batchUpdate(@Nonnull final List<String> batchList, @Nonnull final TableWrapper... tableWrappers);

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
	 *                        Note: the key you set has to be the primary key you want to update.
	 */
	public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList) {
		this.saveAll(tableName, dataWrapperList, false);
	}

	/**
	 * Saves all rows to the specified database table, based on the provided primary key and associated data.
	 *
	 * @param tableName       name of the table you want to update rows inside.
	 * @param dataWrapperList List of data you want to cache to database.
	 *                        Note: the key you set has to be the primary key you want to update.
	 * @param shallUpdate     Set to true if you want to update the row, other wise it will replace the old row.
	 */
	public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate) {
		final List<String> sqls = new ArrayList<>();

		final TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		if (!openConnection()) return;
		for (DataWrapper dataWrapper : dataWrapperList) {
			if (dataWrapper == null) continue;

			TableRow primaryRow = tableWrapper.getPrimaryRow();
			if (primaryRow == null) continue;

			ColumnWrapper saveWrapper = new ColumnDataWrapper(tableWrapper, dataWrapper.getPrimaryValue());

			for (Entry<String, Object> entry : dataWrapper.getConfigurationSerialize().serialize().entrySet()) {
				TableRow column = tableWrapper.getColumn(entry.getKey());
				if (column == null) continue;
				saveWrapper.putColumn(entry.getKey(), entry.getValue());
			}
			this.getSqlsCommand(sqls, saveWrapper, shallUpdate);
		}
		this.batchUpdate(sqls, this.getTables().values().toArray(new TableWrapper[0]));
	}

	/**
	 * Saves a single row to the specified database table, based on the provided primary key and associated data.
	 * Note: If you use this method it will replace the old data instead of update it.
	 *
	 * @param tableName   The name of the table to save the row to.
	 * @param dataWrapper The wrapper with the set values, for primaryKey, primaryValue and serialize data.
	 */
	public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper) {
		this.save(tableName, dataWrapper, false);
	}

	/**
	 * Saves a single row to the specified database table, based on the provided primary key and associated data.
	 *
	 * @param tableName   The name of the table to save the row to.
	 * @param dataWrapper The wrapper with the set values, for primaryKey, primaryValue and serialize data.
	 * @param shallUpdate Set to true if you want to update the row, other wise it will replace the old row.
	 */
	public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate) {
		final List<String> sqls = new ArrayList<>();
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		if (dataWrapper == null)
			return;

		ConfigurationSerializable configuration = dataWrapper.getConfigurationSerialize();
		ColumnWrapper saveWrapper = new ColumnDataWrapper(tableWrapper, dataWrapper.getPrimaryValue());


		for (Entry<String, Object> entry : configuration.serialize().entrySet()) {
			TableRow column = tableWrapper.getColumn(entry.getKey());
			if (column == null) continue;
			saveWrapper.putColumn(entry.getKey(), entry.getValue());
		}
		this.getSqlsCommand(sqls, saveWrapper, shallUpdate);
		this.batchUpdate(sqls, tableWrapper);
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
		if (!openConnection()) return null;
		final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(tableWrapper));
		Validate.checkNotNull(tableWrapper.getPrimaryRow(), "Primary column should not be null");

		try {
			preparedStatement = connection.prepareStatement(sqlCommandUtility.selectTable());
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
		if (!openConnection()) return null;
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

		final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(tableWrapper));
		try {
			preparedStatement = this.connection.prepareStatement(sqlCommandUtility.selectRow(columnValue));
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
	 * Create all tables if it not exist yet, will only create a table if it not already exist.
	 */
	public void createAllTablesIfNotExist() {
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
	public boolean createTableIfNotExist(final String tableName) {
		if (!openConnection()) return false;
		PreparedStatement statement = null;
		try {
			TableWrapper wrapperEntry = this.getTable(tableName);
			if (wrapperEntry == null) {
				LogMsg.warn("Could not find table " + tableName);
				return false;
			}
			final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(wrapperEntry));
			statement = this.connection.prepareStatement(sqlCommandUtility.createTable());
			statement.executeUpdate();
			TableRow wrapper = wrapperEntry.getColumns().values().stream().findFirst().orElse(null);
			Validate.checkNotNull(wrapper, "Could not find a column for this table " + tableName);
			checkIfTableExist(tableName, wrapper.getColumnName());
			return true;
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(statement);
		}
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
		List<String> columns = new ArrayList<>();
		final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(tableWrapper));

		for (String value : values) {
			columns.add(sqlCommandUtility.removeRow(value));
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
		final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(tableWrapper));
		this.batchUpdate(Collections.singletonList(sqlCommandUtility.removeRow(value)), tableWrapper);
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
		final SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(new ColumnWrapper(tableWrapper));
		this.batchUpdate(Collections.singletonList(sqlCommandUtility.dropTable()), tableWrapper);
	}

	protected final void batchUpdate(@Nonnull final List<String> batchList, int resultSetType, int resultSetConcurrency) {
		final ArrayList<String> sqls = new ArrayList<>(batchList);
		if (!openConnection()) return;

		if (sqls.size() == 0)
			return;

		if (!hasStartWriteToDb)
			try {
				hasStartWriteToDb = true;
				final Statement statement = this.connection.createStatement(resultSetType, resultSetConcurrency);
				final int processedCount = sqls.size();

				// Prevent automatically sending db instructions
				this.connection.setAutoCommit(false);

				for (final String sql : sqls)
					statement.addBatch(sql);
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

		final List<String> column = new ArrayList<>();
		final PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM " + tableName);
		final ResultSet rs = statement.executeQuery();
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columnCount = rsmd.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			column.add(rsmd.getColumnName(i).toLowerCase());
		}
		close(statement, rs);
		return column;
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
				final PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + quote + tableWrapper.getTableName() + quote + " ADD " + quote + columnName + quote + " " + tableRow.getDatatype() + ";");
				statement.execute();
			} catch (final SQLException throwable) {
				throwable.printStackTrace();
			}
		}
	}


	private void checkIfTableExist(String tableName, String columName) {
		try {
			if (this.connection == null) return;

			final PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM " + quote + tableName + quote + " WHERE " + quote + columName + quote + " = ?");
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

	protected List<String> getSqlsCommand(@Nonnull final List<String> listOfCommands, @Nonnull final ColumnWrapper saveWrapper, final boolean shallUpdate) {
		String sql = null;
		SqlCommandUtility sqlCommandUtility = new SqlCommandUtility(saveWrapper);
		if (shallUpdate) {
			sql = sqlCommandUtility.updateTable(saveWrapper.getPrimaryKey());
		} else {
			sql = sqlCommandUtility.replaceIntoTable();
		}
		if (sql != null)
			listOfCommands.add(sql);

		return listOfCommands;
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

	public MethodReflectionUtils getMethodReflectionUtils() {
		return methodReflectionUtils;
	}

	public abstract boolean isHasCastException();

}
