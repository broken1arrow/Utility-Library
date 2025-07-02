package org.broken.arrow.library.database.construct.query.builder;


import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import java.util.StringJoiner;

public class TableColumnCache extends ColumnBuilder<Column, Void> {

    public TableColumnCache() {
        //just empty constructor.
    }

    @Override
    public String build() {
        StringJoiner joiner = new StringJoiner(", ");
        for(Column column : this.getColumns()){
            joiner.add(((TableColumn)column).build());
        }
        return joiner + "";
    }
}
