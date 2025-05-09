package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class SqlQueryPair {
    private final String query;
    private final Map<Integer, Object> values;
    private final boolean safeQuery;

    public SqlQueryPair(@Nonnull final QueryBuilder query, @Nonnull final Map<Integer, Object> values) {
        this.safeQuery = query.isGlobalEnableQueryPlaceholders();
        this.query = query.build();
        this.values = values;
    }

    public String getQuery() {
        return query;
    }

    public Map<Integer, Object> getValues() {
        return values;
    }

    public boolean isSafeQuery() {
        return safeQuery;
    }

    @Override
    public String toString() {
        return "SqlQueryPair:\n"+
                "query='" + query + "'\n" +
                "values=" + values + "'\n" +
                "===============";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlQueryPair that = (SqlQueryPair) o;

        if (!Objects.equals(query, that.query)) return false;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        int result = query != null ? query.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
