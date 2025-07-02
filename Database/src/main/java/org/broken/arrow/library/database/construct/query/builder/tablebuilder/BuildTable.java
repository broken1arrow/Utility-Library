package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;

public class BuildTable extends SelectorWrapper {
    private final CreateTableHandler createTableHandler;

    public BuildTable(CreateTableHandler createTableHandler, QueryBuilder queryBuilder) {
        super(createTableHandler, queryBuilder);
        this.createTableHandler = createTableHandler;
    }

    public CreateTableHandler build() {
        return createTableHandler;
    }
}