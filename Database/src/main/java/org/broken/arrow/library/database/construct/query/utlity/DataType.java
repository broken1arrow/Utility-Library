package org.broken.arrow.library.database.construct.query.utlity;

/**
 * Represents SQL data types used for database column definitions.
 * Provides factory methods to create instances for common SQL data types,
 * including integer types, floating-point types, strings, date/time, boolean, binary types,
 * as well as ENUM and SET types with specific values.
 */
public class DataType {

  private final String value;

  /**
   * Constructs a new DataType with the specified SQL type definition.
   *
   * @param value the SQL type as a string (e.g., "INT", "VARCHAR(255)")
   */
  public DataType(String value) {
    this.value = value;
  }


  /**
   * Represents the SQL TINYINT type.
   *
   * @return a DataType instance for TINYINT.
   */
  public static DataType tinyint() {
    return new DataType("TINYINT");
  }

  /**
   * Represents the SQL SMALLINT type.
   *
   * @return a DataType instance for SMALLINT.
   */
  public static DataType smallint() {
    return new DataType("SMALLINT");
  }

  /**
   * Represents the SQL MEDIUMINT type.
   *
   * @return a DataType instance for MEDIUMINT.
   */
  public static DataType mediumint() {
    return new DataType("MEDIUMINT");
  }

  /**
   * Represents the SQL INT (INTEGER) type.
   *
   * @return a DataType instance for INT.
   */
  public static DataType dataInt() {
    return new DataType("INT");
  }

  /**
   * Represents the SQL BIGINT type.
   *
   * @return a DataType instance for BIGINT.
   */
  public static DataType bigint() {
    return new DataType("BIGINT");
  }


  /**
   * Represents the SQL FLOAT type.
   *
   * @return a DataType instance for FLOAT.
   */
  public static DataType dataFloat() {
    return new DataType("FLOAT");
  }

  /**
   * Represents the SQL DOUBLE type.
   *
   * @return a DataType instance for DOUBLE.
   */
  public static DataType dataDouble() {
    return new DataType("DOUBLE");
  }

  /**
   * Represents the SQL DECIMAL type with specified precision and scale.
   *
   * @param precision total number of digits.
   * @param scale number of digits after the decimal point.
   * @return a DataType instance for DECIMAL(precision, scale).
   */
  public static DataType decimal(int precision, int scale) {
    return new DataType("DECIMAL(" + precision + ", " + scale + ")");
  }

  /**
   * Represents the SQL NUMERIC type with specified precision and scale.
   *
   * @param precision total number of digits.
   * @param scale number of digits after the decimal point.
   * @return a DataType instance for NUMERIC(precision, scale).
   */
  public static DataType numeric(int precision, int scale) {
    return new DataType("NUMERIC(" + precision + ", " + scale + ")");
  }


  /**
   * Represents the SQL CHAR type with fixed length.
   *
   * @param length the fixed length of the CHAR type.
   * @return a DataType instance for CHAR(length).
   */
  public static DataType dataChar(int length) {
    return new DataType("CHAR(" + length + ")");
  }

  /**
   * Represents the SQL VARCHAR type with variable length.
   *
   * @param length the maximum length of the VARCHAR type.
   * @return a DataType instance for VARCHAR(length).
   */
  public static DataType varchar(int length) {
    return new DataType("VARCHAR(" + length + ")");
  }

  /**
   * Represents the SQL TEXT type.
   *
   * @return a DataType instance for TEXT.
   */
  public static DataType text() {
    return new DataType("TEXT");
  }

  /**
   * Represents the SQL TINYTEXT type.
   *
   * @return a DataType instance for TINYTEXT.
   */
  public static DataType tinytext() {
    return new DataType("TINYTEXT");
  }

  /**
   * Represents the SQL MEDIUMTEXT type.
   *
   * @return a DataType instance for MEDIUMTEXT.
   */
  public static DataType mediumtext() {
    return new DataType("MEDIUMTEXT");
  }

  /**
   * Represents the SQL LONGTEXT type.
   *
   * @return a DataType instance for LONGTEXT.
   */
  public static DataType longtext() {
    return new DataType("LONGTEXT");
  }


  /**
   * Represents the SQL DATE type.
   *
   * @return a DataType instance for DATE.
   */
  public static DataType date() {
    return new DataType("DATE");
  }

  /**
   * Represents the SQL DATETIME type.
   *
   * @return a DataType instance for DATETIME.
   */
  public static DataType datetime() {
    return new DataType("DATETIME");
  }

  /**
   * Represents the SQL TIMESTAMP type.
   *
   * @return a DataType instance for TIMESTAMP.
   */
  public static DataType timestamp() {
    return new DataType("TIMESTAMP");
  }

  /**
   * Represents the SQL TIME type.
   *
   * @return a DataType instance for TIME.
   */
  public static DataType time() {
    return new DataType("TIME");
  }

  /**
   * Represents the SQL YEAR type.
   *
   * @return a DataType instance for YEAR.
   */
  public static DataType year() {
    return new DataType("YEAR");
  }

  // Boolean type

  /**
   * Represents the SQL BOOLEAN type.
   *
   * @return a DataType instance for BOOLEAN.
   */
  public static DataType dataBoolean() {
    return new DataType("BOOLEAN");
  }

  // Binary types

  /**
   * Represents the SQL BLOB type.
   *
   * @return a DataType instance for BLOB.
   */
  public static DataType blob() {
    return new DataType("BLOB");
  }

  /**
   * Represents the SQL TINYBLOB type.
   *
   * @return a DataType instance for TINYBLOB.
   */
  public static DataType tinyblob() {
    return new DataType("TINYBLOB");
  }

  /**
   * Represents the SQL MEDIUMBLOB type.
   *
   * @return a DataType instance for MEDIUMBLOB.
   */
  public static DataType mediumblob() {
    return new DataType("MEDIUMBLOB");
  }

  /**
   * Represents the SQL LONGBLOB type.
   *
   * @return a DataType instance for LONGBLOB.
   */
  public static DataType longblob() {
    return new DataType("LONGBLOB");
  }

  // Enum and Set types

  /**
   * Represents the SQL ENUM type with a list of possible string values.
   *
   * @param values allowed string values for the ENUM.
   * @return a DataType instance for ENUM with the specified values.
   */
  public static DataType dataEnum(String... values) {
    return new DataType("ENUM(" + String.join(", ", wrapWithQuotes(values)) + ")");
  }

  /**
   * Represents the SQL SET type with a list of possible string values.
   *
   * @param values allowed string values for the SET.
   * @return a DataType instance for SET with the specified values.
   */
  public static DataType set(String... values) {
    return new DataType("SET(" + String.join(", ", wrapWithQuotes(values)) + ")");
  }

  /**
   * Wraps the given strings with single quotes for SQL syntax.
   *
   * @param values the string values to quote.
   * @return an array of quoted strings.
   */
  private static String[] wrapWithQuotes(String... values) {
    String[] quoted = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      quoted[i] = "'" + values[i] + "'";
    }
    return quoted;
  }

  /**
   * Returns the SQL data type representation as a string.
   *
   * @return the SQL type string.
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the SQL data type string representation.
   *
   * @return the SQL type string.
   */
  @Override
  public String toString() {
    return value;
  }
}


