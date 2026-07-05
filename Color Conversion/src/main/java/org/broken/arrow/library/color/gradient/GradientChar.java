package org.broken.arrow.library.color.gradient;

import javax.annotation.Nonnull;

/**
 * Represents a single character within a gradient string paired with its computed hex color.
 * <p>
 * This immutable data class is used after gradient interpolation to map individual text
 * characters to their final hex color values before they are rendered or packed into
 * components.
 */
public class GradientChar {
	private final char character;
	private final String hex;

	/**
	 * Constructs a new {@code GradientChar} mapping a specific character to its color.
	 *
	 * @param character the individual text character
	 * @param hex       the calculated hexadecimal color code (e.g., "#FF0000") for this character
	 */
	public GradientChar(final char character,@Nonnull final String hex) {
		this.character = character;
		this.hex = hex;
	}

	/**
	 * Retrieves the text character.
	 *
	 * @return the raw character
	 */
	public char getCharacter() {
		return character;
	}

	/**
	 * Retrieves the hex string color code assigned to this character.
	 *
	 * @return the hexadecimal color string
	 */
	public String getHex() {
		return hex;
	}
}