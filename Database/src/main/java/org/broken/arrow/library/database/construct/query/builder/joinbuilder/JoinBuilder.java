package org.broken.arrow.library.database.construct.query.builder.joinbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.ParameterSupplier;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builder for SQL JOIN clauses.
 * <p>
 * Supports adding different types of JOINs and building the complete JOIN clause string.
 * Also supports legacy-style joins.
 * </p>
 */
public class JoinBuilder implements ParameterSupplier {
    private final List<JoinCondition> joins = new ArrayList<>();
    private final QueryBuilder queryBuilder;

    /**
     * Creates a new {@code JoinBuilder} instance with the context if query placeholders should be used set in {@link QueryBuilder}.
     *
     * @param queryBuilder the query builder to determine placeholder usage.
     */
    public JoinBuilder(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    public void innerJoin(String table, String alias, Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        this.join(JoinType.INNER, table, alias, joinClause);
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    public void fullJoin(String table, String alias, Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        this.join(JoinType.FULL, table, alias, joinClause);
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    public void leftJoin(String table, String alias, Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        this.join(JoinType.LEFT, table, alias, joinClause);
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    public void rightJoin(String table, String alias, Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        this.join(JoinType.RIGHT, table, alias, joinClause);
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition.
     *
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    public void crossJoin(String table, String alias, Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        this.join(JoinType.CROSS, table, alias, joinClause);
    }


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
        joins.add(new JoinCondition(null, table, alias, (String) null, true));
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


    @Nonnull
    @Override
    public List<Object> getRawParameters() {
        return joins.stream()
                .map(JoinCondition::getRawParameters)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Adds a JOIN clause with the specified type, table, alias, and ON condition if not {@code cross join}.
     *
     * @param joinType   the join type (INNER, LEFT, etc.)
     * @param table      the table name to join
     * @param alias      alias for the joined table
     * @param joinClause join condition
     */
    private void join(@Nonnull final JoinType joinType, @Nonnull final String table, String alias, final Function<JoinBuildContext, ConditionChainer<JoinBuildContext>> joinClause) {
        JoinBuildContext operator = new JoinBuildContext(queryBuilder);
        joins.add(new JoinCondition(joinType, table, alias, joinClause.apply(operator).build(), false));
    }
}
