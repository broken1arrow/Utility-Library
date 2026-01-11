package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.utility.WhereClauseApplier;
import org.broken.arrow.library.database.utility.WhereClauseFunction;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Creates a new {@code DataWrapper} using primary key data
     * and an associated {@link ConfigurationSerializable}.
     *
     * @param wrapper   primary key wrapper used to provide primary
     *                  key columns, values, and SQL {@code WHERE} clause logic
     * @param serialize serializable configuration payload
     */
    public DataWrapper(@Nonnull final PrimaryWrapper wrapper, @Nonnull final ConfigurationSerializable serialize) {
        this("", serialize);
        primaryWrapper = wrapper;
    }

    /**
     * Legacy constructor accepting a single primary key value.
     *
     * @param primaryValue primary key value
     * @param serialize    serializable configuration payload
     * @deprecated Use {@link #DataWrapper(PrimaryWrapper, ConfigurationSerializable)}
     * to support multiple primary keys, as will create problems if you have
     * more than one column as primary key.
     */
    @Deprecated
    public DataWrapper(@Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializable serialize) {
        this.configurationSerialize = serialize;
        this.value = primaryValue;
        primaryWrapper = new PrimaryWrapper();
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
     *
     * @return legacy primary key value
     *
     * @deprecated Use {@link PrimaryWrapper#getPrimaryKeys()} when writing data.
     */
    @Deprecated
    public Object getPrimaryValue() {
        return value;
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
         * @param key   primary key column name
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
