package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.utility.WhereClauseFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contextual data used when executing database write operations (e.g., UPDATE, DELETE).
 * <p>
 * This is particularly useful for storing extra mapped key/value pairs that are not natively
 * included in the main serialized data payload. Examples include composite primary keys,
 * tenant identifiers, extra map keys, or complex types (like a location) that
 * need to be split and mapped across multiple separate database columns.
 * </p>
 * <p>
 * It pairs these extra column constraints with an optional {@link WhereClauseFunction} to
 * precisely target specific rows during the operation.
 * </p>
 * <p>
 * <strong>Note:</strong> It is recommended to also set a custom {@link #withWhereClause(WhereClauseFunction)} if
 * complex targeting is needed; otherwise, the database operation will typically fall back
 * to a standard equality check against the provided keys (e.g., {@code WHERE key = value}) with
 * an {@code "and"} clause for several key/values.
 * </p>
 */
public class WriteContext {
    private final Map<String, Object> columnContext;
    private WhereClauseFunction whereClause;

    private WriteContext(@Nonnull final Map<String, Object> columnContext, @Nullable final WhereClauseFunction whereClause) {
        this.columnContext = new HashMap<>(columnContext);
        this.whereClause = whereClause;
    }

    /**
     * Creates an empty write context, to populate later.
     *
     * @return a new context without any key/value mappings set.
     */
    @Nonnull
    public static WriteContext empty() {
        return new WriteContext(new HashMap<>(), null);
    }

    /**
     * Convenience factory to start a write context initialized with a single key-value mapping.
     *
     * @param key   the primary column name.
     * @param value the value for the column.
     * @return a new context containing the specified key/value pair.
     */
    @Nonnull
    public static WriteContext with(@Nonnull final String key, @Nonnull final Object value) {
        return empty().put(key, value);
    }

    /**
     * Creates a write context populated from an existing map of keys.
     * <p>
     * Useful for bulk-loading extra column context, such as predefined composite keys
     * or a map of auxiliary data fields.
     * </p>
     *
     * @param columnContext the map containing column names and their corresponding values.
     * @return a new context initialized with the provided column mappings.
     */
    @Nonnull
    public static WriteContext fromMap(@Nonnull final Map<String, Object> columnContext) {
        return new WriteContext(columnContext, null);
    }

    /**
     * Adds an extra column mapping and its associated value, returning {@code this} for chaining.
     * <p>
     * Use this to append additional contextual data, such as extra map keys, routing keys,
     * or split fields that belong in their own distinct columns.
     *
     * @param key   the column name.
     * @param value the value for the column.
     * @return this context instance for method chaining.
     */
    @Nonnull
    public WriteContext put(@Nonnull final String key, @Nonnull final Object value) {
        this.columnContext.put(key, value);
        return this;
    }

    /**
     * Sets the custom WHERE clause function.
     *
     * @param whereClause the logical WHERE clause to apply to the database query.
     * @return this context instance for method chaining.
     */
    @Nonnull
    public WriteContext withWhereClause(@Nullable final WhereClauseFunction whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    /**
     * Returns an unmodifiable view of the current column constraints.
     *
     * @return a map of the targeted columns and their associated values.
     */
    @Nonnull
    public Map<String, Object> getColumnContext() {
        return Collections.unmodifiableMap(columnContext);
    }

    /**
     * Returns the target value for a specific column constraint.
     *
     * @param column the column name to look up.
     * @return the value associated with the column, or {@code null} if not present or explicitly set to null.
     */
    @Nullable
    public Object getValue(@Nonnull final String column) {
        return columnContext.get(column);
    }

    /**
     * Returns the custom WHERE clause applier, if one has been set.
     *
     * @return the WHERE clause function, or {@code null} if none is configured.
     */
    @Nullable
    public WhereClauseFunction getWhereClause() {
        return whereClause;
    }
}