package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class AlterTable {

    public List<String> columns = new ArrayList<>();


    public AlterTable(@Nonnull final QueryBuilder queryBuilder) {
    }


    public AlterTable add(String columnName, final DataType dataType) {
        TableColumn tableColumn = new TableColumn(null, columnName, dataType);
        this.add(tableColumn);
        return this;
    }

    public AlterTable add(Column column) {
        if (!(column instanceof TableColumn))  return this;

        final TableColumn tableColumn = (TableColumn) column;
        columns.add("ADD COLUMN " + tableColumn.build().trim());
        return this;
    }

    public AlterTable drop(Column column) {
        if (!(column instanceof TableColumn))  return this;

        final TableColumn tableColumn = (TableColumn) column;
        columns.add("DROP COLUMN " + tableColumn.getColumnName());
        return this;
    }

    public String build() {
        StringJoiner build = new StringJoiner(", ");
        for (String column : this.columns) {
            build.add(column);
        }
        return build + "";
    }
}
