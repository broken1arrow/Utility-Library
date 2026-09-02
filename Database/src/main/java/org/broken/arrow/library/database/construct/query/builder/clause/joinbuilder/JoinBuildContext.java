package org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.broken.arrow.library.database.construct.query.builder.condition.Formatting.formatConditions;

public class JoinBuildContext {
    private final List<ComparisonHandler<JoinBuildContext>> conditionsList = new ArrayList<>();
    private final JoinType joinType;
    private final boolean globalEnableQueryPlaceholders;

    /**
     * Creates a new {@code WhereBuilder} instance with configuration from the given {@link QueryBuilder}.
     *
     * @param joinType the type of join used.
     * @param queryBuilder the query builder to determine placeholder usage
     */
    public JoinBuildContext(@Nonnull final JoinType joinType, @Nonnull final QueryBuilder queryBuilder) {
        this.joinType = joinType;
        this.globalEnableQueryPlaceholders = queryBuilder.isGlobalEnableQueryPlaceholders();
    }

    /**
     * Static factory method to create a {@code WhereBuilder} instance.
     *
     * @param joinType the type of join used.
     * @param queryBuilder the query builder to determine placeholder usage
     * @return a new WhereBuilder instance
     */
    public static JoinBuildContext of(@Nonnull final JoinType joinType, @Nonnull final QueryBuilder queryBuilder) {
        return new JoinBuildContext(joinType, queryBuilder);
    }

    /**
     * Starts a Join condition on the specified column without aggregation.
     *
     * @param columnName the name of the column for the join condition
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<JoinBuildContext> on(final String columnName) {
        return this.on(Column.of(columnName));
    }

    /**
     * Starts a join condition on the specified column with an aggregation callback.
     * Aggregations in join clause are uncommon but supported for flexibility.
     *
     * @param column the column for the join condition
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<JoinBuildContext> on(final Column column) {
        final Marker marker = globalEnableQueryPlaceholders ? Marker.PLACEHOLDER : Marker.USE_VALUE;
        final ComparisonHandler<JoinBuildContext> operator = new ComparisonHandler<>(this, column.toString(), marker);
        addCondition(operator);
        return operator;
    }

    /**
     * Builds the full join clause as a String.
     * Returns an empty string if no conditions are present.
     *
     * @return The join clause SQL fragment (including "on"), or empty string if none.
     */
    public String build() {
        if (conditionsList.isEmpty())
            return "";
        if (this.joinType == JoinType.CROSS)
            return "";
        final String condition = formatConditions(conditionsList);
        return ("ON " + condition).replace(";", "");
    }

    /**
     * Checks if there are no conditions defined.
     *
     * @return true if no conditions exist, false otherwise
     */
    public boolean isEmpty() {
        return conditionsList.isEmpty();
    }

    /**
     * Returns the list of comparison conditions added to this builder.
     *
     * @return list of {@link ComparisonHandler} instances
     */
    public List<ComparisonHandler<JoinBuildContext>> getConditionsList() {
        return conditionsList;
    }

    private void addCondition(ComparisonHandler<JoinBuildContext> condition) {
        conditionsList.add(condition);
    }

}
