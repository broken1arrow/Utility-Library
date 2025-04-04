package org.broken.arrow.database.library.construct.query.builder.withbuilder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.logging.library.Logging;

import java.util.logging.Level;

public class FromWrapper {
    private final StringBuilder withCommandBuilder = new StringBuilder();
    private final StringBuilder fromClaus = new StringBuilder();
    private final WithColumnBuilder columnBuilder;
    private final ColumnBuilder<Column, Void> selectWithBuilder;
    private final QueryBuilder query;
    private final String aliasName;
    private final Logging logging = new Logging(FromWrapper.class);
    private String columns;

    public FromWrapper(WithColumnBuilder withColumnBuilder, WithBuilder withBuilder, QueryBuilder query) {
        this.columnBuilder = withColumnBuilder;
        this.query = query;

        this.selectWithBuilder = withBuilder.getSelectBuilder();
        this.aliasName = withBuilder.getAliasName();
    }

    public void from() {
        build();
        StringBuilder fromClaus = this.fromClaus;
        fromClaus.append("SELECT ");
        if (columns != null && !columns.isEmpty()) fromClaus.append(columns);
        else fromClaus.append("*");

        fromClaus.append(" FROM ").append(aliasName);
    }

    public void build() {
        this.columns = columnBuilder.build();
        StringBuilder withCommandBuilder = this.withCommandBuilder;

        withCommandBuilder.append(aliasName).append(" ");

        String build = this.selectWithBuilder.build();
        int size = this.selectWithBuilder.getColumns().size();
        int columnsSet = query.getAmountColumnsSet();
        if (!build.isEmpty() && size == columnsSet)
            withCommandBuilder.append("(")
                    .append(build)
                    .append(") ");
        else if (!build.isEmpty()) {
            this.logging.log(Level.WARNING, () -> Logging.of("The number of columns in '" + aliasName +
                    "' does not match the expected amount. When setting new column names for the WITH clause, the " +
                    "number of columns must be equal. Current number of columns: " + size + ", expected: " + columnsSet));
        }

        withCommandBuilder.append("AS (")
                .append(this.query.build().replace(";", ""))
                .append(")");
    }

    public StringBuilder getWithCommandBuilder() {
        return withCommandBuilder;
    }

    public StringBuilder getFromClaus() {
        return fromClaus;
    }
}