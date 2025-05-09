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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveContext<K, V extends ConfigurationSerializable> {

    private final String tableName;
    private QueryBuilder  queryBuilder;
    private QueryModifier selectData;
    private final K key;
    private final V value;
    private final Class<K> keyClazz;
    private final Class<V> valueClazz;
    private Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause;

    @SuppressWarnings("unchecked")
    public SaveContext(@Nonnull final String tableName,  Map.Entry<K, V> entry) {
        this.tableName = tableName;
        this.key =  entry.getKey();
        this.value = entry.getValue();
        this.keyClazz = (Class<K>) this.key.getClass();
        this.valueClazz = (Class<V>) this.value.getClass();
    }

    public K getKey() { return key; }
    public V getValue() { return value; }


    @Nullable
    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    @Nullable
    public QueryModifier getSelectData() {
        return selectData;
    }

    @Nullable
    public Function<WhereBuilder, LogicalOperator<WhereBuilder>> getWhereClause() {
        return whereClause;
    }

    public QueryBuilder setSelectCommand(@Nonnull final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause) {
        return this.setSelectCommand(true, whereClause);
    }

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

    public Map<String, Object> toColumnMap(Function<V, Map<String, Object>> serializer) {
        return serializer.apply(value);
    }

    @Nullable
    public <k, v extends ConfigurationSerializable> SaveContext<k, v> isSaveContext(Object obj) {
        if (!(obj instanceof SaveContext)) return null;

        SaveContext<?, ?> context = (SaveContext<?, ?>) obj;
        if (keyClazz.isInstance(context.getKey()) && valueClazz.isInstance(context.getValue())) {
            @SuppressWarnings("unchecked")
            SaveContext<k, v> saveContext = (SaveContext<k, v>) context;
            return saveContext;
            // safe cast if validated
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