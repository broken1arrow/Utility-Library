package org.broken.arrow.library.database.construct.query.builder.table.cte;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnRegistry;

/**
 * Represents a builder for a Common Table Expression (CTE) in SQL WITH clauses.
 * <p>
 * This class allows defining an alias name for the CTE, selecting columns,
 * and associating a query to form the CTE's body.
 * </p>
 */
public class WithBuilder {
    private final String aliasName;
    private ColumnBuilder columnRegistry;
    private FromWrapper fromWrapper;

    /**
     * Creates a WithBuilder with the specified alias name.
     *
     * @param aliasName the alias name for the CTE
     */
    public WithBuilder(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * Starts a SELECT clause by adding the specified column to this CTE.
     *
     * @param column the column to select in the CTE
     * @return this WithBuilder instance for chaining
     */
    public WithBuilder select(Column column) {
        if (this.columnRegistry == null) {
            this.columnRegistry = ColumnBuilder.start(columnBuilder -> columnBuilder.add(column));
        } else {
            this.columnRegistry.add(column);
        }
        return this;
    }

    /**
     * Associates a query with this CTE, initializing a {@link WithColumnRegistry}
     * that manages the query columns and the subsequent FROM clause.
     *
     * @param query the main query builder for the CTE
     * @return a WithColumnBuilder to continue building the CTE's query
     */
    public WithColumnRegistry query(QueryBuilder query) {
        WithColumnRegistry withColumnBuilder = new WithColumnRegistry(this, query);
        this.fromWrapper = withColumnBuilder.getFromWrapper();
        return withColumnBuilder;
    }

    /**
     * Returns the {@link FromWrapper} associated with this CTE builder.
     *
     * @return the from wrapper managing the FROM clause for this CTE
     */
    public FromWrapper getFromWrapper() {
        return fromWrapper;
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
     * Returns the {@link ColumnRegistry} used to build the SELECT columns for this CTE.
     *
     * @return the column builder managing the selected columns
     */
    public ColumnBuilder getSelectBuilder() {
        return columnRegistry;
    }
}