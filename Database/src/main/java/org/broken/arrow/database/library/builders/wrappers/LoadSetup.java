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
     * Applies the configured database settings if present.
     *
     * @param settings the {@link DatabaseSettingsLoad} instance to configure
     */
    public void applyConfigure(final DatabaseSettingsLoad settings) {
        if (this.settings == null)
            return;
        this.settings.accept(settings);
    }



    public DatabaseQueryHandler<LoadDataWrapper<T>> getDatabaseHandler() {
        return databaseHandler;
    }

}
