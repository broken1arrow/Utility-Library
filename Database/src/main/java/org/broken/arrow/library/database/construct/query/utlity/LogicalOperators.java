package org.broken.arrow.library.database.construct.query.utlity;
/**
 * Enum representing various logical and comparison operators commonly used in SQL queries.
 * <p>
 * Includes equality, relational, pattern matching, set membership, range, and logical operators.
 * Each operator is associated with its symbolic string representation as used in SQL syntax.
 */
public enum LogicalOperators {

    /** Equality operator: "=" */
    EQUALS("="),

    /** Less-than operator: "&lt;" */
    LESS_THAN("<"),

    /** Greater-than operator: "&gt;" */
    GREATER_THAN(">"),

    /** Pattern matching operator: "LIKE" */
    LIKE("LIKE"),

    /** Set membership operator: "IN" */
    IN("IN"),

    /** Set exclusion operator: "NOT IN" */
    NOT_IN("NOT IN"),

    /** Range operator: "BETWEEN" */
    BETWEEN("BETWEEN"),

    /** Negated range operator: "NOT BETWEEN" */
    NOT_BETWEEN("NOT BETWEEN"),

    /** Logical OR operator: "OR" */
    OR("OR"),

    /** Logical AND operator: "AND" */
    AND("AND"),

    /** Logical NOT operator: "NOT" */
    NOT("NOT")
    ;

    private final String symbol;

    /**
     * Constructs a LogicalOperators enum constant with the specified symbol.
     *
     * @param symbol The string representation of the operator.
     */
    LogicalOperators(String symbol) {
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
}
