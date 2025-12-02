package org.broken.arrow.library.itemcreator.utility.nms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Reflection-based session for 1.20.5/1.20.6 item components (CUSTOM_DATA).
 * Handles the logic required to bridge Mutable NBT -> Immutable CustomData Component.
 */
/**
 * Reflection-based session for 1.20.5+ item components (CUSTOM_DATA + vanilla DataComponentType support).
 *
 * Notes:
 * - CompoundTag is used as a thin wrapper around an NMS Tag instance (any net.minecraft.nbt.Tag).
 * - The class caches expensive MethodHandles and registry objects in the static initializer.
 */
public class ComponentItemDataSession implements NbtEditor {
    private static final Logging log = new Logging(ComponentItemDataSession.class);

    // --- Core Reflection Handles ---
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle ITEMSTACK_GET;
    private static final MethodHandle ITEMSTACK_SET;
    private static final MethodHandle CUSTOMDATA_OF;
    private static final MethodHandle CUSTOMDATA_COPYTAG;
    private static MethodHandle SET_NESTED_COMPOUND;

    // --- Serialization Reflection Handles (Vanilla Support) ---
    private static MethodHandle REGISTRY_GET;             // MappedRegistry.get(ResourceLocation)
    private static MethodHandle RESOURCELOCATION_INIT;   // new ResourceLocation(String)
    private static final MethodHandle COMPONENT_TYPE_CODEC; // DataComponentType.codec()
    private static final MethodHandle CODEC_ENCODE; // Codec.encodeStart(Ops, Object)
    private static final MethodHandle CODEC_PARSE;  // Codec.parse(Ops, Object)
    private static final MethodHandle DATARESULT_GET_OR_THROW; // DataResult.getOrThrow()
    private static final Object NBT_OPS_INSTANCE; // NbtOps.INSTANCE
    private static Object DATA_COMPONENT_REGISTRY; // the registry instance

    private static final Class<?> NMS_COMPOUND_TAG_CLASS;
    private static Class<?> NMS_TAG_CLASS;
    private static final Object CUSTOM_DATA_TYPE_KEY;
    private static boolean ready = true;

    // --- Session State ---
    private final ItemStack originalBukkit;
    private final Object nmsStack;

    // Cache for your plugin's internal data (inside custom_data)
    // This holds the mutable NMS CompoundTag instance from CustomData.copyTag()
    private Object rootCustomDataCache;

    // Cache for Vanilla components (converted to NBT for editing)
    // Map<String (minecraft:id), NMS Tag (Compound / Int / String / List / etc.)>
    private final Map<String, Object> vanillaComponentCache = new HashMap<>();

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            // --- Classes ---
            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsItem = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");
            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
            NMS_COMPOUND_TAG_CLASS = Class.forName("net.minecraft.nbt.CompoundTag");
            NMS_TAG_CLASS = Class.forName("net.minecraft.nbt.Tag");

            // New Classes for Vanilla Translation
            Class<?> builtInRegistries = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Class<?> registriesClass = Class.forName("net.minecraft.core.registries.Registries");
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Class<?> codecClass = Class.forName("com.mojang.serialization.Codec");
            Class<?> dynamicOpsClass = Class.forName("com.mojang.serialization.DynamicOps");
            Class<?> dataResultClass = Class.forName("com.mojang.serialization.DataResult");
            Class<?> nbtOpsClass = Class.forName("net.minecraft.nbt.NbtOps");

            // --- Core handles (NMS <-> Bukkit) ---
            AS_NMS_COPY = lookup.findStatic(craftItem, "asNMSCopy", MethodType.methodType(nmsItem, ItemStack.class));
            AS_BUKKIT_COPY = lookup.findStatic(craftItem, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItem));

            // CUSTOM_DATA key (obfuscation safe)
            Field dataField;
            try {
                dataField = dataComponentsClass.getField("CUSTOM_DATA");
            } catch (NoSuchFieldException e) {
                dataField = dataComponentsClass.getField("b"); // fallback obf-name
            }
            CUSTOM_DATA_TYPE_KEY = dataField.get(null);

            // ItemStack.get(DataComponentType) and set(DataComponentType, value)
            Method getMethod = findMethod(nmsItem, Object.class, Class.forName("net.minecraft.core.component.DataComponentType"));
            ITEMSTACK_GET = lookup.unreflect(getMethod);

            Method setMethod = findMethod(nmsItem, Object.class, Class.forName("net.minecraft.core.component.DataComponentType"), Object.class);
            ITEMSTACK_SET = lookup.unreflect(setMethod);

            // CustomData.of(CompoundTag) and copyTag()
            Method ofMethod = findStaticMethod(customDataClass, customDataClass, NMS_COMPOUND_TAG_CLASS);
            CUSTOMDATA_OF = lookup.unreflect(ofMethod);

            Method copyTagMethod = findMethod(customDataClass, NMS_COMPOUND_TAG_CLASS);
            CUSTOMDATA_COPYTAG = lookup.unreflect(copyTagMethod);

            // SET_NESTED_COMPOUND -> CompoundTag.put(name, tag)
            try {
                SET_NESTED_COMPOUND = lookup.findVirtual(NMS_COMPOUND_TAG_CLASS, "put", MethodType.methodType(NMS_TAG_CLASS, String.class, NMS_TAG_CLASS));
            } catch (Throwable t) {
                // fallback to reflective method find
                SET_NESTED_COMPOUND = lookup.unreflect(findMethod(NMS_COMPOUND_TAG_CLASS, NMS_TAG_CLASS, String.class, NMS_TAG_CLASS));
            }

            // --- Vanilla codec helpers ---
            NBT_OPS_INSTANCE = nbtOpsClass.getField("INSTANCE").get(null);

            // Get the DataComponent registry directly from BuiltInRegistries (MappedRegistry)
            DATA_COMPONENT_REGISTRY = builtInRegistries.getField("DATA_COMPONENT_TYPE").get(null);

            // ResourceLocation constructor
            RESOURCELOCATION_INIT = lookup.findConstructor(resourceLocationClass, MethodType.methodType(void.class, String.class));

            // Bind the MappedRegistry.get(ResourceLocation) on the registry instance
            REGISTRY_GET = lookup.findVirtual(
                    DATA_COMPONENT_REGISTRY.getClass(),
                    "get",
                    MethodType.methodType(Object.class, resourceLocationClass)
            );

            // codecs & encode/parse handles
            COMPONENT_TYPE_CODEC = lookup.findVirtual(dataComponentTypeClass, "codec", MethodType.methodType(codecClass));
            CODEC_ENCODE = lookup.findVirtual(codecClass, "encodeStart", MethodType.methodType(dataResultClass, dynamicOpsClass, Object.class));
            CODEC_PARSE = lookup.findVirtual(codecClass, "parse", MethodType.methodType(dataResultClass, dynamicOpsClass, Object.class));
            DATARESULT_GET_OR_THROW = lookup.findVirtual(dataResultClass, "getOrThrow", MethodType.methodType(Object.class));

        } catch (Throwable t) {
            ready = false;
            t.printStackTrace();
            throw new RuntimeException("Failed to initialize NMS 1.20.5+ reflection", t);
        }
    }

    /**
     * The new instance of the component.
     *
     * @param bukkitItem the item stack to modify.
     */
    public ComponentItemDataSession(ItemStack bukkitItem) {
        this.originalBukkit = bukkitItem;
        this.nmsStack = toNmsItemStack(bukkitItem);
        this.rootCustomDataCache = loadRootFromItem();
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean hasTag() {
        return rootCustomDataCache != null;
    }

    @Override
    public boolean hasTag(@Nonnull String name) {
        if (rootCustomDataCache == null) return false;
        if (name.isEmpty()) return true;
        try {
            Object res = rootCustomDataCache.getClass().getMethod("contains", String.class).invoke(rootCustomDataCache, name);
            return res != null && (boolean) res;
        } catch (Throwable t) {
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
        return getCompoundLogic(name, true);
    }

    @Nullable
    @Override
    public CompoundTag getCompound() {
        return getCompound("");
    }

    @Nullable
    @Override
    public CompoundTag getCompound(@Nonnull String name) {
        return getCompoundLogic(name, false);
    }

    @Nonnull
    @Override
    public ItemStack finalizeChanges() {
        try {
            applyCustomDataCache();
            applyVanillaCache();
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsStack);
        } catch (Throwable t) {
            t.printStackTrace();
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
            log.logError(t, () -> "Could not create the NMS itemStack.");
            return null;
        }
    }

    /**
     * Reads the current CUSTOM_DATA component from the NMS stack
     * and unwraps it into a mutable NMS CompoundTag (copy).
     *
     * @return the root component or new one i not exist.
     */
    private Object loadRootFromItem() {
        try {
            Object customData = ITEMSTACK_GET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY);
            if (customData == null) return null;
            return CUSTOMDATA_COPYTAG.invoke(customData);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Wraps the cached NMS CompoundTag into a CustomData component
     * and sets it onto the NMS stack.
     */
    private void applyCustomDataCache() throws Throwable {
        if (rootCustomDataCache == null) return;
        Object customDataInstance = CUSTOMDATA_OF.invoke(rootCustomDataCache);
        ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY, customDataInstance);
    }

    private CompoundTag getCompoundLogic(@Nonnull String name, boolean create) {
        if (name.contains(":")) { // treat names with namespace as vanilla components
            return getVanillaComponentAsNbt(name, create);
        } else {
            return getInternalCustomData(name, create);
        }
    }

    /**
     * Internal custom_data logic (NMS CompoundTag backed)
     *
     * @param name of your custom tag.
     * @param create if it shall create new one if missing.
     * @return the {@link CompoundTag} that you can set values.
     */
    private CompoundTag getInternalCustomData(String name, boolean create) {
        if (rootCustomDataCache == null) {
            if (!create) return null;
            try {
                rootCustomDataCache = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        }

        Object root = rootCustomDataCache;
        if (name.isEmpty()) return new CompoundTag(root);

        try {
            boolean exists = (boolean) root.getClass().getMethod("contains", String.class).invoke(root, name);
            Object nested;
            if (!exists) {
                if (!create) return null;
                nested = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
                SET_NESTED_COMPOUND.invoke(root, name, nested);
            } else {
                nested = root.getClass().getMethod("getCompound", String.class).invoke(root, name);
            }
            return new CompoundTag(nested);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Vanilla component -> NBT translation (supports any NBT tag)
     *
     * @param componentId the minecraft vanillas name
     * @param create if it shall creater new one if missing.
     * @return the {@link CompoundTag} that you can set values.
     */
    private CompoundTag getVanillaComponentAsNbt(String componentId, boolean create) {
        // fast cache check
        if (vanillaComponentCache.containsKey(componentId)) {
            return new CompoundTag(vanillaComponentCache.get(componentId));
        }

        try {
            // Resolve type via registry
            Object resLoc = RESOURCELOCATION_INIT.invoke(componentId);
            Object componentType = REGISTRY_GET.invoke(DATA_COMPONENT_REGISTRY, resLoc);
            if (componentType == null) return null;

            // Read current component value from ItemStack
            Object currentValue = ITEMSTACK_GET.invoke(nmsStack, componentType);

            Object tagBase;
            if (currentValue == null) {
                if (!create) return null;
                // create empty compound for new components
                tagBase = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
            } else {
                // encode via codec -> DataResult<Tag>
                Object codec = COMPONENT_TYPE_CODEC.invoke(componentType);
                Object dataResult = CODEC_ENCODE.invoke(codec, NBT_OPS_INSTANCE, currentValue);
                tagBase = DATARESULT_GET_OR_THROW.invoke(dataResult);

                if (!NMS_TAG_CLASS.isInstance(tagBase)) {
                    // codec did not produce an NBT Tag (unexpected), bail out
                    return null;
                }
            }

            // cache raw tag (Compound / Int / String / List / etc)
            vanillaComponentCache.put(componentId, tagBase);

            // return wrapper (CompoundTag constructor should accept any Tag)
            return new CompoundTag(tagBase);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void applyVanillaCache() {
        for (Map.Entry<String, Object> entry : vanillaComponentCache.entrySet()) {
            try {
                String id = entry.getKey();
                Object nbtTag = entry.getValue();

                Object resLoc = RESOURCELOCATION_INIT.invoke(id);
                Object componentType = REGISTRY_GET.invoke(DATA_COMPONENT_REGISTRY, resLoc);
                if (componentType == null) continue;

                Object codec = COMPONENT_TYPE_CODEC.invoke(componentType);
                Object dataResult = CODEC_PARSE.invoke(codec, NBT_OPS_INSTANCE, nbtTag);
                Object valObject = DATARESULT_GET_OR_THROW.invoke(dataResult);

                ITEMSTACK_SET.invoke(nmsStack, componentType, valObject);

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    // --- Reflection helpers (loose matching for generics) ---
    private static Method findMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (m.getReturnType().isAssignableFrom(returnType)
                    && parameterMatch(m.getParameterTypes(), params)) {
                return m;
            }
        }
        throw new RuntimeException("Could not find method in " + holder.getSimpleName() + " returning " + returnType.getSimpleName());
    }

    private static Method findStaticMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())
                    && m.getReturnType().isAssignableFrom(returnType)
                    && parameterMatch(m.getParameterTypes(), params)) {
                return m;
            }
        }
        throw new RuntimeException("Could not find static method in " + holder.getSimpleName());
    }

    private static boolean parameterMatch(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) {
            if (expected[i] == Object.class) continue; // Loose matching for generics
            if (!actual[i].equals(expected[i]) && !actual[i].isAssignableFrom(expected[i])) return false;
        }
        return true;
    }
}