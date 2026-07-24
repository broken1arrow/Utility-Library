package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.ParameterSupplier;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.havingbuilder.HavingBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.LiteralVal;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.utlity.LogicalComparison;
import org.broken.arrow.library.database.construct.query.utlity.Marker;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles SQL comparison operations for a specific column within a query.
 * <p>
 * This class provides a fluent API to define comparison conditions such as
 * {@code =}, {@code <}, {@code >}, {@code IN}, {@code BETWEEN}, and others.
 * It supports both direct values and subqueries as comparison targets.
 * </p>
 *
 * @param <T> the parent query or builder type for fluent chaining
 */
public class ComparisonHandler<T> {

    private final ConditionChainer<T> conditionChainer;
    private final String columnName;
    private final boolean isWhereClause;
    private ConditionBuilder<T> condition;
    private SubqueryHandler<T> subqueryHandler;
    private LogicalComparison operator;
    private Object[] values;


    /**
     * Creates a new comparison handler for the specified column within the given query context.
     * <p>
     * This constructor initializes the {@link ConditionBuilder} and {@link ConditionChainer}
     * using the provided parent query instance and a marker for condition grouping or position.
     * </p>
     *
     * @param clazz      the parent query object or builder instance
     * @param columnName the name of the column to apply comparisons on
     * @param marker     the marker used to track this condition's grouping or position
     */
    public ComparisonHandler(T clazz, String columnName, Marker marker) {
        this.columnName = columnName;
        condition = new ConditionBuilder<>(this, marker);
        this.conditionChainer = new ConditionChainer<>(clazz, columnName, condition);
        this.isWhereClause = clazz instanceof HavingBuilder;
    }

    /**
     * Creates an empty comparison handler with no column or logical operator.
     * <p>
     * Primarily used as a placeholder or for cases where initialization is deferred.
     * @param isWhereClause if it used for a where clause.
     */
    public ComparisonHandler(final boolean isWhereClause) {
        this.conditionChainer = null;
        this.columnName = "";
        this.isWhereClause = false;
    }

    private void init(LogicalComparison symbol, Object value) {
        this.operator = symbol;
        if (value instanceof List<?>) {
            this.values = new ArrayList<>((List<?>) value).toArray();
        } else {
            if (value instanceof SubqueryHandler<?>) {
                @SuppressWarnings("unchecked") final SubqueryHandler<T> subquery = (SubqueryHandler<T>) value;
                this.subqueryHandler = subquery;
                final Map<Integer, Object> subValues = subquery.getSubquery().getValues();
                this.values = subValues.entrySet().stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                        .map(Map.Entry::getValue).toArray();
            } else {
                this.values = new Object[]{value};
            }
        }

    }

    private void init(LogicalComparison symbol, Object firstValue, Object secondValue) {
        this.operator = symbol;
        if (firstValue instanceof List<?>) {
            this.values = new ArrayList<>((List<?>) firstValue).toArray();
        } else
            this.values = new Object[]{firstValue, secondValue};
    }

    /**
     * Adds an {@code =} comparison with the given value.
     *
     * @param value The value
     * @return this class for chaining.
     */
    public ConditionChainer<T> equal(Object value) {
        if (value instanceof Column) {
            return equal((Column) value);
        }
        else if (value instanceof SqlArg) {
            return equal((SqlArg) value);
        }
        else {
            return equal(SqlArg.val(value));
        }
    }

    /**
     * Adds an {@code =} comparison using a subquery.
     *
     * @param subquery The subquery
     * @return this class for chaining.
     */
    public ConditionChainer<T> equal(QueryBuilder subquery) {
        this.init(LogicalComparison.EQUALS, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds an {@code =} comparison with the given value.
     *
     * @param value The value
     * @return this class for chaining.
     */
    public ConditionChainer<T> equal(SqlArg value) {
        if(this.isWhereClause && value instanceof Column){
            Column column = (Column) value;
            if (column.hasAggregate()) {
                throw new Validate.ValidateExceptions(
                        "Invalid SQL: Cannot compare against an aggregate function in a WHERE clause. "
                                + "Found as value for column '" + column.getColumnName() + "'."
                );
            }
        }
        this.init(LogicalComparison.EQUALS, value);
        return this.conditionChainer;
    }

    /**
     * Adds a {@code <} comparison with the given value.
     *
     * @param value The value
     * @return this class for chaining.
     */
    public ConditionChainer<T> lessThan(Object value) {
        this.init(LogicalComparison.LESS_THAN, value);
        return this.conditionChainer;
    }

    /**
     * Adds a {@code <} comparison using a subquery.
     *
     * @param subquery The subquery
     * @return this class for chaining.
     */
    public ConditionChainer<T> lessThan(QueryBuilder subquery) {
        this.init(LogicalComparison.LESS_THAN, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds a {@code LIKE} comparison with the given value.
     *
     * @param value The value.
     * @return this class for chaining.
     */
    public ConditionChainer<T> like(Object value) {

        this.init(LogicalComparison.LIKE, value);
        return this.conditionChainer;
    }

    /**
     * Adds a {@code LIKE}  comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     */
    public ConditionChainer<T> like(QueryBuilder subquery) {
        this.init(LogicalComparison.LIKE, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds an {@code IN} comparison with the given values.
     *
     * @param values The values.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public ConditionChainer<T> in(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("IN requires at least one value.");
        }

        this.init(LogicalComparison.IN, Arrays.asList(values));
        return this.conditionChainer;
    }

    /**
     * Adds an {@code IN} comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public ConditionChainer<T> in(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("IN requires a valid subquery.");
        }
        this.init(LogicalComparison.IN, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds a {@code NOT IN} comparison with the given values.
     *
     * @param values The values.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public ConditionChainer<T> notIn(Object... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("NOT IN requires at least one value.");
        }
        this.init(LogicalComparison.NOT_IN, Arrays.asList(values));
        return this.conditionChainer;
    }

    /**
     * Adds a {@code NOT IN} comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     * @throws IllegalArgumentException if no values are provided
     */
    public ConditionChainer<T> notIn(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalComparison.NOT_IN, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds a {@code >} comparison with the given value.
     *
     * @param value The values.
     * @return this class for chaining.
     */
    public ConditionChainer<T> greaterThan(Object value) {
        this.init(LogicalComparison.GREATER_THAN, value);
        return this.conditionChainer;
    }

    /**
     * Adds a {@code >}  comparison using a subquery.
     *
     * @param subquery The subquery.
     * @return this class for chaining.
     */
    public ConditionChainer<T> greaterThan(QueryBuilder subquery) {
        if (subquery == null) {
            throw new IllegalArgumentException("NOT IN requires a valid subquery.");
        }
        this.init(LogicalComparison.GREATER_THAN, new SubqueryHandler<>(subquery));
        return this.conditionChainer;
    }

    /**
     * Adds a {@code BETWEEN} comparison with two values.
     *
     * @param firstValue  The value.
     * @param secondValue The value.
     * @return this class for chaining.
     * @throws IllegalArgumentException if any value is null.
     */
    public ConditionChainer<T> between(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("BETWEEN requires exactly two values.");
        }
        this.init(LogicalComparison.BETWEEN, firstValue, secondValue);
        return this.conditionChainer;
    }

    /**
     * Adds a {@code NOT BETWEEN} comparison with two values.
     *
     * @param firstValue  The value.
     * @param secondValue The value.
     * @return this class for chaining.
     * @throws IllegalArgumentException if any value is null.
     */
    public ConditionChainer<T> notBetween(Object firstValue, Object secondValue) {
        if (firstValue == null || secondValue == null) {
            throw new IllegalArgumentException("NOT BETWEEN requires exactly two values.");
        }
        this.init(LogicalComparison.NOT_BETWEEN, firstValue, secondValue);
        return this.conditionChainer;
    }

    /**
     * Returns the associated subquery handler if present.
     *
     * @return Returns the associated subquery handler if present.
     */
    public SubqueryHandler<T> getSubqueryHandler() {
        return subqueryHandler;
    }

    /**
     * Returns the values used in the comparison.
     *
     * @return Returns the values used in the comparison.
     */
    @Nonnull
    public Object[] getValues() {
        if (values == null)
            return new Object[0];
        return values;
    }

    /**
     * Returns the values filtered to only returns relevant values set the comparison.
     *
     * @return Returns the values used in the comparison.
     */
    @Nonnull
    public List<Object> getValuesFiltered() {
        if (values == null)
            return new ArrayList<>();
        return Stream.of(values)
                // 1. Filter out structural column fields
                .filter(object -> !(object instanceof Column))
                // 2. Flatten collections, subqueries, and primitives into a uniform stream
                .flatMap(object -> {
                    if (object instanceof ParameterSupplier ) {
                        ParameterSupplier supplier = (ParameterSupplier) object;
                        return supplier.getRawParameters().stream();
                    }
                    if (object instanceof QueryBuilder) {
                        QueryBuilder qb = (QueryBuilder) object;
                        return qb.getValues().values().stream();
                    }
                    if (object instanceof LiteralVal) {
                        LiteralVal literalVal = (LiteralVal) object;
                        return Stream.of(literalVal.value());
                    }

                    if (object instanceof Collection<?>) {
                        Collection<?> col = Collections.singleton(object);
                        return col.stream();
                    }

                    if (object instanceof Object[] ) {
                        Object[] arr = (Object[]) object;
                        return Arrays.stream(arr);
                    }

                    return Stream.of(object);
                })
                .filter(object -> object != null && !object.equals(""))
                .collect(Collectors.toList());
    }

    /**
     * Returns the SQL comparison symbol.
     *
     * @return Returns the SQL comparison symbol.
     */
    public String getSymbol() {
        return operator.getSymbol();
    }

    /**
     * Returns the SQL comparison symbol.
     *
     * @return Returns the SQL comparison enum.
     */
    public LogicalComparison getComparison() {
        return operator;
    }

    /**
     * Returns the {@link ConditionBuilder} linked to this comparison.
     *
     * @return Returns the {@link ConditionBuilder} linked to this comparison.
     */
    public ConditionBuilder<T> getCondition() {
        return condition;
    }

    /**
     * Returns the column name for this comparison.
     *
     * @return Returns the column name for this comparison.
     */
    @Nonnull
    public String getColumnName() {
        return columnName;
    }

    /**
     * Returns the logical operator for chaining further conditions.
     *
     * @return Returns the logical operator for chaining further conditions.
     */
    public ConditionChainer<T> getLogicalOperator() {
        return conditionChainer;
    }

    /**
     * Returns the SQL symbol as a string.
     *
     * @return Returns the SQL symbol as a string.
     */
    @Override
    public String toString() {
        return operator.getSymbol();
    }

}
