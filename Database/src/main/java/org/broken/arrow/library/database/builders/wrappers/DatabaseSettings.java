package org.broken.arrow.library.database.builders.wrappers;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Base class for settings used in database operations such as loading or saving data.
 * <p>
 * Stores the table name and an optional column filter that can be used to limit which
 * columns are included in queries or updates.
 */
public class DatabaseSettings {

    @Nonnull
    private final String tableName;
    private Predicate<String> filter;

    /**
     * Creates a new settings object for the specified table.
     *
     * @param tableName The name of the table this configuration applies to.
     */
    public DatabaseSettings(@Nonnull final String tableName) {
        this.tableName = tableName;
    }

    /**
     * Returns the current filter function used to check if a column is allowed.
     *
     * @return A function that returns {@code true} if the column should be included; otherwise {@code false}. May be {@code null}.
     */
    public Predicate<String> getFilter() {
        return filter;
    }

    /**
     * Sets a custom filter function for determining which columns to include.
     *
     * @param filterColumn A function that returns {@code true} for columns that should be included.
     */
    public void setFilter(@Nonnull Predicate<String> filterColumn) {
        filter = filterColumn;
    }

    /**
     * Sets a simple column filter based on column names. When you load from database, this is used to sort out values such as primary or other data you want to
     * extract. When you save to database it is used for filter out columns you don't want to update.
     *
     * @param allowedColumns One or more column names to include.
     */
    public void setFilter(@Nonnull final String... allowedColumns) {
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedColumns));
        setFilter(allowedSet::contains);
    }

    /**
     * Returns the name of the table this settings object is associated with.
     *
     * @return The table name.
     */
    @Nonnull
    protected String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return "DatabaseSettings{" +
                "tableName='" + tableName + '\'' +
                ", filter=" + filter +
                '}';
    }
}