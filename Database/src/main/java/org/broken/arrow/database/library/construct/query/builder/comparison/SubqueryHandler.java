package org.broken.arrow.database.library.construct.query.builder.comparison;

import org.broken.arrow.database.library.construct.query.QueryBuilder;

public class SubqueryHandler<T> extends ComparisonHandler<T> {

  private final QueryBuilder subquery;

  public SubqueryHandler(QueryBuilder subquery) {
    this.subquery = subquery;
  }

  public QueryBuilder getSubquery() {
    return subquery;
  }

  @Override
  public String toString() {
    return  "(" + subquery.build() + ")";
  }
}
