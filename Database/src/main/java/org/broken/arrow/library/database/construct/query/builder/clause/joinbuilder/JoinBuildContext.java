package org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.broken.arrow.library.database.construct.query.builder.condition.Formatting.formatConditions;

public class JoinBuildContext  {
    private final List<ComparisonHandler<JoinBuildContext>> conditionsList = new ArrayList<>();
    private final boolean globalEnableQueryPlaceholders;

    /**
     * Creates a new {@code WhereBuilder} with placeholders enabled by default.
     */
    public JoinBuildContext() {
        this.globalEnableQueryPlaceholders = true;
    }

    /**
     * Creates a new {@code WhereBuilder} instance with configuration from the given {@link QueryBuilder}.
     *
     * @param queryBuilder the query builder to determine placeholder usage
     */
    public JoinBuildContext(@Nonnull final QueryBuilder queryBuilder) {
        this.globalEnableQueryPlaceholders = queryBuilder.isGlobalEnableQueryPlaceholders();
    }

    /**
     * Returns whether query placeholders are enabled globally for this builder.
     *
     * @return true if placeholders are enabled; false otherwise
     */
    public boolean isGlobalEnableQueryPlaceholders() {
        return globalEnableQueryPlaceholders;
    }

    /**
     * Static factory method to create a {@code WhereBuilder} instance.
     *
     * @param queryBuilder the query builder to determine placeholder usage
     * @return a new WhereBuilder instance
     */
    public static JoinBuildContext of(@Nonnull final QueryBuilder queryBuilder) {
        return new JoinBuildContext(queryBuilder);
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
     * @param column  the column for the join condition
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<JoinBuildContext> on(final Column column) {
        Marker marker = globalEnableQueryPlaceholders ? Marker.PLACEHOLDER : Marker.USE_VALUE;

        ComparisonHandler<JoinBuildContext> operator = new ComparisonHandler<>(this, column.toString(), marker);
        addCondition(operator);
        return operator;
    }

    /**
     * Builds the full join clause as a String.
     * Returns an empty string if no conditions are present.
     *
     * @param joinType The type of join.
     * @return The join clause SQL fragment (including "on"), or empty string if none.
     */
    public String build(@Nonnull final JoinType joinType) {
        if (conditionsList.isEmpty())
            return "";
        final String condition = formatConditions(conditionsList);
        if (joinType == JoinType.CROSS)
            return "";
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
