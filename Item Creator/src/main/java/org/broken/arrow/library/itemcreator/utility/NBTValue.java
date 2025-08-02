package org.broken.arrow.library.itemcreator.utility;

import javax.annotation.Nullable;

public class NBTValue {
    private final boolean removeKey;
    private final Object value;

    public NBTValue(final Object value) {
        this(value,false);
    }
    public NBTValue(final Object value,final boolean removeKey) {
        this.value = value;
        this.removeKey = removeKey;
    }

    public Object getValue() {
        return value;
    }

    @Nullable
    public <T> T getValue(Class<T> type) {
        Object val = value;
        if (type.isInstance(val)) {
            return type.cast(val);
        }
        return null;
    }

    public boolean isRemoveKey() {
        return removeKey;
    }

}
