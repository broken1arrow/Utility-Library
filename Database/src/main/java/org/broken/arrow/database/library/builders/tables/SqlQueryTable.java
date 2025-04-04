package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.CreateTableHandler;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class SqlQueryTable {
    private final QueryBuilder queryBuilder;
    private final CreateTableHandler tableHandler;


    public SqlQueryTable(@Nonnull final Function<QueryBuilder, CreateTableHandler> callback) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        this.queryBuilder = queryBuilder;
        this.tableHandler = callback.apply(queryBuilder);
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    @Nonnull
    public CreateTableHandler getTable() {
        return tableHandler;
    }

    @Nonnull
    public WhereBuilder createWhereClauseFromPrimaryColumns(final boolean isPreparedStatement, Object... values) {
        WhereBuilder whereBuilder = WhereBuilder.of(isPreparedStatement);
        int index = 0;
        for (Column primaryColumns : this.getTable().getPrimaryColumns()) {
            if (values.length < index + 1)
                break;
            if (values.length > index + 1 && index + 1 < this.getTable().getPrimaryColumns().size())
                whereBuilder.where(primaryColumns.getColumnName()).equal(values[index]).and();
            else
                whereBuilder.where(primaryColumns.getColumnName()).equal(values[index]);
            index++;
        }
        return whereBuilder;
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
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.select(this.getTable().getColumns()).from(this.getQueryBuilder().getTableName());
        return queryBuilder.build();
    }

    /**
     * This will build the table query with columns,data type and primary key you have set.
     *
     * @return string with prepared query to run on your database.
     */
    public String createTable() {
        return queryBuilder.build();
    }

}
