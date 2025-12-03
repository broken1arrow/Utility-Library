package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.VanillaCompoundTag;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified legacy-friendly session for item custom data (CUSTOM_DATA) + optional nested vanilla component helper.
 * <p>
 * - ComponentItemDataSession handles just CUSTOM_DATA (cheap reflection in static init).
 * - VanillaComponentSession is a lazy-loaded static nested class: its static initializer only runs when
 * you reference VanillaComponentSession, so you pay the extra reflection cost only if you need vanilla components.
 * <p>
 */
public class ComponentItemDataSession implements NbtEditor {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    // Core handles (CUSTOM_DATA path)
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle ITEMSTACK_GET;
    private static final MethodHandle ITEMSTACK_SET;
    private static final MethodHandle CUSTOMDATA_OF;
    private static final MethodHandle CUSTOMDATA_COPYTAG;
    private static final MethodHandle NMS_COMPOUND_PUT; // CompoundTag.put(String, Tag)

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
            t.printStackTrace();
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
                vanillaSession.applyBuffered();
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

    // ----------------- VanillaComponentSession nested (lazy) -----------------

    /**
     * Lazy-loaded helper for vanilla components.
     * Manages NMS ItemStack components and a buffered set of staged changes.
     */
    public static final class VanillaComponentSession {
        private static final boolean READY_VANILLA;

        // minimal handles used by injector
        static {
            boolean ok = true;
            try {
                // No static work here except verification; ComponentInjector will perform heavy binding lazily.
                Class.forName("net.minecraft.world.item.ItemStack");
            } catch (Throwable t) {
                ok = false;
            }
            READY_VANILLA = ok;
        }

        private final Map<String, Object> buffer = new HashMap<>();

        private final Object nmsStack;                     // The real NMS ItemStack
        private final Map<String, Object> bufferedValues;// key → primitive or String

        /**
         * Creates a new session for the given NMS ItemStack.
         *
         * @param nmsStack the NMS ItemStack to operate on
         */
        public VanillaComponentSession(@Nonnull final Object nmsStack) {
            this.nmsStack = nmsStack;
            this.bufferedValues = new HashMap<>();
        }

        /**
         * Checks if this helper is ready (NMS classes found).
         *
         * @return true if ready
         */
        public boolean isReady() {
            return READY_VANILLA;
        }

        /**
         * Stages an integer value to be applied to the NMS ItemStack.
         *
         * @param key   component key
         * @param value integer value
         */
        public void setInt(String key, int value) {
            bufferedValues.put(key, Integer.valueOf(value));
        }

        /**
         * Stages a string value to be applied to the NMS ItemStack.
         *
         * @param key   component key
         * @param value your value, like string,int,short and more or
         *              {@code null} to remove the key if it exists.
         */
        public void setValue(@Nonnull final String key, @Nullable final Object value) {
            bufferedValues.put(key, value);
        }

        /**
         * Removes a component from both the NMS ItemStack and the buffer.
         *
         * @param key component key
         */
        public void remove(String key) {
            bufferedValues.remove(key);
            ComponentInjector.removeComponent(nmsStack, key);
        }

        /**
         * Checks whether a component key exists in the underlying NMS ItemStack.
         *
         * @param key component key
         * @return true if the key exists
         */
        public boolean hasKey(String key) {
            return ComponentInjector.hasComponent(nmsStack, key);
        }

        /**
         * Retrieve the component as stored in the NMS ItemStack.
         *
         * @param key the component key
         * @return the raw component, which may be a primitive, String,
         * or a nested structure (like a CompoundTag or ListTag),
         * or {@code null} if the component does not exist.
         */
        @Nullable
        public Object getRaw(String key) {
            return ComponentInjector.getComponent(nmsStack, key);
        }

        /**
         * Retrieve a buffered value that was set via {@link #setInt} or {@link #setValue}.
         * This only includes values staged in this session, not the underlying NMS component.
         *
         * @param key the component key
         * @return the buffered value, or {@code null} if none is buffered.
         */
        @Nullable
        public Object get(String key) {
            return bufferedValues.get(key);
        }

        /**
         * Applies all buffered values to the underlying NMS ItemStack
         * and clears the buffer. Nested structures are not supported here;
         * only primitives and Strings.
         */
        public void applyBuffered() {
            if (buffer.isEmpty()) return;
            try {
                for (Map.Entry<String, Object> e : buffer.entrySet()) {
                    ComponentInjector.setComponentRaw(this.nmsStack, e.getKey(), e.getValue());
                }
                buffer.clear();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }


        // ----------------- ComponentInjector (reflection-only) -----------------
        private static final class ComponentInjector {
            private static final MethodHandles.Lookup L = MethodHandles.lookup();

            private static MethodHandle CREATE_COMPOUND;
            private static MethodHandle COMPOUND_PUT;
            private static MethodHandle COMPOUND_GET_COMPOUND;
            private static MethodHandle GET_TAG_ON_ITEM;
            private static MethodHandle SET_TAG_ON_ITEM;
            private static Class<?> NBT_TAG_CLASS;
            private static Class<?> NBT_COMPOUND_CLASS;
            private static Class<?> LIST_TAG_CLASS;

            static {
                try {
                    Class<?> itemStack = Class.forName("net.minecraft.world.item.ItemStack");
                    NBT_COMPOUND_CLASS = Class.forName("net.minecraft.nbt.CompoundTag");
                    NBT_TAG_CLASS = Class.forName("net.minecraft.nbt.Tag");
                    LIST_TAG_CLASS = findClassOrNull("net.minecraft.nbt.ListTag");

                    Constructor<?> ctor = NBT_COMPOUND_CLASS.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    CREATE_COMPOUND = L.unreflectConstructor(ctor);

                    Method put = findMethodByName(NBT_COMPOUND_CLASS, "put", String.class, NBT_TAG_CLASS);
                    COMPOUND_PUT = L.unreflect(put);

                    Method getCompound = findMethodByName(NBT_COMPOUND_CLASS, "getCompound", String.class);
                    COMPOUND_GET_COMPOUND = L.unreflect(getCompound);

                    Method getTag = findMethodByName(itemStack, "getTag");
                    GET_TAG_ON_ITEM = (getTag != null) ? L.unreflect(getTag) : null;
                    Method setTag = findMethodByName(itemStack, "setTag", NBT_COMPOUND_CLASS);
                    SET_TAG_ON_ITEM = (setTag != null) ? L.unreflect(setTag) : null;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            private ComponentInjector() {
            }

            /**
             * Set or update a component in the "components" compound
             * @param nmsStack   the nms itemStack.
             * @param componentId  the id of the component to remove for example minecraft:damage.
             * @param value the value to set or {@code null} to remove the value if it set.
             */
            static void setComponentRaw(@Nonnull final Object nmsStack,@Nonnull final String componentId,final Object value) {
                try {
                    Object root = (GET_TAG_ON_ITEM != null) ? GET_TAG_ON_ITEM.invoke(nmsStack) : null;
                    if (root == null) {
                        root = CREATE_COMPOUND.invoke();
                        if (SET_TAG_ON_ITEM != null) SET_TAG_ON_ITEM.invoke(nmsStack, root);
                    }

                    Object comps = COMPOUND_GET_COMPOUND.invoke(root, "components");
                    if (comps == null) {
                        comps = CREATE_COMPOUND.invoke();
                        COMPOUND_PUT.invoke(root, "components", comps);
                    }

                    if (value == null) {
                        removeFromCompound(comps, componentId);
                    } else {
                        Object tag = buildTag(value);
                        if (tag != null) {
                            COMPOUND_PUT.invoke(comps, componentId, tag);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            public static boolean hasComponent(Object nmsStack, String key) {
                try {
                    Object comp = getComponent(nmsStack, key);
                    return comp != null;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Nullable
            public static Object getComponent(Object nmsStack, String key) {
                try {
                    Object root = (GET_TAG_ON_ITEM != null) ? GET_TAG_ON_ITEM.invoke(nmsStack) : null;
                    if (root != null) {
                        Object comps = COMPOUND_GET_COMPOUND.invoke(root, "components");
                        if (comps != null) {
                            Method get = NBT_COMPOUND_CLASS.getMethod("get", String.class);
                            return get.invoke(comps, key);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

            /**
             * Remove a component from the "components" compound
             *
             * @param nmsStack the itemStack to remove the key.
             * @param componentId the id of the component to remove for example minecraft:damage.
             */
            static void removeComponent(Object nmsStack, String componentId) {
                try {
                    Object root = (GET_TAG_ON_ITEM != null) ? GET_TAG_ON_ITEM.invoke(nmsStack) : null;
                    if (root == null) return;

                    Object comps = COMPOUND_GET_COMPOUND.invoke(root, "components");
                    if (comps != null) {
                        removeFromCompound(comps, componentId);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            /**
             * Utility: remove a key from a CompoundTag
             *
             * @param compound the compound to remove the key.
             * @param key the key toi remove.
             */
            private static void removeFromCompound(Object compound, String key) {
                try {
                    Method remove = NBT_COMPOUND_CLASS.getMethod("remove", String.class);
                    remove.invoke(compound, key);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            /**
             * Convert Java object to NBT tag
             *
             * @param value The value to check what class it is from.
             * @return it will try to wrap your value correct either inside a compound or directly to the component
             */
            private static Object buildTag(Object value) throws Throwable {
                if (value == null) return null;
                if (NBT_TAG_CLASS.isInstance(value)) return value;

                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) value;
                    Object comp = CREATE_COMPOUND.invoke();
                    for (Map.Entry<String, Object> e : m.entrySet()) {
                        Object child = buildTag(e.getValue());
                        if (child != null) COMPOUND_PUT.invoke(comp, e.getKey(), child);
                    }
                    return comp;
                }

                if (value instanceof List) {
                    if (LIST_TAG_CLASS == null) return null;
                    Object list = LIST_TAG_CLASS.getDeclaredConstructor().newInstance();
                    Method add = findMethodByName(LIST_TAG_CLASS, "add", NBT_TAG_CLASS);
                    for (Object el : (List<?>) value) {
                        Object child = buildTag(el);
                        if (child != null) add.invoke(list, child);
                    }
                    return list;
                }

                Object tmp = CREATE_COMPOUND.invoke();
                if (value instanceof Integer) {
                    Method putInt = findMethodByName(NBT_COMPOUND_CLASS, "putInt", String.class, int.class);
                    L.unreflect(putInt).invoke(tmp, "__tmp", (int) value);
                    return L.unreflect(findMethodByName(NBT_COMPOUND_CLASS, "getInt", String.class)).invoke(tmp, "__tmp");
                }
                if (value instanceof Boolean) {
                    Method putByte = findMethodByName(NBT_COMPOUND_CLASS, "putByte", String.class, byte.class);
                    L.unreflect(putByte).invoke(tmp, "__tmp", (byte) (((Boolean) value) ? 1 : 0));
                    return L.unreflect(findMethodByName(NBT_COMPOUND_CLASS, "getByte", String.class)).invoke(tmp, "__tmp");
                }
                if (value instanceof String) {
                    Method putString = findMethodByName(NBT_COMPOUND_CLASS, "putString", String.class, String.class);
                    L.unreflect(putString).invoke(tmp, "__tmp", (String) value);
                    return L.unreflect(findMethodByName(NBT_COMPOUND_CLASS, "getString", String.class)).invoke(tmp, "__tmp");
                }

                // fallback
                Method putString = findMethodByName(NBT_COMPOUND_CLASS, "putString", String.class, String.class);
                L.unreflect(putString).invoke(tmp, "__tmp", value.toString());
                return L.unreflect(findMethodByName(NBT_COMPOUND_CLASS, "getString", String.class)).invoke(tmp, "__tmp");
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
                if (m.getName().equals(name) && parameterTypesMatch(m.getParameterTypes(), params)) return m;
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
