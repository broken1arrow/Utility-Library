package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class help you to wrap your command.
 */
public final class SqlCommandUtility {

	private final TableWrapper tableWrapper;
	private String columnsArray;

	public SqlCommandUtility(@Nonnull final TableWrapper tableWrapper) {
		this.tableWrapper = tableWrapper;
	}

	public TableWrapper getTableWrapper() {
		return tableWrapper;
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
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");
		columns.append(tableWrapper.getQuote()).append(primaryKey.getColumnName())
				.append(tableWrapper.getQuote())
				.append(" ")
				.append(primaryKey.getDatatype());

		for (final Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			TableRow column = entry.getValue();
			columns.append((columns.length() == 0) ? "" : ", ").append(tableWrapper.getQuote())
					.append(entry.getKey())
					.append(tableWrapper.getQuote())
					.append(" ").append(column.getDatatype());

			if (column.isAutoIncrement())
				columns.append(" AUTO_INCREMENT");

			else if (column.isNotNull())
				columns.append(" NOT NULL");

			if (column.getDefaultValue() != null)
				columns.append(" DEFAULT ").append("'").append(column.getDefaultValue()).append("'");
		}
		if (tableWrapper.isSupportMySQL())
			columns.append(", PRIMARY KEY (")
					.append(tableWrapper.getQuote())
					.append(primaryKey.getColumnName())
					.append(tableWrapper.getQuote())
					.append(")");
		else
			columns.append(", PRIMARY KEY (")
					.append(tableWrapper.getQuote())
					.append(primaryKey.getColumnName())
					.append(tableWrapper.getQuote())
					.append("(").append(this.tableWrapper.getPrimaryKeyLength()).append("))");


		String string = "CREATE TABLE IF NOT EXISTS " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " (" + columns + ")" + (tableWrapper.isSupportMySQL() ? "" : " DEFAULT CHARSET=utf8mb4" /*COLLATE=utf8mb4_unicode_520_ci*/) + ";";
		return string;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String replaceIntoTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		return "REPLACE INTO " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " " + this.merge() + ";";
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
		return "MERGE INTO " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " " + this.merge() + ";";
	}

	/**
	 * Update data in your database, on columns you added. Will replace old data on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String updateTable() {
		Set<String> records = this.getTableWrapper().getRecord();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		String record = this.getTableWrapper().getRecord().stream().findFirst().orElse(null);
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		return this.updateTable(record);
	}

	/**
	 * Update data in your database, on columns you added. Will replace old data on columns you added.
	 *
	 * @return list of prepared query's to run on your database.
	 */
	public List<String> updateTables() {
		List<String> list = new ArrayList<>();
		Set<String> records = getTableWrapper().getRecord();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		for (String record : records)
			list.add(this.updateTable(record));
		return list;
	}

	/**
	 * Select specific table.
	 *
	 * @return the constructed SQL command for get the table.
	 */
	public String selectTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		return "SELECT * FROM " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + ";";
	}

	/**
	 * Select specific table.
	 *
	 * @param columnValue the primary key value you want to get from database.
	 * @return the constructed SQL command for get the table.
	 */
	public String selectRow(String columnValue) {
		TableWrapper tableWrapper = this.getTableWrapper();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		return "SELECT * FROM " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " WHERE " + tableWrapper.getQuote() + primaryKey.getColumnName() + tableWrapper.getQuote() + " = '" + columnValue + "';";
	}

	/**
	 * Remove a specific row from the table.
	 *
	 * @param value the primary key value you want to remove from database.
	 * @return the constructed SQL command for remove the row.
	 */
	public String removeRow(final String value) {
		TableWrapper tableWrapper = this.getTableWrapper();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");

		return "DELETE FROM " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " WHERE " + tableWrapper.getQuote() + primaryKey.getColumnName() + tableWrapper.getQuote() + " = '" + value + "'";
	}

	/**
	 * Remove this table from the database.
	 *
	 * @return the constructed SQL command for drop the table.
	 */
	public String dropTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		return "DROP TABLE `" + tableWrapper.getTableName() + "`";
	}

	/**
	 * Creates the command for update the row.
	 *
	 * @param record the record match in the database to update.
	 * @return the constructed SQL command for the database.
	 */
	private String updateTable(String record) {
		TableWrapper tableWrapper = this.getTableWrapper();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Map<String, TableRow> tableRowMap = tableWrapper.getColumns();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primary key, for update records in the table or all records get updated.");
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		final StringBuilder columns = new StringBuilder();
		int index = 0;
		columns.append(tableWrapper.getQuote())
				.append(primaryKey.getColumnName())
				.append(tableWrapper.getQuote())
				.append(" ");
		columns.append("= '").append(primaryKey.getColumnValue()).append(tableRowMap.size() > 0 ? "', " : "' ");

		for (Entry<String, TableRow> entry : tableRowMap.entrySet()) {
			index++;
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			final boolean endOfString = index == tableRowMap.size();
			Object value = column.getColumnValue();
			if (value == null && column.isNotNull())
				value = "";
			if (value == null && column.getDefaultValue() != null)
				value = column.getDefaultValue();
			columns.append(tableWrapper.getQuote()).append(columnName).append(tableWrapper.getQuote());
			if (columns.length() == 0 || endOfString) {
				columns.append(" = ").append(value == null ? null : " '" + value + "'");
			} else {
				columns.append(" = ").append(value == null ? null + "," : " '" + value + "',");
			}
		}
		return "UPDATE " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " SET " + columns + " WHERE " + tableWrapper.getQuote() + primaryKey.getColumnName() + tableWrapper.getQuote() + " = '" + record + "'" + ";";
	}

	/**
	 * Constructs the SQL command for merging data into a database table.
	 * The resulting command can be executed using "REPLACE INTO" or "MERGE INTO".
	 *
	 * @return the constructed SQL command for database merging.
	 */
	public StringBuilder merge() {
		TableWrapper tableWrapper = this.getTableWrapper();
		final StringBuilder columns = new StringBuilder();
		final StringBuilder values = new StringBuilder();
		TableRow primaryKey = tableWrapper.getPrimaryRow();
		Map<String, TableRow> tableRowMap = tableWrapper.getColumns();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primary key, for update records in the table or all records get updated.");

		int index = 0;
		columns.append(tableWrapper.getQuote())
				.append("(")
				.append(primaryKey.getColumnName())
				.append(tableWrapper.getQuote())
				.append(tableRowMap.size() > 0 ? ", " : " ");
		values.append("'").append(primaryKey.getColumnValue()).append(tableRowMap.size() > 0 ? "', " : "' ");
		for (Entry<String, TableRow> entry : tableWrapper.getColumns().entrySet()) {
			index++;
			//for (int index = 0; index < this.getColumns().size(); index++) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			final boolean endOfString = index == tableRowMap.size();
			columns.append((columns.length() == 0) ? "(" : "").append(tableWrapper.getQuote()).append(columnName).append(tableWrapper.getQuote()).insert(columns.length(), columns.length() == 0 || endOfString ? "" : ",");
			Object value = column.getColumnValue();
			if (value == null && column.isNotNull())
				value = "";
			if (value == null && column.getDefaultValue() != null)
				value = column.getDefaultValue();
			values.append(value != null ? "'" : "").append(value).insert(values.length(), values.length() == 0 || endOfString ? "" : value == null ? ", " : "',");
		}
		columns.insert(columns.length(), ") VALUES(" + values + "')");

		return columns;
	}
}
