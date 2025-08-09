package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Utility class for managing WHERE clause removals or modifications in a query.
 * <p>
 * Provides access to the internal WhereBuilder to build or modify WHERE conditions.
 * </p>
 */
public class QueryRemover {
    private WhereBuilder whereBuilder;
    private final QueryBuilder queryBuilder;

    /**
     * Creates a QueryRemover for the given QueryBuilder.
     *
     * @param queryBuilder the parent query builder
     */
    public QueryRemover(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * Sets the WHERE clause using a builder function and returns the original QueryBuilder.
     *
     * @param whereClause function to build the WHERE clause
     * @return the parent QueryBuilder for chaining
     */
    public QueryBuilder where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        this. whereBuilder = new WhereBuilder(queryBuilder);
        whereClause.apply(whereBuilder);
        return queryBuilder;
    }

    /**
     * Gets the internal WhereBuilder if available.
     *
     * @return the WhereBuilder or null if not set
     */
    @Nullable
    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }
}
