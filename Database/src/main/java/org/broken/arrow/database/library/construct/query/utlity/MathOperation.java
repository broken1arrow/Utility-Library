package org.broken.arrow.database.library.construct.query.utlity;

import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

public enum MathOperation {
  ADD("+"),
  SUBTRACT("-"),
  MULTIPLY("*"),
  DIVIDE("/"),
  MODULUS("%"),
  EXPONENTIATE("^"),
  BITWISE_AND("&"),
  BITWISE_OR("|"),
  SHIFT_LEFT("<<"),
  SHIFT_RIGHT(">>"),

  /**
   * Ensures that each aggregation or function is applied separately rather than grouped under a single operation.
   * This affects both rounding and mathematical/logical operations.
   * <p>
   * - **With ROUND**: Each function gets its own rounding operation instead of being wrapped together.
   * Example: `ROUND(MIN(u.length), 2), ROUND(SUM(u.length), 2)`
   * <p>
   * - **Without ROUND**: Functions are listed separately instead of being combined.
   * Example: `MIN(u.length), SUM(u.length)`
   * <p>
   * This is the default behavior for {@link Column.Separator#withAggregation(CalcFunc)}.
   * If using math operations via {@link Column.Separator#round(MathOperation, Number, String)} or
   * {@link Column.Separator#round(MathOperation, Number)}, you can combine calculations as needed.
   */
  PER_ROUND(",");

  private final String symbol;

  MathOperation(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  public boolean isSplit() {
    return this == PER_ROUND;
  }
}