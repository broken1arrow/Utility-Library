package org.broken.arrow.color.library.utility;

import org.broken.arrow.color.library.ChatColors;
import org.broken.arrow.library.logging.Logging;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.broken.arrow.color.library.ChatColors.COLOR_AMPERSAND;

public class StringUtility {

	private static final Logging LOG = new Logging(StringUtility.class);

	private StringUtility() {
	}

	/**
	 * Convert RGB to hex.
	 *
	 * @param color the color you want to convert to hex.
	 * @return hex color or 0 if RGB values are over 255 or below 0.
	 */
	public static String convertColorToHex(Color color) {
		StringBuilder hex = new StringBuilder(String.format("%06X", color.getRGB() & 0xffffff));
		hex.insert(0, "#");
		return hex.toString();
	}

	/**
	 * Check if it valid color symbol.
	 *
	 * @param letter check color symbol.
	 * @return true if it is valid color symbol.
	 */
	public static boolean checkIfColor(char letter) {

		for (char color : ChatColors.getAllColorCodes())
			if (color == letter)
				return true;
		return false;
	}

	/**
	 * Check if it is a valid hex or not.
	 *
	 * @param str you want to check
	 * @return true if it valid hex color.
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
	 * Get values inside &lt; &gt;.
	 *
	 * @param string the string to check for it.
	 * @param end    where it shall stop split the hex.
	 * @return null if not exist or list of values.
	 */
	public static String[] getValuesInside(String string, int end) {
		int start = string.indexOf("<") + 1;
		if (end < 0) return new String[]{};

		return string.substring(start, end).split(":");
	}

	public static String getHexFromString(String hex, int from, int to) {
		return hex.substring(from, to);
	}

	public static String getHexFromString(String hex, int from) {
		return hex.substring(from);
	}

	/**
	 * Convert hex to RGB.
	 *
	 * @param colorStr hex you want to transform.
	 * @return the Colors instance with set colors from the hex string.
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

	public static int getNextColor(String subMessage) {
		int nextGrad = subMessage.indexOf("<#");
		int vanillaColor = checkIfContainsColor(subMessage);
		if (nextGrad < 0)
			return vanillaColor;
		if (vanillaColor < 0)
			return nextGrad;

		return Math.min(nextGrad, vanillaColor);
	}

	public static int getEndOfColor(String subMessage) {
		int nextGrad = subMessage.indexOf(">");
		int vanillaColor = checkIfContainsColor(subMessage);

		return Math.max(nextGrad, vanillaColor);
	}

	public static Double[] checkPortions(Color[] colorList, Double[] portionsList) {
		if (colorList == null || portionsList == null) return new Double[0];
		if (colorList.length == portionsList.length) return new Double[0];
		double num = 0.0;
		for (int i = 0; i < portionsList.length; i++) {
			Double number = portionsList[i];
			if (number == null) {
				portionsList[i] = 0.0;
				number = 0.0;
			}
			num += number;
			if (num > 1.0) {
				portionsList[i] = Math.round((1.0 - (num - number)) * 100.0) / 100.0;
			}
		}
		return portionsList;
	}

	public static String getStringStriped(String message, int startIndex, int endIndex) {
		String subColor = message.substring(startIndex);
		final String substring = subColor.substring(0, endIndex > 0 ? endIndex + 1 : message.length());
		return message.replace(substring, "");
	}

	public static String[] getMultiColors(String message, int startIndex) {
		String subcolor = message.substring(startIndex);
		int endOfColor = subcolor.indexOf(">");
		final String substring = subcolor.substring(0, endOfColor > 0 ? endOfColor + 1 : subcolor.length());
		return substring.substring(1, substring.length() - 1).split(":");
	}
}
