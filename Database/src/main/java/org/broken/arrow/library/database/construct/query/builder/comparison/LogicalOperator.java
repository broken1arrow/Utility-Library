package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.builder.condition.ConditionQuery;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.library.database.construct.query.utlity.LogicalOperators;

/**
 * Represents a logical operator wrapper used in building SQL condition queries.
 * It provides fluent methods to chain logical operators (AND, OR) with conditions,
 * while keeping track of the owning class instance for chaining.
 *
 * @param <T> the type of the parent builder or owner class that uses this logical operator
 */
public class LogicalOperator<T> {
    private final ConditionQuery<T> conditionQuery;
    private final T clazz;

    /**
     * Constructs a new {@code LogicalOperator} with the provided context.
     *
     * @param clazz            the parent builder or owner instance
     * @param columnName       the column name for the condition
     * @param conditionBuilder the condition builder associated with this operator
     */
    public LogicalOperator(T clazz, String columnName,  ConditionBuilder<T> conditionBuilder) {
        this.clazz = clazz;
        this.conditionQuery = new ConditionQuery<>(columnName, conditionBuilder);
    }

    /**
     * Applies the logical AND operator to the condition query
     * and returns the parent builder instance for chaining.
     *
     * @return the parent builder instance
     */
    public T and() {
        conditionQuery.setLogicalOperator(LogicalOperators.AND);
        return clazz;
    }

    /**
     * Applies the logical OR operator to the condition query
     * and returns the parent builder instance for chaining.
     *
     * @return the parent builder instance
     */
    public T or() {
        conditionQuery.setLogicalOperator(LogicalOperators.OR);
        return clazz;
    }

    /**
     * Completes building and returns the parent builder instance.
     *
     * @return the parent builder instance
     */
    public T build() {
        return clazz;
    }

    /**
     * Returns the internal {@link ConditionQuery} wrapped by this logical operator.
     *
     * @return the condition query instance
     */
    public ConditionQuery<T> getConditionQuery() {
        return conditionQuery;
    }
}