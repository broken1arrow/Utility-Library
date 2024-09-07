package org.broken.arrow.nbt.library;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.logging.library.Validate.ValidateExceptions;
import org.broken.arrow.nbt.library.utility.NBTDataWriterWrapper;
import org.broken.arrow.nbt.library.utility.NBTReaderWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.broken.arrow.nbt.library.RegisterNbtAPI.isHasScoreboardTags;
import static org.broken.arrow.nbt.library.utility.ConvertObjectType.setNBTValue;


/**
 * Utility class for persistent metadata manipulation
 * <p>&nbsp;</p>
 * <p>
 * We apply scoreboard tags to ensure permanent metadata storage if supported,
 * otherwise it is lost on reload.
 * </p>
 * <p>&nbsp;</p>
 */
public final class CompMetadata {

	/**
	 * The tag delimiter
	 */
	private static final String DELIMITER = "%-%";
	private static final float SERVER_VERSION;
	private final Plugin plugin;
	private static final String READING_NULL_ITEM = "Reading NBT tag got null item";
	private static  final String ITEM_IS_NULL = "Setting NBT tag got null item";
	private static  final String BLOCK_STATE = "BlockState must be instance of a TileState not ";

	public CompMetadata(Plugin plugin) {
		this.plugin = plugin;
	}

	static {
		final String[] versionPieces = Bukkit.getServer().getBukkitVersion().split("\\.");
		final String firstNumber;
		String secondNumber;
		final String firstString = versionPieces[1];
		if (firstString.contains("-")) {
			firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

			secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
			final int index = secondNumber.toUpperCase().indexOf("R");
			if (index >= 0) secondNumber = secondNumber.substring(index + 1);
		} else {
			final String secondString = versionPieces[2];
			firstNumber = firstString;
			secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
		}
		SERVER_VERSION = Float.parseFloat(firstNumber + "." + secondNumber);
	}
	// ----------------------------------------------------------------------------------------
	// Setting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut for setting a tag with a key-value pair on an item.
	 * <p>&nbsp;</p>
	 * <p>
	 * For a list of values that can be retrieved from the NBT, please refer to the "See Also" section.
	 * For other classes not supported by this class, you may need to handle serialization and deserialization
	 * of the values manually.
	 * </p>
	 *
	 * @param item  you want to set metadata on.
	 * @param key   The key you want to set on this item.
	 * @param value The value you want to set on this item, it will try convert it
	 *              to right class, if not fund it will be converted to string.
	 * @return The original itemStack with the metadata set.
	 * @see org.broken.arrow.nbt.library.utility.NBTReaderWrapper
	 */
	public ItemStack setMetadata(@Nonnull final ItemStack item, @Nonnull final String key, @Nonnull final Object value) {
		Validate.checkNotNull(item, ITEM_IS_NULL);

		return NBT.modify(item, writeItemNBT -> {
			ReadWriteNBT compound = writeItemNBT.getOrCreateCompound(this.getCompoundKey());
			if (compound != null) {
				setNBTValue(compound, key, value);
			}
			return item;
		});

	}

	/**
	 * A shortcut for setting a tag with a key-value pair on an item using the provided Consumer.
	 * The tag will be written only if the compound is not null.
	 * <p>&nbsp;</p>
	 * <p>
	 * For classes not supported by NBT, you can manually serialize and when retrieve the values you deserialize it.
	 * Consider setting the serialized data as a string for such cases.
	 * </p>
	 *
	 * @param item     The ItemStack you want to set metadata on.
	 * @param writeNBT The NBT data consumer to set several metadata values.
	 * @return The original itemStack with the metadata set.
	 */
	public ItemStack setMetadata(@Nonnull final ItemStack item, @Nonnull Consumer<NBTDataWriterWrapper> writeNBT) {
		Validate.checkNotNull(item, ITEM_IS_NULL);

		return NBT.modify(item, writeItemNBT -> {
			ReadWriteNBT compound = writeItemNBT.getOrCreateCompound(this.getCompoundKey());
			if (compound != null) {
				writeNBT.accept(new NBTDataWriterWrapper(compound));
			}
			return item;
		});
	}

	/**
	 * A shortcut for setting several tags with key-value pairs on an item. It will attempt
	 * to cast the values to the correct data types. If a matching NBT supported class is not
	 * found, the values will be cast to strings.
	 * <p>&nbsp;</p>
	 * <p>
	 * For a list of values that can be retrieved from the NBT, please refer to the "See Also" section.
	 * For other classes not supported by this class, you may need to handle serialization and deserialization
	 * of the values manually.
	 * </p>
	 *
	 * @param item   The item on which you want to set metadata.
	 * @param nbtMap A map containing all the key-value pairs you want to set.
	 * @return the original itemStack with the metadata set.
	 * @see org.broken.arrow.nbt.library.utility.NBTReaderWrapper
	 */
	public ItemStack setAllMetadata(@Nonnull final ItemStack item, @Nonnull final Map<String, Object> nbtMap) {
		Validate.checkNotNull(item, ITEM_IS_NULL);
		Validate.checkNotNull(nbtMap, "The map with nbt should not be null");

		return NBT.modify(item, nbt -> {
			ReadWriteNBT compound = nbt.getOrCreateCompound(this.getCompoundKey());
			if (compound != null) {
				for (Entry<String, Object> entry : nbtMap.entrySet()) {
					Object value = entry.getValue();
					setNBTValue(compound, entry.getKey(), value);
				}
			}
			return item;
		});
	}

	/**
	 * Attempts to set a persistent metadata for entity
	 *
	 * @param entity you want to set metadata on.
	 * @param tag    you want to set on this entity.
	 */
	public void setMetadata(@Nonnull final Entity entity, @Nonnull final String tag) {
		setMetadata(entity, tag, tag);
	}

	/**
	 * Attempts to set a persistent metadata tag with value for entity
	 *
	 * @param entity you want to set metadata on.
	 * @param key    you want to set on this entity.
	 * @param value  you want to set on this entity.
	 */
	public void setMetadata(@Nonnull final Entity entity, @Nonnull final String key, @Nonnull final String value) {
		Validate.checkNotNull(entity);

		if (SERVER_VERSION >= 1.14F) {
			this.setPersistentMetadata(entity, key, value);
		} else {
			entity.setMetadata(key, new FixedMetadataValue(plugin, value));
		}
	}

	/**
	 * Sets persistent tile entity metadata
	 *
	 * @param tileEntity you want set metadata.
	 * @param key        you want to set on this tileEntity.
	 * @param value      you want to set on this tileEntity.
	 */
	public void setMetadata(@Nonnull final BlockState tileEntity, @Nonnull final String key, @Nonnull final String value) {
		Validate.checkNotNull(tileEntity);
		Validate.checkNotNull(key);
		Validate.checkNotNull(value);

		if (SERVER_VERSION >= 1.14F) {
			Validate.checkBoolean(tileEntity instanceof TileState,
					BLOCK_STATE + tileEntity);

			this.setPersistentMetadata(tileEntity, key, value);
			tileEntity.update(true);
		} else {
			tileEntity.setMetadata(key, new FixedMetadataValue(plugin, value));
			tileEntity.update(true);
		}
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent and is
	 * removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(org.bukkit.entity.Entity, String)} to set persistent
	 * custom tags for entities.
	 *
	 * @param entity you want set metadata.
	 * @param tag    you want to set.
	 */
	public void setTempMetadata(@Nonnull final Entity entity, @Nonnull final String tag) {
		entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(plugin, tag));
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent and is
	 * removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(org.bukkit.entity.Entity, String)} to set persistent
	 * custom tags for entities.
	 *
	 * @param entity you want set metadata.
	 * @param tag    you want to set.
	 * @param key    you want to set.
	 */
	public void setTempMetadata(@Nonnull final Entity entity, @Nonnull final String tag, @Nonnull final Object key) {
		entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(plugin, key));
	}

	// ----------------------------------------------------------------------------------------
	// Getting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut from reading a certain key from an item's given compound tag
	 *
	 * @param itemStack you want get metadata.
	 * @param key       to get the metadata on item.
	 * @return metadata value.
	 */
	@Nullable
	public String getMetadata(@Nonnull final ItemStack itemStack, @Nonnull final String key) {
		return this.getMetadata(itemStack,nbtWrapper -> {
			if (nbtWrapper != null) {
				return nbtWrapper.getString(key);
			}
			return null;
		});
	}

    /**
     * Retrieve specific NBT data from an item's compound tag.
     * <p>&nbsp;</p>
     * <p>
     * This method gives you access to retrieve multiple set tags inside
     * the compound tag without creating a new {@link NBTItem} instance for
     * every retrieval of a value.
     * </p>
     * <p>&nbsp;</p>
     *
     * @param item     The item from which to retrieve metadata.
     * @param function The function that return NBT values you can read on your item
     *                 and have your own return type.
     * @param <T>      type of class the return value.
     * @return The NBTReaderWrapper instance if the compound key was found and applied, null otherwise.
     */
	@Nullable
	public <T> T getMetadata(@Nonnull final ItemStack item, Function<NBTReaderWrapper, T> function) {
		Validate.checkNotNull(item, this.setMessageItemNull());
		if (item.getType() == Material.AIR)
			return null;

		final String compoundTag = getCompoundKey();
		return NBT.get(item, nbt -> {
			final boolean hasTag = nbt.hasTag(compoundTag);
			if (hasTag) {
				ReadableNBT compound = nbt.getCompound(compoundTag);
				if (compound != null) {
					T returnedObject = function.apply(new NBTReaderWrapper(compound));
					if (returnedObject instanceof NBTReaderWrapper)
						throw new ValidateExceptions("You can't return NBTReaderWrapper instance, because it will be closed after this call.");
					return getOrNull (returnedObject);
				}
			}
			return null;
		});
	}

	/**
	 * Retrieve specific NBT data from an item's compound tag.
	 * <p>&nbsp;</p>
	 * <p>
	 * This method gives you access to retrieve multiple set tags inside
	 * the compound tag without creating a new {@link NBTItem} instance for
	 * every retrieval of a value.
	 * </p>
	 * <p>&nbsp;</p>
	 *
	 * @param item     The item from which to retrieve metadata.
	 * @param consumer The consumer that return NBT values you can read on your item.
	 */
	public void getMetadata(@Nonnull final ItemStack item, Consumer<NBTReaderWrapper> consumer) {
		Validate.checkNotNull(item, this.setMessageItemNull());
		if (item.getType() == Material.AIR)
			return;

		final String compoundTag = getCompoundKey();
		NBT.get(item, nbt -> {
			final boolean hasTag = nbt.hasTag(compoundTag);
			if (hasTag) {
				ReadableNBT compound = nbt.getCompound(compoundTag);
				if (compound != null) {
					consumer.accept(new NBTReaderWrapper(compound));
				}
			}
		});
	}

	/**
	 * Attempts to get the entity's metadata, first from scoreboard tag, second from
	 * Bukkit metadata
	 *
	 * @param entity you want get metadata.
	 * @param key    to get the metadata on item.
	 * @return the value, or null.
	 */
	@Nullable
	public String getMetadata(@Nonnull final Entity entity, @Nonnull final String key) {
		Validate.checkNotNull(entity);

		if (isHasScoreboardTags()) {
			for (final String line : entity.getScoreboardTags()) {
				final String tag = getTag(line, key);

				if (tag != null && !tag.isEmpty())
					return tag;
			}

		}
		if (SERVER_VERSION >= 1.14F) {
			return getPersistentMetadata(entity, key);
		}
		final String value = entity.hasMetadata(key) ? entity.getMetadata(key).get(0).asString() : null;

		return getOrNull(value);
	}

	/**
	 * Return saved tile entity metadata, or null if none
	 *
	 * @param tileEntity you want get metadata.
	 * @param key        to get the metadata on item.
	 * @return the value, or null.
	 */
	@Nullable
	public String getMetadata(@Nonnull final BlockState tileEntity, @Nonnull final String key) {
		Validate.checkNotNull(tileEntity);
		Validate.checkNotNull(key);


		if (SERVER_VERSION >= 1.14F) {
			Validate.checkBoolean(tileEntity instanceof TileState,
					BLOCK_STATE + tileEntity);

			return this.getPersistentMetadata( tileEntity, key) ;
		}
		final String value = tileEntity.hasMetadata(key) ? tileEntity.getMetadata(key).get(0).asString() : null;

		return getOrNull(value);
	}

	/**
	 * Return entity metadata value or null if has none
	 * <p>
	 * Only usable if you set it using the
	 * {@link #setTempMetadata(org.bukkit.entity.Entity, String, Object)} with the
	 * key parameter because otherwise the tag is the same as the value we return
	 *
	 * @param entity you want get metadata.
	 * @param tag    you want to set.
	 * @return the value you set or null.
	 */
	@Nullable
	public MetadataValue getTempMetadata(@Nonnull final Entity entity, @Nonnull final String tag) {
		final String key = createTempMetadataKey(tag);

		return entity.hasMetadata(key) ? entity.getMetadata(key).get(0) : null;
	}

	// Parses the tag and gets its value
	private String getTag(final String raw, final String key) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(plugin.getName()) && parts[1].equals(key) ? parts[2] : null;
	}

	// ----------------------------------------------------------------------------------------
	// Checking for metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * Return true if the given itemStack has the given key stored at its compound
	 * tag
	 *
	 * @param item you want to check.
	 * @param key  you want to check this item have.
	 * @return true if it has this key.
	 */
	public boolean hasMetadata(@Nonnull final ItemStack item, @Nonnull final String key) {
		Validate.checkBoolean(true, "NBT ItemStack tags only support MC 1.7.10+");
		Validate.checkNotNull(item);

		if (item.getType() == Material.AIR)
			return false;

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.getCompound(getCompoundKey());

		return tag != null && tag.hasKey(key);
	}

	/**
	 * Returns if the entity has the given tag by key, first checks scoreboard tags,
	 * and then bukkit metadata
	 *
	 * @param entity you want to check.
	 * @param key    you want to check this entity have.
	 * @return true if it has this key.
	 */
	public boolean hasMetadata(final Entity entity, final String key) {
		Validate.checkNotNull(entity);
		if (isHasScoreboardTags())
			for (final String line : entity.getScoreboardTags())
				if (hasTag(line, key))
					return true;

		return entity.hasMetadata(key);
	}

	/**
	 * Return true if the given tile entity block such as
	 * {@link org.bukkit.block.CreatureSpawner} has the given key
	 *
	 * @param tileEntity you want to check.
	 * @param key        you want to check this tileEntity have.
	 * @return true if it has this key.
	 */
	public boolean hasMetadata(final BlockState tileEntity, final String key) {
		Validate.checkNotNull(tileEntity);
		Validate.checkNotNull(key);

		if (SERVER_VERSION >= 1.14F) {
			Validate.checkBoolean(tileEntity instanceof TileState,
					BLOCK_STATE + tileEntity);

			return hasNameSpacedKey((TileState) tileEntity, key);
		}


		return tileEntity.hasMetadata(key);
	}

	/**
	 * Returns if the player has the given tag.
	 *
	 * @param player you want to check
	 * @param tag    you want to check if player have.
	 * @return true if player has it.
	 */
	public boolean hasTempMetadata(final Entity player, final String tag) {
		return player.hasMetadata(createTempMetadataKey(tag));
	}

	// Parses the tag and gets its value
	private boolean hasTag(final String raw, final String tag) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(plugin.getName()) && parts[1].equals(tag);
	}

	/**
	 * Remove temporary metadata from the entity
	 *
	 * @param player you want to remove metadata.
	 * @param tag    you want to remove.
	 */
	public void removeTempMetadata(final Entity player, final String tag) {
		final String key = createTempMetadataKey(tag);

		if (player.hasMetadata(key))
			player.removeMetadata(key, plugin);
	}

	/*
	 * Create a new temporary metadata key
	 */
	private String createTempMetadataKey(final String tag) {
		return plugin.getName() + "_" + tag;
	}

	public String getCompoundKey() {
		return plugin.getName() + "_NBT";
	}

	/**
	 * If the String equals to none or is empty, return null
	 *
	 * @param input string to check.
	 * @return input or null
	 */
	public static String getOrNull(final String input) {
		return input == null || "none".equalsIgnoreCase(input) || input.isEmpty() ? null : input;
	}

	/**
	 * If the type equals to none or is empty, return null
	 *
	 * @param input to check.
	 * @param <T>   the class type.
	 * @return input or null
	 */
	public static <T> T getOrNull(final T input) {
		return input == null || "none".equalsIgnoreCase(input.toString()) || input.toString().isEmpty() ? null : input;
	}

	// Format the syntax of stored tags
	private String format(final String key, final String value) {
		return plugin.getName() + DELIMITER + key + DELIMITER + value;
	}

	private void setNameSpacedKey(final TileState tile, final String key, final String value) {
		tile.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
	}

	private boolean hasNameSpacedKey(final TileState tile, final String key) {
		return tile.getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
	}

	/**
	 * Sets the message when item is null.
	 *
	 * @return the message to set when item is null.
	 */
	public String setMessageItemNull(){
		return READING_NULL_ITEM;
	}

	/**
	 * Returns persistent metadata with our plugin assigned as namedspaced key for MC 1.14+
	 */
	private String getPersistentMetadata(final Object entity, final String key) {
		Validate.checkBoolean(entity instanceof PersistentDataHolder, "Can only use CompMetadata#setMetadata(" + key + ") for persistent data holders, got " + entity.getClass());
		final PersistentDataContainer data = ((PersistentDataHolder) entity).getPersistentDataContainer(); // Prevents no class def error on legacy MC

		return getOrNull(data.get(new NamespacedKey(plugin, key), PersistentDataType.STRING));
	}

	/**
	 * Sets persistent metadata with our plugin assigned as namedspaced key for MC 1.14+
	 */
	private  void setPersistentMetadata(final Object entity, final String key, final String value) {
		Validate.checkBoolean(!(entity instanceof PersistentDataHolder), "Can only use CompMetadata#setMetadata(" + key + ") for persistent data holders, got " + entity.getClass());

		final PersistentDataContainer data = ((PersistentDataHolder) entity).getPersistentDataContainer();
		final boolean remove = value == null || value.isEmpty();

		if (remove)
			data.remove(new NamespacedKey(plugin, key));
		else
			data.set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
	}
}
