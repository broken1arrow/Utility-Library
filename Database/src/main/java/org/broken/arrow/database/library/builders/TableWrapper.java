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
	private final boolean SQLite;
	private final Map<String, TableRow> columns = new LinkedHashMap<>();

	private TableWrapper() {
		throw new CatchExceptions("You should not try create empty constractor");
	}

	private TableWrapper(@Nonnull final String tableName, @Nonnull final TableRow tableRow, final boolean isSQlittle) {
		this(tableName, tableRow, 120, isSQlittle);
	}

	private TableWrapper(@Nonnull final String tableName, @Nonnull final TableRow primaryRow, final int valueLength, final boolean isSQLite) {
		Validate.checkNotEmpty(tableName, "Table name is empty.");
		Validate.checkNotEmpty(primaryRow, "Primary key is empty, if you not set this you can`t have unique rows in the database.");
		this.SQLite = isSQLite;
		this.tableName = tableName;
		this.primaryRow = primaryRow;
		this.primaryKeyLength = valueLength > 0 ? valueLength : 120;
		this.table = this;
	}

	/**
	 * Method to create new database command. Will create first part of the table. You need then use methods
	 * {@link TableWrapper#add(String, String)}, {@link TableWrapper#addNotNull(String, String)}
	 * {@link TableWrapper#addDefult(String, String, String)}, {@link TableWrapper#addAutoIncrement(String, String)}
	 * and lastly use this {@link TableWrapper#createTable()}. To build the string for the database command.
	 *
	 * @param tableName  name on your table.
	 * @param primaryRow key that is primary if not set you can add duplicate records.
	 * @param isSQlittle some commands are not suported in SQlittle (so need to know what type of database).
	 * @return TableWrapper class you need then add columms to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull TableRow primaryRow, final boolean isSQlittle) {
		return new TableWrapper(tableName, primaryRow, isSQlittle).getTableWrapper();
	}

	/**
	 * Method to create new database command. Will create first part of the table. You need then use method
	 * {@link TableWrapper#replaceIntoTable()} to build the string for the database command.
	 *
	 * @param tableName   name on your table.
	 * @param primaryRow  key that is primary if not set you can add duplicate records.
	 * @param valueLength Length of the value for primary key (used for text and similar in SQL database).
	 * @param isSQlittle  some commands are not suported in SQlittle (so need to know what type of database).
	 * @return TableWrapper class you need then add columms to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull TableRow primaryRow, final int valueLength, final boolean isSQlittle) {
		return new TableWrapper(tableName, primaryRow, valueLength, isSQlittle).getTableWrapper();
	}

	public TableWrapper getTableWrapper() {
		return table;
	}

	public TableWrapper add(final String columnName, final String datatype) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).build());
		return this;
	}

	public TableWrapper addDefult(final String columnName, final String datatype, final String defult) {
		columns.put(columnName, new TableRow.Builder(columnName, datatype)
				.setPrimaryKey(getPrimaryRow().getColumnName().equals(columnName)).setDefaultValue(defult).build());
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

	public boolean isSQLite() {
		return SQLite;
	}

	/**
	 * Create new table with columns,data type and primary key you have set.
	 * From this methods {@link #add(String, String)}, {@link #addNotNull(String, String)}
	 * {@link #addDefult(String, String, String)} {@link #addAutoIncrement(String, String)}
	 *
	 * @return string with prepered query to run on your database.
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
		if (this.isSQLite())
			columns.append(", PRIMARY KEY (`").append(primaryKey.getColumnName()).append("`)");
		else
			columns.append(", PRIMARY KEY (`").append(primaryKey.getColumnName()).append("`(").append(this.getPrimaryKeyLength()).append("))");

		columnsArray = this.columns.values().stream().map(TableRow::getColumnName).collect(Collectors.joining(","));

		String string = "CREATE TABLE IF NOT EXISTS `" + this.getTableName() + "` (" + columns + ")" + (this.isSQLite() ? "" : " DEFAULT CHARSET=utf8mb4" /*COLLATE=utf8mb4_unicode_520_ci*/) + ";";
		System.out.println("string " + string);
		return string;
	}

	/**
	 * Replace data in your database, on columns you added.
	 *
	 * @return string with prepered query to run on your database.
	 */
	public String replaceIntoTable() {
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
		return "REPLACE INTO `" + this.getTableName() + "` " + columns + ";";
	}

	/**
	 * Update data in your database, on columns you added. Will replace old data on colums you added.
	 *
	 * @return string with prepered query to run on your database.
	 */
	public String updateTable() {
		Set<String> records = this.getRecord();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		String record = this.getRecord().stream().findFirst().orElse(null);
		Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		return this.updateTable(record);
	}

	/**
	 * Update data in your database, on columns you added. Will replace old data on colums you added.
	 *
	 * @return list of prepered querys to run on your database.
	 */
	public List<String> updateTables() {
		List<String> list = new ArrayList<>();
		Set<String> records = this.getRecord();
		Validate.checkBoolean(records == null || records.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
		for (String record : records)
			list.add(this.updateTable(record));
		return list;
	}

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

}
