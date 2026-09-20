package org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.strategy;

import org.broken.arrow.library.database.construct.query.builder.column.refernces.SqlArg;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fluent builder for configuring how to handle record collisions during an {@code INSERT}.
 * <p>
 * Supports ignoring conflicts, automatically updating matching columns, mapping to specific
 * fallback literal values, or executing raw SQL expressions (such as counters).
 */
public class ConflictBuilder {
    private final Map<String, Object> updateColumns = new LinkedHashMap<>();
    private final ConflictStrategy conflictStrategy;
    private boolean doNothing;
    private boolean updateAll;

    /**
     * Constructs a new ConflictBuilder.
     *
     * @param conflictStrategy the parent strategy responsible for SQL generation
     */
    public ConflictBuilder(@Nonnull final ConflictStrategy conflictStrategy) {
        this.conflictStrategy = conflictStrategy;
    }

    /**
     * Ignores the insertion if a duplicate key or constraint failure occurs.
     * <p>
     * Maps to {@code DO NOTHING} in PostgreSQL/SQLite, or an empty clause/INSERT IGNORE in MySQL.
     * </p>
     */
    public void doNothing() {
        this.doNothing = true;
    }

    /**
     * Updates the specified columns with the new incoming values if a conflict occurs.
     *
     * @param columns the names of the columns to update
     * @return this builder instance for chaining
     */
    public ConflictBuilder doUpdate(@Nonnull final String... columns) {
        for (String column : columns) {
            this.updateColumns.put(column, column);
        }
        return this;
    }

    /**
     * Updates a specific column using a custom SQL argument.
     * <p>
     * Use {@link SqlArg#raw(String)} to inject unescaped SQL (like {@code "tablename.count + 1"}),
     * or {@link SqlArg#val(Object)} to provide a hardcoded fallback value.
     * </p>
     *
     * @param columnName the name of the column to update
     * @param raw the custom SQL argument or literal value
     * @return this builder instance for chaining
     */
    public ConflictBuilder doUpdate(@Nonnull final String columnName, @Nonnull final SqlArg raw) {
        this.updateColumns.put(columnName, raw);
        return this;
    }

    /**
     * Sets whether to automatically update all inserted columns (excluding target keys)
     * when a conflict occurs.
     *
     * @param updateAll true to enable automatic updating of all dynamic columns
     * @return this builder instance for chaining
     */
    public ConflictBuilder updateAll(final boolean updateAll) {
        this.updateAll = updateAll;
        return this;
    }

    /**
     * Retrieves the map of configured columns to their update strategy or value.
     *
     * @return an unmodifiable map of update configurations
     */
    public Map<String, Object> getUpdateColumns() {
        if (updateColumns.isEmpty())
            return Collections.emptyMap();
        return Collections.unmodifiableMap(updateColumns);
    }

    /**
     * Checks if the collision strategy is set to ignore conflicts.
     *
     * @return true if doNothing is active
     */
    public boolean isDoNothing() {
        return doNothing;
    }

    /**
     * Checks if the builder is configured to automatically update all inserted columns.
     *
     * @return true if updateAll is active
     */
    public boolean isUpdateAll() {
        return updateAll;
    }

    /**
     * Checks if there are any specific column updates configured.
     *
     * @return true if no specific updates exist
     */
    public boolean isUpdateColumnsEmpty() {
        return updateColumns.isEmpty();
    }

    /**
     * Checks if a specific column is already tracked for an update.
     *
     * @param columnName the name of the column
     * @return true if the column is present in the update map
     */
    public boolean containsUpdateColumnsKey(@Nonnull final String columnName) {
        return updateColumns.containsKey(columnName);
    }

    /**
     * Retrieves the parent strategy orchestrating the generation.
     *
     * @return the parent {@link ConflictStrategy}
     */
    public ConflictStrategy getConflictStrategy() {
        return conflictStrategy;
    }
}
