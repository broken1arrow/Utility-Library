package org.broken.arrow.library.database.construct.query.builder.condition;

import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.SubqueryHandler;
import org.broken.arrow.library.database.construct.query.utlity.Marker;
import org.broken.arrow.library.database.construct.query.utlity.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return operator.getValues() != null ? Arrays.asList(operator.getValues()) : new ArrayList<>();
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
     * Formats a BETWEEN or NOT BETWEEN clause.
     * <p>
     * If the marker is {@link Marker#USE_VALUE}, actual values are inserted.
     * Otherwise, placeholder markers are used.
     *
     * @return the SQL BETWEEN clause fragment
     */
    @Nonnull
    private String getBetweenFormatted() {
        if(this.marker == Marker.USE_VALUE) {
            Object firstValue = operator.getValues().length > 0 ? operator.getValues()[0] : "";
            Object secondValue = operator.getValues().length > 1 ? operator.getValues()[1] : "";
            return " " + operator.getSymbol() + " " + firstValue + " AND " + secondValue;
        }
        return " " + operator.getSymbol() + " " + this.getMarker() + " AND " + this.getMarker();
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
        if (operator.getValues() != null) {
            if (operator.getSymbol().equals("IN") || operator.getSymbol().equals("NOT IN")) {
                return " " + operator.getSymbol() + " (" + StringUtil.repeat(this.getMarker(),operator.getValues().length) + ")";
            }
            if (operator.getSymbol().equals("BETWEEN") || operator.getSymbol().equals("NOT BETWEEN")) {
                return getBetweenFormatted();
            }
            if (this.marker == Marker.USE_VALUE && operator.getValues().length >= 1)
                return " " + operator.getSymbol() + " " + operator.getValues()[0];
        }


        return " " + operator.getSymbol() + " " + this.getMarker();
    }
}
