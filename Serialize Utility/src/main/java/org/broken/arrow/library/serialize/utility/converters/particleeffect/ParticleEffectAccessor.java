package org.broken.arrow.library.serialize.utility.converters.particleeffect;

import org.broken.arrow.library.serialize.utility.converters.particleeffect.ParticleEffect.Builder;
import org.broken.arrow.library.serialize.utility.converters.particleeffect.resolver.ParticleDataResolver;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

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
     * @deprecated use the {@link #getResolveParticle()} instead.
     */
    @Deprecated
    @Nullable
    Material getMaterial();

    /**
     * Retrieves the Material data associated with this ParticleEffect.
     *
     * @return the Material data object, or null if not set or if this effect don't use this class.
     * @deprecated use the {@link #getResolveParticle()} instead.
     */

    @Deprecated
    @Nullable
    Class<? extends MaterialData> getMaterialData();

    /**
     * Retrieves the Material BlockData associated with this ParticleEffect.
     *
     * @return the Material BlockData object, or null if not set or if this effect don't use this class.
     * @deprecated use the {@link #getResolveParticle()} instead.
     */
    @Deprecated
    @Nullable
    BlockData getMaterialBlockData();

    /**
     * Retrieves the block face associated with this ParticleEffect.
     *
     * @return the block face, or null if not set or if this effect don't use this class.
     * @deprecated use the {@link #getResolveParticle()} instead.
     */
    @Deprecated
    @Nullable
    BlockFace getBlockFace();

    /**
     * Retrieves the potion associated with this ParticleEffect.
     *
     * @return the potion object, or null if not set or if this effect don't use this class.
     * @deprecated use the {@link #getResolveParticle()} instead.
     */
    @Deprecated
    @Nullable
    PotionsData getPotion();

    /**
     * Retrieves the resolver associated with this ParticleEffect.
     *
     * @return the resolver, or null if not set or if this effect don't use this class.
     */
    @Nullable
    ParticleDataResolver getResolveParticle();

    /**
     * Retrieves the amount of particles associated with this ParticleEffect.
     *
     * @return amount of particles that should spawn at the same time.
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
     * Retrieves the extra data on the Particle effect.
     * This number have variety of functions depending
     * on the particle. Can be everything from speed to
     * the size of the particle.
     *
     * @return the extra data set.
     */
    double getExtra();

    /**
     * Retrieves the data set on the effect.
     *
     * @return the class type for this effect or particle.
     */
    @Nonnull
    Class<?> getDataType();

    /**
     * Retrieves ParticleDustOptions, but this can only be used
     * if the Minecraft version support it.
     *
     * @return the ParticleDustOptions instance.
     */
    @Nullable
    ParticleDustOptions getParticleDustOptions();

    /**
     * Returns a {@link ParticleEffect.Builder} for creating or modifying
     * {@link ParticleEffect} instances based on the current accessor's state.
     *
     * @return a builder pre-populated with this accessor's data
     */
    Builder getBuilder();
}
