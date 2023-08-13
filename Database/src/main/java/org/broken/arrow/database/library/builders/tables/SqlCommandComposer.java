package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.builders.ColumnWrapper;
import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class help you to wrap your command.
 */
public final class SqlCommandComposer {

	private final TableWrapper tableWrapper;
	private final ColumnWrapper saveWrapper;
	private final Database database;
	private final Map<Integer, Object> columns = new HashMap<>();
	private final StringBuilder preparedSQLBatch = new StringBuilder();
	private String queryCommand = "";
	private final char quote;

	public SqlCommandComposer(@Nonnull final ColumnWrapper columnWrapper, Database database) {
		this.tableWrapper = columnWrapper.getTableWrapper();
		this.saveWrapper = columnWrapper;
		this.database = database;
		this.quote = database.getQuote();
	}

	public TableWrapper getTableWrapper() {
		return tableWrapper;
	}

	public ColumnWrapper getColumnWrapper() {
		return saveWrapper;
	}

	public Map<Integer, Object> getColumns() {
		return columns;
	}

	public String getPreparedSQLBatch() {
		return preparedSQLBatch.toString();
	}

	/**
	 * Retrieves the SQL command that has been constructed for execution in the database.
	 * <p></p>
	 * <p>
	 * Note that the returned string has not been checked for potential SQL injection or
	 * other security vulnerabilities. It is recommended to use {@link #getPreparedSQLBatch()}
	 * and {@link #getColumns()} in conjunction with the preparedStatement method to ensure
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
		TableWrapper tableWrapper = this.getTableWrapper();
		final StringBuilder columns = new StringBuilder();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Map<String, TableRow> tableRowMap = tableWrapper.getColumns();
		char quote = this.quote;

		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		columns.append(quote).append(primaryKey.getColumnName())
				.append(quote)
				.append(" ")
				.append(primaryKey.getDatatype());

		for (final Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			TableRow column = entry.getValue();
			columns.append((columns.length() == 0) ? "" : ", ").append(quote)
					.append(entry.getKey())
					.append(quote)
					.append(" ").append(column.getDatatype());

			if (column.isAutoIncrement())
				columns.append(" AUTO_INCREMENT");

			else if (column.isNotNull())
				columns.append(" NOT NULL");

			if (column.getDefaultValue() != null)
				columns.append(" DEFAULT ").append("'").append(column.getDefaultValue()).append("'");
		}
		columns.append(", PRIMARY KEY (")
				.append(quote)
				.append(primaryKey.getColumnName())
				.append(quote);
		if (tableWrapper.getPrimaryKeyLength() > 0)
			columns.append("(").append(tableWrapper.getPrimaryKeyLength()).append("))");
		else
			columns.append(")");

		String string = "CREATE TABLE IF NOT EXISTS " + quote + tableWrapper.getTableName() + quote + " (" + columns + ")" + (!database.getCharacterSet().isEmpty() ? " " + database.getCharacterSet() : "" /*COLLATE=utf8mb4_unicode_520_ci*/) + ";";
		return string;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String replaceIntoTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		queryCommand = "REPLACE INTO " + this.quote + tableWrapper.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "REPLACE INTO " + this.quote + tableWrapper.getTableName() + this.quote + " " + " ");
		return queryCommand;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String insertIntoTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		queryCommand = "INSERT INTO " + this.quote + tableWrapper.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "INSERT INTO " + this.quote + tableWrapper.getTableName() + this.quote + " " + " ");
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
		TableWrapper tableWrapper = this.getTableWrapper();
		queryCommand = "MERGE INTO " + this.quote + tableWrapper.getTableName() + this.quote + " " + this.merge() + ";";
		preparedSQLBatch.insert(0, "MERGE INTO " + this.quote + tableWrapper.getTableName() + this.quote + " ");
		return queryCommand;
	}

	/**
	 * Update data in your database from the provided record value, on columns you added. Will update old data on columns you added.
	 *
	 * @param record the record for the primary key you want to update.
	 * @return list of prepared query's to run on your database.
	 */
	public String updateTable(String record) {
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value.You can't update the row without it.");
		return this.createUpdateCommand(record);
	}

	/**
	 * Update data in your database from the list of record values, on columns you added. Will update old data on columns you added.
	 *
	 * @param records the records for the primary key you want to update.
	 * @return list of prepared query's to run on your database.
	 */
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
		TableWrapper tableWrapper = this.getTableWrapper();
		return "SELECT * FROM " + this.quote + tableWrapper.getTableName() + this.quote + ";";
	}

	/**
	 * Select specific table.
	 *
	 * @param primaryValue the primary key value you want to get from database.
	 * @return the constructed SQL command for get the table.
	 */
	public String selectRow(String primaryValue) {
		TableWrapper tableWrapper = this.getTableWrapper();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		return "SELECT * FROM " + this.quote + tableWrapper.getTableName() + this.quote + " WHERE " + this.quote + primaryKey.getColumnName() + this.quote + " = '" + primaryValue + "';";
	}

	/**
	 * Remove a specific row from the table.
	 *
	 * @param value the primary key value you want to remove from database.
	 */
	public void removeRow(final String value) {
		final TableWrapper tableWrapper = this.getTableWrapper();
		final ColumnWrapper columnWrapper = getColumnWrapper();
		Validate.checkBoolean(columnWrapper.getPrimaryKey().isEmpty(), "You need set primaryKey, for drop the column in this table." + tableWrapper.getTableName());
		queryCommand = "DELETE FROM " + this.quote + tableWrapper.getTableName() + this.quote + " WHERE " + this.quote + columnWrapper.getPrimaryKey() + this.quote + " = '" + value + "';";
		preparedSQLBatch.insert(0, queryCommand);
	}

	/**
	 * Remove this table from the database.
	 */
	public void dropTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		queryCommand = "DROP TABLE " + this.quote + tableWrapper.getTableName() + this.quote + ";";
		preparedSQLBatch.insert(0, queryCommand);
	}

	/**
	 * Creates the command for update the row.
	 *
	 * @param record the record match in the database to update.
	 * @return the constructed SQL command for the database.
	 */
	/**
	 * Creates the command for update the row.
	 *
	 * @param record the record match in the database to update.
	 * @return the constructed SQL command for the database.
	 */
	private String createUpdateCommand(String record) {
		final TableWrapper tableWrapper = this.getTableWrapper();
		final ColumnWrapper columnWrapper = getColumnWrapper();
		final Map<String, TableRow> tableRowMap = tableWrapper.getColumns();
		final char quote = this.quote;

		Validate.checkNotNull(columnWrapper, "The ColumnWrapper instance you try to save is null, for this record: " + record);
		Validate.checkBoolean(columnWrapper.getPrimaryKey().isEmpty(), "You need set primary key, for update records in the table.");
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");

		final StringBuilder columns = new StringBuilder();
		for (Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			Object value = columnWrapper.getColumnValue(columnName);
			if (value == null && column.isNotNull())
				value = "";
			if (value == null && column.getDefaultValue() != null)
				value = column.getDefaultValue();
			columns.append(quote).append(columnName).append(quote);
			if (columns.length() == 0) {
				columns.append(" = ").append(value == null ? null : "'" + value + "'");
			} else {
				columns.append(" = ").append(value == null ? null + "," : "'" + value + "', ");
			}
		}
		columns.setLength(columns.length() - 2);
		return "UPDATE " + quote + tableWrapper.getTableName() + quote + " SET " + columns + " WHERE " + quote + columnWrapper.getPrimaryKey() + quote + " = " + quote + record + quote + ";";
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
		final TableWrapper tableWrapper = this.getTableWrapper();
		final Map<String, TableRow> tableRowMap = tableWrapper.getColumns();
		final ColumnWrapper saveWrapper = getColumnWrapper();
		final char quote = this.quote;

		Validate.checkBoolean(saveWrapper.getPrimaryKey().isEmpty(), "You need set primary key, for update records in the table or no records get updated.");
		Object primaryKeyColumnValue = saveWrapper.getPrimaryKeyValue();
		Validate.checkNotNull(primaryKeyColumnValue, "Value for primary key can't be null.");

		columns.append("(")
				.append(quote)
				.append(saveWrapper.getPrimaryKey())
				.append(quote)
				.append(tableRowMap.size() > 0 ? "," : " ");

		values.append("'");
		values.append(primaryKeyColumnValue);
		values.append(tableRowMap.size() > 0 ? "'," : "' ");
		this.columns.put(1, primaryKeyColumnValue);
		prepareValues.append("?,");
		int index = 2;
		for (Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();

			columns.append((columns.length() == 0) ? "(" : "").append(quote).append(columnName).append(quote).insert(columns.length(), columns.length() == 0 ? "" : ",");
			Object value = saveWrapper.getColumnValue(columnName);
			if (value == null && column.isNotNull())
				value = "";
			if (value == null && column.getDefaultValue() != null)
				value = column.getDefaultValue();
			boolean setQuote = value != null;
			if (column.getDatatype().equalsIgnoreCase("INTEGER"))
				setQuote = false;

			values.append(setQuote ? "'" : "").append(value).insert(values.length(), values.length() == 0 ? "" : setQuote ? "," : "',");
			prepareValues.append("?,");
			this.columns.put(index++, value);
		}
		columns.setLength(columns.length() - 1);
		values.setLength(values.length() - 1);
		prepareValues.setLength(prepareValues.length() - 1);
		preparedSQLBatch.append(columns).append(") VALUES (").append(prepareValues).append(")");
		columns.insert(columns.length(), ") VALUES(" + values + ")");

		return columns;
	}

}
