package org.broken.arrow.library.database.builders.wrappers;

import javax.annotation.Nonnull;

/**
 * Configuration settings used when saving data to the database.
 */
public class DatabaseSettingsSave extends DatabaseSettings {

    private boolean shallUpdate;

    /**
     * Creates a new save settings object for the specified table.
     *
     * @param tableName the name of the database table this configuration applies to; must not be {@code null}
     */
    public DatabaseSettingsSave(@Nonnull final String tableName) {
        super(tableName);

    }

    /**
     * Indicates whether existing rows should be updated instead of replaced.
     *
     * @return {@code true} if updates to existing rows are enabled; {@code false} if rows will be replaced
     */
    public boolean isShallUpdate() {
        return shallUpdate;
    }

    /**
     * Sets the flag to update existing rows instead of replacing them.
     * <p>
     * When set to {@code true}, existing rows matching the criteria will be updated.
     * If set to {@code false}, existing rows will be fully replaced.
     * The update behavior is automatically applied only if the row exists.
     * <p>
     * You can also use filtering options to control which columns are updated when rows exist.
     *
     * @param shallUpdate {@code true} to update existing rows; {@code false} to replace them entirely
     */
    public void setShallUpdate(boolean shallUpdate) {
        this.shallUpdate = shallUpdate;
    }
}
