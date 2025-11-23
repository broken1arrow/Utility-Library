package org.broken.arrow.library.itemcreator.utility.compound;


import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps an NMS ItemStack and provides access to its NBTTagCompound through
 * the {@link CompoundTag} abstraction.
 *
 * <p>This class is only fully functional in legacy Minecraft versions where
 * certain vanilla item properties are not exposed by the Bukkit API.
 * These values (for example {@code Unbreakable}) are written at a low NMS
 * level and affect the actual behaviour of the item, unlike custom NBT
 * values used only by plugins.</p>
 */
public class NbtData {
    private static final Logging logger = new Logging(NbtData.class);
    private static final Method asNMSCopy;
    private static final Method asBukkitCopy;
    private static final Method hasTag;
    private static final Method getTag;
    private static final Method setTag;
    private static final Constructor<?> nbtTagConstructor;
    private static final boolean reflectionReady;

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
            logger.logError(e, () -> "Failed to initialize NMS bridge");
        }
        nbtTagConstructor = tagConstructor;
        setTag = setNBTTag;
        getTag = getNBTTag;
        hasTag = hasNBTTag;
        asBukkitCopy = bukkitCopy;
        asNMSCopy = asNMSCopyItem;
        reflectionReady = reflectionDone;
    }

    /**
     * Creates a new NMS bridge.
     *
     * <p>Checks for availability of {@link org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)}.
     * If present, reflection loading is skipped.</p>
     *
     * @param itemStack the itemStack to alter the metadata on.
     */
    public NbtData(ItemStack itemStack) {
        if (!reflectionReady) {
            this.nmsItemCopy = null;
            return;
        }
        this.nmsItemCopy = toNmsItemStack(itemStack);
    }

    /**
     * Indicates whether required NMS and NBT classes were successfully
     * resolved through reflection.
     *
     * @return {@code true} if legacy NBT operations are supported
     */
    public boolean isReflectionReady() {
        return reflectionReady;
    }

    /**
     * Checks if the item has a NBTTagCompound.
     *
     * @return {@code true} if it has an NBTTagCompound.
     */
    public boolean hasTag() {
        if (!reflectionReady || nmsItemCopy == null) return false;
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
     *         Returns {@code null} only if reflection failed or the underlying
     *         NBTTagCompound could not be created.
     */
    @Nullable
    public CompoundTag getOrCreateCompound() {
        try {
            final Object tag = hasTag()
                    ? getTag.invoke(nmsItemCopy)
                    : nbtTagConstructor.newInstance();
            return new CompoundTag(tag);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.logError(e, () -> "Failed to initialize CompoundTag");
        }
        return null;
    }

    /**
     * Returns the existing {@link CompoundTag}  if one is present,
     * otherwise creates a new one.
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
        if (!reflectionReady || nmsItemCopy == null) return null;
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


    static String getNbtTagPath() {
        return getNmsPath() + ".NBTTagCompound";
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
