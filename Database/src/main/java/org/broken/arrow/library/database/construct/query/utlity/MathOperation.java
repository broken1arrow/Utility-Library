package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.construct.query.columnbuilder.Aggregation;

/**
 * Enum representing various mathematical and bitwise operations with their associated symbols.
 * <p>
 * This includes common arithmetic operations like addition, subtraction, multiplication,
 * division, modulus, and exponentiation, as well as bitwise operations such as AND, OR,
 * and bit shifts.
 * <p>
 * The special constant {@link #PER_ROUND} is used to indicate that each aggregation or function
 * should be applied separately rather than grouped under a single operation. This influences
 * how rounding and other mathematical or logical operations are handled in aggregation contexts.
 * <p>
 * Usage notes for {@link #PER_ROUND}:
 * <ul>
 *   <li>With rounding functions, it ensures each function is rounded independently:
 *   e.g., {@code ROUND(MIN(u.length), 2), ROUND(SUM(u.length), 2)}</li>
 *   <li>Without rounding, it lists functions separately instead of combining them:
 *   e.g., {@code MIN(u.length), SUM(u.length)}</li>
 * </ul>
 * This behavior is the default for {@link Aggregation#withAggregation(CalcFunc)}.
 * When using math operations via {@link Aggregation#round(MathOperation, Number, String)} or
 * {@link Aggregation#round(MathOperation, Number)}, you can choose to combine calculations as needed.
 */
public enum MathOperation {

    /**
     * Addition operation symbol: "+"
     */
    ADD("+"),

    /**
     * Subtraction operation symbol: "-"
     */
    SUBTRACT("-"),

    /**
     * Multiplication operation symbol: "*"
     */
    MULTIPLY("*"),

    /**
     * Division operation symbol: "/"
     */
    DIVIDE("/"),

    /**
     * Modulus (remainder) operation symbol: "%"
     */
    MODULUS("%"),

    /**
     * Exponentiation operation symbol: "^"
     */
    EXPONENTIATE("^"),

    /**
     * Bitwise AND operation symbol: "&amp;"
     */
    BITWISE_AND("&"),

    /**
     * Bitwise OR operation symbol: "|"
     */
    BITWISE_OR("|"),

    /**
     * Bitwise left shift operation symbol: "&lt;&lt;"
     */
    SHIFT_LEFT("<<"),

    /**
     * Bitwise right shift operation symbol: "&gt;&gt;"
     */
    SHIFT_RIGHT(">>"),

    /**
     * Ensures that each aggregation or function is applied separately rather than grouped under a single operation.
     * This affects both rounding and mathematical/logical operations.
     * <p>
     * - **With ROUND**: Each function gets its own rounding operation instead of being wrapped together.
     * Example: `ROUND(MIN(u.length), 2), ROUND(SUM(u.length), 2)`
     * <p>
     * - **Without ROUND**: Functions are listed separately instead of being combined.
     * Example: `MIN(u.length), SUM(u.length)`
     * <p>
     * This is the default behavior for {@link Aggregation#withAggregation(CalcFunc)}.
     * If using math operations via {@link Aggregation#round(MathOperation, Number, String)} or
     * {@link Aggregation#round(MathOperation, Number)}, you can combine calculations as needed.
     */
    PER_ROUND(",");

    private final String symbol;

    /**
     * Constructs a MathOperation with the specified symbol.
     *
     * @param symbol the string representation of the operation (e.g., "+", "-", "*")
     */
    MathOperation(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the symbol string associated with this mathematical operation.
     *
     * @return the symbol of the operation.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Checks if this operation is {@link #PER_ROUND}, indicating that
     * aggregation or functions should be applied separately.
     *
     * @return true if this operation is {@code PER_ROUND}, false otherwise.
     */
    public boolean isSplit() {
        return this == PER_ROUND;
    }
}