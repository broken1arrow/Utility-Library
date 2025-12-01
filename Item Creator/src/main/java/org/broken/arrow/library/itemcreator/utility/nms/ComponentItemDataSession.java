package org.broken.arrow.library.itemcreator.utility.nms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ComponentItemDataSession implements NbtEditor { // Implements NbtEditor (Assuming interface exists)
    private static final Logging log = new Logging(ComponentItemDataSession.class);

    // --- Core Reflection Handles ---
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle ITEMSTACK_GET;
    private static final MethodHandle ITEMSTACK_SET;
    private static final MethodHandle CUSTOMDATA_OF;
    private static final MethodHandle CUSTOMDATA_COPYTAG;
    private static MethodHandle SET_NESTED_COMPOUND;

    // --- Serialization Reflection Handles (New for Vanilla Support) ---
    private static final MethodHandle REGISTRY_GET; // BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation)
    private static final MethodHandle RESOURCELOCATION_INIT; // new ResourceLocation(String)
    private static final MethodHandle COMPONENT_TYPE_CODEC; // DataComponentType.codec()
    private static final MethodHandle CODEC_ENCODE; // Codec.encodeStart(Ops, Object)
    private static final MethodHandle CODEC_PARSE;  // Codec.parse(Ops, Object)
    private static final MethodHandle DATARESULT_GET_OR_THROW; // DataResult.getOrThrow()
    private static final Object NBT_OPS_INSTANCE; // NbtOps.INSTANCE
    private static final Object DATA_COMPONENT_REGISTRY; // BuiltInRegistries.DATA_COMPONENT_TYPE

    private static final Class<?> NMS_COMPOUND_TAG_CLASS;
    private static final Object CUSTOM_DATA_TYPE_KEY;
    private static boolean ready = true;

    // --- Session State ---
    private final ItemStack originalBukkit;
    private final Object nmsStack;

    // Cache for your plugin's internal data (inside custom_data)
    private Object rootCustomDataCache;

    // Cache for Vanilla components (converted to NBT for editing)
    // Map<String (minecraft:id), CompoundTag (NMS)>
    private final Map<String, Object> vanillaComponentCache = new HashMap<>();

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            // 1. Classes
            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsItem = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");
            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
            NMS_COMPOUND_TAG_CLASS = Class.forName("net.minecraft.nbt.CompoundTag");
            Class<?> nmsTagClass = Class.forName("net.minecraft.nbt.Tag");

            // New Classes for Vanilla Translation
            Class<?> builtInRegistries = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Class<?> registriesClass = Class.forName("net.minecraft.core.registries.Registries");
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Class<?> codecClass = Class.forName("com.mojang.serialization.Codec");
            Class<?> dynamicOpsClass = Class.forName("com.mojang.serialization.DynamicOps");
            Class<?> dataResultClass = Class.forName("com.mojang.serialization.DataResult");
            Class<?> nbtOpsClass = Class.forName("net.minecraft.nbt.NbtOps");

            // 2. Setup Basic Handles (Same as before)
            AS_NMS_COPY = lookup.findStatic(craftItem, "asNMSCopy", MethodType.methodType(nmsItem, ItemStack.class));
            AS_BUKKIT_COPY = lookup.findStatic(craftItem, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItem));

            // Obfuscation safe lookup for CUSTOM_DATA
            Field dataField = null;
            try {
                // Try Mojang mapped name first (Paper)
                dataField = dataComponentsClass.getField("CUSTOM_DATA");
            } catch (NoSuchFieldException e) {
                // 'b' is usually CustomData
                dataField = dataComponentsClass.getField("b");
            }
            CUSTOM_DATA_TYPE_KEY = dataField.get(null);

            Method getMethod = findMethod(nmsItem, Object.class, dataComponentTypeClass);
            ITEMSTACK_GET = lookup.unreflect(getMethod);

            Method setMethod = findMethod(nmsItem, Object.class, dataComponentTypeClass, Object.class);
            ITEMSTACK_SET = lookup.unreflect(setMethod);

            Method ofMethod = findStaticMethod(customDataClass, customDataClass, NMS_COMPOUND_TAG_CLASS);
            CUSTOMDATA_OF = lookup.unreflect(ofMethod);

            Method copyTagMethod = findMethod(customDataClass, NMS_COMPOUND_TAG_CLASS);
            CUSTOMDATA_COPYTAG = lookup.unreflect(copyTagMethod);

            try {
                SET_NESTED_COMPOUND = lookup.findVirtual(NMS_COMPOUND_TAG_CLASS, "put", MethodType.methodType(nmsTagClass, String.class, nmsTagClass));
            } catch (Exception e) {
                SET_NESTED_COMPOUND = lookup.unreflect(findMethod(NMS_COMPOUND_TAG_CLASS, nmsTagClass, String.class, nmsTagClass));
            }

            // 3. Setup Complex Handles for Vanilla Component Support

            // NbtOps.INSTANCE
            NBT_OPS_INSTANCE = nbtOpsClass.getField("INSTANCE").get(null);

            // BuiltInRegistries.DATA_COMPONENT_TYPE
            //DATA_COMPONENT_REGISTRY = builtInRegistries.getField("DATA_COMPONENT_TYPE").get(null);
            DATA_COMPONENT_REGISTRY =  builtInRegistries.getField("DATA_COMPONENT_TYPE").get(null);

            Field keyField = registriesClass.getField("DATA_COMPONENT_TYPE");
            Object DATA_COMPONENT_TYPE_KEY = keyField.get(null);
            // new ResourceLocation("minecraft:foo")
            RESOURCELOCATION_INIT = lookup.findConstructor(resourceLocationClass, MethodType.methodType(void.class, String.class));

            // Registry.get(ResourceLocation) -> DataComponentType
           // REGISTRY_GET = lookup.findVirtual( resourceLocationClass, "get", MethodType.methodType(Object.class, resourceLocationClass));
            //System.out.println("REGISTRY_GET = " + REGISTRY_GET);
            Constructor<?> itemStackCtor = null;

            for (Constructor<?> ctor : nmsItem.getDeclaredConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();

                // Match: (Holder, int)
                if (params.length == 2 &&
                        params[0].getName().equals("net.minecraft.core.Holder") &&
                        params[1] == int.class) {

                    itemStackCtor = ctor;
                    itemStackCtor.setAccessible(true);
                    break;
                }
            }
            if (itemStackCtor == null)
                throw new IllegalStateException("No valid ItemStack constructor found!");
// Create a dummy ItemStack (Items.AIR)
            Class<?> itemsClass = Class.forName("net.minecraft.world.item.Items");
            Object AIR_ITEM = itemsClass.getField("AIR").get(null);
// AIR builtInRegistryHolder()
            Method holderMethod = AIR_ITEM.getClass().getMethod("builtInRegistryHolder");
            Object AIR_HOLDER = holderMethod.invoke(AIR_ITEM);
// Create stack
            Object airItemStack = itemStackCtor.newInstance(AIR_HOLDER, 1);
// Step 2: Resolve lookup provider method
            Method getLookup;
            try {
                getLookup = nmsItem.getMethod("getComponents");
            } catch (NoSuchMethodException ex) {
                getLookup = nmsItem.getMethod("holderLookup");
            }

// Extract HolderLookup.Provider
            Object lookupProvider = getLookup.invoke( airItemStack);

// Step 3: registryOrThrow(ResourceKey)
            Method registryOrThrow = lookupProvider.getClass().getMethod("registryOrThrow", DATA_COMPONENT_TYPE_KEY.getClass());
            Object dataComponentRegistry = registryOrThrow.invoke(lookupProvider, DATA_COMPONENT_TYPE_KEY);

// Step 4: Setup REGISTRY_GET
            REGISTRY_GET = MethodHandles.lookup().findVirtual(
                    dataComponentRegistry.getClass(),
                    "get",
                    MethodType.methodType(Object.class, resourceLocationClass)
            );

            // DataComponentType.codec() -> Codec
            COMPONENT_TYPE_CODEC = lookup.findVirtual(dataComponentTypeClass, "codec", MethodType.methodType(codecClass));

            // Codec.encodeStart(Ops, Object) -> DataResult
            CODEC_ENCODE = lookup.findVirtual(codecClass, "encodeStart", MethodType.methodType(dataResultClass, dynamicOpsClass, Object.class));

            // Codec.parse(Ops, Object) -> DataResult
            CODEC_PARSE = lookup.findVirtual(codecClass, "parse", MethodType.methodType(dataResultClass, dynamicOpsClass, Object.class));

            // DataResult.getOrThrow()
            DATARESULT_GET_OR_THROW = lookup.findVirtual(dataResultClass, "getOrThrow", MethodType.methodType(Object.class));

        } catch (Throwable t) {
            ready = false;
            t.printStackTrace();
            throw new RuntimeException("Failed to initialize NMS 1.20.5+ reflection", t);
        }
    }

    /**
     * Create the item
     *
     * @param bukkitItem the item.
     */
    public ComponentItemDataSession(ItemStack bukkitItem) {
        this.originalBukkit = bukkitItem;
        this.nmsStack = toNmsItemStack(bukkitItem);
        // Load the tag immediately into cache
        this.rootCustomDataCache = loadRootFromItem();
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean hasTag() {
        // We check the cache, as that represents the current session state
        return rootCustomDataCache != null;
    }

    @Override
    public boolean hasTag(@Nonnull String name) {
        if (rootCustomDataCache == null) return false;
        if (name.isEmpty()) return true;

        try {
            Object nested = rootCustomDataCache.getClass()
                    .getMethod("contains", String.class)
                    .invoke(rootCustomDataCache, name);
            return nested != null && (boolean) nested;
        } catch (Throwable t) {
            // logger.logError...
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
            // 1. Save Custom Data
            applyCustomDataCache();

            // 2. Save Vanilla Components (Decode NBT -> Object)
            applyVanillaCache();

            // Convert NMS back to Bukkit
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsStack);
        } catch (Throwable t) {
            // logger.logError...
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
     * and unwraps it into a mutable NMS CompoundTag.
     *
     * @return Returns the component set in the itemStack.
     */
    private Object loadRootFromItem() {
        try {
            // 1. Get CustomData component
            Object customData = ITEMSTACK_GET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY);
            if (customData == null) return null;

            // 2. Unwrap to CompoundTag (copyTag returns a generic CompoundTag)
            return CUSTOMDATA_COPYTAG.invoke(customData);
        } catch (Throwable t) {
            // logger...
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Wraps the cached NMS CompoundTag into a CustomData component
     * and sets it onto the NMS stack.
     */
    private void applyCacheToItem() {
        if (rootCustomDataCache == null) return;
        try {
            Object customDataInstance = CUSTOMDATA_OF.invoke(rootCustomDataCache);
            ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY, customDataInstance);
        } catch (Throwable t) {
            // logger...
            t.printStackTrace();
        }
    }

    private CompoundTag getCompoundLogic(@Nonnull String name, boolean create) {
        // STRATEGY: IF name contains ":", treat as Vanilla Component. Else, treat as internal NBT.
        if (name.contains("minecraft:")) {
            return getVanillaComponentAsNbt(name, create);
        } else {
            return getInternalCustomData(name, create);
        }
    }

    /**
     * Handles standard logic for data inside "minecraft:custom_data"
     *
     * @param name   the name for the compound.
     * @param create if it shall create new compound if it doesn't exist.
     * @return Returns the {@link CompoundTag} instance.
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
     * Handles translation of Vanilla Components (Objects) -> NBT
     *
     * @param componentId the id for the compound.
     * @param create      if it shall create new compound if it doesn't exist.
     * @return Returns the {@link CompoundTag} instance.
     */
    private CompoundTag getVanillaComponentAsNbt(String componentId, boolean create) {
        // 1. Check cache first (so we don't overwrite edits)
        if (vanillaComponentCache.containsKey(componentId)) {
            return new CompoundTag(vanillaComponentCache.get(componentId));
        }

        try {
            // 2. Resolve Type from Registry
            Object resLoc = RESOURCELOCATION_INIT.invoke(componentId);
            // EXECUTE THE DIRECT LOOKUP (REGISTRY_GET now returns Object or null)
            Object componentType = REGISTRY_GET.invoke(DATA_COMPONENT_REGISTRY, resLoc);

            if (componentType == null) {
                // Component doesn't exist in Minecraft (e.g. invalid name)
                return null;
            }

            // 3. Get Current Value from ItemStack
            Object currentComponentValue = ITEMSTACK_GET.invoke(nmsStack, componentType);

            Object nbtTag;
            if (currentComponentValue == null) {
                if (!create) return null;
                // If creating new, we start with empty compound
                nbtTag = NMS_COMPOUND_TAG_CLASS.getDeclaredConstructor().newInstance();
            } else {
                // 4. Encode Object -> NBT using Codec
                Object codec = COMPONENT_TYPE_CODEC.invoke(componentType);
                Object dataResult = CODEC_ENCODE.invoke(codec, NBT_OPS_INSTANCE, currentComponentValue);
                // Unwrap result
                Object tagBase = DATARESULT_GET_OR_THROW.invoke(dataResult);

                // Ensure it is a CompoundTag (Some components are just Ints or Strings, this only supports Compound based ones)
                if (NMS_COMPOUND_TAG_CLASS.isInstance(tagBase)) {
                    nbtTag = tagBase;
                } else {
                    // This is a primitive component (like MaxStackSize = int), cannot wrap in CompoundTag wrapper.
                    // You might want to log this.
                    return null;
                }
            }

            // 5. Cache it
            vanillaComponentCache.put(componentId, nbtTag);
            return new CompoundTag(nbtTag);

        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void applyCustomDataCache() throws Throwable {
        if (rootCustomDataCache == null) return;
        Object customDataInstance = CUSTOMDATA_OF.invoke(rootCustomDataCache);
        ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY, customDataInstance);
    }

    private void applyVanillaCache() {
        for (Map.Entry<String, Object> entry : vanillaComponentCache.entrySet()) {
            try {
                String id = entry.getKey();
                Object nbtTag = entry.getValue();

                // 1. Resolve Type
                Object resLoc = RESOURCELOCATION_INIT.invoke(id);
                Object componentType = REGISTRY_GET.invoke(DATA_COMPONENT_REGISTRY, resLoc);
                if (componentType == null) continue;

                // 2. Decode NBT -> Object using Codec
                Object codec = COMPONENT_TYPE_CODEC.invoke(componentType);
                Object dataResult = CODEC_PARSE.invoke(codec, NBT_OPS_INSTANCE, nbtTag);
                Object valObject = DATARESULT_GET_OR_THROW.invoke(dataResult);

                // 3. Set on ItemStack
                ITEMSTACK_SET.invoke(nmsStack, componentType, valObject);

            } catch (Throwable t) {
                // System.out.println("Failed to save vanilla component: " + entry.getKey());
                t.printStackTrace();
            }
        }
    }

    // --- Helper to find methods by Signature (Type Matching) ---
    private static Method findMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        System.out.println("########## for ###############");
        for (Method m : holder.getMethods()) {
            if (m.getReturnType().isAssignableFrom(returnType)
                    && parameterMatch(m.getParameterTypes(), params)) {
                System.out.println("findMethod " + m.getName());
                System.out.println("returnType " + returnType);
                return m;
            }
        }
        throw new RuntimeException("Could not find method in " + holder.getSimpleName() + " returning " + returnType.getSimpleName());
    }

    private static Method findStaticMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        System.out.println("########## for static ###############");
        for (Method m : holder.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())
                    && m.getReturnType().isAssignableFrom(returnType)
                    && parameterMatch(m.getParameterTypes(), params)) {
                System.out.println("Static m " + m.getName());
                System.out.println("returnType " + returnType);
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