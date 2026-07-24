package org.broken.arrow.library.database.construct.query.columnbuilder.function;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.MathOperation;

import javax.annotation.Nonnull;

/**
 * Represents a binary mathematical operation combining the current SQL expression
 * with a right-hand side {@link Column}.
 */
public class SqlMath implements ColumnStrategy {
    private final MathOperation operator;
    private final Column right;

    /**
     * Constructs a binary mathematical operation.
     *
     * @param operator the mathematical or bitwise operator (e.g., ADD, MULTIPLY)
     * @param right    the right-hand side column or aggregated column
     */
    public SqlMath(MathOperation operator, Column right) {
        this.operator = operator;
        this.right = right;
    }

    /**
     * Combines the incoming SQL expression with the right-hand column using the operator.
     *
     * @param context the left-hand SQL expression string
     * @return {@code "(context OPERATOR right)"}
     */
    @Nonnull
    @Override
    public String build(@Nonnull final String context) {
        return "(" + context + " " + operator.getSymbol() + " " + right.toString() + ")";
    }

    @Override
    public String toString() {
        return "(... " + operator.getSymbol() + " " + right.toString() + ")";
    }
}