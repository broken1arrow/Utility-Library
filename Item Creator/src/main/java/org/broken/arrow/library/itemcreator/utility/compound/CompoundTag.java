package org.broken.arrow.library.itemcreator.utility.compound;


import org.broken.arrow.library.itemcreator.utility.nms.LegacyNBT;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Wraps an underlying NBTTagCompound belonging to an NMS ItemStack.
 *
 * <p>This is <b>not</b> intended for normal plugin-specific NBT storage.
 * It is used to modify low-level, vanilla-controlled item properties
 * which are otherwise inaccessible through the Bukkit API in legacy
 * Minecraft versions.</p>
 *
 * <p>Examples include keys such as {@code "Unbreakable"} that affect the
 * behaviour of the actual item, not just custom plugin data.</p>
 *
 * <p>This class is backed by reflection. If the required NMS classes or
 * methods cannot be resolved, operations will fail silently (with logging)
 * and no changes will be applied to the compound.</p>
 *
 */
public final class CompoundTag {
    private final LegacyNBT.CompoundSession compoundSession;

    /**
     * Creates a new {@link CompoundTag} for the given NBTTagCompound handle.
     *
     * <p>This method is responsible for binding reflective access to the
     * underlying {@code NBTTagCompound} instance. It enables operations such
     * as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}
     * </p>
     *
     * @param handle the raw NBTTagCompound instance from NMS
     */
    public CompoundTag(@Nonnull final Object  handle) {
        Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
        compoundSession = LegacyNBT.compoundSession(handle);
        Validate.checkNotNull(compoundSession, "The compound session could not be loaded.");
    }

    /**
     * Checks whether this {@link CompoundTag} contains the given key.
     *
     * @param key the NBT key to check
     * @return {@code true} if the key exists, otherwise {@code false}
     */
    public boolean hasKey(@Nonnull String key) {
        return this.compoundSession.hasKey(key);
    }


    /**
     * Removes the specified key from this compound.
     *
     * @param key the key to remove
     */
    public void remove(@Nonnull final String key) {
        this.compoundSession.remove(key);
    }

    /**
     * Sets a int value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the int value to assign
     */
    public void setInt(@Nonnull final String key, final int value) {
        this.compoundSession.setInt(key, value);
    }

    /**
     * Gets a int value from the underlying NBTTagCompound.
     *
     * @param key the key of the int value
     * @return the stored int value, or {@code -1} if reflection fail
     *         or if the key does not exist in the NBT data.
     */
    public int getInt(@Nonnull final String key) {
        return this.compoundSession.getInt(key);
    }

    /**
     * Sets a String value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the String value to assign
     */
    public void setString(@Nonnull final String key, final String value) {
        this.compoundSession.setString(key, value);
    }

    /**
     * Gets a string value from the underlying NBTTagCompound.
     *
     * @param key the key of the string value
     * @return the stored string value, or empty string if reflection fail
     *         or {@code null} if the key does not exist in the NBT data.
     */
    public String getString(@Nonnull final String key) {
        return this.compoundSession.getString(key);
    }

    /**
     * Sets a byte value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the byte value to assign
     */
    public void setByte(@Nonnull final String key, final byte value) {
        this.compoundSession.setByte(key, value);
    }

    /**
     * Gets a byte value from the underlying NBTTagCompound.
     *
     * @param key the key of the byte value
     * @return the stored byte value, or {@code -1} if unavailable
     */
    public byte getByte(@Nonnull final String key) {
        return this.compoundSession.getByte(key);
    }

    /**
     * Stores a byte array under the specified key in the underlying NBTTagCompound.
     * <p>
     * This method provides flexibility for attaching arbitrary binary data to an NBT
     * structure, allowing more complex or custom payloads to be stored efficiently.
     *
     * @param key   the name of the tag to write
     * @param value the byte array to store, may be {@code null} depending on implementation
     */
    public void setByteArray(@Nonnull final String key, final byte[] value) {
        this.compoundSession.setByteArray(key, value);
    }

    /**
     * Retrieves a stored byte array associated with the given key from the underlying
     * NBTTagCompound.
     * <p>
     * This is useful for reading custom binary data previously written with
     * {@link #setByteArray(String, byte[])}.
     *
     * @param key the name of the tag to read
     * @return the byte array associated with the key, an empty array if reflection
     *         access fails, or {@code null} if the key does not exist in the NBT data.
     */
    @Nullable
    public byte[] getByteArray(@Nonnull final String key) {
        return this.compoundSession.getByteArray(key);
    }

    /**
     * Sets a boolean value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the boolean value to assign
     */
    public void setBoolean(@Nonnull final String key, final boolean value) {
        this.compoundSession.setBoolean(key, value);
    }

    /**
     * Gets a boolean value from the underlying NBTTagCompound.
     *
     * @param key the key of the boolean value
     * @return the stored boolean value, or {@code false} if unavailable
     */
    public boolean getBoolean(@Nonnull final String key) {
        return this.compoundSession.getBoolean(key);
    }

    /**
     * Sets a short value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the short value to assign
     */
    public void setShort(@Nonnull final String key, final short value) {
        this.compoundSession.setShort(key, value);
    }

    /**
     * Gets a short value from the underlying NBTTagCompound.
     *
     * @param key the key of the short value
     * @return the stored short value, or {@code -1} if unavailable
     */
    public short getShort(@Nonnull final String key) {
        return this.compoundSession.getShort(key);
    }

}
