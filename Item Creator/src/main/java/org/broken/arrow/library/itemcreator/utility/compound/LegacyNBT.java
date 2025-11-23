package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Provides reflective access to NMS ItemStacks and NBTTagCompounds in legacy
 * Minecraft versions see {@link NbtData} for more info.
 *
 * <p>This class is intended as an internal utility for low-level item modifications.
 * It should generally be accessed through {@link NbtData} or {@link CompoundTag} rather than
 * directly by plugin developers.</p>
 */
public class LegacyNBT {
    private static final Logging logger = new Logging(NbtData.class);

    private LegacyNBT() {}

    /**
     * Creates a new {@link NmsItemSession} for the given Bukkit {@link ItemStack}.
     *
     * <p>This method converts the provided {@link ItemStack} into its internal
     * NMS representation and prepares all required reflective access for
     * reading and writing its underlying {@code NBTTagCompound}.</p>
     *
     * <p>If the required NMS classes or methods could not be resolved during
     * startup, this method will throw an {@link IllegalStateException}. Always
     * verify availability first via {@link NmsItemSession#isReady()}, it will have
     * checks so nothing breaks if you miss it.</p>
     *
     * <p>This method is intended to be used internally by {@link NbtData} and
     * other bridge classes. End-user plugin code should prefer {@link NbtData}.</p>
     *
     * @param stack the Bukkit ItemStack to wrap
     * @return a new {@link NmsItemSession} for the given ItemStack
     * @throws IllegalStateException if the NMS bridge is not available
     */
    public static NmsItemSession session(@Nonnull final ItemStack stack) {
        if (!NmsItemSession.REFLECTION_READY) {
            logger.log(Level.WARNING, () -> "NMS bridge not loaded");
            return null;
        }
        return new NmsItemSession(stack);
    }

    /**
     * Creates a new {@link CompoundSession} for the given NBTTagCompound handle.
     *
     * <p>This method is responsible for binding reflective access to the
     * underlying {@code NBTTagCompound} instance. It enables operations such
     * as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}</p>
     *
     * <p>This is a low-level internal factory and is used by {@link CompoundTag}.
     * End-user code should never call this method directly.</p>
     *
     * @param handle the raw NBTTagCompound instance from NMS
     * @return a new {@link CompoundSession} bound to the provided handle
     * @throws IllegalStateException if the CompoundSession layer is not available
     */
    public static CompoundSession compoundSession(@Nonnull final Object handle) {
        if (!CompoundSession.isReady()) {
            logger.log(Level.WARNING, () -> "CompoundSession not loaded");
            return null;
        }
        return new CompoundSession(handle);
    }

    /**
     * A per-item reflective session that provides access to the underlying NMS ItemStack
     * and its NBTTagCompound.
     *
     * <p>All operations assume that reflection has been successfully initialized. If not,
     * methods will fail silently or throw exceptions as appropriate.</p>
     */
    public static class NmsItemSession {
        private static final Method asNMSCopy;
        private static final Method asBukkitCopy;
        private static final Method hasTag;
        private static final Method getTag;
        private static final Method setTag;
        private static final Constructor<?> nbtTagConstructor;
        private static final boolean REFLECTION_READY;

        private final Object nmsItemCopy;

        static {
            Constructor<?> tagConstructor = null;
            Method setNBTTag = null;
            Method getNBTTag = null;
            Method hasNBTTag = null;
            Method bukkitCopy = null;
            Method asNMSCopyItem = null;
            boolean reflectionDone = false;
            try {
                String craftPath = getCraftBukkitPath();
                String nmsPath = getNmsPath();

                Class<?> craftItemStack = Class.forName(craftPath + ".inventory.CraftItemStack");
                Class<?> nmsItemStack = Class.forName(nmsPath + ".ItemStack");
                Class<?> nbtTagCompound = Class.forName(nmsPath + ".NBTTagCompound");

                asNMSCopyItem = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
                bukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);

                hasNBTTag = nmsItemStack.getMethod("hasTag");
                getNBTTag = nmsItemStack.getMethod("getTag");
                setNBTTag = nmsItemStack.getMethod("setTag", nbtTagCompound);

                tagConstructor = nbtTagCompound.getConstructor();
                reflectionDone = true;
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                logger.logError(e, () -> "Failed to initialize all NMS methods needed.");
            }
            nbtTagConstructor = tagConstructor;
            setTag = setNBTTag;
            getTag = getNBTTag;
            hasTag = hasNBTTag;
            asBukkitCopy = bukkitCopy;
            asNMSCopy = asNMSCopyItem;
            REFLECTION_READY = reflectionDone;
        }

        private NmsItemSession(@Nonnull ItemStack item) {
            this.nmsItemCopy = toNmsItemStack(item);
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public boolean isReady() {
            if (!REFLECTION_READY)
                return false;

            return nmsItemCopy != null;
        }

        /**
         * Checks if the item has a NBTTagCompound.
         *
         * @return {@code true} if it has an NBTTagCompound.
         */
        public boolean hasTag() {
            if (!REFLECTION_READY || nmsItemCopy == null) return false;
            try {
                return (boolean) hasTag.invoke(this.nmsItemCopy);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.logError(e, () -> "Failed to initialize hasTag");
            }
            return false;
        }

        /**
         * Returns the existing {@link CompoundTag} if one is present,
         * otherwise creates a new one.
         *
         * @return the existing {@link CompoundTag} or a new instance if none exists.
         * Returns {@code null} only if reflection failed or the underlying
         * NBTTagCompound could not be created.
         */
        @Nullable
        public CompoundTag getOrCreateCompound() {
            try {
                Object tag;
                if (hasTag()) {
                    tag = getTag.invoke(nmsItemCopy);
                } else {
                    tag = nbtTagConstructor.newInstance();
                    setTag.invoke(nmsItemCopy, tag);
                }
                return new CompoundTag(tag);

            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                logger.logError(e, () -> "Failed to initialize CompoundTag");
            }
            return null;
        }

        /**
         * Returns the existing {@link CompoundTag} if one is present.
         *
         * @return the existing {@link CompoundTag}  or {@code null} if none exists.
         */
        @Nullable
        public CompoundTag getCompound() {
            try {
                if (!hasTag()) return null;
                return new CompoundTag(getTag.invoke(this.nmsItemCopy));
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.logError(e, () -> "Failed to initialize CompoundTag");
            }
            return null;
        }

        /**
         * Applies the current CompoundTag to the ItemStack and returns
         * a new Bukkit ItemStack instance.
         *
         * @param tag the {@link CompoundTag} instance that wraps NBTTagCompound.
         * @return Returns the copy of your itemStack with the nbt set.
         */
        @Nullable
        public ItemStack apply(@Nonnull final CompoundTag tag) {
            if (!REFLECTION_READY || nmsItemCopy == null) return null;
            try {
                setTag.invoke(nmsItemCopy, tag.getHandle());
                return (ItemStack) asBukkitCopy.invoke(null, nmsItemCopy);
            } catch (Exception e) {
                logger.logError(e, () -> "Failed to apply back to itemStack");
            }
            return null;
        }

        /**
         * Converts a Bukkit ItemStack into its NMS counterpart.
         *
         * @param item Bukkit ItemStack to convert.
         * @return the underlying Nms itemStack.
         */
        private Object toNmsItemStack(@Nonnull final ItemStack item) {
            try {
                return asNMSCopy.invoke(null, item);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.logError(e, () -> "Failed to copy the bukkit stack to nms stack");
            }
            return null;
        }

        private static String getCraftBukkitPath() {
            return "org.bukkit.craftbukkit." + getPackageVersion();
        }
    }

    /**
     * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
     * properties on the NMS compound.
     *
     * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
     */
    public static class CompoundSession {
        private static final Method hasKey;
        private static final Method setBoolean;
        private static final Method getBoolean;

        private final Object handle;

        static {
            Method hasTagKey = null;
            Method setBooleanM = null;
            Method getBooleanM = null;
            try {
                final Class<?> nbtTag = Class.forName(getNbtTagPath());
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

        CompoundSession(@Nonnull final Object handle) {
            Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
            this.handle = handle;
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady() {
            return hasKey != null && setBoolean != null && getBoolean != null;
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

        private static String getNbtTagPath() {
            return getNmsPath() + ".NBTTagCompound";
        }

    }

    private static String getNmsPath() {
        return "net.minecraft.server." + getPackageVersion();
    }

    /**
     * Extracts the version identifier from the Bukkit server package.
     * This version will only work on legacy, as the path changed in newer
     * minecraft versions.
     * Example: v1_8_R3
     *
     * @return it returns for example v1_8_R3
     */
    private static String getPackageVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}
