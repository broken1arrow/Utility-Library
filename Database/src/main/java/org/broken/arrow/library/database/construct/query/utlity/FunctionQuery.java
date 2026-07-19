package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.utility.query.build.QueryBuildContext;

/**
 * Defines a custom SQL query transformation used during command execution.
 * <p>
 * Implementations can modify the generated query, alter parameters,
 * or adjust the WHERE clause dynamically before execution.
 * </p>
 *
 * <p>Typically provided to {@link org.broken.arrow.library.database.utility.DatabaseCommandConfig} for
 * fine-grained control over SQL generation based on runtime conditions.</p>
 */
@FunctionalInterface
public interface FunctionQuery {
    /**
     * Functional interface for customizing SQL queries before execution.
     * <p>
     * Used in {@link org.broken.arrow.library.database.utility.DatabaseCommandConfig} to adjust queries based on column values,
     * conditions, and whether the target row already exists.
     * </p>
     *
     * @param sqlHandler  assists in building the SQL query
     * @param columnsMap  column-value mappings for the query
     * @param whereClause function to define the WHERE condition
     * @param rowExist    {@code true} if the row already exists; {@code false} otherwise
     * @return the built SQL statement and its parameters
     */
    SqlQueryPair apply(QueryBuildContext queryBuildContext);
}
