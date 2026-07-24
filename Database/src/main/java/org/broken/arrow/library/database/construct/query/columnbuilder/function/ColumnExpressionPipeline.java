package org.broken.arrow.library.database.construct.query.columnbuilder.function;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.construct.query.utlity.MathOperation;

import javax.annotation.Nonnull;

/**
 * A stateful implementation of {@link ColumnExpressionBuilder} that constructs SQL column
 * expressions using a linear pipeline approach.
 * <p>
 * This builder mutates its own state and returns itself ({@code this}) for fluent method chaining.
 * When {@link #build()} is invoked, the SQL string is assembled strictly from the inside out
 * in the following order:
 * <ol>
 *     <li><b>Base Column:</b> Retrieves the raw column name.</li>
 *     <li><b>Function:</b> Wraps the column in an aggregate or mathematical function (e.g., {@code AVG(col)}).</li>
 *     <li><b>Math:</b> Appends a mathematical operation (e.g., {@code AVG(col) / MIN(other_col)}).</li>
 *     <li><b>Round:</b> Wraps the entire preceding expression in a rounding function (e.g., {@code ROUND(..., precision)}).</li>
 *     <li><b>Alias:</b> Appends the SQL alias at the very end (e.g., {@code ... AS alias}).</li>
 * </ol>
 * </p>
 * <p>
 * <i>Note: This class is not thread-safe and is intended to be used as a short-lived
 * builder within a single query generation phase.</i>
 * </p>
 */
public class ColumnExpressionPipeline implements ColumnExpressionBuilder {
    private final Column column;
    private SqlFunction function;
    private SqlMath math;
    private SqlAlias aliasContext;
    private SqlRound round;

    /**
     * Creates a new expression pipeline for the specified base column.
     *
     * @param column the base {@link Column} to which functions and operations will be applied
     */
    public ColumnExpressionPipeline(final Column column) {
        this.column = column;
    }

    @Override
    public ColumnExpressionPipeline math(@Nonnull final MathOperation op, @Nonnull final Column right) {
        math = new SqlMath(op, right);
        return this;
    }

    @Override
    public ColumnExpressionPipeline multiply(@Nonnull final Column right) {
        return math(MathOperation.MULTIPLY, right);
    }

    @Override
    public ColumnExpressionPipeline add(@Nonnull final Column right) {
        return math(MathOperation.ADD, right);
    }

    @Override
    public ColumnExpressionPipeline avg() {
        return function(CalcFunc.AVG);
    }

    @Override
    public ColumnExpressionPipeline min() {
        return function(CalcFunc.MIN);
    }

    @Override
    public ColumnExpressionPipeline function(@Nonnull CalcFunc func) {
        function = new SqlFunction(func, this.column);
        return this;
    }

    @Override
    public ColumnExpressionPipeline as(@Nonnull String alias) {
        this.aliasContext = new SqlAlias(alias);
        return this;
    }

    @Override
    public ColumnExpressionPipeline round(int precision) {
        round = new SqlRound(precision);
        return this;
    }

    @Override
    public boolean hasAggregate() {
        return this.function != null;
    }

    @Nonnull
    @Override
    public String build() {
        String sql = this.column.getColumnName();

        if (this.function != null) {
            sql = this.function.build(sql);
        }
        if (this.math != null) {
            sql = this.math.build(sql);
        }
        if (this.round != null) {
            sql = this.round.build(sql);
        }
        if (this.aliasContext != null) {
            sql += this.aliasContext.build("");
        }

        return sql;
    }

    @Override
    public String toString() {
        return build();
    }
}
