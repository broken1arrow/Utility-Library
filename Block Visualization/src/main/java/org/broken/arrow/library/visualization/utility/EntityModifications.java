package org.broken.arrow.library.visualization.utility;

import de.tr7zw.changeme.nbtapi.NBTEntity;
import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;

/**
 * Handles spawning and modifying block-related entities, such as falling blocks,
 * with compatibility across different Minecraft server versions.
 */
public class EntityModifications {

	private final float serverVersion;
	private final Logging log = new Logging(EntityModifications.class);

	/**
	 * Creates a new entity modifications handler.
	 *
	 * @param serverVersion the current server version, used to determine which methods are available.
	 */
	public EntityModifications(float serverVersion) {
		this.serverVersion = serverVersion;
	}

	/**
	 * Spawns a falling block at the given location.
	 * <p>
	 * If the server version is too old to support falling block spawning,
	 * this method returns {@code null}.
	 * </p>
	 *
	 * @param location the location to spawn the block at.
	 * @param mask     the material mask to apply to the block.
	 * @param text     the custom name text to display above the block, or {@code null} for none.
	 * @return a {@link FallingBlock} instance, or {@code null} if unsupported.
	 */
	public FallingBlock spawnFallingBlock(@Nonnull final Location location, @Nonnull final Material mask, @Nullable final String text) {
		if (serverVersion < 9) {
			return null;
		} else {
			final FallingBlock falling = spawnFallingBlock(location.clone().add(0.5D, 0.0, 0.5D), mask);
			falling.setDropItem(false);
			falling.setVelocity(new Vector(0, 0, 0));
			setCustomName(falling, text);
			apply(falling, true);
			return falling;
		}
	}

	/**
	 * Sets a custom name on the given entity.
	 *
	 * @param en   the entity to name.
	 * @param name the name to set, or {@code null} / empty to skip setting a name.
	 */
	public void setCustomName(@Nonnull final Entity en, final String name) {
		try {
			en.setCustomNameVisible(true);
			if (name != null && !name.equals(""))
				en.setCustomName(TextTranslator.toSpigotFormat(name));
		} catch (final NoSuchMethodError ignored) {
			log.log(Level.INFO,()-> "Could not set name on the entity " + en.getName());
		}

	}

	/**
	 * Applies gravity and glowing settings to an entity or falling block.
	 * <p>
	 * On older server versions, NBT tags are used to disable gravity. On newer versions,
	 * built-in API methods are used.
	 * </p>
	 *
	 * @param object the entity or falling block to modify.
	 * @param key    {@code true} to disable gravity and enable glowing, {@code false} to restore defaults.
	 */
	public void apply(@Nonnull final Object object, final boolean key) {
		if (object instanceof Entity) {
			final Entity entity = ((Entity) object);
			if (this.serverVersion < 13) {
				final NBTEntity nbtEntity = new NBTEntity((Entity) object);
				nbtEntity.setInteger("NoGravity", !key ? 0 : 1);
				entity.setGlowing(key);
			} else {
				entity.setGravity(!key);
				entity.setGlowing(key);
			}
		}
		setFallingBlockData(object, key);
	}

	/**
	 * Sets gravity and glowing flags for a falling block.
	 * <p>
	 * On older server versions, NBT tags are used. On newer versions, built-in API methods are used.
	 * </p>
	 *
	 * @param instance the falling block to modify.
	 * @param key      {@code true} to disable gravity and enable glowing, {@code false} to restore defaults.
	 */
	private void setFallingBlockData(@Nonnull final Object instance, final boolean key) {
		if (instance instanceof FallingBlock) {
			FallingBlock entity = ((FallingBlock) instance);
			if (this.serverVersion < 13) {
				NBTEntity nbtEntity = new NBTEntity((Entity) instance);
				nbtEntity.setInteger("NoGravity", key ? 0 : 1);
				entity.setGlowing(key);
			}else {
				entity.setGravity(!key);
				entity.setGlowing(key);
			}
		}
	}

	/**
	 * Spawns a falling block with the given material.
	 *
	 * @param loc      the spawn location.
	 * @param material the block material.
	 * @return the spawned falling block, or {@code null} if unsupported.
	 */
	private FallingBlock spawnFallingBlock(final Location loc, final Material material) {
		return spawnFallingBlock(loc, material, (byte) 0);
	}

	/**
	 * Spawns a falling block with the given material and data value.
	 *
	 * @param loc      the spawn location.
	 * @param material the block material.
	 * @param data     the legacy data value for the block.
	 * @return the spawned falling block, or {@code null} if unsupported.
	 */
	private FallingBlock spawnFallingBlock(final Location loc, final Material material, final byte data) {
		if (loc.getWorld() == null) return null;

		if (this.serverVersion >= 13) {
			if (this.serverVersion >= 16)
				return loc.getWorld().spawnFallingBlock(loc, material.createBlockData());
			else return loc.getWorld().spawnFallingBlock(loc, material, data);
		} else {
			try {
				return (FallingBlock) loc.getWorld().getClass().getMethod("spawnFallingBlock", Location.class, Integer.TYPE, Byte.TYPE).invoke(loc.getWorld(), loc, material.getId(), data);
			} catch (final ReflectiveOperationException error) {
				log.logError(error,() -> "Could create a falling block. You probably trying this on to old version of Minecraft as it does only supports 1.8.8 and up, your version: " + serverVersion);
				return null;
			}
		}
	}
}
