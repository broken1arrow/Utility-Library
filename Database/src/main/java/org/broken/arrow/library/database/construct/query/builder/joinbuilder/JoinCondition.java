package org.broken.arrow.library.database.construct.query.builder.joinbuilder;

public class JoinCondition {
    private final JoinType type;
    private final String table;
    private final String alias;
    private final String onCondition;
    private final boolean oldStyle;

    public JoinCondition(JoinType type, String table, String alias, String onCondition, boolean oldStyle) {
        this.type = type;
        this.table = table;
        this.alias = alias;
        this.onCondition = onCondition;
        this.oldStyle = oldStyle;
    }

    public boolean isOldStyle() {
        return oldStyle;
    }

    @Override
    public String toString() {
        String aliasPart = alias != null ? " AS " + alias : "";
        return oldStyle ? ", " + table + aliasPart :" " + type + " " + table + aliasPart + " ON " + onCondition;
    }
}
