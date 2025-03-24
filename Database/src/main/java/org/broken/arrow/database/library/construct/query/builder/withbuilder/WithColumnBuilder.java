package org.broken.arrow.database.library.construct.query.builder.withbuilder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

public class WithColumnBuilder extends ColumnBuilder<Column, FromWrapper> {

    private final FromWrapper fromWrapper;

    public WithColumnBuilder(WithBuilder withBuilder, QueryBuilder query) {
        this.fromWrapper = new FromWrapper(this, withBuilder, query);
    }

    @Override
    public FromWrapper add(Column column) {
        super.add(column);
        return this.fromWrapper;
    }

    public FromWrapper getFromWrapper() {
        return this.fromWrapper;
    }

}