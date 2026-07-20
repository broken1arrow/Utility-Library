package org.broken.arrow.library.database.utility.query.build;

import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a single normalized row retrieved from a database query result.
 * <p>
 * This class wraps raw database map entries, automatically forcing all column names
 * to lowercase using {@link Locale#ROOT} to prevent driver-specific casing issues.
 * It features a type-safe getter with dynamic number conversion to safely navigate
 * driver fluctuations between integer types (e.g., {@code Integer} vs {@code Long}).
 * </p>
 */
public class SqlResultRow {
    private final Map<String, Object> columns = new HashMap<>();

    /**
     * Associates a value with a column name. The column name is automatically normalized to lowercase.
     *
     * @param columnName The name of the database column.
     * @param value      The value to associate with the column.
     * @throws Validate.ValidateExceptions if the column name is null.
     */
    public void put(@Nonnull final String columnName,@Nullable final Object value) {
        Validate.checkNotNull(columnName,"Column name cant be null");
        columns.put(columnName.toLowerCase(Locale.ROOT), value);
    }

    /**
     * Retrieves the raw untyped map.
     *
     * @return Returns the raw map with all values found.
     */
    @Nullable
    public Object getAll() {
        return Collections.unmodifiableMap(columns);
    }

    /**
     * Retrieves the raw untyped object associated with the specified column.
     *
     * @param columnName The name of the database column; cannot be null.
     * @return The raw object value, or {@code null} if the column is absent or holds a SQL NULL value.
     * @throws Validate.ValidateExceptions if the column name is null.
     */
    @Nullable
    public Object get(@Nonnull final String columnName) {
        Validate.checkNotNull(columnName,"Column name cant be null");
        return columns.get(columnName.toLowerCase(Locale.ROOT));
    }

    /**
     * Retrieves a column value cast to a specified Java type, offering smart numeric coercion.
     * <p>
     * Database drivers frequently toggle between returning {@code Integer} and {@code Long} types
     * depending on database state and size. If the requested class is a numeric wrapper and the underlying data
     * is a {@link Number}, this method safely handles the primitive conversion up or down to avoid
     * manual casting exceptions.
     * </p>
     *
     * @param <T>        The expected target return type.
     * @param columnName The name of the database column; cannot be null.
     * @param clazz      The class representation of the target type; cannot be null.
     * @return The cast or converted object, or {@code null} if the database value is null.
     * @throws Validate.ValidateExceptions if the value cannot be assigned or converted into the target class.
     */
    @Nullable
    public <T> T get(@Nonnull final String columnName, @Nonnull final Class<T> clazz) {
        Object value = this.get(columnName);
        if (value == null) {
            return null;
        }

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        // Essential for SQLite/MySQL drivers which frequently fluctuate 
        // between returning Integer and Long for numeric IDs
        if (value instanceof Number && Number.class.isAssignableFrom(clazz)) {
            Number num = (Number) value;
            if (clazz == Long.class) return clazz.cast(num.longValue());
            if (clazz == Integer.class) return clazz.cast(num.intValue());
            if (clazz == Double.class) return clazz.cast(num.doubleValue());
            if (clazz == Float.class) return clazz.cast(num.floatValue());
            if (clazz == Short.class) return clazz.cast(num.shortValue());
            if (clazz == Byte.class) return clazz.cast(num.byteValue());
        }
        throw new  Validate.ValidateExceptions("Cannot cast SQL type " + value.getClass().getName() + " to " + clazz.getName());
    }
}