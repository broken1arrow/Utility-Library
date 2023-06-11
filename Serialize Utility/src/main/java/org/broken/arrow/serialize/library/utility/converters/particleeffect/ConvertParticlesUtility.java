package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import org.broken.arrow.serialize.library.utility.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for converting particles and particle-related data.
 */
public class ConvertParticlesUtility {

	private static final float serverVersion;
	private static final Logger logger = Logger.getLogger("ConvertParticlesUtility");

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
		serverVersion = Float.parseFloat(firstNumber + "." + secondNumber);
	}

	/**
	 * Converts a list of particle names to a list of ParticleEffect instances.
	 *
	 * @param particles The list of particle names.
	 * @return A list of ParticleEffect instances.
	 */
	public static List<ParticleEffect> convertListOfParticles(final List<String> particles) {
		if (particles == null) return null;

		final List<ParticleEffect> particleList = new ArrayList<>();
		for (final String particle : particles) {
			final ParticleEffect particleEffect = getParticleOrEffect(particle, null, 0.7F);
			if (particleEffect == null) continue;
			particleList.add(particleEffect);
		}
		return particleList;
	}

	/**
	 * Converts a map of particle names and colors to a list of ParticleEffect instances.
	 *
	 * @param particles The map of particle names and colors.
	 * @return A list of ParticleEffect instances.
	 */
	public static List<ParticleEffect> convertListOfParticles(final Map<String, Object> particles) {
		if (particles == null) return null;

		final List<ParticleEffect> particleList = new ArrayList<>();
		for (final Map.Entry<String, Object> particle : particles.entrySet()) {
			final ParticleEffect particleEffect = getParticleOrEffect(particle.getKey(), (String) particle.getValue(), 0.7F);
			if (particleEffect == null) continue;
			particleList.add(particleEffect);
		}
		return particleList;
	}

	/**
	 * Converts a map of particle names and colors represented as pairs to a list of ParticleEffect instances.
	 *
	 * @param particles The map of particle names and colors as pairs.
	 *                  The key represents the particle name, and the value is a Pair object
	 *                  containing the first color and the second color (if applicable). The value can
	 *                  also be set to null if no color needed for the effect.
	 * @return A list of ParticleEffect instances.
	 */
	public static List<ParticleEffect> convertListOfParticlesPair(final Map<String, Pair<String, String>> particles) {
		if (particles == null) return null;
		final List<ParticleEffect> particleList = new ArrayList<>();
		for (final Map.Entry<String, Pair<String, String>> pairEntry : particles.entrySet()) {
			final Pair<String, String> colors = pairEntry.getValue();
			final ParticleEffect particleEffect;
			if (colors == null)
				particleEffect = getParticleOrEffect(pairEntry.getKey(), null, 0.7F);
			else
				particleEffect = getParticleOrEffect(pairEntry.getKey(), colors.getFirst(), colors.getSecond(), 0.7F);
			if (particleEffect == null) continue;
			particleList.add(particleEffect);
		}
		return particleList;
	}


	/**
	 * Retrieves the ParticleEffect instance for the given particle name and color.
	 *
	 * @param particle The particle name.
	 * @param color    The color for the particle (if applicable).
	 * @param flot     Additional float value with different usage depending on the particle.
	 * @return The ParticleEffect instance corresponding to the particle name and color.
	 */
	public static ParticleEffect getParticleOrEffect(final Object particle, @Nullable final String color, final float flot) {
		return getParticleOrEffect(particle, color, null, flot);
	}

	/**
	 * Get the particle effect.
	 *
	 * @param particle    the particle you want to convert.
	 * @param firstColor  the first color if you use effect you can change color.
	 * @param secondColor the second color for the effet.
	 * @param flot        this have diffrent usage depending on particle.
	 * @return particle effect particle.
	 */
	public static ParticleEffect getParticleOrEffect(final Object particle, @Nullable final String firstColor, @Nullable final String secondColor, final float flot) {
		if (particle == null) return null;
		Object partc;
		if (serverVersion < 9) {
			partc = getEffect(String.valueOf(particle));
		} else {
			partc = getParticle(String.valueOf(particle));
			if (partc == null) {
				partc = getEffect(String.valueOf(particle));
			}
		}
		ParticleEffect.Builder builder = null;
		if (serverVersion >= 9)
			if (partc instanceof Particle) {
				final Particle part = (Particle) partc;
				builder = new ParticleEffect.Builder(part, part.getDataType());

				if (part.name().equals("BLOCK_MARKER")) {
					builder.setMaterial(Material.BARRIER);
				}
				if (firstColor != null && part.name().equals("REDSTONE"))
					if (secondColor != null)
						builder.setDustOptions(new ParticleDustOptions(firstColor, secondColor, flot));
					else
						builder.setDustOptions(new ParticleDustOptions(firstColor, flot));
			}
		if (partc instanceof Effect) {
			final Effect part = (Effect) partc;
			builder = new ParticleEffect.Builder(part, part.getData() != null ? part.getData() : Void.class);
			if (firstColor != null && part.name().equals("REDSTONE"))
				if (secondColor != null)
					builder.setDustOptions(new ParticleDustOptions(firstColor, secondColor, flot));
				else
					builder.setDustOptions(new ParticleDustOptions(firstColor, flot));
		}
		if (partc == null || builder == null)
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

		if (serverVersion >= 17)
			if (particle.equals("BARRIER"))
				return "BLOCK_MARKER";
		return particle;
	}


}