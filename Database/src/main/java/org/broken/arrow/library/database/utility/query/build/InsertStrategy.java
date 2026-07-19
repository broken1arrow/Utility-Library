package org.broken.arrow.library.database.utility.query.build;

import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Strategy interface for dynamically constructing a SQL INSERT or REPLACE statement.
 * <p>
 * Implementations specify how new data should be persisted when the target row
 * does not yet exist in the database.
 * </p>
 */
@FunctionalInterface
public interface InsertStrategy {

    /**
     * Builds the SQL insert query pair.
     *
     * @param sql     The handler tool used to construct the insert statement.
     * @param columns The column-to-value map containing the data to insert.
     * @return A {@link SqlQueryPair} containing the generated SQL string and parameters.
     */
    SqlQueryPair build(@Nonnull final SqlHandler sql,@Nonnull final Map<Column, Object> columns);
}