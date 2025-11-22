package org.broken.arrow.library.database.utility.resulttype;

import java.sql.ResultSet;

/**
 * Enum representing the type of JDBC ResultSet cursor.
 *
 * <p>Maps directly to {@link java.sql.ResultSet} type constants.</p>
 */
public enum ResultSetType {
    /**
     * The cursor can only move forward through the ResultSet.
     */
    TYPE_FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    /**
     * The cursor can scroll, but changes made by others are not visible.
     */
    TYPE_SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    /**
     * The cursor can scroll, and changes made by others are visible.
     */
    TYPE_SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);
    private final int type;

    ResultSetType(final int type) {
        this.type = type;
    }

    /**
     * Returns the JDBC integer constant representing this ResultSet cursor type.
     *
     * <p>This determines how the cursor can move through the ResultSet:
     * forward-only, scroll-insensitive, or scroll-sensitive.</p>
     *
     * @return the JDBC ResultSet type constant
     */
    public int getType() {
        return type;
    }
}
