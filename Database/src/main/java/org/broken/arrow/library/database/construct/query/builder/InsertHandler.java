package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * A utility class for managing column-value pairs for an SQL {@code INSERT} operation.
 * <p>
 * This class stores {@link InsertBuilder} instances in insertion order and automatically
 * assigns an index to each column-value pair starting at {@code 1}. The index can be used
 * for binding parameters in prepared statements.
 * </p>
 * <p>
 * Values can be added individually, from a collection, or from a map of {@link Column} objects
 * to their corresponding values.
 * </p>
 */
public class InsertHandler {

    private final Map<Integer, InsertBuilder> insertValues = new HashMap<>();
    private int columnIndex = 1;

    /**
     * Adds a single column-value pair to the handler.
     *
     * @param value the {@link InsertBuilder} containing the column name and value
     */
    public void add(InsertBuilder value) {
        insertValues.put(columnIndex++, value);
    }

    /**
     * Adds multiple column-value pairs to the handler.
     *
     * @param values one or more {@link InsertBuilder} instances to add
     */
    public void addAll(InsertBuilder... values) {
        for (InsertBuilder insert : values) {
            this.add(insert);
        }
    }

    /**
     * Adds column-value pairs from a map of {@link Column} objects to values.
     * <p>
     * Each {@link Column} is converted into an {@link InsertBuilder} using its column name.
     * </p>
     *
     * @param columnData a map where the key is a {@link Column} and the value is the data to insert
     */
    public void addAll(Map<Column, Object> columnData) {
        for (Map.Entry<Column, Object> insert : columnData.entrySet()) {
            this.add(new InsertBuilder(insert.getKey().getColumnName(), insert.getValue()));
        }
    }

    /**
     * Returns all indexed {@link InsertBuilder} instances stored in this handler.
     *
     * @return a map of index to {@link InsertBuilder} instances
     */
    public Map<Integer, InsertBuilder> getInsertValues() {
        return insertValues;
    }

    /**
     * Returns only the indexed values without column names.
     * <p>
     * This is useful for parameter binding in prepared statements.
     * </p>
     *
     * @return a map of index to column value objects
     */
    public Map<Integer, Object> getIndexedValues() {
        return insertValues.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getColumnValue()
                ));
    }

}
