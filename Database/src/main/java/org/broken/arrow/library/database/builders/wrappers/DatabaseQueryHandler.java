package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
/**
 * Handles database query results and settings, providing filtering capabilities and storage for retrieved data.
 *
 * <p>This class maintains a list of data objects of type {@code T} retrieved from the database
 * and uses {@link DatabaseSettings} to manage query configuration and filtering logic.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Storing query results in a generic list.</li>
 *   <li>Applying column name filters via predicates defined in {@link DatabaseSettings}.</li>
 *   <li>Accessing the underlying {@link QueryBuilder} if available.</li>
 * </ul>
 *
 * @param <T> the type of data objects managed by this handler
 */
public class DatabaseQueryHandler<T> {
    private final List<T> data = new ArrayList<>();
    private final DatabaseSettings databaseSettings;

    /**
     * Creates a new instance of {@code DatabaseQueryHandler} with the specified database settings.
     *
     * @param databaseSettings the configuration settings that define query behavior and filtering; must not be {@code null}
     */
    public DatabaseQueryHandler(@Nonnull final DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
    }
    /**
     * Returns the filter predicate for column names defined in the associated {@link DatabaseSettings}.
     *
     * @return a {@link Predicate} used to filter column names, or {@code null} if no filter is set
     */
    public Predicate<String> getFilter() {
        return databaseSettings.getFilter();
    }

    /**
     * Retrieves the underlying {@link QueryBuilder} if available.
     * <p>
     * This method returns the {@link QueryBuilder} only if the {@link DatabaseSettings} instance
     * is of type {@link DatabaseSettingsLoad}; otherwise, it returns {@code null}.
     *
     * @return the {@link QueryBuilder} instance or {@code null} if not applicable
     */
    public QueryBuilder getQueryBuilder() {
        if (databaseSettings instanceof DatabaseSettingsLoad)
            return ((DatabaseSettingsLoad) databaseSettings).getQueryBuilder();
        return null;
    }

    /**
     * Checks if a filter predicate is set in the associated {@link DatabaseSettings}.
     *
     * @return {@code true} if a filter is defined; {@code false} otherwise
     */
    public boolean isFilterSet() {
        final Predicate<String> filter = this.getFilter();
        return filter != null;
    }

    /**
     * Determines if a given column name passes the current filter.
     *
     * @param columnName the name of the column to check; must not be {@code null}
     * @return {@code true} if no filter is set or the filter accepts the column name; {@code false} otherwise
     */
    public boolean containsFilteredColumn(@Nonnull final String columnName) {
        final Predicate<String> filter = this.getFilter();

        return filter == null || filter.test(columnName);
    }

    /**
     * Adds a data object to the internal storage list.
     *
     * @param queryData the data object to add
     */
    public void add(T queryData) {
        this.data.add(queryData);
    }

    /**
     * Returns an unmodifiable view of the list of data objects managed by this handler.
     *
     * @return the list of data objects
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Returns a string representation of the {@code DatabaseQueryHandler} including its data and settings.
     *
     * @return a string describing this instance
     */
    @Override
    public String toString() {
        return "DatabaseQueryHandler{" +
                "data=" + data +
                ", databaseSettings=" + databaseSettings +
                '}';
    }
}
