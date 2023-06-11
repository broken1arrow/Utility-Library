package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import org.broken.arrow.serialize.library.utility.Validate;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is responsible for creating particles in Minecraft. It automatically detects the particle type
 * and uses the appropriate method based on the Minecraft version. It is compatible with Minecraft versions 1.8
 * to the latest version.
 */
public class ParticleCreator {

	private final Effect effect;
	private final Particle particle;
	private final Material material;
	private final Class<?> dataType;
	private int count;
	private double offsetX;
	private double offsetY;
	private double offsetZ;
	private final int data;
	private final ParticleDustOptions particleDustOptions;
	private final Player player;
	private final World world;
	private final double x;
	private final double y;
	private final double z;

	/**
	 * Creates a particle effect at the specified location for the given player.
	 *
	 * @param player   the player to show the particle effect to.
	 * @param effect   the ParticleEffect class wrapper.
	 * @param location the location where the effect should spawn.
	 */
	public ParticleCreator(@Nullable final Player player, @Nonnull final ParticleEffect effect, @Nonnull final Location location) {
		this(player, effect, location.getWorld(), location.getX(), location.getY(), location.getZ());
	}

	/**
	 * Creates a particle effect at the specified location.
	 *
	 * @param effect   the ParticleEffect class wrapper.
	 * @param location the location where the effect should spawn.
	 */
	public ParticleCreator(final ParticleEffect effect, @Nonnull final Location location) {
		this(null, effect, location.getWorld(), location.getX(), location.getY(), location.getZ());
	}

	/**
	 * Creates a particle effect at the specified location in the given world.
	 *
	 * @param effect the ParticleEffect class wrapper.
	 * @param world  the world where the effect should spawn.
	 * @param x      the x-coordinate where the effect should spawn.
	 * @param y      the y-coordinate where the effect should spawn.
	 * @param z      the z-coordinate where the effect should spawn.
	 */
	public ParticleCreator(@Nonnull final ParticleEffect effect, final World world, final double x, final double y, final double z) {
		this(null, effect, world, x, y, z);
	}

	/**
	 * Creates a particle effect at the specified location in the given world for the given player.
	 *
	 * @param player the player to show the particle effect to.
	 * @param effect the ParticleEffect class wrapper.
	 * @param world  the world where the effect should spawn.
	 * @param x      the x-coordinate where the effect should spawn.
	 * @param y      the y-coordinate where the effect should spawn.
	 * @param z      the z-coordinate where the effect should spawn.
	 */
	public ParticleCreator(final Player player, @Nonnull final ParticleEffect effect, final World world, final double x, final double y, final double z) {
		Validate.checkNotNull(world, "World is null, so can't spawn the particle");
		Validate.checkNotNull(effect, "Effect is null, so can't spawn the particle");
		
		this.particle = effect.getParticle();
		this.effect = effect.getEffect();
		this.material = effect.getMaterial();
		this.data = effect.getData();
		this.dataType = effect.getDataType();
		this.particleDustOptions = effect.getParticleDustOptions();
		this.count = effect.getCount();
		this.offsetX = effect.getOffsetX();
		this.offsetY = effect.getOffsetY();
		this.offsetZ = effect.getOffsetZ();
		this.world = world;
		this.player = player;
		this.x = x;
		this.y = y;
		this.z = z;
	}


	/**
	 * Creates the particle effect.
	 *
	 * @return true if the particle effect was successfully spawned.
	 */
	public boolean create() {
		final ParticleDustOptions particleDustOptions = this.particleDustOptions;
		if (particleDustOptions != null) {
			if (particleDustOptions.getToColor() != null)
				spawnDustTransitionParticle(new Particle.DustTransition(particleDustOptions.getFromColor(), particleDustOptions.getToColor(), particleDustOptions.getSize()));
			else
				spawnDustOptionsParticle(new Particle.DustOptions(particleDustOptions.getFromColor(), particleDustOptions.getSize()));
			return true;
		} else
			return checkTypeParticle();
	}

	/**
	 * Creates the DustOptionsParticle if the micraft version suport it.
	 */
	public void spawnDustOptionsParticle(final Particle.DustOptions dustOptions) {
		Location location = null;
		if (this.effect != null)
			location = new Location(this.world, this.x, this.y, this.z);

		if (player != null) {
			if (location != null)
				player.playEffect(location, this.effect, this.data);
			else
				player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
		} else {
			if (location != null)
				this.world.playEffect(location, this.effect, this.data);
			else
				this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
		}
	}

	/**
	 * Creates the DustTransitionParticle if the micraft version suport it.
	 */
	public void spawnDustTransitionParticle(final Particle.DustTransition dustOptions) {
		Location location = null;
		if (this.effect != null)
			location = new Location(this.world, this.x, this.y, this.z);

		if (player != null) {
			if (location != null)
				player.playEffect(location, this.effect, this.data);
			else
				player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
		} else {
			if (location != null)
				this.world.playEffect(location, this.effect, this.data);
			else
				this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
		}
	}

	/**
	 * Check the type of particle, if it effect or partile.
	 *
	 * @return true if it could spawn the particle.
	 */
	public boolean checkTypeParticle() {
		if (this.effect != null)
			return this.spawnEffect();
		else
			return spawnParticle();
	}

	/**
	 * Spawn the effect. If mincraft version is at least 8.8 or older.
	 *
	 * @return true if it could spawn the particle.
	 */
	public boolean spawnEffect() {
		if (this.effect == null) return false;
		Location location = new Location(this.world, this.x, this.y, this.z);

		if (this.material != null) {
			if (player != null) {
				if (this.dataType == Material.class)
					player.playEffect(location, this.effect, this.material);
				if (this.dataType == MaterialData.class)
					player.playEffect(location, this.effect, this.material.getData());
			} else {
				if (this.dataType == Material.class)
					this.world.playEffect(location, this.effect, this.material);
				if (this.dataType == MaterialData.class)
					this.world.playEffect(location, this.effect, this.material.getData());
			}
		} else {
			if (player != null)
				player.playEffect(location, this.effect, this.data);
			else
				this.world.playEffect(location, this.effect, this.data);
		}
		return true;
	}

	/**
	 * Spawn the effect. If mincraft version is at least 9 or newer.
	 *
	 * @return true if it could spawn the particle.
	 */
	public boolean spawnParticle() {
		if (particle == null) return false;

		if (this.material != null && this.dataType != Void.class) {
			if (player != null) {
				if (this.dataType == BlockData.class)
					player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data, this.material.createBlockData());
				if (this.dataType == ItemStack.class)
					player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data, new ItemStack(this.material));
			} else {
				if (this.dataType == BlockData.class)
					this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data, this.material.createBlockData());
				if (this.dataType == ItemStack.class)
					this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data, new ItemStack(this.material));
			}
			return true;
		} else if (this.dataType == Void.class) {
			if (player != null) {
				player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data);
			}
			this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.data);
			return true;
		}
		return false;
	}
}
