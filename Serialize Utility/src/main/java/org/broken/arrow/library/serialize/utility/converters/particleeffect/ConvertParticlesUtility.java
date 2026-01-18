package org.broken.arrow.library.serialize.utility.converters.particleeffect;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.serialize.utility.Pair;
import org.broken.arrow.library.serialize.utility.converters.particleeffect.ParticleEffect.Builder;
import org.broken.arrow.library.serialize.utility.converters.particleeffect.resolver.ParticleDataResolver;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting particles and particle-related data.
 */
public class ConvertParticlesUtility {

    private static final float SERVER_VERSION;
    private static final Logging logger = new Logging(ConvertParticlesUtility.class);

    private ConvertParticlesUtility() {
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
            if (index >= 0)
                secondNumber = secondNumber.substring(index + 1);
        } else {
            final String secondString = versionPieces[2];
            firstNumber = firstString;
            secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
        }
        SERVER_VERSION = Float.parseFloat(firstNumber + "." + secondNumber);
    }

    /**
     * Gets the parsed server version as a float value representing major.minor version.
     *
     * @return the server version, e.g., 20.4 or 19.3
     */
    public static float getServerVersion() {
        return SERVER_VERSION;
    }

    /**
     * Converts a list of particle names to a list of ParticleEffect instances.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particles The list of particle names.
     * @return A list of ParticleEffect instances.
     */
    public static List<ParticleEffectAccessor> convertListOfParticles(final List<String> particles) {
        if (particles == null) return new ArrayList<>();

        final List<ParticleEffectAccessor> particleList = new ArrayList<>();
        for (final String particle : particles) {
            final ParticleEffectAccessor particleEffect = getParticleOrEffect(particle, 1.0F);
            if (particleEffect == null) continue;
            particleList.add(particleEffect);
        }
        return particleList;
    }

    /**
     * Converts a map of particle names and colors to a list of ParticleEffect instances.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particles The map of particle names and colors.
     * @return A list of ParticleEffect instances.
     */
    public static List<ParticleEffectAccessor> convertListOfParticles(final Map<String, Object> particles) {
        if (particles == null) return new ArrayList<>();

        final List<ParticleEffectAccessor> particleList = new ArrayList<>();
        for (final Map.Entry<String, Object> particle : particles.entrySet()) {
            final ParticleEffectAccessor particleEffect = getParticleOrEffect(particle.getKey(), (String) particle.getValue(), 1.0F);
            if (particleEffect == null) continue;
            particleList.add(particleEffect);
        }
        return particleList;
    }

    /**
     * Converts a map of particle names and colors represented as pairs to a list of ParticleEffect instances.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particles The map of particle names and colors as pairs.
     *                  The key represents the particle name, and the value is a Pair object
     *                  containing the first color and the second color (if applicable). The value can
     *                  also be set to null if no color needed for the effect.
     * @return A list of ParticleEffect instances.
     */
    public static List<ParticleEffectAccessor> convertListOfParticlesPair(final Map<String, Pair<String, String>> particles) {
        if (particles == null) return new ArrayList<>();
        final List<ParticleEffectAccessor> particleList = new ArrayList<>();
        for (final Map.Entry<String, Pair<String, String>> pairEntry : particles.entrySet()) {
            final Pair<String, String> colors = pairEntry.getValue();
            final ParticleEffectAccessor particleEffect;
            if (colors == null)
                particleEffect = getParticleOrEffect(pairEntry.getKey(), 1.0F);
            else
                particleEffect = getParticleOrEffect(pairEntry.getKey(), colors.getFirst(), colors.getSecond(), 1, 0.7F);
            if (particleEffect == null) continue;
            particleList.add(particleEffect);
        }
        return particleList;
    }

    /**
     * Retrieves the particle effect with configured properties.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particle The particle name.
     * @param extra    Additional float value with different usage depending on the particle.
     * @return The ParticleEffect instance corresponding to the particle name and color.
     */
    public static ParticleEffectAccessor getParticleOrEffect(final Object particle, final float extra) {
        return getParticleOrEffect(particle, null, null, 1, extra);
    }

    /**
     * Retrieves the particle effect with configured properties.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particle The particle name.
     * @param color    The color for the particle (if applicable).
     * @param extra    Additional float value with different usage depending on the particle.
     * @return The ParticleEffect instance corresponding to the particle name and color.
     */
    public static ParticleEffectAccessor getParticleOrEffect(final Object particle, @Nullable final String color, final float extra) {
        return getParticleOrEffect(particle, color, null, 1, extra);
    }

    /**
     * Retrieves the particle effect with configured properties.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particle          the particle you want to convert.
     * @param firstColor        the first color if you use effect you can change color.
     * @param secondColor       the second color for the effect.
     * @param amountOfParticles the amount of particles.
     * @param extra             this have different usage depending on particle.
     * @return particle effect particle.
     */
    public static ParticleEffectAccessor getParticleOrEffect(final Object particle, @Nullable final String firstColor, @Nullable final String secondColor, final int amountOfParticles, final float extra) {
        return getParticleOrEffect(particle, firstColor, secondColor, null, amountOfParticles, extra);
    }

    /**
     * Retrieves the particle effect with configured properties.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particle          the particle you want to convert.
     * @param firstColor        the first color if you use effect you can change color.
     * @param secondColor       the second color for the effect.
     * @param particleData      if the particle demands Material, MaterialData, MaterialBlockData, BlockFace, or Potion data to be set.
     * @param amountOfParticles the amount of particles.
     * @param extra             this have different usage depending on particle.
     * @return particle effect particle.
     */
    public static ParticleEffectAccessor getParticleOrEffect(final Object particle, @Nullable final String firstColor, @Nullable final String secondColor, @Nullable ParticleDataResolver particleData, final int amountOfParticles, final float extra) {
        ParticleEffectWrapper wrapper = new ParticleEffectWrapper(particle, particleData, amountOfParticles, extra);
        wrapper.setFromColor(firstColor);
        wrapper.setToColor(secondColor);
        return getParticleOrEffect(wrapper);
    }


    /**
     * Retrieves the particle effect with configured properties.
     *
     * <p>
     * Note: As of 1.17, the BARRIER particle is called BLOCK_MARKER. For BARRIER, this method
     * automatically handles setting the material to BARRIER. However, this automation is limited
     * to BARRIER; other materials for BLOCK_MARKER or other effects requiring data must be set manually
     * due to the variety of configuration options available.
     * </p>
     *
     * @param particleEffectWrapper A utility wrapper containing the configured data for the particle.
     * @return The configured particle effect.
     */
    public static ParticleEffectAccessor getParticleOrEffect(final ParticleEffectWrapper particleEffectWrapper) {
        Object particle = particleEffectWrapper.getParticle();
        if (particle == null) return null;
        Object object;
        if (SERVER_VERSION < 9) {
            object = getEffect(String.valueOf(particle));
        } else {
            object = getParticle(String.valueOf(particle));
            if (object == null) {
                object = getEffect(String.valueOf(particle));
            }
        }
        Object particleData = particleEffectWrapper.getParticleData();
        String firstColor = particleEffectWrapper.getFromColor();
        String secondColor = particleEffectWrapper.getToColor();
        int amountOfParticles = particleEffectWrapper.getAmountOfParticles();
        float extra = particleEffectWrapper.getExtra();

        ParticleEffect.Builder builder;
        if (SERVER_VERSION >= 9 && !(object instanceof Effect)) {
            builder = buildParticle(object, particleData, firstColor, secondColor, amountOfParticles, extra);
        } else {
            builder = buildEffect(object, particleData, firstColor, secondColor, amountOfParticles, extra);
        }

        if (object == null || builder == null)
            return null;

        return builder.build();
    }

    /**
     * Retrieves the Particle instance for the given particle name.
     *
     * @param particle The particle name.
     * @return The Particle instance corresponding to the particle name, or null if not found.
     */
    public static Particle getParticle(String particle) {
        if (particle == null) return null;
        final Particle[] particles = Particle.values();
        particle = particle.toUpperCase();
        particle = replaceBarrier(particle);
        for (final Particle partic : particles) {
            if (partic.name().equals(particle))
                return partic;
        }
        return null;
    }

    /**
     * Retrieves the Effect instance for the given particle name.
     *
     * @param effectParticle The particle name.
     * @return The Effect instance corresponding to the particle name, or null if not found.
     */
    public static Effect getEffect(String effectParticle) {
        if (effectParticle == null) return null;
        final Effect[] effects = Effect.values();
        effectParticle = effectParticle.toUpperCase();

        for (final Effect effect : effects) {
            if (effect.name().equals(effectParticle))
                return effect;
        }
        return null;
    }

    /**
     * Automatically converts the "BARRIER" particle to "BLOCK_MARKER" for Minecraft version 17 or newer.
     *
     * @param particle The particle to check.
     * @return The updated particle name.
     */
    public static String replaceBarrier(final String particle) {

        if (SERVER_VERSION >= 17 && particle.equals("BARRIER"))
            return "BLOCK_MARKER";
        return particle;
    }

    /**
     * Configures extra data on a {@link ParticleEffect.Builder} based on the given particle data object and particle type.
     * <p>
     * This method checks if the provided {@code particleData} matches the expected data type of the {@code effect}.
     * If so, it sets the extra data accordingly with help of the {@link ParticleDataResolver} class.
     * <p>
     * If the data is non-null but incompatible with the effect's expected data type, a warning is logged suggesting the correct type.
     * <p>
     * If the data type does not match the expected type for the particle, a warning is logged advising the correct data type to use.
     *
     * @param builder      the ParticleEffect.Builder to configure
     * @param particleData the extra data object to set, expected to match the particle's data type
     * @param part         the particle type that dictates the expected data type
     */
    public static void setBuilderExtraDataParticle(ParticleEffect.Builder builder, Object particleData, Particle part) {
        if (part.getDataType().isInstance(particleData)) {
            ParticleDataResolver resolveParticle = new ParticleDataResolver(particleData);
            builder.setParticleData(resolveParticle);

        } else {
            if (particleData == null && part.getDataType() != Void.class)
                logger.warn(messageWrapper -> {
                    messageWrapper.setMessage("You must set the extra data for this '{effect-name}' or the effect will not spawn. The class you should use is '{data-type}'.")
                            .putPlaceholder("{effect-name}", part.name())
                            .putPlaceholder("{data-type}", part.getDataType() + "");
                });
        }
    }

    /**
     * Configures extra data on a {@link ParticleEffect.Builder} based on the given particle data object and effect type.
     * <p>
     * This method checks if the provided {@code particleData} matches the expected data type of the {@code effect}.
     * If so, it sets the extra data accordingly with help of the {@link ParticleDataResolver} class.
     * <p>
     * If the data is non-null but incompatible with the effect's expected data type, a warning is logged suggesting the correct type.
     *
     * @param builder      the ParticleEffect.Builder to configure
     * @param particleData the extra data object to set, expected to match the effect's data type
     * @param effect       the effect type that dictates the expected data type
     */
    public static void setBuilderExtraDataEffect(ParticleEffect.Builder builder, Object particleData, Effect effect) {
        Class<?> effectData = effect.getData();
        if (effectData != null && effectData.isInstance(particleData)) {
            ParticleDataResolver resolveParticle = new ParticleDataResolver(particleData);
            builder.setParticleData(resolveParticle);
        } else {
            if (particleData != null && effectData != null)
                logger.warn(messageWrapper -> {
                    messageWrapper.setMessage("You must set the extra data for this '{effect-name}' or the effect will not spawn. The class you should use is '{data-type}'.")
                            .putPlaceholder("{effect-name}", effect.name())
                            .putPlaceholder("{data-type}", effectData + "");
                });
        }
    }


    private static Builder buildParticle(final Object object, final Object particleData, final String firstColor, final String secondColor, final int amountOfParticles, final float extra) {
        Builder builder = null;
        if (object instanceof Particle) {
            final Particle part = (Particle) object;
            builder = new ParticleEffect.Builder(part, part.getDataType());

            if (part.name().equals("BLOCK_MARKER")) {
                builder.setParticleData(new ParticleDataResolver(Material.BARRIER));
            } else {
                if (particleData == null) {
                    if (part.getDataType() == Integer.class)
                        builder.setParticleData(ParticleDataResolver.ofInteger());
                    if (part.getDataType() == Float.class)
                        builder.setParticleData(ParticleDataResolver.ofFloat());
                    if (part.getDataType() == Double.class)
                        builder.setParticleData(new ParticleDataResolver(Double.valueOf(0)));
                }
            }

            setBuilderExtraDataParticle(builder, particleData, part);
            if (firstColor != null && part.name().equals("REDSTONE")) {
                if (secondColor != null)
                    builder.setDustOptions(new ParticleDustOptions(firstColor, secondColor, extra));
                else
                    builder.setDustOptions(new ParticleDustOptions(firstColor, extra));
            } else {
                builder.setExtra(extra);
            }
            builder.setCount(amountOfParticles);
        }
        return builder;
    }

    private static Builder buildEffect(final Object object, final Object particleData, final String firstColor, final String secondColor, final int amountOfParticles, final float extra) {
        Builder builder = null;
        if (object instanceof Effect) {
            final Effect part = (Effect) object;
            builder = new Builder(part, part.getData() != null ? part.getData() : Void.class);
            if (firstColor != null && part.name().equals("REDSTONE")) {
                if (secondColor != null)
                    builder.setDustOptions(new ParticleDustOptions(firstColor, secondColor, extra));
                else
                    builder.setDustOptions(new ParticleDustOptions(firstColor, extra));
            } else {
                builder.setExtra(extra);
            }
            setBuilderExtraDataEffect(builder, particleData, part);
            builder.setCount(amountOfParticles);
        }
        return builder;
    }

}