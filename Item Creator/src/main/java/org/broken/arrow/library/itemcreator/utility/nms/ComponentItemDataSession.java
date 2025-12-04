package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.VanillaCompoundTag;
import org.broken.arrow.library.itemcreator.utility.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.logging.Logging;
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

/**
 * Unified legacy-friendly session for item custom data (CUSTOM_DATA) + optional nested vanilla component helper.
 * <p>
 * - ComponentItemDataSession handles just CUSTOM_DATA (cheap reflection in static init).
 * - VanillaComponentSession is a lazy-loaded static nested class: its static initializer only runs when
 * you reference VanillaComponentSession, so you pay the extra reflection cost only if you need vanilla components.
 * <p>
 */
public class ComponentItemDataSession implements NbtEditor {
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
            Field f;
            try {
                f = dataComponentsClass.getField("CUSTOM_DATA");
            } catch (NoSuchFieldException e) {
                f = dataComponentsClass.getField("b");
            }
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
            try {
                put = LOOKUP.findVirtual(compoundClass, "put", MethodType.methodType(Class.forName("net.minecraft.nbt.Tag"), String.class, Class.forName("net.minecraft.nbt.Tag")));
            } catch (Throwable t) {
                // fallback - find a method named put with (String, Tag)
                Method m = findMethodByName(compoundClass, "put", String.class, Class.forName("net.minecraft.nbt.Tag"));
                put = LOOKUP.unreflect(m);
            }

        } catch (Throwable t) {
            logger.logError(t,()->"Could not load ComponentItemDataSession reflections");
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
    private VanillaComponentSession vanillaSession;

    /**
     * The instance for the new components.
     *
     * @param stack the bukkit itemStack to modify.
     */
    public ComponentItemDataSession(@Nonnull ItemStack stack) {
        this.originalBukkit = stack;
        this.nmsStack = toNms(stack);
        this.rootCustomDataCache = loadRootFromItem();
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
        if (rootCustomDataCache == null) return false;
        if (name.isEmpty()) return true;
        try {
            Method m = rootCustomDataCache.getClass().getMethod("contains", String.class);
            Object r = m.invoke(rootCustomDataCache, name);
            return r != null && (boolean) r;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Enables vanilla tag editing and returns a tag wrapper for the user.
     *
     * @return the vanilla option to set the values direct to the item.
     */
    public VanillaCompoundTag enableVanillaTagEditor() {
        if (vanillaSession == null)
            vanillaSession = new VanillaComponentSession(nmsStack);

        return new VanillaCompoundTag(this.rootCustomDataCache, vanillaSession);
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
            t.printStackTrace();
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

    private Object loadRootFromItem() {
        if (!READY) return null;
        try {
            Object customData = ITEMSTACK_GET.invoke(nmsStack, CUSTOM_DATA_TYPE_KEY);
            if (customData == null) return null;
            return CUSTOMDATA_COPYTAG.invoke(customData);
        } catch (Throwable t) {
            t.printStackTrace();
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
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Handle the new way where minecraft using Codec and MinecraftKey, however in paper and newer spigot
     * it is using ResourceLocation and ResourceKey.
     *
     */
    public static final class VanillaComponentSession implements CompoundEditor {

        private final Object nmsStack;
        private final Map<String, Object> buffer = new HashMap<>();

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady(){
            return ComponentAccess.isReady();
        }

        /**
         * Create vanilla
         *
         * @param nmsStack the nms itemStack
         */
        public VanillaComponentSession(@Nonnull final Object nmsStack) {
            this.nmsStack = nmsStack;
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
            Object v = get(key);
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
            Object v = get(key);
            return (v != null) ? v.toString() : "";
        }

        @Override
        public void setByte(@Nonnull final String key, final byte value) {
            buffer.put(key, value);
        }

        @Override
        public byte getByte(@Nonnull final String key) {
            Object v = get(key);
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
            Object v = get(key);
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
            Object v = get(key);
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
            Object v = get(key);
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
            Object type = ComponentAccess.resolve(key);
            return ComponentAccess.getComponent(nmsStack, type) != null;
        }

        /**
         * Get the raw set value
         *
         * @param key component key
         * @return the object.
         */
        private Object getRaw(String key) {
            Object type = ComponentAccess.resolve(key);
            return ComponentAccess.getComponent(nmsStack, type);
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
            } catch (Throwable t) {
                t.printStackTrace();
            }

            buffer.clear();
        }

        public Object get(String key) {
            Object type = ComponentAccess.resolve(key);
            return ComponentAccess.getComponent(nmsStack, type);
        }
    }


    public static final class ComponentAccess {
        private static final Logging logger = new Logging(ComponentAccess.class);
        private static final MethodHandle SET;
        private static final MethodHandle GET;
        private static final MethodHandle REMOVE;

        static final ComponentResolver COMPONENT_RESOLVER;
        private static boolean READY = true;

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

            } catch (Throwable t) {
                READY = false;
                logger.logError(t, () -> "Failed to initialize ComponentAccess");
            }
            SET = set;
            GET = get;
            REMOVE = remove;

            if (READY)
                COMPONENT_RESOLVER = new ComponentResolver();
            else
                COMPONENT_RESOLVER = null;
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady() {
            return READY && COMPONENT_RESOLVER != null && COMPONENT_RESOLVER.isReady();
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
                throw new RuntimeException(e);
            }
        }

        /**
         * Retrieve the component
         *
         * @param nmsStack the nms itemStack
         * @param type     the type of data.
         * @return the raw set object component tag.
         */
        public static Object getComponent(Object nmsStack, Object type) {
            try {
                return GET.invoke(nmsStack, type);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
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
                throw new RuntimeException(e);
            }
        }

    }

    static class ComponentResolver {
        private static final Logging logger = new Logging(ComponentResolver.class);
        private static final Class<?> RESOURCE_LOCATION_CLASS;
        private static final Object DATA_COMPONENT_REGISTRY;
        private static final Method CREATE_REGISTRY_KEY_METHOD;
        private static final Method CREATE_METHOD;
        private static final Method REGISTRY_GET_METHOD;
        private static boolean READY = true;

        static {
            Object dataComponentRegistry = null;
            Class<?> resourceLocationClass = null;
            Method createRegistryKeyMethod = null;
            Method createMethod = null;
            Method registryGetMethod = null;
            try {
                resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
                Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");

                Class<?> BUILT_IN_REGISTRIES = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
                Field f = BUILT_IN_REGISTRIES.getField("DATA_COMPONENT_TYPE");
                dataComponentRegistry = f.get(null);

                createRegistryKeyMethod = resourceKeyClass.getMethod("createRegistryKey", resourceLocationClass);
                createMethod = resourceKeyClass.getMethod("create", resourceKeyClass, resourceLocationClass);

                registryGetMethod = dataComponentRegistry.getClass().getMethod("get", resourceKeyClass);
            } catch (Exception ex) {
                READY = false;
                logger.logError(ex, () -> "Failed to load DataComponent registry");
            }
            DATA_COMPONENT_REGISTRY = dataComponentRegistry;
            RESOURCE_LOCATION_CLASS = resourceLocationClass;
            CREATE_REGISTRY_KEY_METHOD = createRegistryKeyMethod;
            CREATE_METHOD = createMethod;
            REGISTRY_GET_METHOD = registryGetMethod;
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public boolean isReady() {
            return READY;
        }

        /**
         * Resolve a component type using its resource location ("minecraft:damage", "minecraft:unbreaking").
         * Uses only invoke / newInstance consistently.
         *
         * @param key the component key string
         * @return the DataComponentType instance
         */
        public Object resolve(String key) {
            try {
                // Create ResourceLocation instance
                final Object rl = RESOURCE_LOCATION_CLASS.getConstructor(String.class).newInstance(key);
                // Create ResourceKey for the DATA_COMPONENT_TYPE registry
                final Object registryKeyName = RESOURCE_LOCATION_CLASS.getConstructor(String.class).newInstance("minecraft:data_component_type");
                final Object registryKey = CREATE_REGISTRY_KEY_METHOD.invoke(null, registryKeyName);
                final Object resourceKey = CREATE_METHOD.invoke(null, registryKey, rl);

                return REGISTRY_GET_METHOD.invoke(DATA_COMPONENT_REGISTRY, resourceKey);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to resolve component: " + key, ex);
            }
        }
    }

    // -------------------- reflection helpers --------------------
    private static Method findMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (m.getReturnType().isAssignableFrom(returnType) && parameterMatch(m.getParameterTypes(), params))
                return m;
        }
        throw new RuntimeException("Could not find method in " + holder.getName() + " returning " + returnType.getName());
    }

    private static Method findStaticMethod(Class<?> holder, Class<?> returnType, Class<?>... params) {
        for (Method m : holder.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getReturnType().isAssignableFrom(returnType) && parameterMatch(m.getParameterTypes(), params))
                return m;
        }
        throw new RuntimeException("Could not find static method in " + holder.getName());
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

    private static Class<?> findClassOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            return null;
        }
    }
}
