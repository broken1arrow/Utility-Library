package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

public  class SelectorWrapper {
    private final CreateTableHandler createTableHandler;
    private Selector<ColumnBuilder<Column,Void>> selector;

    public SelectorWrapper(CreateTableHandler createTableHandler, Selector<ColumnBuilder<Column,Void>> selector) {
      this.createTableHandler = createTableHandler;
      this.selector = selector;
    }

    public SelectorWrapper select(Column columnsBuilder) {
      selector = new Selector<>(new ColumnBuilder<>());
      selector.select(tablesColumns -> tablesColumns.add(columnsBuilder));
      return this;
    }

    public Selector<ColumnBuilder<Column,Void>> build() {
      return selector;
    }
  }