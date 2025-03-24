package org.broken.arrow.database.library.construct.query.builder.comparison;

import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereCondition;

public class LogicalOperator<T> {
    private final StringBuilder builder;
    private final WhereCondition<T> whereCondition;
    private final T clazz;

    public LogicalOperator(T clazz, String columnName, StringBuilder builder, WhereCondition<T> whereCondition) {
        this.clazz = clazz;
        this.builder = builder;
        this.builder.append(columnName);
        this.whereCondition = whereCondition;
    }

    public T and() {
        this.builder.append(whereCondition.toString());
        this.builder.append(" AND ");
        return clazz;
    }

    public T or() {
        this.builder.append(whereCondition.toString());
        this.builder.append(" OR ");
        return clazz;
    }

    public T build() {
        this.builder.append(whereCondition.toString());
        return clazz;
    }

}