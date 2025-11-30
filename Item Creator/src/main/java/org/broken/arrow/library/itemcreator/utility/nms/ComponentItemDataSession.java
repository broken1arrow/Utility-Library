package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Reflection-based session for 1.20.5+ item components (CUSTOM_DATA).
 * Works by reading/writing net.minecraft.world.item.component.CustomData via DataComponentTypes.CUSTOM_DATA.
 * <p>
 * Relies on your project-local CompoundTag wrapper which must:
 * - have a constructor CompoundTag(Object nmsCompound)
 * - expose boolean hasKey(String)
 * - expose CompoundTag getCompound(String)
 * - expose Object getHandle() -> returns underlying net.minecraft.nbt.CompoundTag
 */
public class ComponentItemDataSession implements NbtEditor {

    private static final Logging logger = new Logging(ComponentItemDataSession.class);

    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle ITEMSTACK_GET;
    private static final MethodHandle ITEMSTACK_SET;
    private static final MethodHandle CUSTOMDATA_OF;
    private static final MethodHandle CUSTOMDATA_COPYTAG;
    private static final MethodHandle SET_NESTED_COMPOUND;
    private static final Class<?> NMS_COMPOUND_TAG_CLASS;
    private static final Object CUSTOM_DATA_KEY;

    private final ItemStack originalBukkit;
    private final Object nmsStack;
    private final boolean ready = true;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsItem = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> customData = Class.forName("net.minecraft.world.item.component.CustomData");
            Class<?> dataComponentTypes = Class.forName("net.minecraft.world.item.component.DataComponentTypes");
            NMS_COMPOUND_TAG_CLASS = Class.forName("net.minecraft.nbt.CompoundTag");

            // CraftItemStack.asNMSCopy(ItemStack)
            AS_NMS_COPY = lookup.findStatic(craftItem,
                    "asNMSCopy",
                    MethodType.methodType(nmsItem, ItemStack.class));
            // CraftItemStack.asBukkitCopy(nmsStack)
            AS_BUKKIT_COPY = lookup.findStatic(craftItem,
                    "asBukkitCopy",
                    MethodType.methodType(ItemStack.class, nmsItem));

            // DataComponentTypes.CUSTOM_DATA
            CUSTOM_DATA_KEY = dataComponentTypes.getField("CUSTOM_DATA").get(null);

            // ItemStack.get(DataComponentType)
            ITEMSTACK_GET = lookup.findVirtual(nmsItem,
                    "get",
                    MethodType.methodType(Object.class, CUSTOM_DATA_KEY.getClass()));

            // ItemStack.set(DataComponentType, DataComponent)
            ITEMSTACK_SET = lookup.findVirtual(nmsItem,
                    "set",
                    MethodType.methodType(void.class, CUSTOM_DATA_KEY.getClass(), customData));

            // CustomData.of(CompoundTag)
            CUSTOMDATA_OF = lookup.findStatic(customData,
                    "of",
                    MethodType.methodType(customData, NMS_COMPOUND_TAG_CLASS));

            // CustomData.copyTag()
            CUSTOMDATA_COPYTAG = lookup.findVirtual(customData,
                    "copyTag",
                    MethodType.methodType(NMS_COMPOUND_TAG_CLASS));

            // Reflection handle for setting nested compound: root.set(name, compound)
            SET_NESTED_COMPOUND = lookup.findVirtual(NMS_COMPOUND_TAG_CLASS, "put", MethodType.methodType(NMS_COMPOUND_TAG_CLASS, String.class, NMS_COMPOUND_TAG_CLASS));

        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize reflection handles", t);
        }
    }

    public ComponentItemDataSession(ItemStack bukkitItem) {
        this.originalBukkit = bukkitItem;
        this.nmsStack = toNmsItemStack(bukkitItem);
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean hasTag() {
        return getRootNms().isPresent();
    }

    @Override
    public boolean hasTag(@Nonnull String name) {
        Optional<Object> root = getRootNms();
        if (!root.isPresent()) return false;
        if (name.isEmpty()) return true;

        try {
            Object nested = root.get().getClass()
                    .getMethod("contains", String.class)
                    .invoke(root.get(), name);
            return nested != null && (boolean) nested;
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to check tag " + name);
            return false;
        }
    }

    @Nullable
    @Override
    public CompoundTag getOrCreateCompound() {
        return getOrCreateCompound("");
    }

    @Nullable
    @Override
    public CompoundTag getOrCreateCompound(@Nonnull String name) {
        Optional<Object> rootOpt = getOrCreateRootNms();
        if (!rootOpt.isPresent()) return null;
        Object root = rootOpt.get();

        if (name.isEmpty()) return new CompoundTag(root); // wrapper only

        try {
            Object nested = root.getClass().getMethod("getCompound", String.class).invoke(root, name);
            if (nested == null) {
                // Create a new NMS CompoundTag
                nested = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
                // Set directly into the root
                SET_NESTED_COMPOUND.invoke(root, name, nested);
                // Write back to item stack
                setRootNms(root);
            }
            return new CompoundTag(nested); // wrapper only
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to getOrCreate nested compound " + name);
            return null;
        }
    }

    @Nullable
    @Override
    public CompoundTag getCompound() {
        return getCompound("");
    }

    @Nullable
    @Override
    public CompoundTag getCompound(@Nonnull String name) {
        Optional<Object> rootOpt = getRootNms();
        if (!rootOpt.isPresent()) return null;
        Object root = rootOpt.get();

        if (name.isEmpty()) return new CompoundTag(root);

        try {
            Object nested = root.getClass().getMethod("getCompound", String.class).invoke(root, name);
            if (nested == null) return null;
            return new CompoundTag(nested);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to get nested compound " + name);
            return null;
        }
    }

    @Nonnull
    @Override
    public ItemStack finalizeChanges() {
        try {
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsStack);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to finalize item");
            return originalBukkit;
        }
    }

    // -----------------------
    // Internal helpers
    // -----------------------

    private Object toNmsItemStack(@Nonnull ItemStack item) {
        try {
            return AS_NMS_COPY.invoke(item);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to convert Bukkit to NMS stack");
            return null;
        }
    }

    private Optional<Object> getRootNms() {
        try {
            Object customData = ITEMSTACK_GET.invoke(nmsStack, CUSTOM_DATA_KEY);
            if (customData == null) return Optional.empty();
            Object tag = CUSTOMDATA_COPYTAG.invoke(customData);
            return Optional.of(tag);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to get CUSTOM_DATA root");
            return Optional.empty();
        }
    }

    private Optional<Object> getOrCreateRootNms() {
        Optional<Object> existing = getRootNms();
        if (existing.isPresent()) return existing;

        try {
            Object newTag = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
            Object customDataInstance = CUSTOMDATA_OF.invoke(newTag);
            ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_KEY, customDataInstance);
            return Optional.of(newTag);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to create CUSTOM_DATA root");
            return Optional.empty();
        }
    }

    private void setRootNms(@Nonnull Object root) {
        try {
            Object customDataInstance = CUSTOMDATA_OF.invoke(root);
            ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_KEY, customDataInstance);
        } catch (Throwable t) {
            logger.logError(t, () -> "Failed to set CUSTOM_DATA root");
        }
    }

}