package org.broken.arrow.library.itemcreator.utility.nbt;

import javax.annotation.Nullable;

/**
 * Represents an NBT (Named Binary Tag) value that can either be applied to or removed from an item.
 * <p>
 * This class allows setting various NBT-compatible data types, as well as marking the key for removal.
 * Supported value types include:
 * <ul>
 *   <li>{@link String}</li>
 *   <li>Primitive wrapper types: {@link Byte}, {@link Long}, {@link Integer}, {@link Float}, {@link Double}</li>
 *   <li>{@link java.util.UUID} (via a custom adapter)</li>
 *   <li>Arrays: {@code byte[]}, {@code int[]}, {@code long[]}</li>
 * </ul>
 */
public class NBTValue {
    private final boolean removeKey;
    private final Object value;

    /**
     * Creates an {@code NBTValue} that will set the given value on the target item.
     *
     * @param value the value to set; supports string, byte, long, int, UUID, float, double,
     *              as well as byte[], int[], and long[] arrays
     */
    public NBTValue(final Object value) {
        this(value,false);
    }

    /**
     * Creates an {@code NBTValue} that can set or remove the given value on the target item.
     *
     * @param value the value to set; supports string, byte, long, int, UUID, float, double,
     *              as well as byte[], int[], and long[] arrays
     * @param removeKey if {@code true}, this key and value will be removed from the item
     */
    public NBTValue(final Object value,final boolean removeKey) {
        this.value = value;
        this.removeKey = removeKey;
    }

    /**
     * Returns the stored value.
     *
     * @return the stored NBT value, may be {@code null}
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the stored value cast to the given type if compatible.
     *
     * @param type the target type class
     * @param <T> the generic type
     * @return the cast value if it matches the given type, otherwise {@code null}
     */
    @Nullable
    public <T> T getValue(Class<T> type) {
        Object val = value;
        if (type.isInstance(val)) {
            return type.cast(val);
        }
        return null;
    }

    /**
     * Checks whether this value is marked for removal.
     *
     * @return {@code true} if the key and value should be removed from the item
     */
    public boolean isRemoveKey() {
        return removeKey;
    }

}
