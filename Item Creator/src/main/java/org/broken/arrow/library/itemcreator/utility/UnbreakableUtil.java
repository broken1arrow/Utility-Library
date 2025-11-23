package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class UnbreakableUtil {
    private static final Logging logger = new Logging(UnbreakableUtil.class);
    private static final NmsNbtBridge NMS_NBT_BRIDGE;

    private UnbreakableUtil() {
    }

    static {
        NMS_NBT_BRIDGE = new NmsNbtBridge();
    }

    /**
     * Applies the "Unbreakable" property to the given ItemMeta.
     *
     * <p>On legacy versions (1.8–1.12), this will return a new copy of the metadata.
     * On modern versions (1.13+), the original metadata instance is modified and returned.</p>
     *
     * @param meta        the ItemMeta to modify
     * @param unbreakable true to make the item unbreakable, false otherwise
     * @return the modified ItemMeta, it will be a new instance on legacy versions.
     */
    public static ItemMeta applyToMeta(@Nullable final ItemMeta meta, final boolean unbreakable) {
        if (meta == null) return null;

        if (NMS_NBT_BRIDGE.isModernSupported()) {
            meta.setUnbreakable(unbreakable);
            return meta;
        }
        if (!NMS_NBT_BRIDGE.isReflectionReady()) return meta;

        Material original = getMetaType(meta);
        if (original == null || original == Material.AIR) original = Material.STONE;
        ItemStack stack = new ItemStack(original);
        stack.setItemMeta(meta);

        stack = applyToItem(stack, unbreakable);
        return stack.getItemMeta();
    }

    /**
     * Applies the "Unbreakable" property directly to the given ItemStack.
     *
     * <p>On legacy versions, this may create a new ItemStack copy.
     * On modern versions, the original ItemStack is modified.</p>
     *
     * @param item        the ItemStack to modify
     * @param unbreakable true to make the item unbreakable, false otherwise
     * @return the modified ItemStack, may be a new instance on legacy versions
     */
    public static ItemStack applyToItem(@Nullable final ItemStack item, final boolean unbreakable) {
        if (item == null) return null;

        if (NMS_NBT_BRIDGE.isModernSupported()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null)
                meta.setUnbreakable(unbreakable);
            item.setItemMeta(meta);
            return item;
        }

        if (!NMS_NBT_BRIDGE.isReflectionReady()) return item;

        try {
            return NMS_NBT_BRIDGE.applyUnbreakableTag(item, "Unbreakable", unbreakable);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException t) {
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
    public static boolean isUnbreakable(@Nullable final ItemStack item) {
        if (item == null) return false;

        if (NMS_NBT_BRIDGE.isModernSupported()) {
            final ItemMeta itemMeta = item.getItemMeta();
            return itemMeta != null && itemMeta.isUnbreakable();
        } else {
            if (!NMS_NBT_BRIDGE.isReflectionReady()) return false;
            try {
                return NMS_NBT_BRIDGE.hasBooleanTag(item, "Unbreakable");
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                logger.logError(e, () -> "Failed to invoke the item to check if it is unbreakable.");
                return false;
            }
        }
    }


    @Nullable
    private static Material getMetaType(@Nonnull final ItemMeta meta) {
        Material material;
        if (meta instanceof SkullMeta) material = Material.getMaterial("SKULL_ITEM");
        else if (meta instanceof LeatherArmorMeta) material = Material.LEATHER_HELMET;
        else if (meta instanceof BannerMeta) material = Material.getMaterial("BANNER");
        else if (meta instanceof FireworkEffectMeta) material = Material.FIREWORK_STAR;
        else if (meta instanceof FireworkMeta) material = Material.FIREWORK_ROCKET;
        else if (meta instanceof MapMeta) material = Material.MAP;
        else if (meta instanceof BookMeta) material = Material.WRITTEN_BOOK;
        else if (meta instanceof PotionMeta) material = Material.POTION;
        else material = Material.STONE;

        return material;
    }

    /**
     * Provides version-safe access to NMS (net.minecraft.server) and NBT functionality
     * using reflection. This is required for legacy versions of Minecraft where
     * Bukkit's modern API does not expose certain meta fields such as "Unbreakable".
     *
     * <p>If the server version supports {@code ItemMeta#setUnbreakable(boolean)},
     * this class disables itself automatically.</p>
     *
     * <p>Otherwise it dynamically resolves required CraftBukkit and NMS classes
     * and methods.</p>
     */
    private static class NmsNbtBridge {
        private Method asNMSCopyMethod;
        private Method asBukkitCopyMethod;
        private Method hasTagMethod;
        private Method getTagMethod;
        private Method setTagMethod;
        private Method setBooleanMethod;
        private Method getBooleanMethod;
        private Constructor<?> nbtTagConstructor;

        private boolean reflectionReady;
        private boolean modernSupported;

        /**
         * Creates a new NMS bridge.
         *
         * <p>Checks for availability of {@link org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)}.
         * If present, reflection loading is skipped.</p>
         */
        private NmsNbtBridge() {
            try {
                Class.forName("org.bukkit.inventory.meta.ItemMeta").getMethod("setUnbreakable", boolean.class);
                this.modernSupported = true;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                this.modernSupported = false;
            }
            if (this.modernSupported) return;
            loadLegacyReflection();
        }

        public Method getAsBukkitCopyMethod() {
            return asBukkitCopyMethod;
        }

        public Method getHasTagMethod() {
            return hasTagMethod;
        }

        public Method getTagMethod() {
            return getTagMethod;
        }

        public Method getSetTagMethod() {
            return setTagMethod;
        }

        public Method getSetBooleanMethod() {
            return setBooleanMethod;
        }

        public Method getBooleanMethod() {
            return getBooleanMethod;
        }

        public Constructor<?> getNbtTagConstructor() {
            return nbtTagConstructor;
        }

        public boolean isReflectionReady() {
            return reflectionReady;
        }

        public boolean isModernSupported() {
            return modernSupported;
        }

        /**
         * Adds or updates the "Unbreakable" NBT boolean on an ItemStack.
         * This runs only on legacy versions since modern servers provide an API.
         *
         * @param item        the Bukkit ItemStack
         * @param key         the NBT key (typically "Unbreakable")
         * @param unbreakable whether the item should be unbreakable
         * @return a new Bukkit ItemStack instance with updated NBT
         */
        public ItemStack applyUnbreakableTag(@Nonnull final ItemStack item, @Nonnull final String key, final boolean unbreakable) throws InvocationTargetException, IllegalAccessException, InstantiationException {
            Object nmsItem = this.toNmsItemStack(item);
            this.applyBooleanTag(nmsItem, key, unbreakable);
            return (ItemStack) this.getAsBukkitCopyMethod().invoke(null, nmsItem);
        }

        /**
         * Checks whether the given Bukkit item contains the specified
         * boolean NBT key.
         *
         * @param item Bukkit ItemStack
         * @param key  NBT key
         * @return {@code true} if the key exists and is set to {@code true}
         */
        public boolean hasBooleanTag(@Nonnull final ItemStack item, @Nonnull String key) throws InvocationTargetException, IllegalAccessException, InstantiationException {
            Object nmsItem = this.toNmsItemStack(item);
            if (!(Boolean) this.getHasTagMethod().invoke(nmsItem)) {
                return false;
            }
            Object tag = this.getOrCreateNbtTag(nmsItem);
            return (Boolean) this.getBooleanMethod().invoke(tag, key);
        }

        /**
         * Applies a boolean tag to an NMS ItemStack.
         *
         * @param nmsItem     the nms stack.
         * @param key         the key to set
         * @param unbreakable if it set to {@code true} it will be unbreakable, otherwise the tool can break.
         */
        public void applyBooleanTag(@Nonnull final Object nmsItem, @Nonnull final String key, final boolean unbreakable) throws InvocationTargetException, IllegalAccessException, InstantiationException {
            final Object tag = this.getOrCreateNbtTag(nmsItem);
            this.setBooleanTag(tag, key, unbreakable);
            this.applyTagCompound(nmsItem, tag);
        }

        /**
         * Applies an NBT compound to an NMS ItemStack.
         *
         * @param nmsItem the nms stack.
         * @param tag     the NBTTagCompound to be set.
         */
        public void applyTagCompound(@Nonnull final Object nmsItem, @Nonnull final Object tag) throws InvocationTargetException, IllegalAccessException {
            getSetTagMethod().invoke(nmsItem, tag);
        }

        /**
         * Sets a boolean inside an NBTTagCompound.
         *
         * @param tag         the NBTTagCompound to be set.
         * @param key         the key to set
         * @param unbreakable if it shall be true or false.
         */
        public void setBooleanTag(@Nonnull final Object tag, @Nonnull final String key, final boolean unbreakable) throws InvocationTargetException, IllegalAccessException {
            getSetBooleanMethod().invoke(tag, key, unbreakable);
        }

        /**
         * Returns the existing NBTTagCompound if one is present,
         * otherwise creates a new one.
         *
         * @param nmsItem the nms stack.
         * @return the NBTTagCompound if exist or create new one.
         */
        public Object getOrCreateNbtTag(@Nonnull final Object nmsItem) throws InvocationTargetException, IllegalAccessException, InstantiationException {
            boolean hasTag = (boolean) hasTagMethod.invoke(nmsItem);
            return hasTag
                    ? getTagMethod.invoke(nmsItem)
                    : nbtTagConstructor.newInstance();
        }

        /**
         * Resolves all CraftBukkit and NMS classes and methods using reflection.
         * This is only required for legacy versions.
         */
        private void loadLegacyReflection() {
            try {

                String craftPath = getCraftBukkitPath();

                final Class<?> craftItemStackClass = Class.forName(craftPath + ".inventory.CraftItemStack");
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", Class.forName(getMinecraftPath() + ".ItemStack"));

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

        private String getMinecraftPath() {
            return "net.minecraft.server."
                    + getPackageVersion();
        }

        private String getCraftBukkitPath() {
            return "org.bukkit.craftbukkit." + getPackageVersion();
        }

        /**
         * Extracts the version identifier from the Bukkit server package.
         * This version will only work on legacy, as the path changed in newer
         * minecraft versions.
         * Example: v1_8_R3
         *
         * @return it returns for example v1_8_R3
         */
        private String getPackageVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }

        /**
         * Converts a Bukkit ItemStack into its NMS counterpart.
         *
         * @param item Bukkit ItemStack to convert.
         * @return the underlying Nms itemStack.
         */
        private Object toNmsItemStack(@Nonnull final ItemStack item) throws IllegalAccessException, InvocationTargetException {
            return this.asNMSCopyMethod.invoke(null, item);
        }

    }
}
