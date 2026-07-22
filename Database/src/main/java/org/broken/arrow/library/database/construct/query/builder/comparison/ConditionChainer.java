package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.builder.condition.ConditionQuery;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.library.database.construct.query.utlity.LogicalOperator;

/**
 * Fluent wrapper for chaining SQL conditions using logical operators (e.g., {@code AND}, {@code OR}).
 * <p>
 * Acts as an intermediate step in the query step-builder pipeline, providing access to logical
 * operators after a comparison predicate is evaluated while maintaining a reference to the
 * parent builder context.
 * </p>
 *
 * @param <T> the type of the parent builder or query context for fluent chaining
 */
public class ConditionChainer<T> {
    private final ConditionQuery<T> conditionQuery;
    private final T parentContext;

    /**
     * Constructs a new {@code ConditionChainer} with the provided parent context and condition details.
     *
     * @param parentContext    the parent builder or owner instance
     * @param columnName       the column name for the condition
     * @param conditionBuilder the condition builder associated with this operator
     */
    public ConditionChainer(T parentContext, String columnName, ConditionBuilder<T> conditionBuilder) {
        this.parentContext = parentContext;
        this.conditionQuery = new ConditionQuery<>(columnName, conditionBuilder);
    }

    /**
     * Applies the logical AND operator to the condition query
     * and returns the parent builder instance for chaining.
     *
     * @return the parent builder instance
     */
    public T and() {
        conditionQuery.setLogicalOperator(LogicalOperator.AND);
        return parentContext;
    }

    /**
     * Applies the logical OR operator to the condition query
     * and returns the parent builder instance for chaining.
     *
     * @return the parent builder instance
     */
    public T or() {
        conditionQuery.setLogicalOperator(LogicalOperator.OR);
        return parentContext;
    }

    /**
     * Completes building and returns the parent builder instance.
     *
     * @return the parent builder instance
     */
    public T build() {
        return parentContext;
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