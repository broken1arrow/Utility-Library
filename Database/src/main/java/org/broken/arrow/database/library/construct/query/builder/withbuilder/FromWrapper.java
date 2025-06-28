package org.broken.arrow.database.library.construct.query.builder.withbuilder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.logging.library.Logging;

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
        StringBuilder claus = this.fromClaus;
        claus.append("SELECT ");
        if (columns != null && !columns.isEmpty()) claus.append(columns);
        else claus.append("*");

        claus.append(" FROM ").append(aliasName);
    }

    public void build() {
        this.columns = columnBuilder.build();
        StringBuilder withCommand = this.withCommandBuilder;

        withCommand.append(aliasName).append(" ");

        String build = this.selectWithBuilder.build();
        int size = this.selectWithBuilder.getColumns().size();
        int columnsSet = query.getAmountColumnsSet();
        if (!build.isEmpty() && size == columnsSet)
            withCommand.append("(")
                    .append(build)
                    .append(") ");
        else if (!build.isEmpty()) {
            this.logging.warn(messageWrapper -> {
                messageWrapper.setMessage("The number of columns in '{alias-name}' does not match the expected amount. " +
                                "When setting new column names for the WITH clause, the number of columns must be equal. " +
                                "Current number of columns: {size}, expected: '{columnsSet}'.")
                        .putPlaceholder("{alias-name}", aliasName)
                        .putPlaceholder("{size}", size +"")
                        .putPlaceholder("{columnsSet}", columnsSet +"");
            });
        }

        withCommand.append("AS (")
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