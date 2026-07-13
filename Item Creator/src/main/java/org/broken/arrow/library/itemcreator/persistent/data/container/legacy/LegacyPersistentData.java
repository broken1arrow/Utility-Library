package org.broken.arrow.library.itemcreator.persistent.data.container.legacy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.broken.arrow.library.itemcreator.utility.nbt.NBTValue;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class LegacyPersistentData {
    private final Plugin plugin;

    /**
     * Creates a new persistent data utility for the specified plugin, only for the 13.2 version.
     *
     * @param plugin the plugin instance; used to create {@link NamespacedKey}s
     */
    public LegacyPersistentData(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Writes a value to a {@link CustomItemTagContainer} for older Minecraft versions.
     *
     * @param key      the tag key
     * @param nbtValue the value to write
     * @param meta     the item meta containing the custom tag container
     */
    public void setCustomTagContainer(String key, NBTValue nbtValue, ItemMeta meta) {
        final Object value = nbtValue.getValue();
        final Class<?> targetType = value.getClass();
        final CustomItemTagContainer customTagContainer = meta.getCustomTagContainer();
        final NamespacedKey namespacedKey = new NamespacedKey(this.plugin, key);
        if (nbtValue.isRemoveKey()) {
            customTagContainer.removeCustomTag(namespacedKey);
        }

        if (targetType == String.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.STRING, (String) value);
            return;
        }
        if (targetType == Integer.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.INTEGER, (Integer) value);
            return;
        }
        if (targetType == Double.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.DOUBLE, (Double) value);
            return;
        }
        if (targetType == Byte.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.BYTE, (Byte) value);
            return;
        }
        if (targetType == Long.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.LONG, (Long) value);
            return;
        }
        if (targetType == Float.class) {
            customTagContainer.setCustomTag(namespacedKey, ItemTagType.FLOAT, (Float) value);
            return;
        }
        if (targetType == ItemStack.class) {
            ItemTagType<String, ItemStack> itemStackTagType = new ItemStackTagTypeOld();
            customTagContainer.setCustomTag(namespacedKey, itemStackTagType, (ItemStack) value);
            return;
        }
        if (targetType == UUID.class) {
            ItemTagType<byte[], UUID> uuidItemTagType = new UUIDItemTagTypeOld();
            customTagContainer.setCustomTag(namespacedKey, uuidItemTagType, (UUID) value);
            return;
        }
        if (setArrays(targetType, value, customTagContainer, namespacedKey)) return;

        customTagContainer.setCustomTag(namespacedKey, ItemTagType.STRING, value + "");
    }

    /**
     * A legacy {@link ItemTagType} implementation for storing {@link UUID} values.
     */
    public class UUIDItemTagTypeOld implements ItemTagType<byte[], UUID> {

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
        public byte[] toPrimitive(UUID uuid, ItemTagAdapterContext itemTagAdapterContext) {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getLeastSignificantBits());
            buffer.putLong(uuid.getMostSignificantBits());
            return buffer.array();
        }

        @Nonnull
        @Override
        public UUID fromPrimitive(@Nonnull byte[] bytes, ItemTagAdapterContext itemTagAdapterContext) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long leastBits = buffer.getLong();
            long mostBits = buffer.getLong();
            return new UUID(mostBits, leastBits);
        }
    }

    /**
     * A legacy {@link ItemTagType} implementation for storing {@link ItemStack} values.
     * Data is serialized to JSON and then Base64 encoded for storage.
     */
    public class ItemStackTagTypeOld implements ItemTagType<String, ItemStack> {

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

        @Nonnull
        @Override
        public String toPrimitive(@Nonnull ItemStack itemStack, ItemTagAdapterContext itemTagAdapterContext) {
            Map<String, Object> serialized = itemStack.serialize();
            String json = new Gson().toJson(serialized);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }

        @Nonnull
        @Override
        public ItemStack fromPrimitive(@Nonnull String primitive, ItemTagAdapterContext itemTagAdapterContext) {
            String json = new String(Base64.getDecoder().decode(primitive), StandardCharsets.UTF_8);
            Map<String, Object> map = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            return ItemStack.deserialize(map);
        }
    }


    /**
     * Attempts to store array types in a custom tag container.
     *
     * @param targetType         the class type the value are.
     * @param value              the value to set.
     * @param customTagContainer the container where you want to set the tag.
     * @param namespacedKey      the name space key to set as the key.
     * @return {@code true} if the array type was handled successfully; {@code false} otherwise
     */
    private boolean setArrays(final Class<?> targetType, final Object value, final CustomItemTagContainer customTagContainer, final NamespacedKey namespacedKey) {
        if (targetType.isArray()) {
            if (value instanceof byte[]) {
                customTagContainer.setCustomTag(namespacedKey, ItemTagType.BYTE_ARRAY, (byte[]) value);
                return true;
            }
            if (value instanceof int[]) {
                customTagContainer.setCustomTag(namespacedKey, ItemTagType.INTEGER_ARRAY, (int[]) value);
                return true;
            }
            if (value instanceof long[]) {
                customTagContainer.setCustomTag(namespacedKey, ItemTagType.LONG_ARRAY, (long[]) value);
                return true;
            }
        }
        return false;
    }

}
