package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.log.Validate;
import org.broken.arrow.database.library.utility.serialize.ConfigurationSerializeUtility;

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
	protected boolean batchUpdateGoingOn = false;
	private final Map<String, TableWrapper> tables = new HashMap<>();
	protected boolean hasStartWriteToDb = false;
	private final boolean sQlittle;
	private Set<String> removeColums = new HashSet<>();

	public Database() {
		this.sQlittle = this instanceof SQLite;
	}

	public abstract Connection connect();

	public void createTables() {
		Validate.checkBoolean(tables.isEmpty(), "The table is empty, add tables to the map before call this method");
		try {
			openConnection();
			for (final Entry<String, TableWrapper> entitytables : tables.entrySet()) {
				load(entitytables);
			}
			try {
				for (final Entry<String, TableWrapper> entityTables : tables.entrySet()) {
					final List<String> columns = updateTableColumnsInDb(entityTables.getKey());
					createMissingColums(entityTables.getValue(), columns);
				}
			} catch (final SQLException throwables) {
				throwables.printStackTrace();
			}
		} finally {
			closeConnection();
		}
	}

	protected List<String> updateTableColumnsInDb(final String tableName) throws SQLException {
		if (!openConnection()) return new ArrayList<>();
		if (this.connection.isClosed()) return new ArrayList<>();

		final List<String> column = new ArrayList<>();
		final PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM " + tableName);
		final ResultSet rs = statement.executeQuery();
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columnCount = rsmd.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			column.add(rsmd.getColumnName(i));
		}
		close(statement, rs);
		return column;
	}

	/**
	 * Saves all row to the specified database table, based on the provided primary key and associated data.
	 * Note that this method can also use previously added records using either the {@link TableWrapper#addRecord(String)}
	 * or {@link TableWrapper#addAllRecord(Set)} methods and it will then update all rows you added. If only one
	 * record added it will update only one row.
	 *
	 * @param utilityMap new data you want to cache to database.
	 *                   Note the key need to be the table name you want to update.
	 */
	public void saveAll(@Nonnull final Map<String, DataWrapper> utilityMap) {
		final List<String> sqls = new ArrayList<>();

		for (final TableWrapper tableWrapper : this.getTables().values()) {
			DataWrapper dataWrapper = utilityMap.get(tableWrapper.getTableName());
			if (dataWrapper == null) continue;
			String primaryKey = dataWrapper.getPrimaryKey();

			if (!primaryKey.isEmpty()) {
				TableRow column = tableWrapper.getPrimaryRow();
				if (column != null)
					tableWrapper.setPrimaryRow(column.getBuilder().setColumnValue(dataWrapper.getValue()).build());
			}
			for (Entry<String, Object> entry : dataWrapper.getConfigurationSerialize().serialize().entrySet()) {
				TableRow column = tableWrapper.getColumn(entry.getKey());
				if (column == null) continue;
				tableWrapper.addCustom(entry.getKey(), column.getBuilder().setColumnValue(entry.getValue()));

			}
			this.getSqlsCommands(sqls, tableWrapper);
		}
		this.batchUpdate(sqls);
	}

	/**
	 * Saves a single row to the specified database table, based on the provided primary key and associated data.
	 * Note that this method can also use previously added records using either the {@link TableWrapper#addRecord(String)}
	 * or {@link TableWrapper#addAllRecord(Set)} methods and it will then update all rows you added. If not set or only one
	 * record added it will update only one row.
	 *
	 * @param tableName     The name of the table to save the row to.
	 * @param primaryKey    The primary key column for the row.
	 * @param primaryValue  The value for the primary key column.
	 * @param configuration The serialized data to be stored with the primary key.
	 */

	public void save(@Nonnull final String tableName, @Nonnull final String primaryKey, @Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializeUtility configuration) {
		final List<String> sqls = new ArrayList<>();
		TableWrapper tableWrapper = this.getTable(tableName);
		if (tableWrapper == null) {
			LogMsg.warn("Could not find table " + tableName);
			return;
		}
		if (!primaryKey.isEmpty()) {
			TableRow primaryRow = tableWrapper.getPrimaryRow();
			if (primaryRow != null)
				tableWrapper.setPrimaryRow(primaryRow.getBuilder().setColumnValue(primaryValue).build());
		}
		for (Entry<String, Object> entry : configuration.serialize().entrySet()) {
			TableRow column = tableWrapper.getColumn(entry.getKey());
			if (column == null) continue;
			tableWrapper.addCustom(entry.getKey(), column.getBuilder().setColumnValue(entry.getValue()));
		}
		this.getSqlsCommands(sqls, tableWrapper);
		this.batchUpdate(sqls);
	}

	public boolean load(final String tableName) {
		if (!openConnection()) return false;

		try {
			TableWrapper wrapperEntry = this.getTable(tableName);
			if (wrapperEntry == null) {
				LogMsg.warn("Could not find table " + tableName);
				return false;
			}
			final PreparedStatement statement = this.connection.prepareStatement(wrapperEntry.createTable());
			statement.executeUpdate();
			close(statement);
			return true;
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void load(final Entry<String, TableWrapper> wrapperEntry) {
		if (!openConnection()) return;

		try {
			final PreparedStatement statement = this.connection.prepareStatement(wrapperEntry.getValue().createTable());
			statement.executeUpdate();
			close(statement);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		TableRow wraper = wrapperEntry.getValue().getColumns().values().stream().findFirst().orElse(null);
		Validate.checkNotNull(wraper, "Could not find a column for this table " + wrapperEntry.getKey());
		checkIfTableExist(wrapperEntry.getKey(), wraper.getColumnName());
	}

	public void remove(final String tableName, final String columnName, final String value) {
		final String sql = "DELETE FROM `" + tableName + "` WHERE  `" + columnName + "` = `" + value + "`";
		this.batchUpdate(Collections.singletonList(sql));
	}

	public void dropTable(final String tableName) {
		final String sql = "DROP TABLE `" + tableName + "`";
		this.batchUpdate(Collections.singletonList(sql));
	}

	protected abstract void batchUpdate(@Nonnull final List<String> batchupdate);

	protected void batchUpdate(@Nonnull final List<String> batchupdate, int resultSetType, int resultSetConcurrency) {
		final ArrayList<String> sqls = new ArrayList<>(batchupdate);
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

				LogMsg.info("Updated " + processedCount + " database entries.");
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


	protected void dropColumn(final String createTableCmd, final List<String> existingColumns, final String tableName) throws SQLException {

		final TableWrapper updatedTableColumns = tables.get(tableName);

		if (updatedTableColumns == null || updatedTableColumns.getColumns().isEmpty()) return;
		// Remove the columns we don't want anymore from the table's list of columns

		if (this.removeColums != null)
			for (final String removed : this.removeColums) {
				existingColumns.remove(removed);
			}
		final String columnsSeperated = getColumsFromTable(updatedTableColumns.getColumns().values());
		if (!openConnection()) return;

		// Rename the old table, so we can remove old name and rename colums.
		final PreparedStatement alterTable = connection.prepareStatement("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");
		// Creating the table on its new format (no redundant columns)
		final PreparedStatement createTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsSeperated + ");");

		alterTable.execute();
		createTable.execute();

		// Populating the table with the data
		final PreparedStatement movedata = connection.prepareStatement("INSERT INTO " + tableName + "(" + TextUtils(existingColumns) + ") SELECT "
				+ TextUtils(existingColumns) + " FROM " + tableName + "_old;");
		movedata.execute();

		final PreparedStatement removeOldtable = connection.prepareStatement("DROP TABLE " + tableName + "_old;");
		removeOldtable.execute();

		close(movedata, alterTable, createTable, removeOldtable);
	}

	protected void createMissingColums(final TableWrapper tableWrapper, final List<String> existingColumns) throws SQLException {
		if (existingColumns == null) return;
		if (!openConnection()) return;

		for (final Entry<String, TableRow> entry : tableWrapper.getColumns().entrySet()) {
			String columnName = entry.getKey();
			TableRow tableRow = entry.getValue();
			if (removeColums.contains(columnName)) continue;
			if (existingColumns.contains(columnName)) continue;
			try {
				final PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + tableWrapper.getTableName() + "` ADD `" + columnName + "` " + tableRow.getDatatype() + ";");
				statement.execute();
			} catch (final SQLException throwables) {
				throwables.printStackTrace();
			}
		}
	}


	private void checkIfTableExist(String tableName, String columName) {
		try {
			if (this.connection == null) return;

			final PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM `" + tableName + "` WHERE `" + columName + "` = ?");
			preparedStatement.setString(1, "");
			final ResultSet resultSet = preparedStatement.executeQuery();
			close(preparedStatement, resultSet);

		} catch (final SQLException ex) {
			LogMsg.warn("Unable to retreive connection ", ex);
		}
	}


	protected String getColumsFromTable(final Collection<TableRow> colums) {
		final StringBuilder columRow = new StringBuilder();
		for (final TableRow colum : colums) {
			columRow.append(colum).append(" ");
		}
		return columRow.toString();
	}

	protected List<String> getSqlsCommands(final List<String> listOfCommands, TableWrapper tableWrapper) {
		String sql = null;
		if (tableWrapper.getRecord() != null && !tableWrapper.getRecord().isEmpty()) {
			if (tableWrapper.getRecord().size() > 1) {
				listOfCommands.addAll(tableWrapper.updateTables());
			} else
				sql = tableWrapper.updateTable();
		} else
			sql = tableWrapper.replaceIntoTable();
		if (sql != null)
			listOfCommands.add(sql);

		return listOfCommands;
	}

	protected String TextUtils(final List<String> colums) {
		return colums.toString().replace("[", "").replace("]", "");
	}

	public boolean isHasStartWriteToDb() {
		return hasStartWriteToDb;
	}

	public Set<String> getRemoveColums() {
		return removeColums;
	}

	public void setRemoveColums(final Set<String> removeColums) {
		this.removeColums = removeColums;
	}

	public boolean issQlittle() {
		return sQlittle;
	}

	@Nonnull
	public Map<String, TableWrapper> getTables() {
		return tables;
	}

	@Nullable
	public TableWrapper getTable(String tableName) {
		return tables.get(tableName);
	}

	public void addTable(TableWrapper tableWrapper) {
		tables.put(tableWrapper.getTableName(), tableWrapper);
	}

	public boolean openConnection() {
		try {
			if (this.connection == null || this.connection.isClosed())
				this.connection = connect();
		} catch (SQLException e) {
			LogMsg.warn("Could not check if the connection is closed ", e);
			return false;
		}
		return true;
	}

	protected void closeConnection() {
		try {
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (final SQLException exception) {
			exception.printStackTrace();
		}
	}

	public abstract boolean isHasCastExeption();

}
