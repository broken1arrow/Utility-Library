package org.broken.arrow.database.library.construct.query.utlity;

public enum CalcFunc {
  COUNT("COUNT"),
  AVG("AVG"),
  SUM("SUM"),
  MIN("MIN"),
  MAX("MAX"),
  ROUND("ROUND");


  private final String type;

  private CalcFunc(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

  public static CalcFunc  getStartsWithType(final String type) {
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