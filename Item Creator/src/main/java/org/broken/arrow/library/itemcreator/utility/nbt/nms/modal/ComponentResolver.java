package org.broken.arrow.library.itemcreator.utility.nbt.nms.modal;

import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentResolver {
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
    public boolean isReady() {
        return ready;
    }

    /**
     * Resolve a component type using its resource location ("minecraft:damage", "minecraft:unbreaking").
     * Uses only invoke / newInstance consistently.
     *
     * @param key the component key string
     * @return the DataComponentType instance
     */
    public Object resolve(String key) {
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
                    DATA_COMPONENT_REGISTRY_KEY,
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