package org.broken.arrow.library.database.construct.query.builder.insertbuilder;

/**
 * Represents a single column-value pair for use in an SQL {@code INSERT} statement.
 * <p>
 * This class is typically used with {@link org.broken.arrow.library.database.construct.query.builder.InsertHandler} to store column names and their
 * associated values, which can then be bound to a prepared statement.
 * </p>
 */
public class InsertBuilder {
    private final String columnName;
    private final Object columnValue;

    /**
     * Creates a new {@link InsertBuilder} instance.
     *
     * @param columnName  the name of the column (must not be {@code null})
     * @param columnValue the value to insert into the column (can be {@code null})
     */
    public InsertBuilder(final String columnName, final Object columnValue) {
        this.columnName = columnName;
        this.columnValue = columnValue;
    }

    /**
     * Creates a new {@link InsertBuilder} instance using a static factory method.
     * <p>
     * This is a convenience method equivalent to {@code new InsertBuilder(columnName, columnValue)}.
     * </p>
     *
     * @param columnName  the name of the column (must not be {@code null})
     * @param columnValue the value to insert into the column (can be {@code null})
     * @return a new {@link InsertBuilder} instance
     */
    public static InsertBuilder of(String columnName, Object columnValue) {
        return new InsertBuilder(columnName, columnValue);
    }

    /**
     * Returns the name of the column.
     *
     * @return the column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Returns the value associated with the column.
     *
     * @return the column value
     */
    public Object getColumnValue() {
        return columnValue;
    }
}
