package org.broken.arrow.library.serialize.utility.converters.particleeffect;

import org.broken.arrow.library.serialize.utility.converters.particleeffect.resolver.ParticleDataResolver;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A utility class that simplifies the process of configuring particle effects or effects.
 * It serves as a wrapper for the provided values, maintaining their integrity without alteration.
 */
public class ParticleEffectWrapper {

    private final Object particle;
    private final ParticleDataResolver particleData;
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
    public ParticleEffectWrapper(final Object particle, @Nullable ParticleDataResolver particleData, final int amountOfParticles, final float extra) {
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

    /**
     * Returns the particle type or effect associated with this wrapper.
     * <p>
     * The returned value is stored as a generic {@link Object} to allow
     * compatibility across different server versions and APIs.
     * Depending on the implementation, this could be:
     * <ul>
     *   <li>a {@code org.bukkit.Particle}</li>
     *   <li>a {@code org.bukkit.Effect} </li>
     * </ul>
     *
     * @return the particle or effect representation
     */
    public Object getParticle() {
        return particle;
    }

    /**
     * Retrieves the resolver associated with this ParticleEffect.
     *
     * @return the resolver, or {@code null} if not set or if this effect don't use this class.
     */
    public ParticleDataResolver getParticleData() {
        return particleData;
    }

    /**
     * Returns the starting color for a redstone particle effect.
     * <p>
     * If no color has been set, this will return {@code null}.
     *
     * @return the starting color as a string, or {@code null} if not set
     */
    @Nullable
    public String getFromColor() {
        return fromColor;
    }

    /**
     * Returns the ending color for a redstone particle effect.
     * <p>
     * This is only applicable to redstone particles in Minecraft 1.18+.
     *
     * @return the ending color as a string, or {@code null} if not set
     */
    @Nullable
    public String getToColor() {
        return toColor;
    }

    /**
     * Returns the number of particles to spawn.
     *
     * @return the amount of particles
     */
    public int getAmountOfParticles() {
        return amountOfParticles;
    }

    /**
     * Returns the extra value for the particle effect.
     * <p>
     * For redstone particles, this defines the particle size.
     * For most other particles, this controls the speed.
     *
     * @return the extra parameter value
     */
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
