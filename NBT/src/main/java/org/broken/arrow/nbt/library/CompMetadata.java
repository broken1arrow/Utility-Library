package org.broken.arrow.nbt.library;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.broken.arrow.nbt.library.utility.NBTDataWriterWrapper;
import org.broken.arrow.nbt.library.utility.NBTValueWrapper;
import org.broken.arrow.nbt.library.utility.ServerVersion;
import org.broken.arrow.nbt.library.utility.Valid;
import org.broken.arrow.nbt.library.utility.Valid.CatchExceptions;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.broken.arrow.nbt.library.utility.ConvertObjectType.setNBTValue;
import static org.broken.arrow.nbt.library.utility.ServerVersion.isHasScoreboardTags;


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
	private final Plugin plugin;

	public CompMetadata(Plugin plugin) {
		this.plugin = plugin;
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
	 * @see org.broken.arrow.nbt.library.utility.NBTValueWrapper
	 */
	public ItemStack setMetadata(@Nonnull final ItemStack item, @Nonnull final String key, @Nonnull final Object value) {
		Valid.checkNotNull(item, "Setting NBT tag got null item");

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
		Valid.checkNotNull(item, "Setting NBT tag got null item");

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
	 * @see org.broken.arrow.nbt.library.utility.NBTValueWrapper
	 */
	public ItemStack setAllMetadata(@Nonnull final ItemStack item, @Nonnull final Map<String, Object> nbtMap) {
		Valid.checkNotNull(item, "Setting NBT tag got null item");
		Valid.checkNotNull(nbtMap, "The map with nbt should not be null");

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
		Valid.checkNotNull(entity);

		final String tag = format(key, value);

		if (isHasScoreboardTags()) {
			if (!entity.getScoreboardTags().contains(tag))
				entity.addScoreboardTag(tag);

		} else {
			entity.setMetadata(key, new FixedMetadataValue(plugin, value));

			MetadataFile.getInstance().addMetadata(entity, key, value);
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
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);
		Valid.checkNotNull(value);

		if (ServerVersion.atLeast(ServerVersion.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState,
					"BlockState must be instance of a TileState not " + tileEntity);

			setNameSpacedKey((TileState) tileEntity, key, value);
			tileEntity.update();

		} else {
			tileEntity.setMetadata(key, new FixedMetadataValue(plugin, value));
			tileEntity.update();

			MetadataFile.getInstance().addMetadata(tileEntity, key, value);
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
	 * A shortcut from reading a certain key from an item's given compound tag
	 *
	 * @param item  you want get metadata.
	 * @param clazz class you set as value.
	 * @param key   to get the metadata on item.
	 * @param <T>   type of class the value
	 * @return metadata value.
	 * @deprecated use {@link #getMetadata(org.bukkit.inventory.ItemStack, String)}
	 */
	@Deprecated
	@Nullable
	public <T> T getMetadata(@Nonnull final ItemStack item, @Nonnull Class<T> clazz, @Nonnull final String key) {
		Valid.checkNotNull(item, this.setMessageItemNull());
		if (item.getType() == Material.AIR)
			// if (item == null || CompMaterial.isAir(item.getType()))
			return null;

		final String compoundTag = getCompoundKey();
		final NBTItem nbt = new NBTItem(item);
		final boolean hasTag = nbt.hasTag(compoundTag);
		final NBTCompound compound = hasTag ? nbt.getCompound(compoundTag) : null;
		T value = null;

		if (compound != null) {
			if (clazz.isInstance(String.class))
				value = clazz.cast(compound.getString(key));
			else
				value = compound.getObject(key, clazz);
		}

		return getOrNull(value);
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
	 * @return The NBTValueWrapper instance if the compound key was found and applied, null otherwise.
	 */
	@Nullable
	public <T> T getMetadata(@Nonnull final ItemStack item, Function<NBTValueWrapper, T> function) {
		Valid.checkNotNull(item, this.setMessageItemNull());
		if (item.getType() == Material.AIR)
			return null;

		final String compoundTag = getCompoundKey();
		return NBT.get(item, nbt -> {
			final boolean hasTag = nbt.hasTag(compoundTag);
			if (hasTag) {
				ReadableNBT compound = nbt.getCompound(compoundTag);
				if (compound != null) {
					T returnedObject = function.apply(new NBTValueWrapper(compound));
					if (returnedObject instanceof NBTValueWrapper)
						throw new CatchExceptions("You can't return NBTValueWrapper instance, because it will be closed after this call.");
					return returnedObject;
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
	public void getMetadata(@Nonnull final ItemStack item, Consumer<NBTValueWrapper> consumer) {
		Valid.checkNotNull(item, this.setMessageItemNull());
		if (item.getType() == Material.AIR)
			return;

		final String compoundTag = getCompoundKey();
		NBT.get(item, nbt -> {
			final boolean hasTag = nbt.hasTag(compoundTag);
			if (hasTag) {
				ReadableNBT compound = nbt.getCompound(compoundTag);
				if (compound != null) {
					consumer.accept(new NBTValueWrapper(compound));
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
		Valid.checkNotNull(entity);

		if (false)
			// if (Remain.hasScoreboardTags())
			for (final String line : entity.getScoreboardTags()) {
				final String tag = getTag(line, key);

				if (tag != null && !tag.isEmpty())
					return tag;
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
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);


		if (ServerVersion.atLeast(ServerVersion.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState,
					"BlockState must be instance of a TileState not " + tileEntity);

			return getNameSpacedKey((TileState) tileEntity, key);
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

	private String getNameSpacedKey(final TileState tile, final String key) {
		final String value = tile.getPersistentDataContainer().get(new NamespacedKey(plugin, key),
				PersistentDataType.STRING);

		return getOrNull(value);
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
		Valid.checkBoolean(true, "NBT ItemStack tags only support MC 1.7.10+");
		Valid.checkNotNull(item);

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
		Valid.checkNotNull(entity);
		if (true)
			// if (Remain.hasScoreboardTags())
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
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);

		if (ServerVersion.atLeast(ServerVersion.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState,
					"BlockState must be instance of a TileState not " + tileEntity);

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

	private String setMessageItemNull(){
		return"Reading NBT tag got null item";
	}

	/**
	 * Note!!! This is not implemented.
	 * <p>
	 * Due to lack of persistent metadata implementation until Minecraft 1.14.x, we
	 * simply store them in a file during server restart and then apply as a
	 * temporary metadata for the Bukkit entities.
	 * <p>
	 * internal use only
	 */
	public static final class MetadataFile {// extends YamlSectionConfig {

		private static volatile Object LOCK = new Object();

		private static final MetadataFile instance = new MetadataFile();

		public static MetadataFile getInstance() {
			return instance;
		}

		public void addMetadata(BlockState tileEntity, String key, String value) {
		}

		public void addMetadata(Entity tileEntity, String key, String value) {
		}
		/*
		 * private final StrictMap<UUID, List<String>> entityMetadataMap = new
		 * StrictMap<>(); private final StrictMap<Location, BlockCache> blockMetadataMap
		 * = new StrictMap<>();
		 *
		 * private MetadataFile() { super("Metadata");
		 *
		 * loadConfiguration(NO_DEFAULT, FoConstants.File.DATA); }
		 *
		 * @Override protected void onLoadFinish() { synchronized (LOCK) {
		 * loadEntities(); loadBlockStates();
		 *
		 * save(); } }
		 *
		 * private void loadEntities() { synchronized (LOCK) {
		 * entityMetadataMap.clear();
		 *
		 * for (final String uuidName : getMap("Entity").keySet()) { final UUID uuid =
		 * UUID.fromString(uuidName);
		 *
		 * // Remove broken key if (!(getObject("Entity." + uuidName) instanceof List))
		 * { setNoSave("Entity." + uuidName, null);
		 *
		 * continue; }
		 *
		 * final List<String> metadata = getStringList("Entity." + uuidName); final
		 * Entity entity = Remain.getEntity(uuid);
		 *
		 * // Check if the entity is still real if (!metadata.isEmpty() && entity !=
		 * null && entity.isValid() && !entity.isDead()) { entityMetadataMap.put(uuid,
		 * metadata);
		 *
		 * applySavedMetadata(metadata, entity); } }
		 *
		 * save("Entity", this.entityMetadataMap); } }
		 *
		 * private void loadBlockStates() { synchronized (LOCK) {
		 * blockMetadataMap.clear();
		 *
		 * for (final String locationRaw : getMap("Block").keySet()) { final Location
		 * location = SerializeUtil.deserializeLocation(locationRaw); final BlockCache
		 * blockCache = get("Block." + locationRaw, BlockCache.class);
		 *
		 * final Block block = location.getBlock();
		 *
		 * // Check if the block remained the same if (!CompMaterial.isAir(block) &&
		 * CompMaterial.fromBlock(block) == blockCache.getType()) {
		 * blockMetadataMap.put(location, blockCache);
		 *
		 * applySavedMetadata(blockCache.getMetadata(), block); } }
		 *
		 * save("Block", this.blockMetadataMap); } }
		 *
		 * private void applySavedMetadata(final List<String> metadata, final
		 * Meta table entity) { synchronized (LOCK) { for (final String metadataLine :
		 * metadata) { if (metadataLine.isEmpty()) continue;
		 *
		 * final String[] lines = metadataLine.split(DELIMITER);
		 * Valid.checkBoolean(lines.length == 3, "Malformed metadata line for " + entity
		 * + ". Length 3 != " + lines.length + ". Data: " + metadataLine);
		 *
		 * final String key = lines[1]; final String value = lines[2];
		 *
		 * entity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(),
		 * value)); } } }
		 *
		 * protected void addMetadata(final Entity entity, @Nonnull final String key,
		 * final String value) { synchronized (LOCK) { final List<String> metadata =
		 * entityMetadataMap.getOrPut(entity.getUniqueId(), new ArrayList<>());
		 *
		 * for (final Iterator<String> i = metadata.iterator(); i.hasNext(); ) { final
		 * String meta = i.next();
		 *
		 * if (getTag(meta, key) != null) i.remove(); }
		 *
		 * if (value != null && !value.isEmpty()) { final String formatted = format(key,
		 * value);
		 *
		 * metadata.add(formatted); }
		 *
		 * save("Entity", entityMetadataMap); } }
		 *
		 * protected void addMetadata(final BlockState blockState, final String key,
		 * final String value) { synchronized (LOCK) { final BlockCache blockCache =
		 * blockMetadataMap.getOrPut(blockState.getLocation(), new
		 * BlockCache(CompMaterial.fromBlock(blockState.getBlock()), new
		 * ArrayList<>()));
		 *
		 * for (final Iterator<String> i = blockCache.getMetadata().iterator();
		 * i.hasNext(); ) { final String meta = i.next();
		 *
		 * if (getTag(meta, key) != null) i.remove(); }
		 *
		 * if (value != null && !value.isEmpty()) { final String formatted = format(key,
		 * value);
		 *
		 * blockCache.getMetadata().add(formatted); }
		 *
		 * { // Save for (final Map.Entry<Location, BlockCache> entry :
		 * blockMetadataMap.entrySet()) setNoSave("Block." +
		 * SerializeUtil.serializeLoc(entry.getKey()), entry.getValue().serialize());
		 *
		 * save(); } }
		 */
	}
	/*
	 * @Getter
	 *
	 * @RequiredArgsConstructor public static final class BlockCache implements
	 * ConfigSerializable { private final CompMaterial type; private final
	 * List<String> metadata;
	 *
	 * public static BlockCache deserialize(final SerializedMap map) { final
	 * CompMaterial type = map.getMaterial("Type"); final List<String> metadata =
	 * map.getStringList("Metadata");
	 *
	 * return new BlockCache(type, metadata); }
	 *
	 * @Override public SerializedMap serialize() { final SerializedMap map = new
	 * SerializedMap();
	 *
	 * map.put("Type", type.toString()); map.put("Metadata", metadata);
	 *
	 * return map; } }
	 *
	 * public static void onReload() { instance = new MetadataFile(); } }
	 */
}
