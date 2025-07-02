package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DatabaseQueryHandler<T> {
    private final List<T> data = new ArrayList<>();
    private final DatabaseSettings databaseSettings;

    public DatabaseQueryHandler(@Nonnull final DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
    }

    public Predicate<String> getFilter() {
        return databaseSettings.getFilter();
    }

    public QueryBuilder getQueryBuilder() {
        if (databaseSettings instanceof DatabaseSettingsLoad)
            return ((DatabaseSettingsLoad) databaseSettings).getQueryBuilder();
        return null;
    }

    public boolean isFilterSet() {
        final Predicate<String> filter = this.getFilter();
        return filter != null;
    }


    public boolean containsFilteredColumn(@Nonnull final String columnName) {
        final Predicate<String> filter = this.getFilter();

        return filter == null || filter.test(columnName);
    }

    public void add(T queryData) {
        this.data.add(queryData);
    }

    public List<T> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DatabaseQueryHandler{" +
                "data=" + data +
                ", databaseSettings=" + databaseSettings +
                '}';
    }


}
