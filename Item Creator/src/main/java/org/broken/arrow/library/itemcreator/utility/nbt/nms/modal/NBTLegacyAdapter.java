package org.broken.arrow.library.itemcreator.utility.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.NbtWrapper;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.ComponentFactory;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.api.NbtEditor;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.mappings.NBTItemTagMappings;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.logging.Level;


/**
 * A per-item reflective session providing access to the underlying NMS {@code ItemStack}
 * and its {@code NBTTagCompound}.
 *
 * <p>This class acts as a bridge between Bukkit {@link org.bukkit.inventory.ItemStack}
 * instances and the internal Minecraft representation. It exposes methods to read
 * and write primitive NBT values and manipulate item metadata directly.</p>
 *
 * <p>All operations are safe to call even if reflection is not fully initialized:
 * <ul>
 *   <li>It logs a warning message if the reflection layer could not be loaded.</li>
 *   <li>You can check {@link #isReady()} to verify that reflection is fully initialized.</li>
 *   <li>Null pointer checks are performed internally, so method calls will not break the server.</li>
 * </ul>
 * </p>
 *
 * <p>Use this class when you need low-level access to NMS item data. For higher-level,
 * version-independent operations, prefer using {@link NbtWrapper} or {@link ComponentFactory}.</p>
 */
public class NBTLegacyAdapter implements NbtEditor {
    private static final boolean IS_NEVER_16 = ItemCreator.getVersion().compareTo(16,5).newer();

    private static final Logging logger = new Logging(NBTLegacyAdapter.class);
    private static final MethodHandle NMS_ITEM_COPY;
    private static final MethodHandle AS_BUKKIT_ITEM_COPY;
    private static final MethodHandle HAS_TAG;
    private static final MethodHandle GET_TAG;
    private static final MethodHandle SET_TAG;
    private static final Constructor<?> nbtTagConstructor;
    private static final MethodHandle GET_NESTED_COMPOUND;
    private static final MethodHandle SET_NESTED_COMPOUND;

    private static final boolean REFLECTION_READY;
    private final Object nmsItemCopy;
    private final ItemStack bukkitItem;
    private CompoundState compoundState = CompoundState.NOT_CREATED;

    static {
        Constructor<?> tagConstructor = null;
        MethodHandle setNBTTag = null;
        MethodHandle getNBTTag = null;
        MethodHandle hasNBTTag = null;
        MethodHandle bukkitCopy = null;
        MethodHandle nMSCopyItem = null;
        MethodHandle getNestedCompound = null;
        MethodHandle setNestedCompound = null;
        boolean reflectionDone = false;

        try {
            final String craftPath = getCraftBukkitPath();
            final String nmsPath = getItemStackPath();
            final Class<?> craftItemStack = Class.forName(craftPath + ".inventory.CraftItemStack");
            final Class<?> nmsItemStack = Class.forName(nmsPath);
            final Class<?> nbtTagCompound = Class.forName(getNbtTagPath());
            final Class<?> nbtTagBase = Class.forName(getNbtTagBasePath());

            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final NBTItemTagMappings itemTagName = new NBTItemTagMappings();

            nMSCopyItem = lookup.findStatic(craftItemStack, "asNMSCopy",
                    MethodType.methodType(nmsItemStack, ItemStack.class));
            bukkitCopy = lookup.findStatic(craftItemStack, "asBukkitCopy",
                    MethodType.methodType(ItemStack.class, nmsItemStack));


            hasNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.hasTagName(),
                    MethodType.methodType(boolean.class));
            getNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.getTagName(),
                    MethodType.methodType(nbtTagCompound));
            setNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.setTagName(),
                    MethodType.methodType(void.class, nbtTagCompound));

            if (ItemCreator.getVersion().versionOlder(13.0))
                setNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.setNestedCompoundName(), MethodType.methodType(void.class, String.class, nbtTagBase));
            else
                setNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.setNestedCompoundName(), MethodType.methodType(nbtTagBase, String.class, nbtTagBase));
            getNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.getNestedCompoundName(), MethodType.methodType(nbtTagBase, String.class));

            tagConstructor = nbtTagCompound.getConstructor();
            reflectionDone = true;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.logError(e, () -> "Failed to initialize all NMS methods needed for modern minecraft.");
        }
        nbtTagConstructor = tagConstructor;
        SET_TAG = setNBTTag;
        GET_TAG = getNBTTag;
        HAS_TAG = hasNBTTag;
        AS_BUKKIT_ITEM_COPY = bukkitCopy;
        NMS_ITEM_COPY = nMSCopyItem;
        GET_NESTED_COMPOUND = getNestedCompound;
        SET_NESTED_COMPOUND = setNestedCompound;
        REFLECTION_READY = reflectionDone;

    }

    public NBTLegacyAdapter(@Nonnull final ItemStack item) {
        this.nmsItemCopy = toNmsItemStack(item);
        this.bukkitItem = item.clone();
    }



    @Override
    public boolean isReady() {
        if (!REFLECTION_READY)
            return false;

        return nmsItemCopy != null;
    }

    @Override
    public boolean hasTag() {
        if (!REFLECTION_READY || nmsItemCopy == null) return false;
        try {
            return (boolean) HAS_TAG.invoke(this.nmsItemCopy);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to initialize hasTag");
        }
        return false;
    }

    @Override
    public boolean hasTag(@Nonnull final String name) {
        try {
            if (!hasTag()) return false;
            Object root = GET_TAG.invoke(nmsItemCopy);
            if (name.isEmpty()) return root != null;
            Object nested = GET_NESTED_COMPOUND.invoke(root, name);
            return nested != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nonnull
    @Override
    public CompoundTag enableVanillaTagEditor() {
        return new CompoundTag(this.getCompound());
    }

    @Override
    @Nullable
    public CompoundTag getOrCreateCompound() {
        return getOrCreateCompoundTag("", false);
    }

    @Override
    @Nullable
    public CompoundTag getOrCreateCompound(@Nonnull final String name) {
        return getOrCreateCompoundTag(name, true);
    }

    @Override
    @Nullable
    public CompoundTag getCompound() {
        return getCompound("", false);
    }

    @Override
    @Nullable
    public CompoundTag getCompound(@Nonnull final String name) {
        return getCompound(name, true);
    }

    @Override
    @Nonnull
    public ItemStack finalizeChanges() {
        if (!REFLECTION_READY || nmsItemCopy == null) return this.bukkitItem;
        if (compoundState != CompoundState.CREATED) {
            logger.log(() -> "FinalizeChanges: " + compoundState.getMessage());
            return this.bukkitItem;
        }
        try {
            Object compound = GET_TAG.invoke(nmsItemCopy);
            if (compound == null) {
                logger.log(() -> "Failed to initialize the item creation, because the compound is not created yet.");
                return this.bukkitItem;
            }
            SET_TAG.invoke(nmsItemCopy, compound);
            return (ItemStack) AS_BUKKIT_ITEM_COPY.invoke(nmsItemCopy);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to apply back to itemStack");
        }
        return this.bukkitItem;
    }

    /**
     * Retrieve the compound path
     *
     * @return the path to the compound class.
     */
    public static String getNbtTagPath() {
        final String nmsPath = getNmsPath();

        if (IS_NEVER_16) {
            if (ItemCreator.getVersion().versionNewer(20.4))
                return nmsPath + ".nbt.CompoundTag";
            return nmsPath + ".nbt.NBTTagCompound";
        }
        return nmsPath + ".NBTTagCompound";
    }

    /**
     * Converts a Bukkit ItemStack into its NMS counterpart.
     *
     * @param item Bukkit ItemStack to convert.
     * @return the underlying Nms itemStack.
     */
    private Object toNmsItemStack(@Nonnull final ItemStack item) {
        if(!REFLECTION_READY) return null;
        try {
            return NMS_ITEM_COPY.invoke(item);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to copy the bukkit stack to nms stack");
        }
        return null;
    }

    /**
     * Internal helper to get or create a {@link CompoundTag}.
     * <p>
     * If {@code usingName} is true and the {@code name} parameter is empty,
     * a debug log is written suggesting to use {@link #getOrCreateCompound()} instead
     * for the root compound.
     *
     * @param name      the name of the nested compound; empty string returns the root.
     * @param usingName whether this call is intended for a named compound.
     * @return the requested {@link CompoundTag}, or null if reflection fails.
     */
    private CompoundTag getOrCreateCompoundTag(final String name, final boolean usingName) {
        if (usingName && name.isEmpty())
            logger.log(Level.FINE, () -> "Empty string passed to getOrCreateCompound(name). Use getOrCreateCompound() for root instead.");
        try {
            Object root;
            Object nested = null;
            if (hasTag()) {
                root = GET_TAG.invoke(nmsItemCopy);
            } else {
                root = nbtTagConstructor.newInstance();
                SET_TAG.invoke(nmsItemCopy, root);
            }
            if (!name.isEmpty()) {
                nested = GET_NESTED_COMPOUND.invoke(root, name);
                if (nested == null) {
                    nested = nbtTagConstructor.newInstance();
                    SET_NESTED_COMPOUND.invoke(root, name, nested);
                }
            }
            this.compoundState = CompoundState.CREATED;
            return new CompoundTag((nested != null ? nested : root));
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to initialize CompoundTag");
            this.compoundState = CompoundState.ERROR;
        }
        return null;
    }

    /**
     * Internal helper to get a {@link CompoundTag} if present.
     * <p>
     * If {@code usingName} is true and the {@code name} parameter is empty,
     * a debug log is written suggesting to use {@link #getCompound()} instead
     * for the root compound.
     *
     * @param name      the name of the nested compound; empty string returns the root.
     * @param usingName whether this call is intended for a named compound.
     * @return the requested {@link CompoundTag}, or null if not present or reflection fails.
     */
    private CompoundTag getCompound(final String name, final boolean usingName) {
        if (usingName && name.isEmpty())
            logger.log(Level.FINE, () -> "Empty string passed to getCompound(name). Use getCompound() for root instead.");

        try {
            if (!hasTag()) {
                this.compoundState = CompoundState.NULL;
                return null;
            }
            Object root = GET_TAG.invoke(nmsItemCopy);
            Object nested = null;
            if (!name.isEmpty()) {
                nested = GET_NESTED_COMPOUND.invoke(root, name);
                if (nested == null) {
                    logger.log(Level.CONFIG, () -> "Requested nested compound '" + name + "' not found. Returning root instead.");
                }
            }
            this.compoundState = CompoundState.CREATED;
            return new CompoundTag((nested != null ? nested : root));
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to initialize CompoundTag");
            this.compoundState = CompoundState.ERROR;
        }
        return null;
    }

    private static String getNbtTagBasePath() {
        final String nmsPath = getNmsPath();
        if (IS_NEVER_16)
            return nmsPath + ".nbt.NBTBase";

        return nmsPath + ".NBTBase";
    }

    private static String getCraftBukkitPath() {
        final String packageVersion = getPackageVersion();
        if (packageVersion.isEmpty())
            return "org.bukkit.craftbukkit";
        return "org.bukkit.craftbukkit." + packageVersion;
    }

    private static String getItemStackPath() {
        final String nmsPath = getNmsPath();
        if (IS_NEVER_16)
            return nmsPath + ".world.item.ItemStack";
        return nmsPath + ".ItemStack";
    }

    private static String getNmsPath() {
        if (IS_NEVER_16)
            return "net.minecraft";
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
        if (ItemCreator.getVersion().versionNewer(20.4))
            return "";
        return Bukkit.getServer().getClass().toGenericString().split("\\.")[3];
    }

    /**
     * Represents the current state of a compound during retrieval or creation.
     *
     * <p>There are four possible states:</p>
     * <ul>
     *     <li><strong>NOT_CREATED</strong> – The compound has not been created yet.</li>
     *     <li><strong>CREATED</strong> – The compound was successfully retrieved or created.
     *         This may refer to either the root compound or a nested compound.</li>
     *     <li><strong>NULL</strong> – The root or nested compound does not exist.</li>
     *     <li><strong>ERROR</strong> – An error occurred while attempting to retrieve or create the compound.</li>
     * </ul>
     */
    public enum CompoundState {
        NOT_CREATED("Compound has not been created yet and may be null"),
        CREATED("Compound created successfully"),
        ERROR("Failed to initialize compound"),
        NULL("Compound does not exist");

        private final String message;

        CompoundState(@Nonnull final String message) {
            this.message = message;
        }

        /**
         * Returns a descriptive message suitable for logging.
         *
         * @return the descriptive message for this state.
         */
        public String getMessage() {
            return message;
        }
    }

}
