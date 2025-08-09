package org.broken.arrow.library.database.construct.query.builder.havingbuilder;


import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Aggregation;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.broken.arrow.library.database.construct.query.utlity.Formatting.formatConditions;
/**
 * Builder class to construct SQL HAVING clause conditions.
 * <p>
 * Used to specify conditions on aggregated data (e.g., GROUP BY results).
 * Allows chaining multiple comparison conditions and supports placeholders
 * for safe query parameterization.
 * </p>
 */
public class HavingBuilder {

    private final List<ComparisonHandler<HavingBuilder>> conditionsList = new ArrayList<>();
    private boolean globalEnableQueryPlaceholders = true;

    /**
     * Creates a new {@code HavingBuilder} instance with configuration from the given {@link QueryBuilder}.
     *
     * @param queryBuilder the query builder to determine placeholder usage
     */
    public HavingBuilder(@Nonnull final QueryBuilder queryBuilder) {
        this.globalEnableQueryPlaceholders = queryBuilder.isGlobalEnableQueryPlaceholders();

    }

    /**
     * Static factory method to create a {@code HavingBuilder} instance.
     *
     * @param queryBuilder the query builder to determine placeholder usage
     * @return a new HavingBuilder instance
     */
    public static HavingBuilder of(@Nonnull final QueryBuilder queryBuilder) {
        return new HavingBuilder( queryBuilder);
    }

    /**
     * Starts a HAVING condition on the specified column without aggregation callback.
     *
     * @param columnName the name of the column to apply the HAVING condition
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<HavingBuilder> having(@Nonnull final String columnName) {
        return this.having(columnName, a -> {});
    }

    /**
     * Starts a HAVING condition on the specified column with an aggregation callback.
     *
     * @param columnName the name of the column to apply the HAVING condition
     * @param callBack   a consumer to configure aggregation (e.g., SUM, COUNT)
     * @return a {@link ComparisonHandler} to specify comparison operations
     */
    public ComparisonHandler<HavingBuilder> having(@Nonnull final String columnName, Consumer<Aggregation> callBack) {
        final Marker marker = globalEnableQueryPlaceholders ? Marker.PLACEHOLDER : Marker.USE_VALUE;
        Column column = new Column(columnName, "");
        callBack.accept(column.getAggregation());

        final ComparisonHandler<HavingBuilder> comparisonHandler = new ComparisonHandler<>(this, column.toString(), marker);

        this.conditionsList.add(comparisonHandler);
        return comparisonHandler;
    }

    /**
     * Builds the full HAVING clause as a String.
     * Returns an empty string if no conditions are present.
     *
     * @return The HAVING clause SQL fragment (including "HAVING"), or empty string if none.
     */
    public String build() {
        if (conditionsList.isEmpty())
            return "";
        String condition = formatConditions(conditionsList);
        return (" HAVING " + condition).replace(";", "");
    }

    /**
     * Returns the list of comparison conditions added to this builder.
     *
     * @return list of {@link ComparisonHandler} instances
     */
    public List<ComparisonHandler<HavingBuilder>> getConditionsList() {
        return conditionsList;
    }

    /**
     * Retrieves a map of placeholder indexes to values for this HAVING clause.
     * Useful for setting parameters in a PreparedStatement.
     *
     * @return Map of 1-based placeholder indexes to their corresponding values.
     */
    public Map<Integer, Object> getValues() {
        if (conditionsList.isEmpty())
            return new HashMap<>();

        List<Object> values = conditionsList.stream()
                .map(ComparisonHandler::getValues)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Map<Integer, Object> valuesMap = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            valuesMap.put(i + 1, values.get(i));
        }

        return valuesMap;
    }

}
