package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.utility.query.build.SqlResultRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Configuration settings used when saving data to the database.
 */
public class DatabaseSettingsSave extends DatabaseSettings {
    private boolean shallUpdate;
    private Consumer<SqlResultRow> callback;

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

    /**
     * Registers a callback to capture and process auto-generated keys or updated column values
     * returned by the database execution engine for this specific data unit.
     * <p>
     * When performing batch insertions or updates, this callback allows developers to intercept
     * database-managed primary keys (such as {@code AUTOINCREMENT} IDs) or structural increments
     * immediately after this payload is written to the table.
     * </p>
     *
     * @param callback The consumer callback that will receive the populated {@link SqlResultRow}; cannot be null.
     * @throws NullPointerException if the provided callback is null.
     */
    public void setGeneratedKeyCallback(@Nonnull final Consumer<SqlResultRow> callback) {
        this.callback = callback;
    }

    /**
     * Retrieves the registered generated-key callback mapped to this payload.
     * <p>
     * Used internally by the execution engine to bridge user-configured callbacks from the high-level
     * data structure down into the final compiled execution parameters.
     * </p>
     *
     * @return The registered {@link Consumer} callback, or {@code null} if no callback was configured.
     */
    @Nullable
    public Consumer<SqlResultRow> getGeneratedKeyCallback() {
        return this.callback;
    }

}
