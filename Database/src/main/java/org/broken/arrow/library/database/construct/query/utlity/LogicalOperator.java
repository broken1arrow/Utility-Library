package org.broken.arrow.library.database.construct.query.utlity;
/**
 * Enum representing logical conjunction operators used to combine condition blocks in SQL queries.
 * <p>
 * This enum defines the boolean operators ({@code AND}, {@code OR}, {@code NOT}) used to chain
 * multiple filter conditions together within {@code WHERE}, {@code HAVING}, and {@code JOIN ON} clauses.
 * </p>
 */
public enum LogicalOperator {
    /** Logical OR operator: {@code "OR"} */
    OR("OR"),

    /** Logical AND operator: {@code "AND"} */
    AND("AND"),

    /** Logical NOT operator: {@code "NOT"} */
    NOT("NOT");

    private final String symbol;

    /**
     * Constructs an {@code Operator} constant with the specified SQL symbol.
     *
     * @param symbol the SQL string representation of the operator
     */
    LogicalOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the SQL keyword associated with this logical operator.
     *
     * @return the operator symbol as an uppercase string (e.g., {@code "AND"}, {@code "OR"})
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the string representation of this operator, identical to {@link #getSymbol()}.
     *
     * @return the SQL operator keyword
     */
    @Override
    public String toString() {
        return symbol;
    }
}
