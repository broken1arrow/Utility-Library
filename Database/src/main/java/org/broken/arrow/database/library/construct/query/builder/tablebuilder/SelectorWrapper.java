package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;

public class SelectorWrapper extends Selector<ColumnBuilder<Column, SelectorWrapper>, Column> {
    private final CreateTableHandler createTableHandler;

    public SelectorWrapper(CreateTableHandler createTableHandler) {
        super(new ColumnBuilder<>());
        this.createTableHandler = createTableHandler;
    }
    public SelectorWrapper select(ColumnManger column) {
        super.select(tableColumn -> tableColumn.addAll(column.getColumnsBuilt()));
        return this;
    }

    @Override
    public SelectorWrapper from(String table) {
        super.from(table);
        return this;
    }

    @Override
    public BuildTable where(WhereBuilder whereBuilder) {
        super.where(whereBuilder);
        return new BuildTable(this.createTableHandler);
    }

    public Selector<ColumnBuilder<Column, SelectorWrapper>, Column> getSelector() {
        return this;
    }


}