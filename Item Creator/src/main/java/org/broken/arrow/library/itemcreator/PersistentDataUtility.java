package org.broken.arrow.library.itemcreator;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.broken.arrow.library.itemcreator.utility.nbt.NBTValue;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for working with Bukkit's {@link PersistentDataContainer}.
 * <p>
 * This class provides helper methods for storing and retrieving custom
 * data types such as {@link String}, {@link Integer}, {@link Double},
 * {@link Byte}, {@link Long}, {@link Float}, {@link ItemStack}, {@link UUID},
 * and primitive arrays in an item's persistent data container.
 * <p>
 * It also contains custom {@link PersistentDataType} implementations
 * for {@link UUID} and {@link ItemStack} values to allow complex type storage.
 */
public final class PersistentDataUtility {
    private final Plugin plugin;

    /**
     * Creates a new persistent data utility for the specified plugin.
     *
     * @param plugin the plugin instance; used to create {@link NamespacedKey}s
     */
    public PersistentDataUtility(@Nonnull final Plugin plugin ){
        this.plugin = plugin;
    }

    /**
     * Sets a value in the given {@link PersistentDataContainer} using the appropriate data type.
     * <p>
     * This method supports standard Bukkit types as well as custom types for
     * {@link UUID} and {@link ItemStack}, including array handling for
     * {@code byte[]}, {@code int[]}, and {@code long[]}.
     * <p>
     * If the {@link NBTValue} indicates that the key should be removed,
     * the key is deleted from the container.
     *
     * @param key           the key to store the value under
     * @param nbtValue      the value wrapper containing the actual value and removal flag
     * @param dataContainer the persistent data container to modify
     */
    public void setPersistentDataContainer(String key, NBTValue nbtValue, final PersistentDataContainer dataContainer) {
        final Object value = nbtValue.getValue();
        final Class<?> targetType = value.getClass();
        final NamespacedKey namespacedKey = new NamespacedKey(this.plugin, key);
        if (nbtValue.isRemoveKey()) {
            dataContainer.remove(namespacedKey);
        }

        if (targetType == String.class) {
            dataContainer.set(namespacedKey, PersistentDataType.STRING, (String) value);
            return;
        }
        if (targetType == Integer.class) {
            dataContainer.set(namespacedKey, PersistentDataType.INTEGER, (Integer) value);
            return;
        }
        if (targetType == Double.class) {
            dataContainer.set(namespacedKey, PersistentDataType.DOUBLE, (Double) value);
            return;
        }
        if (targetType == Byte.class) {
            dataContainer.set(namespacedKey, PersistentDataType.BYTE, (Byte) value);
            return;
        }
        if (targetType == Long.class) {
            dataContainer.set(namespacedKey, PersistentDataType.LONG, (Long) value);
            return;
        }
        if (targetType == Float.class) {
            dataContainer.set(namespacedKey, PersistentDataType.FLOAT, (Float) value);
            return;
        }
        if (targetType == ItemStack.class) {
            PersistentDataType<String, ItemStack> itemStackTagType = new ItemStackTagType();
            dataContainer.set(namespacedKey, itemStackTagType, (ItemStack) value);
            return;
        }
        if (targetType == UUID.class) {
            PersistentDataType<byte[], UUID> uuidItemTagType = new UUIDItemTagType();
            dataContainer.set(namespacedKey, uuidItemTagType, (UUID) value);
            return;
        }
        if (setArrays(targetType, value, dataContainer, namespacedKey)) return;

        dataContainer.set(namespacedKey, PersistentDataType.STRING, value + "");
    }

    /**
     * Attempts to store array types in the {@link PersistentDataContainer}.
     * <p>
     * This method supports {@code byte[]}, {@code int[]}, and {@code long[]} arrays.
     *
     * @param targetType    the type of the value
     * @param value         the array value to store
     * @param dataContainer the persistent data container to modify
     * @param namespacedKey the key under which to store the value
     * @return {@code true} if the array type was handled successfully; {@code false} otherwise
     */
    private boolean setArrays(final Class<?> targetType, final Object value, final PersistentDataContainer dataContainer, final NamespacedKey namespacedKey) {
        if (targetType.isArray()) {
            if (value instanceof byte[]) {
                dataContainer.set(namespacedKey, PersistentDataType.BYTE_ARRAY, (byte[]) value);
                return true;
            }
            if (value instanceof int[]) {
                dataContainer.set(namespacedKey, PersistentDataType.INTEGER_ARRAY, (int[]) value);
                return true;
            }
            if (value instanceof long[]) {
                dataContainer.set(namespacedKey, PersistentDataType.LONG_ARRAY, (long[]) value);
                return true;
            }
        }
        return false;
    }

    /**
     * Custom {@link PersistentDataType} for storing {@link UUID} values in a {@code byte[]} format.
     * <p>
     * UUIDs are stored in a 16-byte array (two longs).
     */
    public static class UUIDItemTagType implements PersistentDataType<byte[], UUID> {

        @Nonnull
        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Nonnull
        @Override
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Nonnull
        @Override
        public byte[] toPrimitive(UUID uuid, PersistentDataAdapterContext context) {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
            return buffer.array();
        }

        @Nonnull
        @Override
        public UUID fromPrimitive(@Nonnull byte[] bytes, PersistentDataAdapterContext context) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long leastBits = buffer.getLong();
            long mostBits = buffer.getLong();
            return new UUID(mostBits, leastBits);
        }
    }

    /**
     * Custom {@link PersistentDataType} for storing {@link ItemStack} values as {@link String}.
     * <p>
     * ItemStacks are serialized to a {@code Map}, converted to JSON, and Base64-encoded.
     * When reading, they are Base64-decoded, parsed from JSON, and deserialized.
     */
    public static class ItemStackTagType implements PersistentDataType<String, ItemStack> {

        @Nonnull
        @Override
        public Class<String> getPrimitiveType() {
            return String.class;
        }

        @Nonnull
        @Override
        public Class<ItemStack> getComplexType() {
            return ItemStack.class;
        }

        @Override
        public String toPrimitive(final ItemStack complex, final PersistentDataAdapterContext context) {
            Map<String, Object> serialized = complex.serialize();
            String json = new Gson().toJson(serialized);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public ItemStack fromPrimitive(final String primitive, final PersistentDataAdapterContext context) {
            String json = new String(Base64.getDecoder().decode(primitive), StandardCharsets.UTF_8);
            Map<String, Object> map = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            return ItemStack.deserialize(map);
        }
    }
}
