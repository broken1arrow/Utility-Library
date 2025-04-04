package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.DataType;

import java.util.StringJoiner;

public class TableColumn extends Column {

    private final DataType dataType;
    private final SQLConstraints[] constraints;
    private final ColumnManger columnManger;
    private final boolean isPrimaryKey;

    public TableColumn(ColumnManger columnManger, String columnName, final DataType dataType, final SQLConstraints... constraints) {
        super( columnName, "");
        this.dataType = dataType;
        this.constraints = constraints;
        this.columnManger = columnManger;
        this.isPrimaryKey = containsPrimaryKey(constraints);
    }


    public SQLConstraints[] getConstraints() {
        return constraints;
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    private boolean containsPrimaryKey(SQLConstraints... constraints) {
        if (constraints == null) return false;
        for (SQLConstraints constraint : constraints) {
            if (SQLConstraints.isPrimary(constraint)) {
                return true;
            }
        }
        return false;
    }

    public String build(){
        StringJoiner joiner = new StringJoiner(", ");
        for (SQLConstraints constraints: constraints){
            joiner.add(constraints.toString());
        }

        return this.getColumnName() + " " + dataType.getValue() + " " + joiner;
    }

    public static class Separator {
        private final TableColumn column;

        public Separator(TableColumn column) {
            this.column = column;
            this.column.columnManger.add(column);
        }

        public Separator column(final String communeName, final DataType datatype, final SQLConstraints... constraints) {
            return new Separator(new TableColumn(this.column.columnManger, communeName, datatype, constraints));
        }

        public ColumnManger build() {
            return this.column.columnManger;
        }

    }

}
