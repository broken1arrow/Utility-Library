package org.broken.arrow.library.database.construct.query.columnbuilder.refernces;

import java.util.Objects;

/**
 * Wrapper for literal parameter values in SQL comparisons.
 * <p>
 * Signals to the builder that the contained value should be treated
 * as a prepared statement placeholder rather than a database identifier.
 */
public final class LiteralVal implements SqlArg {
    private final Object value;
    /**
     * Constructs a new literal value wrapper.
     *
     * @param value the raw value to be bound to the SQL query
     */
    public LiteralVal(final Object value) {
        this.value = value;
    }

    /**
     * Retrieves the wrapped raw value.
     *
     * @return the underlying object value
     */
    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        LiteralVal that = (LiteralVal) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "LiteralVal[" +
                "value=" + value + ']';
    }
}