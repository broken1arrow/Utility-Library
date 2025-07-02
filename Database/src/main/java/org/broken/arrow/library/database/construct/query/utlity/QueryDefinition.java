package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;

public class QueryDefinition {

    private final QueryBuilder queryBuilder;
    private final String query;

    private QueryDefinition(@Nonnull final QueryBuilder queryBuilder, @Nonnull final String query) {
        this.queryBuilder = queryBuilder;
        this.query = query;
    }

    public static QueryDefinition of(@Nonnull final String query) {
        return new QueryDefinition(new QueryBuilder(), query);
    }

    public static QueryDefinition of(@Nonnull final QueryBuilder queryBuilder) {
        return new QueryDefinition(queryBuilder, "");
    }

    @Nonnull
    public String getQuery() {
        if (!queryBuilder.isQuerySet() && !query.isEmpty())
            return query;
        if (queryBuilder.isQuerySet())
            return queryBuilder.build();
        return "";
    }

}
