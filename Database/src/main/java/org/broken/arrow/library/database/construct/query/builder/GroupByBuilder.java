package org.broken.arrow.library.database.construct.query.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for SQL GROUP BY clauses.
 * <p>
 * Collects column names to be included in the GROUP BY clause and
 * generates the corresponding SQL string.
 * </p>
 */
public class GroupByBuilder {
    private final List<String> groupByColumns = new ArrayList<>();

    /**
     * Adds one or more columns to the GROUP BY clause.
     *
     * @param columns columns to group by
     * @return this builder instance for chaining
     */
    public GroupByBuilder groupBy(String... columns) {
        groupByColumns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Builds the GROUP BY clause SQL string.
     *
     * @return SQL GROUP BY clause or empty string if no columns added
     */
    public String build() {
        return groupByColumns.isEmpty() ? "" : " GROUP BY " + String.join(", ", groupByColumns);
    }
}
