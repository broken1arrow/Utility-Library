package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.VanillaComponentTag;
import org.broken.arrow.library.itemcreator.utility.nms.api.ComponentEditor;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified component layer for modern item data handling in Minecraft.
 * <p>
 * This adapter provides a consistent interface for working with both:
 * <ul>
 *   <li><strong>CUSTOM_DATA</strong> — modern custom item data stored in the item’s CompoundTag/Component system.</li>
 *   <li><strong>Vanilla item components</strong> — optional editing of raw vanilla item properties.</li>
 * </ul>
 * <p>
 * Modern Minecraft versions split item data between custom components and vanilla components.
 * This class provides an abstraction for both without forcing the cost of heavy reflection initialization.
 * Only the minimal reflection required for handling custom data is loaded by default; all additional
 * reflection needed for vanilla component editing is deferred.
 *
 * <p><strong>Implementation details:</strong></p>
 * <ul>
 *   <li><code>ComponentItemDataSession</code> handles <strong>CUSTOM_DATA</strong> only.
 *       Its reflection usage is lightweight and initialized eagerly in the static initializer.</li>
 *   <li><code>VanillaComponentSession</code> is a <em>lazy-loaded</em> static nested class.
 *       Its static initializer runs only when {@link #enableVanillaTagEditor()} is invoked,
 *       meaning the heavier reflection cost of vanilla component support is incurred only if needed.</li>
 * </ul>
 */
public class ComponentAdapter implements NbtEditor {
    private static final Logging logger = new Logging(ComponentAccess.class);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    // Core handles (CUSTOM_DATA path)
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle ITEMSTACK_GET;
    private static final MethodHandle ITEMSTACK_SET;
    private static final MethodHandle CUSTOMDATA_OF;
    private static final MethodHandle CUSTOMDATA_COPYTAG;
    private static final MethodHandle NMS_COMPOUND_PUT;// CompoundTag.put(String, Tag)

    private static final Class<?> NMS_COMPOUND_CLASS;
    private static final Object CUSTOM_DATA_TYPE_KEY;
    private static final boolean READY;

    static {
        boolean ok = true;
        MethodHandle asNms = null;
        MethodHandle asBukkit = null;
        MethodHandle get = null;
        MethodHandle set = null;
        MethodHandle customOf = null;
        MethodHandle copyTag = null;
        MethodHandle put = null;
        Class<?> compoundClass = null;
        Object customKey = null;

        try {
            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> itemStack = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");
            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            compoundClass = Class.forName("net.minecraft.nbt.CompoundTag");

            asNms = LOOKUP.findStatic(craftItem, "asNMSCopy", MethodType.methodType(itemStack, ItemStack.class));
            asBukkit = LOOKUP.findStatic(craftItem, "asBukkitCopy", MethodType.methodType(ItemStack.class, itemStack));

            // find CUSTOM_DATA key (robust to obf)
            Field f = getCustomDataField(dataComponentsClass);
            customKey = f.get(null);

            // ItemStack#get(DataComponentType)  and #set(type, value)
            Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
            Method getM = findMethod(itemStack, Object.class, dataComponentTypeClass);
            Method setM = findMethod(itemStack, Object.class, dataComponentTypeClass, Object.class);
            get = LOOKUP.unreflect(getM);
            set = LOOKUP.unreflect(setM);

            // CustomData.of(CompoundTag)
            Method ofMethod = findStaticMethod(customDataClass, customDataClass, compoundClass);
            customOf = LOOKUP.unreflect(ofMethod);
            Method copyMethod = findMethod(customDataClass, compoundClass);
            copyTag = LOOKUP.unreflect(copyMethod);

            // CompoundTag.put
            put = getCompoundTagPut(compoundClass);

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException t) {
            logger.logError(t, () -> "Could not load ComponentItemDataSession reflections");
            ok = false;
        }
        AS_NMS_COPY = asNms;
        AS_BUKKIT_COPY = asBukkit;
        ITEMSTACK_GET = get;
        ITEMSTACK_SET = set;
        CUSTOMDATA_OF = customOf;
        CUSTOMDATA_COPYTAG = copyTag;
        NMS_COMPOUND_PUT = put;
        NMS_COMPOUND_CLASS = compoundClass;
        CUSTOM_DATA_TYPE_KEY = customKey;
        READY = ok;
    }

    private final ItemStack originalBukkit;
    private final Object nmsStack;
    private Object rootCustomDataCache; // mutable NMS CompoundTag (copy)
    private MethodHandle rootCustomDataContains;
    private VanillaComponentSession vanillaSession;

    /**
     * The instance for the new components.
     *
     * @param stack the bukkit itemStack to modify.
     */
    public ComponentAdapter(@Nonnull final ItemStack stack) {
        this.originalBukkit = stack;
        this.nmsStack = toNms(stack);
        this.rootCustomDataCache = loadRootFromItem(this.nmsStack);

        if (this.rootCustomDataCache == null) {
            rootCustomDataContains = null;
            return;
        }

        try {
            Method contains = rootCustomDataCache.getClass().getMethod("contains", String.class);
            rootCustomDataContains = LOOKUP.unreflect(contains);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            logger.logError(e, () -> "Could not find the contains method ComponentItemDataSession reflections");
        }
    }

    @Nonnull
    @Override
    public VanillaComponentTag enableVanillaTagEditor() {
        if (vanillaSession == null)
            vanillaSession = new VanillaComponentSession(nmsStack);
        return new VanillaComponentTag(this.rootCustomDataCache, vanillaSession);
    }

    // ---------------- NbtEditor API (custom-data only) ----------------
    @Override
    public boolean isReady() {
        return READY;
    }

    @Override
    public boolean hasTag() {
        return rootCustomDataCache != null;
    }

    @Override
    public boolean hasTag(@Nonnull String name) {
        if (rootCustomDataCache == null || rootCustomDataContains == null) return false;
        if (name.isEmpty()) return true;
        try {
            Object r = rootCustomDataContains.invoke(rootCustomDataCache, name);
            return r != null && (boolean) r;
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
        return getInternalCompound(name, true);
    }

    @Nullable
    @Override
    public CompoundTag getCompound() {
        return getCompound("");
    }

    @Nullable
    @Override
    public CompoundTag getCompound(@Nonnull String name) {
        return getInternalCompound(name, false);
    }

    @Nonnull
    @Override
    public ItemStack finalizeChanges() {
        try {
            // apply custom_data back
            applyCustomDataCache();
            if (vanillaSession != null)
                vanillaSession.apply();
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsStack);
        } catch (Throwable t) {
            logger.logError(t, () -> "Could not finalize your item and set the components, will return your original stack back.");
            return originalBukkit;
        }
    }

    // ---------------- internal custom-data helpers ----------------
    private Object toNms(@Nonnull ItemStack b) {
        try {
            return AS_NMS_COPY.invoke(b);
        } catch (Throwable t) {
            return null;
        }
    }

    private Object loadRootFromItem(@Nullable final Object nmsStack) {
        if (!READY || nmsStack == null) return null;
        try {
            Object customData = ITEMSTACK_GET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY);
            if (customData == null) return null;
            return CUSTOMDATA_COPYTAG.invoke(customData);
        } catch (Throwable t) {
            logger.logError(t, () -> "Could not load the custom tag from the item.");
            return null;
        }
    }

    private void applyCustomDataCache() throws Throwable {
        if (rootCustomDataCache == null) return;
        Object custom = CUSTOMDATA_OF.invoke(rootCustomDataCache);
        ITEMSTACK_SET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY, custom);
    }

    private CompoundTag getInternalCompound(String name, boolean create) {
        try {
            if (rootCustomDataCache == null) {
                if (!create) return null;
                rootCustomDataCache = NMS_COMPOUND_CLASS.getDeclaredConstructor().newInstance();
            }

            if (name.isEmpty()) return new CompoundTag(rootCustomDataCache);

            Method contains = rootCustomDataCache.getClass().getMethod("contains", String.class);
            boolean exists = (boolean) contains.invoke(rootCustomDataCache, name);
            Object nested;
            if (!exists) {
                if (!create) return null;
                nested = NMS_COMPOUND_CLASS.getDeclaredConstructor().newInstance();
                NMS_COMPOUND_PUT.invoke(rootCustomDataCache, name, nested);
            } else {
                Method getComp = rootCustomDataCache.getClass().getMethod("getCompound", String.class);
                nested = getComp.invoke(rootCustomDataCache, name);
            }
            return new CompoundTag(nested);
        } catch (Throwable t) {
            logger.logError(t, () -> "Could not set the custom component.");
            return null;
        }
    }

    /**
     * Handle the new way where minecraft using Codec and MinecraftKey, however in paper and newer spigot
     * it is using ResourceLocation and ResourceKey.
     *
     */
    public static final class VanillaComponentSession implements ComponentEditor {

        private final Object nmsStack;
        private final Map<String, Object> buffer = new HashMap<>();
        private final Map<String, Object> cachedRoot;

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady() {
            return ComponentAccess.isReady();
        }

        /**
         * Create vanilla
         *
         * @param nmsStack the nms itemStack
         */
        public VanillaComponentSession(@Nonnull final Object nmsStack) {
            this.nmsStack = nmsStack;
            this.cachedRoot = loadRootComponentMap(nmsStack);
        }

        /**
         * Set the int value to the raw compound.
         *
         * @param key   component key.
         * @param value a number
         */
        @Override
        public void setInt(@Nonnull final String key, final int value) {
            buffer.put(key, Integer.valueOf(value));
        }

        /**
         * Retrieve the int value from  the raw compound.
         *
         * @param key component key.
         */
        @Override
        public int getInt(@Nonnull final String key) {
            Object v = getRaw(key);
            if (v instanceof Number)
                return ((Number) v).intValue();
            return -1;
        }

        /**
         * Set the String value to the raw compound.
         *
         * @param key   component key.
         * @param value a string
         */
        @Override
        public void setString(@Nonnull final String key, final String value) {
            buffer.put(key, value);
        }

        /**
         * Retrieve the String value from the raw compound.
         *
         * @param key component key.
         */
        @Override
        @Nonnull
        public String getString(@Nonnull final String key) {
            Object v = getRaw(key);
            return (v != null) ? v.toString() : "";
        }

        @Override
        public void setByte(@Nonnull final String key, final byte value) {
            buffer.put(key, value);
        }

        @Override
        public byte getByte(@Nonnull final String key) {
            Object v = getRaw(key);
            if (v instanceof Byte)
                return (byte) v;
            return -1;
        }

        @Override
        public void setByteArray(@Nonnull final String key, final byte[] value) {
            buffer.put(key, value);
        }

        @Nonnull
        @Override
        public byte[] getByteArray(@Nonnull final String key) {
            Object v = getRaw(key);
            if (v instanceof byte[])
                return (byte[]) v;
            return new byte[0];
        }

        /**
         * Set the String value to the raw compound.
         *
         * @param key   component key.
         * @param value true or false
         */
        @Override
        public void setBoolean(@Nonnull final String key, boolean value) {
            buffer.put(key, value);
        }

        /**
         * Retrieve the boolean value from the raw compound.
         *
         * @param key component key.
         * @return Returns {@code true} if value exists and set to true. Check
         * with {@link #hasKey(String)} to make sure the key is set.
         */
        @Override
        public boolean getBoolean(@Nonnull final String key) {
            Object v = getRaw(key);
            if (v instanceof Boolean)
                return (boolean) v;
            return false;
        }

        @Override
        public void setShort(@Nonnull final String key, final short value) {
            buffer.put(key, value);
        }

        @Override
        public short getShort(@Nonnull final String key) {
            Object v = getRaw(key);
            if (v instanceof Short)
                return (short) v;
            return -1;
        }


        @Nonnull
        @Override
        public Object getHandle() {
            return "";
        }

        /**
         * Checks whether a component key exists in the underlying NMS ItemStack.
         *
         * @param key component key
         * @return true if the key exists
         */
        @Override
        public boolean hasKey(@Nonnull String key) {
            Object type = getRaw(key);
            return type != null;
        }

        /**
         * Remove a component from the "components" compound
         *
         * @param key component key.
         */
        public void remove(@Nonnull String key) {
            Object type = ComponentAccess.resolve(key);
            ComponentAccess.removeComponent(nmsStack, type);
            buffer.remove(key);
        }

        /**
         * Get the raw set value
         *
         * @param key component key
         * @return the component object.
         */
        @Nullable
        private Object getRaw(String key) {
            // Pending writes override everything
            if (buffer.containsKey(key))
                return buffer.get(key);

            // Cached root snapshot
            Object v = cachedRoot.get(key);
            if (v != null)
                return v;

            // Slow path: read from NMS once
            Object type = ComponentAccess.resolve(key);
            v = ComponentAccess.getComponent(nmsStack, type);
            if (v != null) {
                cachedRoot.put(key, v);
            }
            return v;
        }

        /**
         * Apply the set data.
         */
        public void apply() {
            if (buffer.isEmpty()) return;

            try {
                for (Map.Entry<String, Object> e : buffer.entrySet()) {

                    String key = e.getKey();
                    Object value = e.getValue();

                    // Resolve NMS component type
                    Object type = ComponentAccess.resolve(key);
                    // Write into NMS ItemStack
                    ComponentAccess.setComponent(nmsStack, type, value);
                }
            } catch (Exception t) {
                logger.logError(t, () -> "Could not set the vanilla tags.");
            }

            buffer.clear();
        }

        @SuppressWarnings("unchecked")
        private Map<Object, Object> loadRootComponentMapOld(Object nmsStack) {
            try {
                Field f = nmsStack.getClass().getDeclaredField("components");
                f.setAccessible(true);

                Object container = f.get(nmsStack); // vanilla ComponentMap
                return new HashMap<>((Map<Object, Object>) container);
            } catch (Exception ex) {
                logger.logError(ex, () -> "Could not read vanilla component root map");
                return new HashMap<>();
            }
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> loadRootComponentMap(Object nmsStack) {
            Map<String, Object> out = new HashMap<>();

            try {
                Field f = nmsStack.getClass().getDeclaredField("components");
                f.setAccessible(true);
                Map<Object, Object> container = (Map<Object, Object>) f.get(nmsStack);

                for (Map.Entry<Object, Object> entry : container.entrySet()) {
                    String id = ComponentAccess.getId(entry.getKey()); // e.g. "minecraft:damage"
                    out.put(id, entry.getValue());
                }
            } catch (Exception ex) {
                logger.logError(ex, () -> "Could not read vanilla component root map");
            }
            return out;
        }
    }

    public static final class ComponentAccess {
        private static final Logging logger = new Logging(ComponentAccess.class);
        private static final MethodHandle SET;
        private static final MethodHandle GET;
        private static final MethodHandle REMOVE;

        static final ComponentResolver COMPONENT_RESOLVER;
        private static boolean ready = true;

        static {
            MethodHandle set = null;
            MethodHandle get = null;
            MethodHandle remove = null;
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                final Class<?> itemstackClass = Class.forName("net.minecraft.world.item.ItemStack");
                final Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");

                final Method mSet = itemstackClass.getMethod("set", dataComponentTypeClass, Object.class);
                final Method mGet = itemstackClass.getMethod("get", dataComponentTypeClass);
                final Method mRemove = itemstackClass.getMethod("remove", dataComponentTypeClass);

                set = lookup.unreflect(mSet);
                get = lookup.unreflect(mGet);
                remove = lookup.unreflect(mRemove);

            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException t) {
                ready = false;
                logger.logError(t, () -> "Failed to initialize ComponentAccess");
            }
            SET = set;
            GET = get;
            REMOVE = remove;

            if (ready)
                COMPONENT_RESOLVER = new ComponentResolver();
            else
                COMPONENT_RESOLVER = null;
        }

        private ComponentAccess() {
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady() {
            return ready && COMPONENT_RESOLVER != null && COMPONENT_RESOLVER.isReady();
        }

        /**
         * Resolve a component type using its resource location ("minecraft:damage", "minecraft:unbreaking").
         *
         * @param key the component key string
         * @return the DataComponentType instance.
         */
        public static Object resolve(String key) {
            if (COMPONENT_RESOLVER == null) return "";

            return COMPONENT_RESOLVER.resolve(key);
        }

        /**
         * Set the component
         *
         * @param nmsStack the nms itemStack
         * @param type     the type of data.
         * @param value    the value.
         */
        public static void setComponent(Object nmsStack, Object type, Object value) {
            try {
                SET.invoke(nmsStack, type, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set the component.");
            }
        }

        /**
         * Retrieve the component
         *
         * @param nmsStack the nms itemStack
         * @param type     the type of data.
         * @return the raw set object component tag.
         */
        @Nullable
        public static Object getComponent(Object nmsStack, Object type) {
            try {
                return GET.invoke(nmsStack, type);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to get the component.");
            }
            return null;
        }

        /**
         * Remove the component
         *
         * @param nmsStack the nms itemStack
         * @param type     the type of data.
         */
        public static void removeComponent(Object nmsStack, Object type) {
            try {
                REMOVE.invoke(nmsStack, type);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to remove the component.");
            }
        }

        /**
         * Get the id for the component.
         *
         * @param componentKey Get the component key.
         * @return returns the name.
         */
        public static String getId(Object componentKey) {
            try {
                Object keyObj = null;
                // Find field whose type ends with "ComponentTypeKey"
                for (Field f : componentKey.getClass().getDeclaredFields()) {
                    if (f.getType().getSimpleName().contains("ComponentTypeKey")) {
                        f.setAccessible(true);
                        keyObj = f.get(componentKey);
                        break;
                    }
                }

                if (keyObj == null) return null;

                Object locObj = getResourceKey(keyObj);
                if (locObj == null) return null;
                // toString gives namespace:path
                return locObj.toString();
            } catch (Throwable t) {
                return null;
            }
        }

        private static Object getResourceKey(final Object keyObj) throws IllegalAccessException {
            Object locObj = null;
            // Find field whose type ends with "MinecraftKey" or "ResourceLocation"
            for (Field f : keyObj.getClass().getDeclaredFields()) {
                String n = f.getType().getSimpleName();
                if (n.contains("MinecraftKey") || n.contains("ResourceLocation")) {
                    f.setAccessible(true);
                    locObj = f.get(keyObj);
                    break;
                }
            }
            return locObj;
        }
    }

    static class ComponentResolver {
        private static final Logging logger = new Logging(ComponentResolver.class);
        private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();
        private static final Class<?> RESOURCE_LOCATION_CLASS;
        private static final Object DATA_COMPONENT_REGISTRY;
        private static final Method CREATE_REGISTRY_KEY_METHOD;
        private static final Method CREATE_METHOD;
        private static final Method REGISTRY_GET_METHOD;
        private static final Object DATA_COMPONENT_REGISTRY_KEY;
        private static boolean ready = true;

        static {
            Object dataComponentRegistry = null;
            Class<?> resourceLocationClass = null;
            Method createRegistryKeyMethod = null;
            Method createMethod = null;
            Method registryGetMethod = null;
            Object registryKey = null;
            try {
                resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
                Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");

                Class<?> builtInRegistries = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
                Field f = builtInRegistries.getField("DATA_COMPONENT_TYPE");
                dataComponentRegistry = f.get(null);

                createRegistryKeyMethod = resourceKeyClass.getMethod("createRegistryKey", resourceLocationClass);
                createMethod = resourceKeyClass.getMethod("create", resourceKeyClass, resourceLocationClass);
                registryGetMethod = dataComponentRegistry.getClass().getMethod("get", resourceKeyClass);

                Field keyField = dataComponentRegistry.getClass().getDeclaredField("key");
                keyField.setAccessible(true);
                registryKey = keyField.get(dataComponentRegistry);
            } catch (Exception ex) {
                ready = false;
                logger.logError(ex, () -> "Failed to load DataComponent registry");
            }
            DATA_COMPONENT_REGISTRY = dataComponentRegistry;
            RESOURCE_LOCATION_CLASS = resourceLocationClass;
            CREATE_REGISTRY_KEY_METHOD = createRegistryKeyMethod;
            CREATE_METHOD = createMethod;
            REGISTRY_GET_METHOD = registryGetMethod;
            DATA_COMPONENT_REGISTRY_KEY = registryKey;
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        private boolean isReady() {
            return ready;
        }

        /**
         * Resolve a component type using its resource location ("minecraft:damage", "minecraft:unbreaking").
         * Uses only invoke / newInstance consistently.
         *
         * @param key the component key string
         * @return the DataComponentType instance
         */
        private Object resolve(String key) {
            return CACHE.computeIfAbsent(key, ComponentResolver::resolveInternal);
        }

        @Nullable
        private static Object resolveInternal(String key) {
            try {
                final String keyChecked = rl(key);

                // Create ResourceLocation for the component
                final Object componentRL = RESOURCE_LOCATION_CLASS
                        .getConstructor(String.class).newInstance(keyChecked);
                // Create ResourceKey<DataComponentType> for this component
                final Object resourceKey = CREATE_METHOD.invoke(
                        null,
                        DATA_COMPONENT_REGISTRY_KEY,  // <--- THIS IS THE IMPORTANT FIX
                        componentRL
                );
                // Fetch component type from registry
                return REGISTRY_GET_METHOD.invoke(DATA_COMPONENT_REGISTRY, resourceKey);
            } catch (Exception ex) {
                logger.logError(ex, () -> "Failed to resolve component: " + key);
            }
            return null;
        }

        // Construct a ResourceLocation namespace:path
        private static String rl(String key) {
            int i = key.indexOf(':');
            if (i == -1)
                return "minecraft:" + key;
            return key;
        }
    }

    // -------------------- reflection helpers --------------------
    private static Method findMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (m.getReturnType().isAssignableFrom(returnType) && parameterMatch(m.getParameterTypes(), params))
                return m;
        }
        throw new Validate.ValidateExceptions("Could not find method in " + holder.getName() + " returning " + returnType.getName());
    }

    private static Method findStaticMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getReturnType().isAssignableFrom(returnType) && parameterMatch(m.getParameterTypes(), params))
                return m;
        }
        throw new Validate.ValidateExceptions("Could not find static method in " + holder.getName());
    }

    private static Method findMethodByName(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && parameterTypesMatch(m.getParameterTypes(), params)) {
                    return m;
                }
            }
            return null;
        }
    }

    private static boolean parameterMatch(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) {
            if (expected[i] == Object.class) continue;
            if (!actual[i].equals(expected[i]) && !actual[i].isAssignableFrom(expected[i])) return false;
        }
        return true;
    }

    private static boolean parameterTypesMatch(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) if (!actual[i].isAssignableFrom(expected[i])) return false;
        return true;
    }

    private static MethodHandle getCompoundTagPut(@Nonnull final Class<?> compoundClass) throws IllegalAccessException, ClassNotFoundException {
        // CompoundTag.put
        final Class<?> tagInterface = Class.forName("net.minecraft.nbt.Tag");
        try {
            return LOOKUP.findVirtual(compoundClass, "put", MethodType.methodType(tagInterface, String.class, tagInterface));
        } catch (NoSuchMethodException | IllegalAccessException t) {
            // fallback - find a method named put with (String, Tag)
            Method m = findMethodByName(compoundClass, "put", String.class, tagInterface);
            return LOOKUP.unreflect(m);
        }
    }

    private static Field getCustomDataField(@Nonnull final Class<?> dataComponentsClass) throws NoSuchFieldException {
        Field f;
        try {
            f = dataComponentsClass.getField("CUSTOM_DATA");
        } catch (NoSuchFieldException e) {
            f = dataComponentsClass.getField("b");
        }
        return f;
    }
}
