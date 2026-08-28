package org.broken.arrow.library.database.construct.query.builder.table.constraint.referential;

/**
 * Represents standard SQL referential actions used with {@code ON DELETE} foreign key constraints.
 * <p>
 * These actions define what happens to a row in a child table when the referenced
 * row in the parent table is deleted.
 * </p>
 */
public enum ForeignKeyDeleteAction {

    /**
     * Automatically deletes the matching rows in the child table.
     */
    CASCADE("CASCADE"),

    /**
     * Sets the foreign key column(s) in the child table to {@code NULL}.
     */
    SET_NULL("SET NULL"),

    /**
     * Sets the foreign key column(s) to their default values.
     */
    SET_DEFAULT("SET DEFAULT"),

    /**
     * Rejects the delete operation in the parent table if dependent rows exist.
     */
    RESTRICT("RESTRICT"),

    /**
     * Rejects the delete operation if dependent rows exist. Depending on the database
     * engine (like PostgreSQL), validation can be deferred until the end of the transaction.
     */
    NO_ACTION("NO ACTION");

    private final String action;

    ForeignKeyDeleteAction(String action) {
        this.action = action;
    }

    /**
     * Retrieves the raw string associated with this referential action.
     *
     * @return the raw action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Retrieves the raw string associated with this referential action.
     *
     * @return the raw action string
     */
    @Override
    public String toString() {
        return action;
    }
}