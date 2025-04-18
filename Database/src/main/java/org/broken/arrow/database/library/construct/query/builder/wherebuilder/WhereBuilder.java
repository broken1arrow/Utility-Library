package org.broken.arrow.database.library.construct.query.builder.wherebuilder;


import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.columnbuilder.Aggregation;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.broken.arrow.database.library.construct.query.utlity.Formatting.formatConditions;


public class WhereBuilder {
    private final List<ComparisonHandler<?>> conditionsList = new ArrayList<>();
    private final boolean globalEnableQueryPlaceholders;

    public WhereBuilder() {
        this(true);
    }

    public WhereBuilder(boolean enableQueryPlaceholders) {
        this.globalEnableQueryPlaceholders = enableQueryPlaceholders;
    }

    public static WhereBuilder of() {
        return new WhereBuilder();
    }

    public static WhereBuilder of(boolean enableQueryPlaceholders) {
        return new WhereBuilder(enableQueryPlaceholders);
    }

    public ComparisonHandler<WhereBuilder> where(final String columnName) {
        return this.where(columnName, this.globalEnableQueryPlaceholders);
    }

    public ComparisonHandler<WhereBuilder> where(final String columnName, final boolean enableQueryPlaceholders) {
        return this.where(columnName, a -> {
        }, enableQueryPlaceholders);
    }

    public ComparisonHandler<WhereBuilder> where(final String columnName, final Consumer<Aggregation> callBack) {
        return this.where(columnName, callBack, this.globalEnableQueryPlaceholders);
    }

    public ComparisonHandler<WhereBuilder> where(final String columnName, final Consumer<Aggregation> callBack, final boolean enableQueryPlaceholders) {
        Marker marker = isAllowingQueryPlaceholders(enableQueryPlaceholders) ? Marker.PLACEHOLDER : Marker.USE_VALUE;
        Column column = new Column(columnName, "");
        callBack.accept(column.getAggregation());

        ComparisonHandler<WhereBuilder> operator = new ComparisonHandler<>(this, column.toString(), marker);
        addCondition(operator);
        return operator;
    }

    private void addCondition(ComparisonHandler<WhereBuilder> condition) {
        conditionsList.add(condition);
    }

    public String build() {
        if (conditionsList.isEmpty())
            return "";
        String condition = formatConditions(conditionsList);
        return (" WHERE " + condition).replace(";", "");
    }

    public boolean isEmpty() {
        return conditionsList.isEmpty();
    }

    public List<ComparisonHandler<?>> getConditionsList() {
        return conditionsList;
    }

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

    private boolean isAllowingQueryPlaceholders(boolean enableQueryPlaceholders) {
        return enableQueryPlaceholders && globalEnableQueryPlaceholders;
    }

}
