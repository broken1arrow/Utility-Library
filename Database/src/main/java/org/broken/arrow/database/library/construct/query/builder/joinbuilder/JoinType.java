package org.broken.arrow.database.library.construct.query.builder.joinbuilder;

public enum JoinType {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    FULL("FULL JOIN"),
    CROSS("CROSS JOIN");

    private final String sql;

    JoinType(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
