package org.broken.arrow.library.database.construct.query.builder.column.function;

import org.broken.arrow.library.database.construct.query.builder.column.function.strategy.ColumnStrategy;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a SQL {@code ROUND(expression, precision)} function wrapper.
 */
public class SqlRound implements ColumnStrategy {
    private final int precision;

    /**
     *
     * @param precision the rounding for the values
     */
    public SqlRound(int precision) {
        this.precision = precision;
    }

    /**
     * Wraps the provided SQL expression inside a SQL {@code ROUND} function.
     *
     * @param context the SQL expression to wrap
     * @return {@code "ROUND(context, precision)"}
     */
    @Nonnull
    @Override
    public String build(@Nonnull final String context) {
        return "ROUND(" + context + ", " + precision + ")";
    }

    @Override
    public String toString() {
        return "ROUND(..., " + ", " + precision + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SqlRound)) return false;
        SqlRound sqlRound = (SqlRound) object;
        return precision == sqlRound.precision;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(precision);
    }
}
