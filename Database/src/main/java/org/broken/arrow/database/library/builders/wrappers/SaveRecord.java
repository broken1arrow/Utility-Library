package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.QueryModifier;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a record to be saved in the database, associated with a specific table and map entry.
 * <p>
 * This includes the original key-value pair, where the value must implement {@link ConfigurationSerializable},
 * along with additional database-related metadata such as:
 * <ul>
 *   <li>Table name</li>
 *   <li>Primary or foreign key columns not present in the class that implements {@link ConfigurationSerializable}</li>
 *   <li>Custom where clauses for conditional updates or inserts</li>
 * </ul>
 *
 * @param <K> The type of the key used in the original cache map.
 * @param <V> The type of the value, which must implement {@link ConfigurationSerializable}.
 */
public class SaveRecord<K, V extends ConfigurationSerializable> {
    private final Map<Column, Object> keys = new HashMap<>();
    private final String tableName;
    private final K key;
    private final V value;
    private final Class<K> keyClazz;
    private final Class<V> valueClazz;
    private QueryBuilder queryBuilder;
    private QueryModifier selectData;
    private Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause;

    /**
     * Constructs a save context for a given table and entry.
     *
     * @param tableName the name of the database table this save operation is targeting.
     * @param entry     a map entry containing the key and a {@link ConfigurationSerializable} value.
     */
    @SuppressWarnings("unchecked")
    public SaveRecord(@Nonnull final String tableName, Map.Entry<K, V> entry) {
        this.tableName = tableName;
        this.key = entry.getKey();
        this.value = entry.getValue();
        this.keyClazz = (Class<K>) this.key.getClass();
        this.valueClazz = (Class<V>) this.value.getClass();
    }

    /**
     * Gets the key associated with this save context.
     *
     * @return The map key instance.
     */
    public K getKey() {
        return key;
    }

    /**
     * Gets the value to be saved, which must implement {@link ConfigurationSerializable}.
     *
     * @return the {@link ConfigurationSerializable} value from your cache.
     */
    public V getValue() {
        return value;
    }

    /**
     * Gets the {@link QueryBuilder} used for building custom SQL queries during save.
     *
     * @return the {@link QueryBuilder} instance, or {@code null} if none was set.
     */
    @Nullable
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    /**
     * Gets the {@link QueryModifier} object representing a SELECT query for loading row data.
     *
     * @return the {@link QueryModifier} instance, or {@code null} if none was configured.
     */
    @Nullable
    public QueryModifier getSelectData() {
        return selectData;
    }

    /**
     * Gets the WHERE clause used in the configured select command.
     *
     * @return the function that builds a logical where clause, or {@code null} if not set.
     */
    @Nullable
    public Function<WhereBuilder, LogicalOperator<WhereBuilder>> getWhereClause() {
        return whereClause;
    }

    /**
     * Configures a SELECT command to query the current row in the database, using a custom WHERE clause.
     * Placeholders in the query are enabled by default.
     * <p>&nbsp;</p>
     * <strong>Important:</strong> You should also call {@link #addKeys(String, Object)} to provide any necessary
     * primary or foreign key values. If the row does not exist and an insert is attempted without the required keys,
     * a warning will be logged and null values may be used unintentionally—potentially assigning {@code null}
     * to a primary key column. This is not automatically prevented, as it may be intentional in your design.
     *
     * @param whereClause a builder function for defining the WHERE clause.
     * @return the built {@link QueryBuilder} instance.
     */
    public QueryBuilder setSelectCommand(@Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        return this.setSelectCommand(true, whereClause);
    }

    /**
     * Configures a SELECT command with optional query placeholders and a WHERE clause.
     * This is useful for checking if the row exists before deciding whether to update or insert it.
     * <p>&nbsp;</p>
     * <strong>Important:</strong> You should also call {@link #addKeys(String, Object)} to provide any necessary
     * primary or foreign key values. If the row does not exist and an insert is attempted without the required keys,
     * a warning will be logged and null values may be used unintentionally—potentially assigning {@code null}
     * to a primary key column. This is not automatically prevented, as it may be intentional in your design.
     *
     * @param queryPlaceholder whether to enable query placeholders in the query (recommended).
     * @param whereClause      a builder function for defining the WHERE clause.
     * @return the built {@link QueryBuilder} instance.
     */
    public QueryBuilder setSelectCommand(final boolean queryPlaceholder, @Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        final QueryBuilder builder = new QueryBuilder();
        builder.setGlobalEnableQueryPlaceholders(queryPlaceholder);
        List<Column> columnList = value.serialize().keySet().stream()
                .map(columnName -> ColumnManger.of().column(columnName).getColumn())
                .collect(Collectors.toList());
        this.whereClause = whereClause;
        this.selectData = builder.select(columnList).from(this.tableName).where(whereClause);
        this.queryBuilder = builder;

        return builder;
    }

    /**
     * Adds an additional column and value pair to the context,
     * useful for including data that isn't part of the {@link ConfigurationSerializable},
     * such as primary keys, foreign keys, or other lookup values.
     *
     * @param columnName the column name to associate.
     * @param value      the value to be stored under the column.
     */
    public void addKeys(String columnName, Object value) {
        keys.put(ColumnManger.of().column(columnName).getColumn(), value);
    }

    /**
     * Gets all additional key-value pairs added via {@link #addKeys(String, Object)}.
     *
     * @return a map of extra column-to-value entries not included in the serialized value.
     */
    public Map<Column, Object> getKeys() {
        return keys;
    }

    /**
     * Attempts to safely cast an object to a {@code SaveContext<k, v>} of the expected types.
     *
     * @param obj the object to test and cast.
     * @param <Z> expected key type.
     * @param <Y> expected value type.
     * @return a casted {@code SaveContext<k, v>} if the types match, or {@code null} otherwise.
     */
    @Nullable
    public <Z, Y extends ConfigurationSerializable> SaveRecord<Z, Y> isSaveContext(Object obj) {
        if (!(obj instanceof SaveRecord)) return null;

        SaveRecord<?, ?> context = (SaveRecord<?, ?>) obj;
        if (keyClazz.isInstance(context.getKey()) && valueClazz.isInstance(context.getValue())) {
            @SuppressWarnings("unchecked")
            SaveRecord<Z, Y> saveRecord = (SaveRecord<Z, Y>) context;
            return saveRecord;
        }
        return null;
    }

    @Override
    public String toString() {
        return "SaveContext{" +
                "tableName='" + tableName + '\'' +
                ", queryBuilder=" + queryBuilder +
                ", key=" + key +
                ", value=" + value +
                ", keyClazz=" + keyClazz +
                ", valueClazz=" + valueClazz +
                '}';
    }
}