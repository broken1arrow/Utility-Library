package org.broken.arrow.library.database.construct.query;


import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.InsertHandler;
import org.broken.arrow.library.database.construct.query.builder.QueryRemover;
import org.broken.arrow.library.database.construct.query.builder.UpdateBuilder;
import org.broken.arrow.library.database.construct.query.builder.WithManger;
import org.broken.arrow.library.database.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.AlterTable;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.QueryType;
import org.broken.arrow.library.database.construct.query.utlity.StringUtil;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
/**
 * Central builder class for constructing SQL queries of various types.
 * <p>
 * This class supports building different SQL statements including SELECT, INSERT, UPDATE, DELETE,
 * CREATE TABLE, DROP TABLE, ALTER TABLE, and WITH clauses, among others.
 * It internally delegates to specialized handlers and builders for each query type,
 * allowing flexible and composable query construction.
 * <p>
 * Usage typically involves choosing the query type via methods like {@code select()}, {@code update(table)},
 * {@code insertInto(table, callback)}, or {@code deleteFrom(table)}, then chaining modifications
 * or providing configuration callbacks.
 * <p>
 * The builder supports:
 * <ul>
 *   <li>Select queries with flexible column selections, joins, where clauses, groupings, ordering, and limits</li>
 *   <li>Update queries with dynamic set values and where filters</li>
 *   <li>Insert, merge, and replace queries with multiple rows and columns</li>
 *   <li>Table creation, alteration, and dropping commands</li>
 *   <li>Common query clauses managed by helper classes like {@link QueryModifier}, {@link UpdateBuilder}, {@link InsertHandler}, etc.</li>
 *   <li>Parameter placeholder control via {@code globalEnableQueryPlaceholders} flag</li>
 * </ul>
 * <p>
 * After configuration, the query can be built into an SQL string with {@link #build()}, and the parameter values
 * can be retrieved with {@link #getValues()} for prepared statement usage.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * QueryBuilder builder = new QueryBuilder();
 * String sql = builder.select()
 *                     .from("users")
 *                     .where("id", LogicalOperators.EQUALS, 123)
 *                     .build();
 * Map<Integer, Object> params = builder.getValues();
 * }
 * </pre>
 * <p>
 * Note: The actual method chaining and detailed API for QueryModifier, UpdateBuilder, InsertHandler,
 * and other internal classes provide further query customization capabilities.
 */
public class QueryBuilder {
    private final UpdateBuilder updateBuilder = new UpdateBuilder(this);
    private final InsertHandler insertHandler = new InsertHandler(this);
    private final QueryModifier queryModifier = new QueryModifier(this);
    private final CreateTableHandler createTableHandler = new CreateTableHandler(this);
    private final AlterTable alterTable = new AlterTable();
    private final QueryRemover queryRemover = new QueryRemover(this);
    private final WithManger withManger = new WithManger();
    private QueryType queryType;
    private String table;
    private boolean globalEnableQueryPlaceholders = true;

    /**
     * Build a query with query type set to non.
     */
    public QueryBuilder() {
        this.queryType = QueryType.NON;
    }

    /**
     * Starts building a CREATE TABLE query for the specified table.
     *
     * @param table the name of the table to create
     * @return the handler to configure the table creation details
     */
    public CreateTableHandler createTable(String table) {
        this.queryType = QueryType.CREATE;
        this.table = table;
        return createTableHandler;
    }

    /**
     * Starts building a CREATE TABLE IF NOT EXISTS query for the specified table.
     *
     * @param table the name of the table to create if not exists
     * @return the handler to configure the table creation details
     */
    public CreateTableHandler createTableIfNotExists(String table) {
        this.queryType = QueryType.CREATE_IF_NOT_EXISTS;
        this.table = table;
        return createTableHandler;
    }

    /**
     * Starts building an ALTER TABLE query for the specified table.
     *
     * @param table the name of the table to alter
     * @return the handler to configure the alteration details
     */
    public AlterTable alterTable(String table) {
        this.queryType = QueryType.ALTER_TABLE;
        this.table = table;
        return  alterTable;
    }

    /**
     * Starts building a DROP TABLE query for the specified table.
     *
     * @param table the name of the table to drop
     * @return the handler used for drop table query construction
     */
    public CreateTableHandler dropTable(String table) {
        this.queryType = QueryType.DROP;
        this.table = table;
        return createTableHandler;
    }

    /**
     * Starts building a SELECT query.
     *
     * @return the query modifier for configuring the select query
     */
    public QueryModifier select() {
        this.queryType = QueryType.SELECT;
        return queryModifier;
    }

    /**
     * Starts building a SELECT query with the given column manager.
     *
     * @param column the column manager containing columns to select
     * @return the query modifier for configuring the select query
     */
    public QueryModifier select(ColumnManager column) {
        this.queryType = QueryType.SELECT;
        queryModifier.select(selectBuilder -> selectBuilder.addAll(column.getColumnsBuilt()));
        return queryModifier;
    }

    /**
     * Starts building a SELECT query with the given list of columns.
     *
     * @param column list of columns to select
     * @return the query modifier for configuring the select query
     */
    public QueryModifier select(List<Column> column) {
        this.queryType = QueryType.SELECT;
        queryModifier.select(selectBuilder -> selectBuilder.addAll(column));
        return queryModifier;
    }

    /**
     * Starts building an UPDATE query for the specified table, applying the given configuration callback.
     *
     * @param table the name of the table to update
     * @param callback callback to configure the update builder (e.g., setting columns and where clauses)
     * @return the update builder for further configuration or execution
     */
    public UpdateBuilder update(String table, Consumer<UpdateBuilder> callback) {
        callback.accept(updateBuilder);
        this.queryType = QueryType.UPDATE;
        this.table = table;
        return updateBuilder;
    }

    /**
     * Starts building an UPDATE query for the specified table.
     *
     * @param table the name of the table to update
     * @return the update builder for configuring the update query
     */
    public UpdateBuilder update(String table) {
        this.queryType = QueryType.UPDATE;
        this.table = table;
        return this.updateBuilder;
    }

    /**
     * Starts building an INSERT INTO query for the specified table with a configuration callback.
     *
     * @param table the name of the table to insert into
     * @param callback callback to configure the insert handler (e.g., setting columns and values)
     */
    public void insertInto(String table, Consumer<InsertHandler> callback) {
        callback.accept(insertHandler);
        this.queryType = QueryType.INSERT;
        this.table = table;
    }

    /**
     * Starts building a MERGE INTO query for the specified table with a configuration callback.
     *
     * @param table the name of the table to merge into
     * @param callback callback to configure the insert handler for merge operation
     */
    public void mergeInto(String table, Consumer<InsertHandler> callback) {
        callback.accept(insertHandler);
        this.queryType = QueryType.MERGE_INTO;
        this.table = table;
    }

    /**
     * Starts building a REPLACE INTO query for the specified table with a configuration callback.
     *
     * @param table the name of the table to replace into
     * @param callback callback to configure the insert handler for replace operation
     */
    public void replaceInto(String table, Consumer<InsertHandler> callback) {
        callback.accept(insertHandler);
        this.queryType = QueryType.REPLACE_INTO;
        this.table = table;
    }

    /**
     * Starts building a DELETE FROM query for the specified table.
     *
     * @param table the name of the table to delete from
     * @return the query remover for configuring delete conditions
     */
    public QueryRemover deleteFrom(String table) {
        this.queryType = QueryType.DELETE;
        this.table = table;
        return queryRemover;
    }

    /**
     * Starts building a WITH clause query.
     *
     * @param callback callback to configure the WITH manager
     * @return the WITH manager for further query construction
     */
    public WithManger with(Consumer<WithManger> callback) {
        this.queryType = QueryType.WITH;
        callback.accept(withManger);
        return withManger;
    }

    /**
     * Checks whether the query type has been set, i.e., if the query is ready to be built.
     *
     * @return true if a query type other than NON is set, false otherwise
     */
    public boolean isQuerySet() {
        return this.queryType != null && this.queryType != QueryType.NON;
    }

    /**
     * Checks whether query placeholders (e.g., '?') are globally enabled for parameterized queries.
     *
     * @return true if placeholders are enabled, false otherwise
     */
    public boolean isGlobalEnableQueryPlaceholders() {
        return globalEnableQueryPlaceholders;
    }

    /**
     * Sets whether query placeholders (e.g., '?') should be used globally.
     *
     * @param globalEnableQueryPlaceholders true to enable placeholders, false to embed literal values
     * @return this QueryBuilder instance for chaining
     */
    public QueryBuilder setGlobalEnableQueryPlaceholders(final boolean globalEnableQueryPlaceholders) {
        this.globalEnableQueryPlaceholders = globalEnableQueryPlaceholders;
        return this;
    }

    /**
     * Gets the name of the table involved in the current query.
     *
     * @return the table name or null if not set
     */
    public String getTableName() {
        return table;
    }

    /**
     * Gets the current query type.
     *
     * @return the QueryType enum representing the current query type
     */
    public QueryType getQueryType() {
        return queryType;
    }

    /**
     * Gets the QueryModifier instance used for building SELECT queries.
     *
     * @return the QueryModifier instance
     */
    public QueryModifier getQueryModifier() {
        return queryModifier;
    }

    /**
     * Builds and returns the complete SQL query string with a trailing semicolon.
     *
     * @return the SQL query string
     * @throws IllegalStateException if the query type has not been set
     */
    public String build() {
        if (this.queryType == null) {
            throw new IllegalStateException("Query type must be set before building.");
        }
        StringBuilder sql = getBuiltCommand(this.queryModifier);
        return sql + ";";
    }

    /**
     * Returns a map of parameter values indexed by their position in the query.
     * <p>
     * The returned map depends on the current query type:
     * <ul>
     *   <li>UPDATE: Returns parameter values used in the update builder (e.g., SET and WHERE clauses).</li>
     *   <li>INSERT, MERGE_INTO, REPLACE_INTO: Returns parameter values from the insert handler.</li>
     *   <li>SELECT: Returns parameters from the WHERE clause if present; otherwise, parameters from the HAVING clause.</li>
     *   <li>DELETE: Returns parameters from the WHERE clause if set.</li>
     *   <li>If no parameters exist or query type is not set, returns an empty map.</li>
     * </ul>
     *
     * @return a map where keys are parameter indexes and values are the corresponding parameter values
     */
    public Map<Integer, Object> getValues() {

        if (queryType == QueryType.UPDATE) {
            return updateBuilder.getIndexedValues();
        } else if (queryType == QueryType.INSERT || queryType == QueryType.MERGE_INTO || queryType == QueryType.REPLACE_INTO) {
            return insertHandler.getIndexedValues();
        } else if (queryType == QueryType.SELECT) {
            if (!queryModifier.getWhereBuilder().getValues().isEmpty()) {
                return queryModifier.getWhereBuilder().getValues();
            } else {
                return queryModifier.getHavingBuilder().getValues();
            }
        } else if (queryType == QueryType.DELETE) {
            WhereBuilder whereBuilder = queryRemover.getWhereBuilder();
            if (whereBuilder != null)
                return whereBuilder.getValues();
        }
        return new HashMap<>();
    }

    /**
     * Returns the number of columns involved in the current query.
     * <p>
     * The count depends on the query type:
     * <ul>
     *   <li>UPDATE: Returns the number of columns being updated (from the select builder inside updateBuilder).</li>
     *   <li>INSERT: Returns the number of columns being inserted (from insertHandler).</li>
     *   <li>SELECT: Returns the number of selected columns.</li>
     *   <li>DELETE: Returns -1, as columns are not applicable for delete operations.</li>
     *   <li>If query type is not set or unknown, returns -1.</li>
     * </ul>
     *
     * @return the number of columns involved, or -1 if not applicable
     */
    public int getAmountColumnsSet() {

        if (queryType == QueryType.UPDATE) {
            return updateBuilder.getSelector().getSelectBuilder().getColumns().size();
        } else if (queryType == QueryType.INSERT) {
            return insertHandler.getInsertValues().size();
        } else if (queryType == QueryType.SELECT) {
            return queryModifier.getSelectBuilder().getColumns().size();
        } else if (queryType == QueryType.DELETE) {
            return -1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "QueryBuilder{" +
                "queryType=" + queryType +
                ", table='" + table + '\'' +
                ", globalEnableQueryPlaceholders=" + globalEnableQueryPlaceholders +
                '}';
    }

    /**
     * Returns a map of parameter values indexed by their placeholder position in the query.
     * <p>
     * For UPDATE, INSERT, and SELECT queries, returns the parameters used in WHERE or HAVING clauses.
     * For DELETE queries, returns the WHERE clause parameters.
     * <p>
     * If no parameters exist for the query, returns an empty map.
     *
     * @param queryModifier the query modification like join, where, groupBy and more.
     * @return a map of parameter index to parameter value
     */
    @Nonnull
    private StringBuilder getBuiltCommand(final QueryModifier queryModifier) {
        StringBuilder sql = new StringBuilder();
        switch (this.queryType) {
            case SELECT:
                createSelectQuery(queryModifier, sql);
                break;
            case DELETE:
                sql.append("DELETE FROM ").append(table);
                WhereBuilder whereBuilder = this.queryRemover.getWhereBuilder();
                sql.append(whereBuilder != null ? whereBuilder.build() : "");
                break;

            case DROP:
                sql.append("DROP TABLE ").append(table);
                break;

            case CREATE:
                sql.append("CREATE TABLE ").append(table).append(this.createTableHandler.build());
                break;

            case CREATE_IF_NOT_EXISTS:
                sql.append("CREATE TABLE IF NOT EXISTS ").append(table).append(this.createTableHandler.build());
                break;
            case UPDATE:
                createUpdateQuery(sql);
                break;
            case INSERT:
            case MERGE_INTO:
            case REPLACE_INTO:
                createInsertQuery(sql);
                break;
            case WITH:
                sql.append(withManger.build());
                break;
            case ALTER_TABLE:
                sql.append("ALTER TABLE ").append(table).append(" ").append(this.alterTable.build());
                break;
            default:
                break;
        }
        return sql;
    }

    /**
     * Returns the number of columns set for the current query.
     * <ul>
     *   <li>For UPDATE queries, returns the number of columns being updated.</li>
     *   <li>For INSERT queries, returns the number of columns being inserted.</li>
     *   <li>For SELECT queries, returns the number of columns selected.</li>
     *   <li>For DELETE queries, returns -1.</li>
     * </ul>
     * @param  queryModifier the query modification like join, where, groupBy and more.
     * @param sql the builder for the sql command.
     */
    private void createSelectQuery(final QueryModifier queryModifier, final StringBuilder sql) {
        sql.append("SELECT ");

        sql.append(queryModifier.getSelectBuilder().getColumns().isEmpty() ? "*" : queryModifier.getSelectBuilder().build());

        sql.append(" FROM ").append(queryModifier.getTableWithAlias())
                .append(queryModifier.getJoinBuilder().build())
                .append(queryModifier.getWhereBuilder().build())
                .append(queryModifier.getGroupByBuilder().build())
                .append(queryModifier.getHavingBuilder().build())
                .append(queryModifier.getOrderByBuilder().build())
                .append(queryModifier.getLimit());
    }

    private void createUpdateQuery(final StringBuilder sql) {
        Map<String, Object> updateValues = updateBuilder.build();
        if (updateValues.isEmpty()) {
            throw new IllegalStateException("UPDATE queries require at least one SET value.");
        }
        sql.append("UPDATE ").append(table).append(" SET ");
        if (this.globalEnableQueryPlaceholders) {
            sql.append(updateValues.entrySet().stream()
                    .map(entry -> entry.getKey() + " = ?")
                    .collect(Collectors.joining(", ")));
        } else {
            sql.append(updateValues.entrySet().stream()
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", ")));
        }
        sql.append(updateBuilder.getSelector().getWhereBuilder().build());
    }

    private void createInsertQuery(final StringBuilder sql) {
        String sqlKeyword = getInsertStart();
        Set<Map.Entry<Integer, InsertBuilder>> insertValues = this.insertHandler.getInsertValues().entrySet();

        List<InsertBuilder> insertBuilders = insertValues.stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        List<String> columnNames = insertBuilders.stream()
                .map(InsertBuilder::getColumnName)
                .collect(Collectors.toList());
        this.insertHandler.getQueryModifier().getSelectBuilder().build()


        sql.append(sqlKeyword).append(table).append(" (")
                .append(StringUtil.stringJoin(columnNames))
                .append(") VALUES (");

        if (this.globalEnableQueryPlaceholders) {
            sql.append(StringUtil.repeat("?,", insertBuilders.size()).replaceAll(",$", ""));
        } else {
            List<Object> columnValues = insertBuilders.stream()
                    .map(InsertBuilder::getColumnValue)
                    .collect(Collectors.toList());
            sql.append(StringUtil.stringJoin(columnValues));
        }
        sql.append(")");
    }

    @Nonnull
    private String getInsertStart() {
        String sqlKeyword;
        switch (queryType) {
            case INSERT:
                sqlKeyword = "INSERT INTO ";
                break;
            case MERGE_INTO:
                sqlKeyword = "MERGE INTO ";
                break;
            case REPLACE_INTO:
                sqlKeyword = "REPLACE INTO ";
                break;
            default:
                sqlKeyword = "";
        }
        return sqlKeyword;
    }

}