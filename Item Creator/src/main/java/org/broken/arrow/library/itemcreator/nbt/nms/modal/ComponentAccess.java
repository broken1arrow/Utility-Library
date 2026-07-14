package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public final class ComponentAccess {
    private static final Logging logger = new Logging(ComponentAccess.class);
    private static final Map<Class<?>, Optional<MethodHandle>> TYPE_KEY_HANDLES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<MethodHandle>> RESOURCE_LOC_HANDLES = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final MethodHandle SET;
    private static final MethodHandle GET;
    private static final MethodHandle REMOVE;
    private static final MethodHandle GET_COMPONENTS;
    private static final MethodHandle GET_TYPE;
    private static final MethodHandle GET_VALUE;
    private static final ComponentResolver COMPONENT_RESOLVER;

    private static boolean ready = true;


    static {
        MethodHandle set = null;
        MethodHandle get = null;
        MethodHandle remove = null;
        MethodHandle getComponents = null;
        MethodHandle getType = null;
        MethodHandle getValue = null;


        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Class<?> itemstackClass = Class.forName("net.minecraft.world.item.ItemStack");
            final Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");

            final Method mSet = itemstackClass.getMethod("set", dataComponentTypeClass, Object.class);
            final Method mGet = itemstackClass.getMethod("get", dataComponentTypeClass);
            final Method mRemove = itemstackClass.getMethod("remove", dataComponentTypeClass);



            Field componentsField = null;
            try {
                componentsField = itemstackClass.getDeclaredField("components");
            } catch (NoSuchFieldException e) {
                // Fallback for different mappings
                for (Field field : itemstackClass.getDeclaredFields()) {
                    if (field.getType().getSimpleName().equals("PatchedDataComponentMap") ||
                            field.getType().getSimpleName().equals("DataComponentMap")) {
                        componentsField = field;
                        break;
                    }
                }
            }

            if (componentsField != null) {
                componentsField.setAccessible(true);
                getComponents = lookup.unreflectGetter(componentsField);
            }

            // 2. Resolve TypedDataComponent (1.20.5+) record methods
            Class<?> typedDataComponentClass = Class.forName("net.minecraft.core.component.TypedDataComponent");
            Method typeMethod = typedDataComponentClass.getMethod("type");
            Method valueMethod = typedDataComponentClass.getMethod("value");

            getType = lookup.unreflect(typeMethod);
            getValue = lookup.unreflect(valueMethod);

            set = lookup.unreflect(mSet);
            get = lookup.unreflect(mGet);
            remove = lookup.unreflect(mRemove);


        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException t) {
            ready = false;
            logger.logError(t, () -> "Failed to preload ComponentAccess");
        }
        SET = set;
        GET = get;
        REMOVE = remove;
        GET_COMPONENTS = getComponents;
        GET_TYPE = getType;
        GET_VALUE = getValue;

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
            logger.logError(e, () -> "Failed to set the component to the stack.");
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
     * Reflectively extracts all data components and their associated values from an NMS ItemStack.
     * <p>
     * This method seamlessly handles both the modern 1.20.5+ {@code PatchedDataComponentMap} (which is {@link Iterable})
     * and legacy NMS component maps, utilizing high-performance cached {@code MethodHandle}s.
     *
     * @param nmsStack The underlying NMS ItemStack to extract components from.
     * @return A map containing the resolved component IDs (e.g., "minecraft:damage", "minecraft:unbreaking") as keys and their raw NMS values as values.
     * Returns an empty map if component extraction fails or is unsupported.
     */
    public static Map<String, Object> loadRootComponentMap(Object nmsStack) {
        Map<String, Object> out = new HashMap<>();
        if (GET_COMPONENTS == null) {
            return out;
        }
        try {
            Object componentMap = GET_COMPONENTS.invoke(nmsStack);
            if (componentMap == null) return out;

            // 1.20.5+ Fast Path with MethodHandles
            if (componentMap instanceof Iterable<?>) {
                final Iterable<?> container = (Iterable<?>) componentMap;
                for (Object typedDataComponent : container) {
                    Object type = GET_TYPE.invoke(typedDataComponent);
                    Object value = GET_VALUE.invoke(typedDataComponent);

                    String id = ComponentAccess.getId(type);
                    out.put(id, value);
                }
            } else if (componentMap instanceof Map<?, ?>) {
                Map<?, ?> oldContainer = (Map<?, ?>) componentMap;
                for (Map.Entry<?, ?> entry : oldContainer.entrySet()) {
                    String id = ComponentAccess.getId(entry.getKey());
                    out.put(id, entry.getValue());
                }
            }
        } catch (Throwable ex) {
            logger.logError(ex, () -> "Could not read vanilla component root map");
        }
        return out;
    }

    /**
     * Resolves the string identifier (Namespaced Key) from a raw component key object.
     * <p>
     * This acts as a safe, fail-fast wrapper around the underlying reflection lookups.
     *
     * @param componentKey The raw NMS component key (e.g., a DataComponentType).
     * @return The string representation of the resource location (e.g., "minecraft:damage", "minecraft:unbreaking"),
     * or {@code null} if the key is null or resolution fails.
     */
    @Nullable
    public static String getId(Object componentKey) {
        if (componentKey == null) return null;

        try {
            Object keyObj = getComponentTypeKey(componentKey);
            if (keyObj == null) return null;

            Object locObj = getResourceKey(keyObj);
            if (locObj == null) return null;

            return locObj.toString();

        } catch (Throwable t) {
            logger.logError(t, () -> "Could not get the Id for vanilla component key: " + componentKey);
            return null;
        }
    }

    /**
     * Reflectively resolves and unwraps the inner {@code ComponentTypeKey} object from the main component key.
     * <p>
     * This method scans the component's class fields for the exact type name and caches the resulting
     * {@code MethodHandle} for near-zero overhead on subsequent calls.
     *
     * @param componentKey The outer component key object (e.g., DataComponentType).
     * @return The unwrapped inner component type key, or {@code null} if the field cannot be found.
     * @throws Throwable If the {@code MethodHandle} invocation fails.
     */
    @Nullable
    private static Object getComponentTypeKey(Object componentKey) throws Throwable {
        Class<?> compClass = componentKey.getClass();

        Optional<MethodHandle> typeKeyOpt = TYPE_KEY_HANDLES.computeIfAbsent(compClass, clazz -> {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType().getSimpleName().contains("ComponentTypeKey")) {
                    try {
                        f.setAccessible(true);
                        return Optional.of(LOOKUP.unreflectGetter(f));
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            return Optional.empty();
        });
        if (!typeKeyOpt.isPresent()) return null;
        return typeKeyOpt.get().invoke(componentKey);
    }

    /**
     * Reflectively resolves the actual {@code ResourceLocation} (or legacy {@code MinecraftKey})
     * from the unwrapped {@code ComponentTypeKey}.
     * <p>
     * This method scans the component's class fields for the exact type name and caches the resulting
     * {@code MethodHandle} for near-zero overhead on subsequent calls.
     *
     * @param keyObj The unwrapped inner {@code ComponentTypeKey} object.
     * @return The raw NMS {@code ResourceLocation} or {@code MinecraftKey} object, or {@code null} if not found.
     * @throws Throwable If the {@code MethodHandle} invocation fails.
     */
    @Nullable
    private static Object getResourceKey(Object keyObj) throws Throwable {
        Class<?> keyClass = keyObj.getClass();

        Optional<MethodHandle> resLocOpt = RESOURCE_LOC_HANDLES.computeIfAbsent(keyClass, clazz -> {
            for (Field f : clazz.getDeclaredFields()) {
                String n = f.getType().getSimpleName();
                if (n.contains("MinecraftKey") || n.contains("ResourceLocation")) {
                    try {
                        f.setAccessible(true);
                        return Optional.of(LOOKUP.unreflectGetter(f));
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            return Optional.empty();
        });

        if (!resLocOpt.isPresent()) return null;

        return resLocOpt.get().invoke(keyObj);
    }

    /**
     * Get the id for the component.
     *
     * @param componentKey Get the component key.
     * @return returns the name.
     */
    public static String getIdold(Object componentKey) {
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

            Object locObj = getResourceKeyr(keyObj);
            if (locObj == null) return null;
            // toString gives namespace:path
            return locObj.toString();
        } catch (Exception t) {
            return null;
        }
    }

    private static Object getResourceKeyr(final Object keyObj) throws IllegalAccessException {
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