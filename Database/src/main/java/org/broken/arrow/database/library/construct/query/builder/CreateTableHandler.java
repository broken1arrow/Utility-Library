package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.SelectorWrapper;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableSelector;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableSelectorWrapper;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.database.library.construct.query.utlity.SqlExpressionType;

import java.util.List;

public class CreateTableHandler {
    private SqlExpressionType copyMethod = null;
    private TableSelectorWrapper selectorWrapper;
    private SelectorWrapper selector;

    public SelectorWrapper as() {
        copyMethod = SqlExpressionType.AS;
        selector = new SelectorWrapper(this, new Selector<>(new ColumnBuilder<>()));
        return selector;
    }

    public SelectorWrapper like() {
        copyMethod = SqlExpressionType.LIKE;
        selector = new SelectorWrapper(this, new Selector<>(new ColumnBuilder<>()));
        return selector;
    }

    public CreateTableHandler select(TableColumn column) {
        selectorWrapper = new TableSelectorWrapper(this, new TableSelector(new TableColumnCache()));
        selectorWrapper.select(column);
        return this;
    }
    public CreateTableHandler select(List<TableColumn> column) {
        selectorWrapper = new TableSelectorWrapper(this, new TableSelector(new TableColumnCache()));
        selectorWrapper.select(column);
        return this;
    }

    public SqlExpressionType getCopyMethod() {
        return copyMethod;
    }

    public String build() {
        StringBuilder sql = new StringBuilder();

        Selector<?> selectorWrapper = this.selector != null ? this.selector.build() : null;
        if (this.selectorWrapper != null)
            selectorWrapper = this.selectorWrapper.build();

        if (copyMethod != null && selectorWrapper != null) {
            sql.append(" ")
                    .append(this.getCopyMethod().toString())
                    .append(" ")
                    .append("SELECT")
                    .append(" ")
                    .append(selectorWrapper.getSelectBuilder().build())
                    .append(" ")
                    .append("FROM")
                    .append(" ")
                    .append(selectorWrapper.getTableWithAlias())
                    .append(selectorWrapper.getWhereBuilder().build());


            return sql + "";

        }
        if (selectorWrapper != null) {
            sql.append(" (");
            sql.append(selectorWrapper.getSelectBuilder().build());
            sql.append(")");
        }

        return sql + "";
    }
}
