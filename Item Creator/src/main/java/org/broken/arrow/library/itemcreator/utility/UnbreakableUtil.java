package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class UnbreakableUtil {
    private static final Logging logger = new Logging(UnbreakableUtil.class);
    private static Method asNMSCopyMethod = null;
    private static Method asBukkitCopyMethod = null;
    private static Method hasTagMethod = null;
    private static Method getTagMethod = null;
    private static Method setTagMethod = null;
    private static Method setBooleanMethod = null;
    private static Method getBooleanMethod = null;
    private static Method metaGetMaterialMethod = null;
    private static Constructor<?> nbtTagConstructor = null;

    private static boolean reflectionReady = false;
    private static boolean modernSupported;


    static {
        try {
            Class.forName("org.bukkit.inventory.meta.ItemMeta").getMethod("setUnbreakable", boolean.class);
            modernSupported = true;
        } catch (Throwable ignored) {
            modernSupported = false;
        }

        if (!modernSupported) {
            try {
                // ---- Legacy NBT way (1.8 - 1.12) ----
                String craftPath = getCraftBukkitPath();

                final Class<?> craftItemStackClass = Class.forName(craftPath + ".inventory.CraftItemStack");
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", Class.forName(getMinecraftPath() + ".ItemStack"));

                Class<?> craftMetaItem = Class.forName(craftPath + ".inventory.CraftMetaItem"
                );
                metaGetMaterialMethod = craftMetaItem.getDeclaredMethod("getMaterial");
                metaGetMaterialMethod.setAccessible(true);

                final Class<?> nbtTagCompoundClass = Class.forName(getMinecraftPath() + ".NBTTagCompound");

                nbtTagConstructor = nbtTagCompoundClass.getConstructor();

                Class<?> nmsStackClass = Class.forName(getMinecraftPath() + ".ItemStack");

                hasTagMethod = nmsStackClass.getMethod("hasTag");
                getTagMethod = nmsStackClass.getMethod("getTag");
                setTagMethod = nmsStackClass.getMethod("setTag", nbtTagCompoundClass);
                setBooleanMethod = nbtTagCompoundClass.getMethod("setBoolean", String.class, boolean.class);
                getBooleanMethod = nbtTagCompoundClass.getMethod("getBoolean", String.class);

                reflectionReady = true;
            } catch (ClassNotFoundException | NoSuchMethodException t) {
                logger.logError(t, () -> "Could not resolve the unbreakable tag for this minecraft version");
                reflectionReady = false;
            }
        }
    }

    /**
     * Applies the "Unbreakable" property to the given ItemMeta.
     *
     * <p>On legacy versions (1.8–1.12), this will return a new copy of the metadata.
     * On modern versions (1.13+), the original metadata instance is modified and returned.</p>
     *
     * @param meta the ItemMeta to modify
     * @param unbreakable true to make the item unbreakable, false otherwise
     * @return the modified ItemMeta, it will be a new instance on legacy versions.
     */
    public static ItemMeta applyToMeta(final ItemMeta meta, final boolean unbreakable) {
        if (meta == null) return null;

        if (modernSupported) {
            meta.setUnbreakable(unbreakable);
            return meta;
        }
        if (!reflectionReady) return meta;

        try {
            Material original = (Material) metaGetMaterialMethod.invoke(meta);
            if (original == null || original == Material.AIR) original = Material.STONE;

            ItemStack stack = new ItemStack(original);
            stack.setItemMeta(meta);

            stack = applyToItem(stack, unbreakable);
            return stack.getItemMeta();
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.logError(e, () -> "Failed to invoke the material for this meta via legacy fallback");
            return meta;
        }
    }

    /**
     * Applies the "Unbreakable" property directly to the given ItemStack.
     *
     * <p>On legacy versions, this may create a new ItemStack copy.
     * On modern versions, the original ItemStack is modified.</p>
     *
     * @param item the ItemStack to modify
     * @param unbreakable true to make the item unbreakable, false otherwise
     * @return the modified ItemStack, may be a new instance on legacy versions
     */
    public static ItemStack applyToItem(final ItemStack item, final boolean unbreakable) {
        if (item == null) return null;

        if (modernSupported) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null)
                meta.setUnbreakable(unbreakable);
            item.setItemMeta(meta);
            return item;
        }

        if (!reflectionReady) return item;

        try {
            Object nmsItem = getNmsStack(item);

            boolean hasTag = (Boolean) hasTagMethod.invoke(nmsItem);
            Object tag = hasTag
                    ? getTagMethod.invoke(nmsItem)
                    : nbtTagConstructor.newInstance();

            setBooleanMethod.invoke(tag, "Unbreakable", unbreakable);
            setTagMethod.invoke(nmsItem, tag);

            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItem);

        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to apply legacy unbreakable");
            return item;
        }
    }

    /**
     * Checks whether the given ItemStack is marked as unbreakable.
     *
     * <p>On modern versions, this reads the metadata.
     * On legacy versions, it falls back to NBT reflection.</p>
     *
     * @param item the ItemStack to check
     * @return {@code true} if the item is unbreakable, {@code false} otherwise (or if the check failed on legacy versions)
     */
    public static boolean isUnbreakable(ItemStack item) {
        if (modernSupported) {
            final ItemMeta itemMeta = item.getItemMeta();
            return itemMeta != null && itemMeta.isUnbreakable();
        } else {
            if (!reflectionReady) return false;
            try {
                Object nmsItem = getNmsStack(item);

                if (!(Boolean) hasTagMethod.invoke(nmsItem)) {
                    return false;
                }
                Object tag = getTagMethod.invoke(nmsItem);
                return (Boolean) getBooleanMethod.invoke(tag, "Unbreakable");
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to invoke the item to check if it is unbreakable.");
                return false;
            }
        }
    }



    private static String getMinecraftPath() {
        return "net.minecraft.server."
                + getPackageVersion();
    }

    private static String getCraftBukkitPath() {
        return "org.bukkit.craftbukkit." + getPackageVersion();
    }


    private static String getPackageVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static Object getNmsStack(final ItemStack item) throws IllegalAccessException, InvocationTargetException {
        return asNMSCopyMethod.invoke(null, item);
    }
}
