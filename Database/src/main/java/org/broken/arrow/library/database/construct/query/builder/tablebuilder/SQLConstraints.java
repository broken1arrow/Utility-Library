package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import javax.annotation.Nonnull;
/**
 * Represents common SQL constraints and provides factory methods for creating them.
 * <p>
 * This class is designed to simplify the construction of SQL column or table constraints
 * by providing static factory methods for frequently used constraint types such as
 * {@code PRIMARY KEY}, {@code NOT NULL}, {@code UNIQUE}, and {@code FOREIGN KEY}.
 * </p>
 * <p>
 * Constraints are stored as raw SQL fragments and can be used when building
 * SQL table creation statements or altering table definitions.
 * </p>
 *
 * <strong>Equality and Hashing:</strong>
 * Two {@link SQLConstraints} instances are considered equal if their SQL string values match exactly.
 *
 * @see #primaryKey()
 * @see #notNull()
 * @see #unique()
 * @see #foreignKey(String, String, String)
 */
public class SQLConstraints {
  /** SQL keyword for default value constraints. */
  public static final String DEFAULT = "DEFAULT";
  /** The raw SQL value representing the constraint. */
  private final String value;

  /**
   * Creates a new SQL constraint with the given SQL fragment.
   *
   * @param value the SQL constraint string (must not be {@code null})
   */
  public SQLConstraints(@Nonnull final String value) {
    this.value = value;
  }

  /**
   * Returns a {@code NULL} constraint.
   *
   * @return a nullable constraint
   */
  public static SQLConstraints nullable() {
    return new SQLConstraints("NULL");
  }

  /**
   * Returns a {@code NOT NULL} constraint.
   *
   * @return a not-null constraint
   */
  public static SQLConstraints notNull() {
    return new SQLConstraints("NOT NULL");
  }

  /**
   * Returns a {@code PRIMARY KEY} constraint.
   *
   * @return a primary key constraint
   */
  public static SQLConstraints primaryKey() {
    return new SQLConstraints("PRIMARY KEY");
  }

  /**
   * Returns a composite {@code PRIMARY KEY} constraint.
   *
   * @param columns the columns to include in the primary key
   * @return a primary key constraint for multiple columns
   */
  public static SQLConstraints primaryKeys(String... columns) {
    return new SQLConstraints("PRIMARY KEY (" + String.join(", ", columns) + ")");

  }
  /**
   * Returns an {@code AUTO_INCREMENT} constraint.
   *
   * @return an auto-increment constraint
   */
  public static SQLConstraints autoIncrement() {
    return new SQLConstraints("AUTO_INCREMENT");
  }

  /**
   * Returns a {@code UNIQUE} constraint.
   *
   * @return a unique constraint
   */
  public static SQLConstraints unique() {
    return new SQLConstraints("UNIQUE");
  }

  /**
   * Returns a {@code DEFAULT} constraint with the specified string value.
   *
   * @param value the default value as a string
   * @return a default value constraint
   */
  public static SQLConstraints defaultVal(String value) {
    return new SQLConstraints(DEFAULT + " " + value);
  }

  /**
   * Returns a {@code DEFAULT} constraint with the specified integer value.
   *
   * @param value the default integer value
   * @return a default value constraint
   */
  public static SQLConstraints defaultVal(int value) {
    return new SQLConstraints(DEFAULT + " " + value);
  }

  /**
   * Returns a {@code DEFAULT} constraint with the specified boolean value.
   *
   * @param value the default boolean value
   * @return a default value constraint
   */
  public static SQLConstraints defaultVal(boolean value) {
    return new SQLConstraints(DEFAULT + " " + (value ? "TRUE" : "FALSE"));
  }

  /**
   * Returns a {@code CHECK} constraint with the specified condition.
   *
   * @param condition the check condition
   * @return a check constraint
   */
  public static SQLConstraints check(String condition) {
    return new SQLConstraints("CHECK(" + condition + ")");
  }
  /**
   * Returns a {@code FOREIGN KEY} constraint referencing another table.
   *
   * @param column          the column in the current table
   * @param referenceTable  the referenced table
   * @param referenceColumn the referenced column
   * @return a foreign key constraint
   */
  public static SQLConstraints foreignKey(String column, String referenceTable, String referenceColumn) {
    return new SQLConstraints("FOREIGN KEY (" + column + ") REFERENCES " + referenceTable + "(" + referenceColumn + ")");
  }

  /**
   * Returns an {@code ON DELETE CASCADE} action constraint.
   *
   * @return an on-delete cascade constraint
   */
  public static SQLConstraints onDeleteCascade() {
    return new SQLConstraints("ON DELETE CASCADE");
  }

  /**
   * Returns an {@code ON UPDATE CASCADE} action constraint.
   *
   * @return an on-update cascade constraint
   */
  public static SQLConstraints onUpdateCascade() {
    return new SQLConstraints("ON UPDATE CASCADE");
  }

  /**
   * Returns an {@code ON DELETE SET NULL} action constraint.
   *
   * @return an on-delete set null constraint
   */
  public static SQLConstraints onDeleteSetNulL() {
    return new SQLConstraints("ON DELETE SET NULL");
  }

  /**
   * Returns an {@code ON UPDATE SET NULL} action constraint.
   *
   * @return an on-update set null constraint
   */
  public static SQLConstraints onUpdateSetNull() {
    return new SQLConstraints("ON UPDATE SET NULL");
  }

  /**
   * Returns a {@code UNIQUE} constraint for a specific column.
   *
   * @param column the column name
   * @return a unique key constraint
   */
  public static SQLConstraints uniqueKey(String column) {
    return new SQLConstraints("UNIQUE (" + column + ")");
  }

  /**
   * Gets the raw SQL value for this constraint.
   *
   * @return the SQL constraint string
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the SQL string representation of the constraint.
   *
   * @return the SQL constraint string
   */
  @Override
  public String toString() {
    return value;
  }

  /**
   * Checks if this constraint is a {@code PRIMARY KEY}.
   *
   * @param o the object to check
   * @return {@code true} if the given object is a primary key constraint, otherwise {@code false}
   */
  public static boolean isPrimary(Object o) {
    if (o == null || SQLConstraints.class != o.getClass()) return false;

    SQLConstraints that = (SQLConstraints) o;
    return that.value.startsWith("PRIMARY KEY");

  }

  /**
   * Compares this constraint to another for equality based on their SQL string values.
   *
   * @param o the object to compare
   * @return {@code true} if both constraints have the same SQL string value
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SQLConstraints that = (SQLConstraints) o;
    return value.equals(that.value);
  }

  /**
   * Returns a hash code based on the SQL string value.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return value.hashCode();
  }
}