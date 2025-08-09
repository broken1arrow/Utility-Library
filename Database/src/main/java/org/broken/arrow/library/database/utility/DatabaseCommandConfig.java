package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.FunctionQuery;
import org.broken.arrow.library.database.core.Database;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration holder for database command execution options.
 * <p>
 * This class encapsulates settings related to the result set behavior for database commands,
 * such as the {@code resultSetType} and {@code resultSetConcurrency}, and optionally allows
 * a custom function query to modify the command execution behavior.
 * <p>
 * The class supports two usage modes:
 * <ul>
 *   <li>Basic configuration with result set type and concurrency only.</li>
 *   <li>Extended configuration with a {@link FunctionQuery} to customize query execution.</li>
 * </ul>
 */
public class DatabaseCommandConfig {
    private final int resultSetType;
    private final int resultSetConcurrency;
    private final FunctionQuery query;

    /**
     * Creates a new configuration with the specified result set type and concurrency.
     *
     * @param resultSetType        the type of the {@link java.sql.ResultSet} (e.g., {@code ResultSet.TYPE_SCROLL_INSENSITIVE})
     * @param resultSetConcurrency the concurrency mode of the {@link java.sql.ResultSet} (e.g., {@code ResultSet.CONCUR_UPDATABLE})
     */
    public DatabaseCommandConfig(int resultSetType, int resultSetConcurrency) {
        this(resultSetType, resultSetConcurrency, null);
    }

    /**
     * Creates a new configuration with the specified result set type, concurrency, and
     * an optional custom function query to modify command behavior.
     *
     * @param resultSetType        the type of the {@link java.sql.ResultSet}
     * @param resultSetConcurrency the concurrency mode of the {@link java.sql.ResultSet}
     * @param query                an optional {@link FunctionQuery} to customize query execution
     */
    public DatabaseCommandConfig(final int resultSetType, final int resultSetConcurrency, final FunctionQuery query) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.query = query;
    }

    /**
     * Gets the configured result set type for this database command.
     *
     * @return the {@code ResultSet} type (e.g., {@code ResultSet.TYPE_FORWARD_ONLY},
     * {@code ResultSet.TYPE_SCROLL_INSENSITIVE}, etc.)
     */
    public int getResultSetType() {
        return resultSetType;
    }

    /**
     * Gets the configured result set concurrency mode for this database command.
     *
     * @return the concurrency mode of the {@code ResultSet} (e.g., {@code ResultSet.CONCUR_READ_ONLY},
     * {@code ResultSet.CONCUR_UPDATABLE}, etc.)
     */
    public int getResultSetConcurrency() {
        return resultSetConcurrency;
    }

    /**
     * Executes a database command and optionally applies a custom function for additional operations.
     * This allows modifying the standard behavior, such as altering queries or handling data differently.
     * Whether this customization is available depends on which constructor is used—if you use
     * {@link DatabaseCommandConfig#DatabaseCommandConfig(int, int, FunctionQuery)} instead of {@link DatabaseCommandConfig#DatabaseCommandConfig(int, int)},
     * additional functionality can be applied.
     *
     * @param sqlHandler     Provides access to various query methods for retrieving command and query values.
     * @param columnValueMap A map of columns and their corresponding values, determining which data belongs to which column.
     * @param whereClause    Used for updating data, specifying conditions for identifying which row(s) to update.
     *                       Supports multiple conditions.
     * @param rowExists      Indicates whether the row already exists in the database. If {@code false}, the method
     *                       will replace the data by default. Depending on the provided function, additional
     *                       operations may be executed instead.
     * @return A {@link SqlQueryPair#SqlQueryPair(QueryBuilder, Map)} )} containing the generated SQL query and associated values.
     * If {@link Database#setSecureQuery(boolean)} is set to {@code false},
     * the query will not use parameterized values, as they are already included in the generated SQL string.
     */
    public SqlQueryPair applyDatabaseCommand(@Nonnull final SqlHandler sqlHandler, final Map<Column, Object> columnValueMap, final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, final boolean rowExists) {
        if (this.query != null) {
            return this.query.apply(sqlHandler, columnValueMap, whereClause, rowExists);
        }
        if (rowExists)
            return sqlHandler.updateTable(updateBuilder -> updateBuilder.putAll(columnValueMap), whereClause);
        else return sqlHandler.replaceIntoTable(insertHandler -> insertHandler.addAll(columnValueMap));
    }
}
