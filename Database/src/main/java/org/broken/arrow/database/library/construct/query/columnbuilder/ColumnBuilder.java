package org.broken.arrow.database.library.construct.query.columnbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ColumnBuilder<T extends Column, V> {
  private final List<String> columns = new ArrayList<>();
  private final V clazzType;

  public ColumnBuilder() {
    this(null);
  }

  public ColumnBuilder(V clazzType) {
    this.clazzType = clazzType;
  }

  public V add(T column) {
    columns.add(column.getFinishColumName());
    return this.clazzType;
  }

  public V addAll(List<T> columnsList) {
    if (columnsList == null || columnsList.isEmpty())
      return this.clazzType;
    columnsList.forEach(this::add);
    return this.clazzType;
  }

  @SafeVarargs
  public final V addAll(T... columns) {
    if (columns != null)
      Arrays.stream(columns).forEach(this::add);
    return this.clazzType;
  }

/*
  public void select(Column column) {
    if(column == null || column.length == 0) return;
    Arrays.stream(column).forEach(columData -> this.columns.add(columData.toString()));
  }
*/

  public List<String> getColumns() {
    return columns;
  }

  public String build() {
    return columns.isEmpty() ? "" : String.join(", ", columns);
  }

}
