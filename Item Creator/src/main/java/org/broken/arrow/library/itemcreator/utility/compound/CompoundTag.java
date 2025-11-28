package org.broken.arrow.library.itemcreator.utility.compound;


import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;


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
    CompoundTag(@Nonnull final Object handle) {
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
     * Sets a boolean value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the boolean value to assign
     */
    public void setBoolean(@Nonnull String key, boolean value) {
        this.compoundSession.setBoolean(key,value);
    }

    /**
     * Gets a boolean value from the underlying NBTTagCompound.
     *
     * @param key the key of the boolean value
     * @return the stored boolean value, or {@code false} if unavailable
     */
    public boolean getBoolean(@Nonnull String key) {
        return this.compoundSession.getBoolean(key);
    }
}
