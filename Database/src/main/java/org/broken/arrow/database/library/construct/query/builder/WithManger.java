package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.withbuilder.FromWrapper;
import org.broken.arrow.database.library.construct.query.builder.withbuilder.WithBuilder;

import java.util.ArrayList;
import java.util.List;

public class WithManger {

  private final List<WithBuilder> buildersList = new ArrayList<>();
  private boolean union;

  public WithManger(QueryBuilder queryBuilder) {

  }

    public WithBuilder as(String aliasName) {
    WithBuilder withBuilder = new WithBuilder(aliasName);
    buildersList.add(withBuilder);
    return withBuilder;
  }

  public void setUnion(boolean union) {
    this.union = union;
  }

  public String build() {
    final StringBuilder buildSQLQuery = new StringBuilder();
    final List<String> cteQueries = new ArrayList<>();
    final List<String> finalSelects = new ArrayList<>();
    buildSQLQuery.append("WITH ");
    for (WithBuilder query : buildersList) {
      FromWrapper fromWrapper = query.getFromWrapper();
      if (fromWrapper != null) {
        cteQueries.add(fromWrapper.getWithCommandBuilder() + "");
        finalSelects.add(fromWrapper.getFromClaus() + "");
      }
    }
    buildSQLQuery.append(String.join(", ", cteQueries));

    buildSQLQuery.append(" ");
    if (union) {
      buildSQLQuery.append(String.join(" UNION ALL ", finalSelects));
    } else {
      buildSQLQuery.append(String.join(" ", finalSelects));
    }

    return buildSQLQuery + "";
  }

}
