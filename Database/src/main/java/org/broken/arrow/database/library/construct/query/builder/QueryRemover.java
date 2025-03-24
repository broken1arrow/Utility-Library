package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;

import java.util.function.Consumer;

public class QueryRemover {
    private final WhereBuilder whereBuilder = new WhereBuilder();

    public QueryRemover where(Consumer<WhereBuilder> callback) {
        callback.accept(whereBuilder);
        return this;
    }

    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }
}
