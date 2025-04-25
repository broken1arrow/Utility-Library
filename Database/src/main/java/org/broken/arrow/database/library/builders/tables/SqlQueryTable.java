package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SqlQueryTable {
    private final QueryBuilder queryBuilder;
    private final CreateTableHandler tableHandler;


    public SqlQueryTable(@Nonnull final Function<QueryBuilder, CreateTableHandler> callback) {
        this.queryBuilder = new QueryBuilder();
        this.tableHandler = callback.apply(queryBuilder);
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    @Nonnull
    public CreateTableHandler getTable() {
        return tableHandler;
    }

    @Nullable
    public LogicalOperator<WhereBuilder> createWhereClauseFromPrimaryColumns(@Nonnull final WhereBuilder whereBuilder, final Object... values) {
        int index = 0;
        LogicalOperator<WhereBuilder> logicalOperator = null;
        for (Column primaryColumns : this.getTable().getPrimaryColumns()) {
            if (values.length < index + 1)
                break;
            if (values.length > index + 1 && index + 1 < this.getTable().getPrimaryColumns().size())
                whereBuilder.where(primaryColumns.getColumnName()).equal(values[index]).and();
            else
                logicalOperator = whereBuilder.where(primaryColumns.getColumnName()).equal(values[index]);
            index++;
        }
        return logicalOperator;
    }

    public List<Column> getPrimaryColumns() {
        return this.getTable().getPrimaryColumns();
    }

    @Nonnull
    public String getTableName() {
        return getQueryBuilder().getTableName();
    }

    /**
     * Select specific table.
     *
     * @return the constructed SQL command for get the table.
     */
    public String selectTable() {
        QueryBuilder selectTableBuilder = new QueryBuilder();
        selectTableBuilder.select(this.getTable().getColumns()).from(this.getQueryBuilder().getTableName());
        return selectTableBuilder.build();
    }

    /**
     * This will build the table query with columns,data type and primary key you have set.
     *
     * @return string with prepared query to run on your database.
     */
    public String createTable() {
        return queryBuilder.build();
    }

    @Override
    public String toString() {
        return "SqlQueryTable{" +
                "queryBuilder=" + queryBuilder +
                ", tableHandler=" + tableHandler +
                '}';
    }

}
