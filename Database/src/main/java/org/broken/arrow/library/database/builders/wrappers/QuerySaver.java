package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.database.core.SQLDatabaseQuery;
import org.broken.arrow.library.database.utility.BatchExecutor;
import org.broken.arrow.library.database.utility.BatchExecutorUnsafe;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles the persistence of data from an in-memory cache into a database table.
 * <p>
 * This class coordinates mapping the provided cache entries into SQL insert or update
 * statements, applying any configured save strategy, and executing the queries in batch.
 * </p>
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cache value, which must implement {@link ConfigurationSerializable}
 */
public class QuerySaver<K, V extends ConfigurationSerializable> extends QueryContext<SaveRecord<K, V>> {
    @Nonnull
    private final Logging log = new Logging(QueryLoader.class);
    @Nonnull
    private final String tableName;
    @Nonnull
    private final Map<K, V> cacheToSave;
    @Nonnull
    final Consumer<SaveSetup> strategy;

    /**
     * Creates a new {@code QuerySaver} instance responsible for persisting data
     * from a provided cache into a specific database table.
     * <p>
     * This class uses the configured save strategy to map cache entries to the
     * appropriate database columns and executes the generated SQL statements
     * in batch mode.
     * </p>
     *
     * @param sqlDatabaseQuery the active {@link SQLDatabaseQuery} instance
     *                         providing database access and query-building utilities.
     * @param tableName        the name of the database table where data will be stored.
     * @param cacheToSave      the in-memory map containing the entries to be saved,
     *                         where the key is the identifier and the value is
     *                         a {@link ConfigurationSerializable} object.
     * @param strategy         a consumer that configures the {@link SaveSetup} for
     *                         this save operation, defining how data should be mapped
     *                         and what constraints or clauses to apply.
     */
    public QuerySaver(@Nonnull final SQLDatabaseQuery sqlDatabaseQuery, @Nonnull String tableName, @Nonnull Map<K, V> cacheToSave, @Nonnull Consumer<SaveSetup> strategy) {
        super(sqlDatabaseQuery,tableName);
        this.tableName = tableName;
        this.cacheToSave = cacheToSave;
        this.strategy = strategy;
    }

    /**
     * Executes the save process for the provided cache data.
     * <p>
     * The configured {@link SaveSetup} is applied before batch execution.
     * If the database is configured for secure queries, a safe executor is used.
     * </p>
     */
    public void save() {
        Database database = this.getSqlDatabaseQuery().getDatabase();
        final Connection connection = database.attemptToConnect();
        final BatchExecutor<SaveRecord<K, V>> batchExecutor;

        final DatabaseSettingsSave databaseSettings = new DatabaseSettingsSave(this.tableName);
        final DatabaseQueryHandler<SaveRecord<K, V>> databaseQueryHandler = new DatabaseQueryHandler<>(databaseSettings);
        if (connection == null) {
            database.printFailToOpen();
            return;
        }
        final SaveSetup saveSetup = new SaveSetup();
        this.strategy.accept(saveSetup);
        saveSetup.applyConfigure(databaseSettings);

        final List<SaveRecord<K, V>> data = cacheToSave.entrySet().stream().map(kvEntry -> this.applyQuery(new SaveRecord<>(this.tableName, kvEntry))).collect(Collectors.toList());
        if (data.isEmpty()) {
            this.log.log(Level.WARNING, () -> "No data in the map for the table:'" + this.tableName + "' . Just must provide data and also don't forget to set your where clause.");
            return;
        }

        if (database.isSecureQuery())
            batchExecutor = new BatchExecutor<>(database, connection, data);
        else {
            batchExecutor = new BatchExecutorUnsafe<>(database, connection, data);
        }
        batchExecutor.save(tableName, databaseSettings.isShallUpdate(), databaseQueryHandler);
    }

}
