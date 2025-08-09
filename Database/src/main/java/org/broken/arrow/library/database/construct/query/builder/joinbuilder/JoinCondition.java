package org.broken.arrow.library.database.construct.query.builder.joinbuilder;

/**
 * Represents a SQL JOIN condition including the join type,
 * the target table, optional alias, join condition expression,
 * and a flag indicating if the old style (comma-separated) join syntax is used.
 */
public class JoinCondition {
    private final JoinType type;
    private final String table;
    private final String alias;
    private final String onCondition;
    private final boolean oldStyle;

    /**
     * Constructs a new {@code JoinCondition} with specified parameters.
     *
     * @param type        the type of join (INNER, LEFT, etc.)
     * @param table       the name of the table to join
     * @param alias       optional alias for the joined table (can be null)
     * @param onCondition the SQL ON condition for the join
     * @param oldStyle    true if using old-style comma join syntax, false otherwise
     */
    public JoinCondition(JoinType type, String table, String alias, String onCondition, boolean oldStyle) {
        this.type = type;
        this.table = table;
        this.alias = alias;
        this.onCondition = onCondition;
        this.oldStyle = oldStyle;
    }

    /**
     * Indicates whether the join uses old-style (comma separated) syntax.
     *
     * @return true if old-style join syntax is used; false otherwise
     */
    public boolean isOldStyle() {
        return oldStyle;
    }

    /**
     * Returns the string representation of this join condition,
     * formatted either in old-style or explicit join syntax.
     *
     * @return the SQL join clause as a string
     */
    @Override
    public String toString() {
        String aliasPart = alias != null ? " AS " + alias : "";
        return oldStyle ? ", " + table + aliasPart :" " + type + " " + table + aliasPart + " ON " + onCondition;
    }
}
