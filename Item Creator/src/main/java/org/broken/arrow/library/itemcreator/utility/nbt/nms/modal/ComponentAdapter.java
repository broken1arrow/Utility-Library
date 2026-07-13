package org.broken.arrow.library.itemcreator.utility.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.utility.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.NbtWrapper;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.compound.VanillaComponentTag;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.ComponentFactory;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.api.NbtEditor;
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

/**
 * Unified component layer for modern item data handling in Minecraft.
 *
 * <p>This adapter provides a consistent interface for working with both:
 * <ul>
 *   <li><strong>CUSTOM_DATA</strong> — modern custom item data stored in the item’s
 *       CompoundTag/Component system.</li>
 *   <li><strong>Vanilla item components</strong> — optional editing of raw vanilla item properties.</li>
 * </ul>
 *
 * <p>Modern Minecraft versions split item data between custom components and vanilla components.
 * This class abstracts both systems without forcing the cost of heavy reflection initialization:
 * only the minimal reflection required for handling custom data is loaded eagerly.
 * Reflection required for vanilla component editing is deferred until explicitly needed.</p>
 *
 * <p><strong>Implementation details:</strong></p>
 * <ul>
 *   <li><code>ComponentItemDataSession</code> handles <strong>CUSTOM_DATA</strong> only.
 *       Its reflection usage is lightweight and initialized eagerly in the static initializer.</li>
 *   <li><code>VanillaComponentSession</code> is a <em>lazy-loaded</em> static nested class.
 *       Its static initializer runs only when {@link #enableVanillaTagEditor()} is called,
 *       meaning the heavier reflection cost of vanilla component support is incurred only if used.</li>
 * </ul>
 *
 * <p>All operations are safe to call even if reflection is not fully initialized:
 * <ul>
 *   <li>Warnings are logged if the reflection layer could not be loaded.</li>
 *   <li>Use {@link #isReady()} to verify that reflection is fully initialized.</li>
 *   <li>Null pointer checks are performed internally, so method calls will not break the server.</li>
 * </ul>
 *
 * <p>Use this class when you need low-level, version-independent access to both
 * custom and vanilla item data. For simpler, high-level operations, consider using
 * {@link ComponentFactory} or {@link NbtWrapper}.</p>
 */
public class ComponentAdapter implements NbtEditor {
    private static final Logging logger = new Logging(ComponentAdapter.class);
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
        if (!READY) return null;
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
