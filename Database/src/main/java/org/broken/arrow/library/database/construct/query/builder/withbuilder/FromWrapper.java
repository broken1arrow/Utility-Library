package org.broken.arrow.library.database.construct.query.builder.withbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.logging.Logging;
/**
 * A helper class for building SQL {@code WITH} and {@code FROM} clauses dynamically.
 * <p>
 * This class works in conjunction with {@link WithColumnBuilder}, {@link ColumnBuilder}, and {@link QueryBuilder}
 * to assemble complex SQL queries that make use of Common Table Expressions (CTEs) via the {@code WITH} clause.
 * It supports aliasing, column renaming, and ensures column count validation when defining CTEs.
 * </p>
 * <p>
 * Usage flow:
 * <ol>
 *     <li>Build the {@code WITH} clause definition using {@link #build()}.</li>
 *     <li>Generate the {@code FROM} clause with {@link #from()}.</li>
 *     <li>Access the generated parts with {@link #getWithCommandBuilder()} and {@link #getFromClaus()}.</li>
 * </ol>
 * </p>
 */
public class FromWrapper {
    private final StringBuilder withCommandBuilder = new StringBuilder();
    private final StringBuilder fromClaus = new StringBuilder();
    private final WithColumnBuilder columnBuilder;
    private final ColumnBuilder<Column, Void> selectWithBuilder;
    private final QueryBuilder query;
    private final String aliasName;
    private final Logging logging = new Logging(FromWrapper.class);
    private String columns;

    /**
     * Creates a new {@code FromWrapper} for building SQL CTE and FROM clauses.
     *
     * @param withColumnBuilder builder for defining column names in the {@code WITH} clause
     * @param withBuilder       builder containing the alias name and select column builder for the CTE
     * @param query             the {@link QueryBuilder} used to generate the inner query for the CTE
     */
    public FromWrapper(WithColumnBuilder withColumnBuilder, WithBuilder withBuilder, QueryBuilder query) {
        this.columnBuilder = withColumnBuilder;
        this.query = query;

        this.selectWithBuilder = withBuilder.getSelectBuilder();
        this.aliasName = withBuilder.getAliasName();
    }

    /**
     * Builds the {@code FROM} clause string using the alias name and selected columns.
     * <p>
     * If no columns are defined via the {@link WithColumnBuilder}, the method defaults to using {@code *}.
     * </p>
     */
    public void from() {
        build();
        StringBuilder claus = this.fromClaus;
        claus.append("SELECT ");
        if (columns != null && !columns.isEmpty()) claus.append(columns);
        else claus.append("*");

        claus.append(" FROM ").append(aliasName);
    }

    /**
     * Builds the {@code WITH} clause definition for the query.
     * <p>
     * Validates that the number of renamed columns matches the number of columns in the subquery.
     * If the counts mismatch, a warning is logged and the column renaming is skipped.
     * </p>
     */
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

    /**
     * @return the {@link StringBuilder} containing the {@code WITH} clause definition
     */
    public StringBuilder getWithCommandBuilder() {
        return withCommandBuilder;
    }

    /**
     * @return the {@link StringBuilder} containing the {@code FROM} clause
     */
    public StringBuilder getFromClaus() {
        return fromClaus;
    }
}