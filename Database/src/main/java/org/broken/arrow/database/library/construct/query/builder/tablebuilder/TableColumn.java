package org.broken.arrow.database.library.construct.query.builder.tablebuilder;

import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.DataType;

public class TableColumn extends Column {

  private final DataType dataType;
  private final SQLConstraints[] constraints;
  private final ColumnManger columnManger;

  public TableColumn(ColumnManger columnManger, String columnName, final DataType dataType, final SQLConstraints... constraints) {
    super(columnManger, columnName, "");
    this.dataType = dataType;
    this.constraints = constraints;
    this.columnManger = columnManger;
  }

  public Separator of(final String communeName, final DataType datatype, final SQLConstraints... constraints) {
    return new Separator(new TableColumn(columnManger, communeName, datatype, constraints));
  }


  public SQLConstraints[] getConstraints() {
    return constraints;
  }

  public DataType getDataType() {
    return dataType;
  }

  public static class Separator {
    private final TableColumn column;

    public Separator(TableColumn column) {
      this.column = column;
      this.column.columnManger.add(column);
    }

    public TableColumn next() {
      return this.column;
    }

    public TableColumn build() {
      return this.column;
    }

  }

}
