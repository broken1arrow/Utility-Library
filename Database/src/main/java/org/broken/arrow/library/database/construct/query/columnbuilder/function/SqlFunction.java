package org.broken.arrow.library.database.construct.query.columnbuilder.function;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.function.strategy.ColumnStrategy;
import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;

import javax.annotation.Nonnull;

/**
 * Strategy implementation that wraps a SQL expression in a calculation or aggregate
 * function (e.g., {@code AVG(column)}, {@code COUNT(*)}, {@code SUM(price)}).
 */
public class SqlFunction implements ColumnStrategy {
    private final CalcFunc function;
    private final Column column;
    private final String alias;// Optional

    /**
     * Constructs a SQL function strategy for the specified function type and target column.
     *
     * @param calcFunc the function to apply (e.g., {@code AVG}, {@code SUM}, {@code COUNT})
     * @param column   the base column being wrapped (maybe null if delegated by a pipeline context)
     */
    public SqlFunction(@Nonnull final CalcFunc calcFunc, final Column column) {
        this.function = calcFunc;
        this.column = column;
        this.alias = column != null ? column.getAlias() : "";
    }

    /**
     * Wraps the provided SQL context string in the configured SQL function.
     *
     * @param context the current column name or expression string
     * @return the function-wrapped SQL string (e.g., {@code "AVG(test_v)"})
     */
    @Nonnull
    @Override
    public String build(@Nonnull final String context) {
        String sql = function.name() + "(" + context + ")";
        return (alias != null && !alias.isEmpty()) ? sql + " AS " + alias : sql;
    }

    @Override
    public String toString() {
        String target = (column != null) ? column.toString() : "...";
        return build(target);
    }

}