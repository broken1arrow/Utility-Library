package org.broken.arrow.library.database.construct.query.builder.table.cte;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnRegistry;

import javax.annotation.Nonnull;

/**
 * A specialized column registry used within a SQL {@code WITH} clause context (CTE).
 * <p>
 * It extends {@link ColumnRegistry} to collect columns for a Common Table Expression
 * and provides a {@link FromWrapper} to seamlessly transition back to the main query builder.
 * </p>
 */
public class WithColumnRegistry extends ColumnRegistry<Column, FromWrapper> {
    private final FromWrapper fromWrapper;

    /**
     * Constructs a {@link WithColumnRegistry} associated with the specified {@code WITH}
     * clause builder and the main query builder.
     *
     * @param withBuilder the {@code WITH} clause builder managing this registry
     * @param query       the main query builder associated with this {@code WITH} clause
     */
    public WithColumnRegistry(WithBuilder withBuilder, QueryBuilder query) {
        this.fromWrapper = new FromWrapper(this, withBuilder, query);
    }

    /**
     * Returns the {@link FromWrapper} associated with this registry.
     *
     * @return the {@link FromWrapper} instance used to continue building the query
     */
    public FromWrapper getFromWrapper() {
        return this.fromWrapper;
    }

    @Nonnull
    @Override
    protected FromWrapper getContext() {
        return this.fromWrapper;
    }

}