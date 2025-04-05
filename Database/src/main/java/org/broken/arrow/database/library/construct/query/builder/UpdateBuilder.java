package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateBuilder {
    private final Map<String, Object> updateData = new LinkedHashMap<>();
    private final Map<Integer, Object> values = new LinkedHashMap<>();
    private final Selector<ColumnBuilder<Column, Void>,Column> selector = new Selector<>(new ColumnBuilder<>());
    private int columnIndex = 1;

    public UpdateBuilder() {
    }

    public UpdateBuilder put(final String column, final Object value) {
        final ColumnManger columnManger = new ColumnManger();

        return this.put(new Column(column,""), value);
    }

    public UpdateBuilder put(final Column column, final Object value) {
        updateData.put(column.getColumnName(), value);
        values.put(columnIndex++, value);
        return this;
    }

    public UpdateBuilder putAll(Map<Column, Object> map) {
        map.forEach(this::put);
        return this;
    }

    public Selector<ColumnBuilder<Column, Void>,Column> getSelector() {
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
        selector.getWhereBuilder().getValues().entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(integerObjectEntry -> values.put(columnIndex++, integerObjectEntry.getValue())
                );
    }

}
