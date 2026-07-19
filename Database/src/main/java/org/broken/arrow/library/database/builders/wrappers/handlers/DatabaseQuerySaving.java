package org.broken.arrow.library.database.builders.wrappers.handlers;

import org.broken.arrow.library.database.builders.wrappers.DatabaseSettings;
import org.broken.arrow.library.database.builders.wrappers.DatabaseSettingsLoad;
import org.broken.arrow.library.database.builders.wrappers.DatabaseSettingsSave;
import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.utility.query.build.SqlResultRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DatabaseQuerySaving<T> extends DatabaseQueryHandler<T> {
    @Nonnull
    private final DatabaseSettingsSave databaseSettings;

    /**
     * Creates a new instance of {@code DatabaseQueryHandler} with the specified database settings.
     *
     * @param databaseSettings the configuration settings that define query behavior and filtering
     */
    public DatabaseQuerySaving(@Nonnull final DatabaseSettingsSave databaseSettings) {
        super(databaseSettings);
        this.databaseSettings = databaseSettings;
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
        return this.databaseSettings.getGeneratedKeyCallback();
    }

}
