package org.broken.arrow.library.database.construct.query.builder.table.constraint.referential;

import javax.annotation.Nonnull;

/**
 * Configuration builder for defining SQL {@code FOREIGN KEY} constraints on a column.
 * <p>
 * This class encapsulates the referenced parent table and column, as well as
 * the referential actions to execute when the parent record is deleted ({@code ON DELETE})
 * or updated ({@code ON UPDATE}). It utilizes a fluent API for easy setup.
 * </p>
 */
public class ForeignKeyConfig {
    private final String parentTable;
    private final String parentColumn;
    private ForeignKeyDeleteAction foreignKeyDELETEAction;
    private ForeignKeyUpdateAction updateAction;

    /**
     * Constructs a new foreign key configuration linking to a parent table.
     *
     * @param parentTable  the name of the referenced parent table
     * @param parentColumn the name of the referenced column in the parent table
     */
    public ForeignKeyConfig(String parentTable, String parentColumn) {
        this.parentTable = parentTable;
        this.parentColumn = parentColumn;
    }

    /**
     * Sets the action to perform when the referenced parent row is deleted.
     *
     * @param removeAction the {@link ForeignKeyDeleteAction} to apply (must not be null)
     * @return this configuration instance for method chaining
     */
    public ForeignKeyConfig setRemoveAction(@Nonnull final ForeignKeyDeleteAction removeAction) {
        this.foreignKeyDELETEAction = removeAction;
        return this;
    }

    /**
     * Sets the action to perform when the referenced parent row is updated.
     *
     * @param updateAction the {@link ForeignKeyUpdateAction} to apply (must not be null)
     * @return this configuration instance for method chaining
     */
    public ForeignKeyConfig setUpdateAction(@Nonnull final ForeignKeyUpdateAction updateAction) {
        this.updateAction = updateAction;
        return this;
    }

    /**
     * Gets the action to perform when the referenced parent row is deleted.
     *
     * @return the {@link ForeignKeyDeleteAction}, or {@code null} if not set
     */
    public ForeignKeyDeleteAction getRemoveAction() {
        return foreignKeyDELETEAction;
    }

    /**
     * Gets the action to perform when the referenced parent row is updated.
     *
     * @return the {@link ForeignKeyUpdateAction}, or {@code null} if not set
     */
    public ForeignKeyUpdateAction getUpdateAction() {
        return updateAction;
    }

    /**
     * Gets the name of the referenced parent table.
     *
     * @return the parent table name
     */
    public String getParentTable() {
        return parentTable;
    }

    /**
     * Gets the name of the referenced parent column.
     *
     * @return the parent column name
     */
    public String getParentColumn() {
        return parentColumn;
    }
}
