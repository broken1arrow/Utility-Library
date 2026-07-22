package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.builders.WriteContext;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.QueryModifier;
import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.utility.WhereClauseFunction;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

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
    private Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereClause;

    /**
     * Constructs a save record for a given table and entry.
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
     * Gets the key associated with this save record.
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
    public Function<WhereBuilder, ConditionChainer<WhereBuilder>> getWhereClause() {
        return whereClause;
    }

    /**
     * Applies a {@link WriteContext} to this save record.
     * <p>
     * This automatically registers any supplemental columns (like missing primary keys)
     * and configures the internal match criteria used to check if the row exists
     * before performing an update.
     * </p>
     *
     * @param context the write context containing keys and where clause logic
     * @return the built {@link QueryBuilder} instance, or {@code null} if no where clause was provided
     */
    public QueryBuilder applyContext(@Nonnull final WriteContext context) {
        context.getColumnContext().forEach(this::addKey);
        if (context.getWhereClause() != null) {
            return this.createSelectCommand(true, context.getWhereClause());
        }
        return null;
    }

    /**
     * Applies a {@link WriteContext} to this save record.
     * <p>
     * This automatically registers any supplemental columns (like missing primary keys)
     * and configures the internal match criteria used to check if the row exists
     * before performing an update.
     * </p>
     *
     * @param context the write context containing keys and where clause logic
     * @param queryPlaceholder whether to enable query placeholders in the query (recommended).
     * @return the built {@link QueryBuilder} instance, or {@code null} if no where clause was provided
     */
    public QueryBuilder applyContext(@Nonnull final WriteContext context,final boolean queryPlaceholder) {
        context.getColumnContext().forEach(this::addKey);
        if (context.getWhereClause() != null) {
            return this.createSelectCommand(queryPlaceholder, context.getWhereClause());
        }
        return null;
    }

    /**
     * Adds a column and value pair to the context,
     * useful for including data that isn't part of the {@link ConfigurationSerializable},
     * such as primary keys, foreign keys, or other lookup values.
     *
     * @param columnName the column name to associate.
     * @param value      the value to be stored under the column.
     */
    public void addKey(@Nonnull final String columnName,@Nonnull final Object value) {
        keys.put(new Column(columnName, ""), value);
    }

    /**
     * Gets all additional key-value pairs added via {@link #addKey(String, Object)}.
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
     * @return a cast {@code SaveContext<k, v>} if the types match, or {@code null} otherwise.
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

    /**
     * Internal helper that configures a SELECT command to check if the row exists
     * before deciding whether to update or insert it.
     * <p>
     * This is automatically invoked by {@link #applyContext(WriteContext)} when
     * a custom WHERE clause is provided.
     * </p>
     *
     * @param queryPlaceholder whether to enable query placeholders in the query (recommended).
     * @param clauseFunction      a builder function for defining the WHERE clause.
     * @return the built {@link QueryBuilder} instance.
     */
    private QueryBuilder createSelectCommand(final boolean queryPlaceholder, @Nonnull final WhereClauseFunction clauseFunction) {
        final QueryBuilder builder = new QueryBuilder();
        builder.setGlobalEnableQueryPlaceholders(queryPlaceholder);
        List<Column> columnList = value.serialize().keySet().stream()
                .map(columnName -> ColumnManager.of().column(columnName).getColumn())
                .collect(Collectors.toList());
        final Function<WhereBuilder, ConditionChainer<WhereBuilder>> whereStrategy = clauseFunction::apply;
        this.whereClause = whereStrategy;
        builder.select(columnList).from(this.tableName).where(whereClause -> whereClause.where("id").equal("123"));
        this.selectData = builder.select(columnList).from(this.tableName).where(whereStrategy);
        this.queryBuilder = builder;

        return builder;
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