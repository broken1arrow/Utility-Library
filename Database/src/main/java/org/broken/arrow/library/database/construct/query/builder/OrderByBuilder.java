package org.broken.arrow.library.database.construct.query.builder;

import java.util.ArrayList;
import java.util.List;

public class OrderByBuilder {
    private final List<String> orderByColumns = new ArrayList<>();

    public void add(String column, boolean ascending) {
        orderByColumns.add(column + (ascending ? " ASC" : " DESC"));
    }

    public String build() {
        return orderByColumns.isEmpty() ? "" : " ORDER BY " + String.join(", ", orderByColumns);
    }
}
