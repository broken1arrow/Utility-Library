package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;

import java.util.function.Function;
/**
 * Provides methods for selecting columns and applying conditions when
 * constructing a table creation query.
 * <p>
 * This class extends the generic {@link Selector} to support selecting columns
 * and specifying conditions (e.g., WHERE clauses) relevant to table creation.
 * </p>
 * <p>
 * After setting up selections and conditions, the {@link #build()} method
 * can be called to retrieve the associated {@link CreateTableHandler} for further processing.
 * </p>
 */
public class SelectorWrapper extends Selector<ColumnBuilder<Column, SelectorWrapper>, Column> {
    private final CreateTableHandler createTableHandler;

    /**
     * Creates a new SelectorWrapper with the given table handler and query builder.
     *
     * @param createTableHandler the handler responsible for managing table creation
     * @param queryBuilder       the query builder used for constructing SQL queries
     */
    public SelectorWrapper(final CreateTableHandler createTableHandler,final QueryBuilder queryBuilder) {
        super(new ColumnBuilder<>(), queryBuilder);
        this.createTableHandler = createTableHandler;
    }

    /**
     * Adds all columns from the given {@link ColumnManager} to the current selection.
     *
     * @param column the column manager containing columns to select
     * @return this SelectorWrapper instance for method chaining
     */
    public SelectorWrapper select(final ColumnManager column) {
        super.select(tableColumn -> tableColumn.addAll(column.getColumnsBuilt()));
        return this;
    }

    /**
     * Specifies the table to select from.
     *
     * @param table the table name
     * @return this SelectorWrapper instance for method chaining
     */
    @Override
    public SelectorWrapper from(final String table) {
        super.from(table);
        return this;
    }

    /**
     * Applies a WHERE condition to the query using the provided callback.
     *
     * @param callback a function to build the WHERE clause
     * @return this SelectorWrapper instance for method chaining; after calling this,
     *         {@link #build()} can be invoked to obtain the create table handler.
     */
    @Override
    public SelectorWrapper where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> callback) {
        super.where(callback);
        return this;
    }

    /**
     * Returns the {@link CreateTableHandler} associated with this builder.
     *
     * @return the CreateTableHandler responsible for executing the table creation
     */
    public CreateTableHandler build() {
        return createTableHandler;
    }

    /**
     * Returns this Selector instance for further query building or inspection.
     *
     * @return this SelectorWrapper instance as a Selector
     */
    public Selector<ColumnBuilder<Column, SelectorWrapper>, Column> getSelector() {
        return this;
    }

}