package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SaveRecordContext {
    private final Map<Column, Object> columnsValues = new HashMap<>();

    /**
     * Adds a column and value pair to the context,
     * useful for including data that isn't part of the {@link ConfigurationSerializable},
     * such as primary keys, foreign keys, or other lookup values.
     *
     * @param columnName the column name to associate.
     * @param value      the value to be stored under the column.
     */
    public void putValue(@Nonnull final String columnName, @Nonnull final Object value) {
        columnsValues.put(Column.of(columnName), value);
    }

    /**
     * Gets all additional key-value pairs added via {@link #putValue(String, Object)}.
     *
     * @param columnName the column name to associate.
     * @return a map of extra column-to-value entries not included in the serialized value.
     */
    @Nullable
    public Object getColumnValue(@Nonnull final String columnName) {
        return columnsValues.get(Column.of(columnName));
    }

    /**
     * Gets all additional key-value pairs added via {@link #putValue(String, Object)}.
     *
     * @return a map of extra column-to-value entries not included in the serialized value.
     */
    @Nonnull
    public Map<Column, Object> getColumnValues() {
        return Collections.unmodifiableMap(columnsValues);
    }
}
