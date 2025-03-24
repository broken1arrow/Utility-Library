package org.broken.arrow.database.library.construct.query.utlity;

public enum Marker {
    PLACEHOLDER("?"),
    USE_VALUE("set-value");
    private final String symbol;

    Marker(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
