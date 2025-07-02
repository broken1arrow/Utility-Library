package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import java.util.ArrayList;
import java.util.List;

public class ColumnManger {
    private final List<Column> columnsBuilt = new ArrayList<>();

    public ColumnManger() {
        //just a constructor
    }

    public static ColumnManger of() {
        return new ColumnManger();
    }

    public static TableColumn.Separator tableOf(final String communeName, final DataType datatype, final SQLConstraints... constraints) {
        final ColumnManger columnManger = new ColumnManger();
        return new TableColumn.Separator(new TableColumn(columnManger, communeName, datatype, constraints));
    }

    public Aggregation column(String name) {
        return column(name, "");
    }

    public Aggregation column(String name, String alias) {
        return new Aggregation(this, name, alias);
    }

    public void add(Column column) {
        columnsBuilt.add(column);
    }

    public void addAll(List<Column> columns) {
        columnsBuilt.addAll(columns);
    }

    public List<Column> getColumnsBuilt() {
        return columnsBuilt;
    }


}
