package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import java.util.function.Consumer;

/**
 * Represents a setup for saving data to a database, encapsulating both
 * the configuration of database settings and the logic for preparing query contexts.
 *
 * @param <K> the type of the map key used in the save operation
 * @param <V> the type of the value, which must implement {@link ConfigurationSerializable}
 */
public class SaveSetup<K, V extends ConfigurationSerializable> {

    private Consumer<DatabaseSettingsSave> settings;

    /**
     * Configures database-specific settings such as the table name, update flag, and option to filter out columns.
     *
     * @param settings a consumer that applies configuration to a {@link DatabaseSettingsSave} object.
     */
    public void configure(final Consumer<DatabaseSettingsSave> settings) {
        this.settings = settings;
    }

    /**
     * Applies the configured database settings if set.
     *
     * @param settings the {@link DatabaseSettingsSave} instance to configure
     */
    public void applyConfigure(final DatabaseSettingsSave settings) {
        if (this.settings == null)
            return;
        this.settings.accept(settings);
    }

}