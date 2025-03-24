package org.broken.arrow.database.library.construct.query.columnbuilder;

public class HavingSeparator extends Column.Separator{

  public HavingSeparator(Column column) {
    super(column);
    column.getColumnManger().add(column);
  }

  @Override
  public Column next() {
    return null;
  }

}