package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereCondition;
import org.broken.arrow.database.library.construct.query.utlity.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.broken.arrow.database.library.construct.query.utlity.Formatting.formatConditions;


public class WhereBuilder {
  private final List<WhereCondition<WhereBuilder>> conditions = new ArrayList<>();
  private final StringBuilder columnsBuilt = new StringBuilder();

  private final boolean globalEnableQueryPlaceholders;

  protected WhereBuilder() {
    this(true);
  }

  protected WhereBuilder(boolean enableQueryPlaceholders) {
    this.globalEnableQueryPlaceholders = enableQueryPlaceholders;
  }
  public static WhereBuilder of() {
    return new  WhereBuilder();
  }

  public static WhereBuilder of(boolean enableQueryPlaceholders) {
    return new  WhereBuilder(enableQueryPlaceholders);
  }
  public ComparisonHandler<WhereBuilder> where(String column) {
    return where( column, globalEnableQueryPlaceholders);
  }

  public ComparisonHandler<WhereBuilder> where(String column, boolean enableQueryPlaceholders) {
    Marker marker = isAllowingQueryPlaceholders(enableQueryPlaceholders) ? Marker.PLACEHOLDER : Marker.USE_VALUE;
    ComparisonHandler<WhereBuilder> operator = new ComparisonHandler<>(this, column, columnsBuilt, marker);
    addCondition(operator.getCondition());
    return operator;
  }

  private void addCondition(WhereCondition<WhereBuilder> condition) {
    conditions.add(condition);
  }


  public String build() {
    return conditions.isEmpty() ? "" :
            " WHERE" + formatConditions(columnsBuilt.toString().replace(";",""));
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
