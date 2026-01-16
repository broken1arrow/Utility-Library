package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * A builder class for managing and constructing a list of {@link Column} instances or its subclasses.
 * <p>
 * Supports adding individual columns, collections of columns, or arrays of columns,
 * and building a combined string representation of all added columns.
 * The builder maintains a reference to a generic type {@code V} that can be used for fluent API chaining.
 * </p>
 *
 * @param <T> the type of columns managed by this builder, extending {@link Column}
 * @param <V> the type returned by add methods to allow fluent chaining (usually the parent or selector class)
 */
public class ColumnBuilder<T extends Column, V> {
    private final List<T> columns = new ArrayList<>();
    private final V clazzType;

    /**
     * Creates a ColumnBuilder without a reference to the selector instance.
     */
    public ColumnBuilder() {
        this(null);
    }

    /**
     * Creates a ColumnBuilder with a reference to the selector instance, enabling fluent chaining.
     *
     * @param clazzType the selector or parent instance to return from add methods
     */
    public ColumnBuilder(V clazzType) {
        this.clazzType = clazzType;
    }

    /**
     * Adds a single column to this builder.
     *
     * @param column the column to add
     * @return the selector instance of type {@code V} for fluent chaining
     */
    public V add(T column) {
        columns.add(column);
        return this.clazzType;
    }

    /**
     * Adds all columns from the provided list to this builder.
     * If the list is null or empty, no columns are added.
     *
     * @param columnsList the list of columns to add
     * @return the selector instance of type {@code V} for fluent chaining
     */
    public V addAll(List<T> columnsList) {
        if (columnsList == null || columnsList.isEmpty())
            return this.clazzType;
        columnsList.forEach(this::add);
        return this.clazzType;
    }

    /**
     * Adds all columns from the provided array to this builder.
     * If the array is null or empty, no columns are added.
     *
     * @param columns the array of columns to add
     * @return the selector instance of type {@code V} for fluent chaining
     */
    @SafeVarargs
    public final V addAll(T... columns) {
        if (columns != null)
            Arrays.stream(columns).forEach(this::add);
        return this.clazzType;
    }

    /**
     * Returns the current list of columns managed by this builder.
     *
     * @return the list of columns
     */
    public List<T> getColumns() {
        return columns;
    }

    /**
     * Returns the selector instance associated with this builder,
     * which can be used for fluent API chaining.
     *
     * @return the selector instance of type {@code V}
     */
    public V getSelectorInstance() {
        return clazzType;
    }

    /**
     * Builds a comma-separated string representation of all added columns
     * by invoking their {@code toString()} methods.
     * Returns an empty string if no columns have been added.
     *
     * @return a comma-separated string of columns.
     */
    public String build() {
        if (columns.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner(", ");

        for (T column : this.getColumns()) {
            joiner.add(column.toString());
        }
        return joiner + "";
    }

    /**
     * Builds the SQL fragment representing this column's definition,
     * excluding for example {@link SQLConstraints#primaryKey() primary key} constraints.
     * <p>
     * This method is intended for generating column definitions in contexts
     * where like primary keys are defined separately (e.g. composite keys or
     * table-level constraints).
     *
     * @return the SQL string fragment for the column definition without
     *         some constraints. 
     */
    public  String buildCampsiteKey(){
        return "";
    }

    @Override
    public String toString() {
        return "ColumnBuilder{" +
                "columns=" + columns +
                ", clazzType=" + clazzType +
                '}';
    }

}
