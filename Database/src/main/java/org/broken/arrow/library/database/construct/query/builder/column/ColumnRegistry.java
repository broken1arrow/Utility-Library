package org.broken.arrow.library.database.construct.query.builder.column;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * An abstract base class for managing and constructing a list of {@link Column} instances.
 * <p>
 * This registry supports adding individual columns, collections, or arrays of columns,
 * and can build a combined, comma-separated string representation of all added columns.
 * It uses a generic type {@code V} to maintain a reference to the calling context,
 * enabling fluent API chaining across different implementations.
 * </p>
 *
 * @param <T> the type of columns managed by this registry, extending {@link Column}
 * @param <V> the context type returned by the {@code add} methods to allow fluent chaining
 */
public abstract class ColumnRegistry<T extends Column, V> {
    private final List<T> columns = new ArrayList<>();

    /**
     * Returns the context instance associated with this registry.
     * This is used dynamically by the {@code add} methods to maintain fluent API chaining.
     *
     * @return the context instance of type {@code V}
     */
    @Nonnull
    protected abstract V getContext();

    /**
     * Adds a single column to this registry.
     *
     * @param column the column to add
     * @return the context instance of type {@code V} for fluent chaining
     */
    public V add(T column) {
        columns.add(column);
        return this.getContext();
    }

    /**
     * Adds all columns from the provided list to this registry.
     * If the list is null or empty, no columns are added.
     *
     * @param columnsList the list of columns to add
     * @return the context instance of type {@code V} for fluent chaining
     */
    public V addAll(List<T> columnsList) {
        if (columnsList == null || columnsList.isEmpty())
            return this.getContext();
        columnsList.forEach(this::add);
        return this.getContext();
    }

    /**
     * Adds all columns from the provided array to this registry.
     * If the array is null or empty, no columns are added.
     *
     * @param columns the array of columns to add
     * @return the context instance of type {@code V} for fluent chaining
     */
    @SafeVarargs
    public final V addAll(T... columns) {
        if (columns != null)
            Arrays.stream(columns).forEach(this::add);
        return this.getContext();
    }

    /**
     * Returns the current list of columns managed by this registry.
     *
     * @return the list of columns
     */
    public List<T> getColumns() {
        return columns;
    }


    /**
     * Builds a comma-separated string representation of all added columns
     * by invoking their {@code toString()} methods.
     * Returns an empty string if no columns have been added.
     *
     * @return a comma-separated string of columns
     */
    public String build() {
        if (columns.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner(", ");

        for (T column : this.getColumns()) {
            joiner.add(column.toString());
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "ColumnRegistry{" +
                "columns=" + columns +
                '}';
    }
}
