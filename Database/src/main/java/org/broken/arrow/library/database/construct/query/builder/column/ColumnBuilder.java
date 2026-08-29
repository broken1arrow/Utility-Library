package org.broken.arrow.library.database.construct.query.builder.column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A concrete implementation of {@link ColumnRegistry} for building a standard list of columns.
 * <p>
 * Use {@link #make(Consumer)} to initialize and populate the builder fluently.
 * </p>
 */
public class ColumnBuilder extends ColumnRegistry<Column, ColumnBuilder> {

    private ColumnBuilder() {
    }

    /**
     * Create empty {@link ColumnBuilder} instance to build your list of columns
     *
     * @return a {@link ColumnBuilder} instance to chain additional operations
     */
    public static ColumnBuilder empty() {
        return new ColumnBuilder();
    }

    /**
     * Starts building columns fluently using a callback function.
     *
     * @param callback a consumer function to register the columns
     * @return a {@link ColumnBuilder} instance to chain additional operations
     */
    public static ColumnBuilder make(@Nonnull final Consumer<ColumnBuilder> callback) {
        ColumnBuilder columnBuilder = new ColumnBuilder();
        callback.accept(columnBuilder);
        return columnBuilder;
    }


    @Nonnull
    @Override
    protected ColumnBuilder getContext() {
        return this;
    }

    @Nonnull
    @Override
    protected Column createColumn(@Nonnull String columnName) {
        return Column.of(columnName);
    }
}
