package org.broken.arrow.database.library.construct.query.builder.havingbuilder;


import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.columnbuilder.Aggregation;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.Marker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.broken.arrow.database.library.construct.query.utlity.Formatting.formatConditions;


public class HavingBuilder {

    private final List<ComparisonHandler<?>> conditionsList = new ArrayList<>();
    private boolean globalEnableQueryPlaceholders = true;


    public HavingBuilder(@Nonnull final QueryBuilder queryBuilder) {
        this.globalEnableQueryPlaceholders = queryBuilder.isGlobalEnableQueryPlaceholders();

    }

    public static HavingBuilder of(@Nonnull final QueryBuilder queryBuilder) {
        return new HavingBuilder( queryBuilder);
    }

    public ComparisonHandler<HavingBuilder> having(@Nonnull final String columnName) {
        return this.having(columnName, a -> {});
    }


    public ComparisonHandler<HavingBuilder> having(@Nonnull final String columnName, Consumer<Aggregation> callBack) {
        final Marker marker = globalEnableQueryPlaceholders ? Marker.PLACEHOLDER : Marker.USE_VALUE;
        Column column = new Column(columnName, "");
        callBack.accept(column.getAggregation());

        final ComparisonHandler<HavingBuilder> comparisonHandler = new ComparisonHandler<>(this, column.toString(), marker);

        this.conditionsList.add(comparisonHandler);
        return comparisonHandler;
    }

    public String build() {
        if (conditionsList.isEmpty())
            return "";
        String condition = formatConditions(conditionsList);
        return (" HAVING " + condition).replace(";", "");
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
