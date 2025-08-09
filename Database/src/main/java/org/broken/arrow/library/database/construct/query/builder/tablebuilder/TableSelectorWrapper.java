package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.TableColumnCache;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import java.util.List;
/**
 * Wrapper around {@link TableSelector} that manages the selection process
 * in conjunction with a {@link CreateTableHandler}.
 * <p>
 * Provides convenience methods to reset and customize the columns selection.
 * </p>
 */
public class TableSelectorWrapper {
    private final CreateTableHandler createTableHandler;
    private TableSelector selector;

    /**
     * Constructs a new {@code TableSelectorWrapper} with the given
     * {@link CreateTableHandler} and initial {@link TableSelector}.
     *
     * @param createTableHandler the handler managing table creation logic
     * @param selector           the initial {@link TableSelector} instance
     */
    public TableSelectorWrapper(CreateTableHandler createTableHandler, TableSelector selector) {
      this.createTableHandler = createTableHandler;
      this.selector = selector;
    }

    /**
     * Resets the internal {@link TableSelector} and applies the given list of columns
     * as the new selection.
     *
     * @param columnsBuilder the list of {@link Column} instances to select
     * @return this {@code TableSelectorWrapper} instance for chaining
     */
    public TableSelectorWrapper select(List<Column> columnsBuilder) {
        selector = new TableSelector(this.createTableHandler.getQueryBuilder(), new TableColumnCache());
        selector.select(tablesColumns -> tablesColumns.addAll(columnsBuilder));
        return this;
    }

    /**
     * Returns the current internal {@link TableSelector} instance.
     *
     * @return the {@link TableSelector} being wrapped
     */
    public TableSelector getTableSelector() {
      return selector;
    }

}