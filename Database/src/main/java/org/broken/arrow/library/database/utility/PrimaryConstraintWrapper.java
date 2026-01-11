package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Container for primary key values used when constructing
 * SQL WHERE clauses.
 *
 * <p>
 * The keys in this wrapper are <strong>intended</strong> to represent
 * primary key columns for a database table.
 * </p>
 */
public class PrimaryConstraintWrapper {
    final Map<String, Object> primaryKeys = new HashMap<>();
    private final Database database;
    private final SqlQueryTable queryTable;
    private Consumer<Map<String, Object>> loadMapFromDB;
    private WhereClauseFunction whereClause;
    private boolean unique;


    public PrimaryConstraintWrapper(final Database database, final SqlQueryTable queryTable) {
        this.database = database;
        this.queryTable = queryTable;
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
     * Set where it shall update data or get data from database.
     *
     * @param whereClause where constructor to create your clause.
     */
    public void setWhereClause(final WhereClauseFunction whereClause) {
        this.whereClause = whereClause;
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

    /**
     * If it shall replace the primary key with unique if you not provide
     * value that could be set for the new primary column.
     *
     * @return Returns {@code true} if it shall replace the primary key constrain with unique.
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Set the constraint if it shall replace if no value is provided.
     *
     * @param unique Set to {@code true} if it shall replace the primary key constrain with unique.
     */
    public void setUnique(final boolean unique) {
        this.unique = unique;
    }

    /**
     * Checks if at least one value is set and the clause is specified.
     *
     * @return Returns {true} if at least one value added and the whereClause is not null
     */
    public boolean isSet() {
        return !(primaryKeys.isEmpty() && whereClause == null);
    }

    public void loadMap(Map<String, Object> dataFromDB) {
        if (this.loadMapFromDB == null) return;

        this.loadMapFromDB.accept(dataFromDB);
    }

    public <T extends ConfigurationSerializable> void forEachLoadedData(Consumer<LoadDataWrapper<T>> loadedData, Class<T> clazz) {
        final CreateTableHandler tableHandler = this.queryTable.getTable();
        this.loadMapFromDB = (dataFromDB) -> {
            final T deserialize = this.database.deSerialize(clazz, dataFromDB);
            final List<Column> primaryColumns = tableHandler.getPrimaryColumns();
            final Map<String, Object> objectList = new HashMap<>();
            if (!primaryColumns.isEmpty()) {
                for (Column column : primaryColumns) {
                    Object primaryValue = dataFromDB.get(column.getColumnName());
                    objectList.put(column.getColumnName(), primaryValue);
                }
            }
            loadedData.accept(new LoadDataWrapper<>(objectList, deserialize));
        };
    }

    public void forEachLoadedData(Consumer<Map<String, Object>> loadedData) {
        this.loadMapFromDB = loadedData;
    }

}
