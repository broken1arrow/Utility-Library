package org.broken.arrow.library.database.construct.query.utlity;

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
    SHIFT_RIGHT(">>");


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


}