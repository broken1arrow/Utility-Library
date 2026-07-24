package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.columnbuilder.function.ColumnExpressionPipeline;
import org.broken.arrow.library.database.construct.query.columnbuilder.function.strategy.ColumnExpressionBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.refernces.SqlArg;
import org.broken.arrow.library.database.construct.query.utlity.SqlExpressionType;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Represents a database column with optional aliasing and aggregation support.
 * <p>
 * Encapsulates a column name and optional alias, and supports applying aggregation
 * functions (such as COUNT, SUM) through the {@link ColumnExpressionPipeline} class.
 * Provides methods to build the final SQL representation of the column including
 * alias and aggregation expressions.
 * </p>
 */
public class Column implements SqlArg {
    private final String columnName;
    private final String alias;
    private ColumnExpressionBuilder computedContext;

    /**
     * Constructs a Column instance with the specified column name and optional alias.
     *
     * @param columnName the name of the database column
     * @param alias      optional alias for the column, can be null or empty if none
     */
    public Column(@Nonnull final String columnName, @Nonnull final String alias) {
        this.columnName = columnName;
        this.alias = alias;
    }

    /**
     * Constructs a Column instance with the specified column name and optional alias.
     *
     * @param columnName the name of the database column
     * @param alias      optional alias for the column, can be empty.
     * @return The column instance.
     */
    public static Column of(String columnName, @Nonnull final String alias) {
        return new Column(columnName, alias);
    }

    /**
     * Constructs a Column instance with the specified column name with no alias.
     *
     * @param columnName the name of the database column
     * @return The column instance.
     */
    public static Column of(String columnName) {
        return new Column(columnName, "");
    }

    /**
     * Returns the fully qualified column name for SQL queries, including aliasing if present.
     * <p>
     * Example output: "columnName AS alias" if alias is set, otherwise just "columnName".
     * </p>
     *
     * @return the column name optionally suffixed with an alias expression
     */
    public String getFinishColumName() {
        if (alias == null || alias.isEmpty())
            return columnName;
        return columnName + " " + SqlExpressionType.AS + " " + alias;
    }

    /**
     * Set the aggregation for the column.
     *
     * @param functionContext the context for the column you want to aggregate
     * @return this class for method chaining.
     */
    public Column aggregation(Consumer<ColumnExpressionBuilder> functionContext) {
        ColumnExpressionBuilder baseLeaf = new ColumnExpressionPipeline(this);
        functionContext.accept(baseLeaf);
        this.computedContext = baseLeaf;
        return this;
    }

    /**
     * Returns the raw column name (without alias or aggregation).
     *
     * @return the column name string
     */
    public String getColumnName() {
        return this.columnName;
    }


    /**
     * Checks if this column contains an aggregate function (e.g., AVG, MIN, MAX).
     * <p>
     * This is highly useful for validating SQL constraints, such as preventing
     * aggregate functions from being unlawfully executed inside a {@code WHERE} clause.
     * </p>
     *
     * @return {@code true} if an aggregate function has been applied; {@code false} otherwise
     */
    public boolean hasAggregate() {
        return this.computedContext != null && this.computedContext.hasAggregate();
    }

    /**
     * Returns the alias for the column
     *
     * @return the alias
     */
    public String getAlias() {
        return this.alias;
    }


    /**
     * Returns the SQL expression string representing this column,
     * including any aggregation and rounding functions applied.
     *
     * @return the SQL string for this column
     */
    @Override
    public String toString() {
        ColumnExpressionBuilder expression = this.computedContext;
        if (expression != null) {
            String build = expression.build();
            return build == null || build.isEmpty() ? getFinishColumName() : build;
        }
        return getFinishColumName();
    }


}