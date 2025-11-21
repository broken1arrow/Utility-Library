package org.broken.arrow.library.database.utility.resulttype;

import java.sql.ResultSet;

public enum ResultSetType {

    TYPE_FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    TYPE_SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    TYPE_SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);
    private final int type;

    ResultSetType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
