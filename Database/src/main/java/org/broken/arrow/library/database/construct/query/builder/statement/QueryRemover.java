package org.broken.arrow.library.database.construct.query.builder.statement;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.QueryModifier;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.clause.OrderByBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.utlity.QueryType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for managing WHERE clause removals or modifications in a query.
 * <p>
 * Provides access to the internal WhereBuilder to build or modify WHERE conditions.
 * </p>
 */
public class QueryRemover {
    private final QueryBuilder queryBuilder;
    private WhereBuilder whereBuilder;
    private RemoveModifier removeModifier;


    /**
     * Creates a QueryRemover for the given QueryBuilder.
     *
     * @param queryBuilder the parent query builder
     */
    public QueryRemover(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;

    }

    /**
     * Sets the WHERE clause using a builder function and returns the original QueryBuilder.
     *
     * @param whereClause function to build the WHERE clause
     * @return the parent QueryBuilder for chaining
     */
    public QueryBuilder where(Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        this.whereBuilder = new WhereBuilder(queryBuilder);
        whereClause.apply(whereBuilder);
        return queryBuilder;
    }

    /**
     * Get the modifier like join, order by and similar to modify a table.
     *
     * @return the modifier instance for the remove operation.
     */
    public RemoveModifier getQueryModifier() {
        if (this.removeModifier == null)
            this.removeModifier = new RemoveModifier(this.queryBuilder);
        return this.removeModifier;
    }

    /**
     * Gets the internal WhereBuilder if available.
     *
     * @return the WhereBuilder or null if not set
     */
    @Nullable
    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }

    /**
     * Builds and returns the final SQL string for the removal operation.
     *
     * @return the constructed SQL query string
     */
    @Nonnull
    public String build() {
        StringBuilder sql = new StringBuilder();

        final RemoveModifier modifier = this.removeModifier;
        if (modifier != null) {
            sql.append(modifier.getJoinBuilder().build());
        }
        final WhereBuilder whereBuilder = this.getWhereBuilder();
        sql.append(whereBuilder != null ? whereBuilder.build() : "");

        if (modifier != null) {
            sql.append(modifier.getOrderByBuilder().build());
            sql.append(modifier.getLimit());
        }
        return sql.toString();
    }

    /**
     * Extracts positional parameter bindings for the built query.
     *
     * @return a map of 1-based index positions to parameter values,
     * or an empty map if placeholders are globally disabled
     */
    @Nonnull
    public Map<Integer, Object> getValues() {
        if (!this.queryBuilder.isGlobalEnableQueryPlaceholders()) {
            return new HashMap<>();
        }
        final RemoveModifier modifier = this.removeModifier;
        final Map<Integer, Object> values = new HashMap<>();
        int index = 1;

        if (modifier != null) {
            for (Object value : modifier.getJoinBuilder().getRawParameters()) {
                values.put(index++, value);
            }
        }

        final WhereBuilder whereBuilder = this.getWhereBuilder();
        if (whereBuilder != null) {
            for (Object value : whereBuilder.getRawParameters()) {
                values.put(index++, value);
            }
        }
        return values;
    }

    /**
     * Builder modifier for configuring JOINs, ORDER BY, and LIMIT clauses.
     */
    public class RemoveModifier {
        private final OrderByBuilder orderByBuilder = new OrderByBuilder();
        private final JoinBuilder joinBuilder;
        private int limit = -1;

        /**
         * Creates a RemoveModifier bound to the given QueryBuilder.
         *
         * @param queryBuilder the parent query builder
         */
        public RemoveModifier(@Nonnull final QueryBuilder queryBuilder) {
            this.joinBuilder = new JoinBuilder(queryBuilder);
        }

        /**
         * Applies a callback to the join builder to specify JOIN clauses.
         *
         * @param callback a consumer that configures the join builder
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier join(Consumer<JoinBuilder> callback) {
            callback.accept(joinBuilder);
            return this;
        }

        /**
         * Applies a callback to the OrderByBuilder to specify ORDER BY clauses.
         *
         * @param callback a consumer that configures the order by builder
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier orderBy(Consumer<OrderByBuilder> callback) {
            callback.accept(orderByBuilder);
            return this;
        }

        /**
         * Sets the maximum number of rows to modify or remove.
         *
         * @param limit the row limit (must be greater than zero)
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Gets the OrderByBuilder used for building ORDER BY clauses.
         *
         * @return the OrderByBuilder instance
         */
        public OrderByBuilder getOrderByBuilder() {
            return orderByBuilder;
        }

        /**
         * Returns join builder
         *
         * @return the join builder.
         */
        public JoinBuilder getJoinBuilder() {
            return joinBuilder;
        }

        /**
         * Returns the SQL fragment representing the LIMIT clause,
         * or an empty string if no limit is set.
         *
         * @return the LIMIT clause string or empty string if limit below one
         */
        public String getLimit() {
            if (limit < 1)
                return "";
            return " LIMIT " + limit;
        }
    }
}

