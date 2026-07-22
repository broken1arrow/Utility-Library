package org.broken.arrow.library.database.construct.query.columnbuilder.refernces;

import java.util.Objects;

// Implementation 1: The Column
public final class ColumnRef implements SqlArg {
    private final String name;

    public ColumnRef(String name) {
        this.name = name;
    }

    public String value() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ColumnRef that = (ColumnRef) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ColumnRef[" +
                "name=" + name + ']';
    }
}

