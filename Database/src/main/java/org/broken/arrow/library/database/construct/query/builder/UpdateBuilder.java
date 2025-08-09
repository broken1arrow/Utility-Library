package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for SQL UPDATE statements.
 * <p>
 * Manages columns and values to be updated, supports bulk operations,
 * and integrates with a Selector for WHERE clause management.
 * </p>
 */
public class UpdateBuilder {
    private final Map<String, Object> updateData = new LinkedHashMap<>();
    private final Map<Integer, Object> values = new LinkedHashMap<>();
    private final Selector<ColumnBuilder<Column, Void>,Column> selector;
    private int columnIndex = 1;

    /**
     * Constructs an UpdateBuilder for the given QueryBuilder context.
     *
     * @param queryBuilder parent query builder
     */
    public UpdateBuilder(@Nonnull final QueryBuilder queryBuilder) {
        this.selector = new Selector<>(new ColumnBuilder<>(), queryBuilder);
    }

    /**
     * Adds a column and value to update.
     *
     * @param column column name as string
     * @param value  value to set
     * @return this UpdateBuilder instance for chaining
     */
    public UpdateBuilder put(final String column, final Object value) {
        return this.put(new Column(column,""), value);
    }

    /**
     * Adds a column and value to update.
     *
     * @param column Column object
     * @param value  value to set
     * @return this UpdateBuilder instance for chaining
     */
    public UpdateBuilder put(final Column column, final Object value) {
        updateData.put(column.getColumnName(), value);
        values.put(columnIndex++, value);
        return this;
    }

    /**
     * Adds all columns and values from the given map.
     *
     * @param map mapping of Column to values
     * @return this UpdateBuilder instance for chaining
     */
    public UpdateBuilder putAll(Map<Column, Object> map) {
        map.forEach(this::put);
        return this;
    }

    /**
     * Gets the selector used for building WHERE clauses.
     *
     * @return selector instance
     */
    public Selector<ColumnBuilder<Column, Void>,Column> getSelector() {
        return selector;
    }

    /**
     * Builds the update data map for generating the SQL update statement.
     *
     * @return map of column names to values
     */
    public Map<String, Object> build() {
        processWhereValues();
        return updateData;
    }

    /**
     * Gets the map of parameter indexes to values, including WHERE values.
     *
     * @return indexed values map
     */
    public Map<Integer, Object> getIndexedValues() {
        return values;
    }

    /**
     * Processes values from the WHERE clause and adds them to indexed values.
     */
    private void processWhereValues() {
        selector.getWhereBuilder().getValues().entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(integerObjectEntry -> values.put(columnIndex++, integerObjectEntry.getValue())
                );
    }

}
