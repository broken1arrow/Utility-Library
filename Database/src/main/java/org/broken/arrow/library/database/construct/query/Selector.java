package org.broken.arrow.library.database.construct.query;


import org.broken.arrow.library.database.construct.query.builder.JoinBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.havingbuilder.HavingBuilder;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public class Selector<T extends ColumnBuilder<V, ?>, V extends Column> {

    private final T selectBuilder;
    private final QueryBuilder queryBuilder;
    private final JoinBuilder joinBuilder;
    private final HavingBuilder havingBuilder;

    private WhereBuilder whereBuilder;
    private String table;
    private String tableAlias;

    public Selector(@Nonnull final T selectBuilder, QueryBuilder queryBuilder) {
        this.selectBuilder = selectBuilder;
        this.queryBuilder = queryBuilder;
        whereBuilder = new WhereBuilder(queryBuilder);
        havingBuilder = new HavingBuilder(queryBuilder);
        joinBuilder = new JoinBuilder(queryBuilder);
    }

    public Selector<T,V> from(String table) {
        this.table = table;
        return this;
    }

    public Selector<T,V>  from(String table, String alias) {
        this.table = table;
        this.tableAlias = alias;
        return this;
    }

    protected Selector<T,V>  from(QueryBuilder queryBuilder) {
        this.table = "(" + queryBuilder.build().replace(";", "") + ")";
        return this;
    }

    public Selector<T,V>  where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }
    public Selector<T,V>  where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> callback) {
        this.whereBuilder = new WhereBuilder(queryBuilder);
        callback.apply(this.whereBuilder);
        return this;
    }
    public Selector<T,V>  select(Consumer<T> callback) {
        callback.accept(selectBuilder);
        return this;
    }

    public Selector<T,V> join(Consumer<JoinBuilder> callback) {
        callback.accept(joinBuilder);
        return this;
    }

    public Selector<T,V>  having(Consumer<HavingBuilder> callback) {
        callback.accept(havingBuilder);
        return this;
    }

    public T getSelectBuilder() {
        return selectBuilder;
    }

    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }

    public JoinBuilder getJoinBuilder() {
        return joinBuilder;
    }


    public HavingBuilder getHavingBuilder() {
        return havingBuilder;
    }

    public String getTable() {
        return table;
    }

    public String getTableWithAlias() {
        if (tableAlias == null || tableAlias.isEmpty())
            return table;
        return table + " " + tableAlias;
    }

    public String getTableAlias() {
        return tableAlias;
    }

}
