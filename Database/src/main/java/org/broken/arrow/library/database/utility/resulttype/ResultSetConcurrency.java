package org.broken.arrow.library.database.utility.resulttype;

import java.sql.ResultSet;

/**
 * Enum representing the concurrency mode of a JDBC ResultSet.
 *
 * <p>Maps directly to {@link java.sql.ResultSet} concurrency constants.</p>
 */
public enum ResultSetConcurrency {

    /**
     * Read-only ResultSet; changes are not allowed.
     */
    CONCUR_READ_ONLY(ResultSet.CONCUR_READ_ONLY),
    /**
     * Updatable ResultSet; changes can be made to the underlying database.
     */
    CONCUR_UPDATABLE(ResultSet.CONCUR_UPDATABLE);

    private final int type;

    ResultSetConcurrency(final int type) {
        this.type = type;
    }

    /**
     * Returns the JDBC integer constant representing this concurrency mode.
     *
     * @return the JDBC ResultSet concurrency constant
     */
    public int getType() {
        return type;
    }
}
