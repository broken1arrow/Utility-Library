package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

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

    private String dropPrimaryKey;
    private String addPrimaryKey;
    private String addUnique;

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
    public void addUnique(final String...  columns) {
        this.addUnique = "ADD UNIQUE (" + String.join(", ", columns) +")";
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
}
