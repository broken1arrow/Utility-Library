package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class QueryRemover {
    private WhereBuilder whereBuilder;
    private final QueryBuilder queryBuilder;

    public QueryRemover(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public QueryBuilder where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        this. whereBuilder = new WhereBuilder(queryBuilder);
        whereClause.apply(whereBuilder);
        return queryBuilder;
    }

    @Nullable
    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }
}
