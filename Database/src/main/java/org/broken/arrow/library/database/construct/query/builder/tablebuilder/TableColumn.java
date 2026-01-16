package org.broken.arrow.library.database.construct.query.builder.tablebuilder;


import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Represents a database table column with a name, data type, and optional SQL constraints.
 * <p>
 * Extends the {@link Column} class and adds type and constraint information,
 * including whether the column is a primary key.
 * </p>
 */
public class TableColumn extends Column {

    private final DataType dataType;
    private final SQLConstraints[] constraints;
    private final ColumnManager columnManger;
    private final boolean isPrimaryKey;

    /**
     * Constructs a new {@code TableColumn} with the specified column manager, name,
     * data type, and optional SQL constraints.
     *
     * @param columnManger the {@link ColumnManager} managing this column
     * @param columnName   the name of the column
     * @param dataType     the data type of the column, must not be null
     * @param constraints  zero or more SQL constraints applied to the column
     */
    public TableColumn(@Nullable final ColumnManager columnManger, @Nonnull final String columnName, @Nonnull final DataType dataType, @Nullable final SQLConstraints... constraints) {
        super(columnName, "");
        this.dataType = dataType;
        this.constraints = constraints;
        this.columnManger = columnManger;
        this.isPrimaryKey = containsPrimaryKey(constraints);
    }

    /**
     * Returns the SQL constraints applied to this column.
     *
     * @return an array of {@link SQLConstraints} or an empty array if none
     */
    public SQLConstraints[] getConstraints() {
        return constraints;
    }

    /**
     * Returns the data type of this column.
     *
     * @return the {@link DataType} of the column
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Checks if this column is marked as a primary key.
     *
     * @return {@code true} if the column is a primary key, {@code false} otherwise
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Builds the SQL fragment representing this column's definition,
     * including its name, data type, and constraints.
     *
     * @return the SQL string fragment for the column definition
     */
    public String build() {
        StringJoiner joiner = new StringJoiner(" ");
        if (this.constraints != null)
            for (SQLConstraints constraint : this.constraints) {
                joiner.add(constraint.toString());
            }

        return this.getColumnName() + " " + dataType.getValue() + " " + joiner + " ";
    }

    /**
     * Builds a comma-separated string representation of all added columns
     * by invoking their {@code toString()} methods.
     * Returns an empty string if no columns have been added.
     *
     * @return a comma-separated string of columns.
     */
    public String buildCampsiteKey() {
        StringJoiner joiner = new StringJoiner(" ");
        if (this.constraints != null) {
            for (SQLConstraints constraint : this.constraints) {
                if (!SQLConstraints.isPrimary(constraint))
                    joiner.add(constraint.toString());
            }
        }
        return this.getColumnName() + " " + dataType.getValue() + " " + joiner + " ";
    }

    /**
     * A builder-style helper class to chain the creation of {@link TableColumn} instances
     * and add them to a {@link ColumnManager}.
     */
    public static class Separator {
        private final TableColumn column;

        /**
         * Creates a new {@code Separator} wrapping the given {@link TableColumn}
         * and registers the column with its manager if not {@code null}.
         *
         * @param column the {@link TableColumn} to wrap
         */
        public Separator(@Nonnull final TableColumn column) {
            this.column = column;
            final ColumnManager manger = this.column.columnManger;
            if (manger != null)
                manger.add(column);
        }

        /**
         * Adds a new column with the specified name, data type, and constraints
         * to the same {@link ColumnManager}.
         *
         * @param communeName the name of the new column
         * @param datatype    the data type of the new column
         * @param constraints zero or more SQL constraints for the new column
         * @return a new {@code Separator} wrapping the newly created column
         */
        public Separator column(@Nonnull final String communeName, @Nonnull final DataType datatype, @Nullable final SQLConstraints... constraints) {
            return new Separator(new TableColumn(this.column.columnManger, communeName, datatype, constraints));
        }

        /**
         * Finishes the column building process and returns the associated {@link ColumnManager}.
         *
         * @return the {@link ColumnManager} managing the columns
         */
        public ColumnManager build() {
            return this.column.columnManger;
        }

    }

    /**
     * Helper method to check if the provided constraints contain a primary key constraint.
     *
     * @param constraints the SQL constraints to check
     * @return {@code true} if primary key constraint is found, {@code false} otherwise
     */
    private boolean containsPrimaryKey(SQLConstraints... constraints) {
        if (constraints == null) return false;
        for (SQLConstraints constraint : constraints) {
            if (SQLConstraints.isPrimary(constraint)) {
                return true;
            }
        }
        return false;
    }

}
