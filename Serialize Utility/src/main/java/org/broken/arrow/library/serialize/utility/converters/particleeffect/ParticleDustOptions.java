package org.broken.arrow.library.serialize.utility.converters.particleeffect;


import org.bukkit.Color;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bukkit.Color.fromRGB;

/**
 * Wrapper class for handling particle options related to dust particles, supporting different Minecraft versions.
 * This class provides an easy way to work with the {@link org.bukkit.Particle.DustTransition} and {@link org.bukkit.Particle.DustTransition} classes,
 * which may not be available in some Minecraft API versions.
 * <p>
 * Use the getter methods to retrieve the color options for the dust particle effects.
 */
public final class ParticleDustOptions implements ConfigurationSerializable {

	private final Color fromColor;
	private final Color toColor;
	private final float size;

	/**
	 * Constructs a ParticleDustOptions object with the specified fromColor and size.
	 * The fromColor is the color the redstone particle shall start from.
	 * The size represents the size of the redstone particle.
	 *
	 * @param fromColor The color from which the redstone particle should start,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param size      The size of the redstone particle.
	 */
	public ParticleDustOptions(@Nonnull final String fromColor, final float size) {
		this(convertToColor(fromColor), null, size);
	}

	/**
	 * Constructs a ParticleDustOptions object with the specified fromColor, toColor, and size.
	 * The fromColor is the color the redstone particle shall start from.
	 * The toColor is the color the redstone particle shall end with.
	 * The size represents the size of the redstone particle.
	 *
	 * @param fromColor The color from which the redstone particle should start,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param toColor   The color at which the redstone particle should end,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param size      The size of the redstone particle.
	 */
	public ParticleDustOptions(@Nonnull final String fromColor, final String toColor, final float size) {
		this(convertToColor(fromColor), convertToColor(toColor), size);
	}

	/**
	 * Constructs a ParticleDustOptions object with the specified fromColor, toColor, and size.
	 * The fromColor is the color the redstone particle shall start from.
	 * The toColor is the color the redstone particle shall end with.
	 * The size represents the size of the redstone particle.
	 *
	 * @param fromColor The color from which the redstone particle should start,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param size      The size of the redstone particle.
	 */
	public ParticleDustOptions(@Nonnull final Color fromColor, final float size) {
		this(fromColor, null, size);
	}

	/**
	 * Constructs a ParticleDustOptions object with the specified fromColor, toColor, and size.
	 * The fromColor is the color the redstone particle shall start from.
	 * The toColor is the color the redstone particle shall end with.
	 * The size represents the size of the redstone particle.
	 *
	 * @param fromColor The color from which the redstone particle should start,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param toColor   The color at which the redstone particle should end,
	 *                  specified in RGB format. Accepted formats are "#,#,#" or "# # #".
	 * @param size      The size of the redstone particle.
	 */
	public ParticleDustOptions(@Nonnull final Color fromColor, final Color toColor, final float size) {
		this.fromColor = fromColor;
		this.toColor = toColor;
		this.size = size;
	}

	/**
	 * Get the color it shall show first.
	 *
	 * @return the color to start from.
	 */
	public Color getFromColor() {
		return fromColor;
	}

	/**
	 * Get the color it shall show last.
	 *
	 * @return the color to end.
	 */
	public Color getToColor() {
		return toColor;
	}

	/**
	 * The size of the partticle.
	 *
	 * @return the size.
	 */
	public float getSize() {
		return size;
	}


	/**
	 * Converts a color string to a Color object representing the RGB values.
	 * The color string should be in the format "#,#,#" or "# # #", specifying the RGB components.
	 * Each component value should range from 0 to 255 (e.g., "0,255,50").
	 *
	 * @param s The string representing the color in RGB format.
	 * @return The Color object representing the RGB color values parsed from the input string.
	 */
	public static Color convertToColor(@Nonnull final String s) {
		if (s.contains(",")) {
			final String[] string = s.split(",");
			final int size = string.length;
			if (size == 3)
				return fromRGB(numberCheck(string[0]), numberCheck(string[1]), numberCheck(string[2]));

		} else if (s.contains(" ")) {
			final String[] string = s.split(" ");
			final int size = string.length;
			if (size == 3)
				return fromRGB(numberCheck(string[0]), numberCheck(string[1]), numberCheck(string[2]));
		}
		return fromRGB(0, 0, 0);
	}

	/**
	 * Checks if the provided string is a valid integer and returns the parsed number.
	 * If the string does not contain a valid number, it returns 0.
	 *
	 * @param number The string to check for a valid number.
	 * @return The parsed number or 0 if the string does not contain a valid number.
	 */
	public static int numberCheck(final String number) {
		try {
			return Integer.parseInt((number));
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "ParticleDustOptions{" +
				"fromColor=" + fromColor +
				", toColor=" + toColor +
				", particleSize=" + size +
				'}';
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		final Map<String, Object> particleData = new LinkedHashMap<>();
		particleData.put("From_color", this.fromColor);
		if (this.toColor != null)
			particleData.put("To_color", this.toColor);
		particleData.put("Particle_size", this.size);
		return particleData;
	}

	/**
	 * Deserializes a {@link ParticleDustOptions} instance from a map of key-value pairs.
	 * <p>
	 * The map is expected to contain the following keys:
	 * <ul>
	 *   <li><b>"From_color"</b> – the starting {@link Color}</li>
	 *   <li><b>"To_color"</b> – the ending {@link Color}</li>
	 *   <li><b>"Particle_size"</b> – an optional {@link Double} specifying the particle size;
	 *       defaults to {@code 1} if not present, or {@code 0.5} if {@code null}</li>
	 * </ul>
	 * </p>
	 *
	 * @param map a map containing particle dust configuration values
	 * @return a new {@link ParticleDustOptions} object based on the provided map
	 */
	public static ParticleDustOptions deserialize(final Map<String, Object> map) {
		final Color fromColor = (Color) map.get("From_color");
		final Color toColor = (Color) map.get("To_color");
		final Double size = (Double) map.getOrDefault("Particle_size", 1);

		return new ParticleDustOptions(fromColor, toColor, (float) (size == null ? 0.5 : size));
	}
}


