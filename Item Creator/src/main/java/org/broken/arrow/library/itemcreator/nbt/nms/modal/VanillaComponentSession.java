package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.nbt.nms.api.ComponentEditor;
import org.broken.arrow.library.logging.Logging;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
     * Create vanilla component session
     *
     * @param nmsStack the nms itemStack
     */
    public VanillaComponentSession(@Nonnull final Object nmsStack) {
        this.nmsStack = nmsStack;
        this.cachedRoot = ComponentAccess.loadRootComponentMap(nmsStack);
    }

    @Override
    public void setInt(@Nonnull final String key, final int value) {
        buffer.put(key, value);
    }


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

    @Override
    public void setString(@Nonnull final String key, final String value) {
        buffer.put(key, value);
    }

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
    public int @NonNull [] getIntArray(final String key) {
        Object v = getRaw(key);
        if (v instanceof int[])
            return (int[]) v;
        return new int[0];
    }

    @Override
    public long @NonNull [] getLongArray(final String key) {
        Object v = getRaw(key);
        if (v instanceof long[])
            return (long[]) v;
        return new long[0];
    }

    @Override
    public void setBoolean(@Nonnull final String key, boolean value) {
        buffer.put(key, value);
    }


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
     * Checks if the session contains any active components, whether pre-existing or pending.
     *
     * @return true if this item has one or more components/tags.
     */
    public boolean hasKeys() {
        return !this.buffer.isEmpty() || !this.cachedRoot.isEmpty();
    }

    /**
     * Checks if the session contains any pending updates.
     *
     * @param key the NBT key to check
     * @return true if this compund have tags set that sghould be applied.
     */
    public boolean hasPendingUpdates(@Nonnull final String key) {
        return buffer.containsKey(key);
    }

    @Override
    public boolean hasKey(@Nonnull final String key) {
        if (buffer.containsKey(key))
            return buffer.get(key) != null;
        if (cachedRoot.containsKey(key))
            return true;

        final Object type = ComponentAccess.resolve(key);
        return type != null && ComponentAccess.hasComponent(nmsStack, type);
    }

    @Override
    public boolean isEmpty() {
        return !hasKeys();
    }

    @Override
    public void remove(@Nonnull final String key) {
        buffer.put(key, null);

        if (cachedRoot != null) {
            cachedRoot.remove(key);
        }
    }

    /**
     * Get the raw set value
     *
     * @param key component key
     * @return the component object.
     */
    @Nullable
    private Object getRaw(final String key) {
        // Pending writes override everything
        if (buffer.containsKey(key))
            return buffer.get(key);

        // Cached root snapshot
        if (cachedRoot.containsKey(key))
            return cachedRoot.get(key);

        // Slow path: read from NMS once
        Object type = ComponentAccess.resolve(key);
        Object v = ComponentAccess.getComponent(nmsStack, type);
        cachedRoot.put(key, v);
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
                if (type == null) {
                    logger.log(Level.WARNING, () -> "Skipping invalid data component key: " + key);
                    continue;
                }
                if (value == null) {
                    // Remove NMS type from ItemStack
                    ComponentAccess.removeComponent(nmsStack, type);
                } else {
                    // Write into NMS ItemStack
                    ComponentAccess.setComponent(nmsStack, type, value);
                }
            }
        } catch (Exception t) {
            logger.logError(t, () -> "Could not apply vanilla components state changes.");
        }

        buffer.clear();
    }

}