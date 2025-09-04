package org.broken.arrow.library.itemcreator;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.broken.arrow.library.itemcreator.utility.nbt.NBTDataWriter;
import org.broken.arrow.library.itemcreator.utility.nbt.NBTValue;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.broken.arrow.library.nbt.utility.ConvertObjectType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A utility wrapper for handling and applying NBT data to {@link ItemStack} objects.
 * <p>
 * This class provides methods for storing metadata in a structured format, manipulating NBT values,
 * and applying them to items via different APIs depending on the server version.
 * It supports Bukkit's {@link PersistentDataContainer}, {@link CustomItemTagContainer},
 * and a custom NBT API through {@link RegisterNbtAPI}.
 * <p>
 * The wrapper is designed for chaining and fluent usage.
 */
public final class NBTDataWrapper {
    private final Map<String, Object> itemMetaMap = new LinkedHashMap<>();
    private final ItemCreator itemCreator;
    private final float serverVersion;
    private final Plugin plugin;
    private final PersistentDataUtility persistentData;

    private Consumer<NBTDataWriter> consumer;

    /**
     * Constructs a new {@code NBTDataWrapper} for a given {@link ItemCreator}.
     *
     * @param itemCreator the item creator instance used for NBT handling
     */
    public NBTDataWrapper(@Nonnull final ItemCreator itemCreator) {
        this.itemCreator = itemCreator;
        this.serverVersion = ItemCreator.getServerVersion();
        this.plugin = itemCreator.getPlugin();
        this.persistentData = new PersistentDataUtility(this.plugin);
    }

    /**
     * Factory method to create a new instance of {@code NBTDataWrapper}.
     *
     * @param itemCreator the item creator instance
     * @return a new wrapper instance bound to the given {@code ItemCreator}
     */
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

    /**
     * Gets the full metadata map currently stored in this wrapper.
     *
     * @return a map of metadata keys to values
     */
    public Map<String, Object> getMetaDataMap() {
        return itemMetaMap;
    }

    /**
     * Sets a custom NBT application function to be used when applying NBT data.
     *
     * @param function a consumer that modifies the provided {@link NBTDataWriter}
     */
    public void applyNBT(Consumer<NBTDataWriter> function) {
        this.consumer = function;
    }

    /**
     * Returns the current NBT application consumer.
     *
     * @return the NBT consumer, or {@code null} if none set
     */
    public Consumer<NBTDataWriter> getConsumer() {
        return consumer;
    }

    /**
     * Applies the stored NBT data to a given {@link ItemStack}.
     * <p>
     * The method adapts its behavior based on the server version and available NBT API.
     *
     * @param itemStack the item to apply NBT to
     * @return the updated item with applied NBT values
     */
    public ItemStack applyNBT(ItemStack itemStack) {
        final NBTDataWriter nbtData = new NBTDataWriter();
        apply(nbtData);
        final RegisterNbtAPI nbtApi = this.itemCreator.getNbtApi();
        final Map<String, NBTValue> nbtCache = nbtData.getNbtCache();
        final Map<String, Object> metaDataMap = this.getMetaDataMap();

        if (nbtApi != null) {
            return applyNbtToItem(nbtApi,itemStack, nbtData);
        } else {
            this.setPersistentData(itemStack, nbtData);
        }

        return itemStack;
    }


    /**
     * Invokes the current NBT application consumer, if set.
     *
     * @param nbtData the NBT data writer to modify
     */
    public void apply(@Nonnull final NBTDataWriter nbtData) {
        if (this.consumer == null)
            return;
        this.consumer.accept(nbtData);
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

    private void setPersistentData(final ItemStack itemStack, final NBTDataWriter nbtData) {
        final Map<String, NBTValue> nbtCache = nbtData.getNbtCache();
        final Map<String, Object> metaDataMap = this.getMetaDataMap();
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && this.serverVersion > 13.1F && this.serverVersion < 14.0F) {
            nbtCache.forEach((key, nbtValue) -> {
                setCustomTagContainer(key, nbtValue, meta);
            });
        } else if (this.serverVersion > 13.2F && meta != null) {
            if (!metaDataMap.isEmpty())
                metaDataMap.forEach((s, nbtValue) ->
                        this.persistentData.setPersistentDataContainer(s, new NBTValue(nbtValue), meta.getPersistentDataContainer())
                );
            else
                nbtCache.forEach((key, nbtValue) ->
                        this.persistentData.setPersistentDataContainer(key, nbtValue, meta.getPersistentDataContainer())
                );
        }
        itemStack.setItemMeta(meta);
    }

    private ItemStack applyNbtToItem(final RegisterNbtAPI nbtApi, final ItemStack itemStack, final NBTDataWriter nbtData) {
        final Map<String, NBTValue> nbtCache = nbtData.getNbtCache();
        final Map<String, Object> metaDataMap = this.getMetaDataMap();
        return nbtApi.getCompMetadata().setMetadata(itemStack,
                nbtDataWrite -> {
                    if (nbtData.isClearNBT())
                        nbtDataWrite.clearNBT();
                    else {
                        if (!metaDataMap.isEmpty())
                            metaDataMap.forEach((s, nbtValue) ->
                                    ConvertObjectType.setNBTValue(nbtDataWrite.getCompound(), s, nbtValue)
                            );
                        else
                            nbtCache.forEach((key, nbtValue) -> {
                                ConvertObjectType.setNBTValue(nbtDataWrite.getCompound(), key, nbtValue);
                            });
                    }
                });
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

    /**
     * Writes a value to a {@link CustomItemTagContainer} for older Minecraft versions.
     *
     * @param key      the tag key
     * @param nbtValue the value to write
     * @param meta     the item meta containing the custom tag container
     */
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
        if (setArrays(targetType, value, customTagContainer, namespacedKey)) return;

        customTagContainer.setCustomTag(namespacedKey, ItemTagType.STRING, value + "");
    }
}
