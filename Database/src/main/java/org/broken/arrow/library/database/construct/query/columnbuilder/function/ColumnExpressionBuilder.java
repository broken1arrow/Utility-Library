package org.broken.arrow.library.database.construct.query.columnbuilder.function;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.construct.query.utlity.MathOperation;

import javax.annotation.Nonnull;

/**
 * A fluent builder for constructing complex SQL column expressions.
 * <p>
 * This interface provides a linear pipeline for applying aggregate functions,
 * mathematical operations, rounding, and aliasing to a base SQL column.
 * Implementations should return the current instance ({@code this}) to allow
 * for method chaining.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * ColumnExpressionBuilder expression = Column.of("math_test").aggregation(ctx -> ctx
 *     .avg()
 *     .multiply(Column.of("test_exam", ColumnExpressionBuilder::min))
 *     .round(2)
 *     .as("calc_result")
 * );
 *
 * // Renders: ROUND((AVG(math_test) * MIN(test_exam)), 2) AS calc_result
 * }</pre>
 */
public interface ColumnExpressionBuilder {

    /**
     * Applies a mathematical operation between this column expression and a right-hand side column.
     *
     * @param op    the mathematical operator to apply (e.g., ADD, DIVIDE)
     * @param right the column or expression to evaluate on the right side of the operator
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder math(@Nonnull final MathOperation op, @Nonnull final Column right);

    /**
     * Multiplies this column expression by another column.
     * <p>
     * This is a convenience method equivalent to calling {@code math(MathOperation.MULTIPLY, right)}.
     * </p>
     *
     * @param right the column to multiply by
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder multiply(@Nonnull final Column right);

    /**
     * Adds another column to this column expression.
     * <p>
     * This is a convenience method equivalent to calling {@code math(MathOperation.ADD, right)}.
     * </p>
     *
     * @param right the column to add
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder add(@Nonnull final Column right);

    /**
     * Wraps this expression inside the specified SQL aggregate or calculation function.
     *
     * @param func the calculation function to apply (e.g., COUNT, SUM)
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder function(@Nonnull final CalcFunc func);

    /**
     * Wraps this expression inside an {@code AVG(...)} aggregate function.
     * <p>
     * This is a convenience method equivalent to calling {@code function(CalcFunc.AVG)}.
     * </p>
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder avg();

    /**
     * Wraps this expression inside a {@code MIN(...)} aggregate function.
     * <p>
     * This is a convenience method equivalent to calling {@code function(CalcFunc.MIN)}.
     * </p>
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder min();

    /**
     * Assigns a SQL alias to this expression (e.g., {@code AS aliasName}).
     *
     * @param alias the column alias name
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder as(@Nonnull final String alias);

    /**
     * Wraps this expression inside a {@code ROUND(..., precision)} function.
     *
     * @param precision the number of decimal places to round the result to
     * @return this builder instance for method chaining
     */
    ColumnExpressionBuilder round(final int precision);

    /**
     * Compiles the applied operations into a valid SQL string.
     *
     * @return the fully constructed SQL string for this column expression
     */
    @Nonnull
    String build();

    /**
     * Checks if this expression contains an aggregate function (e.g., AVG, MIN, MAX).
     * <p>
     * This is highly useful for validating SQL constraints, such as preventing
     * aggregate functions from being unlawfully executed inside a {@code WHERE} clause.
     * </p>
     *
     * @return {@code true} if an aggregate function has been applied; {@code false} otherwise
     */
    boolean hasAggregate();
}
