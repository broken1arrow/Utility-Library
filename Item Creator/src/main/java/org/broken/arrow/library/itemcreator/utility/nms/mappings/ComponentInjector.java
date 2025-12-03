package org.broken.arrow.library.itemcreator.utility.nms.mappings;

import org.bukkit.inventory.ItemStack;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;

/**
 * ComponentInjector - writes component values directly into the ItemStack "components" NBT.
 * - Reflection-only (no net.minecraft.core.* or com.mojang.* direct usage).
 * - Supports primitives, strings, byte[]/int[], Map -> CompoundTag and List -> ListTag.
 * <p>
 * Usage:
 *   ItemStack updated = ComponentInjector.setComponentRaw(item, "minecraft:max_stack_size", 4);
 *   ItemStack updated = ComponentInjector.setComponentRaw(item, "minecraft:custom_data", Map.of("plugin", Map.of("k", 1)));
 */
public final class ComponentInjector {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // NMS classes
    private static final Class<?> NMS_ITEMSTACK;
    private static final Class<?> NBT_COMPOUND;
    private static final Class<?> NBT_TAG;
    private static final Class<?> INT_TAG_CLASS;
    private static final Class<?> BYTE_TAG_CLASS;
    private static final Class<?> SHORT_TAG_CLASS;
    private static final Class<?> LONG_TAG_CLASS;
    private static final Class<?> FLOAT_TAG_CLASS;
    private static final Class<?> DOUBLE_TAG_CLASS;
    private static final Class<?> STRING_TAG_CLASS;
    private static final Class<?> BYTE_ARRAY_TAG_CLASS;
    private static final Class<?> INT_ARRAY_TAG_CLASS;
    private static final Class<?> LIST_TAG_CLASS;
    private static final Class<?> NMS_ITEMS_CLASS;

    // Handles
    private static final MethodHandle CRAFT_AS_NMS;
    private static final MethodHandle CRAFT_AS_BUKKIT;
    private static final MethodHandle COMPOUND_PUT;      // CompoundTag.put(String, Tag)
    private static final MethodHandle COMPOUND_GET_COMPOUND; // CompoundTag.getCompound(String)
    private static final MethodHandle COMPOUND_CONTAINS; // CompoundTag.contains(String)
    private static final MethodHandle COMPOUND_GET;      // CompoundTag.get(String)
    private static final MethodHandle CREATE_COMPOUND;   // CompoundTag constructor
    private static final MethodHandle CREATE_LIST;       // new ListTag()
    private static final MethodHandle LIST_ADD;          // ListTag.add(Tag)
    private static final MethodHandle SET_TAG_ON_ITEM;   // ItemStack.setTag / set or setTag
    private static final MethodHandle GET_TAG_ON_ITEM;   // ItemStack.getTag()
    //private static final MethodHandle GET_TAG_ON_ITEM_2; // alternative name try

    static {
        try {
            NMS_ITEMSTACK = Class.forName("net.minecraft.world.item.ItemStack");
            NBT_COMPOUND = Class.forName("net.minecraft.nbt.CompoundTag");
            NBT_TAG = Class.forName("net.minecraft.nbt.Tag");
            NMS_ITEMS_CLASS = Class.forName("net.minecraft.world.item.Items");

            // Tag subtypes (not all servers expose dedicated classes in the same package name; we handle by name)
            INT_TAG_CLASS = findClassOrNull("net.minecraft.nbt.IntTag");
            BYTE_TAG_CLASS = findClassOrNull("net.minecraft.nbt.ByteTag");
            SHORT_TAG_CLASS = findClassOrNull("net.minecraft.nbt.ShortTag");
            LONG_TAG_CLASS = findClassOrNull("net.minecraft.nbt.LongTag");
            FLOAT_TAG_CLASS = findClassOrNull("net.minecraft.nbt.FloatTag");
            DOUBLE_TAG_CLASS = findClassOrNull("net.minecraft.nbt.DoubleTag");
            STRING_TAG_CLASS = findClassOrNull("net.minecraft.nbt.StringTag");
            BYTE_ARRAY_TAG_CLASS = findClassOrNull("net.minecraft.nbt.ByteArrayTag");
            INT_ARRAY_TAG_CLASS = findClassOrNull("net.minecraft.nbt.IntArrayTag");
            LIST_TAG_CLASS = findClassOrNull("net.minecraft.nbt.ListTag");

            // CraftItemStack.asNMSCopy/asBukkitCopy
            Class<?> craftItem = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            CRAFT_AS_NMS = LOOKUP.findStatic(craftItem, "asNMSCopy", MethodType.methodType(NMS_ITEMSTACK, ItemStack.class));
            CRAFT_AS_BUKKIT = LOOKUP.findStatic(craftItem, "asBukkitCopy", MethodType.methodType(ItemStack.class, NMS_ITEMSTACK));

            // CompoundTag constructor (new CompoundTag())
            Constructor<?> compCtor = NBT_COMPOUND.getDeclaredConstructor();
            compCtor.setAccessible(true);
            CREATE_COMPOUND = LOOKUP.unreflectConstructor(compCtor);

            // CompoundTag.put(String, Tag)
            Method put = findMethodByName(NBT_COMPOUND, "put", String.class, NBT_TAG);
            COMPOUND_PUT = LOOKUP.unreflect(put);

            // CompoundTag.getCompound(String)
            Method getCompound = findMethodByName(NBT_COMPOUND, "getCompound", String.class);
            COMPOUND_GET_COMPOUND = LOOKUP.unreflect(getCompound);

            // CompoundTag.contains(String)
            Method contains = findMethodByName(NBT_COMPOUND, "contains", String.class);
            COMPOUND_CONTAINS = LOOKUP.unreflect(contains);

            // CompoundTag.get(String)
            Method compGet = findMethodByName(NBT_COMPOUND, "get", String.class);
            COMPOUND_GET = LOOKUP.unreflect(compGet);

            // Try ListTag constructor and add
            if (LIST_TAG_CLASS != null) {
                Constructor<?> listCtor = LIST_TAG_CLASS.getDeclaredConstructor();
                listCtor.setAccessible(true);
                CREATE_LIST = LOOKUP.unreflectConstructor(listCtor);
                Method addM = findMethodByName(LIST_TAG_CLASS, "add", NBT_TAG);
                LIST_ADD = LOOKUP.unreflect(addM);
            } else {
                CREATE_LIST = null;
                LIST_ADD = null;
            }

            // ItemStack.getTag() or getTag -> returns CompoundTag
            Method getTagMethod = null;
            try {
                getTagMethod = NMS_ITEMSTACK.getMethod("getTag");
            } catch (NoSuchMethodException e) {
                // try other names
                for (Method m : NMS_ITEMSTACK.getMethods()) {
                    if ((m.getName().equals("getTag") || m.getName().equals("tag")) &&
                            m.getParameterCount() == 0 &&
                            m.getReturnType().getName().contains("Compound")) {
                        getTagMethod = m;
                        break;
                    }
                }
            }
            if (getTagMethod != null) {
                GET_TAG_ON_ITEM = LOOKUP.unreflect(getTagMethod);
            } else {
                GET_TAG_ON_ITEM = null;
            }

            // ItemStack.setTag(CompoundTag) or setTag
            Method setTagMethod = null;
            try {
                setTagMethod = NMS_ITEMSTACK.getMethod("setTag", NBT_COMPOUND);
            } catch (NoSuchMethodException e) {
                for (Method m : NMS_ITEMSTACK.getMethods()) {
                    if ((m.getName().equals("setTag") || m.getName().equals("set")) &&
                            m.getParameterCount() == 1 &&
                            m.getParameterTypes()[0].getName().contains("Compound")) {
                        setTagMethod = m;
                        break;
                    }
                }
            }
            if (setTagMethod != null) {
                SET_TAG_ON_ITEM = LOOKUP.unreflect(setTagMethod);
            } else {
                SET_TAG_ON_ITEM = null;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize ComponentInjector reflection", t);
        }
    }

    private ComponentInjector() {}

    // -------------------------
    // Public API
    // -------------------------

    /**
     * Writes a component value (raw) into the Bukkit ItemStack's components NBT and returns a new ItemStack.
     * <p>
     * value can be:
     * - Integer/Long/Short/Byte/Float/Double/Boolean/String
     * - byte[] / int[]
     * - Map<String,Object> -> converted to CompoundTag recursively
     * - List<Object> -> converted to ListTag of corresponding element tags
     * - an existing NMS Tag instance (net.minecraft.nbt.Tag)
     *
     * @param bukkitItem the item to modify
     * @param componentId the id like  minecraft:damage
     * @param value the value to set
     * @return return itemStack.
     */
    public static ItemStack setComponentRaw(ItemStack bukkitItem, String componentId, Object value) {
        try {
            // get nms stack
            Object nms = CRAFT_AS_NMS.invoke(bukkitItem);

            // get or create root tag
            Object root = (GET_TAG_ON_ITEM != null) ? GET_TAG_ON_ITEM.invoke(nms) : null;
            if (root == null) {
                root = CREATE_COMPOUND.invoke();
                if (SET_TAG_ON_ITEM != null) SET_TAG_ON_ITEM.invoke(nms, root);
            }

            // get components compound
            Object components = COMPOUND_GET_COMPOUND.invoke(root, "components");
            if (components == null) {
                components = CREATE_COMPOUND.invoke();
                COMPOUND_PUT.invoke(root, "components", components);
            }

            // build tag for the value
            Object tag = buildTagFor(value);
            if (tag == null) return bukkitItem; // unsupported type

            // put into components
            COMPOUND_PUT.invoke(components, componentId, tag);

            // return modified bukkit copy
            return (ItemStack) CRAFT_AS_BUKKIT.invoke(nms);
        } catch (Throwable t) {
            t.printStackTrace();
            return bukkitItem;
        }
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static Object buildTagFor(Object value) throws Throwable {
        if (value == null) return null;

        // pass-through if already Tag
        if (NBT_TAG.isInstance(value)) return value;

        if (value instanceof Integer) return makeIntTag((Integer) value);
        if (value instanceof Long) return makeLongTag((Long) value);
        if (value instanceof Short) return makeShortTag((Short) value);
        if (value instanceof Byte) return makeByteTag((Byte) value);
        if (value instanceof Double) return makeDoubleTag((Double) value);
        if (value instanceof Float) return makeFloatTag((Float) value);
        if (value instanceof Boolean) return makeByteTag((byte) (((Boolean) value) ? 1 : 0)); // booleans as byte
        if (value instanceof String) return makeStringTag((String) value);
        if (value instanceof byte[]) return makeByteArrayTag((byte[]) value);
        if (value instanceof int[]) return makeIntArrayTag((int[]) value);

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            Object compound = CREATE_COMPOUND.invoke();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                Object childTag = buildTagFor(e.getValue());
                if (childTag != null) COMPOUND_PUT.invoke(compound, e.getKey(), childTag);
            }
            return compound;
        }

        if (value instanceof List) {
            if (LIST_TAG_CLASS == null) return null;
            Object list = CREATE_LIST.invoke();
            @SuppressWarnings("unchecked")
            List<Object> listVal = (List<Object>) value;
            for (Object el : listVal) {
                Object child = buildTagFor(el);
                if (child != null) LIST_ADD.invoke(list, child);
            }
            return list;
        }

        // fallback: try to convert via toString -> StringTag
        return makeStringTag(value.toString());
    }

    // Tag factory methods using static 'valueOf' or constructor fallbacks
    private static Object makeIntTag(int v) throws Throwable {
        if (INT_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(INT_TAG_CLASS, "valueOf", int.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        // fallback: CompoundTag.putInt wrapper - create a compound and put
        Object tmp = CREATE_COMPOUND.invoke();
        Method putInt = findMethodByName(NBT_COMPOUND, "putInt", String.class, int.class);
        LOOKUP.unreflect(putInt).invoke(tmp, "__tmp", v);
        return (Object) LOOKUP.unreflect(findMethodByName(NBT_COMPOUND, "getInt", String.class)).invoke(tmp, "__tmp");
    }

    private static Object makeLongTag(long v) throws Throwable {
        if (LONG_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(LONG_TAG_CLASS, "valueOf", long.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        // fallback: use string
        return makeStringTag(Long.toString(v));
    }

    private static Object makeShortTag(short v) throws Throwable {
        if (SHORT_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(SHORT_TAG_CLASS, "valueOf", short.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        return makeIntTag(v);
    }

    private static Object makeByteTag(byte v) throws Throwable {
        if (BYTE_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(BYTE_TAG_CLASS, "valueOf", byte.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        return makeIntTag(v);
    }

    private static Object makeFloatTag(float v) throws Throwable {
        if (FLOAT_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(FLOAT_TAG_CLASS, "valueOf", float.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        return makeStringTag(Float.toString(v));
    }

    private static Object makeDoubleTag(double v) throws Throwable {
        if (DOUBLE_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(DOUBLE_TAG_CLASS, "valueOf", double.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        return makeStringTag(Double.toString(v));
    }

    private static Object makeStringTag(String v) throws Throwable {
        if (STRING_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(STRING_TAG_CLASS, "valueOf", String.class);
            if (valueOf != null) return valueOf.invoke(null, v);
        }
        // fallback: put string into Compound and extract? Simpler: return java String (some methods accept bare)
        // But put into compound
        Object tmp = CREATE_COMPOUND.invoke();
        Method putString = findMethodByName(NBT_COMPOUND, "putString", String.class, String.class);
        LOOKUP.unreflect(putString).invoke(tmp, "__tmp", v);
        return LOOKUP.unreflect(findMethodByName(NBT_COMPOUND, "getString", String.class)).invoke(tmp, "__tmp");
    }

    private static Object makeByteArrayTag(byte[] arr) throws Throwable {
        if (BYTE_ARRAY_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(BYTE_ARRAY_TAG_CLASS, "of", byte[].class);
            if (valueOf != null) return valueOf.invoke(null, new Object[]{arr});
        }
        // fallback: store as Compound with base64? Not ideal. Use putByteArray
        Object tmp = CREATE_COMPOUND.invoke();
        Method put = findMethodByName(NBT_COMPOUND, "putByteArray", String.class, byte[].class);
        LOOKUP.unreflect(put).invoke(tmp, "__tmp", arr);
        return LOOKUP.unreflect(findMethodByName(NBT_COMPOUND, "getByteArray", String.class)).invoke(tmp, "__tmp");
    }

    private static Object makeIntArrayTag(int[] arr) throws Throwable {
        if (INT_ARRAY_TAG_CLASS != null) {
            Method valueOf = findStaticMethodByName(INT_ARRAY_TAG_CLASS, "of", int[].class);
            if (valueOf != null) return valueOf.invoke(null, new Object[]{arr});
        }
        Object tmp = CREATE_COMPOUND.invoke();
        Method put = findMethodByName(NBT_COMPOUND, "putIntArray", String.class, int[].class);
        LOOKUP.unreflect(put).invoke(tmp, "__tmp", arr);
        return LOOKUP.unreflect(findMethodByName(NBT_COMPOUND, "getIntArray", String.class)).invoke(tmp, "__tmp");
    }

    // -------------------------
    // Reflection helpers
    // -------------------------
    private static Class<?> findClassOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method findMethodByName(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException ignore) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && parameterTypesMatch(m.getParameterTypes(), paramTypes)) return m;
            }
            throw new RuntimeException("Method " + name + " not found on " + clazz.getName());
        }
    }

    private static Method findStaticMethodByName(Class<?> clazz, String name, Class<?>... paramTypes) {
        for (Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getName().equals(name) && parameterTypesMatch(m.getParameterTypes(), paramTypes)) {
                return m;
            }
        }
        return null;
    }

    private static boolean parameterTypesMatch(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].isAssignableFrom(expected[i])) return false;
        }
        return true;
    }
}
