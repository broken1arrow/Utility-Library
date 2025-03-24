package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

public class TableColumnCache extends ColumnBuilder<TableColumn, Void> {


    public TableColumnCache() {

    }

    @Override
    public final Void add(TableColumn column) {
        super.add(column);
        return null;
    }


}
