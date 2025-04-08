package org.broken.arrow.database.library.construct.query.builder;


import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;

public class TableColumnCache extends ColumnBuilder<Column, Void> {

    public TableColumnCache() {
        //just empty constructor.
    }

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        for(Column column : this.getColumns()){
            builder.append(((TableColumn)column).build());
        }
        return builder + "";
    }
}
