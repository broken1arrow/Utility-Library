package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.InsertHandler;
import org.broken.arrow.database.library.construct.query.builder.UpdateBuilder;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.utility.SqlFunction;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

public class SqlHandler {
    private final Database database;
    private final char quote;
    private final String tableName;
    private boolean setGlobalEnableQueryPlaceholders = true;

    public SqlHandler(@Nonnull String tableName, @Nonnull final Database database) {
        this.tableName = tableName;
        this.database = database;
        this.quote = database.getQuote();
    }


    /**
     * Replace data in your database, on columns you added.
     *
     * @param callback Consumer where you add your column and value pair.
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair replaceIntoTable(Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.replaceInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Replace data in your database, on columns you added.
     *
     * @param callback Consumer where you add your column and value pair.
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair insertIntoTable(Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.insertInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Replace data in your database, on columns you added. This do same thing as
     * "REPLACE INTO", but this command is often used with a database that not support
     * "REPLACE INTO" for example H2 database.
     *
     * @param callback Consumer where you add your column and value pair.
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair mergeIntoTable(final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.mergeInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Updates data in the database using the provided record values for the specified columns.
     * This will update any existing values for the columns you've added.
     *
     * @param callback     The record or data structure containing the primary key and values to update.
     * @param whereClause  The conditions used to filter which row(s) should be updated.
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair updateTable(final Consumer<UpdateBuilder> callback, SqlFunction<WhereBuilder> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.update(this.tableName, callback).getSelector().where(whereClause.apply(WhereBuilder.of(this.isSetQueryPlaceholders())));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Select specific table.
     *
     * @param callback the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @param whereClause The conditions used to filter which row(s) should be selected.
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManger> callback, @Nonnull final SqlFunction<WhereBuilder> whereClause) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManger columnManger = new ColumnManger();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(whereClause.apply(WhereBuilder.of()));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Select specific table.
     *
     * @param callback the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @param queryPlaceholders set this to {@code false} if you not want a prepared statement.
     * @param where The conditions used to filter which row(s) should be selected.
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManger> callback, final boolean queryPlaceholders, @Nonnull final SqlFunction<WhereBuilder> where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManger columnManger = new ColumnManger();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(where.apply(WhereBuilder.of(queryPlaceholders)));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Select specific table.
     *
     * @param callback the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @param where what conditions you want to filter your rows from.
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManger> callback, @Nonnull final WhereBuilder where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManger columnManger = new ColumnManger();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(where);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Remove a specific row from the table.
     *
     * @param where the where clause it should remove the row from.
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair removeRow(@Nonnull final SqlFunction<WhereBuilder> where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.deleteFrom(this.tableName).where(where.apply(WhereBuilder.of()));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Remove this table from the database.
     *
     * @return A {@link SqlQueryPair#SqlQueryPair(String, Map)} containing the generated SQL command and associated values.
     */
    public SqlQueryPair dropTable() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.dropTable(this.tableName);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    public boolean isSetQueryPlaceholders() {
        return setGlobalEnableQueryPlaceholders;
    }

    public void setSetQueryPlaceholders(boolean setPlaceholders) {
        this.setGlobalEnableQueryPlaceholders = setPlaceholders;
    }
}
