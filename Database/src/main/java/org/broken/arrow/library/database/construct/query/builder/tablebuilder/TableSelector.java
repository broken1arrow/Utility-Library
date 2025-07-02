package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.TableColumnCache;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TableSelector extends Selector<TableColumnCache, Column> {
  private final TableColumnCache tablesColumnsBuilder;

  public TableSelector(QueryBuilder queryBuilder, @Nonnull final TableColumnCache tableColumnCache )  {
    super(tableColumnCache, queryBuilder);
    this.tablesColumnsBuilder = tableColumnCache;
  }

  @Override
  public Selector<TableColumnCache, Column> select(Consumer<TableColumnCache> callback) {
    callback.accept(tablesColumnsBuilder);
    return this;
  }

  public TableColumnCache getTablesColumnsBuilder() {
    return tablesColumnsBuilder;
  }
}
