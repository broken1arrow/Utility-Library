package org.broken.arrow.library.database.utility.constrains;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.tables.SqlQueryTable;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.CreateTableHandler;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.AlterTable;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.database.utility.DatabaseType;
import org.broken.arrow.library.database.utility.PrimaryConstraintWrapper;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * Synchronizes an existing database table with its declared table definition.
 *
 * <p>This class handles post-creation schema adjustments by:</p>
 * <ul>
 *     <li>Creating columns that exist in the table definition but are missing in the database.</li>
 *     <li>Detecting newly declared PRIMARY KEY columns.</li>
 *     <li>Populating primary key values for existing rows via a configured callback.</li>
 *     <li>Applying PRIMARY KEY or UNIQUE constraints depending on data completeness and configuration.</li>
 * </ul>
 *
 * <p>If constraint changes are required and the database does not support direct
 * primary key modification (e.g., SQLite), the table is recreated inside a transaction.</p>
 *
 * <p>This class only adds missing columns and updates constraints safely.
 * It does not remove existing columns or automatically drop unrelated constraints.</p>
 *
 * <p><strong>Requirements:</strong> A constraint handler must be configured via
 * {@link Database#getHandleConstraints()} to apply new constraints.
 * Otherwise, only missing columns are created.
 * </p>
 */
public class SchemaMigrationHandler {
    private final Logging log = new Logging(SchemaMigrationHandler.class);
    private final Connection connection;
    private final Database databaseCore;

    /**
     * Creates a new schema migration handler.
     *
     * @param databaseCore The database configuration and migration behavior controller.
     * @param connection   Active SQL connection used for schema and data updates.
     */
    public SchemaMigrationHandler(@Nonnull final Database databaseCore, final Connection connection) {
        this.connection = connection;
        this.databaseCore = databaseCore;
    }

    /**
     * Detects and creates columns that exist in the table definition
     * but are missing in the actual database table.
     *
     * <p>If new PRIMARY KEY columns are detected, a migration process
     * will be triggered to populate values and apply constraints.</p>
     *
     * @param queryTable      The table definition wrapper.
     * @param existingColumns List of column names currently present in the database (lowercase expected).
     *
     */
    public void createMissingColumns(final SqlQueryTable queryTable, final List<String> existingColumns) {
        if (existingColumns == null) return;
        if (this.connection == null) {
            log.log(Level.WARNING, () -> "You must set the connection instance.");
            return;
        }
        final Set<String> newPrimaryKeys = new HashSet<>();
        final List<Column> columnsToAdd = new ArrayList<>();
        boolean failCreateColumns = false;

        for (final Column column : queryTable.getTable().getColumns()) {
            String columnName = column.getColumnName();
            if (databaseCore.getRemoveColumns().contains(columnName) || existingColumns.contains(columnName.toLowerCase()))
                continue;
            Column tableColumn = column;

            if (column instanceof TableColumn) {
                final boolean isPrimaryKey = ((TableColumn) column).isPrimaryKey();
                if (isPrimaryKey) {
                    newPrimaryKeys.add(columnName);
                    tableColumn = new TableColumn(null, column.getColumnName(), ((TableColumn) column).getDataType());
                }
            }
            columnsToAdd.add(tableColumn);
        }


        if (!columnsToAdd.isEmpty()) {
            failCreateColumns = executeCreation(queryTable, columnsToAdd, failCreateColumns);
        }
        if (failCreateColumns) {
            log.log(Level.SEVERE, () -> "Schema migration aborted for " + queryTable.getTableName() +
                    " due to previous errors. The constraints will not be set.");
            return;
        }

        this.preparePrimaryKeyMigration(queryTable, newPrimaryKeys);
    }

    private boolean executeCreation(@Nonnull final SqlQueryTable queryTable, @Nonnull final List<Column> columnsToAdd, @Nonnull boolean failCreateColumns) {
        if (this.databaseCore.getDatabaseType() == DatabaseType.SQLITE) {
            for (Column col : columnsToAdd) {
                final QueryBuilder queryBuilder = new QueryBuilder();
                final AlterTable alterBuilder = queryBuilder.alterTable(queryTable.getTableName());
                alterBuilder.add(col);
                final String query = queryBuilder.build();
                try (final PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.execute();
                    log.log(Level.FINE, () -> "Successfully added column " + col.getColumnName() + " to " + queryTable.getTableName());
                } catch (final SQLException throwable) {
                    log.log(throwable, () -> "Failed to add missing column " + col.getColumnName() + " . Query: '" + query + "'");
                    failCreateColumns = true;
                }
            }
        } else {
            final QueryBuilder queryBuilder = new QueryBuilder();
            final AlterTable alterBuilder = queryBuilder.alterTable(queryTable.getTableName());
            for (Column col : columnsToAdd) {
                alterBuilder.add(col);
            }
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
                log.log(Level.FINE, () -> "Successfully added " + columnsToAdd.size() + " columns to " + queryTable.getTableName());
            } catch (final SQLException throwable) {
                log.log(throwable, () -> "Failed to add missing columns in batch. Query: '" + query + "'");
                failCreateColumns = true;
            }
        }
        return failCreateColumns;
    }

    private void preparePrimaryKeyMigration(final SqlQueryTable queryTable, final Set<String> newPrimaryKeys) {
        if (newPrimaryKeys.isEmpty()) {
            log.log(Level.FINE, () -> "No new primary key columns detected. Skipping primary key migration for table '" + queryTable.getTableName() + "'");
            return;
        }
        final BiConsumer<String, PrimaryConstraintWrapper> handleConstraints = this.databaseCore.getHandleConstraints();
        Validate.checkNotNull(handleConstraints, "Constraint handler not configured. You must provide a callback to define how constraints should be applied to newly created columns.");
        final PrimaryConstraintWrapper primaryWrapper = new PrimaryConstraintWrapper(this.databaseCore, queryTable);
        handleConstraints.accept(queryTable.getTableName(), primaryWrapper);

        final QueryBuilder builder = new QueryBuilder();
        builder.select(new ColumnManager().column("*").finish()).from(queryTable.getTableName());
        final String builtQuery = builder.build();
        try (final ResultSet resultSet = this.connection.prepareStatement(builtQuery).executeQuery()) {
            while (resultSet.next()) {
                final CreateTableHandler tableHandler = queryTable.getTable();
                final Map<String, Object> dataFromDB = this.databaseCore.getDataFromDB(resultSet, tableHandler.getColumns());
                primaryWrapper.loadMap(dataFromDB);
            }
        } catch (final SQLException throwable) {
            log.log(throwable, () -> "Failed to read existing rows while preparing primary key migration. The query '" + builtQuery + "' and this table '" + queryTable.getTableName() + "'");
        }
        Validate.checkBoolean(!primaryWrapper.isSet() && !primaryWrapper.isUnique(), "Primary key creation requested, but required values are missing. Provide values for all primary key columns or configure UNIQUE fallback instead.");

        boolean primaryMapValuesSet = saveDataToColumns(queryTable, primaryWrapper);
        Validate.checkBoolean(!primaryMapValuesSet && !primaryWrapper.isUnique(), "Primary key creation failed. One or more primary columns contain null " +
                "values, and UNIQUE fallback is disabled. Either provide values for all primary columns or enable UNIQUE fallback.");

        boolean primaryValuesComplete = primaryWrapper.allPrimaryValuesPresent(newPrimaryKeys);
        Validate.checkBoolean(!primaryValuesComplete && !primaryWrapper.isUnique(), "Primary key creation failed. Not all columns marked as primary received values during migration. Ensure all PRIMARY KEY columns are populated, or enable UNIQUE fallback. ");

        this.setConstraints(queryTable, newPrimaryKeys, primaryMapValuesSet && primaryValuesComplete);
    }

    private boolean saveDataToColumns(final SqlQueryTable queryTable, final PrimaryConstraintWrapper primaryWrapper) {
        boolean primaryMapValuesSet = true;
        final Map<String, List<Map<Integer, Object>>> batchGroups = new LinkedHashMap<>();

        if (!primaryWrapper.getPrimaryWrappers().isEmpty()) {
            primaryMapValuesSet = setValuesToDatabase(queryTable, primaryWrapper, primaryMapValuesSet, batchGroups);
        }

        if (!batchGroups.isEmpty()) {
            for (Map.Entry<String, List<Map<Integer, Object>>> entry : batchGroups.entrySet()) {
                String sql = entry.getKey();
                List<Map<Integer, Object>> allRowsParams = entry.getValue();

                try (final PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
                    for (Map<Integer, Object> rowParams : allRowsParams) {
                        for (Map.Entry<Integer, Object> param : rowParams.entrySet()) {
                            preparedStatement.setObject(param.getKey(), param.getValue());
                        }
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                } catch (final SQLException throwable) {
                    log.log(throwable, () -> "Failed to populate primary key values. SQL: '" + sql + "'. Table: '" + queryTable.getTableName() + "'");
                }
            }
        }

        return primaryMapValuesSet;
    }

    private boolean setValuesToDatabase(final SqlQueryTable queryTable, final PrimaryConstraintWrapper primaryWrapper, boolean primaryMapValuesSet, final Map<String, List<Map<Integer, Object>>> batchGroups) {
        for (DataWrapper.PrimaryWrapper primary : primaryWrapper.getPrimaryWrappers()) {
            if (primary == null) {
                log.log(Level.WARNING, () -> "A row for this table '" + queryTable.getTableName() + "' is not set.");
                continue;
            }

            final QueryBuilder saveBuilder = new QueryBuilder();
            final Map<String, Object> primaryKeys = primary.getPrimaryKeys();

            if (primaryKeys.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
                this.sendLogMessage(primaryWrapper, primaryKeys);
                primaryMapValuesSet = false;
            }
            Selector<ColumnBuilder<Column, Void>, Column> update = saveBuilder
                    .update(queryTable.getTableName())
                    .putAll(primaryWrapper.convert(primaryKeys))
                    .getSelector()
                    .where(whereBuilder -> {
                        if (primary.getWhereClause() == null)
                            return null;
                        return primary.getWhereClause().apply(whereBuilder);
                    });
            if (update.getWhereBuilder() == null) {
                log.log(Level.WARNING, () -> "Update skipped, no WHERE clause was provided. For this table '" + queryTable.getTableName() + "'" + ". Updates without a WHERE clause are not allowed for safety reasons.");
            } else {
                String sql = saveBuilder.build();
                batchGroups.computeIfAbsent(sql, k -> new ArrayList<>()).add(saveBuilder.getValues());
            }
        }
        return primaryMapValuesSet;
    }


    private void setConstraints(final SqlQueryTable queryTable, final Set<String> newPrimaryKeys, final boolean primaryValuesComplete) {
        final List<Column> columnsToBeModified = new ArrayList<>();
        final List<Column> primaryColumns = queryTable.getPrimaryColumns();

        for (final Column column : primaryColumns) {
            boolean addingPrimary = false;
            if (primaryValuesComplete) {
                columnsToBeModified.add(column);
                addingPrimary = true;
            }
            if (addingPrimary || !newPrimaryKeys.contains(column.getColumnName())) {
                continue;
            }
            columnsToBeModified.add(column);
        }

        if (!columnsToBeModified.isEmpty() && this.databaseCore.getDatabaseType() == DatabaseType.SQLITE) {
            copyTable(queryTable, columnsToBeModified);
            return;
        }
        final String tableName = queryTable.getTableName();

        if (!columnsToBeModified.isEmpty() && !primaryValuesComplete) {
            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.alterTable(tableName).setConstraints(modifyConstraints ->
                    modifyConstraints.addUnique(columnsToBeModified.stream().map(Column::getColumnName).toArray(String[]::new)));
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> getMessage("Failed to apply UNIQUE constraint during primary key migration. Columns '", tableName, columnsToBeModified));
            }
        }
        if (!columnsToBeModified.isEmpty() && primaryValuesComplete) {
            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.alterTable(tableName).setConstraints(modifyConstraints -> {
                modifyConstraints.dropPrimaryKey();
                modifyConstraints.addPrimaryKey(columnsToBeModified.stream().map(Column::getColumnName).toArray(String[]::new));
            });
            final String query = queryBuilder.build();
            try (final PreparedStatement statement = this.connection.prepareStatement(query)) {
                statement.execute();
            } catch (final SQLException throwable) {
                log.log(throwable, () -> getMessage("Failed to apply PRIMARY KEY constraint during migration. Columns ", tableName, columnsToBeModified));
            }
        }
    }

    private void copyTable(@Nonnull final SqlQueryTable queryTable, @Nonnull final List<Column> columnsToBeModified) {
        boolean autoCommit = false;
        final Connection databaseConnection = this.connection;
        try {
            autoCommit = databaseConnection.getAutoCommit();
            databaseConnection.setAutoCommit(false);
            recreateTable(queryTable, columnsToBeModified);
            databaseConnection.commit();
        } catch (SQLException e) {
            try {
                databaseConnection.rollback();
            } catch (SQLException ex) {
                log.log(ex, () -> "could not rollback the changes");
            }
            log.log(e, () -> "Failed to change the contains on the SSQLite database.");
        } finally {
            try {
                databaseConnection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                log.log(e, () -> "Failed to set auto commit back on the SSQLite database.");
            }
        }
    }

    private void recreateTable(final SqlQueryTable queryTable, final List<Column> columnsToBeModified) {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final String tableName = queryTable.getTableName();
        final String temporaryTable = tableName + "_new";

        queryBuilder.createTable(temporaryTable).addAllColumns(queryTable.getColumns());
        final String query = queryBuilder.build();
        final Connection databaseConnection = this.connection;

        try (final PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to create table during primary key migration. Columns ", tableName, columnsToBeModified));
        }

        final QueryBuilder queryInsertBuilder = new QueryBuilder();
        queryInsertBuilder.insertInto(temporaryTable, insertHandler -> {
            insertHandler.addAll(queryTable.getColumns()).getQueryModifier()
                    .select(columnBuilder ->
                            columnBuilder.addAll(queryTable.getColumns()))
                    .from(tableName);
        });
        final String insertQuery = queryInsertBuilder.build();
        try (final PreparedStatement statement = databaseConnection.prepareStatement(insertQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to create table during primary key migration. Query ", tableName, insertQuery));
        }
        //todo handle when the the the primary column is an index.
        // updateIndex(connection, tableName);

        final QueryBuilder queryDropBuilder = new QueryBuilder();
        queryDropBuilder.dropTable(tableName);
        final String dropQuery = queryDropBuilder.build();
        try (final PreparedStatement statement = databaseConnection.prepareStatement(dropQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to drop table during primary key migration. Query ", tableName, dropQuery));
        }

        final QueryBuilder queryAlterBuilder = new QueryBuilder();
        queryAlterBuilder.alterTable(temporaryTable).rename(tableName);
        final String alterQuery = queryAlterBuilder.build();
        try (final PreparedStatement statement = databaseConnection.prepareStatement(alterQuery)) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to alter table during primary key migration. Query ", tableName, alterQuery));
        }
    }

    private void updateIndex(final Connection connection, final String tableName) {

        final QueryBuilder incrementIndexBuilder = new QueryBuilder();
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", ""));
        columns.add(new Column("seq", ""));

        incrementIndexBuilder.insertOrReplaceInto("sqlite_sequence", insertHandler -> insertHandler.addAll(columns)
                .getQueryModifier()
                .select(columnBuilder -> {
                    columnBuilder.add(new Column("'" + tableName + "'", ""));
                    columnBuilder.add(new Column("id", "").setAggregation()
                            .withAggregation(CalcFunc.MAX).getColumn());
                })
                .from(tableName)
        );

        try (final PreparedStatement statement = connection.prepareStatement(incrementIndexBuilder.build())) {
            statement.execute();
        } catch (final SQLException throwable) {
            log.log(throwable, () -> getMessage("Failed to update index during primary key migration. Query ", tableName, incrementIndexBuilder));
        }
    }

    private String getMessage(final String message, final String tableName, final Object columnsToBeModified) {
        return message + "'" + columnsToBeModified + "'. To this table '" + tableName + "'";
    }

    private void sendLogMessage(final PrimaryConstraintWrapper primaryWrapper, final Map<String, Object> primaryKeys) {
        if (primaryWrapper.isUnique()) {
            log.log(Level.FINE, () -> "Primary key values are incomplete (null key or value detected). Provided values: '" + primaryKeys + "'. Primary key will not be created for this row. Unique constraint will be used instead, as configured.");
        } else {
            log.log(Level.FINE, () -> "Primary key values are incomplete (null key or value detected). Provided values: '" + primaryKeys + "' . Primary key cannot be created and UNIQUE fallback is disabled. Migration will be aborted.");
        }
    }
}
