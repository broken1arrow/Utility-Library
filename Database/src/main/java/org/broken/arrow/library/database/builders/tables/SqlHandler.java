package org.broken.arrow.library.database.builders.tables;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.InsertHandler;
import org.broken.arrow.library.database.construct.query.builder.UpdateBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.core.Database;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A convenience wrapper around the {@link QueryBuilder} class that operates on a specific table
 * defined in the {@link Database} instance.
 * <p>
 * This class provides high-level methods for common SQL operations (INSERT, UPDATE, DELETE, SELECT, etc.)
 * and automatically applies the configured table name and quoting style from the {@link Database}.
 * </p>
 */
public class SqlHandler {
    private final Database database;
    private final char quote;
    private final String tableName;
    private boolean setGlobalEnableQueryPlaceholders = true;

    /**
     * Creates a new {@code SqlHandler} for the specified table using the provided database context.
     *
     * @param tableName the name of the table this handler operates on
     * @param database  the database instance that provides quoting rules and stored table metadata
     */
    public SqlHandler(@Nonnull final String tableName, @Nonnull final Database database) {
        this.tableName = tableName;
        this.database = database;
        this.quote = database.getQuote();
    }


    /**
     * Replaces data in the table for the specified columns.
     *
     * @param callback a consumer to define the column-value pairs.
     * @return a {@link SqlQueryPair} containing the generated SQL command and associated values.
     */
    public SqlQueryPair replaceIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.replaceInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Inserts data into the table for the specified columns.
     *
     * @param callback a consumer to define the column-value pairs.
     * @return a {@link SqlQueryPair} containing the generated SQL command and associated values.
     */
    public SqlQueryPair insertIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.insertInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Merges data into the table for the specified columns.
     * <p>This works similarly to <code>REPLACE INTO</code> but is often used for databases
     * that do not support that command (e.g., H2 database).</p>
     *
     * @param callback a consumer to define the column-value pairs.
     * @return a {@link SqlQueryPair} containing the generated SQL command and associated values.
     */
    public SqlQueryPair mergeIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.mergeInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Updates data in the database using the provided record values for the specified columns.
     * This will update any existing values for the columns you've added.
     *
     * @param callback    The record or data structure containing the primary key and values to update.
     * @param whereClause The conditions used to filter which row(s) should be updated.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair updateTable(@Nonnull final Consumer<UpdateBuilder> callback, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.update(this.tableName, callback).getSelector().where(whereClause);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }


    /**
     * Select specific table.
     *
     * @param callback          the columns to select or set to '*' get all columns, but that have performance penalty compare to type all columns.
     * @param queryPlaceholders set this to {@code false} if you not want a prepared statement.
     * @param whereClause       The conditions used to filter which row(s) should be selected.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManager> callback, final boolean queryPlaceholders, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManager columnManger = new ColumnManager();
        callback.accept(columnManger);
        queryBuilder.setGlobalEnableQueryPlaceholders(queryPlaceholders);
        queryBuilder.select(columnManger).from(this.tableName).where(whereClause);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }


    /**
     * Select specific table.
     *
     * @param callback    the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @param whereClause what conditions you want to filter your rows from.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManager> callback, @Nonnull final WhereBuilder whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManager columnManger = new ColumnManager();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(whereClause);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Remove a specific row from the table.
     *
     * @param whereClause the where clause it should remove the row from.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair removeRow(@Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.deleteFrom(this.tableName).where(whereClause);
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Remove this table from the database.
     *
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair dropTable() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.dropTable(this.tableName);

        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Just build and wrap the query for be used in the batch update.
     *
     * @param queryBuilder The built query command.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair wrapQuery(QueryBuilder queryBuilder) {
        return new SqlQueryPair(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Checks whether SQL query values should be replaced with placeholders.
     * <p>
     * When enabled, values are represented by placeholders (e.g., {@code ?})
     * in the generated SQL query, allowing the use of prepared statements for
     * improved security and performance.
     *
     * @return {@code true} if placeholders should be used; {@code false} if actual
     * values should be embedded directly in the query.
     */
    public boolean isQueryPlaceholdersEnabled() {
        return setGlobalEnableQueryPlaceholders;
    }

    /**
     * Sets whether SQL query values should be replaced with placeholders.
     * <p>
     * When enabled, the generated SQL will use placeholders (e.g., {@code ?}) instead
     * of directly embedding the actual values, making it suitable for prepared statements.
     * When disabled, values will be embedded directly into the query, which may be less safe.
     *
     * @param setPlaceholders {@code true} to enable placeholders; {@code false} to insert
     *                        values directly into the SQL query.
     */
    public void setQueryPlaceholders(final boolean setPlaceholders) {
        this.setGlobalEnableQueryPlaceholders = setPlaceholders;
    }


}
