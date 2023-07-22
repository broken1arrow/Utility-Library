package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents a wrapper for a database column, providing a convenient way to
 * set the primary key value , for later be used for example when creating database commands.
 */
public class ColumnWrapper {
	private final TableWrapper tableWrapper;
	private final String primaryKey;

	/**
	 * Constructs a new ColumnWrapper object for the given database table and set the primary column name.
	 *
	 * @param tableWrapper the TableWrapper object representing the database table.
	 */
	public ColumnWrapper(@Nonnull final TableWrapper tableWrapper) {
		this.tableWrapper = tableWrapper;
		Validate.checkNotNull(tableWrapper.getPrimaryRow(), "The primary column are not set.");
		final String columnName = tableWrapper.getPrimaryRow().getColumnName();
		this.primaryKey = columnName != null ? columnName : "";

	}

	/**
	 * Get the TableWrapper instance for this primary key.
	 *
	 * @return the TableWrapper instance.
	 */
	public TableWrapper getTableWrapper() {
		return tableWrapper;
	}

	/**
	 * Get the primary key.
	 *
	 * @return The primary column key.
	 */
	@Nonnull
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Get the primary value for the key.
	 *
	 * @return the value currently set for the primary key.
	 */
	@Nonnull
	public Object getPrimaryKeyValue() {
		return "";
	}

	/**
	 * Sets the value for a non-primary column with the given column name.
	 *
	 * @param columnName  the name of the non-primary column.
	 * @param columnValue the value to be set for the specified column.
	 */
	public void putColumn(String columnName, Object columnValue) {
	}

	/**
	 * Retrieves the value of a non-primary column by its name.
	 *
	 * @param columnName the name of the non-primary column to retrieve the value for.
	 * @return the value set for the specified column name, or null if the column name is not found.
	 */
	@Nullable
	public Object getColumnValue(String columnName) {
		return "";
	}
}
