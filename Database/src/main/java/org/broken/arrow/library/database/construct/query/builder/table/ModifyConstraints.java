package org.broken.arrow.library.database.construct.query.builder.table;

import org.broken.arrow.library.database.construct.query.builder.table.constraint.referential.ForeignKeyConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a set of constraint modification operations used when generating
 * {@code ALTER TABLE} statements.
 * <p>
 * This class acts as a lightweight builder for SQL constraint fragments such as
 * dropping or adding primary keys and unique constraints. Each method sets
 * the corresponding SQL clause, which can later be retrieved and combined
 * by the {@link AlterTable#setConstraints(Consumer)}.
 * <p>
 * Only one operation of each type can be defined at a time. If a method is
 * called multiple times, the previously generated clause will be overwritten.
 *
 * <h3>Example usage</h3>
 * <pre>{@code
 * ModifyConstraints constraints = new ModifyConstraints();
 * constraints.dropPrimaryKey();
 * constraints.addPrimaryKey("id");
 * constraints.addUnique("email");
 *
 * // Resulting fragments:
 * // DROP PRIMARY KEY
 * // ADD PRIMARY KEY (id)
 * // ADD UNIQUE (email)
 * }</pre>
 */
public class ModifyConstraints {
    private final List<String> constraintsActions = new ArrayList<>();
    private final String tableName;
    private String dropPrimaryKey;
    private String addPrimaryKey;
    private String addUnique;

    /**
     * Constructs a new AlterTable.
     *
     * @param tableName the table name for alter the constraints.
     */
    public ModifyConstraints(@Nonnull final String tableName) {
        this.tableName = tableName;
    }

    /**
     * Generates a {@code DROP PRIMARY KEY} clause.
     * <p>
     * This is typically used when replacing an existing primary key
     * with a new definition.
     */
    public void dropPrimaryKey() {
        this.dropPrimaryKey = "DROP PRIMARY KEY";
    }

    /**
     * Generates an {@code ADD PRIMARY KEY} clause using the specified columns.
     *
     * @param columns one or more column names to be included in the primary key
     * @throws IllegalArgumentException if {@code columns} is empty
     */
    public void addPrimaryKey(final String... columns) {
        this.addPrimaryKey = "ADD PRIMARY KEY (" + String.join(", ", columns) + ")";
    }

    /**
     * Generates an {@code ADD UNIQUE} clause using the specified columns.
     *
     * @param columns one or more column names that must be unique
     * @throws IllegalArgumentException if {@code columns} is empty
     */
    public void addUnique(final String... columns) {
        this.addUnique = "ADD UNIQUE (" + String.join(", ", columns) + ")";
    }

    /**
     * Generates an {@code ADD CONSTRAINT ... FOREIGN KEY} clause using an automatically
     * generated constraint name ({@code fk_tableName_columnName}).
     *
     * @param columnName       the column in the current table
     * @param parentTable      the name of the referenced parent table
     * @param parentColumnName the name of the referenced parent column
     * @param callback         the foreign key configuration linking the parent table
     */
    public void addForeignKey(@Nonnull final String columnName, @Nonnull final String parentTable, @Nonnull final String parentColumnName, @Nonnull final Consumer<ForeignKeyConfig> callback) {
        final String constraintName = "fk_" + this.tableName + "_" + columnName;
        this.addForeignKey(constraintName, columnName, parentTable, parentColumnName, callback);
    }

    /**
     * Generates an {@code ADD CONSTRAINT ... FOREIGN KEY} clause.
     *
     * @param constraintName   the named identifier for the constraint, example {@code fk_tableName_columnName}
     * @param columnName       the column in the current table
     * @param parentTable      the name of the referenced parent table
     * @param parentColumnName the name of the referenced parent column
     * @param callback         the foreign key configuration linking the parent table
     */
    public void addForeignKey(@Nonnull final String constraintName, @Nonnull final String columnName, @Nonnull final String parentTable, @Nonnull final String parentColumnName, @Nonnull final Consumer<ForeignKeyConfig> callback) {
        final StringBuilder sql = new StringBuilder();
        final ForeignKeyConfig config = new ForeignKeyConfig(parentTable, parentColumnName);
        callback.accept(config);
        sql.append("FOREIGN KEY (").append(columnName).append(") ")
                .append("REFERENCES ").append(config.getParentTable())
                .append("(").append(config.getParentColumn()).append(")");

        if (config.getDeleteAction() != null) {
            sql.append(" ON DELETE ").append(config.getDeleteAction());
        }
        if (config.getUpdateAction() != null) {
            sql.append(" ON UPDATE ").append(config.getUpdateAction());
        }
        this.addConstraint(constraintName, sql.toString());
    }

    /**
     * Generates a generic {@code ADD CONSTRAINT} clause.
     * <p>
     * This can be used for custom constraints such as {@code CHECK} or named {@code UNIQUE} constraints.
     *
     * @param constraintName the named identifier for the constraint
     * @param definition     the SQL constraint definition (e.g., "{@code CHECK (age >= 18)}", "{@code UNIQUE (email)}"
     *                       or even "{@code FOREIGN KEY(columnName) REFERENCES ParentTable (ParentColumn)}" clause)
     */
    public void addConstraint(@Nonnull final String constraintName, @Nonnull final String definition) {
        this.constraintsActions.add("ADD CONSTRAINT " + constraintName + " " + definition);
    }

    /**
     * Generates a {@code DROP CONSTRAINT} clause for removing a constraint.
     *
     * @param constraintName the named identifier of the constraint to drop for example {@code fk_tableName_columnName}
     */
    public void dropConstraint(String constraintName) {
        this.constraintsActions.add("DROP CONSTRAINT " + constraintName);
    }

    /**
     * Returns the {@code DROP PRIMARY KEY} clause, or {@code null}
     * if no drop operation has been defined.
     *
     * @return the drop primary key SQL fragment
     */
    public String getDropPrimaryKey() {
        return dropPrimaryKey;
    }

    /**
     * Returns the {@code ADD PRIMARY KEY} clause, or {@code null}
     * if no primary key addition has been defined.
     *
     * @return the add primary key SQL fragment
     */
    public String getAddPrimaryKey() {
        return addPrimaryKey;
    }

    /**
     * Returns the {@code ADD UNIQUE} clause, or {@code null}
     * if no unique constraint has been defined.
     *
     * @return the add unique constraint SQL fragment
     */
    public String getAddUnique() {
        return addUnique;
    }

    /**
     * Retrieve the actions to preform when alter the table
     *
     * @return returns a list of constraints to preform.
     */
    public List<String> getConstraintsActions() {
        return constraintsActions;
    }
}
