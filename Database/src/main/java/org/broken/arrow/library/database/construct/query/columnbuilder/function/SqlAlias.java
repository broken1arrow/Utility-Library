package org.broken.arrow.library.database.construct.query.columnbuilder.function;

import javax.annotation.Nonnull;

/**
 * Represents a SQL column alias segment (e.g., {@code AS alias_name}).
 */
public class SqlAlias implements ColumnStrategy {
    private final String alias;

    /**
     * Constructs an alias operation with the given alias name.
     *
     * @param alias the SQL alias name to assign
     */
    public SqlAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Appends the SQL alias clause to the pipeline.
     *
     * @param context the incoming SQL expression string (unused in string formatting)
     * @return {@code " AS alias"} if a valid alias is present; empty string otherwise
     */
    @Nonnull
    @Override
    public String build(@Nonnull final String context) {
        if (alias == null || alias.trim().isEmpty()) {
            return "";
        }
        return " AS " + alias;
    }
    @Override
    public String toString() {
        return build("");
    }
}
