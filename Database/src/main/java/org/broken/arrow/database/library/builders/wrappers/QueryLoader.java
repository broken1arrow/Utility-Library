package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.database.library.construct.query.utlity.QueryDefinition;
import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.database.library.core.SQLDatabaseQuery;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.logging.library.Logging;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

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


    public QueryLoader(@Nonnull final SQLDatabaseQuery sqlDatabaseQuery, @Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final Consumer<LoadSetup<T>> setup) {
        super(sqlDatabaseQuery, tableName);
        this.clazz = clazz;
        this.setup = setup;
        this.databaseSettings = new DatabaseSettingsLoad(tableName);
        this.databaseQueryHandler = new DatabaseQueryHandler<>(databaseSettings);
    }

    public void load() {
        this.executeLoadQuery();
    }

    /**
     * If you don't want to get data in the {@link #forEachQuery(Consumer)} you can get all processed values here.
     *
     * @return list with all found rows and the filtered keys if it set.
     */
    @Nonnull
    public List<LoadDataWrapper<T>> getCachedResult() {
        return databaseQueryHandler.getData();
    }


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
