package org.broken.arrow.library.database.construct.query.utlity;

/**
 * Represents a SQL value marker used in query generation.
 * <p>
 * A marker can either be a {@code "?"} placeholder for prepared statements
 * or a literal {@code "set-value"} indicating that the value should be
 * inserted directly into the query.
 * </p>
 */
public enum Marker {
    PLACEHOLDER("?"),
    USE_VALUE("set-value");
    private final String symbol;

    Marker(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol associated with this marker.
     * <p>
     * This can be a {@code "?"} for prepared statements or {@code "set-value"}
     * for direct value insertion in the query.
     * </p>
     * @return the marker symbol
     */
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
