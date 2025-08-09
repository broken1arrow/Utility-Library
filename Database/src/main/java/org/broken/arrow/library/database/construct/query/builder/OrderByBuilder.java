package org.broken.arrow.library.database.construct.query.builder;

import java.util.ArrayList;
import java.util.List;
/**
 * Builder for SQL ORDER BY clauses.
 * <p>
 * Supports adding columns with ascending or descending order and building
 * the ORDER BY clause.
 * </p>
 */
public class OrderByBuilder {
    private final List<String> orderByColumns = new ArrayList<>();

    /**
     * Adds a column to the ORDER BY clause with the specified direction.
     *
     * @param column    the column name
     * @param ascending true for ASC, false for DESC
     */
    public void add(String column, boolean ascending) {
        orderByColumns.add(column + (ascending ? " ASC" : " DESC"));
    }

    /**
     * Builds the ORDER BY clause SQL string.
     *
     * @return SQL ORDER BY clause or empty string if no columns added
     */
    public String build() {
        return orderByColumns.isEmpty() ? "" : " ORDER BY " + String.join(", ", orderByColumns);
    }
}
