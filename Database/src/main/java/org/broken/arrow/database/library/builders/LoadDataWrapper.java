package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.core.Database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


/**
 * Wrapper class for loading and storing deserialized data along with its primary value.
 *
 * @param <T> The type of the deserialized data.
 */
public class LoadDataWrapper<T> {
    private final T deSerializedData;
    private final Object primaryValue;
    private final Map<String, Object> primaryValues;

    /**
     * Constructs a LoadDataWrapper instance with the provided primary column value, deserialized the values from the database.
     *
     * @param primaryKeyValue  The primary column value.
     * @param deSerializedData The deserialized data.
     */
    public LoadDataWrapper(@Nullable final Object primaryKeyValue, @Nonnull final T deSerializedData) {
        this.deSerializedData = deSerializedData;
        this.primaryValue = primaryKeyValue;
        this.primaryValues = new HashMap<>();
    }

    /**
     * Constructs a LoadDataWrapper instance with the provided primary column value, deserialized the values from the database.
     *
     * @param primaryKeyValues The map of key and value par set for the primary key.
     * @param deSerializedData The deserialized data.
     */
    public LoadDataWrapper(@Nullable final Map<String, Object> primaryKeyValues, @Nonnull final T deSerializedData) {
        this.deSerializedData = deSerializedData;
        this.primaryValues = primaryKeyValues == null ? new HashMap<>() : primaryKeyValues;
        this.primaryValue = null;
    }

    /**
     * Retrieves the deserialized data stored in this wrapper.
     *
     * @return The deserialized data.
     */
    @Nonnull
    public T getDeSerializedData() {
        return deSerializedData;
    }

    /**
     * Retrieves the value associated with the specified column name, as provided during the call to
     * {@link Database#load(String, Class, String)}. Alternatively, if {@link Database#loadAll(String, Class)} was used,
     * this returns the primary key value for the given column from the registered table.
     *
     * <p>This map typically stores either a single primary value or multiple primary values individually.
     *
     * @param columnName the name of the column whose value was specified in {@link Database#load} or is
     *                   part of the primary key set in {@link Database#loadAll}
     * @return the value associated with the specified primary column, or {@code null} if none exists
     */
    @Nullable
    public Object getPrimaryValue(final String columnName) {
        return this.primaryValues.get(columnName);
    }

    /**
     * Map of primary key and value value set, will return just one key/value par
     * if you did not set up your own where clause.
     *
     * @return a map of key and value par set for the primary key.
     */
    @Nonnull
    public Map<String, Object> getPrimaryValues() {
        return this.primaryValues;
    }
}
