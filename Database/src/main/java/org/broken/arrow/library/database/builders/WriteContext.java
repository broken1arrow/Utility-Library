package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.utlity.LogicalComparison;
import org.broken.arrow.library.database.utility.WhereClauseFunction;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contextual data and lookup logic used when executing database write operations (e.g., INSERT, UPDATE, DELETE).
 * <p>
 * This class serves as the canonical builder for write operations by requiring a target {@link WhereClauseFunction}
 * as its primary entry point. It cleanly separates the <strong>conditions used to find target rows</strong> from the
 * <strong>explicit column assignments (values) being written</strong>.
 * </p>
 * <p>
 * Column assignments (e.g., extra mapped columns, tenant identifiers, or composite key values) can be chained
 * via {@link #put(String, Object)} or {@link #putAll(Map)}. If no explicit column values are provided, the context
 * will attempt to automatically infer single-value equality conditions from the {@link WhereClauseFunction}.
 * </p>
 */
public class WriteContext {
    private final Map<String, Object> columnContext;
    private final WhereClauseFunction whereClause;
    private boolean valuesCompiled;

    private WriteContext(@Nonnull final Map<String, Object> columnContext, @Nullable final WhereClauseFunction whereClause) {
        this.columnContext = new HashMap<>(columnContext);
        this.whereClause = whereClause;
    }

    /**
     * Entry point for creating a write context initialized with a target WHERE clause.
     * <p>
     * Use this method to establish row-targeting logic (e.g., exact match, IN, BETWEEN, complex AND/OR chains).
     * Additional key/value pairs to be updated or inserted can then be appended using
     * {@link #put(String, Object)} or {@link #putAll(Map)}.
     * </p>
     *
     * @param whereClause the logical WHERE clause defining row targeting criteria for the query.
     * @return a new, empty context configured with the specified WHERE clause.
     */
    @Nonnull
    public static WriteContext whereClause(@Nullable final WhereClauseFunction whereClause) {
        return new WriteContext(new HashMap<>(), whereClause);
    }

    /**
     * Adds an explicitly defined column and its associated value to the cache.
     *
     * @param key   the target database column name.
     * @param value the value to set for the column.
     * @return this context instance for method chaining.
     */
    @Nonnull
    public WriteContext put(@Nonnull final String key, @Nonnull final Object value) {
        this.columnContext.put(key, value);
        return this;
    }

    /**
     * Adds an explicit set of column mappings to the cache.
     *
     * @param columnContext a map containing column names and their corresponding write values.
     * @return this context instance for method chaining.
     */
    @Nonnull
    public WriteContext putAll(@Nonnull final Map<String, Object> columnContext) {
        this.columnContext.putAll(columnContext);
        return this;
    }

    /**
     * Returns an unmodifiable view of the data column mappings to be written.
     *
     * @return an unmodifiable map of the targeted write columns and their values.
     */
    @Nonnull
    public Map<String, Object> getColumnContext() {
        compileValues();
        return Collections.unmodifiableMap(columnContext);
    }

    /**
     * Returns the target write value for a specific column.
     *
     * @param column the column name to look up.
     * @return the value associated with the column, or {@code null} if not present or set to null.
     */
    @Nullable
    public Object getValue(@Nonnull final String column) {
        compileValues();
        return columnContext.get(column);
    }

    /**
     * Returns the custom WHERE clause function used to target specific rows, if one was configured.
     *
     * @return the WHERE clause function, or {@code null} if none is set.
     */
    @Nullable
    public WhereClauseFunction getWhereClause() {
        return whereClause;
    }

    /**
     * Lazily parses the WHERE clause to autofill the column assignments when explicit values are omitted.
     */
    private synchronized void compileValues() {
        if (!this.columnContext.isEmpty()) return;
        if (this.valuesCompiled) return;
        final WhereBuilder builder = getWhereBuilder();

        for (ComparisonHandler<WhereBuilder> comparison : builder.getConditionsList()) {
            if (!comparison.getLogicalComparison().equals(LogicalComparison.EQUALS) || comparison.getValues().length != 1) {
                throw new Validate.ValidateExceptions(
                        "No explicit column values were provided, and automatic inference failed. " +
                                "Column '" + comparison.getColumnName() + "' uses a non-EQUALS or multi-value operator. " +
                                "You must explicitly specify the values to write using put(column, value) or putAll(map)."
                );
            }
            this.columnContext.put(comparison.getColumnName(), comparison.getValues()[0]);
        }
        this.valuesCompiled = true;
    }

    @Nonnull
    private WhereBuilder getWhereBuilder() {
        final WhereClauseFunction clause = getWhereClause();
        if (clause == null) {
            throw new Validate.ValidateExceptions(
                    "No WHERE clause or column values were configured for this WriteContext. " +
                            "You must specify write values using put(column, value) or putAll(map), " +
                            "or provide a valid whereClause function."
            );
        }
        final WhereBuilder builder = new WhereBuilder(new QueryBuilder());
        clause.apply(builder);
        return builder;
    }

    @Override
    public String toString() {
        return "WriteContext{" +
                "columnContext=" + columnContext +
                ", hasWhereClause=" + (whereClause != null) +
                '}';
    }
}