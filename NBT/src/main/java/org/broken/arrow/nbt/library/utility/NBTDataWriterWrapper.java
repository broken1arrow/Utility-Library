package org.broken.arrow.nbt.library.utility;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * A utility class for setting various types of NBT data in an item or entity's compound tag.
 * This class provides methods to set string, integer, double, byte, short, long, float, byte array,
 * integer array, boolean, ItemStack, ItemStack array, and UUID values.
 */
public final class NBTDataWriterWrapper extends NBTReaderWrapper {

	private final ReadWriteNBT readWriteNBT;

	/**
	 * Constructs a new NBTDataWriterWrapper instance.
	 *
	 * @param readWriteNBT The ReadWriteNBT instance used for interacting with NBT data.
	 */
	public NBTDataWriterWrapper(ReadWriteNBT readWriteNBT) {
		super(readWriteNBT);
		this.readWriteNBT = readWriteNBT;
	}

	/**
	 * Merges all data from the provided compound tag into this compound. This is done in one action, so
	 * it also works with Tiles/Entities
	 *
	 * @param comp The ReadableNBT instance containing the data to merge.
	 */
	public void mergeCompound(ReadableNBT comp) {
		this.readWriteNBT.mergeCompound(comp);
	}

	/**
	 * Sets a string value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The string value to set.
	 */
	public void setString(String key, String value) {
		this.readWriteNBT.setString(key, value);
	}

	/**
	 * Sets an integer value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The integer value to set.
	 */
	public void setInteger(String key, Integer value) {
		this.readWriteNBT.setInteger(key, value);
	}

	/**
	 * Sets a double value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The double value to set.
	 */
	public void setDouble(String key, Double value) {
		this.readWriteNBT.setDouble(key, value);
	}

	/**
	 * Sets a byte value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The byte value to set.
	 */
	public void setByte(String key, Byte value) {
		this.readWriteNBT.setByte(key, value);
	}

	/**
	 * Sets a short value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The short value to set.
	 */
	public void setShort(String key, Short value) {
		this.readWriteNBT.setShort(key, value);
	}

	/**
	 * Sets a long value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The long value to set.
	 */
	public void setLong(String key, Long value) {
		this.readWriteNBT.setLong(key, value);
	}

	/**
	 * Sets a float value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The float value to set.
	 */
	public void setFloat(String key, Float value) {
		this.readWriteNBT.setFloat(key, value);
	}

	/**
	 * Sets a byte array value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The byte array value to set.
	 */
	public void setByteArray(String key, byte[] value) {
		this.readWriteNBT.setByteArray(key, value);
	}

	/**
	 * Sets an integer array value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The integer array value to set.
	 */
	public void setIntArray(String key, int[] value) {
		this.readWriteNBT.setIntArray(key, value);
	}

	/**
	 * Sets a boolean value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The boolean value to set.
	 */
	public void setBoolean(String key, Boolean value) {
		this.readWriteNBT.setBoolean(key, value);
	}

	/**
	 * Save an ItemStack as a compound under a given key
	 *
	 * @param key  the key for retrieve this value.
	 * @param item The ItemStack value to set.
	 */
	public void setItemStack(String key, ItemStack item) {
		this.readWriteNBT.setItemStack(key, item);
	}

	/**
	 * Save an ItemStack Array as a compound under a given key
	 *
	 * @param key   the key for retrieve this value.
	 * @param items The ItemStack array value to set.
	 */
	public void setItemStackArray(String key, ItemStack[] items) {
		this.readWriteNBT.setItemStackArray(key, items);
	}

	/**
	 * Set the UUID value on the item
	 *
	 * @param key   the key for retrieve this value.
	 * @param value the UUID value to set.
	 */
	public void setUUID(String key, UUID value) {
		this.readWriteNBT.setUUID(key, value);
	}
	
	/**
	 * Set the Enum value as a compound under a given key.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value the Enum value to set.
	 */
	public <E extends Enum<?>> void setEnum(String key, E value){
		this.readWriteNBT.setEnum(key, value);
	}
	
	/**
	 * Removes a key from the item or entity's compound tag.
	 *
	 * @param key The key to delete.
	 */
	public void removeKey(String key) {
		this.readWriteNBT.removeKey(key);
	}

	/**
	 * Remove all keys from this compound
	 */
	public void clearNBT(){
		this.readWriteNBT.clearNBT();
	}
}
