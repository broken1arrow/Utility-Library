package org.broken.arrow.database.library.builders.tables;


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

public final class TableWrapper {
	private final TableWrapper table;
	private TableRow primaryRow;
	private final String tableName;
	private Set<String> record;
	private String columnsArray;
	private String quoteColumnKey = "`";
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
		Validate.checkNotEmpty(primaryRow, "Primary key is empty, if you not set this you can't have unique rows in the database.");
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
	 *                   Set to true to enable support, or false to disable it. Use this option if you encounter errors.
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
	 *                    *                    Set to true to enable support, or false to disable it. Use this option if you encounter errors.
	 * @return TableWrapper class you need then add columns to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull TableRow primaryRow, final int valueLength, final boolean support) {
		return new TableWrapper(tableName, primaryRow, valueLength, support).getTableWrapper();
	}

	public TableWrapper getTableWrapper() {
		return table;
	}

	/**
	 * Add a column to database.
	 *
	 * @param columnName The name of the column.
	 * @param datatype   The datatype you will store inside this row.
	 * @return instance of this class.
	 */
	public TableWrapper add(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setIsPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).build());
		return this;
	}

	/**
	 * Add a column to database, with a default value.
	 *
	 * @param columnName   The name of the column.
	 * @param datatype     The datatype you will store inside this row.
	 * @param defaultValue The value to set if the value is null.
	 * @return instance of this class.
	 */
	public TableWrapper addDefault(final String columnName, final String datatype, final Object defaultValue) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setIsPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setDefaultValue(defaultValue).build());
		return this;
	}

	/**
	 * Add a column to database, with auto increment the row.
	 *
	 * @param columnName The name of the column.
	 * @param datatype   The datatype you will store inside this row.
	 * @return instance of this class.
	 */
	public TableWrapper addAutoIncrement(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setIsPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setAutoIncrement(true).build());
		return this;
	}

	/**
	 * Add a column to database were the value could not be null.
	 *
	 * @param columnName The name of the column.
	 * @param datatype   The datatype you will store inside this row.
	 * @return instance of this class.
	 */
	public TableWrapper addNotNull(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setIsPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setNotNull(true).build());
		return this;
	}

	/**
	 * Add a column to database with your own settings.
	 *
	 * @param columnName The name of the column.
	 * @return instance of this class.
	 */
	public TableWrapper addCustom(final String columnName, final TableRow.Builder builder) {
		columns.put(columnName, builder.build());
		return this;
	}

	/**
	 * The value for the primary key.
	 *
	 * @param record the row you want to update in the database.
	 * @return instance of this class.
	 */
	public TableWrapper addRecord(final String record) {
		if (this.record == null)
			this.record = new HashSet<>();

		this.record.add(record);
		return this;
	}

	/**
	 * The value's for the primary key.
	 *
	 * @param record the row's you want to update in the database.
	 * @return instance of this class.
	 */
	public TableWrapper addAllRecord(final Set<String> record) {
		if (this.record == null)
			this.record = new HashSet<>();

		this.record.addAll(record);
		return this;
	}

	/**
	 * Set the primary row inside the database.
	 *
	 * @param primaryRow The primary row.
	 * @return instance of this class.
	 */
	public TableWrapper setPrimaryRow(@Nonnull final TableRow primaryRow) {
		this.primaryRow = primaryRow;
		return this;
	}

	/**
	 * Get the type of quote, it currently uses.
	 *
	 * @return the type quote currently set.
	 */
	public String getQuote() {
		return quoteColumnKey;
	}

	/**
	 * Set the quote around columns name and on the table name.
	 * set to empty string if you not want any quotes. If you not
	 * set this it will use a default quote.
	 *
	 * @param quoteToUse the quote around the columns name and on the table name.
	 * @return instance of this class.
	 */
	public TableWrapper setQuoteColumnKey(final String quoteToUse) {
		Validate.checkBoolean(quoteToUse == null, "The quote, can't be null");
		this.quoteColumnKey = quoteToUse;
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
					.append("(").append(tableWrapper.getPrimaryKeyLength()).append("))");


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
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepared query to run on your database.
	 */
	public String insertIntoTable() {
		TableWrapper tableWrapper = this.getTableWrapper();
		return "INSERT INTO " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + " " + this.merge() + ";";
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
		return "DROP TABLE " + tableWrapper.getQuote() + tableWrapper.getTableName() + tableWrapper.getQuote() + ";";
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
		columns.append("(")
				.append(tableWrapper.getQuote())
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
