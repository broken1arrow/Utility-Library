package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.FunctionQuery;

import javax.annotation.Nonnull;
import java.util.Map;

public class DatabaseCommandConfig {
    protected final int resultSetType;
    protected final int resultSetConcurrency;
    private final ConfigConsumer config;
    private final FunctionQuery query;

    public DatabaseCommandConfig(int resultSetType, int resultSetConcurrency) {
        this(resultSetType, resultSetConcurrency, (FunctionQuery) null);
    }

    @Deprecated
    public DatabaseCommandConfig(final int resultSetType, final int resultSetConcurrency, final ConfigConsumer config) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.config = config;
        this.query = null;
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
     * Handles the execution of a database command based on the existence of a row.
     * This method provides support for SQL databases that do not use `REPLACE INTO`
     * when modifying a row. It determines whether to update an existing row or
     * replace it entirely, based on the provided parameters.
     *
     * @param commandComposer the composer responsible for constructing and executing
     *                        the appropriate SQL command during database operations.
     * @param primaryKeyValue the primary key value of the row to be modified.
     *                        This value is used to locate the specific row in the database.
     * @param rowExist        a flag indicating whether the row exists in the database.
     *                        If {@code false}, the method will use a `REPLACE INTO`
     *                        command to insert or replace the row.
     */
    @Deprecated
    public final void applyDatabaseCommand(final SqlCommandComposer commandComposer, final Object primaryKeyValue, final boolean rowExist) {
        if (this.config != null) {
            this.config.apply(commandComposer, primaryKeyValue, rowExist);
            return;
        }
        if (rowExist)
            commandComposer.updateTable(primaryKeyValue);
        else commandComposer.replaceIntoTable();
    }

    /**
     * Executes a database command and optionally applies a custom function for additional operations.
     * This allows modifying the standard behavior, such as altering queries or handling data differently.
     * Whether this customization is available depends on which constructor is used—if you use
     * {@link DatabaseCommandConfig(int, int, FunctionQuery)} instead of {@link DatabaseCommandConfig(int, int)},
     * additional functionality can be applied.
     *
     * @param sqlHandler     Provides access to various query methods for retrieving command and query values.
     * @param columnValueMap A map of columns and their corresponding values, determining which data belongs to which column.
     * @param whereClause    Used for updating data, specifying conditions for identifying which row(s) to update.
     *                       Supports multiple conditions.
     * @param rowExists      Indicates whether the row already exists in the database. If {@code false}, the method
     *                       will replace the data by default. Depending on the provided function, additional
     *                       operations may be executed instead.
     * @return A {@link SqlQueryPair} containing the generated SQL query and associated values.
     * If {@link org.broken.arrow.database.library.Database#setSecureQuery(boolean)} is set to {@code false},
     * the query will not use parameterized values, as they are already included in the generated SQL string.
     */
    public SqlQueryPair applyDatabaseCommand(@Nonnull final SqlHandler sqlHandler, final Map<Column, Object> columnValueMap, SqlFunction<WhereBuilder> whereClause, final boolean rowExists) {
        if (this.query != null) {
            return this.query.apply(sqlHandler, columnValueMap, whereClause, rowExists);
        }
        if (rowExists)
            return sqlHandler.updateTable(updateBuilder -> updateBuilder.putAll(columnValueMap), whereClause);
        else return sqlHandler.replaceIntoTable(insertHandler -> insertHandler.addAll(columnValueMap));
    }
}
