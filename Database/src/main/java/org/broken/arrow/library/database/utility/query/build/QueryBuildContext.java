package org.broken.arrow.library.database.utility.query.build;

import org.broken.arrow.library.database.builders.tables.TableQuery;
import org.broken.arrow.library.database.builders.wrappers.SqlQuery;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds the contextual data required to dynamically customize a SQL query before execution.
 * <p>
 * This class acts as a configuration builder, allowing developers to define separate
 * execution strategies for updating an existing row versus inserting a new one.
 * The strategies are evaluated lazily, and only the strategy matching the current
 * database state (based on {@link #rowExists()}) is executed.
 * </p>
 */
public final class QueryBuildContext {
    private final TableQuery tableQuery;
    private final Map<Column, Object> columnsMap;
    private final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause;
    private Supplier<SqlQuery> executionBlueprint;
    private final boolean rowExists;

    /**
     * Constructs a new context for building a SQL query.
     *
     * @param tableQuery  The tool used to construct the final SQL statements.
     * @param columnsMap  The column-to-value mappings for the query.
     * @param whereClause The function defining the WHERE conditions (used for updates).
     * @param rowExists   {@code true} if the target row already exists in the database; {@code false} otherwise.
     */
    public QueryBuildContext(
            @Nonnull final TableQuery tableQuery,
            @Nonnull final Map<Column, Object> columnsMap,
            @Nullable final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause,
            final boolean rowExists) {

        this.tableQuery = tableQuery;
        this.columnsMap = columnsMap;
        this.whereClause = whereClause;
        this.rowExists = rowExists;
    }

    /**
     * Defines the execution strategy to run if the row already exists in the database.
     *
     * @param strategy The logic used to build the update query.
     */
    public void onUpdate(@Nonnull final UpdateStrategy strategy) {
        if (!rowExists) return;
        this.executionBlueprint = () -> strategy.build(tableQuery, columnsMap, whereClause);
    }

    /**
     * Defines the execution strategy to run if the row does not yet exist in the database.
     *
     * @param strategy The logic used to build the insert query.
     */
    public void onInsert(@Nonnull final InsertStrategy strategy) {
        if (rowExists) return;
        this.executionBlueprint = () -> strategy.build(tableQuery, columnsMap);
    }

    /**
     * Compiles and executes the selected blueprint.
     * <p>
     * This method is executed internally by the library engine after the configuration
     * block has been applied.
     * </p>
     *
     * @return The generated SQL query and its associated parameters.
     * @throws Validate.ValidateExceptions if neither an update nor an insert strategy was configured.
     */
    @Nonnull
    public SqlQuery compile() {
        if (executionBlueprint == null) {
            throw new Validate.ValidateExceptions("Neither an update nor an insert strategy was configured for this execution state.");
        }
        return executionBlueprint.get();
    }

    /**
     * Checks if the target row already exists in the database.
     *
     * @return {@code true} if the row exists, otherwise {@code false}.
     */
    public boolean rowExists() {
        return rowExists;
    }

}