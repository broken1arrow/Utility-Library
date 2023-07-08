package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import org.broken.arrow.serialize.library.utility.converters.particleeffect.ParticleEffect.Builder;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ParticleEffectAccessor {
	/**
	 * Retrieves the Particle associated with this ParticleEffect.
	 *
	 * @return the Particle object, or null if not set.
	 */
	@Nullable
	Particle getParticle();

	/**
	 * Retrieves the Effect associated with this ParticleEffect.
	 *
	 * @return the Effect object, or null if not set.
	 */
	@Nullable
	Effect getEffect();

	/**
	 * Retrieves the Material associated with this ParticleEffect.
	 *
	 * @return the Material object, or null if not set.
	 */
	@Nullable
	Material getMaterial();

	/**
	 * Retrieves the amount of particles associated with this ParticleEffect.
	 *
	 * @return amount of particels that should spawn at the same time.
	 */
	int getCount();

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	double getOffsetX();

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	double getOffsetY();

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	double getOffsetZ();

	/**
	 * Retrieves the data on the Particle effect.
	 *
	 * @return the data.
	 */
	double getData();

	/**
	 * Retrieves the data set on the effect.
	 *
	 * @return the class type for this effect or particle.
	 */
	@Nonnull
	Class<?> getDataType();

	/**
	 * Retrieves ParticleDustOptions, but this can only be used
	 * if the Minecraft version suport it.
	 *
	 * @return the ParticleDustOptions instance.
	 */
	@Nullable
	ParticleDustOptions getParticleDustOptions();

	Builder getBuilder();
}
