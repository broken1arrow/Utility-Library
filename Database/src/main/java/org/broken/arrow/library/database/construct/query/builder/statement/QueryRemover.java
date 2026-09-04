package org.broken.arrow.library.database.construct.query.builder.statement;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.QueryModifier;
import org.broken.arrow.library.database.construct.query.builder.clause.OrderByBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuildContext;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.joinbuilder.JoinCondition;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnManager;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.utility.DatabaseType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final DatabaseType databaseType;
    private WhereBuilder whereBuilder;
    private RemoveModifier removeModifier;


    /**
     * Creates a QueryRemover for the given QueryBuilder.
     *
     * @param queryBuilder the parent query builder
     */
    public QueryRemover(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        this.databaseType = DatabaseType.POSTGRESQL;

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
        final WhereBuilder whereBuilder = this.getWhereBuilder();

        if (modifier != null) {
            final String[] tables = modifier.getUsingTables();
            final JoinBuilder joinBuilder = modifier.getJoinBuilder();
            if (tables.length > 0) {
                sql.append("USING ").append(String.join(", ", tables)).append(" ");
            } else if (databaseType != DatabaseType.POSTGRESQL) {
                sql.append(joinBuilder.build());
            }
            if (tables.length == 0 && databaseType == DatabaseType.POSTGRESQL)
                translateJoin(joinBuilder, sql, whereBuilder);
        }

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
            String[] tables = modifier.getUsingTables();
            if (tables.length == 0 && databaseType != DatabaseType.POSTGRESQL) {
                for (Object value : modifier.getJoinBuilder().getRawParameters()) {
                    values.put(index++, value);
                }
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

    private void translateJoin(JoinBuilder joinBuilder, StringBuilder sql, WhereBuilder mainWhereBuilder) {
        // We might need to collect tables for the Postgres USING clause
        List<String> usingTables = new ArrayList<>();
        for (JoinCondition join : joinBuilder.getJoinBuilders()) {
            switch (join.getType()) {
                case INNER:
                    usingTables.add(join.getTable());
                    transferConditions(join, mainWhereBuilder.chainWhere().and());
                    break;
                case LEFT:
                    final QueryBuilder subQueryBuilder = new QueryBuilder();
                    final QueryModifier modifier = subQueryBuilder.select(ColumnManager.of().column("1"))
                            .from(join.getTable());

                    WhereBuilder subWhere = new WhereBuilder();
                    transferConditions(join, subWhere);
                    modifier.where(subWhere);
                    mainWhereBuilder.chainWhere().and().where("").notExists(subQueryBuilder);
                    break;

                case RIGHT:
                case FULL:
                case CROSS:
                    throw new UnsupportedOperationException(
                            join.getType() + " is not supported in DELETE queries."
                    );
            }
        }
        // If you are building the USING clause for Postgres directly in the sql StringBuilder:
        if (!usingTables.isEmpty()) {
            sql.append("USING ").append(String.join(", ", usingTables)).append(" ");
        }
    }

    private void transferConditions(JoinCondition join, WhereBuilder where) {
        for (ComparisonHandler<JoinBuildContext> handler : join.getJoinBuild().getConditionsList()) {
            handler.transferTo(where::where);
        }
    }


    /**
     * Builder modifier for configuring JOINs, ORDER BY, and LIMIT clauses.
     */
    public class RemoveModifier {
        private final OrderByBuilder orderByBuilder = new OrderByBuilder();
        private String[] usingTables;
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
        public RemoveModifier join(@Nonnull final Consumer<JoinBuilder> callback) {
            callback.accept(joinBuilder);
            return this;
        }

        /**
         * Set the using tables.
         *
         * @param tables The tables to set for the remove.
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier using(@Nonnull final String... tables) {
            usingTables = tables;
            return this;
        }

        /**
         * Applies a callback to the OrderByBuilder to specify ORDER BY clauses.
         *
         * @param callback a consumer that configures the order by builder
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier orderBy(@Nonnull final Consumer<OrderByBuilder> callback) {
            callback.accept(orderByBuilder);
            return this;
        }

        /**
         * Sets the maximum number of rows to modify or remove.
         *
         * @param limit the row limit (must be greater than zero)
         * @return this RemoveModifier instance for chaining
         */
        public RemoveModifier limit(final int limit) {
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

        /**
         * Set the using tables.
         *
         * @return The list of tables set.
         */
        @Nonnull
        public String[] getUsingTables() {
            if (this.usingTables == null) return new String[0];

            return this.usingTables;
        }
    }
}

