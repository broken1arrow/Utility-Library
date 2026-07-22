package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.utility.WhereClauseFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contextual data used when executing database write operations (e.g., UPDATE, DELETE),
 * containing column constraints and optional WHERE clause logic to target specific rows.
 */
public class WriteContext {
    private final Map<String, Object> columnContext;
    private WhereClauseFunction whereClause;

    private WriteContext(@Nonnull final Map<String, Object> columnContext, @Nullable final WhereClauseFunction whereClause) {
        this.columnContext = new HashMap<>(columnContext);
        this.whereClause = whereClause;
    }

    /**
     * Creates an empty write context with default AND logic.
     *
     * @return the context without values set.
     */
    @Nonnull
    public static WriteContext empty() {
        return new WriteContext(new HashMap<>(), null);
    }

    /**
     * Creates a write context populated from an existing map of keys.
     *
     *
     * @param columnContext the map of column name and a value.
     * @return the context without values set.
     */
    @Nonnull
    public static WriteContext fromMap(@Nonnull final Map<String, Object> columnContext) {
        return new WriteContext(columnContext, null);
    }

    /**
     * Convenience factory to start a write context targeting a single primary key.
     *
     * @param key the column name.
     * @param value the value for the column.
     * @return the context without values set.
     */
    @Nonnull
    public static WriteContext with(@Nonnull final String key, @Nonnull final Object value) {
        return empty().and(key, value);
    }

    /**
     * Adds a primary key column and value, returning {@code this} for chaining.
     *
     * @param key the column name.
     * @param value the value for the column.
     * @return the context without values set.
     */
    @Nonnull
    public WriteContext and(@Nonnull final String key, @Nonnull final Object value) {
        this.columnContext.put(key, value);
        return this;
    }

    /**
     * Sets or overrides the custom WHERE clause function.
     *
     * @param whereClause the where clause for the query
     * @return the context without values set.
     */
    @Nonnull
    public WriteContext withWhereClause(@Nullable final WhereClauseFunction whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    /**
     * Returns an unmodifiable view of the column constraints.
     *
     * @return map of targeted columns and their values
     */
    @Nonnull
    public Map<String, Object> getColumnContext() {
        return Collections.unmodifiableMap(columnContext);
    }

    /**
     * Returns the target value for a specific column constraint.
     *
     * @param column column name
     * @return the value associated with the column, or {@code null} if not present
     */
    @Nullable
    public Object getValue(@Nonnull final String column) {
        return columnContext.get(column);
    }

    /**
     * Returns the custom WHERE clause applier, if present.
     *
     * @return WHERE clause applier or {@code null}
     */
    @Nullable
    public WhereClauseFunction getWhereClause() {
        return whereClause;
    }
}