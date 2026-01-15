package org.broken.arrow.library.database.builders.tables;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Represents an SQL table query builder that handles table creation and query construction.
 * <p>
 * This class wraps a {@link QueryBuilder} and a {@link CreateTableHandler} to facilitate
 * building SQL queries related to table structure, including primary key constraints and
 * generating WHERE clauses from primary columns.
 * </p>
 */
public class SqlQueryTable {
    private final QueryBuilder queryBuilder;
    private final CreateTableHandler tableHandler;

    /**
     * Constructs a new {@code SqlQueryTable} using the provided callback to
     * initialize the {@link CreateTableHandler} with a {@link QueryBuilder}.
     *
     * @param callback a function that receives a {@link QueryBuilder} and returns a
     *                 configured {@link CreateTableHandler}, must not be null
     */
    public SqlQueryTable(@Nonnull final Function<QueryBuilder, CreateTableHandler> callback) {
        this.queryBuilder = new QueryBuilder();
        this.tableHandler = callback.apply(queryBuilder);
    }

    /**
     * Gets the underlying {@link QueryBuilder} instance used for constructing SQL queries.
     *
     * @return the {@link QueryBuilder} instance
     */
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    /**
     * Gets the {@link CreateTableHandler} which manages table column definitions,
     * primary keys, and related metadata.
     *
     * @return the {@link CreateTableHandler} instance, never null
     */
    @Nonnull
    public CreateTableHandler getTable() {
        return tableHandler;
    }

    /**
     * Builds a WHERE clause for primary key columns using the provided values.
     * <p>
     * For each primary key column, it generates a condition of the form
     * {@code column = value}, chaining them with logical AND operators.
     * </p>
     *
     * @param whereBuilder the {@link WhereBuilder} to append conditions to, must not be null
     * @param values       the values to match against the primary key columns; if fewer values
     *                     than primary columns are provided, the loop stops early
     * @return the final {@link LogicalOperator} representing the combined WHERE clause,
     *         or null if no conditions were added
     */
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

    /**
     * Returns the list of all set columns for this table.
     *
     * @return a list of set columns {@link Column} objects
     */
    public List<Column> getColumns() {
        return this.getTable().getColumns();
    }


    /**
     * Returns the list of primary key columns for this table.
     *
     * @return a list of primary key {@link Column} objects
     */
    public List<Column> getPrimaryColumns() {
        return this.getTable().getPrimaryColumns();
    }

    /**
     * Returns the name of the table this query builder operates on.
     *
     * @return the table name as a non-null string
     */
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
