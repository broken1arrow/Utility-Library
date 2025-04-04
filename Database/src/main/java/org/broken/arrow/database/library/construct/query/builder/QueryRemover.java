package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;

public class QueryRemover {
    private WhereBuilder whereBuilder = new WhereBuilder();

    public QueryRemover where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }
}
