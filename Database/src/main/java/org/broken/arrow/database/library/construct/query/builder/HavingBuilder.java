package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereCondition;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.broken.arrow.database.library.construct.query.utlity.Formatting.formatConditions;


public class HavingBuilder {

  private final List<WhereCondition<HavingBuilder>> conditions = new ArrayList<>();
  private final StringBuilder columnsBuilt = new StringBuilder();
private boolean globalEnableQueryPlaceholders = true;

  public ComparisonHandler<HavingBuilder> having(final ColumnManger column) {
    return having(column, globalEnableQueryPlaceholders);
  }

  public ComparisonHandler<HavingBuilder> having(final ColumnManger column, boolean enableQueryPlaceholders) {
    Marker marker = isAllowingQueryPlaceholders(enableQueryPlaceholders) ? Marker.PLACEHOLDER : Marker.USE_VALUE;
    ComparisonHandler<HavingBuilder> comparisonHandler = new ComparisonHandler<>(this, column.getColumnsBuilt().stream().findFirst().orElse(ColumnManger.of("").next()).toString(), columnsBuilt, marker);

    conditions.add(comparisonHandler.getCondition());
    return comparisonHandler;
  }

  public void setEnableQueryPlaceholders(boolean globalEnableQueryPlaceholders) {
    this.globalEnableQueryPlaceholders = globalEnableQueryPlaceholders;
  }

  public String build() {
    return conditions.isEmpty() ? "" : " HAVING" + formatConditions(columnsBuilt.toString().replace(";",""));
  }

  public List<Object> getValues() {
    return conditions.stream()
            .map(WhereCondition::getValues)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toList());
  }

  private boolean isAllowingQueryPlaceholders(boolean enableQueryPlaceholders) {
    return enableQueryPlaceholders && globalEnableQueryPlaceholders;
  }

}
