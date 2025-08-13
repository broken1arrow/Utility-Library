package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.builder.joinbuilder.JoinCondition;
import org.broken.arrow.library.database.construct.query.builder.joinbuilder.JoinType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for SQL JOIN clauses.
 * <p>
 * Supports adding different types of JOINs and building the complete JOIN clause string.
 * Also supports legacy-style joins.
 * </p>
 */
public class JoinBuilder {
    private final List<JoinCondition> joins = new ArrayList<>();

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param type        the join type (INNER, LEFT, etc.)
     * @param table       the table name to join
     * @param alias       alias for the joined table
     * @param onCondition join condition
     */
    public void join(JoinType type, String table, String alias, String onCondition) {
        joins.add(new JoinCondition(type, table, alias, onCondition, false));
    }

    /**
     * Adds a legacy-style join without alias or condition.
     *
     * @param table table name
     */
    public void oldJoin(String table) {
        this.oldJoin(table, null);
    }

    /**
     * Adds a legacy-style join with optional alias.
     *
     * @param table table name
     * @param alias alias for the table (nullable)
     */
    public void oldJoin(String table, String alias) {
        joins.add(new JoinCondition(null, table, alias, null, true));
    }

    /**
     * Builds the complete JOIN clause string.
     *
     * @return SQL JOIN clause or empty string if no joins
     */
    public String build() {
        return joins.isEmpty() ? "" : joins.stream().map(JoinCondition::toString).collect(Collectors.joining(" "));
    }

    /**
     * Checks if any legacy-style joins were added.
     *
     * @return true if any old-style joins exist
     */
    public boolean hasOldJoins() {
        return joins.stream().anyMatch(JoinCondition::isOldStyle);
    }
}
