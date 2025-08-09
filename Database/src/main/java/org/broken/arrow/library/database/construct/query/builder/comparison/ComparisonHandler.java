package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.library.database.construct.query.utlity.LogicalOperators;
import org.broken.arrow.library.database.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Handles SQL comparison operations for a specific column within a query.
 * <p>
 * This class provides a fluent API to define comparison conditions such as
 * {@code =}, {@code <}, {@code >}, {@code IN}, {@code BETWEEN}, and others.
 * It supports both direct values and subqueries as comparison targets.
 * </p>
 *
 * @param <T> the parent query or builder type for fluent chaining
 */
public class ComparisonHandler<T> {

    private final LogicalOperator<T> logicalOperator;
    private final String column;
    private ConditionBuilder<T> condition;
    private SubqueryHandler<T> subqueryHandler;
    private Object[] values;
    private String symbol;


    /**
     * Creates a new comparison handler for the specified column within the given query context.
     * <p>
     * This constructor initializes the {@link ConditionBuilder} and {@link LogicalOperator}
     * using the provided parent query instance and a marker for condition grouping or position.
     * </p>
     *
     * @param clazz  the parent query object or builder instance
     * @param column the name of the column to apply comparisons on
     * @param marker the marker used to track this condition's grouping or position
     */
    public ComparisonHandler(T clazz, String column, Marker marker) {
        this.column = column;
        condition = new ConditionBuilder<>(this, marker);
        this.logicalOperator = new LogicalOperator<>(clazz, column, condition);
    }

    /**
     * Creates an empty comparison handler with no column or logical operator.
     * <p>
     * Primarily used as a placeholder or for cases where initialization is deferred.
     */
    public ComparisonHandler() {
        this.logicalOperator = null;
        this.column = "";
    }

    private void init(LogicalOperators symbol, Object value) {
        this.symbol = symbol.getSymbol();
        if (value instanceof List<?>) {
            this.values = new ArrayList<>((List<?>) value).toArray();
        } else {
            if (value instanceof SubqueryHandler<?>) {
                @SuppressWarnings("unchecked") final SubqueryHandler<T> subquery = (SubqueryHandler<T>) value;
                this.subqueryHandler = subquery;
                final Map<Integer, Object> subValues = subquery.getSubquery().getValues();
                this.values = subValues.entrySet().stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                        .map(Map.Entry::getValue).toArray();
            } else {
                this.values = new Object[]{value};
            }
        }
    }

    private void init(LogicalOperators symbol, Object firstValue, Object secondValue) {
        this.symbol = symbol.getSymbol();
        if (firstValue instanceof List<?>) {
            this.values = new ArrayList<>((List<?>) firstValue).toArray();
        } else
            this.values = new Object[]{firstValue, secondValue};
    }

    /**
     * Adds an {@code =} comparison with the given value.
     *
     * @param value The value
     * @return this class for chaining.
     */
    public LogicalOperator<T> equal(Object value) {
        this.init(LogicalOperators.EQUALS, value);
        return this.logicalOperator;
    }

    /**
     * Adds an {@code =} comparison using a subquery.
     *
     * @param subquery The subquery
     * @return this class for chaining.
     */
    public LogicalOperator<T> equal(QueryBuilder subquery) {
        this.init(LogicalOperators.EQUALS, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code <} comparison with the given value.
     *
     * @param value The value
     * @return this class for chaining.
     */
    public LogicalOperator<T> lessThan(Object value) {
        this.init(LogicalOperators.LESS_THAN, value);
        return this.logicalOperator;
    }

    /**
     * Adds a {@code <} comparison using a subquery.
     *
     * @param subquery The subquery
     * @return this class for chaining.
     */
    public LogicalOperator<T> lessThan(QueryBuilder subquery) {
        this.init(LogicalOperators.LESS_THAN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code LIKE} comparison with the given value.
     *
     * @param value The value.
     * @return this class for chaining.
     */
    public LogicalOperator<T> like(Object value) {

        this.init(LogicalOperators.LIKE, value);
        return this.logicalOperator;
    }

    /**
     * Adds a {@code LIKE}  comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     */
    public LogicalOperator<T> like(QueryBuilder subquery) {
        this.init(LogicalOperators.LIKE, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds an {@code IN} comparison with the given values.
     *
     * @param values The values.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public LogicalOperator<T> in(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("IN requires at least one value.");
        }

        this.init(LogicalOperators.IN, Arrays.asList(values));
        return this.logicalOperator;
    }

    /**
     * Adds an {@code IN} comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public LogicalOperator<T> in(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("IN requires a valid subquery.");
        }
        this.init(LogicalOperators.IN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code NOT IN} comparison with the given values.
     *
     * @param values The values.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public LogicalOperator<T> notIn(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("NOT IN requires at least one value.");
        }
        this.init(LogicalOperators.NOT_IN, Arrays.asList(values));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code NOT IN} comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public LogicalOperator<T> notIn(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalOperators.NOT_IN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code >} comparison with the given value.
     *
     * @param value The values.
     * @return this class for chaining.
     */
    public LogicalOperator<T> greaterThan(Object value) {
        this.init(LogicalOperators.GREATER_THAN, value);
        return this.logicalOperator;
    }

    /**
     * Adds a {@code >}  comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     */
    public LogicalOperator<T> greaterThan(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalOperators.GREATER_THAN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    /**
     * Adds a {@code BETWEEN} comparison with two values.
     *
     * @param firstValue  The value.
     * @param secondValue The value.
     * @return this class for chaining.
     * @throws IllegalArgumentException if any value is null.
     */
    public LogicalOperator<T> between(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("BETWEEN requires exactly two values.");
        }
        this.init(LogicalOperators.BETWEEN, firstValue, secondValue);
        return this.logicalOperator;
    }

    /**
     * Adds a {@code NOT BETWEEN} comparison with two values.
     *
     * @param firstValue  The value.
     * @param secondValue The value.
     * @return this class for chaining.
     * @throws IllegalArgumentException if any value is null.
     */
    public LogicalOperator<T> notBetween(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("NOT BETWEEN requires exactly two values.");
        }
        this.init(LogicalOperators.NOT_BETWEEN, firstValue, secondValue);
        return this.logicalOperator;
    }

    /**
     * Returns the associated subquery handler if present.
     *
     * @return Returns the associated subquery handler if present.
     */
    public SubqueryHandler<T> getSubqueryHandler() {
        return subqueryHandler;
    }

    /**
     * Returns the values used in the comparison.
     *
     * @return Returns the values used in the comparison.
     */
    public Object[] getValues() {
        if (values == null)
            return new Object[0];
        return values;
    }

    /**
     * Returns the SQL comparison symbol.
     *
     * @return Returns the SQL comparison symbol.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the {@link ConditionBuilder} linked to this comparison.
     *
     * @return Returns the {@link ConditionBuilder} linked to this comparison.
     */
    public ConditionBuilder<T> getCondition() {
        return condition;
    }

    /**
     * Returns the column name for this comparison.
     *
     * @return Returns the column name for this comparison.
     */
    @Nonnull
    public String getColumn() {
        return column;
    }

    /**
     * Returns the logical operator for chaining further conditions.
     *
     * @return  Returns the logical operator for chaining further conditions.
     */
    public LogicalOperator<T> getLogicalOperator() {
        return logicalOperator;
    }

    /**
     * Returns the SQL symbol as a string.
     *
     * @return Returns the SQL symbol as a string.
     */
    @Override
    public String toString() {
        return symbol;
    }

}
