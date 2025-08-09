package org.broken.arrow.library.database.construct.query.builder.comparison;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
/**
 * Handler class for subqueries used within comparisons.
 * Extends {@link ComparisonHandler} to wrap a {@link QueryBuilder}
 * representing the subquery.
 *
 * @param <T> the type parameter inherited from ComparisonHandler
 */
public class SubqueryHandler<T> extends ComparisonHandler<T> {

  private final QueryBuilder subquery;

  /**
   * Constructs a new {@code SubqueryHandler} wrapping the given subquery.
   *
   * @param subquery the {@link QueryBuilder} representing the subquery
   */
  public SubqueryHandler(QueryBuilder subquery) {
    this.subquery = subquery;
  }

  /**
   * Returns the {@link QueryBuilder} representing the subquery.
   *
   * @return the subquery builder
   */
  public QueryBuilder getSubquery() {
    return subquery;
  }

  /**
   * Returns the string representation of the subquery enclosed in parentheses.
   *
   * @return the subquery as a string wrapped in parentheses
   */
  @Override
  public String toString() {
    return  "(" + subquery.build() + ")";
  }
}
