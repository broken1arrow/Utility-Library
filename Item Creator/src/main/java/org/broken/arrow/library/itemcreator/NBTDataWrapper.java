package org.broken.arrow.library.itemcreator;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.broken.arrow.library.itemcreator.nbt.nms.NbtWrapper;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.persistent.data.container.NBTDataUtility;
import org.broken.arrow.library.itemcreator.persistent.data.container.legacy.LegacyPersistentData;
import org.broken.arrow.library.itemcreator.persistent.data.container.PersistentDataUtility;
import org.broken.arrow.library.itemcreator.nbt.NBTDataWriter;
import org.broken.arrow.library.itemcreator.nbt.NBTValue;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.broken.arrow.library.nbt.utility.ConvertObjectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final double serverVersion;
    private final Plugin plugin;
    private PersistentDataUtility persistentData;
    private LegacyPersistentData legacyPersistentData;
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
        if (ItemCreator.getVersion().versionAtLeast(13.1)) {
            if (ItemCreator.getVersion().versionAtLeast(14.0))
                this.persistentData = new PersistentDataUtility(this.plugin);
            else
                this.legacyPersistentData = new LegacyPersistentData(this.plugin);
        }
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
     * Sets a custom NBT application function to be used when applying NBT data.
     * Compared to {@link #add(String, Object, boolean)} and
     * {@link #add(String, Object)}, this gives you greater control over
     * which metadata is applied and also allows removing keys.
     *
     * @param function a consumer that modifies the provided {@link NBTDataWriter}
     */
    public void applyNBT(Consumer<NBTDataWriter> function) {
        this.consumer = function;
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

        if (nbtApi != null) {
            return applyNbtToItem(nbtApi, itemStack, nbtData);
        } else {
            return this.setPersistentData(itemStack, nbtData);
        }
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


    private ItemStack setPersistentData(final ItemStack itemStack, final NBTDataWriter nbtData) {
        final Map<String, NBTValue> nbtCache = nbtData.getNbtCache();
        final Map<String, Object> metaDataMap = this.getMetaDataMap();
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && ItemCreator.getVersion().versionBetween(13.1, 14.0)) {
            if (this.legacyPersistentData == null) return itemStack;

            nbtCache.forEach((key, nbtValue) -> {
                legacyPersistentData.setCustomTagContainer(key, nbtValue, meta);
            });
        } else if (ItemCreator.getVersion().versionNewer(13.2) && meta != null) {
            if (this.persistentData == null) return itemStack;

            if (!metaDataMap.isEmpty())
                metaDataMap.forEach((s, nbtValue) ->
                        this.persistentData.setPersistentDataContainer(s, new NBTValue(nbtValue), meta.getPersistentDataContainer())
                );
            else
                nbtCache.forEach((key, nbtValue) ->
                        this.persistentData.setPersistentDataContainer(key, nbtValue, meta.getPersistentDataContainer())
                );
        } else {
            NbtWrapper nbtWrapper = new NbtWrapper(itemStack);
            final CompoundTag compound = nbtWrapper.getOrCreateCompound(this.getCompoundKey());
            NBTDataUtility nBTDataUtility = new NBTDataUtility();
            if (!metaDataMap.isEmpty()) {
                metaDataMap.forEach((s, nbtValue) ->
                        nBTDataUtility.setPersistentData(compound, s, new NBTValue(nbtValue))
                );
            } else {
                nbtCache.forEach((key, nbtValue) ->
                        nBTDataUtility.setPersistentData(compound, key, nbtValue)
                );
            }
            return nbtWrapper.apply();
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack applyNbtToItem(final RegisterNbtAPI nbtApi, final ItemStack itemStack, final NBTDataWriter nbtData) {
        final Map<String, NBTValue> nbtCache = nbtData.getNbtCache();
        final Map<String, Object> metaDataMap = this.getMetaDataMap();
        return nbtApi.getCompMetadata().setMetadata(itemStack,
                nbtDataWrite -> {
                    if (nbtData.isClearNBT())
                        nbtDataWrite.clearNBT();
                    else {
                        final ReadWriteNBT compound = nbtDataWrite.getCompound();
                        if (!metaDataMap.isEmpty())
                            metaDataMap.forEach((s, nbtValue) ->
                                    ConvertObjectType.setNBTValue(compound, s, nbtValue)
                            );
                        else
                            nbtCache.forEach((key, nbtValue) -> {
                                if (nbtValue.isRemoveKey()) {
                                    compound.removeKey(key);
                                } else {
                                    ConvertObjectType.setNBTValue(compound, key, nbtValue.getValue());
                                }
                            });
                    }
                });
    }

    /**
     * Retrieve the key used for set metadata.
     *
     * @return the compound key.
     */
    private String getCompoundKey() {
        return plugin.getName() + "_NBT";
    }
}




