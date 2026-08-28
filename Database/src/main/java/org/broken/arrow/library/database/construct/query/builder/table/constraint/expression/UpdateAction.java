package org.broken.arrow.library.database.construct.query.builder.table.constraint.expression;

/**
 * Represents standard SQL expressions and functions commonly used
 * as column-level constraints for {@code ON UPDATE} triggers.
 * <p>
 * This is primarily used for auto-updating timestamp columns when a row is modified.
 * Note: These are distinct from foreign key referential actions, and are inserted
 * directly into the SQL statement.
 * </p>
 */
public enum UpdateAction {

    /**
     * Represents the SQL standard {@code CURRENT_TIMESTAMP} function.
     * Commonly used for automatically recording the time a row was last modified.
     */
    CURRENT_TIMESTAMP("CURRENT_TIMESTAMP"),

    /**
     * Represents the SQL standard {@code LOCALTIME} function.
     */
    LOCAL_TIME("LOCALTIME"),

    /**
     * Represents the SQL standard {@code LOCALTIMESTAMP} function.
     */
    LOCAL_TIMESTAMP("LOCALTIMESTAMP"),

    /**
     * Represents the {@code NOW()} function.
     * <p>
     * Note: This is dialect-specific and primarily used in MySQL and PostgreSQL.
     * </p>
     */
    NOW("NOW()");
    private final String action;

    /**
     * Constructs the update action with its raw string equivalent.
     *
     * @param action the raw SQL expression string
     */
    UpdateAction(final String action) {
        this.action = action;

    }

    /**
     * Returns the raw SQL string representation of the function or keyword.
     *
     * @return the raw SQL string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the raw SQL string representation of the function or keyword.
     *
     * @return the raw SQL string
     */
    @Override
    public String toString() {
        return action;
    }
}
