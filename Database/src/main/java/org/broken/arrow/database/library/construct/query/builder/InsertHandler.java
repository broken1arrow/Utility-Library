package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertHandler {

    private final Map<Integer, InsertBuilder> insertValues = new HashMap<>();
    private int columnIndex = 1;

    public void add(InsertBuilder value) {
        insertValues.put(columnIndex++, value);
    }

    public void addAll(InsertBuilder... values) {
        for (InsertBuilder insert : values) {
            this.add(insert);
        }
    }

    public void addAll(Map<Column, Object> columnData) {
        for (Map.Entry<Column, Object> insert : columnData.entrySet()) {
            this.add(new InsertBuilder(insert.getKey().getColumnName(), insert.getValue()));
        }
    }

    public Map<Integer, InsertBuilder> getInsertValues() {
        return insertValues;
    }

    public Map<Integer, Object> getIndexedValues() {
        return insertValues.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getColumnValue()
                ));
    }

}
