package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.nbt.nms.api.ComponentEditor;
import org.broken.arrow.library.logging.Logging;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle the new way where Minecraft using Codec and MinecraftKey, however in paper and newer spigot
 * it is using ResourceLocation and ResourceKey.
 */
public final class VanillaComponentSession implements ComponentEditor {
    private static final Logging logger = new Logging(VanillaComponentSession.class);
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
        buffer.put(key, value);
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

    @Override
    public void setDouble(@NonNull String key, double value) {
        buffer.put(key, value);
    }

    @Override
    public double getDouble(@NonNull String key) {
        Object v = getRaw(key);
        if (v instanceof Number)
            return ((Number) v).doubleValue();
        return -1;
    }

    @Override
    public void setLong(@NonNull String key, long value) {
        buffer.put(key, value);
    }

    @Override
    public long getLong(@NonNull String key) {
        Object v = getRaw(key);
        if (v instanceof Number)
            return ((Number) v).longValue();
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

    @Override
    public void setIntArray(String key, int[] value) {
        buffer.put(key, value);
    }

    @Override
    public void setLongArray(String key, long[] value) {
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

    @Override
    public int @NonNull [] getIntArray(String key) {
        Object v = getRaw(key);
        if (v instanceof int[])
            return (int[]) v;
        return new int[0];
    }

    @Override
    public long @NonNull [] getLongArray(String key) {
        Object v = getRaw(key);
        if (v instanceof long[])
            return (long[]) v;
        return new long[0];
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