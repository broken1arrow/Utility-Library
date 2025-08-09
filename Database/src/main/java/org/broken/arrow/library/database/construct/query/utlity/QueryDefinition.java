package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;

/**
 * Wrapper class that holds a query string or a {@link QueryBuilder} instance.
 * <p>
 * This class provides a unified way to manage either a raw query string or
 * a constructed query using the QueryBuilder. It decides which query to return
 * based on whether the QueryBuilder has a query set or not.
 */
public class QueryDefinition {

    private final QueryBuilder queryBuilder;
    private final String query;

    /**
     * Private constructor to initialize with a QueryBuilder and/or raw query string.
     *
     * @param queryBuilder the QueryBuilder instance (never null)
     * @param query the raw query string (never null)
     */
    private QueryDefinition(@Nonnull final QueryBuilder queryBuilder, @Nonnull final String query) {
        this.queryBuilder = queryBuilder;
        this.query = query;
    }

    /**
     * Creates a QueryDefinition from a raw query string.
     * <p>
     * The underlying QueryBuilder will be initialized but empty.
     *
     * @param query the raw SQL query string.
     * @return a new QueryDefinition containing the raw query string
     */
    public static QueryDefinition of(@Nonnull final String query) {
        return new QueryDefinition(new QueryBuilder(), query);
    }

    /**
     * Creates a QueryDefinition from a QueryBuilder instance.
     * <p>
     * The raw query string will be empty.
     *
     * @param queryBuilder the QueryBuilder instance.
     * @return a new QueryDefinition wrapping the QueryBuilder
     */
    public static QueryDefinition of(@Nonnull final QueryBuilder queryBuilder) {
        return new QueryDefinition(queryBuilder, "");
    }

    /**
     * Returns the SQL query string.
     * <p>
     * If the QueryBuilder has a query set, the built query string from QueryBuilder
     * will be returned. Otherwise, if the raw query string is non-empty, it returns
     * the raw query. If neither is available, returns an empty string.
     *
     * @return the SQL query string
     */
    @Nonnull
    public String getQuery() {
        if (!queryBuilder.isQuerySet() && !query.isEmpty())
            return query;
        if (queryBuilder.isQuerySet())
            return queryBuilder.build();
        return "";
    }
}
