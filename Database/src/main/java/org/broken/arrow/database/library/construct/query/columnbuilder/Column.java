package org.broken.arrow.database.library.construct.query.columnbuilder;

import org.broken.arrow.database.library.construct.query.utlity.CalcFunc;
import org.broken.arrow.database.library.construct.query.utlity.MathOperation;
import org.broken.arrow.database.library.construct.query.utlity.SqlExpressionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Column {
  private final String columnName;
  private final ColumnManger columnManger;
  private final String alias;
  private final List<CalcFunc> aggregations = new ArrayList<>();
  private String roundNumber;
  private String roundMode;
  private MathOperation operation = MathOperation.PER_ROUND;

  public Column(ColumnManger columnManger, String columnName, String alias) {
    this.columnManger = columnManger;
    this.columnName = columnName;
    this.alias = alias;
  }

  public Separator of(String name) {
    return new Separator( new Column(this.columnManger, name, ""));
  }

  public Separator of(String name, String alias) {
    return new Separator( new Column(this.columnManger, name, alias));
  }


  public String getFinishColumName() {
    if (alias == null || alias.isEmpty())
      return columnName;
    return columnName + " " + SqlExpressionType.AS + " " + alias;
  }

  @Override
  public String toString() {
    StringBuilder sql = new StringBuilder();
    boolean applyPerFunction = operation.isSplit();
    if (!aggregations.isEmpty()) {
      String aggExpression = aggregations.stream()
              .map(agg -> getValue(!applyPerFunction, agg + "(" + getFinishColumName() + ")"))
              .collect(Collectors.joining(" " + operation.getSymbol() + " "));


      sql.append(applyPerFunction || roundNumber == null ? aggExpression : getValue(false, aggExpression));
    } else {
      sql.append(getValue(false, getFinishColumName()));
    }
    return sql.toString();
  }

  private String getValue(boolean applyPerFunction, String base) {
    if (roundNumber != null && !applyPerFunction) {
      return "ROUND(" + base + ", " + roundNumber + (roundMode != null ? ", " + roundMode : "") + ")";
    }
    return base;
  }

  public String getColumnName() {
    return this.columnName;
  }

  public ColumnManger getColumnManger() {
    return columnManger;
  }

  public static class Separator {
    private final Column column;

    public Separator(Column column) {
      this.column = column;
      this.column.columnManger.add(column);
    }

    public Separator withAggregation(CalcFunc aggregation) {
      return this.withAggregations((MathOperation) null, aggregation);
    }

    public Separator withAggregation(MathOperation operation, CalcFunc aggregation) {
      return this.withAggregations(operation, aggregation);
    }

    public Separator withAggregations(CalcFunc... aggregations) {
      return this.withAggregations(null, aggregations);
    }

    public Separator withAggregations(MathOperation operation, CalcFunc... aggregations) {
      Collections.addAll(this.column.aggregations, aggregations);
      if (operation != null)
        this.column.operation = operation;
      return this;
    }

    // Method to apply ROUND function
    public Separator round(Number numberOfDecimalPlaces) {
      return this.round(numberOfDecimalPlaces, null);
    }

    public Separator round(MathOperation operation, Number numberOfDecimalPlaces) {
      return this.round(operation, numberOfDecimalPlaces, null);
    }

    // Method to apply ROUND function with mode (optional)
    public Separator round(Number numberOfDecimalPlaces, String mode) {
      return this.round(null, numberOfDecimalPlaces, mode);
    }

    public Separator round(MathOperation operation, Number numberOfDecimalPlaces, String mode) {
      this.column.roundNumber = numberOfDecimalPlaces + "";
      if (operation != null)
        this.column.operation = operation;
      this.column.roundMode = mode;
      return this;
    }

   public Column next() {
      return this.column;
    }

    public ColumnManger build() {
      return  this.column.columnManger;
    }

  }

}