package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.Selector;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.SelectorWrapper;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableSelector;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableSelectorWrapper;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.SqlExpressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateTableHandler {
    private SqlExpressionType copyMethod = null;
    private TableSelectorWrapper selectorWrapper;
    private SelectorWrapper selector;

    public SelectorWrapper as() {
        copyMethod = SqlExpressionType.AS;
        selector = new SelectorWrapper(this);
        return selector;
    }

    public SelectorWrapper like() {
        copyMethod = SqlExpressionType.LIKE;
        selector = new SelectorWrapper(this);
        return selector;
    }

    public CreateTableHandler addColumns(ColumnManger column) {
        selectorWrapper = new TableSelectorWrapper(this, new TableSelector(new TableColumnCache()));
        selectorWrapper.select(column.getColumnsBuilt());
        return this;
    }

    public List<Column> getColumns() {
        if (selectorWrapper != null)
            return selectorWrapper.getTableSelector().getSelectBuilder().getColumns();
        if (selector != null)
            return selector.getSelector().getSelectBuilder().getColumns();
        return new ArrayList<>();
    }
    public List<Column> getPrimaryColumns() {
        if (selectorWrapper != null) {
            return selectorWrapper.getTableSelector().getTablesColumnsBuilder().getColumns().stream()
                    .filter(column -> ((TableColumn)column).isPrimaryKey()).collect(Collectors.toList());
        }
        if (selector != null)
            return selector.getSelector().getSelectBuilder().getColumns().stream()
                    .filter(SQLConstraints::isPrimary).collect(Collectors.toList());
        return new ArrayList<>();
    }
    public SqlExpressionType getCopyMethod() {
        return copyMethod;
    }

    public String build() {
        StringBuilder sql = new StringBuilder();

        Selector<?,?> selectorWrapper = this.selector != null ? this.selector.getSelector() : null;
        if (this.selectorWrapper != null)
            selectorWrapper = this.selectorWrapper.getTableSelector();

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
