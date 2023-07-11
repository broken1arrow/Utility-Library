package org.broken.arrow.database.library.builders;


import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.log.Validate;
import org.broken.arrow.database.library.log.Validate.CatchExceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public final class TableWrapper {
	private final TableWrapper table;
	private TableRow primaryRow;
	private final String tableName;
	private Set<String> record;
	private String columnsArray;
	private final int primaryKeyLength;
	private final boolean supportMySQL;
	private final Map<String, TableRow> columns = new LinkedHashMap<>();

	private TableWrapper() {
		throw new CatchExceptions("You should not try create empty constructor");
	}

	private TableWrapper(@Nonnull final String tableName, @Nonnull final TableRow tableRow, final boolean supportMySQL) {
		this(tableName, tableRow, 120, supportMySQL);
	}

	private TableWrapper(@Nonnull final String tableName, @Nonnull final TableRow primaryRow, final int valueLength, final boolean supportMySQL) {
		Validate.checkNotEmpty(tableName, "Table name is empty.");
		Validate.checkNotEmpty(primaryRow, "Primary key is empty, if you not set this you can`t have unique rows in the database.");
		this.supportMySQL = supportMySQL;
		this.tableName = tableName;
		this.primaryRow = primaryRow;
		this.primaryKeyLength = valueLength > 0 ? valueLength : 120;
		this.table = this;
	}

	/**
	 * Creates a new TableWrapper object to build a database command for creating a table.
	 * Use the provided methods to add columns and define table properties. You can then call {@link TableWrapper#createTable()}
	 * to construct the final database command string or use {@link org.broken.arrow.database.library.Database#createTables()}.
	 *
	 * @param tableName  the name of the table.
	 * @param primaryRow the key that serves as the primary key. If not set, duplicate records may be added.
	 * @param support    a flag indicating whether certain commands and settings are supported in the target SQL database.
	 *                   Set to `true` to enable support, or `false` to disable it. Use this option if you encounter errors.
	 * @return a TableWrapper object that allows you to add columns and define properties for the table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull TableRow primaryRow, final boolean support) {
		return new TableWrapper(tableName, primaryRow, support).getTableWrapper();
	}

	/**
	 * Creates a new TableWrapper object to build a database command for creating a table.
	 * Use the provided methods to add columns and define table properties. You can then call {@link TableWrapper#createTable()}
	 * to construct the final database command string or use {@link org.broken.arrow.database.library.Database#createTables()}.
	 *
	 * @param tableName   name on your table.
	 * @param primaryRow  the key that serves as the primary key. If not set, duplicate records may be added.
	 * @param valueLength Length of the value for primary key (used for text and similar in SQL database).
	 * @param support     a flag indicating whether certain commands and settings are supported in the target SQL database.
	 *                    *                    Set to `true` to enable support, or `false` to disable it. Use this option if you encounter errors.
	 * @return TableWrapper class you need then add columns to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull TableRow primaryRow, final int valueLength, final boolean support) {
		return new TableWrapper(tableName, primaryRow, valueLength, support).getTableWrapper();
	}

	public TableWrapper getTableWrapper() {
		return table;
	}

	public TableWrapper add(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).build());
		return this;
	}

	public TableWrapper addDefault(final String columnName, final String datatype, final Object defaultValue) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setDefaultValue(defaultValue).build());
		return this;
	}

	public TableWrapper addAutoIncrement(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setAutoIncrement(true).build());
		return this;
	}

	public TableWrapper addNotNull(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setNotNull(true).build());
		return this;
	}

	public TableWrapper addCustom(final String columnName, final TableRow.Builder builder) {
		columns.put(columnName, builder.build());
		return this;
	}

	/**
	 * The value for the primary key.
	 *
	 * @param record the row you want to update in the database.
	 * @return this class.
	 */
	public TableWrapper addRecord(final String record) {
		if (this.record == null)
			this.record = new HashSet<>();

		this.record.add(record);
		return this;
	}

	/**
	 * The value`s for the primary key.
	 *
	 * @param record the row`s you want to update in the database.
	 * @return this class.
	 */
	public TableWrapper addAllRecord(final Set<String> record) {
		if (this.record == null)
			this.record = new HashSet<>();

		this.record.addAll(record);
		return this;
	}

	public TableWrapper setPrimaryRow(@Nonnull final TableRow primaryRow) {
		this.primaryRow = primaryRow;
		return this;
	}

	@Nullable
	public TableRow getPrimaryRow() {
		return primaryRow;
	}

	public int getPrimaryKeyLength() {
		return primaryKeyLength;
	}

	public Set<String> getRecord() {
		return record;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, TableRow> getColumns() {
		return columns;
	}

	@Nullable
	public TableRow getColumn(String columnName) {
		return columns.get(columnName);
	}

	public String getColumnsArray() {
		return columnsArray;
	}

	public boolean isSupportMySQL() {
		return supportMySQL;
	}

	/**
	 * Create new table with columns,data type and primary key you have set.
	 * From this methods {@link #add(String, String)}, {@link #addNotNull(String, String)}
	 * {@link #addDefault(String, String, Object)} {@link #addAutoIncrement(String, String)}
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String createTable() {
		final StringBuilder columns = new StringBuilder();
		TableRow primaryKey = this.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primaryKey, for create a table.");
		columns.append("`").append(primaryKey.getColumnName()).append("` ").append(primaryKey.getDatatype());

		for (final Entry<String, TableRow> entry : this.getColumns().entrySet()) {
			TableRow column = entry.getValue();
			columns.append((columns.length() == 0) ? "" : ", ").append("`").append(entry.getKey()).append("` ").append(column.getDatatype());

			if (column.isAutoIncrement())
				columns.append(" AUTO_INCREMENT");

			else if (column.isNotNull())
				columns.append(" NOT NULL");

			if (column.getDefaultValue() != null)
				columns.append(" DEFAULT ").append("'").append(column.getDefaultValue()).append("'");
		}
		if (this.isSupportMySQL())
			columns.append(", PRIMARY KEY (`").append(primaryKey.getColumnName()).append("`)");
		else
			columns.append(", PRIMARY KEY (`").append(primaryKey.getColumnName()).append("`(").append(this.getPrimaryKeyLength()).append("))");

		columnsArray = this.columns.values().stream().map(TableRow::getColumnName).collect(Collectors.joining(","));

		String string = "CREATE TABLE IF NOT EXISTS `" + this.getTableName() + "` (" + columns + ")" + (this.isSupportMySQL() ? "" : " DEFAULT CHARSET=utf8mb4" /*COLLATE=utf8mb4_unicode_520_ci*/) + ";";
		return string;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String replaceIntoTable() {
		return "REPLACE INTO `" + this.getTableName() + "` " + this.merge() + ";";
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String mergeIntoTable() {
		return "MERGE INTO `" + this.getTableName() + "` " + this.merge() + ";";
	}

	/**
	 * Update data in your database, on columns you added. Will replace old data on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String updateTable() {
		Set<String> records = this.getRecord();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		String record = this.getRecord().stream().findFirst().orElse(null);
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
		Set<String> records = this.getRecord();
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
		return "SELECT * FROM  `" + this.getTableName() + "`";
	}

	/**
	 * Select specific table.
	 *
	 * @param columnValue the primary key value you want to get from database.
	 * @return the constructed SQL command for get the table.
	 */
	public String selectRow(String columnValue) {
		TableRow primaryKey = this.getPrimaryRow();
		return "SELECT * FROM  `" + this.getTableName() + "` WHERE `" + primaryKey.getColumnName() + "` = '" + columnValue + "'";
	}

	/**
	 * Remove a specific row from the table.
	 *
	 * @param value the primary key value you want to remove from database.
	 * @return the constructed SQL command for remove the row.
	 */
	public String removeRow(final String value) {
		TableRow primaryKey = this.getPrimaryRow();
		return "DELETE FROM `" + this.getTableName() + "` WHERE  `" + primaryKey.getColumnName() + "` = `" + value + "`";
	}

	/**
	 * Remove this table from the database.
	 *
	 * @return the constructed SQL command for drop the table.
	 */
	public String dropTable() {
		return "DROP TABLE `" + this.getTableName() + "`";
	}

	/**
	 * Creates the command for update the row.
	 *
	 * @param record the record match in the database to update.
	 * @return the constructed SQL command for the database.
	 */
	private String updateTable(String record) {
		TableRow primaryKey = this.getPrimaryRow();
		Validate.checkBoolean(primaryKey == null || primaryKey.getColumnName() == null || primaryKey.getColumnName().equals("non"), "You need set primary key, for update records in the table or all records get updated.");
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		final StringBuilder columns = new StringBuilder();
		int index = 0;
		columns.append("`").append(primaryKey.getColumnName()).append("` ");
		columns.append("= '").append(primaryKey.getColumnValue()).append(this.getColumns().size() > 0 ? "', " : "' ");

		for (Entry<String, TableRow> entry : this.getColumns().entrySet()) {
			index++;
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			final boolean endOfString = index == this.getColumns().size();
			Object value = column.getColumnValue();
			if (value == null && column.isNotNull())
				value = "";
			if (value == null && column.getDefaultValue() != null)
				value = column.getDefaultValue();
			columns.append("`").append(columnName).append("`");
			if (columns.length() == 0 || endOfString) {
				columns.append(" = ").append(value == null ? null : " '" + value + "'");
			} else {
				columns.append(" = ").append(value == null ? null + "," : " '" + value + "',");
			}
		}
		return "UPDATE `" + this.getTableName() + "` SET " + columns + " WHERE `" + primaryKey.getColumnName() + "` = '" + record + "'" + ";";
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
		TableRow primaryKey = this.getPrimaryRow();
		int index = 0;
		columns.append("(`").append(primaryKey.getColumnName()).append(this.getColumns().size() > 0 ? "`, " : "` ");
		values.append("'").append(primaryKey.getColumnValue()).append(this.getColumns().size() > 0 ? "', " : "' ");
		for (Entry<String, TableRow> entry : this.getColumns().entrySet()) {
			index++;
			//for (int index = 0; index < this.getColumns().size(); index++) {
			final String columnName = entry.getKey();
			final TableRow column = entry.getValue();
			final boolean endOfString = index == this.getColumns().size();
			columns.append((columns.length() == 0) ? "(" : "").append("`").append(columnName).append("`").insert(columns.length(), columns.length() == 0 || endOfString ? "" : ",");
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
