package org.broken.arrow.library.itemcreator.utility.nms.api;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CompoundEditor{

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
     * Sets a byte array in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the byte array to assign
     */
    void setByteArray(@Nonnull final String key, final byte[] value);

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


}