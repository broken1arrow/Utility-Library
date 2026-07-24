package org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a SQL JOIN condition including the join type,
 * the target table, optional alias, join condition expression,
 * and a flag indicating if the old style (comma-separated) join syntax is used.
 */
public class JoinCondition {
    private JoinBuildContext joinBuildContext;
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
     * Constructs a new {@code JoinCondition} with specified parameters.
     *
     * @param joinType the type of join (INNER, LEFT, etc.)
     * @param table    the name of the table to join
     * @param alias    optional alias for the joined table (can be null)
     * @param context  the SQL ON condition for the join
     * @param oldStyle true if using old-style comma join syntax, false otherwise
     */
    public JoinCondition(JoinType joinType, String table, String alias, JoinBuildContext context, boolean oldStyle) {
        this.type = joinType;
        this.table = table;
        this.alias = alias;
        this.onCondition = context.build(joinType);
        this.joinBuildContext = context;
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
        String aliasPart = alias != null && !alias.isEmpty() ? " AS " + alias : "";
        return oldStyle ? ", " + table + aliasPart : " " + type + " " + table + aliasPart + " " + onCondition;
    }

    /**
     * Get the raw parameters set in the order is put in.
     *
     * @return An ordered list of raw parameter values for this specific clause.
     */
    @Nonnull
    public List<Object> getRawParameters() {
        if (this.type == JoinType.CROSS)
            return new ArrayList<>();
        return joinBuildContext.getConditionsList().stream()
                .flatMap(comparison -> comparison.getValuesFiltered().stream())
                .collect(Collectors.toList());
    }
}
