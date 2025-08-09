package org.broken.arrow.library.database.construct.query;


import org.broken.arrow.library.database.construct.query.builder.GroupByBuilder;
import org.broken.arrow.library.database.construct.query.builder.JoinBuilder;
import org.broken.arrow.library.database.construct.query.builder.OrderByBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.havingbuilder.HavingBuilder;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Extends {@link Selector} to add support for additional query modifiers
 * commonly used in SELECT statements, such as GROUP BY, ORDER BY, and LIMIT.
 *
 * <p>
 * This class is suitable for building complete SQL SELECT queries with
 * grouping, ordering, and pagination features.
 * </p>
 */
public class QueryModifier extends Selector<ColumnBuilder<Column, Void>, Column> {
    private final GroupByBuilder groupByBuilder = new GroupByBuilder();

    private final OrderByBuilder orderByBuilder = new OrderByBuilder();
    private final QueryBuilder queryBuilder;
    private int limit;

    /**
     * Creates a new query modifier instance with the given query builder.
     *
     * @param queryBuilder  the query builder used to build the full SQL query
     */
    public QueryModifier(QueryBuilder queryBuilder) {
        super(new ColumnBuilder<>(), queryBuilder);
        this.queryBuilder = queryBuilder;
    }

    @Override
    public QueryModifier from(String table) {
        super.from(table);
        return this;
    }

    @Override
    public QueryModifier from(String table, String alias) {
        super.from(table, alias);
        return this;
    }

    @Override
    public QueryModifier from(QueryBuilder queryBuilder) {
        super.from(queryBuilder);
        return this;
    }

    @Override
    public QueryModifier where(WhereBuilder whereBuilder) {
        super.where(whereBuilder);
        return this;
    }
    @Override
    public QueryModifier where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> callback) {
        super.where(callback);
        return this;
    }

    @Override
    public QueryModifier select(Consumer<ColumnBuilder<Column, Void>> callback) {
        super.select(callback);
        return this;
    }

    @Override
    public QueryModifier join(Consumer<JoinBuilder> callback) {
        super.join(callback);
        return this;
    }

    @Override
    public QueryModifier having(Consumer<HavingBuilder> callback) {
        super.having(callback);
        return this;
    }

    /**
     * Specifies the columns to group the results by.
     *
     * @param columns the column names to include in the GROUP BY clause
     * @return this QueryModifier instance for chaining
     */
    public QueryModifier groupBy(String... columns) {
        groupByBuilder.groupBy(columns);
        return this;
    }

    /**
     * Applies a callback to the OrderByBuilder to specify ORDER BY clauses.
     *
     * @param callback a consumer that configures the order by builder
     * @return this QueryModifier instance for chaining
     */
    public QueryModifier orderBy(Consumer<OrderByBuilder> callback) {
        callback.accept(orderByBuilder);
        return this;
    }

    /**
     * Sets the maximum number of rows to return in the query result.
     *
     * @param limit the row limit (must be greater than zero)
     * @return this QueryModifier instance for chaining
     */
    public QueryModifier limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Returns the SQL fragment representing the LIMIT clause,
     * or an empty string if no limit is set.
     *
     * @return the LIMIT clause string or empty string if limit below one
     */
    public String getLimit() {
        if(limit < 1)
            return "";
        return "LIMIT" + limit;
    }

    /**
     * Gets the GroupByBuilder used for building GROUP BY clauses.
     *
     * @return the GroupByBuilder instance
     */
    public GroupByBuilder getGroupByBuilder() {
        return groupByBuilder;
    }

    /**
     * Gets the OrderByBuilder used for building ORDER BY clauses.
     *
     * @return the OrderByBuilder instance
     */
    public OrderByBuilder getOrderByBuilder() {
        return orderByBuilder;
    }

}