package org.broken.arrow.library.database.utility.query.build;

import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

/**
 * Strategy interface for dynamically constructing a SQL UPDATE statement.
 * <p>
 * Implementations specify how table data should be modified when the target row
 * already exists in the database.
 * </p>
 */
@FunctionalInterface
public interface UpdateStrategy {
    /**
     * Builds the SQL update query pair.
     *
     * @param sql     The handler tool used to construct the update statement.
     * @param columns The column-to-value map containing the data to update.
     * @param where   The function used to define the filtering conditions for the update.
     * @return A {@link SqlQueryPair} containing the generated SQL string and parameters.
     */
    SqlQueryPair build(@Nonnull final  SqlHandler sql,@Nonnull final  Map<Column, Object> columns,@Nonnull final  Function<WhereBuilder, LogicalOperator<WhereBuilder>> where);
}

