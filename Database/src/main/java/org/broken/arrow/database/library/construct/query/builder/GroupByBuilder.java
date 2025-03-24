package org.broken.arrow.database.library.construct.query.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupByBuilder {
    private final List<String> groupByColumns = new ArrayList<>();

    public GroupByBuilder groupBy(String... columns) {
        groupByColumns.addAll(Arrays.asList(columns));
        return this;
    }

    public String build() {
        return groupByColumns.isEmpty() ? "" : " GROUP BY " + String.join(", ", groupByColumns);
    }
}
