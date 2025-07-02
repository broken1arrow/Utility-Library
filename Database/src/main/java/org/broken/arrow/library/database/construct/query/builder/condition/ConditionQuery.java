package org.broken.arrow.library.database.construct.query.builder.condition;

import org.broken.arrow.library.database.construct.query.utlity.LogicalOperators;

public class ConditionQuery<T> {
    private final String column;
    private final ConditionBuilder<T> conditionBuilder;
    private LogicalOperators logicalOperator;

    public ConditionQuery(String columnName, ConditionBuilder<T> conditionBuilder) {
      this.column = columnName;
      this.conditionBuilder = conditionBuilder;
    }

    public String getColumn() {
      return column;
    }

    public ConditionBuilder<T> getWhereCondition() {
      return conditionBuilder;
    }

    public LogicalOperators getLogicalOperator() {
      return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperators logicalOperator) {
      this.logicalOperator = logicalOperator;
    }

    @Override
    public String toString() {
      return "ConditionQuery{" +
              "column='" + column + '\'' +
              ", whereCondition='" + conditionBuilder.toString() + '\'' +
              ", logicalOperator=" + logicalOperator +
              '}';
    }
  }