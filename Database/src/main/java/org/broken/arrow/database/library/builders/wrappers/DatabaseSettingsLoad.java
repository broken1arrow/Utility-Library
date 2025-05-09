package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseSettingsLoad extends DatabaseSettings {

    private QueryBuilder queryBuilder;
    public DatabaseSettingsLoad(@Nonnull String tableName) {
        super(tableName);
    }

    public void setSelectCommand(@Nonnull final Consumer<ColumnManger> columns, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause){
        this.setSelectCommand(true,columns, whereClause);
    }

    public void setSelectCommand(final boolean queryPlaceholder, @Nonnull final Consumer<ColumnManger> columns, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause){
        QueryBuilder builder = new QueryBuilder();
        ColumnManger columnManger = new ColumnManger();
        columns.accept(columnManger);
        builder.setGlobalEnableQueryPlaceholders(queryPlaceholder);

        builder.select(columnManger).from(this.getTableName()).where(whereClause);
        this.queryBuilder = builder;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }


}
