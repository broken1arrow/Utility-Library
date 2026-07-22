package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.database.utility.query.build.QueryBuildContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;
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
 *   <li>Extended configuration with a {@link QueryBuildContext} to customize query execution.</li>
 * </ul>
 */
public class DatabaseCommandConfig {
    private final int resultSetType;
    private final int resultSetConcurrency;
    private final Consumer<QueryBuildContext> query;

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
     * @param query                an optional {@link QueryBuildContext} to customize query execution
     */
    public DatabaseCommandConfig(final int resultSetType, final int resultSetConcurrency, final Consumer<QueryBuildContext> query) {
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
     * Executes a database command, falling back to a default implementation unless a custom
     * configuration has been provided via {@link DatabaseCommandConfig}.
     * <p>
     * By default, if the row exists, an UPDATE statement is generated. If it does not exist,
     * a REPLACE statement is generated. If a custom query consumer was provided during configuration,
     * execution is delegated to the {@link QueryBuildContext} to build the custom SQL.
     * </p>
     *
     * @param sqlHandler  Provides access to query building methods for generating SQL strings.
     * @param columns     A map of database columns and their corresponding runtime values.
     * @param whereClause Used to specify conditions for identifying which row(s) to target (primarily for updates).
     * @param rowExists   Indicates whether the target row already exists in the database.
     * @return A {@link SqlQueryPair} containing the generated SQL query and associated parameterized values.
     *         If {@link Database#setSecureQuery(boolean)} is set to {@code false}, the parameterized
     *         values map may be empty as values will be injected directly into the SQL string.
     */
    public SqlQueryPair applyDatabaseCommand(@Nonnull final SqlHandler sqlHandler, final Map<Column, Object> columns, @Nullable final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, final boolean rowExists) {
        if (this.query != null) {
            final QueryBuildContext context = new QueryBuildContext(sqlHandler, columns, whereClause, rowExists);
            this.query.accept(context);
            return context.compile();
        }

        if (rowExists && whereClause != null)
            return sqlHandler.updateTable(updateBuilder -> updateBuilder.putAll(columns), whereClause);
        else return sqlHandler.replaceIntoTable(insertHandler -> insertHandler.addAll(columns));
    }
}
