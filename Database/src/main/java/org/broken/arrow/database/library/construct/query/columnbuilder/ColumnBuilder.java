package org.broken.arrow.database.library.construct.query.columnbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;


public class ColumnBuilder<T extends Column, V> {
    private final List<T> columns = new ArrayList<>();
    private final V clazzType;

    public ColumnBuilder() {
        this(null);
    }

    public ColumnBuilder(V clazzType) {
        this.clazzType = clazzType;
    }

    public V add(T column) {
        columns.add(column);
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

    public List<T> getColumns() {
        return columns;
    }

    public String build() {
        if (columns.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner(", ");

        for (T column : this.getColumns()) {
            joiner.add(column.toString());
        }
        return joiner + "";
    }

}
