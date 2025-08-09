package org.broken.arrow.library.database.construct.query.builder.condition;

import org.broken.arrow.library.database.construct.query.utlity.LogicalOperators;
/**
 * Represents a single SQL condition within a query, including its target column,
 * the comparison logic, and an optional logical operator to chain conditions.
 * <p>
 * This class binds a column name to a {@link ConditionBuilder} instance, which
 * defines the actual SQL comparison (e.g., {@code = ?}, {@code BETWEEN ? AND ?}).
 * An optional {@link LogicalOperators} value may be set to combine multiple
 * conditions (e.g., {@code AND}, {@code OR}).
 *
 * @param <T> the type of the parent query or builder that this condition belongs to
 */
public class ConditionQuery<T> {
    private final String column;
    private final ConditionBuilder<T> conditionBuilder;
    private LogicalOperators logicalOperator;

    /**
     * Creates a new condition query for the given column and condition builder.
     *
     * @param columnName       the name of the column to apply the condition to
     * @param conditionBuilder the builder responsible for constructing the SQL comparison
     */
    public ConditionQuery(String columnName, ConditionBuilder<T> conditionBuilder) {
      this.column = columnName;
      this.conditionBuilder = conditionBuilder;
    }

    /**
     * Returns the column name that this condition targets.
     *
     * @return the column name
     */
    public String getColumn() {
      return column;
    }

    /**
     * Returns the condition builder that produces the SQL comparison for this column.
     *
     * @return the associated {@link ConditionBuilder}
     */
    public ConditionBuilder<T> getWhereCondition() {
      return conditionBuilder;
    }

    /**
     * Returns the logical operator used to combine this condition with others.
     * <p>
     * May be {@code null} if this condition is standalone.
     *
     * @return the logical operator, or {@code null} if not set
     */
    public LogicalOperators getLogicalOperator() {
      return logicalOperator;
    }

    /**
     * Sets the logical operator to combine this condition with others.
     *
     * @param logicalOperator the logical operator (e.g., {@code AND}, {@code OR})
     */
    public void setLogicalOperator(LogicalOperators logicalOperator) {
      this.logicalOperator = logicalOperator;
    }

    /**
     * Returns a string representation of this condition query, including
     * the column, the built condition, and the logical operator.
     *
     * @return a string representation for debugging
     */
    @Override
    public String toString() {
      return "ConditionQuery{" +
              "column='" + column + '\'' +
              ", whereCondition='" + conditionBuilder.toString() + '\'' +
              ", logicalOperator=" + logicalOperator +
              '}';
    }
  }