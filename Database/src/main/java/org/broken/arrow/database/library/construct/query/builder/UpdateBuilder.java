package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateBuilder {
  private final Map<String, Object> updateData = new LinkedHashMap<>();
  private final Map<Integer, Object> values = new LinkedHashMap<>();
  private final Selector<ColumnBuilder<Column,Void>> selector = new Selector<>(new ColumnBuilder<>());
  private int columnIndex = 1;

  public UpdateBuilder() {
  }

  public UpdateBuilder set(Column column, Object value) {
    updateData.put(column.getColumnName(), value);
    values.put(columnIndex++, value);
    return this;
  }

  public UpdateBuilder setAll(Map<Column, Object> map) {
    map.forEach(this::set);
    return this;
  }

  public Selector<ColumnBuilder<Column,Void>> getSelector() {
    return selector;
  }

  public Map<String, Object> build() {
    processWhereValues();
    return updateData;
  }

  public Map<Integer, Object> getIndexedValues() {
    return values;
  }

  private void processWhereValues() {
    for (Object value : selector.getWhereBuilder().getValues()) {
      values.put(columnIndex++, value);
    }
  }

}
