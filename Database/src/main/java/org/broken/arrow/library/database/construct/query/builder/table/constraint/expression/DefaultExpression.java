package org.broken.arrow.library.database.construct.query.builder.table.constraint.expression;

import javax.annotation.Nonnull;

/**
 * Represents standard SQL expressions, functions, and keywords commonly used
 * as default values or action constraints (e.g., DEFAULT, ON UPDATE).
 */
public enum DefaultExpression {

    /**
     * Represents the SQL standard {@code CURRENT_TIMESTAMP} function.
     * Commonly used for tracking creation or update times.
     */
    CURRENT_TIMESTAMP("CURRENT_TIMESTAMP"),
    /**
     * Represents the SQL standard {@code CURRENT_DATE} function.
     */
    CURRENT_DATE("CURRENT_DATE"),
    /**
     * Represents the SQL standard {@code CURRENT_TIME} function.
     */
    CURRENT_TIME("CURRENT_TIME"),
    /**
     * Represents the {@code NOW()} function.
     * Note: This is dialect-specific and primarily used in MySQL/PostgreSQL.
     */
    NOW("NOW()"),

    /**
     * Represents the SQL {@code NULL} keyword.
     */
    NULL("NULL"),

    /**
     * Represents the SQL boolean {@code TRUE} keyword.
     */
    TRUE("TRUE"),
    /**
     * Represents the SQL boolean {@code FALSE} keyword.
     */
    FALSE("FALSE"),

    /**
     * Represents a numeric default of {@code 0}.
     */
    ZERO("0"),
    /**
     * Represents a numeric default of {@code 1}.
     */
    ONE("1");

    private final String expression;

    /**
     * Constructs the default expression with its raw string equivalent.
     *
     * @param expression the raw SQL expression string
     */
    DefaultExpression(@Nonnull final String expression) {
        this.expression = expression;
    }

    /**
     * Returns the SQL string representation of the expression.
     *
     * @return the raw SQL string
     */
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }
}