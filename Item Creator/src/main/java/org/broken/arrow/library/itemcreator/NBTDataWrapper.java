package org.broken.arrow.library.itemcreator;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.broken.arrow.library.itemcreator.utility.NBTDataWriter;
import org.broken.arrow.library.itemcreator.utility.NBTValue;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.broken.arrow.library.nbt.utility.ConvertObjectType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class NBTDataWrapper {
    private final Map<String, Object> itemMetaMap = new LinkedHashMap<>();
    private final ItemCreator itemCreator;
    private final float serverVersion;
    private final Plugin plugin;

    private Consumer<NBTDataWriter> consumer;

    public NBTDataWrapper(@Nonnull final ItemCreator itemCreator) {
        this.itemCreator = itemCreator;
        this.serverVersion = ItemCreator.getServerVersion();
        this.plugin = itemCreator.getPlugin();
    }

    public static NBTDataWrapper of(@Nonnull final ItemCreator itemCreator) {
        return new NBTDataWrapper(itemCreator);
    }

    /**
     * Add metadata to your item. To get the metadata with method {@link #getMetaDataMap()}
     * This method will convert value to string always, use {@link #add(String, Object, boolean)}
     * if you want to keep all data on the value.
     *
     * @param key   key to get the value.
     * @param value value you has set .
     * @return this class
     */

    public NBTDataWrapper add(String key, Object value) {
        return add(key, value, false);
    }

    /**
     * Add metadata to your item. To get the metadata with method {@link #getMetaDataMap()}
     *
     * @param key           key to get the value.
     * @param value         value you has set.
     * @param keepClazzData true if it shall keep all data on the item or false to convert value to string.
     * @return this class
     */
    public NBTDataWrapper add(String key, Object value, boolean keepClazzData) {
        itemMetaMap.put(key, (keepClazzData ? value : value + ""));
        return this;
    }

    public Map<String, Object> getMetaDataMap() {
        return itemMetaMap;
    }

    public void applyNBT(Consumer<NBTDataWriter> function) {
        this.consumer = function;
    }

    public Consumer<NBTDataWriter> getConsumer() {
        applyNBT(nbtDataWriterWrapper ->
                nbtDataWriterWrapper.setBoolean("", false)
        );

        return consumer;
    }

    public ItemStack applyNBT(ItemStack itemStack) {
        NBTDataWriter nBTData = new NBTDataWriter();
        apply(nBTData);
        RegisterNbtAPI nbtApi = this.itemCreator.getNbtApi();
        Map<String, NBTValue> nbtCache = nBTData.getNbtCache();

        if (nbtApi != null) {
            return nbtApi.getCompMetadata().setMetadata(itemStack,
                    nbtDataWrite -> {
                        if (nBTData.isClearNBT())
                            nbtDataWrite.clearNBT();
                        else {
                            nbtCache.forEach((key, nbtValue) -> {
                                ConvertObjectType.setNBTValue(nbtDataWrite.getCompound(), key, nbtValue);
                            });
                        }
                    });
        } else {
            final ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && this.serverVersion > 13.1F && this.serverVersion < 14.0F) {
                nbtCache.forEach((key, nbtValue) -> {
                    setCustomTagContainer(key, nbtValue, meta);
                });
            } else if (this.serverVersion > 13.2F && meta != null) {
                nbtCache.forEach((key, nbtValue) -> {
                    this.setPersistentDataContainer(key, nbtValue, meta);
                });
            }
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private void setCustomTagContainer(String key, NBTValue nbtValue, ItemMeta meta) {
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
        if (setArrays(nbtValue, targetType, value, customTagContainer, namespacedKey)) return;

        customTagContainer.setCustomTag(namespacedKey, ItemTagType.STRING, value + "");
    }

    private boolean setArrays(final NBTValue nbtValue, final Class<?> targetType, final Object value, final CustomItemTagContainer customTagContainer, final NamespacedKey namespacedKey) {
        if (targetType.isArray() && nbtValue.getClass().isArray()) {
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

    private void setPersistentDataContainer(String key, NBTValue nbtValue, ItemMeta meta) {
        final Object value = nbtValue.getValue();
        final Class<?> targetType = value.getClass();
        final PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
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
        if (setArrays(nbtValue, targetType, value, dataContainer, namespacedKey)) return;

        dataContainer.set(namespacedKey, PersistentDataType.STRING, value + "");
    }

    private boolean setArrays(final NBTValue nbtValue, final Class<?> targetType, final Object value, final PersistentDataContainer dataContainer, final NamespacedKey namespacedKey) {
        if (targetType.isArray() && nbtValue.getClass().isArray()) {
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

    public void apply(@Nonnull final NBTDataWriter nbtData) {
        if (this.consumer == null)
            return;
        this.consumer.accept(nbtData);
    }

    public class UUIDItemTagType implements PersistentDataType<byte[], UUID> {

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

    public class ItemStackTagType implements PersistentDataType<String, ItemStack> {

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

}
