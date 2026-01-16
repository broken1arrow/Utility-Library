package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.QueryModifier;
import org.broken.arrow.library.database.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.utlity.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final Map<Integer, InsertBuilder> insertValues = new LinkedHashMap<>();
    private final QueryModifier queryModifier;
    private final QueryBuilder queryBuilder;
    private int columnIndex = 1;

    /**
     * This handle the inner parts of your insert command.
     *
     * @param queryBuilder the top class for build the command.
     */
    public InsertHandler(final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        this.queryModifier = new QueryModifier(queryBuilder);
    }

    /**
     * Adds a single column-value pair to the handler.
     *
     * @param value the {@link InsertBuilder} containing the column name and value
     * @return this instance for chaining
     */
    public InsertHandler add(InsertBuilder value) {
        insertValues.put(columnIndex++, value);
        return this;
    }

    /**
     * Adds multiple column-value pairs to the handler.
     *
     * @param values one or more {@link InsertBuilder} instances to add
     * @return this instance for chaining
     */
    public InsertHandler addAll(InsertBuilder... values) {
        for (InsertBuilder insert : values) {
            this.add(insert);
        }
        return this;
    }

    /**
     * Adds column-value pairs from a map of {@link Column} objects to values.
     * <p>
     * Each {@link Column} is converted into an {@link InsertBuilder} using its column name.
     * </p>
     *
     * @param columnData a map where the key is a {@link Column} and the value is the data to insert
     * @return this instance for chaining
     */
    public InsertHandler addAll(Map<Column, Object> columnData) {
        for (Map.Entry<Column, Object> insert : columnData.entrySet()) {
            this.add(new InsertBuilder(insert.getKey().getColumnName(), insert.getValue()));
        }
        return this;
    }


    /**
     * Adds column from a list of {@link Column} and the values is set to null.
     * <p>
     * Each {@link Column} is converted into an {@link InsertBuilder} using its column name.
     * </p>
     *
     * @param columns a map where the key is a {@link Column} and column set to null.
     * @return this instance for chaining
     */
    public InsertHandler addAll(List<Column> columns) {
        for (Column column : columns) {
            this.add(new InsertBuilder(column.getColumnName(), null));
        }
        return this;
    }

    /**
     * Get the modifier like select and similar for modify a table.
     *
     * @return the modifies instance for the insert operation.
     */
    public QueryModifier getQueryModifier() {
        return this.queryModifier;
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
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    /**
     * Build the inner parts of the query command.
     *
     * @return the inner parts of the query.
     */
    public String build() {
        final StringBuilder sql = new StringBuilder();
        Collection<InsertBuilder> insertBuilders = this.getInsertValues().values();
        if (insertBuilders.isEmpty()) return "";

        List<String> columnNames = new ArrayList<>();
        List<Object> columnValues = new ArrayList<>();

        for (InsertBuilder builder : insertBuilders) {
            columnNames.add(builder.getColumnName());
            if (!this.queryBuilder.isGlobalEnableQueryPlaceholders()) {
                columnValues.add(builder.getColumnValue());
            }
        }

        String select = setSelect(sql, columnNames);
        if (select != null) return select;

        sql.append(" (")
                .append(StringUtil.stringJoin(columnNames))
                .append(") VALUES (");

        if (this.queryBuilder.isGlobalEnableQueryPlaceholders()) {
            sql.append(StringUtil.repeat("?,", insertBuilders.size()).replaceAll(",$", ""));
        } else {
            sql.append(StringUtil.stringJoin(columnValues));
        }
        sql.append(" )");
        return sql.toString();
    }

    private String setSelect(final StringBuilder sql, final List<String> columnNames) {
        final QueryModifier queryModifier = getQueryModifier();
        final ColumnBuilder<Column, Void> selectBuilder = queryModifier.getSelectBuilder();
        final String from = queryModifier.getTable();
        final String selectSql = selectBuilder.build();

        if (!selectSql.isEmpty() && from != null) {


            sql.append("(")
                    .append(StringUtil.stringJoin(columnNames))
                    .append(") SELECT ")
                    .append(selectSql)
                    .append(" FROM ")
                    .append(from);

            return sql.toString();
        }
        return null;
    }

}
