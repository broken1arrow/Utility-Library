package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.InsertHandler;
import org.broken.arrow.database.library.construct.query.builder.UpdateBuilder;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.utility.SqlFunction;

import javax.annotation.Nonnull;
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
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair mergeIntoTable(final Consumer<InsertHandler> callback) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.mergeInto(this.tableName, callback);
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Update data in your database from the provided record value, on columns you added. Will update old data on columns you added.
     *
     * @param callback the record for the primary key you want to update.
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair updateTable(final Consumer<UpdateBuilder> callback, SqlFunction<WhereBuilder> where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setGlobalEnableQueryPlaceholders(this.isSetQueryPlaceholders());
        queryBuilder.update(this.tableName, callback).getSelector().where(where.apply(WhereBuilder.of(this.isSetQueryPlaceholders())));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Select specific table.
     *
     * @param callback the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair selectRow(@Nonnull final Consumer<ColumnManger> callback, @Nonnull final SqlFunction<WhereBuilder> where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        ColumnManger columnManger = new ColumnManger();
        callback.accept(columnManger);

        queryBuilder.select(columnManger).from(this.tableName).where(where.apply(WhereBuilder.of()));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Select specific table.
     *
     * @param callback the columns to select or set to '*' get all columns, but that have performance penalty compere to type all columns.
     * @return the command and also values with help of a wrapper class.
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
     * @return the command and also values with help of a wrapper class.
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
     * @return the command and also values with help of a wrapper class.
     */
    public SqlQueryPair removeRow(@Nonnull final SqlFunction<WhereBuilder> where) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.deleteFrom(this.tableName).where(where.apply(WhereBuilder.of()));
        return new SqlQueryPair(queryBuilder.build(), queryBuilder.getValues());
    }

    /**
     * Remove this table from the database.
     *
     * @return the command and also values with help of a wrapper class.
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
