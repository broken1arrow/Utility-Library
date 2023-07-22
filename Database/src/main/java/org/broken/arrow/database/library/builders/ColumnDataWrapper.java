package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.builders.tables.TableWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a wrapper for all column's data, providing a convenient way
 * to set both the primary key value and non-primary column values for creating database commands.
 */
public class ColumnDataWrapper extends ColumnWrapper {

	private final Map<String, Object> columns = new LinkedHashMap<>();
	private final Object primaryKeyValue;

	/**
	 * Constructs a new DatabaseColumnWrapper object for the given database table and primary key value.
	 *
	 * @param tableWrapper    the TableWrapper object representing the database table.
	 * @param primaryKeyValue the value of the primary key for this column.
	 */
	public ColumnDataWrapper(@Nonnull final TableWrapper tableWrapper, @Nonnull final Object primaryKeyValue) {
		super(tableWrapper);
		this.primaryKeyValue = primaryKeyValue;
	}

	/**
	 * Get the primary value for the key.
	 *
	 * @return the value currently set for the primary key.
	 */
	@Override
	@Nonnull
	public Object getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	/**
	 * Sets the value for a non-primary column with the given column name.
	 *
	 * @param columnName  the name of the non-primary column.
	 * @param columnValue the value to be set for the specified column.
	 */
	@Override
	public void putColumn(String columnName, Object columnValue) {
		columns.put(columnName, columnValue);
	}

	/**
	 * Retrieves the value of a non-primary column by its name.
	 *
	 * @param columnName the name of the non-primary column to retrieve the value for.
	 * @return the value set for the specified column name, or null if the column name is not found.
	 */
	@Override
	@Nullable
	public Object getColumnValue(String columnName) {
		return columns.get(columnName);
	}
}
