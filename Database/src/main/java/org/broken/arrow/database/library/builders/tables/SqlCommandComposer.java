package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.SqlQueryBuilder;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class help you to wrap your command.
 */
public final class SqlCommandComposer {

	private final TableWrapper tableWrapper;
	private final RowWrapper rowWrapper;
	private final Database<?> database;
	private SqlQueryBuilder queryBuilder;
	private final Map<Integer, Object> cachedDataByColumn = new HashMap<>();
	private Set<String> columnsToUpdate;
	private final StringBuilder preparedSQLBatch = new StringBuilder();
	private String queryCommand = "";
	private final char quote;

	public SqlCommandComposer(@Nonnull final RowWrapper rowWrapper, Database<?> database) {
		this.tableWrapper = rowWrapper.getTableWrapper();
		this.rowWrapper = rowWrapper;
		this.database = database;
		this.quote = database.getQuote();
	}

	public TableWrapper getTableWrapper() {
		return tableWrapper;
	}

	public RowWrapper getColumnWrapper() {
		return rowWrapper;
	}

	/**
	 * Retrieves the cached column names and their corresponding values.
	 *
	 * @return An unmodifiable map with the column name as the key and the associated data as the value.
	 */
	public Map<Integer, Object> getCachedDataByColumn() {
		return Collections.unmodifiableMap(cachedDataByColumn);
	}

	public String getPreparedSQLBatch() {
		return preparedSQLBatch.toString();
	}

	/**
	 * Set the columns name you only want to update.
	 *
	 * @param columnsToUpdate the columns you want to update.
	 */
	public void setColumnsToUpdate(final String... columnsToUpdate) {
		this.columnsToUpdate = new HashSet<>(Arrays.asList(columnsToUpdate));
	}

	/**
	 * Retrieves the SQL command that has been constructed for execution in the database.
	 * <p>&nbsp;</p>
	 * <p>
	 * Note that the returned string has not been checked for potential SQL injection or
	 * other security vulnerabilities. It is recommended to use {@link #getPreparedSQLBatch()}
	 * and {@link #getCachedDataByColumn()} in conjunction with the preparedStatement method to ensure
	 * safer query execution.
	 * </p>
	 *
	 * @return The SQL command that has been constructed.
	 */
	public String getQueryCommand() {
		return queryCommand;
	}

	/**
	 * Create new table with columns,data type and primary key you have set.
	 * From this methods {@link org.broken.arrow.database.library.builders.tables.TableWrapper#add(String, String)}, {@link org.broken.arrow.database.library.builders.tables.TableWrapper#addNotNull(String, String)}
	 * {@link org.broken.arrow.database.library.builders.tables.TableWrapper#addDefault(String, String, Object)} {@link org.broken.arrow.database.library.builders.tables.TableWrapper#addAutoIncrement(String, String)}
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String createTable() {
		TableWrapper tableData = this.getTableWrapper();
		final StringBuilder columns = new StringBuilder();
		TableRow primaryKey = tableData.getPrimaryRow();
		Map<String, TableRow> tableRowMap = tableData.getColumns();
		char quoteColumnName = this.quote;

		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		columns.append(quoteColumnName).append(primaryKey.getColumnName())
				.append(quoteColumnName)
				.append(" ")
				.append(primaryKey.getDatatype());

		for (final Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			TableRow column = entry.getValue();
			columns.append((columns.length() == 0) ? "" : ", ").append(quoteColumnName)
					.append(entry.getKey())
					.append(quoteColumnName)
					.append(" ").append(column.getDatatype());

			if (column.isAutoIncrement())
				columns.append(" AUTO_INCREMENT");

			else if (column.isNotNull())
				columns.append(" NOT NULL");

			if (column.getDefaultValue() != null)
				columns.append(" DEFAULT ").append("'").append(column.getDefaultValue()).append("'");
		}
		columns.append(", PRIMARY KEY (")
				.append(quoteColumnName)
				.append(primaryKey.getColumnName())
				.append(quoteColumnName);
		if (tableData.getPrimaryKeyLength() > 0)
			columns.append("(").append(tableData.getPrimaryKeyLength()).append("))");
		else
			columns.append(")");

		String string = "CREATE TABLE IF NOT EXISTS " + quoteColumnName + tableData.getTableName() + quoteColumnName + " (" + columns + ")" + (!database.getCharacterSet().isEmpty() ? " " + database.getCharacterSet() : "" /*COLLATE=utf8mb4_unicode_520_ci*/) + ";";
		return string;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String replaceIntoTable() {
		TableWrapper tableData = this.getTableWrapper();
		queryCommand = "REPLACE INTO " + this.quote + tableData.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "REPLACE INTO " + this.quote + tableData.getTableName() + this.quote + " " + " ");
		return queryCommand;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String insertIntoTable() {
		TableWrapper tableData = this.getTableWrapper();
		queryCommand = "INSERT INTO " + this.quote + tableData.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "INSERT INTO " + this.quote + tableData.getTableName() + this.quote + " " + " ");
		return queryCommand;
	}

	/**
	 * Replace data in your database, on columns you added. This do same thing as
	 * "REPLACE INTO", but this command is often used with a database that not support
	 * "REPLACE INTO" for example H2 database.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String mergeIntoTable() {
		TableWrapper tableData = this.getTableWrapper();
		queryCommand = "MERGE INTO " + this.quote + tableData.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "MERGE INTO " + this.quote + tableData.getTableName() + this.quote + " ");
		return queryCommand;
	}

	/**
	 * Update data in your database from the provided record value, on columns you added. Will update old data on columns you added.
	 *
	 * @param record the record for the primary key you want to update.
	 */
	public void updateTable(Object record) {
		Validate.checkBoolean(record == null || record.toString().isEmpty(), "You need to set record value.You can't update the row without it.");
		queryCommand = this.createUpdateCommand(record);
	}

	/**
	 * Update data in your database from the list of record values, on columns you added. Will update old data on columns you added.
	 *
	 * @param records the records for the primary key you want to update.
	 * @return list of prepared query's to run on your database.
	 */
	@Deprecated
	public List<String> updateTables(List<String> records) {
		List<String> list = new ArrayList<>();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value.You can't update the row without it.");
		for (String record : records)
			list.add(this.createUpdateCommand(record));
		return list;
	}

	/**
	 * Select specific table.
	 *
	 * @return the constructed SQL command for get the table.
	 */
	public String selectTable() {
		TableWrapper tableData = this.getTableWrapper();
		return "SELECT * FROM " + this.quote + tableData.getTableName() + this.quote + ";";
	}

	/**
	 * Select specific table.
	 *
	 * @param primaryValue the primary key value you want to get from database.
	 * @return the constructed SQL command for get the table.
	 */
	public String selectRow(String primaryValue) {
		TableWrapper tableData = this.getTableWrapper();
		TableRow primaryKey = tableData.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		return "SELECT * FROM " + this.quote + tableData.getTableName() + this.quote + " WHERE " + this.quote + primaryKey.getColumnName() + this.quote + " = '" + primaryValue + "';";
	}

	/**
	 * Remove a specific row from the table.
	 *
	 * @param value the primary key value you want to remove from database.
	 */
	public void removeRow(final String value) {
		final TableWrapper tableData = this.getTableWrapper();
		final RowWrapper rowWrapper = getColumnWrapper();
		Validate.checkBoolean(rowWrapper.getPrimaryKey().isEmpty(), "You need set primaryKey, for drop the column in this table." + tableData.getTableName());
		queryCommand = "DELETE FROM " + this.quote + tableData.getTableName() + this.quote + " WHERE " + this.quote + rowWrapper.getPrimaryKey() + this.quote + " = '" + value + "';";
		preparedSQLBatch.insert(0, queryCommand);
	}

	/**
	 * Remove this table from the database.
	 */
	public void dropTable() {
		TableWrapper tableData = this.getTableWrapper();
		queryCommand = "DROP TABLE " + this.quote + tableData.getTableName() + this.quote + ";";
		preparedSQLBatch.insert(0, queryCommand);
	}

	/**
	 * Execute your own set command.
	 *
	 * @param command the sql command. You need to always validate and sanitize if you
	 *                use values players can set.
	 */
	public void setCustomCommand(String command) {
		if (this.tableWrapper.getPrimaryRow() != null) {
			this.queryCommand = command;
			preparedSQLBatch.insert(0, queryCommand);
		}
	}

	/**
	 * If you have set your own command. You can execute this method to
	 * set the command and retrieve it from the {@link #getPreparedSQLBatch()}.
	 * <p>
	 * If you parameterized the values, you then get the map with set indices and
	 * values here {@link #getCachedDataByColumn()}
	 */
	public void executeCustomCommand() {
		SqlQueryBuilder queryBuilder = this.getTableWrapper().getSqlQueryBuilder();
		if (queryBuilder != null) {
			String query = queryBuilder.getQuery();
			if (query == null) {
				LogMsg.warn("The query command is not set or not properly setup.");
				return;
			}
			Map<Integer, ColumnWrapper> columnValueMap = queryBuilder.getIndexCachedWithValue();
			if (!columnValueMap.isEmpty()) {
				columnValueMap.forEach((key, value) ->
						this.cachedDataByColumn.put(key, value.getColumnValue()));
			}
			queryCommand = query;
			preparedSQLBatch.insert(0, query);
		}
	}

	/**
	 * Creates the command for update the row.
	 *
	 * @param record the record match in the database to update.
	 * @return the constructed SQL command for the database.
	 */
	private String createUpdateCommand(Object record) {
		final TableWrapper tableData = this.getTableWrapper();
		final RowWrapper rowWrapper = getColumnWrapper();
		final Map<String, TableRow> tableRowMap = tableData.getColumns();
		final char quote = this.quote;
		Validate.checkNotNull(rowWrapper, "The RowWrapper instance you try to save is null, for this record: " + record);
		Validate.checkBoolean(rowWrapper.getPrimaryKey().isEmpty(), "You need set primary key, for update records in the table.");
		Validate.checkBoolean(record == null || record.equals(""), "You need to set record value for the primary key. When you want to update the row.");

		final StringBuilder prepareColumnsToUpdate = new StringBuilder();
		final StringBuilder columns = new StringBuilder();

		int index = 1;
		for (Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			if (columnsToUpdate != null && !columnsToUpdate.isEmpty() && !columnsToUpdate.contains(columnName)) {
				continue;
			}
			Object value = rowWrapper.getColumnValue(columnName);
			index = setColumData(value, prepareColumnsToUpdate, columns, index, columnName, column);
		}
		this.cachedDataByColumn.put(index, record);
		prepareColumnsToUpdate.setLength(prepareColumnsToUpdate.length() - 1);
		preparedSQLBatch.append("UPDATE ")
				.append(quote)
				.append(tableData.getTableName())
				.append(quote).append(" SET ")
				.append(prepareColumnsToUpdate).append(" WHERE ")
				.append(quote)
				.append(rowWrapper.getPrimaryKey())
				.append(quote)
				.append(" = ")
				.append(" ? ")
				.append(";");
		columns.setLength(columns.length() - 2);
		return "UPDATE " + quote + tableData.getTableName() + quote + " SET " + columns + " WHERE " + quote + rowWrapper.getPrimaryKey() + quote + " = " + quote + record + quote + ";";
	}

	private int setColumData(Object value,final StringBuilder prepareColumnsToUpdate, final StringBuilder columns, int index, final String columnName, final TableRow column) {
		final char quoteColumnName = this.quote;

		value = getValue(value, column);
		columns.append(quoteColumnName)
				.append(columnName)
				.append(quoteColumnName);
		columns.append(" = ").append(value == null ? null + "," : "'" + value + "', ");

		prepareColumnsToUpdate.append(quoteColumnName)
				.append(columnName)
				.append(quoteColumnName)
				.append(" = ?,");
		this.cachedDataByColumn.put(index++, value);
		return index;
	}

	/**
	 * Constructs the SQL command for merging data into a database table.
	 * The resulting command can be executed using "REPLACE INTO" or "MERGE INTO".
	 *
	 * @return the constructed SQL command for database merging.
	 */
	public StringBuilder merge() {
		final StringBuilder columns = new StringBuilder();
		final StringBuilder values = new StringBuilder();
		final StringBuilder prepareValues = new StringBuilder();
		final TableWrapper tableData = this.getTableWrapper();
		final Map<String, TableRow> tableRowMap = tableData.getColumns();
		final RowWrapper saveWrapper = getColumnWrapper();
		final char quoteColumnName = this.quote;

		Validate.checkBoolean(saveWrapper.getPrimaryKey().isEmpty(), "You need set primary key, for update records in the table or no records get updated.");
		Object primaryKeyColumnValue = saveWrapper.getPrimaryKeyValue();
		Validate.checkNotNull(primaryKeyColumnValue, "Value for primary key can't be null.");

		columns.append("(")
				.append(quoteColumnName)
				.append(saveWrapper.getPrimaryKey())
				.append(quoteColumnName)
				.append(tableRowMap.size() > 0 ? "," : " ");

		values.append("'");
		values.append(primaryKeyColumnValue);
		values.append(tableRowMap.size() > 0 ? "'," : "' ");
		this.cachedDataByColumn.put(1, primaryKeyColumnValue);
		prepareValues.append("?,");
		this.mergeColumns(columns, values, prepareValues, tableRowMap, saveWrapper);

		columns.setLength(columns.length() - 1);
		values.setLength(values.length() - 1);
		prepareValues.setLength(prepareValues.length() - 1);
		preparedSQLBatch.append(columns).append(") VALUES (").append(prepareValues).append(")");
		columns.insert(columns.length(), ") VALUES(" + values + ")");

		return columns;
	}

	private void mergeColumns(final StringBuilder columns, final StringBuilder values, final StringBuilder prepareValues, final Map<String, TableRow> tableRowMap, final RowWrapper saveWrapper) {
		final char quoteColumnName = this.quote;
		int index = 2;
		for (Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();

			columns.append((columns.length() == 0) ? "(" : "").append(quoteColumnName).append(columnName).append(quoteColumnName).insert(columns.length(), columns.length() == 0 ? "" : ",");
			Object value = saveWrapper.getColumnValue(columnName);
			value = getValue(value, column);
			boolean setQuote = value != null;
			if (!column.getDatatype().equalsIgnoreCase("VARCHAR"))
				setQuote = false;

			values.append(setQuote ? "'" : "").append(value).insert(values.length(), values.length() == 0 ? "" : setQuote ? "," : "',");
			prepareValues.append("?,");
			this.cachedDataByColumn.put(index++, value);
		}
	}

	@Nullable
	private Object getValue(Object value, final TableRow column) {
		if (value == null && column.isNotNull())
			value = "";
		if (value == null && column.getDefaultValue() != null)
			value = column.getDefaultValue();
		return value;
	}

}
