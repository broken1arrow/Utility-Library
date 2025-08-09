package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.core.Database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * Settings for customizing how data is loaded from a SQL database.
 * <p>
 * This class allows you to define which columns to select and optionally apply a WHERE clause
 * when querying data from a database table. It is used in conjunction with the {@link Database#load(String, Class, Consumer)}
 * that support partial or conditional fetching of rows.
 */
public class DatabaseSettingsLoad extends DatabaseSettings {

    private QueryBuilder queryBuilder;

    /**
     * Constructs a new load settings instance for the specified table.
     *
     * @param tableName The name of the table to load data from.
     */
    public DatabaseSettingsLoad(@Nonnull String tableName) {
        super(tableName);
    }

    /**
     * Defines which columns to select when loading data from the database.
     * Use this when you want to load a subset of the columns, instead of selecting all.
     * This method uses placeholders in the query by default.
     *
     * @param columns A consumer that configures the columns to select via {@link ColumnManager}.
     */
    public void setSelectCommand(@Nonnull final Consumer<ColumnManager> columns) {
        this.setSelectCommand(true, columns);
    }

    /**
     * Defines which columns to select when loading data from the database,
     * and optionally whether to use query placeholders.
     *
     * @param queryPlaceholder Whether the query should use placeholders for parameters.
     * @param columns A consumer that configures the columns to select via {@link ColumnManager}.
     */
    public void setSelectCommand(final boolean queryPlaceholder, @Nonnull final Consumer<ColumnManager> columns) {
        this.queryBuilder(queryPlaceholder, columns, null);
    }

    /**
     * Defines both the selected columns and a WHERE clause for loading data.
     * This method uses placeholders in the query by default.
     *
     * @param columns A consumer that configures the columns to select via {@link ColumnManager}.
     * @param whereClause A function that builds the WHERE clause using {@link WhereBuilder}.
     */
    public void setSelectCommand(@Nonnull final Consumer<ColumnManager> columns, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        this.setSelectCommand(true, columns, whereClause);
    }

    /**
     * Fully defines how to select and filter data from the database.
     * You can specify which columns to load, whether to use query placeholders,
     * and define a WHERE clause.
     *
     * @param queryPlaceholder Whether to use placeholders for values in the query.
     * @param columns A consumer that configures the columns to select via {@link ColumnManager}.
     * @param whereClause A function that builds the WHERE clause using {@link WhereBuilder}.
     */
    public void setSelectCommand(final boolean queryPlaceholder, @Nonnull final Consumer<ColumnManager> columns, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        this.queryBuilder(queryPlaceholder, columns, whereClause);
    }

    private void queryBuilder(final boolean queryPlaceholder, @Nonnull final Consumer<ColumnManager> columns, @Nullable final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        QueryBuilder builder = new QueryBuilder();
        ColumnManager columnManager = new ColumnManager();
        columns.accept(columnManager);
        builder.setGlobalEnableQueryPlaceholders(queryPlaceholder);

        if (whereClause != null)
            builder.select(columnManager).from(this.getTableName()).where(whereClause);
        else
            builder.select(columnManager).from(this.getTableName());
        this.queryBuilder = builder;
    }

    /**
     * Returns the current {@link QueryBuilder} used to build the SQL query.
     * <p>
     * You can call {@link QueryBuilder#build()} on it to get the final SQL string.
     *
     * @return the {@code QueryBuilder} instance created for load from database.
     */
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }


}
