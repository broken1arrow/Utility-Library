package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A utility class for setting various types of NBT data in an item or entity's compound tag.
 * This class provides methods to set string, integer, double, byte, short, long, float, byte array,
 * integer array, boolean, ItemStack, ItemStack array, and UUID values.
 */
public final class NBTDataWriter  {
	private final Map<String, NBTValue> nbtCache = new HashMap<>();
	private boolean clearNBT;


	/**
	 * Sets a string value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The string value to set.
	 */
	public void setString(String key, String value) {
		this.putNBT(key,value);

	}

	/**
	 * Sets an integer value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The integer value to set.
	 */
	public void setInteger(String key, Integer value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a double value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The double value to set.
	 */
	public void setDouble(String key, Double value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a byte value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The byte value to set.
	 */
	public void setByte(String key, Byte value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a short value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The short value to set.
	 */
	public void setShort(String key, Short value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a long value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The long value to set.
	 */
	public void setLong(String key, Long value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a float value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The float value to set.
	 */
	public void setFloat(String key, Float value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a byte array value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The byte array value to set.
	 */
	public void setByteArray(String key, byte[] value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets an integer array value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The integer array value to set.
	 */
	public void setIntArray(String key, int[] value) {
		this.putNBT(key,value);
	}

	/**
	 * Sets a boolean value in the item or entity's compound tag.
	 *
	 * @param key   the key for retrieve this value.
	 * @param value The boolean value to set.
	 */
	public void setBoolean(String key, Boolean value) {
		this.putNBT(key,value);
	}

	/**
	 * Save an ItemStack as a compound under a given key
	 *
	 * @param key  the key for retrieve this value.
	 * @param item The ItemStack value to set.
	 */
	public void setItemStack(String key, ItemStack item) {
		this.putNBT(key,item);
	}

	/**
	 * Save an ItemStack Array as a compound under a given key
	 *
	 * @param key   the key for retrieve this value.
	 * @param items The ItemStack array value to set.
	 */
	public void setItemStackArray(String key, ItemStack[] items) {
		this.putNBT(key,items);
	}

	/**
	 * Set the UUID value on the item
	 *
	 * @param key   the key for retrieve this value.
	 * @param value the UUID value to set.
	 */
	public void setUUID(String key, UUID value) {
		this.putNBT(key,value);
	}
	
	/**
	 * Set the Enum value as a compound under a given key.
	 *
	 * @param key   the key for retrieve this value.
	 * @param <E>  The generic class type for the enum.
	 * @param value the Enum value to set.
	 */
	public <E extends Enum<?>> void setEnum(String key, E value){
		this.putNBT(key,value);
	}
	
	/**
	 * Removes a key from the item or entity's compound tag.
	 *
	 * @param key The key to delete.
	 */
	public void removeKey(String key) {
		this.putNBT(key,null);
	}

	public Map<String, NBTValue> getNbtCache() {
		return nbtCache;
	}

	public boolean isClearNBT() {
		return clearNBT;
	}

	public void putNBT(@Nonnull final String key, final Object value) {
		this.putNBT(key,value,false);
	}

	public void putNBT(@Nonnull final String key, final Object value,final boolean removeKey) {
		this.nbtCache.put(key,new NBTValue(value, removeKey));
	}

	/**
	 * Remove all keys from this compound
	 */
	public void clearNBT(){
		this.clearNBT = true;
	}
}
