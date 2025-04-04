package org.broken.arrow.database.library.construct.query.columnbuilder;

import org.broken.arrow.database.library.construct.query.utlity.CalcFunc;
import org.broken.arrow.database.library.construct.query.utlity.MathOperation;
import org.broken.arrow.database.library.construct.query.utlity.SqlExpressionType;

import java.util.List;
import java.util.stream.Collectors;

public class Column {
    private final String columnName;
    private final String alias;
    private Aggregation aggregation;

    public Column(String columnName, String alias) {
        this.columnName = columnName;
        this.alias = alias;
    }

    public String getFinishColumName() {
        if (alias == null || alias.isEmpty())
            return columnName;
        return columnName + " " + SqlExpressionType.AS + " " + alias;
    }

    @Override
    public String toString() {
        final StringBuilder sql = new StringBuilder();
        if (this.aggregation != null) {
            final MathOperation operation = aggregation.getOperation();
            List<CalcFunc> aggregations = aggregation.getAggregations();
            final boolean applyPerFunction = operation.isSplit();
            if (!aggregations.isEmpty()) {
                String aggExpression = aggregations.stream()
                        .map(agg -> getValue(!applyPerFunction, agg + "(" + getFinishColumName() + ")"))
                        .collect(Collectors.joining(" " + operation.getSymbol() + " "));

                sql.append(applyPerFunction || aggregation.getRoundNumber() == null ? aggExpression : getValue(false, aggExpression));
                return sql.toString();
            }
            sql.append(getValue(false, getFinishColumName()));
        } else {
            sql.append(getValue(false, getFinishColumName()));
        }
        return sql.toString();
    }

    private String getValue(boolean applyPerFunction, String base) {
        if (aggregation == null) return base;

        if (aggregation.getRoundNumber() != null && !applyPerFunction) {
            final String roundMode = aggregation.getRoundMode();
            return "ROUND(" + base + ", " + aggregation.getRoundNumber() + (roundMode != null ? ", " + roundMode : "") + ")";
        }
        return base;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public static class Separator {
        private final Column column;
        private final Aggregation aggregation;

        public Separator(Aggregation aggregation, Column column) {
            this.column = column;
            this.aggregation = aggregation;
            this.column.aggregation = aggregation;
            this.aggregation.getColumnManger().add(column);
        }

        public Aggregation colum(String name) {
            return new Aggregation(aggregation.getColumnManger(), name, "");
        }

        public Aggregation colum(String name, String alias) {
            return new Aggregation(aggregation.getColumnManger(), name, alias);
        }

        public Column getColumn() {
            return this.column;
        }

        public ColumnManger build() {
            return this.aggregation.getColumnManger();
        }

    }

}