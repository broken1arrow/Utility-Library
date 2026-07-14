package org.broken.arrow.library.itemcreator.nbt.nms.api;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CompoundEditor {

    /**
     * Checks if it has loaded all reflections.
     *
     * @return true if everything is loaded correctly.
     */
    static boolean isReady() {
        return false;
    }

    /**
     * This handle is the NBTTagCompound object.
     *
     * @return returns the NBTTagCompound object.
     */
    @Nonnull
    Object getHandle();

    /**
     * Checks whether this {@link CompoundTag} contains the given key.
     *
     * @param key the NBT key to check
     * @return {@code true} if the key exists, otherwise {@code false}
     */
    boolean hasKey(@Nonnull final String key);


    /**
     * Remove this {@link CompoundTag} value and the given key.
     *
     * @param key the NBT key to remove.
     */
    void remove(@Nonnull final String key);

    /**
     * Sets a int value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the int value to assign
     */
    void setInt(@Nonnull final String key, final int value);

    /**
     * Gets a int value from the underlying NBTTagCompound.
     *
     * @param key the key of the int value
     * @return the stored int value, or {@code -1} if reflection fail
     * or if the key does not exist in the NBT data.
     */
    int getInt(@Nonnull final String key);

    /**
     * Sets a double value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the double value to assign
     */
    void setDouble(@Nonnull final String key, final double value);

    /**
     * Gets a double value from the underlying NBTTagCompound.
     *
     * @param key the key of the double value
     * @return the stored int value, or {@code -1.0} if reflection fail
     * or if the key does not exist in the NBT data.
     */
    double getDouble(@Nonnull final String key);

    /**
     * Sets a long  value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the long  value to assign
     */
    void setLong(@Nonnull String key, long value);

    /**
     * Gets a long value from the underlying NBTTagCompound.
     *
     * @param key the key of the long value
     * @return the stored int value, or {@code -1} if reflection fail
     * or if the key does not exist in the NBT data.
     */
    long getLong(@Nonnull String key);

    /**
     * Sets a String value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the String value to assign
     */
    void setString(@Nonnull final String key, final String value);

    /**
     * Gets a string value from the underlying NBTTagCompound.
     *
     * @param key the key of the string value
     * @return the stored string value, or empty string if unavailable
     */
    @Nonnull
    String getString(@Nonnull final String key);

    /**
     * Sets a byte value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the byte value to assign
     */
    void setByte(@Nonnull final String key, final byte value);

    /**
     * Gets a byte value from the underlying NBTTagCompound.
     *
     * @param key the key of the byte value
     * @return the stored byte value, or {@code -1} if reflection fail
     * or if the key does not exist in the NBT data.
     */
    byte getByte(@Nonnull final String key);

    /**
     * Sets a boolean value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the boolean value to assign
     */
    void setBoolean(@Nonnull final String key, final boolean value);

    /**
     * Gets a boolean value from the underlying NBTTagCompound.
     *
     * @param key the key of the boolean value
     * @return the stored boolean value, or {@code false} if unavailable
     */
    boolean getBoolean(@Nonnull final String key);

    /**
     * Sets a short value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the short value to assign
     */
    void setShort(@Nonnull final String key, final short value);

    /**
     * Gets a short value from the underlying NBTTagCompound.
     *
     * @param key the key of the short value
     * @return the stored short value, or {@code -1} if reflection fail
     * or if the key does not exist in the NBT data.
     */
    short getShort(@Nonnull final String key);

    /**
     * Sets a byte array in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the byte array to assign
     */
    void setByteArray(@Nonnull final String key, final byte[] value);

    /**
     * Stores a int array under the specified key in the underlying NBTTagCompound.
     * <p>
     * This method provides flexibility for attaching arbitrary binary data to an NBT
     * structure, allowing more complex or custom payloads to be stored efficiently.
     *
     * @param key   the name of the tag to write
     * @param value the byte array to store, may be {@code null} depending on implementation
     */
    void setIntArray(String key, int[] value);

    /**
     * Stores a long array under the specified key in the underlying NBTTagCompound.
     * <p>
     * This method provides flexibility for attaching arbitrary binary data to an NBT
     * structure, allowing more complex or custom payloads to be stored efficiently.
     *
     * @param key   the name of the tag to write
     * @param value the byte array to store, may be {@code null} depending on implementation
     */
    void setLongArray(String key, long[] value);

    /**
     * Retrieves a byte array from the underlying NBTTagCompound.
     *
     * <p>Returns {@code null} if the NBT tag does not exist, or an empty array if
     * the reflective call is unavailable or fails.</p>
     *
     * @param key the key of the stored byte array
     * @return the byte array, {@code null} if the tag is missing, or
     * an empty array on reflection failure.
     */
    @Nullable
    byte[] getByteArray(@Nonnull final String key);

    /**
     * Retrieves a stored int array associated with the given key from the underlying
     * NBTTagCompound.
     * <p>
     * This is useful for reading custom binary data previously written with
     * {@link #setIntArray(String, int[])} .
     *
     * @param key the name of the tag to read
     * @return the byte array, {@code null} if the tag is missing, or
     * an empty array on reflection failure.
     */
    @Nonnull
    int[] getIntArray(String key);

    /**
     * Retrieves a stored long array associated with the given key from the underlying
     * NBTTagCompound.
     * <p>
     * This is useful for reading custom binary data previously written with
     * {@link #setLongArray(String, long[])}.
     *
     * @param key the name of the tag to read
     * @return the byte array, {@code null} if the tag is missing, or
     * an empty array on reflection failure.
     */
    @Nonnull
    long[] getLongArray(String key);
}