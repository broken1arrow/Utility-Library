package org.broken.arrow.library.database.construct.query.builder.condition;

import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.SubqueryHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.LiteralVal;
import org.broken.arrow.library.database.construct.query.utlity.LogicalComparison;
import org.broken.arrow.library.database.construct.query.utlity.Marker;
import org.broken.arrow.library.database.construct.query.utlity.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds SQL condition strings for a specific comparison operation.
 * <p>
 * This class works together with {@link ComparisonHandler} to produce SQL fragments
 * such as <code>= ?</code>, <code>IN (?, ?, ?)</code>, or <code>BETWEEN ? AND ?</code>.
 * It also handles markers and direct value substitution when required.
 *
 * @param <T> the type of the parent query or builder this condition belongs to
 */
public class ConditionBuilder<T> {

    private final ComparisonHandler<T> operator;
    private final Marker marker;

    /**
     * Creates a new condition builder bound to the given comparison handler and marker.
     *
     * @param operator the associated comparison handler containing column, values, and symbol
     * @param marker   the placeholder or value marker used in the SQL condition
     */
    public ConditionBuilder(ComparisonHandler<T> operator, Marker marker) {
        this.operator = operator;
        this.marker = marker;
    }

    /**
     * Returns the list of values associated with this condition.
     *
     * @return a list of condition values, or an empty list if none are set
     */
    public List<Object> getValues() {
        return operator.getValues().length > 0 ? Arrays.asList(operator.getValues()) : new ArrayList<>();
    }

    /**
     * Returns the SQL marker (placeholder or value) for this condition.
     *
     * @return the marker symbol, or {@code "?"} if no marker is set
     */
    public String getMarker() {
        return marker != null ? marker.getSymbol() : "?";
    }


    /**
     * Returns the SQL representation of this condition.
     * <p>
     * Depending on the comparison symbol and marker, the result can contain:
     * <ul>
     *     <li>A subquery (e.g., <code>= (SELECT ...)</code>)</li>
     *     <li>An <code>IN</code> or <code>NOT IN</code> list of placeholders</li>
     *     <li>A <code>BETWEEN</code> or <code>NOT BETWEEN</code> clause</li>
     *     <li>A direct value if {@link Marker#USE_VALUE} is set</li>
     *     <li>A generic placeholder (e.g., <code>= ?</code>)</li>
     * </ul>
     *
     * @return the SQL fragment representing the condition
     */
    @Override
    public String toString() {
        SubqueryHandler<T> subqueryHandler = operator.getSubqueryHandler();
        if (subqueryHandler != null) {
            return " " + operator.getSymbol() + " (" + subqueryHandler.getSubquery().build() + ")";
        }
        final Object[] values = operator.getValues();
        if (values != null) {
            final LogicalComparison comparison = operator.getComparison();
            if (comparison == LogicalComparison.IN || comparison == LogicalComparison.NOT_IN) {
                return getInFormatted();
            }
            if (comparison == LogicalComparison.BETWEEN || comparison == LogicalComparison.NOT_BETWEEN) {
                return getBetweenFormatted();
            }
            if (this.marker == Marker.USE_VALUE && values.length >= 1) {
                Object val = values[0];
                return " " + operator.getSymbol() + " " + formatValue(val);
            }
        }
        return " " + operator.getSymbol() + " " + this.getMarker();
    }


    /**
     * Formats a BETWEEN or NOT BETWEEN clause.
     * <p>
     * If the marker is {@link Marker#USE_VALUE}, actual values are inserted.
     * Otherwise, placeholder markers are used.
     *
     * @return the SQL BETWEEN clause fragment
     */
    @Nonnull
    private String getBetweenFormatted() {
        Object firstValue = operator.getValues().length > 0 ? operator.getValues()[0] : "";
        Object secondValue = operator.getValues().length > 1 ? operator.getValues()[1] : "";

        return " " + operator.getSymbol() + " " + formatValue(firstValue) + " AND " + formatValue(secondValue);
    }

    @Nonnull
    private String getInFormatted() {
        Object[] values = operator.getValues();
        final String comparisonSymbol = operator.getSymbol();
        if (values == null || values.length == 0) {
            return " " + comparisonSymbol + " ()";
        }
        String joinedValues = Arrays.stream(values)
                .map(this::formatValue)
                .collect(Collectors.joining(", "));
        return " " + comparisonSymbol + " (" + joinedValues + ")";
    }

    private String formatValue(Object val) {
        if (val instanceof Column) {
            return ((Column) val).getColumnName();
        }
        if (this.marker == Marker.USE_VALUE) {
            if (val instanceof LiteralVal) {
                Object value = ((LiteralVal) val).value();
                return (value instanceof String) ? "'" + value + "'" : String.valueOf(value);
            }
            return (val instanceof String) ? "'" + val + "'" : String.valueOf(val);
        }
        return this.getMarker();
    }

}
