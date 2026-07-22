package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.utility.WhereClauseFunction;
import org.broken.arrow.library.database.utility.query.build.SqlResultRow;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Wrapper used in SQL/database operations to combine
 * primary-key-based lookup data with a
 * {@link ConfigurationSerializable} payload.
 *
 * <p>
 * The {@code DataWrapper} is intended to:
 * <ul>
 *     <li>Identify database rows using one or more primary key values</li>
 *     <li>Provide non-primary column data via
 *         {@link ConfigurationSerializable}</li>
 * </ul>
 * </p>
 */
public class DataWrapper {

    private final ConfigurationSerializable configurationSerialize;
    private final Object value;
    private PrimaryWrapper primaryWrapper;
    private WriteContext writeContext;
    private Consumer<SqlResultRow> callback;

    /**
     * Creates a new {@code DataWrapper} using the provided write context
     * and an associated {@link ConfigurationSerializable} payload.
     *
     * <p>
     * <strong>Important:</strong> The {@code WriteContext} is the preferred mechanism
     * for supplying primary key values that are not naturally included in the
     * {@link ConfigurationSerializable#serialize()} map. Failing to provide required
     * primary keys here may result in a database constraint violation, unless the
     * underlying table handles key generation automatically (e.g., via auto-increment
     * columns or database triggers).
     * </p>
     *
     * @param writeContext Context containing supplemental column constraints (like missing
     *                     primary keys) and the SQL {@code WHERE} clause logic required
     *                     to target specific rows for updates.
     * @param serialize    The serializable configuration payload containing the main data.
     */
    public DataWrapper(@Nonnull final WriteContext writeContext, @Nonnull final ConfigurationSerializable serialize) {
        this("", serialize);
        this.writeContext = writeContext;
    }

    /**
     * Creates a new {@code DataWrapper} using primary key data
     * and an associated {@link ConfigurationSerializable}.
     *
     * @param wrapper   primary key wrapper used to provide primary
     *                  key columns, values, and SQL {@code WHERE} clause logic
     * @param serialize serializable configuration payload
     * @deprecated Use {@link #DataWrapper(WriteContext, ConfigurationSerializable)} as give better tools to
     * provide keys and values and where clause.
     */
    @Deprecated
    public DataWrapper(@Nonnull final PrimaryWrapper wrapper, @Nonnull final ConfigurationSerializable serialize) {
        this("", serialize);
        primaryWrapper = wrapper;
    }

    /**
     * Legacy constructor accepting a single primary key value.
     *
     * @param primaryValue primary key value
     * @param serialize    serializable configuration payload
     * @deprecated Use {@link #DataWrapper(WriteContext, ConfigurationSerializable)}
     * to support multiple primary keys, as will create problems if you have
     * more than one column as primary key.
     */
    @Deprecated
    public DataWrapper(@Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializable serialize) {
        this.configurationSerialize = serialize;
        this.value = primaryValue;
        primaryWrapper = new PrimaryWrapper();
        this.writeContext = WriteContext.empty();
    }

    /**
     * Returns the primary key wrapper used to construct the SQL
     * {@code WHERE} clause for this operation.
     *
     * <p>
     * The wrapper provides access to the primary key column names
     * and their associated values, as well as an optional custom
     * {@link WhereClauseFunction} for WHERE clause construction.
     * </p>
     *
     * <p>
     * If no custom {@link WhereClauseFunction} is provided,
     * primary keys are combined using {@code AND}.
     * </p>
     *
     * @return primary key wrapper
     */
    @Nonnull
    public PrimaryWrapper getPrimaryWrapper() {
        return primaryWrapper;
    }

    /**
     * Returns the context used to construct both the SQL {@code WHERE} clause
     * for this operation and any supplemental column constraints.
     *
     * <p>
     * The context provides access to column constraint names and their associated
     * values, as well as an optional custom {@link WhereClauseFunction} for custom
     * WHERE clause logic.
     * </p>
     *
     * <p>
     * If no custom {@link WhereClauseFunction} is provided, column constraints
     * are combined using default {@code AND} logic when targeting existing rows
     * for updates.
     * </p>
     *
     * @return the {@link WriteContext} containing supplemental column constraints
     *         (e.g., missing primary keys) and WHERE clause logic
     */
    @Nonnull
    public WriteContext getWriteContext() {
        return writeContext;
    }

    /**
     * Returns the associated {@link ConfigurationSerializable} instance.
     *
     * @return the serializable configuration object
     */
    public ConfigurationSerializable getConfigurationSerialize() {
        return configurationSerialize;
    }

    /**
     * Returns the legacy single primary key value.
     *
     * <p>
     * This method reflects an older design where only a single primary
     * key column was supported. It should not be used when working with
     * tables that define composite (multi-column) primary keys.
     * </p>
     *
     * @return legacy primary key value
     * @deprecated Use {@link PrimaryWrapper#getPrimaryKeys()} when writing data.
     */
    @Deprecated
    public Object getPrimaryValue() {
        return value;
    }

    /**
     * Registers a callback to capture and process auto-generated keys or updated column values
     * returned by the database execution engine for this specific data unit.
     * <p>
     * When performing batch insertions or updates, this callback allows developers to intercept
     * database-managed primary keys (such as {@code AUTOINCREMENT} IDs) or structural increments
     * immediately after this payload is written to the table.
     * </p>
     *
     * @param callback The consumer callback that will receive the populated {@link SqlResultRow}; cannot be null.
     * @return Returns this class for chaining.
     */
    public DataWrapper setGeneratedKeyCallback(@Nonnull final Consumer<SqlResultRow> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Retrieves the registered generated-key callback mapped to this payload.
     * <p>
     * Used internally by the execution engine to bridge user-configured callbacks from the high-level
     * data structure down into the final compiled execution parameters.
     * </p>
     *
     * @return The registered {@link Consumer} callback, or {@code null} if no callback was configured.
     */
    @Nullable
    public Consumer<SqlResultRow> getGeneratedKeyCallback() {
        return this.callback;
    }

    /**
     * Container for primary key values used when constructing
     * SQL WHERE clauses.
     *
     * <p>
     * The keys in this wrapper are <strong>intended</strong> to represent
     * primary key columns for a database table. This is a semantic
     * contract rather than a hard restriction.
     * </p>
     *
     * <p>
     * By default, all keys are combined using an {@code AND} clause.
     * A custom {@link WhereClauseFunction} may be provided to override
     * this behavior (e.g. {@code OR} conditions or composite logic).
     * </p>
     *
     */
    public static class PrimaryWrapper {
        final Map<String, Object> primaryKeys;
        final WhereClauseFunction whereClause;

        /**
         * Creates an empty primary key wrapper using default
         * {@code AND}-based WHERE clause behavior.
         */
        public PrimaryWrapper() {
            this(new HashMap<>(), null);
        }

        /**
         * Creates a primary key wrapper with the given key-value pairs
         * and default {@code AND}-based WHERE clause behavior.
         *
         * @param primaryKeys map of primary key column names to values
         */
        public PrimaryWrapper(@Nonnull final Map<String, Object> primaryKeys) {
            this(primaryKeys, null);
        }

        /**
         * Creates a primary key wrapper with the given key-value pairs
         * and custom WHERE clause construction logic.
         *
         * @param primaryKeys map of primary key column names to values
         * @param whereClause custom WHERE clause applier, or {@code null}
         */
        public PrimaryWrapper(@Nonnull final Map<String, Object> primaryKeys, @Nullable final WhereClauseFunction whereClause) {
            this.primaryKeys = primaryKeys;
            this.whereClause = whereClause;
        }

        /**
         * Adds a primary key column and its associated value.
         *
         * @param key   primary key column name
         * @param value primary key value
         */
        public void putPrimary(@Nonnull final String key, @Nonnull final Object value) {
            primaryKeys.put(key, value);
        }

        /**
         * Returns the primary key column-value mappings.
         *
         * @return map of primary keys
         */
        @Nonnull
        public Map<String, Object> getPrimaryKeys() {
            return primaryKeys;
        }

        /**
         * Returns the primary key column-value mappings.
         *
         * @param key primary key column name
         * @return the value to your primary key or {@code null} if not exist.
         */
        @Nullable
        public Object getPrimaryValue(@Nonnull final String key) {
            return primaryKeys.get(key);
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
}
