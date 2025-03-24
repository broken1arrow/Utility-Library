package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

public class SQLConstraints {
  private final String value;

  public SQLConstraints(String value) {
    this.value = value;
  }

  public static SQLConstraints NULL() {
    return new SQLConstraints("NULL");
  }

  public static SQLConstraints NOT_NULL() {
    return new SQLConstraints("NOT NULL");
  }

  public static SQLConstraints PRIMARY_KEY() {
    return new SQLConstraints("PRIMARY KEY");
  }

  public static SQLConstraints PRIMARY_KEY(String... columns) {
    return new SQLConstraints("PRIMARY KEY (" + String.join(", ", columns) + ")");
  }

  public static SQLConstraints AUTO_INCREMENT() {
    return new SQLConstraints("AUTO_INCREMENT");
  }

  public static SQLConstraints UNIQUE() {
    return new SQLConstraints("UNIQUE");
  }

  public static SQLConstraints DEFAULT(String value) {
    return new SQLConstraints("DEFAULT " + value);
  }

  public static SQLConstraints DEFAULT(int value) {
    return new SQLConstraints("DEFAULT " + value);
  }

  public static SQLConstraints DEFAULT(boolean value) {
    return new SQLConstraints("DEFAULT " + (value ? "TRUE" : "FALSE"));
  }

  public static SQLConstraints CHECK(String condition) {
    return new SQLConstraints("CHECK(" + condition + ")");
  }

  public static SQLConstraints FOREIGN_KEY(String column, String referenceTable, String referenceColumn) {
    return new SQLConstraints("FOREIGN KEY (" + column + ") REFERENCES " + referenceTable + "(" + referenceColumn + ")");
  }

  public static SQLConstraints ON_DELETE_CASCADE() {
    return new SQLConstraints("ON DELETE CASCADE");
  }

  public static SQLConstraints ON_UPDATE_CASCADE() {
    return new SQLConstraints("ON UPDATE CASCADE");
  }

  public static SQLConstraints ON_DELETE_SET_NULL() {
    return new SQLConstraints("ON DELETE SET NULL");
  }

  public static SQLConstraints ON_UPDATE_SET_NULL() {
    return new SQLConstraints("ON UPDATE SET NULL");
  }

  public static SQLConstraints UNIQUE_KEY(String column) {
    return new SQLConstraints("UNIQUE (" + column + ")");
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}