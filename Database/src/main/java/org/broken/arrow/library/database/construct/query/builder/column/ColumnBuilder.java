package org.broken.arrow.library.database.construct.query.builder.column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A concrete implementation of {@link ColumnRegistry} for building a standard list of columns.
 * <p>
 * Use {@link #start(Consumer)} to initialize and populate the builder fluently.
 * </p>
 */
public class ColumnBuilder extends ColumnRegistry<Column, ColumnBuilder> {

    private ColumnBuilder() {
    }

    /**
     * Starts building columns fluently using a callback function.
     *
     * @param callback a consumer function to register the columns
     * @return a {@link ColumnBuilder} instance to chain additional operations
     */
    public static ColumnBuilder start(Consumer<ColumnBuilder> callback) {
        ColumnBuilder columnBuilder = new ColumnBuilder();
        callback.accept(columnBuilder);
        return columnBuilder;
    }


    @Nonnull
    @Override
    protected ColumnBuilder getContext() {
        return this;
    }
}
