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

public class DatabaseCommandConfig {
    protected final int resultSetType;
    protected final int resultSetConcurrency;
    private final ConfigConsumer config;
    private final FunctionQuery query;

    public DatabaseCommandConfig(int resultSetType, int resultSetConcurrency) {
        this(resultSetType, resultSetConcurrency,  null);
    }

    public DatabaseCommandConfig(final int resultSetType, final int resultSetConcurrency, final FunctionQuery query) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.config = null;
        this.query = query;
    }

    public int getResultSetType() {
        return resultSetType;
    }

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
