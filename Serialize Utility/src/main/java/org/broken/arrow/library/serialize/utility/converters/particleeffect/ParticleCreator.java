package org.broken.arrow.library.serialize.utility.converters.particleeffect;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.serialize.utility.converters.particleeffect.resolver.ParticleDataResolver;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Logger;

/**
 * This class is responsible for creating particles in Minecraft. It automatically detects the particle type
 * and uses the appropriate method based on the Minecraft version. It is compatible with Minecraft versions 1.8
 * to the latest version.
 */
public class ParticleCreator {
    private final Logger logger = Logger.getLogger(String.valueOf(ParticleCreator.class));

    private final float serverVersion;
    private final Effect effect;
    private final Particle particle;
    private final ParticleEffectAccessor effectAccessor;
    private final Class<?> dataType;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double extra;
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
    public ParticleCreator(@Nullable final Player player, @Nonnull final ParticleEffectAccessor effect, @Nonnull final Location location) {
        this(player, effect, location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Creates a particle effect at the specified location.
     *
     * @param effect   the ParticleEffect class wrapper.
     * @param location the location where the effect should spawn.
     */
    public ParticleCreator(final ParticleEffectAccessor effect, @Nonnull final Location location) {
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
    public ParticleCreator(@Nonnull final ParticleEffectAccessor effect, final World world, final double x, final double y, final double z) {
        this(null, effect, world, x, y, z);
    }

    /**
     * Creates a particle effect at the specified location in the given world for the given player.
     *
     * @param player         the player to show the particle effect to.
     * @param effectAccessor the ParticleEffect class wrapper.
     * @param world          the world where the effect should spawn.
     * @param x              the x-coordinate where the effect should spawn.
     * @param y              the y-coordinate where the effect should spawn.
     * @param z              the z-coordinate where the effect should spawn.
     */
    public ParticleCreator(final Player player, @Nonnull final ParticleEffectAccessor effectAccessor, final World world, final double x, final double y, final double z) {
        Validate.checkNotNull(world, "World is null, so can't spawn the particle");
        Validate.checkNotNull(effectAccessor, "EffectAccessor is null, so can't spawn the particle");

        this.particle = effectAccessor.getParticle();
        this.effect = effectAccessor.getEffect();
        this.extra = effectAccessor.getExtra();
        this.dataType = effectAccessor.getDataType();
        this.particleDustOptions = effectAccessor.getParticleDustOptions();
        this.count = effectAccessor.getCount();
        this.offsetX = effectAccessor.getOffsetX();
        this.offsetY = effectAccessor.getOffsetY();
        this.offsetZ = effectAccessor.getOffsetZ();
        this.effectAccessor = effectAccessor;
        this.world = world;
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.serverVersion = ConvertParticlesUtility.getServerVersion();
    }


    /**
     * Spawn the particle effect.
     *
     * @return true if the particle effect was successfully spawned.
     */
    public boolean create() {
        final ParticleDustOptions dustOptions = this.particleDustOptions;
        if (dustOptions != null) {
            if (dustOptions.getToColor() != null)
                spawnDustTransitionParticle(new Particle.DustTransition(dustOptions.getFromColor(), dustOptions.getToColor(), dustOptions.getSize()));
            else
                spawnDustOptionsParticle(new Particle.DustOptions(dustOptions.getFromColor(), dustOptions.getSize()));
            return true;
        } else
            return checkTypeParticle();
    }

    /**
     * Creates the DustOptionsParticle if the Minecraft version support it.
     *
     * @param dustOptions The dustoption for this particle.
     */
    public void spawnDustOptionsParticle(final Particle.DustOptions dustOptions) {
        Location location = null;
        if (this.effect != null)
            location = new Location(this.world, this.x, this.y, this.z);

        if (player != null) {
            if (location != null)
                player.playEffect(location, this.effect, this.extra);
            else
                player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
        } else {
            if (location != null)
                this.world.playEffect(location, this.effect, this.extra);
            else
                this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
        }
    }

    /**
     * Creates the DustTransitionParticle if the Minecraft version support it.
     *
     * @param dustOptions The transition for this particle.
     */
    public void spawnDustTransitionParticle(final Particle.DustTransition dustOptions) {
        Location location = null;
        if (this.effect != null)
            location = new Location(this.world, this.x, this.y, this.z);

        if (player != null) {
            if (location != null)
                player.playEffect(location, this.effect, this.extra);
            else
                player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
        } else {
            if (location != null)
                this.world.playEffect(location, this.effect, this.extra);
            else
                this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, dustOptions);
        }
    }

    /**
     * Check the type of particle, if it effect or particle.
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
     * Spawn the effect. If Minecraft version is at least 8.8 or older.
     *
     * @return true if it could spawn the particle.
     */
    public boolean spawnEffect() {
        if (this.effect == null) return false;
        Location location = new Location(this.world, this.x, this.y, this.z);
        int radius = 0;
        if (this.extra >= 0.0)
            radius = (int) Math.floor(this.extra);
        if (this.dataType != Void.class) {
            spawn(location, radius);
        } else {
            if (this.effect.getData() != null) {
                logger.warning("You have to set the data for this effect '" + this.effectAccessor.getEffect() + "' . The type of data you must implement this class " + this.effect.getData());
                return false;
            }
            if (player != null)
                player.playEffect(location, this.effect, null);
            else
                this.world.playEffect(location, this.effect, null, radius);
        }
        return true;
    }

    /**
     * Spawn the effect. If Minecraft version is at least 9 or newer.
     *
     * @return true if it could spawn the particle.
     */
    public boolean spawnParticle() {
        if (particle == null) return false;

        if (this.effectAccessor != null && this.dataType != Void.class) {
            return spawn();
        } else if (this.dataType == Void.class) {
            if (player != null) {
                player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra);
            } else
                this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra);
            return true;
        }
        return false;
    }

    private boolean spawn() {
        final Material material = this.effectAccessor.getMaterial();
        final BlockData blockData = this.effectAccessor.getMaterialBlockData();
        final ParticleDataResolver data = this.effectAccessor.getResolveParticle();
        boolean dataSet = true;
        if (data != null) {
            Object haveClaasSet = data.compute(this.dataType);
            if (haveClaasSet != null) {
                if (player != null) {
                    player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, haveClaasSet);
                } else {
                    this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, haveClaasSet);
                }
                return true;
            } else {
                dataSet = false;
            }
        }
        if (!dataSet && (blockData == null || material == null)) {
            logger.warning("You have to set the data for this particle '" + this.effectAccessor.getParticle() + "' . The type of data you must implement this class '" + this.particle.getDataType() + "'");
            return false;
        }

        if (player != null) {
            if (this.dataType == BlockData.class) {
                player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, blockData);
            }
            if (this.dataType == ItemStack.class) {
                player.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, new ItemStack(material));
            }
        } else {
            if (this.dataType == BlockData.class) {
                this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, blockData);
            }
            if (this.dataType == ItemStack.class) {
                this.world.spawnParticle(particle, this.x, this.y, this.z, this.count, this.offsetX, this.offsetY, this.offsetZ, this.extra, new ItemStack(material));
            }
        }
        return true;
    }

    private void spawn(final Location location, final int radius) {
        if (player != null) {
            ParticleDataResolver data = this.effectAccessor.getResolveParticle();
            if (data != null) {
                Object haveClaasSet = data.compute(this.dataType);
                if (haveClaasSet != null) {
                    this.player.playEffect(location, this.effect, haveClaasSet);
                }
            } else {
                if (this.dataType == Material.class)
                    this.player.playEffect(location, this.effect, this.effectAccessor.getMaterial());
                if (this.dataType == MaterialData.class)
                    this.player.playEffect(location, this.effect, this.effectAccessor.getMaterialData());
                if (this.dataType == BlockFace.class)
                    this.player.playEffect(location, this.effect, this.effectAccessor.getBlockFace());

                PotionsData potionsData = this.effectAccessor.getPotion();
                if (potionsData != null) {
                    if (PotionsData.isOldPotionAvailable()) {
                        Potion potion = potionsData.getPotion();
                        if (potion != null) {
                            world.playEffect(location, effect, potion);
                            return;
                        }
                    }
                    PotionEffect potionEffect = potionsData.getPotionEffect();
                    if (potionEffect != null) {
                        player.playEffect(location, effect, potionEffect);
                        if (serverVersion > 20.0F) {
                            logger.info("Usage of PotionEffect for visual effects is discouraged. Consider using the Particle API for better compatibility and flexibility.");
                        }
                    } else {
                        logger.warning("No valid potion or potion effect found, consider using particle effects.");
                    }
                }
            }
        } else {
            spawnInWorld(location, radius);
        }
    }


    private void spawnInWorld(final Location location, final int radius) {
        final ParticleDataResolver data = this.effectAccessor.getResolveParticle();
        if (data != null) {
            Object computed = data.compute(this.dataType);
            if (computed != null) {
                this.world.playEffect(location, this.effect, computed, radius);
            }
        } else {
            if (this.dataType == Material.class)
                this.world.playEffect(location, this.effect, this.effectAccessor, radius);
            if (this.dataType == MaterialData.class)
                this.world.playEffect(location, this.effect, this.effectAccessor.getMaterialData(), radius);
            if (this.dataType == BlockFace.class)
                this.world.playEffect(location, this.effect, this.effectAccessor.getBlockFace(), radius);

            PotionsData potionsData = this.effectAccessor.getPotion();
            if (potionsData != null) {
                if (PotionsData.isOldPotionAvailable()) {
                    Potion potion = potionsData.getPotion();
                    if (potion != null) {
                        world.playEffect(location, effect, potion);
                        return;
                    }
                }
                PotionEffect potionEffect = potionsData.getPotionEffect();
                if (potionEffect != null) {
                    player.playEffect(location, effect, potionEffect);
                    if (serverVersion > 20.0F) {
                        logger.info("Usage of PotionEffect for visual effects is discouraged. Consider using the Particle API for better compatibility and flexibility.");
                    }
                } else {
                    logger.warning("No valid potion or potion effect found, consider using particle effects.");
                }
            }
        }
    }

}
