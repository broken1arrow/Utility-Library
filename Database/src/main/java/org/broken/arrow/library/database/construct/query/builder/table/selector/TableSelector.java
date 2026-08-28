package org.broken.arrow.library.database.construct.query.builder.table.selector;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.table.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumnRegistry;
import org.broken.arrow.library.database.construct.query.builder.column.Column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * A selector class specialized for selecting columns from a table,
 * extending the generic {@link Selector} class with a {@link TableColumnRegistry}
 * as the builder and {@link Column} as the element type.
 */
public class TableSelector extends Selector<TableColumnRegistry, TableColumn> {
    private final CreateTableHandler createTableHandler;
    private final TableColumnRegistry tablesColumnsBuilder;

    /**
     * Constructs a new {@code TableSelector} with the provided {@link QueryBuilder}
     * and {@link TableColumnRegistry} instance.
     *
     * @param createTableHandler the handler responsible for managing table creation
     * @param queryBuilder       the {@link QueryBuilder} associated with this selector
     * @param tableColumnBuilder the cache of table columns to build selection from
     */
    public TableSelector(@Nonnull final CreateTableHandler createTableHandler, @Nonnull final QueryBuilder queryBuilder, @Nonnull final TableColumnRegistry tableColumnBuilder) {
        super(tableColumnBuilder, queryBuilder);
        this.createTableHandler = createTableHandler;
        this.tablesColumnsBuilder = tableColumnBuilder;
    }

    /**
     * Applies the given callback to the internal {@link TableColumnRegistry} builder
     * to customize column selection.
     *
     * @param callback a consumer that accepts the internal {@link TableColumnRegistry}
     * @return this {@code TableSelector} instance for method chaining
     */
    @Override
    public Selector<TableColumnRegistry, TableColumn> select(Consumer<TableColumnRegistry> callback) {
        callback.accept(tablesColumnsBuilder);
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
    public TableSelector getTableSelector() {
        return this;
    }

    /**
     * Returns the internal {@link TableColumnRegistry} used for building column selections.
     *
     * @return the {@link TableColumnRegistry} instance
     */
    public TableColumnRegistry getTablesColumnsBuilder() {
        return tablesColumnsBuilder;
    }
}
