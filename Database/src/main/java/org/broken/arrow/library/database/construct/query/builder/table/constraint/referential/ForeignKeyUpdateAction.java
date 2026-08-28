package org.broken.arrow.library.database.construct.query.builder.table.constraint.referential;

/**
 * Represents standard SQL referential actions used with {@code ON UPDATE} foreign key constraints.
 * <p>
 * These actions define how the database engine should handle rows in a child table
 * when the referenced primary key in the parent table is modified.
 * </p>
 */
public enum ForeignKeyUpdateAction {

    /**
     * Sets the foreign key column(s) in the child table to their default values.
     */
    SET_DEFAULT("SET DEFAULT"),

    /**
     * Sets the foreign key column(s) in the child table to {@code NULL}.
     */
    SET_NULL("SET NULL"),

    /**
     * Automatically updates the matching rows in the child table to the new parent key value.
     */
    CASCADE("CASCADE"),

    /**
     * Rejects the update operation in the parent table if dependent rows exist in the child table.
     */
    RESTRICT("RESTRICT"),

    /**
     * Rejects the update operation. Depending on the database engine, this may defer
     * validation until the end of the transaction.
     */
    NO_ACTION("NO ACTION");

    private final String action;

    /**
     * Constructs the referential action with its raw SQL string equivalent.
     *
     * @param action the raw SQL expression string
     */
    ForeignKeyUpdateAction(final String action) {
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
