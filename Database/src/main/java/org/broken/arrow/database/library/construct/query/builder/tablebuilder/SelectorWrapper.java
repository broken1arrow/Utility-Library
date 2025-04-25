package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;

import java.util.function.Function;

public class SelectorWrapper extends Selector<ColumnBuilder<Column, SelectorWrapper>, Column> {
    private final CreateTableHandler createTableHandler;
    private final QueryBuilder queryBuilder;

    public SelectorWrapper(CreateTableHandler createTableHandler, QueryBuilder queryBuilder) {
        super(new ColumnBuilder<>(), queryBuilder);
        this.createTableHandler = createTableHandler;
        this.queryBuilder = queryBuilder;
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
    public BuildTable where(Function<WhereBuilder, LogicalOperator<WhereBuilder>> callback) {
        super.where(callback);
        return new BuildTable(this.createTableHandler,queryBuilder);
    }

/*
    @Override
    public BuildTable where(Function<WhereBuilder, LogicalOperator<Column>> whereBuilder) {
        super.where(whereBuilder);
        return new BuildTable(this.createTableHandler,queryBuilder);
    }
*/

    public Selector<ColumnBuilder<Column, SelectorWrapper>, Column> getSelector() {
        return this;
    }


}