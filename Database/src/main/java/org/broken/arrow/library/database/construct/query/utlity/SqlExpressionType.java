package org.broken.arrow.library.database.construct.query.utlity;

public enum SqlExpressionType {
  LIKE,
  AS;

  @Override
  public String toString() {
    return this.name();
  }

}
