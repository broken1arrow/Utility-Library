package org.broken.arrow.library.database.builders.wrappers;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.utility.query.build.SqlResultRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a compiled, parameter-bound SQL query ready for database execution.
 * <p>
 * Encapsulates the generated SQL string, positional parameter values, security configuration,
 * and optional callbacks for generated keys or updated row data.
 * </p>
 */
public class SqlQuery {
    private final String sql;
    private final Map<Integer, Object> parameters;
    private final boolean safeQuery;
    private Consumer<SqlResultRow> callback;

    /**
     * Constructs a {@code SqlQuery} from a {@link QueryBuilder} and its extracted parameter values.
     *
     * @param builder    the {@link QueryBuilder} instance used to compile the SQL statement
     * @param parameters a map of 1-based parameter indices to their corresponding values
     */
    public SqlQuery(@Nonnull final QueryBuilder builder, @Nonnull final Map<Integer, Object> parameters) {
        this.safeQuery = builder.isGlobalEnableQueryPlaceholders();
        this.sql = builder.build();
        this.parameters = parameters;
    }

    /**
     * Returns the generated SQL query string.
     *
     * @return the SQL string
     */
    public String getSql() {
        return sql;
    }

    /**
     * Returns the map of positional parameter indices and their bound values.
     *
     * @return map of parameter values indexed by 1-based placeholder position
     */
    public Map<Integer, Object> getParameters() {
        return parameters;
    }

    /**
     * Indicates whether the query uses parameterized placeholders safely.
     *
     * @return {@code true} if placeholders are enabled; {@code false} if values are embedded directly
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
    public void setGeneratedKeyCallback(@Nonnull final Consumer<SqlResultRow> callback) {
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
    public Consumer<SqlResultRow> getGeneratedKeyCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return "SqlQuery{" +
                "sql='" + sql + '\'' +
                ", parameters=" + parameters +
                ", safeQuery=" + safeQuery +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlQuery that = (SqlQuery) o;
        if (safeQuery != that.safeQuery) return false;
        if (!Objects.equals(sql, that.sql)) return false;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, parameters, safeQuery);
    }
}
