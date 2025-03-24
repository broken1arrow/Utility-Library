package org.broken.arrow.database.library.construct.query.utlity;

public class DataType {

  private final String value;

  public DataType(String value) {
    this.value = value;
  }

  // Integer types
  public static DataType TINYINT() {
    return new DataType("TINYINT");
  }

  public static DataType SMALLINT() {
    return new DataType("SMALLINT");
  }

  public static DataType MEDIUMINT() {
    return new DataType("MEDIUMINT");
  }

  public static DataType INT() {
    return new DataType("INT");
  }

  public static DataType BIGINT() {
    return new DataType("BIGINT");
  }

  // Floating-point types
  public static DataType FLOAT() {
    return new DataType("FLOAT");
  }

  public static DataType DOUBLE() {
    return new DataType("DOUBLE");
  }

  public static DataType DECIMAL(int precision, int scale) {
    return new DataType("DECIMAL(" + precision + ", " + scale + ")");
  }

  public static DataType NUMERIC(int precision, int scale) {
    return new DataType("NUMERIC(" + precision + ", " + scale + ")");
  }

  // String types
  public static DataType CHAR(int length) {
    return new DataType("CHAR(" + length + ")");
  }

  public static DataType VARCHAR(int length) {
    return  new DataType("VARCHAR(" + length + ")");
  }

  public static DataType TEXT() {
    return new DataType("TEXT");
  }

  public static DataType TINYTEXT() {
    return new DataType("TINYTEXT");
  }

  public static DataType MEDIUMTEXT() {
    return new DataType("MEDIUMTEXT");
  }

  public static DataType LONGTEXT() {
    return new DataType("LONGTEXT");
  }

  // Date & Time types
  public static DataType DATE() {
    return new DataType("DATE");
  }

  public static DataType DATETIME() {
    return new DataType("DATETIME");
  }

  public static DataType TIMESTAMP() {
    return new DataType("TIMESTAMP");
  }

  public static DataType TIME() {
    return new DataType("TIME");
  }

  public static DataType YEAR() {
    return new DataType("YEAR");
  }

  // Boolean
  public static DataType BOOLEAN() {
    return new DataType("BOOLEAN");
  }

  // Binary types
  public static DataType BLOB() {
    return new DataType("BLOB");
  }

  public static DataType TINYBLOB() {
    return new DataType("TINYBLOB");
  }

  public static DataType MEDIUMBLOB() {
    return new DataType("MEDIUMBLOB");
  }

  public static DataType LONGBLOB() {
    return new DataType("LONGBLOB");
  }

  public static DataType ENUM(String... values) {
    return new DataType("ENUM(" + String.join(", ", wrapWithQuotes(values)) + ")");
  }

  public static DataType SET(String... values) {
    return new DataType("SET(" + String.join(", ", wrapWithQuotes(values)) + ")");
  }

  private static String[] wrapWithQuotes(String... values) {
    String[] quoted = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      quoted[i] = "'" + values[i] + "'";
    }
    return quoted;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}



