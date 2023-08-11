package org.broken.arrow.database.library.builders.tables;

import javax.annotation.Nonnull;

public final class TableRow {

	private final String columnName;
	private final Object defaultValue;
	private final String datatype;
	private final boolean primaryKey;
	private final boolean autoIncrement;
	private final boolean notNull;
	private final Builder builder;

	private TableRow(final Builder builder) {
		this.columnName = builder.columnName;
		this.defaultValue = builder.defaultValue;
		this.datatype = builder.datatype;
		this.primaryKey = builder.primaryKey;
		this.autoIncrement = builder.autoIncrement;
		this.notNull = builder.notNull;
		this.builder = builder;
	}

	/**
	 * Create a column with only name and datatype.
	 *
	 * @param columnName the name of the column.
	 * @param datatype   the type of data stored in this column.
	 * @return the TableRow object.
	 */

	public static TableRow of(@Nonnull final String columnName, @Nonnull final String datatype) {
		return new Builder(columnName, datatype).build();
	}

	/**
	 * Retrieve the name of the column.
	 *
	 * @return the name of the column.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Retrieve the default value of the column.
	 *
	 * @return the default value of the column.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Retrieve the datatype of the column.
	 *
	 * @return the datatype of the column.
	 */
	public String getDatatype() {
		return datatype;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Retrieve whether the column is auto-incremented.
	 *
	 * @return true if the column is auto-incremented, false otherwise.
	 */
	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	/**
	 * Retrieve whether the column allows null values.
	 *
	 * @return true if the column does not allow null values, false otherwise.
	 */
	public boolean isNotNull() {
		return notNull;
	}

	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private final String columnName;
		private Object defaultValue;
		private final String datatype;
		private boolean primaryKey;
		private boolean autoIncrement;
		private boolean notNull;

		/**
		 * Constructs a new Builder object with the specified column name and datatype.
		 *
		 * @param columnName the name of the column.
		 * @param datatype   the datatype of the column.
		 */
		public Builder(@Nonnull final String columnName, @Nonnull final String datatype) {
			this.columnName = columnName;
			this.datatype = datatype;
		}

		/**
		 * Sets the default value for the TableRow being built.
		 *
		 * @param defaultValue the default value of the column.
		 * @return the Builder object.
		 */
		public Builder setDefaultValue(final Object defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Sets the primary key flag for the TableRow being built.
		 *
		 * @param primaryKey true if the column is a primary key, false otherwise.
		 * @return the Builder object.
		 */
		public Builder setIsPrimaryKey(final boolean primaryKey) {
			this.primaryKey = primaryKey;
			return this;
		}

		/**
		 * Sets the auto-increment flag for the TableRow being built.
		 *
		 * @param autoIncrement true if the column is auto-incremented, false otherwise.
		 * @return the Builder object.
		 */
		public Builder setAutoIncrement(final boolean autoIncrement) {
			this.autoIncrement = autoIncrement;
			return this;
		}

		/**
		 * Sets the not-null flag for the TableRow being built.
		 *
		 * @param notNull true if the column does not allow null values, false otherwise.
		 * @return the Builder object.
		 */
		public Builder setNotNull(final boolean notNull) {
			this.notNull = notNull;
			return this;
		}

		/**
		 * Builds and returns the TableRow object.
		 *
		 * @return the constructed TableRow object.
		 */
		public TableRow build() {
			return new TableRow(this);
		}

	}

}