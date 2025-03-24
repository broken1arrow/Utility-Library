package org.broken.arrow.database.library.construct.query;


import org.broken.arrow.database.library.construct.query.builder.HavingBuilder;
import org.broken.arrow.database.library.construct.query.builder.JoinBuilder;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

import java.util.function.Consumer;

public class Selector<T extends ColumnBuilder<?, Void>> {

    private final T selectBuilder;
    private final JoinBuilder joinBuilder = new JoinBuilder();
    private final HavingBuilder havingBuilder = new HavingBuilder();
    private WhereBuilder whereBuilder = new WhereBuilder();
    private String table;
    private String tableAlias;

    public Selector(T selectBuilder) {
        this.selectBuilder = selectBuilder;
    }

    public Selector<T> from(String table) {
        this.table = table;
        return this;
    }

    public Selector<T> from(String table, String alias) {
        this.table = table;
        this.tableAlias = alias;
        return this;
    }

    protected Selector<T> from(QueryBuilder queryBuilder) {
        this.table = "(" + queryBuilder.build().replace(";", "") + ")";
        return this;
    }

    public Selector<T> where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public Selector<T> select(Consumer<T> callback) {
        callback.accept(selectBuilder);
        return this;
    }

    public Selector<T> join(Consumer<JoinBuilder> callback) {
        callback.accept(joinBuilder);
        return this;
    }

    public Selector<T> having(Consumer<HavingBuilder> callback) {
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
