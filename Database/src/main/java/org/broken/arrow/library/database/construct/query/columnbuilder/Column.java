package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.construct.query.utlity.MathOperation;
import org.broken.arrow.library.database.construct.query.utlity.SqlExpressionType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a database column with optional aliasing and aggregation support.
 * <p>
 * Encapsulates a column name and optional alias, and supports applying aggregation
 * functions (such as COUNT, SUM) through the {@link Aggregation} class.
 * Provides methods to build the final SQL representation of the column including
 * alias and aggregation expressions.
 * </p>
 */
public class Column {
    private final String columnName;
    private final String alias;
    private Aggregation aggregation;

    /**
     * Constructs a Column instance with the specified column name and optional alias.
     *
     * @param columnName the name of the database column
     * @param alias      optional alias for the column, can be null or empty if none
     */
    public Column(String columnName, String alias) {
        this.columnName = columnName;
        this.alias = alias;
    }

    /**
     * Returns the fully qualified column name for SQL queries, including aliasing if present.
     * <p>
     * Example output: "columnName AS alias" if alias is set, otherwise just "columnName".
     * </p>
     *
     * @return the column name optionally suffixed with an alias expression
     */
    public String getFinishColumName() {
        if (alias == null || alias.isEmpty())
            return columnName;
        return columnName + " " + SqlExpressionType.AS + " " + alias;
    }

    /**
     * Internal method that applies aggregation and rounding functions to the base column expression.
     *
     * @param applyPerFunction whether to apply rounding inside aggregation functions
     * @param base             the base SQL expression for the column
     * @return the SQL expression with aggregation and rounding applied as necessary
     */
    private String getValue(boolean applyPerFunction, String base) {
        if (aggregation == null) return base;

        if (aggregation.getRoundNumber() != null && !applyPerFunction) {
            final String roundMode = aggregation.getRoundMode();
            return "ROUND(" + base + ", " + aggregation.getRoundNumber() + (roundMode != null ? ", " + roundMode : "") + ")";
        }
        return base;
    }

    /**
     * Returns the aggregation object associated with this column, if any.
     *
     * @return the aggregation applied to this column, or null if none
     */
    public Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * Returns the raw column name (without alias or aggregation).
     *
     * @return the column name string
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Helper class for chaining column and aggregation operations within a column context.
     */
    public static class Separator {
        private final Column column;
        private final Aggregation aggregation;

        /**
         * Constructs a Separator with the specified aggregation and column,
         * linking the aggregation and column together.
         *
         * @param aggregation the current aggregation context
         * @param column      the column to apply aggregation on
         */
        public Separator(Aggregation aggregation, Column column) {
            this.column = column;
            this.aggregation = aggregation;
            this.column.aggregation = aggregation;
            this.aggregation.finish().add(column);
        }

        /**
         * Starts a new aggregation on the finished aggregation set with the given column name.
         *
         * @param name the column name for the new aggregation
         * @return a new Aggregation instance
         */
        public Aggregation colum(String name) {
            return new Aggregation(aggregation.finish(), name, "");
        }

        /**
         * Starts a new aggregation on the finished aggregation set with the given column name and alias.
         *
         * @param name  the column name for the new aggregation
         * @param alias the alias for the new aggregated column
         * @return a new Aggregation instance
         */
        public Aggregation colum(String name, String alias) {
            return new Aggregation(aggregation.finish(), name, alias);
        }

        /**
         * Returns the current column.
         *
         * @return the column instance
         */
        public Column getColumn() {
            return this.column;
        }

        /**
         * Completes the aggregation and returns the associated {@link ColumnManager}.
         *
         * @return the column manager that contains the finished aggregation set
         */
        public ColumnManager finish() {
            return this.aggregation.finish();
        }

    }

    /**
     * Returns the SQL expression string representing this column,
     * including any aggregation and rounding functions applied.
     *
     * @return the SQL string for this column
     */
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
}