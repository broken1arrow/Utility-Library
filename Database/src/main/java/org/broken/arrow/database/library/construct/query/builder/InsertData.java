package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.builder.insertbuilder.InsertBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertData {

  private final List<InsertBuilder > insertValues = new ArrayList<>();
  public void add(InsertBuilder  value) {
    insertValues.add(value);
  }

  public void addAll(InsertBuilder... values) {
    insertValues.addAll(Arrays.asList(values));
  }

  public List<InsertBuilder> getInsertValues() {
    return insertValues;
  }


}
