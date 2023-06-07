package org.broken.arrow.visualization.library.utility;

import de.tr7zw.nbtapi.NBTEntity;
import org.broken.arrow.color.library.TextTranslator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityModifications {

	private final float serverVersion;

	public EntityModifications(float serverVersion) {
		this.serverVersion = serverVersion;
	}

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

	public void setCustomName(final Entity en, final String name) {
		try {
			en.setCustomNameVisible(true);
			if (name != null && !name.equals(""))
				en.setCustomName(TextTranslator.toSpigotFormat(name));
		} catch (final NoSuchMethodError ignored) {
		}

	}

	public void apply(@Nonnull final Object instance, final boolean key) {
	/*	if (instance instanceof FallingBlock) {
			FallingBlock entity = ((FallingBlock) instance);
			if (ServerVersion.olderThan(ServerVersion.v1_13)) {
				menulibrary.dependencies.nbt.nbtapi.NBTEntity nbtEntity = new menulibrary.dependencies.nbt.nbtapi.NBTEntity((Entity) instance);
				nbtEntity.setInteger("NoGravity", key ? 0 : 1);
				entity.setGlowing(key);
			}else {
				System.out.println("instance  fffffff");
				entity.setGravity(!key);
				entity.setGlowing(key);
			}
		}*/
		if (instance instanceof Entity) {
			final Entity entity = ((Entity) instance);
			if (this.serverVersion < 13) {
				final NBTEntity nbtEntity = new NBTEntity((Entity) instance);
				nbtEntity.setInteger("NoGravity", !key ? 0 : 1);
				entity.setGlowing(key);
			} else {
				entity.setGravity(!key);
				entity.setGlowing(key);
			}
		}
	}

	private FallingBlock spawnFallingBlock(final Location loc, final Material material) {
		return spawnFallingBlock(loc, material, (byte) 0);
	}

	private FallingBlock spawnFallingBlock(final Location loc, final Material material, final byte data) {
		if (loc.getWorld() == null) return null;

		if (this.serverVersion >= 13) {
			if (this.serverVersion >= 16)
				return loc.getWorld().spawnFallingBlock(loc, material.createBlockData());
			else return loc.getWorld().spawnFallingBlock(loc, material, data);
		} else {
			try {
				return (FallingBlock) loc.getWorld().getClass().getMethod("spawnFallingBlock", Location.class, Integer.TYPE, Byte.TYPE).invoke(loc.getWorld(), loc, material.getId(), data);
			} catch (final ReflectiveOperationException var4) {
				var4.printStackTrace();
				return null;
			}
		}
	}
}
