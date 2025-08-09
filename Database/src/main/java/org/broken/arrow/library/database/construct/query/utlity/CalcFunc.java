package org.broken.arrow.library.database.construct.query.utlity;
/**
 * Enumeration of common SQL calculation functions used in query building.
 * <p>
 * Each enum constant represents a calculation function such as COUNT, AVG, SUM,
 * MIN, MAX, and ROUND, which can be used to build SQL aggregate expressions.
 * </p>
 */
public enum CalcFunc {
  /**
   * COUNT
   */
  COUNT("COUNT"),
  /**
   * AVG
   */
  AVG("AVG"),
  /**
   * SUM
   */
  SUM("SUM"),
  /**
   * MIN
   */
  MIN("MIN"),
  /**
   * MAX
   */
  MAX("MAX"),
  /**
   * ROUND
   */
  ROUND("ROUND");


  private final String type;

  private CalcFunc(String type) {
    this.type = type;
  }

  /**
   * Returns the string representation of the calculation function,
   * which corresponds to the SQL function name.
   *
   * @return the SQL function name as a string
   */
  @Override
  public String toString() {
    return type;
  }

  /**
   * Attempts to retrieve a {@link CalcFunc} enum constant whose name
   * matches the beginning of the provided string (case-insensitive).
   *
   * @param type the string to match against the CalcFunc names
   * @return the matching CalcFunc if found; otherwise, null
   */
  public static CalcFunc getCalcType(final String type) {
    if (type == null) return null;
    String typeUp = type.toUpperCase();
    for (CalcFunc queryType : values()) {
      if (typeUp.startsWith(queryType.toString())) {
        return queryType;
      }
    }
    return null;
  }

}