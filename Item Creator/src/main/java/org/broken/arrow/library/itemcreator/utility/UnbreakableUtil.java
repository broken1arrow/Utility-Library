package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.NbtData;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

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
        return NMS_NBT_BRIDGE.applyUnbreakableTag(item, "Unbreakable", unbreakable);
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
            return NMS_NBT_BRIDGE.hasBooleanTag(item, "Unbreakable");
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
    public static class NmsNbtBridge {
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
        }

        /**
         * Indicates whether the current server version supports the modern
         * Bukkit API method {@code ItemMeta#setUnbreakable(boolean)}.
         *
         * <p>If this returns {@code true}, no reflection or NMS handling is required
         * and this bridge will remain inactive.</p>
         *
         * @return {@code true} if the modern API is available, otherwise {@code false}
         */
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
        public ItemStack applyUnbreakableTag(@Nonnull final ItemStack item, @Nonnull final String key, final boolean unbreakable) {
            NbtData nms = new NbtData(item);
            if (!nms.isReflectionReady()) return item;

            CompoundTag compound = nms.getOrCreateCompound();
            compound.setBoolean(key, unbreakable);
            return nms.apply(compound);
        }

        /**
         * Checks whether the given Bukkit item contains the specified
         * NBT key.
         *
         * @param item Bukkit ItemStack
         * @param key  NBT key
         * @return {@code true} if the key exists and is set to {@code true}
         */
        public boolean hasBooleanTag(@Nonnull final ItemStack item, @Nonnull String key) {
            NbtData nms = new NbtData(item);
            if (!nms.isReflectionReady()) return false;

            final CompoundTag compound = nms.getCompound();
            if (compound == null) {
                return false;
            }
            return compound.hasKey(key) && compound.getBoolean(key);
        }

    }
}
