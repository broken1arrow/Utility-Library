package org.broken.arrow.database.library.builders.tables;

import javax.annotation.Nonnull;

public final class TableRow {

	private final String columnName;
	private final Object columnValue;
	private final String defaultValue;
	private final String datatype;
	private final boolean primaryKey;
	private final boolean autoIncrement;
	private final boolean notNull;
	private final Builder builder;

	public TableRow(final Builder builder) {
		this.columnName = builder.columnName;
		this.columnValue = builder.columnValue;
		this.defaultValue = builder.defaultValue;
		this.datatype = builder.datatype;
		this.primaryKey = builder.primaryKey;
		this.autoIncrement = builder.autoIncrement;
		this.notNull = builder.notNull;
		this.builder = builder;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getColumnValue() {
		return columnValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDatatype() {
		return datatype;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private final String columnName;
		private Object columnValue;
		private String defaultValue;
		private final String datatype;
		private boolean primaryKey;
		private boolean autoIncrement;
		private boolean notNull;

		public Builder(@Nonnull final String columnName, @Nonnull final String datatype) {
			this.columnName = columnName;
			this.datatype = datatype;
		}

		public Builder setColumnValue(final Object columnValue) {
			this.columnValue = columnValue;
			return this;
		}

		public Builder setDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder setPrimaryKey(final boolean primaryKey) {
			this.primaryKey = primaryKey;
			return this;
		}

		public Builder setAutoIncrement(final boolean autoIncrement) {
			this.autoIncrement = autoIncrement;
			return this;
		}

		public Builder setNotNull(final boolean notNull) {
			this.notNull = notNull;
			return this;
		}

		public TableRow build() {
			return new TableRow(this);
		}

	}

}