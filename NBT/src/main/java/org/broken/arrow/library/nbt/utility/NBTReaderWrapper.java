package org.broken.arrow.library.nbt.utility;

import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBTList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * This class provides methods for retrieving data from an underlying object without modifying it.
 * <p>&nbsp;</p>
 * <p>
 * <p>
 * Note: The {@link ReadableNBT#getCompound(String)} method was removed because retrieving
 * a compound by key is unnecessary. The compound instance is now supplied directly via
 * the constructor. Since this library wraps your primary compound, exposing access to
 * nested compounds directly would go against its goal of simplicity and ease of use.
 * </p>
 * <p>
 * If you need low-level access to nested compounds, you can use the
 * {@link de.tr7zw.changeme.nbtapi.NBT} class directly, or call
 * {@link NBTReaderWrapper#getCompound()} to access a specific nested compound —
 * but this is only recommended if you're familiar with the structure of the top-level compound.
 * </p>
 *
 * <p>&nbsp;</p>
 *
 * @param <T> the type of NBT compound being wrapped; must extend {@link ReadableNBT}.
 *            This type determines which NBT structure is accessible through this wrapper
 */
public class NBTReaderWrapper<T extends ReadableNBT>  {

	private final T readableNBT;

	/**
	 * Create an instance of this wrapper.
	 *
	 * @param readableNBT the ReadableNBT data to read nbt data from.
	 */
	public NBTReaderWrapper(T readableNBT) {
		this.readableNBT = readableNBT;
	}

	/**
	 * Given a key, return the value associated with that key.
	 *
	 * @param key The key to get the value for.
	 * @return The value of the key.
	 */
	public String getString(final String key) {
		return readableNBT.getString(key);
	}

	/**
	 * Given a key, return the value associated with that key as an Integer, or 0 if
	 * the key is not found.
	 *
	 * @param key The key to look up in the properties file.
	 * @return The value of the key.
	 */
	public Integer getInteger(final String key) {
		return readableNBT.getInteger(key);
	}

	/**
	 * Returns the value associated with the given key as a double, or false of not
	 * found.
	 *
	 * @param key The key of the preference to retrieve.
	 * @return A double value
	 */
	public Double getDouble(final String key) {
		return readableNBT.getDouble(key);
	}

	/**
	 * Get the value of the given key as a byte, or 0 if the key is not found.
	 *
	 * @param key The key to get the value for.
	 * @return A byte
	 */
	public Byte getByte(final String key) {
		return readableNBT.getByte(key);
	}

	/**
	 * Returns the value of the key as a Short, or 0 if the key is not found.
	 *
	 * @param key The key of the value you want to get.
	 * @return A short value
	 */
	public Short getShort(final String key) {
		return readableNBT.getShort(key);
	}

	/**
	 * Returns the value associated with the given key as a Long, or 0 if the key is
	 * not found.
	 *
	 * @param key The key of the value you want to get.
	 * @return A Long object
	 */

	public Long getLong(final String key) {
		return readableNBT.getLong(key);
	}

	/**
	 * Returns the value of the given key as a Float, or 0 if the key does not
	 * exist.
	 *
	 * @param key The key of the preference to retrieve.
	 * @return A float value
	 */

	public Float getFloat(final String key) {
		return readableNBT.getFloat(key);
	}

	/**
	 * Returns the value associated with the given key as a byte array, or null if
	 * the key is not found.
	 *
	 * @param key The key to use to retrieve the value.
	 * @return A byte array.
	 */
	public byte[] getByteArray(final String key) {
		return readableNBT.getByteArray(key);
	}

	/**
	 * Returns the value associated with the given key as an array of integers, or
	 * null if the key does not exist.
	 *
	 * @param key The key of the value you want to get.
	 * @return An array of integers.
	 */
	public int[] getIntArray(final String key) {
		return readableNBT.getIntArray(key);
	}

	/**
	 * Returns the value associated with the given key, or false if the key is not
	 * found.
	 *
	 * @param key The key of the preference to retrieve.
	 * @return A boolean value.
	 */
	public Boolean getBoolean(final String key) {
		return readableNBT.getBoolean(key);
	}

	/**
	 * It returns an ItemStack associated with the given key, or null if the key
	 * does not exist.
	 *
	 * @param key The key of the itemstack you want to get.
	 * @return An ItemStack
	 */
	public ItemStack getItemStack(final String key) {
		return readableNBT.getItemStack(key);
	}

	/**
	 * Get an {@link org.bukkit.inventory.ItemStack} array that was saved at the given key, or null if no
	 * stored data was found
	 *
	 * @param key key
	 * @return The stored {@link org.bukkit.inventory.ItemStack} array, or null if stored data wasn't
	 * found
	 */
	public ItemStack[] getItemStackArray(final String key) {
		return readableNBT.getItemStackArray(key);
	}

	/**
	 * Given a key, return the UUID of the key.
	 *
	 * @param key The key to get the value from
	 * @return A UUID object.
	 */
	public UUID getUUID(final String key) {
		return readableNBT.getUUID(key);
	}

	/**
	 * Checks whether the provided key exists
	 *
	 * @param key String key
	 * @return true, if the key is set
	 */
	public boolean hasTag(final String key) {
		return readableNBT.hasTag(key);
	}

	/**
	 * Checks whether the provided key exists and has the specified type
	 *
	 * @param key  String key
	 * @param type nbt tag type
	 * @return whether the key is set and has the specified type
	 */
	public boolean hasTag(final String key, final NBTType type) {
		return readableNBT.hasTag(key, type);
	}

	/**
	 * @return Set of all stored Keys
	 */
	public Set<String> getKeys() {
		return readableNBT.getKeys();
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved String List
	 */
	public ReadableNBTList<String> getStringList(final String name) {
		return readableNBT.getStringList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Integer List
	 */
	public ReadableNBTList<Integer> getIntegerList(final String name) {
		return readableNBT.getIntegerList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Integer List
	 */
	public ReadableNBTList<int[]> getIntArrayList(final String name) {
		return readableNBT.getIntArrayList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Integer List
	 */
	public ReadableNBTList<UUID> getUUIDList(final String name) {
		return readableNBT.getUUIDList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Float List
	 */
	public ReadableNBTList<Float> getFloatList(final String name) {
		return readableNBT.getFloatList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Double List
	 */
	public ReadableNBTList<Double> getDoubleList(final String name) {
		return readableNBT.getDoubleList(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Long List
	 */
	public ReadableNBTList<Long> getLongList(final String name) {
		return readableNBT.getLongList(name);
	}

	/**
	 * Returns the type of the list, null if not a list
	 *
	 * @param name The key set for this value.
	 * @return Type of list or null if it not a list.
	 */
	@Nullable
	public NBTType getListType(final String name) {
		return readableNBT.getListType(name);
	}

	/**
	 * @param name The key set for this value.
	 * @return The retrieved Compound List
	 */
	public ReadableNBTList<ReadWriteNBT> getCompoundList(final String name) {
		return readableNBT.getCompoundList(name);
	}

	/**
	 * Returns the underlying compound instance provided during construction.
	 *
	 * @return The generic compound instance held by this wrapper.
	 */
	public T getCompound() {
		return this.readableNBT;
	}

	/**
	 * Returns the stored value if exists, or provided value otherwise.
	 * <p>
	 * Supported types:
	 * {@code byte/Byte, short/Short, int/Integer, long/Long, float/Float, double/Double, byte[], int[]},
	 * {@link String}, {@link java.util.UUID}
	 *
	 * @param key          The key to retrieve the set value.
	 * @param defaultValue default non-null value.
	 * @param <E>       the generic class type for the value.
	 * @return Stored or provided value.
	 */
	public <E> E getOrDefault(final String key,@Nonnull final E defaultValue) {
		return readableNBT.getOrDefault(key, defaultValue);
	}

	/**
	 * Returns the stored value if exists, or null.
	 * <p>
	 * Supported types:
	 * {@code Byte, Short, Integer, Long, Float, Double, byte[], int[]},
	 * {@link String}, {@link java.util.UUID}
	 *
	 * @param key  The key to retrieve the set value.
	 * @param type The data type.
	 * @param <E>  the generic class type for the value.
	 * @return Stored or provided value
	 */
	public <E> E getOrNull(final String key,@Nonnull final Class<?> type) {
		return readableNBT.getOrNull(key, type);
	}

	/**
	 * Get an Enum value that has been set via setEnum or setString(key,
	 * value.name()). Passing null/invalid keys will return null.
	 *
	 * @param key  The key to retrive the set value.
	 * @param type The class for the enum.
	 * @param <E>  The generic class type for the enum.
	 * @return the enum set or null if could not cast the enum to that type of class.
	 */
	@Nullable
	public <E extends Enum<E>> E getEnum(final String key, final Class<E> type) {
		return readableNBT.getEnum(key, type);
	}

	/**
	 * @param name The key set for this value.
	 * @return The type of the given stored key or null
	 */
	@Nullable
	public NBTType getType(final String name) {
		return this.readableNBT.getType(name);
	}

	/**
	 * Write the content of this Compound into the provided stream.
	 *
	 * @param stream the stream to write the data from.
	 */
	public void writeCompound(final OutputStream stream) {
		readableNBT.writeCompound(stream);
	}
}
