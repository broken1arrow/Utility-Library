package org.broken.arrow.library.database.construct.query.builder.joinbuilder;

/**
 * Enum representing the types of SQL JOIN operations.
 * Each enum constant is associated with its SQL keyword representation.
 */
public enum JoinType {
    /** Represents an INNER JOIN in SQL */
    INNER("INNER JOIN"),
    /** Represents a LEFT JOIN in SQL */
    LEFT("LEFT JOIN"),
    /** Represents a RIGHT JOIN in SQL */
    RIGHT("RIGHT JOIN"),
    /** Represents a FULL JOIN in SQL */
    FULL("FULL JOIN"),
    /** Represents a CROSS JOIN in SQL */
    CROSS("CROSS JOIN");

    private final String sql;
    /**
     * Constructs a JoinType with the corresponding SQL syntax.
     *
     * @param sql the SQL keyword for this join type
     */
    JoinType(String sql) {
        this.sql = sql;
    }

    /**
     * Returns the SQL keyword string for this join type.
     *
     * @return the SQL join keyword as string
     */
    @Override
    public String toString() {
        return sql;
    }
}
