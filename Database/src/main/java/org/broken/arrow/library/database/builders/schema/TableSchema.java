package org.broken.arrow.library.database.builders.schema;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.refernces.SqlArg;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the structural blueprint and schema metadata of a database table.
 * <p>
 * This class wraps a {@link QueryBuilder} and {@link CreateTableHandler} to manage
 * column definitions, primary keys, DDL creation statements, and key-based query helpers.
 * </p>
 */
public class TableSchema {
    private final QueryBuilder queryBuilder;
    private final CreateTableHandler tableHandler;

    /**
     * Constructs a new {@code TableSchema} using the provided callback to define the table structure.
     *
     * @param callback a function that configures the {@link CreateTableHandler} using a {@link QueryBuilder}; cannot be null
     */
    public TableSchema(@Nonnull final Function<QueryBuilder, CreateTableHandler> callback) {
        this.queryBuilder = new QueryBuilder();
        this.tableHandler = callback.apply(queryBuilder);
    }

    /**
     * Returns the underlying {@link QueryBuilder} instance used for constructing SQL queries.
     *
     * @return the query builder instance
     */
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    /**
     * Returns the {@link CreateTableHandler} managing column definitions and table constraints.
     *
     * @return the table handler instance, never null
     */
    @Nonnull
    public CreateTableHandler getTable() {
        return tableHandler;
    }

    /**
     * Generates a WHERE clause for the table's primary key columns using the provided values.
     * <p>
     * For each primary key column, it appends a {@code column = value} condition. Multiple primary keys
     * are automatically chained with logical {@code AND} operators.
     * </p>
     * <p><strong>Note:</strong> It is recommended to construct an explicit WHERE clause when building queries.
     * This method is primarily used internally by the framework as a fallback when none is provided.
     * </p>
     *
     * @param whereBuilder the {@link WhereBuilder} to append conditions to; cannot be null
     * @param values       the parameter values matching primary key columns in declaration order
     * @return the final {@link ConditionChainer} representing the chained WHERE conditions
     * @throws Validate.ValidateExceptions if no primary key columns are defined for the table, or if the number
     *                                     of provided values does not match the primary key column count
     */
    @Nullable
    public ConditionChainer<WhereBuilder> createWhereClauseFromPrimaryColumns(@Nonnull final WhereBuilder whereBuilder, final Object... values) {
        Validate.checkNotNull(whereBuilder, "WhereBuilder cannot be null");

        List<TableColumn> primaryCols = getPrimaryColumns();

        if (primaryCols.isEmpty()) {
            throw new Validate.ValidateExceptions("Cannot build primary key WHERE clause: Table '"
                    + getTableName() + "' has no primary key columns defined.");
        }

        if (values.length != primaryCols.size()) {
            throw new Validate.ValidateExceptions(String.format(
                    "Primary key value count mismatch for table '%s'. Expected %d value(s) for columns %s, but received %d.",
                    getTableName(),
                    primaryCols.size(),
                    primaryCols.stream().map(TableColumn::getColumnName).collect(Collectors.toList()),
                    values.length
            ));
        }

        ConditionChainer<WhereBuilder> lastChainer = null;
        for (int i = 0; i < primaryCols.size(); i++) {
            TableColumn column = primaryCols.get(i);
            boolean hasNext = (i < primaryCols.size() - 1);

            if (hasNext) {
                whereBuilder.where(column.getColumnName()).equal(SqlArg.val(values[i])).and();
            } else {
                lastChainer = whereBuilder.where(column.getColumnName()).equal(SqlArg.val(values[i]));
            }
        }

        Validate.checkNotNull(lastChainer, "Failed to construct WHERE clause");
        return lastChainer;
    }

    /**
     * Returns all defined columns for this table.
     *
     * @return list of {@link Column} objects
     */
    public List<Column> getColumns() {
        return this.getTable().getColumns();
    }

    /**
     * Returns all primary key columns for this table.
     *
     * @return list of primary key {@link TableColumn} objects
     */
    public List<TableColumn> getPrimaryColumns() {
        return this.getTable().getPrimaryColumns();
    }

    /**
     * Checks whether this table contains at least one auto-incrementing primary key column.
     *
     * @return {@code true} if an auto-increment column exists; {@code false} otherwise
     */
    public boolean isAutoIncrementTable() {
        return getPrimaryColumns().stream().anyMatch(TableColumn::isAutoIncrement);
    }

    /**
     * Returns the configured name of this table.
     *
     * @return the table name string
     */
    @Nonnull
    public String getTableName() {
        return getQueryBuilder().getTableName();
    }

    /**
     * Generates a SQL {@code SELECT} statement string fetching all configured columns from this table.
     *
     * @return the generated SQL SELECT query string
     */
    public String selectTable() {
        QueryBuilder selectTableBuilder = new QueryBuilder();
        selectTableBuilder.select(this.getTable().getColumns()).from(this.getQueryBuilder().getTableName());
        return selectTableBuilder.build();
    }

    /**
     * Generates the DDL statement required to create this table in the database.
     *
     * @return the generated {@code CREATE TABLE} SQL string
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
