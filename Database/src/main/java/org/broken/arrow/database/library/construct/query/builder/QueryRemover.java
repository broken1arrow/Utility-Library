package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;

import java.util.function.Function;

public class QueryRemover {
    private final WhereBuilder whereBuilder;

    public QueryRemover(QueryBuilder queryBuilder) {
        whereBuilder = new WhereBuilder(queryBuilder);

    }

    public QueryRemover where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereBuilder) {
        whereBuilder.apply(this.whereBuilder);
        return this;
    }

    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }
}
