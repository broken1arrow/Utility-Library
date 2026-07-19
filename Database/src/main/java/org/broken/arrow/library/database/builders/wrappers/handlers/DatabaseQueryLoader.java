package org.broken.arrow.library.database.builders.wrappers.handlers;

import org.broken.arrow.library.database.builders.wrappers.DatabaseSettings;
import org.broken.arrow.library.database.builders.wrappers.DatabaseSettingsLoad;
import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;

public class DatabaseQueryLoader<T> extends DatabaseQueryHandler<T> {
    private final DatabaseSettingsLoad databaseSettings;

    /**
     * Creates a new instance of {@code DatabaseQueryHandler} with the specified database settings.
     *
     * @param databaseSettings the configuration settings that define query behavior and filtering
     */
    public DatabaseQueryLoader(@Nonnull final DatabaseSettingsLoad databaseSettings) {
        super(databaseSettings);
        this.databaseSettings = databaseSettings;
    }

    /**
     * Retrieves the underlying {@link QueryBuilder}.
     * <p>
     *
     * @return the {@link QueryBuilder} instance or {@code null} if not set.
     */
    public QueryBuilder getQueryBuilder() {
        return databaseSettings.getQueryBuilder();

    }
}
