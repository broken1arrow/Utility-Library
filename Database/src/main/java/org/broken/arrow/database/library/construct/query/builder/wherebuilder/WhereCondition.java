package org.broken.arrow.database.library.construct.query.builder.wherebuilder;

import org.broken.arrow.database.library.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.database.library.construct.query.builder.comparison.SubqueryHandler;
import org.broken.arrow.database.library.construct.query.utlity.Marker;
import org.broken.arrow.database.library.construct.query.utlity.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhereCondition<T> {

    private final ComparisonHandler<T> operator;
    private final Marker marker;

    public WhereCondition(ComparisonHandler<T> operator, Marker marker) {
        this.operator = operator;
        this.marker = marker;
    }


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
                return " " + operator.getSymbol() + " " + this.getMarker() + " AND " + this.getMarker();
            }
            if (this.marker == Marker.USE_VALUE && operator.getValues().length >= 1)
                return " " + operator.getSymbol() + " " + operator.getValues()[0];
        }
        return " " + operator.getSymbol() + " " + this.getMarker();
    }

    public List<Object> getValues() {
        return operator.getValues() != null ? Arrays.asList(operator.getValues()) : new ArrayList<>();
    }

    public String getMarker() {
        return marker != null ? marker.getSymbol() : "?";
    }

}
