package org.broken.arrow.database.library.construct.query.utlity;

public enum SqlExpressionType {
  LIKE,
  AS;

  @Override
  public String toString() {
    return this.name();
  }

}
