package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wrapper for managing primary key constraints and associated data for a specific database table.
 *
 * <p>
 * This class is used to track primary key values when constructing SQL {@code WHERE} clauses,
 * performing updates, and its data is used to generate ALTER TABLE constraints after the table is created.
 * Existing rows can be loaded from the database and transformed into {@link DataWrapper.PrimaryWrapper}
 * objects representing primary key mappings and their corresponding {@code WHERE} clauses.
 * </p>
 *
 * <p>
 * The recommended usage is through the {@link #forEachLoadedData(Function, Class)}
 * or {@link #forEachLoadedData(Function)} methods, which automatically store
 * the resulting {@code PrimaryWrapper} objects internally. The {@link #addQueryData(DataWrapper.PrimaryWrapper)}
 * method exists for advanced or manual scenarios but is generally not required.
 * </p>
 *
 * <strong>Example usage</strong>
 * <pre>{@code
 * PrimaryConstraintWrapper primaryWrapper = new PrimaryConstraintWrapper(database, queryTable);
 *
 * primaryWrapper.forEachLoadedData(loadDataWrapper -> {
 *     Object world = loadDataWrapper.getPrimaryValue("world");
 *     Object x = loadDataWrapper.getPrimaryValue("x");
 *     Object y = loadDataWrapper.getPrimaryValue("y");
 *     Object z = loadDataWrapper.getPrimaryValue("z");
 *
 *     Map<String, Object> map = Map.of(
 *         "world", world,
 *         "x", x,
 *         "y", y,
 *         "z", z
 *     );
 *
 *     return new DataWrapper.PrimaryWrapper(map, whereClause -> whereClause
 *         .where("world").equal(world).and()
 *         .where("x").equal(x).and()
 *         .where("y").equal(y).and()
 *         .where("z").equal(z)
 *     );
 * }, YourConfigurationSerializableClass.class);
 * }</pre>
 */

public class PrimaryConstraintWrapper {
    private final List<DataWrapper.PrimaryWrapper> primaryWrappers = new ArrayList<>();
    private final Database database;
    private final SqlQueryTable queryTable;
    private Consumer<Map<String, Object>> loadMapFromDB;
    private boolean unique;

    /**
     * Constructs a {@code PrimaryConstraintWrapper} for managing primary key constraints
     * on a specific database table.
     *
     * @param database   the database instance to interact with. Used for
     *                   deserialization and accessing table metadata.
     * @param queryTable the table whose primary key constraints are being
     *                   managed or modified.
     */
    public PrimaryConstraintWrapper(@Nonnull final Database database, @Nonnull final SqlQueryTable queryTable) {
        this.database = database;
        this.queryTable = queryTable;
    }

    /**
     * Adds a primary key mapping manually.
     *
     * <p>
     * Normally, this is handled automatically by {@link #forEachLoadedData(Function, Class)}
     * or {@link #forEachLoadedData(Function)} when the callback returns a
     * {@link DataWrapper.PrimaryWrapper}. This method exists for advanced scenarios
     * where you want to populate primary key data manually. Safeguards prevent
     * incomplete or inconsistent primary key mappings from affecting the database.
     * </p>
     *
     * @param primaryColumnsData the {@link DataWrapper.PrimaryWrapper} containing
     *                           column-value mappings and the corresponding {@code WHERE} clause
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
     * Checks whether all specified primary key columns have non-null values
     * in the internal cache.
     *
     * <p>
     * This can be used to verify that the necessary primary key values are present
     * before performing updates or generating queries.
     * </p>
     *
     * @param keys the set of primary key column names to check
     * @return {@code true} if all specified keys have non-null values in every
     *         {@link DataWrapper.PrimaryWrapper} stored internally, {@code false} otherwise
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
        return !primaryWrappers.isEmpty();
    }

    /**
     * Loads each row of data from the database and transforms it into a
     * {@link DataWrapper.PrimaryWrapper} using the provided callback function.
     * The resulting wrapper is stored internally.
     *
     * @param loadedData the function transforming loaded data into a {@code PrimaryWrapper}
     * @param clazz      the class type to deserialize each row into
     * @param <T>        the type implementing {@link ConfigurationSerializable}
     */
    public <T extends ConfigurationSerializable> void forEachLoadedData(Function<LoadDataWrapper<T>, DataWrapper.PrimaryWrapper> loadedData, Class<T> clazz) {
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
            final DataWrapper.PrimaryWrapper data = loadedData.apply(new LoadDataWrapper<>(objectList, deserialize));
            if (data != null) {
                this.addQueryData(data);
            }
        };
    }

    /**
     * Loads each row of raw data from the database and transforms it into a
     * {@link DataWrapper.PrimaryWrapper} using the provided callback function.
     * The resulting wrapper is stored internally.
     *
     * @param loadedData the function transforming raw database rows into a {@code PrimaryWrapper}
     */
    public void forEachLoadedData(Function<Map<String, Object>, DataWrapper.PrimaryWrapper> loadedData) {
        this.loadMapFromDB = (dataFromDB) -> {
            final DataWrapper.PrimaryWrapper data = loadedData.apply(dataFromDB);
            if (data != null) {
                this.addQueryData(data);
            }
        };
    }


    /**
     * Converts a map of primary key column-value pairs to a map keyed by {@link Column} objects.
     *
     * @param primaryKeys a map of column names to values
     * @return a map of {@link Column} objects to their corresponding values
     */
    public Map<Column, Object> convert(final Map<String, Object> primaryKeys) {
        final Map<Column, Object> map = new HashMap<>();
        primaryKeys.forEach((key, value) -> map.put(new Column(key, ""), value));
        return map;
    }

    /**
     * Internally loads a row of data from the database and invokes the configured callback.
     *
     * @param dataFromDB a map of column-value pairs from the database row
     */
    public void loadMap(Map<String, Object> dataFromDB) {
        if (this.loadMapFromDB == null) return;

        this.loadMapFromDB.accept(dataFromDB);
    }

}
