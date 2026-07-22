package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.builders.wrappers.query.QueryContext;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import java.util.function.Consumer;

/**
 * Represents configuration for a save operation into a database.
 * <p>
 * A {@code SaveSetup} allows customizing how data is stored in the target table,
 * including whether rows should be inserted or updated, and which columns should
 * be included or excluded in the operation.
 * </p>
 *
 * @param <K> The type of the key used in the original cache map.
 * @param <V> The type of the value, which must implement {@link ConfigurationSerializable}.
 */
public class SaveSetup<K, V extends ConfigurationSerializable> extends QueryContext<SaveRecord<K, V>> {
    private Consumer<DatabaseSettingsSave> settings;

    /**
     * Creates a new SaveSetup instance.
     */
    public SaveSetup() {
    }

    /**
     * Defines database-specific settings such as table name, update behavior,
     * and optional column filtering.
     *
     * @param settings a consumer that applies configuration to a {@link DatabaseSettingsSave} instance
     */
    public void configure(final Consumer<DatabaseSettingsSave> settings) {
        this.settings = settings;
    }

    /**
     * Applies the configured database settings to the provided instance.
     *
     * @param settings the {@link DatabaseSettingsSave} object to configure
     */
    public void applyConfigure(final DatabaseSettingsSave settings) {
        if (this.settings == null)
            return;
        this.settings.accept(settings);
    }

}