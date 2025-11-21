package org.broken.arrow.library.database.utility.resulttype;

import java.sql.ResultSet;

public enum ResultSetConcurrency {

    CONCUR_READ_ONLY(ResultSet.CONCUR_READ_ONLY),
    CONCUR_UPDATABLE(ResultSet.CONCUR_UPDATABLE);

    private final int type;

    ResultSetConcurrency(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
