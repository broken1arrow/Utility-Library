package org.broken.arrow.library.database.builders.tables;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.utility.query.build.SqlResultRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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
    private Consumer<SqlResultRow> callback;

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

    /**
     * Registers a callback to capture and process auto-generated keys or updated column values
     * returned by the database execution engine.
     * <p>
     * This method is intended for short-lived, low-level query execution context tracking.
     * Developers tracking batch transactions should prefer registering listeners directly on
     * the higher-level {@link org.broken.arrow.library.database.builders.DataWrapper}.
     * </p>
     *
     * @param callback The consumer callback that will receive the populated {@link SqlResultRow}; cannot be null.
     * @throws NullPointerException if the provided callback is null.
     */
    public void getGeneratedKeyCallback(@Nonnull final Consumer<SqlResultRow> callback) {
        this.callback = callback;
    }

    /**
     * Retrieves the registered generated-key callback for internal engine execution.
     * <p>
     * This method is executed by the statement processor immediately following a successful
     * SQL write transaction to pipeline auto-generated data back to the application.
     * </p>
     *
     * @return The registered {@link Consumer} callback, or {@code null} if no callback was configured.
     */
    @Nullable
    public Consumer<SqlResultRow> getGeneratedKey() {
        return callback;
    }

    @Override
    public String toString() {
        return "SqlQueryPair:\n" +
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
