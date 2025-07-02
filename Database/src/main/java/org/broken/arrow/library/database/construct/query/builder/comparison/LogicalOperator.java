package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.builder.condition.ConditionQuery;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.library.database.construct.query.utlity.LogicalOperators;

public class LogicalOperator<T> {
    private final ConditionQuery<T> conditionQuery;
    private final T clazz;

    public LogicalOperator(T clazz, String columnName,  ConditionBuilder<T> conditionBuilder) {
        this.clazz = clazz;
        this.conditionQuery = new ConditionQuery<>(columnName, conditionBuilder);
    }

    public T and() {
        conditionQuery.setLogicalOperator(LogicalOperators.AND);
        return clazz;
    }

    public T or() {
        conditionQuery.setLogicalOperator(LogicalOperators.OR);
        return clazz;
    }

    public T build() {
        return clazz;
    }

    public ConditionQuery<T> getConditionQuery() {
        return conditionQuery;
    }
}