package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    private static final Logging logger = new Logging(CompoundTag.class);
    private static final Method hasKey;
    private static final Method setBoolean;
    private static final Method getBoolean;
    private final Object handle;

    static {
        Method hasTagKey = null;
        Method setBooleanM = null;
        Method getBooleanM = null;
        try {
            final Class<?> nbtTag = Class.forName(NbtData.getNbtTagPath());
            hasTagKey = nbtTag.getMethod("hasKey", String.class);
            setBooleanM = nbtTag.getMethod("setBoolean", String.class, boolean.class);
            getBooleanM = nbtTag.getMethod("getBoolean", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.logError(e, () -> "Failed to bind NBT methods");
        }
        hasKey = hasTagKey;
        setBoolean = setBooleanM;
        getBoolean = getBooleanM;

    }

    CompoundTag(@Nonnull final Object handle) {
        Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
        this.handle = handle;
    }

    /**
     * This handle is the NBTTagCompound object.
     *
     * @return returns the NBTTagCompound object.
     */
    @Nonnull
    Object getHandle() {
        return handle;
    }

    /**
     * Checks whether this {@link CompoundTag} contains the given key.
     *
     * @param key the NBT key to check
     * @return {@code true} if the key exists, otherwise {@code false}
     */
    public boolean hasKey(@Nonnull String key) {
        if (hasKey == null) return false;

        try {
            return (boolean) hasKey.invoke(handle, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.logError(e, () -> "Failed to check if the compound have the key.");
        }
        return false;
    }

    /**
     * Sets a boolean value in the underlying NBTTagCompound.
     *
     * @param key   the key to set
     * @param value the boolean value to assign
     */
    public void setBoolean(@Nonnull String key, boolean value) {
        if (setBoolean == null) return;

        try {
            setBoolean.invoke(handle, key, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.logError(e, () -> "Failed to set boolean value from reflection");
        }
    }

    /**
     * Gets a boolean value from the underlying NBTTagCompound.
     *
     * @param key the key of the boolean value
     * @return the stored boolean value, or {@code false} if unavailable
     */
    public boolean getBoolean(@Nonnull String key) {
        if (getBoolean == null) return false;

        try {
            return (boolean) getBoolean.invoke(handle, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.logError(e, () -> "Failed to retrieve boolean value from reflection");
        }
        return false;
    }
}
