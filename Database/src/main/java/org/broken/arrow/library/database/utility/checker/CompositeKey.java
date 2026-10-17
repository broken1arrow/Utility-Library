package org.broken.arrow.library.database.utility.checker;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable value object representing a unique composite key entry for a database row.
 */
public final class CompositeKey {
    private final Map<String, Object> columnValues;

    /**
     * Constructs a new key identifier from a map of column names and values.
     *
     * @param columnValues a map representing key column names and their associated values
     */
    public CompositeKey(@Nonnull final Map<String, ?> columnValues) {
        Objects.requireNonNull(columnValues, "Column values cannot be null.");
        this.columnValues = Collections.unmodifiableMap(new LinkedHashMap<>(columnValues));
    }

    /**
     * Returns the unmodifiable column key-value map for this row identifier.
     *
     * @return map of column names and key values
     */
    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CompositeKey)) {
            return false;
        }

        final CompositeKey other = (CompositeKey) o;

        if (!columnValues.keySet().equals(other.columnValues.keySet())) {
            return false;
        }

        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            final Object thisValue = normalize(entry.getValue());
            final Object otherValue = normalize(other.columnValues.get(entry.getKey()));

            if (!Objects.equals(thisValue, otherValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            result += entry.getKey().hashCode() ^ Objects.hashCode(normalize(entry.getValue()));
        }

        return result;
    }

    /**
     * Normalizes values to string representations to preserve comparison behavior across
     * different database drivers (such as SQLite returning Integer vs Long types).
     *
     * @param value the raw value to normalize
     * @return normalized string representation, or {@code null}
     */
    private static String normalize(final Object value) {
        return value == null ? null : value.toString();
    }

    @Override
    public String toString() {
        return  "CompositeKey{" +
                "columnValues=" + columnValues +
                '}';
    }
}