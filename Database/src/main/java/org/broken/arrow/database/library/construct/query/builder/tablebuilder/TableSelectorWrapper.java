package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.TableColumnCache;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import java.util.List;

public class TableSelectorWrapper {
    private final CreateTableHandler createTableHandler;
    private TableSelector selector;

    public TableSelectorWrapper(CreateTableHandler createTableHandler, TableSelector selector) {
      this.createTableHandler = createTableHandler;
      this.selector = selector;
    }

/*    public TableSelectorWrapper select(List<Column> columnsBuilder) {
      selector = new TableSelector(new TableColumnCache());
      selector.select(tablesColumns -> tablesColumns.addAll(columnsBuilder));
      return this;
    }*/

    public TableSelectorWrapper select(List<Column> columnsBuilder) {
        selector = new TableSelector(new TableColumnCache());
        selector.select(tablesColumns -> tablesColumns.addAll(columnsBuilder));
        return this;
    }

    public TableSelector getTableSelector() {
      return selector;
    }

}