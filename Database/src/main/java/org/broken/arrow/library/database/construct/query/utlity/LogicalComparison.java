package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Enum representing various logical and comparison operators commonly used in SQL queries.
 * <p>
 * Includes equality, relational, pattern matching, set membership, range, and logical operators.
 * Each operator is associated with its symbolic string representation as used in SQL syntax.
 */
public enum LogicalComparison {

    /**
     * Equality operator: "="
     */
    EQUALS("="),

    /**
     * Less-than operator: "&lt;"
     */
    LESS_THAN("<"),

    /**
     * Greater-than operator: "&gt;"
     */
    GREATER_THAN(">"),

    /**
     * Pattern matching operator: "LIKE"
     */
    LIKE("LIKE"),

    /**
     * Set membership operator: "IN"
     */
    IN("IN"),

    /**
     * Set exclusion operator: "NOT IN"
     */
    NOT_IN("NOT IN"),

    /**
     * Range operator: "BETWEEN"
     */
    BETWEEN("BETWEEN"),

    /**
     * Negated range operator: "NOT BETWEEN"
     */
    NOT_BETWEEN("NOT BETWEEN"),

    /**
     * Negated not exist operator: "NOT EXISTS"
     */
    NOT_EXISTS("NOT EXISTS"),

    /**
     * Exist operator: "EXISTS"
     */
    EXISTS("EXISTS"),

    /**
     * Is null operator: "IS NULL"
     */
    IS_NULL("IS NULL"),

    /**
     * Is not null operator: "IS NOT NULL"
     */
    IS_NOT_NULL("IS NOT NULL");

    private final String symbol;

    /**
     * Constructs a LogicalOperators enum constant with the specified symbol.
     *
     * @param symbol The string representation of the operator.
     */
    LogicalComparison(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol associated with this logical operator.
     *
     * @return The operator symbol as a string.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the string representation of this logical operator.
     * This is the operator symbol.
     *
     * @return The operator symbol as a string.
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Applies this logical comparison operator to a given target handler, injecting the
     * provided values into the condition.
     *
     * @param targetHandler the handler on which the comparison operation will be applied
     * @param values        a list of values required for the comparison (e.g., the right-side value, or bounds for BETWEEN)
     * @param <T>           the builder type of the target condition
     * @return a {@link ConditionChainer} representing the applied operation, or {@code null} if the operation could not be applied
     */
    @Nullable
    public <T> ConditionChainer<T> applyTo(@Nonnull final ComparisonHandler<T> targetHandler, @Nonnull final List<Object> values) {
        final Object rightSide = !values.isEmpty() ? values.get(0) : null;
        ConditionChainer<T> chainer = null;

        switch (this) {
            case EQUALS:
                chainer = targetHandler.equal(rightSide);
                break;
            case LESS_THAN:
                chainer = targetHandler.lessThan(rightSide);
                break;
            case GREATER_THAN:
                chainer = targetHandler.greaterThan(rightSide);
                break;
            case LIKE:
                chainer = targetHandler.like(rightSide);
                break;
            case IN:
                chainer = targetHandler.in(rightSide);
                break;
            case NOT_IN:
                chainer = targetHandler.notIn(rightSide);
                break;
            case BETWEEN:
                if (values.size() > 1)
                    chainer = targetHandler.between(rightSide, values.get(1));
                break;
            case NOT_BETWEEN:
                if (values.size() > 1)
                    chainer = targetHandler.notBetween(rightSide, values.get(1));
                break;
            case NOT_EXISTS:
                if (rightSide instanceof QueryBuilder) {
                    chainer = targetHandler.notExists((QueryBuilder) rightSide);
                }
                break;
            case EXISTS:
                if (rightSide instanceof QueryBuilder) {
                    chainer = targetHandler.exists((QueryBuilder) rightSide);
                }
                break;
            case IS_NULL:
                chainer = targetHandler.isNull();
                break;
            case IS_NOT_NULL:
                chainer = targetHandler.isNotNull();
                break;
        }
        return chainer;
    }

}
