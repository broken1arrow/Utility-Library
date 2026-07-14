package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentResolver {
    private static final Logging logger = new Logging(ComponentResolver.class);
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();
    private static final Object DATA_COMPONENT_REGISTRY;
    private static final MethodHandle CREATE_RESOURCE_LOCATION;
    private static final MethodHandle CREATE_METHOD;
    private static final MethodHandle REGISTRY_GET_METHOD;
    private static final Object DATA_COMPONENT_REGISTRY_KEY;
    private static final MethodHandle HOLDER_VALUE_METHOD;
    private static final Class<?> HOLDER_CLASS;
    private static boolean ready = true;

    static {
        Object dataComponentRegistry = null;
        Object registryKey = null;
        MethodHandle createResourceLocation = null;
        MethodHandle createMethod = null;
        MethodHandle registryGetMethod = null;
        MethodHandle holderValueMethod = null;
        Class<?> holderClass = null;

        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            final Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
            final Class<?> builtInRegistries = Class.forName("net.minecraft.core.registries.BuiltInRegistries");


            final Field f = builtInRegistries.getField("DATA_COMPONENT_TYPE");
            dataComponentRegistry = f.get(null);

            final Field keyField = dataComponentRegistry.getClass().getDeclaredField("key");
            keyField.setAccessible(true);
            registryKey = keyField.get(dataComponentRegistry);

            try {
                Method parseMethod = resourceLocationClass.getMethod("parse", String.class);
                createResourceLocation = lookup.unreflect(parseMethod);
            } catch (NoSuchMethodException e) {
                Constructor<?> constructor = resourceLocationClass.getConstructor(String.class);
                createResourceLocation = lookup.unreflectConstructor(constructor);
            }

            Method mCreate = resourceKeyClass.getMethod("create", resourceKeyClass, resourceLocationClass);
            Method mGet = dataComponentRegistry.getClass().getMethod("get", resourceKeyClass);

            createMethod = lookup.unreflect(mCreate);
            registryGetMethod = lookup.unreflect(mGet);

            try {
                holderClass = Class.forName("net.minecraft.core.Holder");
                Method valueMethod = holderClass.getMethod("value");
                holderValueMethod = lookup.unreflect(valueMethod);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                // Older versions don't have Holders, which is fine
            }

        } catch (Exception ex) {
            ready = false;
            logger.logError(ex, () -> "Failed to load DataComponent registry");
        }
        DATA_COMPONENT_REGISTRY = dataComponentRegistry;
        DATA_COMPONENT_REGISTRY_KEY = registryKey;
        CREATE_RESOURCE_LOCATION = createResourceLocation;
        CREATE_METHOD = createMethod;
        REGISTRY_GET_METHOD = registryGetMethod;

        HOLDER_CLASS = holderClass;
        HOLDER_VALUE_METHOD = holderValueMethod;
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

            // 1. Create ResourceLocation (Automatically uses 'parse' or 'new' based on static block)
            final Object componentRL = CREATE_RESOURCE_LOCATION.invoke(keyChecked);

            // 2. Create ResourceKey<DataComponentType>
            final Object resourceKey = CREATE_METHOD.invoke(
                    DATA_COMPONENT_REGISTRY_KEY,
                    componentRL
            );
            // 3. Fetch component type from registry
            Object registryResult = REGISTRY_GET_METHOD.invoke(DATA_COMPONENT_REGISTRY, resourceKey);
            // 4. Unwrap Optional if Minecraft wrapped it (1.20.5+)
            if (registryResult instanceof Optional) {
                Optional<?> opt = (Optional<?>) registryResult;
                if (!opt.isPresent()) return null;
                registryResult = opt.get();
            }

            // 5. Unwrap Holder if Mojang wrapped it in a Reference/Holder (1.20.5+ Registry behavior)
            if (HOLDER_CLASS != null && HOLDER_CLASS.isInstance(registryResult)) {
                registryResult = HOLDER_VALUE_METHOD.invoke(registryResult);
            }

            return registryResult;
        } catch (Throwable ex) {
            logger.logError(ex, () -> "Failed to resolve component: " + key);
        }
        return null;
    }
/*
    @Nullable
    private static Object resolveInternalOld(String key) {
        try {
            final String keyChecked = rl(key);
            Object componentRL;

            try {
                Method parseMethod = RESOURCE_LOCATION_CLASS.getMethod("parse", String.class);
                componentRL = parseMethod.invoke(null, keyChecked);
            } catch (NoSuchMethodException e) {
                componentRL = RESOURCE_LOCATION_CLASS
                        .getConstructor(String.class).newInstance(keyChecked);
            }
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
    }*/

    // Construct a ResourceLocation namespace:path
    private static String rl(String key) {
        int i = key.indexOf(':');
        if (i == -1)
            return "minecraft:" + key;
        return key;
    }
}