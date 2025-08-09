package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

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

    /**
     * Constructs a new LoadSetup instance with the specified database query handler.
     *
     * @param databaseHandler the database handler used to load data with the configured settings
     */
    public LoadSetup(DatabaseQueryHandler<LoadDataWrapper<T>> databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    /**
     * Configures database-specific settings such as table name, update flags, or column filters.
     *
     * @param settings a consumer that applies configuration to a {@link DatabaseSettingsLoad} instance
     */
    public void configure(final Consumer<DatabaseSettingsLoad> settings) {
        this.settings = settings;
    }


    /**
     * Applies the previously configured database settings to the provided settings instance.
     * If no configuration has been set, this method does nothing.
     *
     * @param settings the {@link DatabaseSettingsLoad} instance to be configured
     */
    public void applyConfigure(final DatabaseSettingsLoad settings) {
        if (this.settings == null)
            return;
        this.settings.accept(settings);
    }

    /**
     * Retrieves the database query handler associated with this setup.
     *
     * @return the database query handler instance
     */
    public DatabaseQueryHandler<LoadDataWrapper<T>> getDatabaseHandler() {
        return databaseHandler;
    }

}
