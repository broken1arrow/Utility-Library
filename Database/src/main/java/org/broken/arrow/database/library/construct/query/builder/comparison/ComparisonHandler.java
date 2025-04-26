package org.broken.arrow.database.library.construct.query.builder.comparison;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.database.library.construct.query.utlity.LogicalOperators;
import org.broken.arrow.database.library.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ComparisonHandler<T> {

    private final LogicalOperator<T> logicalOperator;
    private final StringBuilder columnsBuilt = new StringBuilder();
    private final String column;
    private ConditionBuilder<T> condition;
    private SubqueryHandler<T> subqueryHandler;
    private Object[] values;
    private String symbol;

    public ComparisonHandler(T clazz, String column, Marker marker) {
        this.column = column;
        condition = new ConditionBuilder<>(this, marker);
        this.logicalOperator = new LogicalOperator<>(clazz, column, condition);
    }

    public ComparisonHandler() {
        this.logicalOperator = null;
        this.column = "";
    }

    public StringBuilder getColumnsBuilt() {
        return columnsBuilt;
    }

    private void init(LogicalOperators symbol, Object value) {
        this.symbol = symbol.getSymbol();
        if (value instanceof List<?>) {
            this.values = new ArrayList<>((List<?>) value).toArray();
        } else {
            if (value instanceof SubqueryHandler<?>) {
                final SubqueryHandler<T> subquery = (SubqueryHandler<T>) value;
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

    public LogicalOperator<T> equal(Object value) {
        this.init(LogicalOperators.EQUALS, value);
        return this.logicalOperator;
    }

    public LogicalOperator<T> equal(QueryBuilder subquery) {
        this.init(LogicalOperators.EQUALS, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> lessThan(Object value) {
        this.init(LogicalOperators.LESS_THAN, value);
        return this.logicalOperator;
    }

    public LogicalOperator<T> lessThan(QueryBuilder subquery) {
        this.init(LogicalOperators.LESS_THAN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> like(Object value) {

        this.init(LogicalOperators.LIKE, value);
        return this.logicalOperator;
    }

    public LogicalOperator<T> like(QueryBuilder subquery) {
        this.init(LogicalOperators.LIKE, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> in(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("IN requires at least one value.");
        }

        this.init(LogicalOperators.IN, Arrays.asList(values));
        return this.logicalOperator;
    }

    public LogicalOperator<T> in(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("IN requires a valid subquery.");
        }
        this.init(LogicalOperators.IN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> notIn(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("NOT IN requires at least one value.");
        }
        this.init(LogicalOperators.NOT_IN, Arrays.asList(values));
        return this.logicalOperator;
    }

    public LogicalOperator<T> notIn(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalOperators.NOT_IN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> greaterThan(Object value) {
        this.init(LogicalOperators.GREATER_THAN, value);
        return this.logicalOperator;
    }

    public LogicalOperator<T> greaterThan(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalOperators.GREATER_THAN, new SubqueryHandler<>(subquery));
        return this.logicalOperator;
    }

    public LogicalOperator<T> between(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("BETWEEN requires exactly two values.");
        }
        this.init(LogicalOperators.BETWEEN, firstValue, secondValue);
        return this.logicalOperator;
    }

    public LogicalOperator<T> notBetween(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("NOT BETWEEN requires exactly two values.");
        }
        this.init(LogicalOperators.NOT_BETWEEN, firstValue, secondValue);
        return this.logicalOperator;
    }

    public SubqueryHandler<T> getSubqueryHandler() {
        return subqueryHandler;
    }

    public Object[] getValues() {
        if (values == null)
            return new Object[0];
        return values;
    }

    public String getSymbol() {
        return symbol;
    }

    public ConditionBuilder<T> getCondition() {
        return condition;
    }

    @Nonnull
    public String getColumn() {
        return column;
    }

    public LogicalOperator<T> getLogicalOperator() {
        return logicalOperator;
    }

    @Override
    public String toString() {
        return symbol;
    }

}
