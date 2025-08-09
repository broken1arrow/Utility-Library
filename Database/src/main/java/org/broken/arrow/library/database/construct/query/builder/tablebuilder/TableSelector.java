package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.TableColumnCache;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
/**
 * A selector class specialized for selecting columns from a table,
 * extending the generic {@link Selector} class with a {@link TableColumnCache}
 * as the builder and {@link Column} as the element type.
 */
public class TableSelector extends Selector<TableColumnCache, Column> {
  private final TableColumnCache tablesColumnsBuilder;

  /**
   * Constructs a new {@code TableSelector} with the provided {@link QueryBuilder}
   * and {@link TableColumnCache} instance.
   *
   * @param queryBuilder       the {@link QueryBuilder} associated with this selector
   * @param tableColumnCache   the cache of table columns to build selection from
   */
  public TableSelector(QueryBuilder queryBuilder, @Nonnull final TableColumnCache tableColumnCache )  {
    super(tableColumnCache, queryBuilder);
    this.tablesColumnsBuilder = tableColumnCache;
  }

  /**
   * Applies the given callback to the internal {@link TableColumnCache} builder
   * to customize column selection.
   *
   * @param callback a consumer that accepts the internal {@link TableColumnCache}
   * @return this {@code TableSelector} instance for method chaining
   */
  @Override
  public Selector<TableColumnCache, Column> select(Consumer<TableColumnCache> callback) {
    callback.accept(tablesColumnsBuilder);
    return this;
  }

  /**
   * Returns the internal {@link TableColumnCache} used for building column selections.
   *
   * @return the {@link TableColumnCache} instance
   */
  public TableColumnCache getTablesColumnsBuilder() {
    return tablesColumnsBuilder;
  }
}
