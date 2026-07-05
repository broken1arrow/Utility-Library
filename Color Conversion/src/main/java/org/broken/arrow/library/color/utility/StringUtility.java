package org.broken.arrow.library.color.utility;

import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.logging.Logging;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.broken.arrow.library.color.ChatColors.COLOR_AMPERSAND;

/**
 * Utility class for working with strings containing Minecraft-style color codes,
 * including legacy color codes,these modules hexadecimal colors, and extracting parts of text
 * related to color formatting.
 */
public class StringUtility {

	private static final Logging LOG = new Logging(StringUtility.class);

	private StringUtility() {
	}

	/**
	 * Convert a {@link Color} object to its hexadecimal string representation.
	 *
	 * @param color the {@link Color} instance to convert
	 * @return the hex string in the format "#RRGGBB"
	 */
	public static String convertColorToHex(Color color) {
		StringBuilder hex = new StringBuilder(String.format("%06X", color.getRGB() & 0xffffff));
		hex.insert(0, "#");
		return hex.toString();
	}

	/**
	 * Check if a given character is a valid Minecraft color code symbol.
	 *
	 * @param letter the character to check
	 * @return true if the character is a valid color code, false otherwise
	 */
	public static boolean checkIfColor(char letter) {

		for (char color : ChatColors.getAllColorCodes())
			if (color == letter)
				return true;
		return false;
	}

	/**
	 * Validate if the given string is a proper hexadecimal color code.
	 * Supports 3 or 6 digit hex codes with a leading '#'.
	 *
	 * @param str the string to validate
	 * @return true if the string is a valid hex color code, false otherwise
	 */
	public static boolean isValidHexCode(String str) {
		// Regex to check valid hexadecimal color code.
		String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

		// If the string is empty
		// return false
		if (str == null) {
			return false;
		}

		// Compile the ReGex
		Pattern pattern = Pattern.compile(regex);

		// Pattern class contains matcher() method
		// to find matching between given string
		// and regular expression.
		Matcher matcher = pattern.matcher(str);

		// Return if the string
		// matched the ReGex
		return matcher.matches();
	}

	/**
	 * Checks if the given message contains a Minecraft color code.
	 *
	 * @param message the text to search within
	 * @return the index where the color code starts, or -1 if none found
	 */
	public static int checkIfContainsColor(String message) {
		int index = message.indexOf(ChatColors.COLOR_CHAR);
		if (index < 0)
			index = message.indexOf(COLOR_AMPERSAND);
		if (index < 0) return -1;
		if (index + 1 > message.length()) return -1;

		char charColor = message.charAt(index + 1);
		for (char color : ChatColors.getAllCharColorCodes())
			if (color == charColor)
				return index;
		return -1;
	}

	/**
	 * Extracts the values contained inside angle brackets (&lt; and &gt;) up to a specified end index.
	 *
	 * @param string the input string containing the values
	 * @param end the end index where extraction stops (exclusive)
	 * @return an array of strings split by ':' or an empty array if not found
	 */
	public static String[] getValuesInside(String string, int end) {
		int start = string.indexOf("<") + 1;
		if (end < 0) return new String[]{};

		return string.substring(start, end).split(":");
	}
	/**
	 * Extracts a substring between two indices.
	 *
	 * @param hex the string containing the hex code
	 * @param from the start index (inclusive)
	 * @param to the end index (exclusive)
	 * @return the extracted substring
	 */
	public static String getHexFromString(String hex, int from, int to) {
		return hex.substring(from, to);
	}

	/**
	 * Extracts a substring from a start index to the end.
	 *
	 * @param hex the string containing the hex code
	 * @param from the start index (inclusive)
	 * @return the extracted substring from 'from' to the end
	 */
	public static String getHexFromString(String hex, int from) {
		return hex.substring(from);
	}

	/**
	 * Converts a hexadecimal color string to a {@link Color} object.
	 * Supports shorthand 3-digit hex (#RGB) and full 6-digit hex (#RRGGBB).
	 * If invalid, returns white color and logs a warning.
	 *
	 * @param colorStr the hex color string, e.g. "#FFF" or "#FFFFFF"
	 * @return a {@link Color} object representing the hex color
	 */
	public static Color hexToRgb(String colorStr) {
		if (colorStr.length() == 4) {
			String red = colorStr.substring(1, 2);
			String green = colorStr.substring(2, 3);
			String blue = colorStr.substring(3, 4);
			return new Color(
					Integer.valueOf(red + red, 16),
					Integer.valueOf(green + green, 16),
					Integer.valueOf(blue + blue, 16));
		}
		if (colorStr.length() < 7) {
			LOG.log(() -> "This `" + colorStr + "` hex color is not valid, set color to white.");
			return new Color(Color.WHITE.getRGB());
		}
		return new Color(
				Integer.valueOf(colorStr.substring(1, 3), 16),
				Integer.valueOf(colorStr.substring(3, 5), 16),
				Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	/**
	 * Finds the position of the next hex color or color code in the given substring.
	 *
	 * @param subMessage the substring to check for color codes
	 * @return the lowest index of the next color code or -1 if none found
	 */
	public static int getNextColor(String subMessage) {
		int nextGrad = subMessage.indexOf("<#");
		int vanillaColor = checkIfContainsColor(subMessage);
		if (nextGrad < 0)
			return vanillaColor;
		if (vanillaColor < 0)
			return nextGrad;

		return Math.min(nextGrad, vanillaColor);
	}

	/**
	 * Finds the index in the string where a hex code ends.
	 *
	 * @param subMessage the substring to check
	 * @return the index position where the color code ends
	 */
	public static int getEndOfColor(String subMessage) {
		int nextGrad = subMessage.indexOf(">");
		int vanillaColor = checkIfContainsColor(subMessage);

		return Math.max(nextGrad, vanillaColor);
	}

}
