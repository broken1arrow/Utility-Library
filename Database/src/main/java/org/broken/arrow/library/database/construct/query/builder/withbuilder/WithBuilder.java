package org.broken.arrow.library.database.construct.query.builder.withbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

public class WithBuilder {
    private final String aliasName;
    private ColumnBuilder<Column, Void> columnBuilder;
    private FromWrapper fromWrapper;

    public WithBuilder(String aliasName) {
        this.aliasName = aliasName;
    }

    public WithBuilder select(Column column) {
        columnBuilder = new ColumnBuilder<>();
        columnBuilder.add(column);
        return this;
    }

    public WithColumnBuilder query(QueryBuilder query) {
        WithColumnBuilder withColumnBuilder = new WithColumnBuilder(this, query);
        this.fromWrapper = withColumnBuilder.getFromWrapper();
        return withColumnBuilder;
    }

    public FromWrapper getFromWrapper() {
        return fromWrapper;
    }

    public String getAliasName() {
        return aliasName;
    }

    public ColumnBuilder<Column, Void> getSelectBuilder() {
        return columnBuilder;
    }
}