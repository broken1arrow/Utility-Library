package org.broken.arrow.library.database.construct.query.builder.joinbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Aggregation;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.ColumnRef;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.broken.arrow.library.database.construct.query.utlity.Formatting.formatConditions;

public class JoinBuildContext {
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
    public ComparisonHandler<JoinBuildContext> on(final String columnName, SqlArg arg) {
        return this.on(columnName, a -> {
        });
    }

    /**
     * Starts a join condition on the specified column with an aggregation callback.
     * Aggregations in join clause are uncommon but supported for flexibility.
     *
     * @param columnName  the name of the column for the join condition
     * @param aggregation a consumer to configure aggregation (e.g., for computed columns)
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<JoinBuildContext> on(final String columnName, final Consumer<Aggregation> aggregation) {
        Marker marker = globalEnableQueryPlaceholders ? Marker.PLACEHOLDER : Marker.USE_VALUE;
        Column column = new Column(columnName, "");
        aggregation.accept(column.getAggregation());

        ComparisonHandler<JoinBuildContext> operator = new ComparisonHandler<>(this, column.toString(), marker);
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
        SqlArg.col("").value();
        on("", SqlArg.col(""));
        if (conditionsList.isEmpty())
            return "";
        String condition = formatConditions(conditionsList);
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

    /**
     * Returns a map of parameter index to values for prepared statement usage.
     *
     * @return map of index to value for query parameters
     */
    public Map<Integer, Object> getValues() {
        if (conditionsList.isEmpty())
            return new HashMap<>();

        List<Object> values = conditionsList.stream()
                .map(ComparisonHandler::getValues)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Map<Integer, Object> valuesMap = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            valuesMap.put(i + 1, values.get(i));
        }

        return valuesMap;
    }

    private void addCondition(ComparisonHandler<JoinBuildContext> condition) {
        conditionsList.add(condition);
    }

}
