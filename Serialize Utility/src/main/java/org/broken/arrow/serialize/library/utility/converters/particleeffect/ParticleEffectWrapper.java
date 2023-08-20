package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A utility class that simplifies the process of configuring particle effects or effects.
 * It serves as a wrapper for the provided values, maintaining their integrity without alteration.
 */
public class ParticleEffectWrapper {

	private final Object particle;
	private final Object particleData;
	@Nullable
	private String fromColor;
	@Nullable
	private String toColor;
	private final int amountOfParticles;
	private final float extra;

	/**
	 * Create a particle effect wrapper.
	 *
	 * @param particle          the particle or effect.
	 * @param amountOfParticles the amount of particles to spawn.
	 * @param extra             this has different usage depending on the particle; for redstone, it sets the size, for others, it's the speed.
	 */
	public ParticleEffectWrapper(final Object particle, final int amountOfParticles, final float extra) {
		this(particle, null, amountOfParticles, extra);
	}

	/**
	 * Create a particle effect wrapper.
	 *
	 * @param particle          the particle or effect.
	 * @param particleData      if the particle demands Material, MaterialData, MaterialBlockData, BlockFace, or Potion data to be set.
	 * @param amountOfParticles the amount of particles to spawn.
	 * @param extra             this has different usage depending on the particle; for redstone, it sets the size, for others, it's the speed.
	 */
	public ParticleEffectWrapper(final Object particle, @Nullable Object particleData, final int amountOfParticles, final float extra) {
		this.particle = particle;
		this.particleData = particleData;
		this.amountOfParticles = amountOfParticles;
		this.extra = extra;
	}

	/**
	 * Set the color or from color if you plan to use {@link #setToColor(String)} for a
	 * redstone particle.
	 *
	 * @param fromColor the color or from what color it should start when you use a redstone particle.
	 */
	public void setFromColor(@Nullable final String fromColor) {
		this.fromColor = fromColor;
	}

	/**
	 * Set the color it should end the redstone particle animation.
	 * Can only be set on redstone particles in Minecraft 1.18+.
	 *
	 * @param toColor the color it should end the redstone particle animation.
	 */
	public void setToColor(@Nullable final String toColor) {
		this.toColor = toColor;
	}

	public Object getParticle() {
		return particle;
	}

	public Object getParticleData() {
		return particleData;
	}

	@Nullable
	public String getFromColor() {
		return fromColor;
	}

	@Nullable
	public String getToColor() {
		return toColor;
	}

	public int getAmountOfParticles() {
		return amountOfParticles;
	}

	public float getExtra() {
		return extra;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof ParticleEffectWrapper)) return false;
		final ParticleEffectWrapper that = (ParticleEffectWrapper) o;
		return amountOfParticles == that.amountOfParticles && Float.compare(that.extra, extra) == 0 && Objects.equals(particle, that.particle) && Objects.equals(particleData, that.particleData) && Objects.equals(fromColor, that.fromColor) && Objects.equals(toColor, that.toColor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(particle, particleData, fromColor, toColor, amountOfParticles, extra);
	}
}
