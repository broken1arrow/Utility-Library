package org.broken.arrow.library.database.construct.query.builder.table.cte.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.cte.WithManager;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import java.util.Map;

/**
 * Builder for a Common Table Expression (CTE) in SQL {@code WITH} clauses.
 * <p>
 * This class allows defining an alias name for the CTE, explicitly overriding column names,
 * and associating the underlying query that forms the CTE's body.
 * </p>
 */
public class WithBuilder {
    private final Logging logging = new Logging(WithBuilder.class);
    private final String aliasName;
    private ColumnBuilder columnBuilder;
    private QueryBuilder query;

    /**
     * Creates a {@code WithBuilder} with the specified alias name.
     *
     * @param aliasName the alias name for the CTE
     */
    public WithBuilder(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * Adds a specific column to this CTE.
     *
     * @param column the column to define in the CTE
     * @return this {@link WithBuilder} instance for fluent chaining
     */
    public WithBuilder add(Column column) {
        if (this.columnBuilder == null) {
            this.columnBuilder = ColumnBuilder.start(columnBuilder -> columnBuilder.add(column));
        } else {
            this.columnBuilder.add(column);
        }
        return this;
    }

    /**
     * Associates the underlying query with this CTE.
     *
     * @param query the main query builder forming the CTE body
     * @return this {@link WithBuilder} instance for fluent chaining
     */
    public WithBuilder query(QueryBuilder query) {
        this.query = query;
        return this;
    }

    /**
     * Returns the alias name of this CTE.
     *
     * @return the alias name
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Builds the SQL string representation of this CTE.
     * <p>
     * Note: This method is intended for internal use by the query generation engine
     * and typically does not need to be invoked directly.
     * </p>
     *
     * @return the formatted CTE SQL string
     */
    public String build() {
        final StringBuilder withCommand = new StringBuilder();
        final ColumnBuilder builder = this.columnBuilder;
        Validate.checkBoolean(query == null, "You must set the query for use this CTE. The alias: " + this.aliasName);
        final int columnsSet = query.getAmountColumnsSet();
        final String columns;
        final int size;

        if (this.columnBuilder != null) {
            columns = builder.build();
            size = builder.getColumns().size();
        } else {
            columns = "";
            size = 0;
        }

        withCommand.append(aliasName).append(" ");
        if (!columns.isEmpty() && size == columnsSet)
            withCommand.append("(")
                    .append(columns)
                    .append(") ");
        else if (!columns.isEmpty()) {
            this.logging.warn(messageWrapper -> messageWrapper
                    .setMessage("The number of columns in {alias-name} does not match the expected amount. " +
                            "When setting new column names for the WITH clause, the number of columns must be equal. " +
                            "Current number of columns: {size}, expected: {columns_set}.")
                    .putPlaceholder("{alias-name}", "'" + aliasName + "'")
                    .putPlaceholder("{size}", size + "")
                    .putPlaceholder("{columns_set}", columnsSet + "")
            );
        }
        withCommand.append("AS (")
                .append(this.query.build().replace(";", ""))
                .append(")");
        return withCommand.toString();
    }

    /**
     * Retrieves the parameter values bound to the underlying query.
     * <p>
     * Note: When retrieving values for execution, use the parent {@link QueryBuilder#getValues()}
     * instead after the full query is built.
     * </p>
     *
     * @return a map of parameter indexes to their corresponding values
     */
    public Map<Integer, Object> getValues() {
        return query.getValues();
    }

}