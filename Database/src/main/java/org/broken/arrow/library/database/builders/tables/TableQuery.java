package org.broken.arrow.library.database.builders.tables;

import org.broken.arrow.library.database.builders.wrappers.SqlQuery;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.InsertHandler;
import org.broken.arrow.library.database.construct.query.builder.statement.UpdateBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnManager;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A convenience wrapper around {@link QueryBuilder} for operating on a specific database table.
 * <p>
 * This class provides high-level methods for generating common SQL operations
 * (INSERT, UPDATE, DELETE, SELECT, etc.) bound to a single configured table.
 * </p>
 */
public class TableQuery {
    private final String tableName;
    private boolean setGlobalEnableQueryPlaceholders = true;

    /**
     * Creates a new {@code TableQuery} for the specified table.
     *
     * @param tableName the name of the database table this builder operates on
     */
    public TableQuery(@Nonnull final String tableName) {
        this.tableName = tableName;
    }

    /**
     * Generates a {@code REPLACE INTO} query for the specified columns.
     *
     * @param callback a consumer to define column-value assignments
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery replaceIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.replaceInto(this.tableName, callback);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Generates an {@code INSERT INTO} query for the specified columns.
     *
     * @param callback a consumer to define column-value assignments
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery insertIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.insertInto(this.tableName, callback);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Generates a {@code MERGE INTO} query for the specified columns.
     * <p>
     * This works similarly to {@code REPLACE INTO} but is tailored for databases
     * that do not support that command directly (e.g., H2 database).
     * </p>
     *
     * @param callback a consumer to define column-value assignments
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery mergeIntoTable(@Nonnull final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.mergeInto(this.tableName, callback);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Generates an {@code UPDATE} query using the provided column values and filtering conditions.
     *
     * @param callback    a consumer to define the column-value assignments to update
     * @param whereClause a function defining the conditions used to filter which row(s) to update
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery updateTable(@Nonnull final Consumer<UpdateBuilder> callback, @Nonnull final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.update(this.tableName, callback).getSelector().where(whereClause);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }


    /**
     * Generates a {@code SELECT} query with explicit placeholder options and filtering conditions.
     *
     * @param callback          a consumer to define columns to select (passing {@code *} selects all columns,
     *                          though listing explicit columns is recommended for better performance)
     * @param queryPlaceholders set to {@code false} if you do not want to use prepared statement placeholders
     * @param whereClause       a function defining the conditions used to filter which row(s) to select
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery selectRow(@Nonnull final Consumer<ColumnManager> callback, final boolean queryPlaceholders, @Nonnull final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManager columnManger = new ColumnManager();
        callback.accept(columnManger);
        queryBuilder.setGlobalEnableQueryPlaceholders(queryPlaceholders);
        queryBuilder.select(columnManger).from(this.tableName).where(whereClause);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }


    /**
     * Generates a {@code SELECT} query using default placeholder settings and the specified filtering conditions.
     *
     * @param callback    a consumer to define columns to select (passing {@code *} selects all columns,
     *                    though listing explicit columns is recommended for better performance)
     * @param whereClause the conditions used to filter which row(s) to select
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery selectRow(@Nonnull final Consumer<ColumnManager> callback, @Nonnull final WhereBuilder whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManager columnManger = new ColumnManager();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(whereClause);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Generates a {@code DELETE FROM} query for rows matching the specified conditions.
     *
     * @param whereClause a function defining the conditions used to filter which row(s) to delete
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery removeRow(@Nonnull final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isQueryPlaceholdersEnabled());
        queryBuilder.deleteFrom(this.tableName).where(whereClause);
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Generates a {@code DROP TABLE} query for this table.
     *
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery dropTable() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.dropTable(this.tableName);

        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Wraps a {@link QueryBuilder} instance into a {@link SqlQuery}, typically for use in batch updates.
     *
     * @param queryBuilder the query builder instance to wrap
     * @return a {@link SqlQuery} containing the generated SQL command and associated values
     */
    public SqlQuery wrapQuery(QueryBuilder queryBuilder) {
        return new SqlQuery(queryBuilder, queryBuilder.getValues());
    }

    /**
     * Checks whether SQL query values should be replaced with placeholders.
     * <p>
     * When enabled, values are represented by placeholders (e.g., {@code ?})
     * in the generated SQL query, allowing the use of prepared statements for
     * improved security and performance.
     * </p>
     *
     * @return {@code true} if placeholders should be used; {@code false} if actual
     * values should be embedded directly into the query
     */
    public boolean isQueryPlaceholdersEnabled() {
        return setGlobalEnableQueryPlaceholders;
    }

    /**
     * Sets whether SQL query values should be replaced with placeholders.
     * <p>
     * When enabled, the generated SQL will use placeholders (e.g., {@code ?}) instead
     * of directly embedding actual values, making it suitable for prepared statements.
     * When disabled, values are embedded directly into the query, which may be less secure.
     * </p>
     *
     * @param setPlaceholders {@code true} to enable placeholders; {@code false} to insert
     *                        values directly into the SQL query
     */
    public void setQueryPlaceholders(final boolean setPlaceholders) {
        this.setGlobalEnableQueryPlaceholders = setPlaceholders;
    }


}
