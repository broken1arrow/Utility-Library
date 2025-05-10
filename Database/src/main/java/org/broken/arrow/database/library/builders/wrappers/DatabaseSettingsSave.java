package org.broken.arrow.database.library.builders.wrappers;

import javax.annotation.Nonnull;

public class DatabaseSettingsSave extends DatabaseSettings {

    private boolean shallUpdate;

    public DatabaseSettingsSave(@Nonnull final String tableName) {
        super(tableName);

    }

    public boolean isShallUpdate() {
        return shallUpdate;
    }

    /**
     * Set the update flag if you want t update current row or just replace it. It will automatic detect with option to use,
     * and only use the update if row exist.
     *
     * @param shallUpdate Set to {@code true} if you want to update existing rows. Alternatively, you can use the filter option in {@code settings}
     *                    to control which columns should be updated when a row already exists. If {@code false}, the existing row/rows will be fully replaced.
     */
    public void setShallUpdate(boolean shallUpdate) {
        this.shallUpdate = shallUpdate;
    }
}
