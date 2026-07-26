package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.WriteContext;
import org.broken.arrow.library.database.builders.schema.TableSchema;
import org.broken.arrow.library.database.construct.query.builder.table.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Context provided during database schema migrations to manage and populate new primary key constraints.
 *
 * <p>
 * When a new primary key column is added to an existing table, existing rows in the database
 * often need to be populated with valid values before the {@code PRIMARY KEY} constraint can be safely applied.
 * This class acts as a callback mechanism, allowing you to define how existing rows are transformed
 * into {@link WriteContext} objects (which hold the new primary key values and the necessary {@code WHERE} clause).
 * </p>
 *
 * <p>
 * The recommended usage is through the {@link #forEachLoadedData(Function, Class)}
 * or {@link #forEachLoadedData(Function)} methods. These iterate over existing database rows,
 * applying your custom logic to map old data to the new constraint requirements. The {@link #addQueryData(WriteContext)}
 * method exists for advanced or manual scenarios but is generally not required.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> This class is instantiated internally by the schema migration engine and passed
 * to your constraint handler callback. It is not intended to be instantiated or extended.
 * </p>
 *
 * <strong>Example usage:</strong>
 * <pre>{@code
 * primaryWrapper.forEachLoadedData(loadDataWrapper -> {
 *     Object world = loadDataWrapper.getPrimaryValue("world");
 *     Object x = loadDataWrapper.getPrimaryValue("x");
 *     Object y = loadDataWrapper.getPrimaryValue("y");
 *     Object z = loadDataWrapper.getPrimaryValue("z");
 *
 *     // Highly recommend setting the WHERE clause to accurately target this specific row;
 *     // otherwise, the fallback will join all provided values with AND clauses.
 *     return WriteContext.with("world", world)
 *         .put("x", x)
 *         .put("y", y)
 *         .put("z", z)
 *         .withWhereClause(whereBuilder -> whereBuilder
 *             .where("world").equal(world).and()
 *             .where("x").equal(x).and()
 *             .where("y").equal(y).and()
 *             .where("z").equal(z)
 *         );
 * // The class below implements:
 * // org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable
 * }, OurConfigurationSerializableClass.class);
 * }</pre>
 */
public class PrimaryKeyMigrationContext {
    private final List<WriteContext> primaryWrappers = new ArrayList<>();
    private final Database database;
    private final TableSchema queryTable;
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
    public PrimaryKeyMigrationContext(@Nonnull final Database database, @Nonnull final TableSchema queryTable) {
        this.database = database;
        this.queryTable = queryTable;

    }

    /**
     * Adds a primary key mapping manually.
     *
     * <p>
     * Normally, this is handled automatically by {@link #forEachLoadedData(Function, Class)}
     * or {@link #forEachLoadedData(Function)} when the callback returns a
     * {@link WriteContext}. This method exists for advanced scenarios
     * where you want to populate primary key data manually. Safeguards prevent
     * incomplete or inconsistent primary key mappings from affecting the database.
     * </p>
     *
     * @param primaryColumnsData the {@link WriteContext} containing
     *                           column-value mappings and the corresponding {@code WHERE} clause
     */
    public void addQueryData(@Nonnull final WriteContext primaryColumnsData) {
        primaryWrappers.add(primaryColumnsData);
    }

    /**
     * Returns the primary key column-value mappings.
     *
     * @return List of primary data wrapper.
     */
    @Nonnull
    public List<WriteContext> getWriteContext() {
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
     *         {@link WriteContext} stored internally, {@code false} otherwise
     */
    public boolean allPrimaryValuesPresent(@Nonnull final Set<String> keys) {
        for (WriteContext wrapper : primaryWrappers) {
            for (String key : keys) {
                if (wrapper.getValue(key) == null) {
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
     * @return Returns {@code true} if the migration should apply a UNIQUE constraint
     * as a fallback when incomplete primary key values are provided.
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Set the constraint if it shall replace if no value is provided.
     *
     * @param unique Sets whether to fall back to a UNIQUE
     *               constraint if primary key values are missing for some rows.
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
     * {@link WriteContext} using the provided callback function.
     * The resulting wrapper is stored internally.
     *
     * @param loadedData the function transforming loaded data into a {@code PrimaryWrapper}
     * @param clazz      the class type to deserialize each row into
     * @param <T>        the type implementing {@link ConfigurationSerializable}
     */
    public <T extends ConfigurationSerializable> void forEachLoadedData(Function<LoadDataWrapper<T>, WriteContext> loadedData, Class<T> clazz) {
        final CreateTableHandler tableHandler = this.queryTable.getTable();
        this.loadMapFromDB = (dataFromDB) -> {
            final T deserialize = this.database.deSerialize(clazz, dataFromDB);
            final List<TableColumn> primaryColumns = tableHandler.getPrimaryColumns();
            final Map<String, Object> objectList = new HashMap<>();
            if (!primaryColumns.isEmpty()) {
                for (Column column : primaryColumns) {
                    Object primaryValue = dataFromDB.get(column.getColumnName());
                    objectList.put(column.getColumnName(), primaryValue);
                }
            }
            final WriteContext data = loadedData.apply(new LoadDataWrapper<>(objectList, deserialize));
            if (data != null) {
                this.addQueryData(data);
            }
        };
    }

    /**
     * Loads each row of raw data from the database and transforms it into a
     * {@link WriteContext} using the provided callback function.
     * The resulting wrapper is stored internally.
     *
     * @param loadedData the function transforming raw database rows into a {@code PrimaryWrapper}
     */
    public void forEachLoadedData(Function<Map<String, Object>, WriteContext> loadedData) {
        this.loadMapFromDB = (dataFromDB) -> {
            final WriteContext data = loadedData.apply(dataFromDB);
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
