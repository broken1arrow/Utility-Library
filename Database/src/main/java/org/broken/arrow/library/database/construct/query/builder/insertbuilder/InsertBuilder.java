package org.broken.arrow.library.database.construct.query.builder.insertbuilder;

public class InsertBuilder {
    private final String columnName;
    private final Object columnValue;

    public InsertBuilder(final String columnName, final Object columnValue) {
        this.columnName = columnName;
        this.columnValue = columnValue;
    }

    public static InsertBuilder of(String columnName, Object columnValue) {
        return new InsertBuilder(columnName, columnValue);
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getColumnValue() {
        return columnValue;
    }
}
