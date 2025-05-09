package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DatabaseQueryHandler<T> {
    private final List<T> data = new ArrayList<>();
    private final DatabaseSettings databaseSettings;
    private Function<T, QueryBuilder> loadData;

    public DatabaseQueryHandler(@Nonnull final DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
    }

    public Function<String, Boolean> getFilter() {
        return databaseSettings.getFilter();
    }

    public QueryBuilder getQueryBuilder() {
        if (databaseSettings instanceof DatabaseSettingsLoad)
            return ((DatabaseSettingsLoad) databaseSettings).getQueryBuilder();
        return null;
    }

    public T setLoadData(T loadData) {
        if (this.loadData != null) {
            this.loadData.apply(loadData);
            return loadData;
        }

        return null;
    }
    public boolean isFilterSet() {
        final Function<String, Boolean> filter = this.getFilter();
        return filter != null;
    }

    public void forEach(Function<T, QueryBuilder> action) {
        loadData = action;
    }


    public boolean containsFilteredColumn(@Nonnull final String columnName) {
        final Function<String, Boolean> filter = this.getFilter();

        return filter == null || filter.apply(columnName);
    }

    public boolean containsFilteredColumn(@Nonnull final Set<String> columnName) {
        final Function<String, Boolean> filter = this.getFilter();
        if (filter == null) {
            return true;
        }
        for (String name : columnName) {
            if (filter.apply(name)) {
                return true;
            }
        }
        return false;
    }

    public void add(T loadDataWrapper) {
        this.data.add(loadDataWrapper);
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
