package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
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
    private final List<DataWrapper.PrimaryWrapper> primaryWrappers = new ArrayList<>();
    private final Database database;
    private final SqlQueryTable queryTable;
    private Consumer<Map<String, Object>> loadMapFromDB;

    private boolean unique;


    public PrimaryConstraintWrapper(final Database database, final SqlQueryTable queryTable) {
        this.database = database;
        this.queryTable = queryTable;
    }

    /**
     * Adds a primary key column and its associated WhereClause.
     *
     * @param primaryColumnsData the {@link DataWrapper.PrimaryWrapper} instance with the where clause and the map with the values that need to be updated.
     */
    public void addQueryData(@Nonnull final DataWrapper.PrimaryWrapper primaryColumnsData) {
        primaryWrappers.add(primaryColumnsData);
    }

    /**
     * Returns the primary key column-value mappings.
     *
     * @return List of primary data wrapper.
     */
    @Nonnull
    public List<DataWrapper.PrimaryWrapper> getPrimaryWrappers() {
        return primaryWrappers;
    }

    /**
     * Returns the primary key column-value mappings.
     *
     * @return Returns {@code true} if the column is set.
     */
    public boolean allPrimaryValuesPresent(@Nonnull final Set<String> keys) {
        for (DataWrapper.PrimaryWrapper wrapper : primaryWrappers) {
            for (String key : keys) {
                if (wrapper.getPrimaryValue(key) == null) {
                    return false;
                }
            }
        }
        return true;
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
        return !(primaryWrappers.isEmpty());
    }

    /**
     * Retrieve the loaded data from the database. It will go through every row, and you can use {@link #addQueryData(DataWrapper.PrimaryWrapper)}
     * to set the new primary key value or if you have several, this columns name and value will be updated to the database.
     *
     * @param loadedData the callback when loading the data set in the database.
     * @param clazz      the class to resolve.
     * @param <T>        the generic type for that class that implements ConfigurationSerializable.
     */
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


    /**
     * Retrieve the loaded data from the database. It will go through every row, and you can use {@link #addQueryData(DataWrapper.PrimaryWrapper)}
     * to set the new primary key value or if you have several, this columns name and value will be updated to the database.
     * <p>
     * You will get it as raw data from the database insted of {@link #forEachLoadedData(Consumer, Class)} that gives you a consumer
     * with the raw map with all columns and values set in the database.
     *
     * @param loadedData the callback when loading the data set in the database.
     */
    public void forEachLoadedData(Consumer<Map<String, Object>> loadedData) {
        this.loadMapFromDB = loadedData;
    }

    /**
     * Used internally only this method.
     *
     * @param dataFromDB the map of raw values from the database.
     */
    public void loadMap(Map<String, Object> dataFromDB) {
        if (this.loadMapFromDB == null) return;

        this.loadMapFromDB.accept(dataFromDB);
    }

    /**
     * Used internally only this method.
     *
     * @param primaryKeys the list of primary column and the key to convert to
     * @return the map converted to a column and the object/value.
     */
    public Map<Column, Object> convert(final Map<String, Object> primaryKeys) {
        final Map<Column, Object> map = new HashMap<>();
        primaryKeys.forEach((key, value) -> map.put(new Column(key, ""), value));
        return map;
    }
}
