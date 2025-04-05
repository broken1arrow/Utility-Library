package org.broken.arrow.database.library.builders.tables;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class SqlQueryPair {
    private final String query;
    private final Map<Integer, Object> values;

    public SqlQueryPair(@Nonnull final String query,@Nonnull final Map<Integer, Object> values) {
        this.query = query;
        this.values = values;
    }

    public String getQuery() {
        return query;
    }

    public Map<Integer, Object> getValues() {
        return values;
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
