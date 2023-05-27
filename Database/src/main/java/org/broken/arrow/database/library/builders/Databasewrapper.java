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

public final class Databasewrapper {
	private final TableWrapper table;

	private Databasewrapper() {
		throw new CatchExceptions("You should not try create empty constractor");
	}

	private Databasewrapper(@Nonnull final String tableName, @Nonnull final String primaryKey, final boolean isSQlittle) {
		this.table = new TableWrapper(tableName, primaryKey, isSQlittle);
	}

	/**
	 * Method to create new database command. Will create first part of the table. You need then use methods
	 * {@link Databasewrapper.TableWrapper#add(String, String)}, {@link Databasewrapper.TableWrapper#addNotNull(String, String)}
	 * {@link Databasewrapper.TableWrapper#addDefult(String, String, String)}, {@link Databasewrapper.TableWrapper#addAutoIncrement(String, String)}
	 * and lastly use this {@link Databasewrapper.TableWrapper#createTable()}. To build the string for the database command.
	 *
	 * @param tableName  name on your table.
	 * @param primaryKey key that is primary if not set you can add duplicate records.
	 * @param isSQlittle some commands are not suported in SQlittle (so need to know what type of database).
	 * @return TableWrapper class you need then add columms to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, @Nonnull final String primaryKey, final boolean isSQlittle) {
		return new Databasewrapper(tableName, primaryKey, isSQlittle).getTableWrapper();
	}

	/**
	 * Method to create new database command. Will create first part of the table. You need then use method
	 * {@link Databasewrapper.TableWrapper#replaceIntoTable()} to build the string for the database command.
	 *
	 * @param tableName  name on your table.
	 * @param isSQlittle some commands are not suported in SQlittle (so need to know what type of database).
	 * @return TableWrapper class you need then add columms to your table.
	 */
	public static TableWrapper of(@Nonnull final String tableName, final boolean isSQlittle) {
		return new Databasewrapper(tableName, "non", isSQlittle).getTableWrapper();
	}

	public TableWrapper getTableWrapper() {
		return table;
	}

	public static final class TableWrapper {
		private final String primaryKey;
		private final String tableName;
		private Set<String> record;
		private String columnsArray;
		private final boolean SQLite;
		private final Map<String, TableRow> columns = new LinkedHashMap<>();

		public TableWrapper(@Nonnull final String tableName, @Nonnull final String primaryKey, final boolean isSQLite) {
			Validate.checkNotEmpty(tableName, "Table name is empty.");
			Validate.checkNotEmpty(primaryKey, "Primary key is empty, if you not set this you can't have unique rows in the database.");
			this.SQLite = isSQLite;
			this.tableName = tableName;
			this.primaryKey = primaryKey;
		}

		public TableWrapper add(final String columnName, final String datatype) {
			columns.put(columnName, new TableRow.Builder(columnName, datatype).setPrimaryKey(getPrimaryKey().equals(columnName)).build());
			return this;
		}

		public TableWrapper addDefult(final String columnName, final String datatype, final String defult) {
			columns.put(columnName, new TableRow.Builder(columnName, datatype).setPrimaryKey(getPrimaryKey().equals(columnName)).setDefaultValue(defult).build());
			return this;
		}

		public TableWrapper addAutoIncrement(final String columnName, final String datatype) {
			columns.put(columnName, new TableRow.Builder(columnName, datatype).setPrimaryKey(getPrimaryKey().equals(columnName)).setAutoIncrement(true).build());
			return this;
		}

		public TableWrapper addNotNull(final String columnName, final String datatype) {
			columns.put(columnName, new TableRow.Builder(columnName, datatype).setPrimaryKey(getPrimaryKey().equals(columnName)).setNotNull(true).build());
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
		 * The value's for the primary key.
		 *
		 * @param record the row's you want to update in the database.
		 * @return this class.
		 */
		public TableWrapper addAllRecord(final Set<String> record) {
			if (this.record == null)
				this.record = new HashSet<>();

			this.record.addAll(record);
			return this;
		}

		public String getPrimaryKey() {
			return primaryKey;
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

			for (final Entry<String, TableRow> entry : this.getColumns().entrySet()) {
				TableRow column = entry.getValue();
				columns.append((columns.length() == 0) ? "" : ", ").append("`").append(entry.getKey()).append("` ").append(column.getDatatype());

				if (column.isAutoIncrement())
					columns.append(" AUTO_INCREMENT");

				else if (column.isNotNull())
					columns.append(" NOT NULL");

				if (column.getDefaultValue() != null)
					columns.append(" DEFAULT ").append(column.getDefaultValue());
			}
			Validate.checkBoolean(getPrimaryKey() == null || getPrimaryKey().equals("non"), "You need set primaryKey, for create a table.");
			columns.append(", PRIMARY KEY (`").append(getPrimaryKey()).append("`)");

			columnsArray = this.columns.values().stream().map(TableRow::getColumnName).collect(Collectors.joining(","));
			return "CREATE TABLE IF NOT EXISTS `" + this.getTableName() + "` (" + columns + ")" + (this.isSQLite() ? "" : " DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci") + ";";
		}

		/**
		 * Replace data in your database, on columns you added.
		 *
		 * @return string with prepered query to run on your database.
		 */
		public String replaceIntoTable() {
			final StringBuilder columns = new StringBuilder();
			final StringBuilder values = new StringBuilder();
			int index = 0;
			for (Entry<String, TableRow> entry : this.getColumns().entrySet()) {
				index++;
				//for (int index = 0; index < this.getColumns().size(); index++) {
				final String columnName = entry.getKey();
				final TableRow column = entry.getValue();
				final boolean endOfString = index  == this.getColumns().size();
				columns.append((columns.length() == 0) ? "(" : "").append("`").append(columnName).append("`").insert(columns.length(), columns.length() == 0 || endOfString ? "" : ",");
				Object value = column.getColumnValue();
				if (value == null && column.isNotNull())
					value = "";
				if (value == null && column.getDefaultValue() != null)
					value = column.getDefaultValue();
				values.append( value).insert(values.length(), values.length() == 0 || endOfString ? "" : "','");
			}
			columns.insert(columns.length(), ") VALUES('" + values + "')");
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
		/*
		    Validate.checkBoolean(getPrimaryKey() == null || getPrimaryKey().equals("non"), "You need set primary key, for update records in the table or all records get updated.");
			Validate.checkBoolean(this.getRecord() == null || this.getRecord().isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
			final StringBuilder columns = new StringBuilder();
			for (int index = 0; index < this.getColumns().size(); index++) {
				final TableRow column = this.getColumns().get(index);
				final boolean endOfString = index + 1 == this.getColumns().size();
				columns.append("`").append(column.getColumnName()).append("`").insert(columns.length(), columns.length() == 0 || endOfString ? " = '" + column.getDatatype() + "'" : " = '" + column.getDatatype() + "',");
			}
			return "UPDATE `" + this.getTableName() + "` SET " + columns + " WHERE `" + getPrimaryKey() + "` = `" + this.getRecord() + "`" + ";";
			*/
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
			Validate.checkBoolean(getPrimaryKey() == null || getPrimaryKey().equals("non"), "You need set primary key, for update records in the table or all records get updated.");
			Validate.checkBoolean(record == null || record.isEmpty(), "You need to set record value for the primary key. When you want to update the row.");
			final StringBuilder columns = new StringBuilder();
			int index = 0;
			for (Entry<String, TableRow> entry : this.getColumns().entrySet()) {
				index++;
				//for (int index = 0; index < this.getColumns().size(); index++) {
				final String columnName = entry.getKey();
				final TableRow column = entry.getValue();
				final boolean endOfString = index  == this.getColumns().size();
				Object value = column.getColumnValue();
				if (value == null && column.isNotNull())
					value = "";
				if (value == null && column.getDefaultValue() != null)
					value = column.getDefaultValue();
				columns.append("`").append(columnName).append("`").insert(columns.length(), columns.length() == 0 || endOfString ? " = '" + value + "'" : " = '" + value + "',");
			}
			return "UPDATE `" + this.getTableName() + "` SET " + columns + " WHERE `" + getPrimaryKey() + "` = `" + record + "`" + ";";
		}
	}
}
