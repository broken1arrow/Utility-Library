package org.broken.arrow.database.library.builders.wrappers;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DatabaseSettings {
    @Nonnull
    private final String tableName;
    private Function<String, Boolean> filter;


    public DatabaseSettings(@Nonnull final String tableName) {
        this.tableName = tableName;
    }

    public Function<String, Boolean> getFilter() {
        return filter;
    }

    public void setFilter(@Nonnull Function<String, Boolean> filterColumn) {
        filter = filterColumn;
    }

    public void setFilter(@Nonnull final String... allowedColumns) {
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedColumns));
        setFilter(allowedSet::contains);
    }

    protected String  getTableName() {
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