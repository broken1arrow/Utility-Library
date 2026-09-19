package org.broken.arrow.library.database.construct.query.builder.table.constraint.referential;

/**
 * Represents the referential action applied to a foreign key for ON UPDATE or ON DELETE events.
 */
public enum ForeignKeyAction {

    /**
     * Sets the foreign key column(s) in the child table to their default values
     * when the parent key is updated or deleted.
     */
    SET_DEFAULT("SET DEFAULT"),

    /**
     * Sets the foreign key column(s) in the child table to {@code NULL}
     * when the parent key is updated or deleted.
     */
    SET_NULL("SET NULL"),

    /**
     * Automatically cascades the operation. For an update, matching rows in the child table
     * are updated to the new parent key. For a delete, matching rows are deleted.
     */
    CASCADE("CASCADE"),

    /**
     * Rejects the update or delete operation in the parent table if dependent rows exist
     * in the child table.
     */
    RESTRICT("RESTRICT"),

    /**
     * Rejects the update or delete operation. Depending on the database engine, this may
     * defer validation until the end of the transaction.
     */
    NO_ACTION("NO ACTION");

    private final String action;

    /**
     * Constructs the referential action with its raw SQL string equivalent.
     *
     * @param action the raw SQL expression string
     */
    ForeignKeyAction(final String action) {
        this.action = action;

    }

    /**
     * Returns the raw SQL string representation of the referential action.
     *
     * @return the SQL action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the raw SQL string representation of the referential action.
     *
     * @return the SQL action string
     */
    @Override
    public String toString() {
        return action;
    }
}
