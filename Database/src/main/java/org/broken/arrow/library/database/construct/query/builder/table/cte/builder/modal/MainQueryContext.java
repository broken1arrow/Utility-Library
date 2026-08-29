package org.broken.arrow.library.database.construct.query.builder.table.cte.builder.modal;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Context provided inside the main query lambda block to safely collect execution queries.
 */
public class MainQueryContext {
    private final List<QueryBuilder> mainQueries;

    /**
     * Constructs a new {@code MainQueryContext}.
     *
     * @param mainQueries the shared reference list where the appended queries are collected
     */
    public MainQueryContext(@Nonnull final List<QueryBuilder> mainQueries) {
        this.mainQueries = mainQueries;
    }

    /**
     * Adds a main query to be executed after the {@code WITH} clause.
     *
     * @param query the main {@link QueryBuilder} instance
     * @return this {@link MainQueryContext} for fluent chaining
     */
    public MainQueryContext addQuery(@Nonnull final QueryBuilder query) {
        mainQueries.add(query);
        return this;
    }

}