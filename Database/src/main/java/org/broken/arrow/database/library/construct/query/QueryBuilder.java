package org.broken.arrow.database.library.construct.query;


import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.InsertData;
import org.broken.arrow.database.library.construct.query.builder.QueryRemover;
import org.broken.arrow.database.library.construct.query.builder.UpdateBuilder;
import org.broken.arrow.database.library.construct.query.builder.WithManger;
import org.broken.arrow.database.library.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.QueryType;
import org.broken.arrow.database.library.construct.query.utlity.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QueryBuilder {
  private final UpdateBuilder updateBuilder = new UpdateBuilder();
  private final InsertData insertData = new InsertData();
  private final QueryModifier queryModifier = new QueryModifier();
  private final CreateTableHandler createTableHandler = new CreateTableHandler();
  private final QueryRemover queryRemover = new QueryRemover();
  private final WithManger withManger = new WithManger();
  private QueryType queryType;
  private String table;

  public CreateTableHandler createTable(String table) {
    this.queryType = QueryType.CREATE;
    this.table = table;
    return createTableHandler;
  }

  public CreateTableHandler createTableIfNotExists(String table) {
    this.queryType = QueryType.CREATE_IF_NOT_EXISTS;
    this.table = table;
    return createTableHandler;
  }

  public CreateTableHandler dropTable(String table) {
    this.queryType = QueryType.DROP;
    this.table = table;
    return createTableHandler;
  }

  public QueryModifier select(ColumnManger column) {
    this.queryType = QueryType.SELECT;
    queryModifier.select(selectBuilder -> selectBuilder.addAll(column.getColumnsBuilt()));
    return queryModifier;
  }

  public QueryModifier select(List<Column> column) {
    this.queryType = QueryType.SELECT;
    queryModifier.select(selectBuilder -> selectBuilder.addAll(column));
    return queryModifier;
  }

  public UpdateBuilder update(String table, Consumer<UpdateBuilder> callback) {
    callback.accept(updateBuilder);
    this.queryType = QueryType.UPDATE;
    this.table = table;
    return updateBuilder;
  }

  public UpdateBuilder update(String table) {
    this.queryType = QueryType.UPDATE;
    this.table = table;
    return this.updateBuilder;
  }

  public void insertInto(String table, Consumer<InsertData> callback) {
    callback.accept(insertData);
    this.queryType = QueryType.INSERT;
    this.table = table;
  }

  public QueryRemover deleteFrom(String table) {
    this.queryType = QueryType.DELETE;
    this.table = table;
    return queryRemover;
  }

  public WithManger with(Consumer<WithManger> callback) {
    this.queryType = QueryType.WITH;
    callback.accept(withManger);
    return withManger;
  }

  // ---- Query Build ----
  public String build() {
    final QueryModifier queryModifier = this.queryModifier;
    if (queryType == null) {
      throw new IllegalStateException("Query type must be set before building.");
    }

    StringBuilder sql = new StringBuilder();
    switch (queryType) {
      case SELECT:
        sql.append("SELECT ")
                .append(queryModifier.getSelectBuilder().getColumns().isEmpty() ? "*" : queryModifier.getSelectBuilder().build())
                .append(" FROM ").append(queryModifier.getTableWithAlias());

        sql.append(queryModifier.getJoinBuilder().build())
                .append(queryModifier.getWhereBuilder().build())
                .append(queryModifier.getGroupByBuilder().build())
                .append(queryModifier.getHavingBuilder().build())
                .append(queryModifier.getOrderByBuilder().build());
        break;
      case DELETE:
        sql.append("DELETE FROM ").append(table);
        sql.append(this.queryRemover.getWhereBuilder().build());
        break;

      case DROP:
        sql.append("DROP TABLE ").append(table);
        break;

      case CREATE:
        sql.append("CREATE TABLE ").append(table).append(this.createTableHandler.build());
        break;

      case CREATE_IF_NOT_EXISTS:
        sql.append("CREATE TABLE IF NOT EXISTS ").append(table);
        break;
      case UPDATE:
        Map<String, Object> updateValues = updateBuilder.build();
        if (updateValues.isEmpty()) {
          throw new IllegalStateException("UPDATE queries require at least one SET value.");
        }
        sql.append("UPDATE ").append(table).append(" SET ");
        sql.append(updateValues.entrySet().stream()
                .map(entry -> entry.getKey() + " = ?")
                .collect(Collectors.joining(", ")));
        sql.append(updateBuilder.getSelector().getWhereBuilder().build());
        break;

      case INSERT:
        sql.append("INSERT INTO ").append(table)
                .append("(")
                .append(insertData.getInsertValues().stream().map(InsertBuilder::getColumnName).collect(Collectors.joining(", ")))
                .append(")")
                .append(" VALUES (")
                .append(StringUtil.repeat("?,", (insertData.getInsertValues().size())).replaceAll(",$", ""))
                .append(")");
        break;
      case WITH:
        sql.append(withManger.build());
        break;
    }
    return sql + ";";
  }

  public List<Object> getValues() {

    List<Object> values = new ArrayList<>();
    if (queryType == QueryType.UPDATE) {
      Map<Integer, Object> updateValues = updateBuilder.getIndexedValues();
      values.addAll(updateValues.values());
    } else if (queryType == QueryType.INSERT) {
      values.addAll(insertData.getInsertValues().stream().map(InsertBuilder::getColumnValue).collect(Collectors.toList()));
    } else if (queryType == QueryType.SELECT) {
      values.addAll(queryModifier.getWhereBuilder().getValues());
      values.addAll(queryModifier.getHavingBuilder().getValues());
    } else if (queryType == QueryType.DELETE) {
      values.addAll(queryRemover.getWhereBuilder().getValues());
    } else if (queryType == QueryType.WITH) {
      values.add("NoN");
    }
    return values;
  }

  public int getAmountColumnsSet() {

    if (queryType == QueryType.UPDATE) {
      return updateBuilder.getSelector().getSelectBuilder().getColumns().size();
    } else if (queryType == QueryType.INSERT) {
      return insertData.getInsertValues().size();
    } else if (queryType == QueryType.SELECT) {
      return queryModifier.getSelectBuilder().getColumns().size();
    } else if (queryType == QueryType.DELETE) {
      return -1;
    } else {
    }
    return -1;
  }


}