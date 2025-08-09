package org.broken.arrow.library.database.builders.tables;

import org.broken.arrow.library.database.construct.query.QueryBuilder;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
/**
 * Represents a paired SQL query string and its associated parameter values,
 * along with a flag indicating whether the query uses placeholders safely.
 * <p>
 * This class encapsulates a SQL query built from a {@link QueryBuilder} and
 * a map of parameter values indexed by their position in the query.
 * </p>
 */
public class SqlQueryPair {
    private final String query;
    private final Map<Integer, Object> values;
    private final boolean safeQuery;

    /**
     * Constructs a new {@code SqlQueryPair} from the provided {@link QueryBuilder} and
     * parameter values map.
     *
     * @param query  the {@link QueryBuilder} instance used to build the SQL query.
     * @param values a map of parameter indices to their corresponding values.
     */
    public SqlQueryPair(@Nonnull final QueryBuilder query, @Nonnull final Map<Integer, Object> values) {
        this.safeQuery = query.isGlobalEnableQueryPlaceholders();
        this.query = query.build();
        this.values = values;
    }

    /**
     * Returns the generated SQL query string.
     *
     * @return the SQL query as a string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the map of parameter indices and their corresponding values for this query.
     *
     * @return the parameter values map, where keys are placeholder positions (1-based)
     */
    public Map<Integer, Object> getValues() {
        return values;
    }

    /**
     * Indicates whether the query uses placeholders safely.
     *
     * @return true if placeholders are enabled and used safely; false otherwise
     */
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
