package org.broken.arrow.database.library.builders.tables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColumnWrapper {

	private final String columnName;
	private final Object value;

	/**
	 * Constructs a new ColumnWrapper object.
	 *
	 * @param  columnName  the columnName.
	 * @param value the value of the column.
	 */
	public ColumnWrapper(@Nonnull final String columnName, @Nullable final Object value) {
		this.columnName = columnName;
		this.value = value;
	}

	/**
	 * Get the set columnName for this value.
	 *
	 * @return the value currently set for the primary key.
	 */
	@Nonnull
	public Object getColumnName() {
		return this.columnName;
	}

	/**
	 * Retrieves the value of the column.
	 *
	 * @return the value set for the specified column name, or null if the value is not set or is null.
	 */
	@Nullable
	public Object getColumnValue() {
		return this.value;
	}
}
