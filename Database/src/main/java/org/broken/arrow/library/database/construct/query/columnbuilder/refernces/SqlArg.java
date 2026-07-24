package org.broken.arrow.library.database.construct.query.columnbuilder.refernces;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

/**
 * Represents an argument used in SQL condition expressions.
 * <p>
 * Distinguishes between literal parameter values and raw column identifiers
 * to ensure correct parameter binding and query formatting in
 * {@link org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler}.
 */
public interface SqlArg {

    /**
     * Wraps a raw value to be used as a query parameter.
     * <p>
     * Values wrapped this way will be safely extracted and passed to the
     * JDBC prepared statement, or formatted safely if raw values are forced.
     *
     * @param value the raw object value (e.g., a String, Integer, or Boolean)
     * @return a {@link LiteralVal} containing the value
     */
    static SqlArg val(Object value) {
        return new LiteralVal(value);
    }


}