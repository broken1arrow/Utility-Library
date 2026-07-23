package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.QueryModifier;
import org.broken.arrow.library.database.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.utlity.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
public class InsertHandler implements ParameterSupplier{

    private final Map<Integer, InsertBuilder> insertValues = new LinkedHashMap<>();
    private final Map<Integer, Object> values = new LinkedHashMap<>();
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
        insertValues.put(insertValues.size() + 1, value);
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
     * Returns 1-based indexed parameter values for prepared statement binding.
     *
     * @return a map of index to column value objects
     */
    public Map<Integer, Object> getIndexedValues() {
        if (isInsertFromSelect()) {
            return queryModifier.getParameterValues();
        }

        final Map<Integer, Object> indexedMap = new LinkedHashMap<>();
        int paramIndex = 1;

        for (Object value : getRawParameters()) {
            indexedMap.put(paramIndex++, value);
        }
        return indexedMap;
    }

    @Nonnull
    @Override
    public List<Object> getRawParameters() {
        if (isInsertFromSelect()) {
            return new ArrayList<>(queryModifier.getParameterValues().values());
        }
        if (!this.queryBuilder.isGlobalEnableQueryPlaceholders() || insertValues.isEmpty()) {
            return Collections.emptyList();
        }
        return insertValues.values().stream()
                .map(InsertBuilder::getColumnValue)
                .collect(Collectors.toList());
    }

    /**
     * Builds the SQL fragment for the INSERT operation.
     *
     * @return the generated SQL fragment
     */
    public String build() {
        if (insertValues.isEmpty()) {
            return "";
        }
        List<String> columnNames = insertValues.values().stream()
                .map(InsertBuilder::getColumnName)
                .collect(Collectors.toList());

        if (isInsertFromSelect()) {
            return buildInsertFromSelect(columnNames);
        }

        return buildStandardInsert(columnNames);
    }

    private boolean isInsertFromSelect() {
        return queryModifier.getTable() != null
                && !queryModifier.getSelectBuilder().getColumns().isEmpty();
    }

    private String buildInsertFromSelect(List<String> columnNames) {
        StringBuilder sql = new StringBuilder();

        sql.append(" (")
                .append(StringUtil.stringJoin(columnNames))
                .append(") ");

        sql.append("SELECT ")
                .append(queryModifier.getSelectBuilder().build())
                .append(" FROM ")
                .append(queryModifier.getTableWithAlias());


        String joins = queryModifier.getJoinBuilder().build();
        if (joins != null && !joins.isEmpty()) {
            sql.append(" ").append(joins);
        }

        String where = queryModifier.getWhereBuilder().build();
        if (where != null && !where.isEmpty()) {
            sql.append(" ").append(where);
        }

        String groupBy = queryModifier.getGroupByBuilder().build();
        if (groupBy != null && !groupBy.isEmpty()) {
            sql.append(" ").append(groupBy);
        }

        String having = queryModifier.getHavingBuilder().build();
        if (having != null && !having.isEmpty()) {
            sql.append(" ").append(having);
        }

        String orderBy = queryModifier.getOrderByBuilder().build();
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ").append(orderBy);
        }

        String limit = queryModifier.getLimit();
        if (limit != null && !limit.isEmpty()) {
            sql.append(" ").append(limit);
        }

        return sql.toString();
    }

    private String buildStandardInsert(List<String> columnNames) {
        StringBuilder sql = new StringBuilder();
        sql.append(" (").append(StringUtil.stringJoin(columnNames)).append(") VALUES (");

        if (this.queryBuilder.isGlobalEnableQueryPlaceholders()) {
            sql.append(String.join(", ", Collections.nCopies(insertValues.size(), "?")));
        } else {
            List<Object> rawValues = insertValues.values().stream()
                    .map(InsertBuilder::getColumnValue)
                    .collect(Collectors.toList());
            sql.append(StringUtil.stringJoin(rawValues));
        }

        sql.append(")");
        return sql.toString();
    }
}
