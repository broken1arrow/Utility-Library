package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.utlity.QueryDefinition;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.database.core.SQLDatabaseQuery;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * QueryLoader is a specialized query context that loads and processes data from a database
 * into objects of type {@code T}, which must implement {@link ConfigurationSerializable}.
 * <p>
 * This class handles building and executing SELECT queries with support for
 * deserialization of result rows into domain objects, caching results, and applying
 * user-defined setup logic through a consumer.
 *
 * @param <T> the type of objects to load, which must implement {@link ConfigurationSerializable}
 */
public class QueryLoader<T extends ConfigurationSerializable> extends QueryContext<LoadDataWrapper<T>> {
    @Nonnull
    private final Logging log = new Logging(QueryLoader.class);
    @Nonnull
    private final Class<T> clazz;
    @Nonnull
    private final Consumer<LoadSetup<T>> setup;
    @Nonnull
    private final DatabaseSettingsLoad databaseSettings;
    @Nonnull
    private final DatabaseQueryHandler<LoadDataWrapper<T>> databaseQueryHandler;

    /**
     * Constructs a new QueryLoader instance.
     *
     * @param sqlDatabaseQuery the underlying database query executor (never null)
     * @param tableName the name of the database table to query (never null)
     * @param clazz the class of the type {@code T} to deserialize rows into (never null)
     * @param setup a consumer that configures the loading setup, e.g. specifying filters or columns (never null)
     */
    public QueryLoader(@Nonnull final SQLDatabaseQuery sqlDatabaseQuery, @Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final Consumer<LoadSetup<T>> setup) {
        super(sqlDatabaseQuery, tableName);
        this.clazz = clazz;
        this.setup = setup;
        this.databaseSettings = new DatabaseSettingsLoad(tableName);
        this.databaseQueryHandler = new DatabaseQueryHandler<>(databaseSettings);
    }

    /**
     * Executes the configured load query, applying the setup consumer logic.
     * <p>
     * This method internally builds and executes the SELECT query, deserializes each
     * row into an object of type {@code T}, wraps it in a {@link LoadDataWrapper}, and caches results.
     */
    public void load() {
        this.executeLoadQuery();
    }

    /**
     * Returns the cached results of the query after {@link #load()} has been called.
     * <p>
     * If you prefer not to use {@link #forEachQuery(Consumer)} for processing results,
     * you can retrieve all processed rows here as a list.
     *
     * @return a non-null list of loaded and processed data wrapped in {@link LoadDataWrapper}
     */
    @Nonnull
    public List<LoadDataWrapper<T>> getCachedResult() {
        return databaseQueryHandler.getData();
    }

    /**
     * Internal method to prepare and execute the load query.
     * <p>
     * It applies the user-defined setup, configures the database settings,
     * executes the SQL SELECT query, deserializes each result row, and
     * passes it through any processing logic.
     */
    private void executeLoadQuery() {
        final LoadSetup<T> loadSetup = new LoadSetup<>(databaseQueryHandler);
        this.setup.accept(loadSetup);
        loadSetup.applyConfigure(databaseSettings);

        final QueryBuilder selectTableBuilder = this.databaseQueryHandler.getQueryBuilder();
        SQLDatabaseQuery databaseQuery = this.getSqlDatabaseQuery();
        if (selectTableBuilder == null) {
            log.log(Level.WARNING, () -> "The query is not set: " + databaseQueryHandler + ". Make sure you set your query into the consumer.");
            return;
        }

        databaseQuery.executeQuery(QueryDefinition.of(selectTableBuilder), statementWrapper -> {
            PreparedStatement preparedStatement = statementWrapper.getContextResult();

            if (!selectTableBuilder.getQueryModifier().getWhereBuilder().isEmpty()) {
                selectTableBuilder.getValues().forEach((index, value) -> {
                    try {
                        preparedStatement.setObject(index, value);
                    } catch (SQLException e) {
                        log.log(Level.WARNING, e, () -> "Failed to set where clause values. The values that could not be executed: " + selectTableBuilder.getValues() + ". Check the stacktrace.");
                    }
                });
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final ColumnBuilder<Column, Void> selectBuilder = selectTableBuilder.getQueryModifier().getSelectBuilder();
                    Database database = databaseQuery.getDatabase();

                    final Map<String, Object> dataFromDB = database.getDataFromDB(resultSet, selectBuilder.getColumns());
                    final T deserialize = database.deSerialize(this.clazz, dataFromDB);
                    final Map<String, Object> columnsFiltered = getColumnsFiltered(selectBuilder, databaseQueryHandler, dataFromDB);
                    final LoadDataWrapper<T> loadDataWrapper = new LoadDataWrapper<>(columnsFiltered, deserialize);

                    this.applyQuery(loadDataWrapper);
                    databaseQueryHandler.add(loadDataWrapper);
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, e, () -> "Could not load all data for this table '" + this.getTableName() + "'. Check the stacktrace.");
            }
        });
    }

    /**
     * Filters the columns from the full database row data based on the columns specified in the select builder
     * and the database query handler's filtered columns.
     *
     * @param columnBuilder the column builder that contains the selected columns
     * @param databaseQueryHandler the query handler containing filtered column information
     * @param dataFromDB the full row data fetched from the database
     * @param <V> the type of the ConfigurationSerializable class
     * @return a map containing only the filtered column names and their values
     */
    @Nonnull
    private <V extends ConfigurationSerializable> Map<String, Object> getColumnsFiltered(final ColumnBuilder<Column, Void> columnBuilder, DatabaseQueryHandler<LoadDataWrapper<V>> databaseQueryHandler, Map<String, Object> dataFromDB) {
        final List<Column> columnList = columnBuilder.getColumns();
        final Map<String, Object> columnsFiltered = new HashMap<>();

        if (columnList.isEmpty()) {
            return columnsFiltered;
        }

        for (Column column : columnList) {
            String columnName = column.getColumnName();
            if (databaseQueryHandler.containsFilteredColumn(columnName)) {
                Object primaryValue = dataFromDB.get(columnName);
                columnsFiltered.put(columnName, primaryValue);
            }
        }

        return columnsFiltered;
    }
}
