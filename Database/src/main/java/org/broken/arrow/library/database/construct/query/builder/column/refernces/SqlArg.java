package org.broken.arrow.library.database.construct.query.builder.column.refernces;

import org.broken.arrow.library.database.construct.query.builder.column.Column;

/**
 * Represents an argument used in SQL condition expressions.
 * <p>
 * This interface distinguishes between literal parameter values (which require binding)
 * and raw column or table identifiers. This ensures correct parameter binding and query
 * formatting within the {@link org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler}.
 */
public interface SqlArg {

    /**
     * Wraps a literal value to be bound as a query parameter.
     * <p>
     * Values wrapped this way are treated as parameter data. They will be extracted,
     * replaced with a placeholder (e.g., {@code ?}) in the SQL string, and passed securely
     * to the JDBC statement to prevent SQL injection.
     *
     * @param value the raw object value (e.g., a String, Integer, or Boolean)
     * @return a {@link LiteralVal} instance wrapping the parameter value
     */
    static SqlArg val(Object value) {
        return new LiteralVal(value);
    }

    /**
     * Wraps a raw SQL expression or identifier to be injected directly into the query.
     * <p>
     * Content wrapped this way is treated as trusted SQL code (e.g., {@code "ro.column_name"}
     * or {@code "NOW()"}). It is injected exactly as-is without parameterization and is
     * automatically excluded from the prepared statement's value bindings map.
     *
     * @param sqlExpression the raw SQL string or object identifier
     * @return a {@link Column} instance wrapping the raw expression to prevent parameterization
     */
    static SqlArg raw(String sqlExpression) {
        return Column.of(sqlExpression);
    }
}