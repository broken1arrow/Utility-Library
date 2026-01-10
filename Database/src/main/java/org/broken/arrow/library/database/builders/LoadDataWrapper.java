package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.database.builders.wrappers.DatabaseSettings;
import org.broken.arrow.library.database.builders.wrappers.LoadSetup;
import org.broken.arrow.library.database.core.Database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wrapper class for loading and storing deserialized data along with its primary value.
 *
 * @param <T> The type of the deserialized data.
 */
public class LoadDataWrapper<T> {
    private final T deSerializedData;
    private final Map<String, Object> filteredMap;

    /**
     * Constructs a LoadDataWrapper instance with the provided primary column value, deserialized the values from the database.
     *
     * @param filteredMap      The map of column names to their raw values, filtered according to the provided filter defined
     *                         in {@link DatabaseSettings#setFilter(Predicate)}.
     * @param deSerializedData The deserialized data.
     */
    public LoadDataWrapper(@Nullable final Map<String, Object> filteredMap, @Nonnull final T deSerializedData) {
        this.deSerializedData = deSerializedData;
        this.filteredMap = filteredMap == null ? new HashMap<>() : filteredMap;
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
        return this.filteredMap.get(columnName);
    }


    /**
     * Returns the pre-filtered map of raw column data for this row.
     *
     * <p>
     * The contents of this map are determined during the load operation
     * and are already filtered before this wrapper is constructed.
     * The filter is defined through the {@link LoadSetup} provided to
     * {@link Database#load(String, Class, Consumer)}.
     * </p>
     *
     * <p>
     * Depending on the load context, the map may also include primary key
     * columns. When using {@link Database#loadAll(String, Class)}, the map
     * will contain only the configured primary key columns.
     * </p>
     *
     * <p>
     * The values in this map are raw database values and are not
     * deserialized.
     * </p>
     *
     * @return an unmodifiable map of column names to raw database values
     */
    public Map<String, Object> getFilteredMap() {
        return Collections.unmodifiableMap(filteredMap);
    }

    /**
     * Map of primary key and value set, will return just one key/value par
     * if you did not set up your own where clause.
     *
     * @return a map of key and value par set for the primary key.
     * @deprecated it is confusing naming on this method use {@link #getFilteredMap()}.
     */
    @Deprecated
    @Nonnull
    public Map<String, Object> getPrimaryValues() {
        return this.filteredMap;
    }
}
