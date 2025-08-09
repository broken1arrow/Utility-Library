package org.broken.arrow.library.database.construct.query;


import org.broken.arrow.library.database.construct.query.builder.JoinBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.havingbuilder.HavingBuilder;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * A generic builder class that supports constructing SQL SELECT queries with
 * flexible clauses including SELECT columns, FROM tables (or subqueries),
 * WHERE conditions, JOINs, and HAVING clauses.
 *
 * <p>
 * This class uses a generic {@code ColumnBuilder} to allow customizable column
 * selection logic and manages core SQL components such as:
 * </p>
 * <ul>
 *   <li>Selection of columns</li>
 *   <li>Table or subquery specification (with optional alias)</li>
 *   <li>WHERE condition building</li>
 *   <li>JOIN clause construction</li>
 *   <li>HAVING clause construction</li>
 * </ul>
 *
 * <p>
 * It is designed to be extended or used directly for constructing a wide range
 * of SQL SELECT queries, including but not limited to simple table selections.
 * </p>
 *
 * <p>
 * For more advanced query modifications, such as GROUP BY, ORDER BY, or LIMIT,
 * consider using {@link QueryModifier}, which extends this class.
 * </p>
 *
 * @param <T> the type of the column builder used for building the SELECT clause
 * @param <V> the type of columns managed by the column builder
 */
public class Selector<T extends ColumnBuilder<V, ?>, V extends Column> {

    private final T selectBuilder;
    private final QueryBuilder queryBuilder;
    private final JoinBuilder joinBuilder;
    private final HavingBuilder havingBuilder;

    private WhereBuilder whereBuilder;
    private String table;
    private String tableAlias;

    /**
     * Creates a new Selector instance with the given select builder and query builder.
     *
     * @param selectBuilder the column builder used to construct SELECT columns
     * @param queryBuilder  the query builder used to build the full SQL query
     */
    public Selector(@Nonnull final T selectBuilder, QueryBuilder queryBuilder) {
        this.selectBuilder = selectBuilder;
        this.queryBuilder = queryBuilder;
        whereBuilder = new WhereBuilder(queryBuilder);
        havingBuilder = new HavingBuilder(queryBuilder);
        joinBuilder = new JoinBuilder();
    }

    /**
     * Specifies the table to select from.
     *
     * @param table the name of the table
     * @return this Selector instance for chaining
     */
    public Selector<T,V> from(String table) {
        this.table = table;
        return this;
    }

    /**
     * Specifies the table and alias to select from.
     *
     * @param table the name of the table
     * @param alias the alias for the table
     * @return this Selector instance for chaining
     */
    public Selector<T,V>  from(String table, String alias) {
        this.table = table;
        this.tableAlias = alias;
        return this;
    }

    /**
     * Specifies a subquery as the table source.
     *
     * @param queryBuilder a QueryBuilder representing the subquery
     * @return this Selector instance for chaining
     */
    protected Selector<T,V>  from(QueryBuilder queryBuilder) {
        this.table = "(" + queryBuilder.build().replace(";", "") + ")";
        return this;
    }

    /**
     * Sets the WHERE clause builder.
     *
     * @param whereBuilder the WhereBuilder instance to use
     * @return this Selector instance for chaining
     */
    public Selector<T,V>  where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    /**
     * Applies a WHERE clause using a callback to build the condition.
     *
     * @param callback a function that configures the WhereBuilder
     * @return this Selector instance for chaining
     */
    public Selector<T,V>  where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> callback) {
        this.whereBuilder = new WhereBuilder(queryBuilder);
        callback.apply(this.whereBuilder);
        return this;
    }

    /**
     * Applies a callback to the select builder to specify columns.
     *
     * @param callback a consumer that configures the select builder
     * @return this Selector instance for chaining
     */
    public Selector<T,V>  select(Consumer<T> callback) {
        callback.accept(selectBuilder);
        return this;
    }

    /**
     * Applies a callback to the join builder to specify JOIN clauses.
     *
     * @param callback a consumer that configures the join builder
     * @return this Selector instance for chaining
     */
    public Selector<T,V> join(Consumer<JoinBuilder> callback) {
        callback.accept(joinBuilder);
        return this;
    }

    /**
     * Applies a callback to the having builder to specify HAVING clauses.
     *
     * @param callback a consumer that configures the having builder
     * @return this Selector instance for chaining
     */
    public Selector<T,V>  having(Consumer<HavingBuilder> callback) {
        callback.accept(havingBuilder);
        return this;
    }

    /**
     * Returns SelectBuilder
     *
     * @return the  SelectBuilder.
     */
    public T getSelectBuilder() {
        return selectBuilder;
    }

    /**
     * Returns where builder
     *
     * @return the  where builder.
     */
    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }

    /**
     * Returns having builder
     *
     * @return the having builder.
     */
    public JoinBuilder getJoinBuilder() {
        return joinBuilder;
    }

    /**
     * Returns having builder
     *
     * @return the having builder.
     */
    public HavingBuilder getHavingBuilder() {
        return havingBuilder;
    }

    /**
     * Returns the table name.
     *
     * @return the table name.
     */
    public String getTable() {
        return table;
    }

    /**
     * Returns the table name combined with its alias if present.
     *
     * @return the table and alias formatted for use in queries
     */
    public String getTableWithAlias() {
        if (tableAlias == null || tableAlias.isEmpty())
            return table;
        return table + " " + tableAlias;
    }

    /**
     * Returns the table alias if present.
     *
     * @return the alias for use in queries.
     */
    public String getTableAlias() {
        return tableAlias;
    }

}
