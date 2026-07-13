package org.broken.arrow.library.itemcreator.utility.nbt.nms.modal;

import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public final class ComponentAccess {
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
        } catch (Exception t) {
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