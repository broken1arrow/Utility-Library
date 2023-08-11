package org.broken.arrow.database.library.builders.tables;


import org.broken.arrow.database.library.log.Validate;
import org.broken.arrow.database.library.log.Validate.CatchExceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TableWrapper {
	private final TableWrapper table;
	private TableRow primaryRow;
	private final String tableName;
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
		this.primaryKeyLength = valueLength > 0 && supportMySQL ? valueLength : -1;
		this.table = this;
	}

	/**
	 * Creates a new TableWrapper object to build a database command for creating a table.
	 * Use the provided methods to add columns and define table properties. You can then call {@link SqlCommandUtility#createTable()}
	 * to construct the final database command string or use {@link org.broken.arrow.database.library.Database#createTables()}.
	 * <p>
	 * Note: if you set up a Mysql database is it recommended you also set the length of the primary column value.
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
	 * Use the provided methods to add columns and define table properties. You can then call {@link SqlCommandUtility#createTable()}
	 * to construct the final database command string or use {@link org.broken.arrow.database.library.Database#createTables()}.
	 *
	 * @param tableName   name on your table.
	 * @param primaryRow  the key that serves as the primary key. If not set, duplicate records may be added.
	 * @param valueLength length of the value for primary key (used for text,varchar and similar in SQL database).
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
}
