package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.TableColumnCache;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TableSelector extends Selector<TableColumnCache, Column> {
  private final TableColumnCache tablesColumnsBuilder;

  public TableSelector(@Nonnull final TableColumnCache tableColumnCache)  {
    super(tableColumnCache);
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
