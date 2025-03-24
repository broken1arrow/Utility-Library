package org.broken.arrow.database.library.construct.query.columnbuilder;

import org.broken.arrow.database.library.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.database.library.construct.query.utlity.DataType;

import java.util.ArrayList;
import java.util.List;

public class ColumnManger {
  private final List<Column> columnsBuilt = new ArrayList<>();

  public ColumnManger() {

  }
  public void add(Column column) {
     columnsBuilt.add(column);
  }
  public List<Column> getColumnsBuilt() {
    return columnsBuilt;
  }

  public static Column.Separator of(String name) {
    return of( name, "") ;
  }

  public static Column.Separator of(String name, String alias) {
    ColumnManger columnManger = new ColumnManger();
    return new Column.Separator(new Column(columnManger, name, alias));
  }
  public static HavingSeparator ofHaving(String name) {
    return  ofHaving( name, "") ;
  }
  public static HavingSeparator ofHaving(String name, String alias) {
    ColumnManger columnManger = new ColumnManger();
    return new HavingSeparator(new Column(columnManger, name, alias));
  }

  public static TableColumn.Separator tableOf(final String communeName, final DataType datatype, final SQLConstraints... constraints) {
    ColumnManger columnManger = new ColumnManger();
    return new TableColumn.Separator(new TableColumn(columnManger, communeName, datatype, constraints));
  }

}
