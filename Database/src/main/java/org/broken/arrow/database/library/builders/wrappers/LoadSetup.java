package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import java.util.function.Consumer;

/**
 * Represents a setup for loading data from a database, encapsulating both
 * the configuration of database settings and the logic for preparing query contexts.
 *
 * @param <T> the type of the value, which must implement {@link ConfigurationSerializable}
 */
public class LoadSetup<T extends ConfigurationSerializable> {

    private final DatabaseQueryHandler<LoadDataWrapper<T>> databaseHandler;
    private Consumer<DatabaseSettingsLoad> settings;
    private Consumer<LoadDataWrapper<T>> context;

    public LoadSetup(DatabaseQueryHandler<LoadDataWrapper<T>> databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    /**
     * Configures database-specific settings such as the table name, update flag, and option to filter out columns.
     *
     * @param settings a consumer that applies configuration to a {@link DatabaseSettingsLoad} object.
     */
    public void configure(final Consumer<DatabaseSettingsLoad> settings) {
        this.settings = settings;
    }

    /**
     * Sets the logic to prepare query data for each entry in the cache.
     * Don't forget to define your primary key(s) or equivalent identifiers not present
     * in your {@link ConfigurationSerializable} implementation, by using
     * {@link SaveRecord#addKeys(String, Object)}.
     *
     * @param queryResult a consumer that populates or modifies a {@link LoadDataWrapper} for loading.
     */
    public void forEachLoadedQuery(final Consumer<LoadDataWrapper<T>> queryResult) {
        this.context = queryResult;
    }

    /**
     * Applies the configured database settings if present.
     *
     * @param settings the {@link DatabaseSettingsLoad} instance to configure
     */
    public void applyConfigure(final DatabaseSettingsLoad settings) {
        if (this.settings == null)
            return;
        this.settings.accept(settings);
    }

    /**
     * Applies the query preparation logic for a given loaded value if it set and data is found.
     *
     * @param loadDataWrapper the {@link LoadDataWrapper} instance to apply found query data to.
     */
    public void applyWrapper(final LoadDataWrapper<T> loadDataWrapper) {
        if (this.context == null)
            return;
        context.accept(loadDataWrapper);
    }

    public DatabaseQueryHandler<LoadDataWrapper<T>> getDatabaseHandler() {
        return databaseHandler;
    }

}
