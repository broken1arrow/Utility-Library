package org.broken.arrow.library.database.construct.query.utlity;

public class DataType {

  private final String value;

  public DataType(String value) {
    this.value = value;
  }

  // Integer types
  public static DataType tinyint() {
    return new DataType("TINYINT");
  }

  public static DataType smallint() {
    return new DataType("SMALLINT");
  }

  public static DataType mediumint() {
    return new DataType("MEDIUMINT");
  }

  public static DataType dataInt() {
    return new DataType("INT");
  }

  public static DataType bigint() {
    return new DataType("BIGINT");
  }

  // Floating-point types
  public static DataType dataFloat() {
    return new DataType("FLOAT");
  }

  public static DataType dataDouble() {
    return new DataType("DOUBLE");
  }

  public static DataType decimal(int precision, int scale) {
    return new DataType("DECIMAL(" + precision + ", " + scale + ")");
  }

  public static DataType numeric(int precision, int scale) {
    return new DataType("NUMERIC(" + precision + ", " + scale + ")");
  }

  // String types
  public static DataType dataChar(int length) {
    return new DataType("CHAR(" + length + ")");
  }

  public static DataType varchar(int length) {
    return  new DataType("VARCHAR(" + length + ")");
  }

  public static DataType text() {
    return new DataType("TEXT");
  }

  public static DataType tinytext() {
    return new DataType("TINYTEXT");
  }

  public static DataType mediumtext() {
    return new DataType("MEDIUMTEXT");
  }

  public static DataType longtext() {
    return new DataType("LONGTEXT");
  }

  // Date & Time types
  public static DataType date() {
    return new DataType("DATE");
  }

  public static DataType datetime() {
    return new DataType("DATETIME");
  }

  public static DataType timestamp() {
    return new DataType("TIMESTAMP");
  }

  public static DataType time() {
    return new DataType("TIME");
  }

  public static DataType year() {
    return new DataType("YEAR");
  }

  // Boolean
  public static DataType dataBoolean() {
    return new DataType("BOOLEAN");
  }

  // Binary types
  public static DataType blob() {
    return new DataType("BLOB");
  }

  public static DataType tinyblob() {
    return new DataType("TINYBLOB");
  }

  public static DataType mediumblob() {
    return new DataType("MEDIUMBLOB");
  }

  public static DataType longblob() {
    return new DataType("LONGBLOB");
  }

  public static DataType dataEnum(String... values) {
    return new DataType("ENUM(" + String.join(", ", wrapWithQuotes(values)) + ")");
  }

  public static DataType set(String... values) {
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



