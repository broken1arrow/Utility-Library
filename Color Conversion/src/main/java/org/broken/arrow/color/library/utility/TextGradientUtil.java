package org.broken.arrow.color.library.utility;

import com.google.common.base.Preconditions;
import org.broken.arrow.color.library.ChatColors;
import org.broken.arrow.color.library.TextTranslator.GradientType;
import org.broken.arrow.color.library.interpolate.GradientInterpolation;
import org.broken.arrow.color.library.interpolate.Interpolator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.broken.arrow.color.library.ChatColors.COLOR_AMPERSAND;
import static org.broken.arrow.color.library.utility.StringUtility.*;

/**
 * Utility class for working with text gradients.
 * This class provides methods to split text based on gradient patterns and to apply various types of gradient effects
 * to text, including RGB and HSV gradients.
 */
public class TextGradientUtil {
	private static final Pattern GRADIENT_PATTERN = Pattern.compile("(<#[a-fA-F0-9]{6}:#[a-fA-F0-9]{6}>)");
	private final GradientType type;
	private final String text;
	private boolean firstMatch;
	private static String deliminator = "_,_";

	public TextGradientUtil(GradientType type, String text) {
		this.type = type;
		this.text = text;
	}

	/**
	 * Splits the input text on every gradient match, excluding the first match.
	 *
	 * @return An array of strings obtained by splitting the input text on gradient matches, excluding the first match.
	 */
	public String[] splitOnGradient() {
		String originalText = this.text;
		this.firstMatch = false;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < originalText.length(); i++) {
			char currentChar = originalText.charAt(i);
			if (currentChar == 'g' && i + 8 < originalText.length()) {
				firstMatch = processGradientMatch(originalText,  builder, i);
			}
			builder.append(currentChar);
		}
		return builder.toString().split(deliminator);
	}

	/**
	 * Check if it first match or second match.
	 * @param text the text you want to add.
	 * @param builder to build the text
	 * @param currentIndex index where it should check in the string after the color.
	 * @return true if it first match.
	 */
	private boolean processGradientMatch(final String text, final StringBuilder builder, final int currentIndex) {
		boolean match = this.firstMatch;
		boolean isGradient = nextGradientMatch(text, currentIndex);
		if (isGradient) {
			if (!match) {
				match = true;
			} else {
				builder.append(deliminator);
			}
		}
		return match;
	}

	private boolean nextGradientMatch(final String text, final int currentIndex) {
		boolean isGradient = false;
		StringBuilder build = new StringBuilder();
		for (int check = (currentIndex > 0 ? currentIndex - 1 : 0); check < text.length(); check++) {
			build.append(text.charAt(check));
			isGradient = build.indexOf(this.type.getType() + "<") >= 0;
			if (isGradient || check > 8 + currentIndex)
				break;
		}
		return isGradient;
	}

	/**
	 * Converts the input text to a new string with added gradient colors and unaffected text after a normal color that starts with
	 * §/&amp; or hex color &lt;#55F758&gt;.
	 *
	 * @param text The text to be checked and translated.
	 * @return A string with gradient colors applied to each letter as specified to either next color or length of the string.
	 */
	public String convertToMultiGradients(String text) {
		GradientType gradientType = this.type;
		if (gradientType != null) {
			Double[] portionsList = null;

			int startIndex = text.indexOf(gradientType.getType());
			String subColor = text.substring(startIndex);
			int subIndex = subColor.indexOf("<");
			int multiBalance = subColor.indexOf("_portion");
			int endOfColor = subColor.indexOf(">");

			text = getStringStriped(text, startIndex, endOfColor);
			if (multiBalance > 0) {
				String portion = subColor.substring(multiBalance);
				int end = portion.indexOf(">");
				portionsList = Arrays.stream(getValuesInside(portion, end)).map(Double::parseDouble).toArray(Double[]::new);
				text = text.replace(portion.substring(0, end + 1), "");
			}
			Color[] colorList = Arrays.stream(getMultiColors(subColor, subIndex)).map(StringUtility::hexToRgb).toArray(Color[]::new);
			text = text.replace(subColor.substring(0, text.length()), "");

			StringBuilder builder = new StringBuilder();
			int end = getNextColor(text);
			if (startIndex > 0)
				builder.append(text, 0, startIndex);
			builder.append(multiRgbGradient(gradientType, text.substring(Math.max(startIndex, 0), end > 0 ? end : text.length()), colorList, checkPortions(colorList, portionsList)));
			if (end > 0)
				builder.append(text, Math.max(end, 0), text.length());
			return builder.toString();


		}
		return text;
	}

	/**
	 * Converts the gradient-encoded portions of the input text to a new string with gradient colors applied.
	 * Will end the gradient with next color or the original string length.
	 *
	 * @return A string with gradient colors applied to the specified portions of the input text.
	 */
	public String convertGradients() {
		StringBuilder textBuilder = new StringBuilder();
		String message = this.text;
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);
		String subMessages = null;
		while (gradientsMatcher.find()) {
			String match = gradientsMatcher.group(0);
			String hexRaw = match.substring(1, match.length() - 1);
			int splitPos = hexRaw.indexOf(":");

			int nextGrads = getLastGradientMatch(message);
			int nextGradientMatch = getFirstGradientMatch(message.substring(nextGrads + 1)) + match.length() + 1;

			int startOfMatchingString = gradientsMatcher.start() >= 0 ? (gradientsMatcher.start() + match.length()) : 0;
			int endOfMatchingString = nextGradientMatch > 0 && nextGradientMatch > startOfMatchingString && nextGrads != nextGradientMatch ? nextGradientMatch : message.length();


			String subMessage = message.substring(startOfMatchingString, endOfMatchingString);
			int nextGrad = getNextColor(subMessage);
			if (nextGrad > 0) {
				subMessages = subMessage.substring(nextGrad);
				subMessage = subMessage.substring(0, nextGrad);
			}
			if (startOfMatchingString > 0 && gradientsMatcher.start() > 0 && textBuilder.length() == 0) {
				textBuilder.append(message, 0, gradientsMatcher.start());
			}
			textBuilder.append(this.applyGradient(subMessage, this.getRBGGradient(hexToRgb(getHexFromString(hexRaw, 0, splitPos)), hexToRgb(getHexFromString(hexRaw, splitPos + 1)))));
		}
		if (subMessages != null)
			textBuilder.append(subMessages);
		return textBuilder.toString();
	}

	/**
	 * This method can you set balance between different gradient colors. Recommend at least 3 colors
	 * to be set for best visual effect.
	 * <p>
	 * The format look like this. gradients_&lt;#D16BA5:#86A8E7:#5FFBF1&gt;_portion&lt;0.2:0.5:0.3&gt;
	 *
	 * @param type     the type of gradients you want to set.
	 * @param str      the text you want to colorize.
	 * @param colors   the list of colors to set.
	 * @param portions amount of each color to set.
	 * @return a string with the colors set.
	 */
	public String multiRgbGradient(GradientType type, String str, Color[] colors, @Nullable Double[] portions) {
		return multiRgbGradient(type, str, colors, portions, new GradientInterpolation());
	}

	/**
	 * This method can you set balance between different gradient colors. Recommend at least 3 colors
	 * to be set for best visual effect.
	 * <p>
	 * The format look like this. gradients_&lt;#D16BA5:#86A8E7:#5FFBF1&gt;_portion&lt;0.2:0.5:0.3&gt;
	 *
	 * @param type         the type of gradients you want to set.
	 * @param str          the text you want to colorize.
	 * @param colors       the list of colors to set.
	 * @param portions     amount of each color to set.
	 * @param interpolator if you want to change how colors is calculated.
	 * @return a string with the colors set.
	 */
	public String multiRgbGradient(GradientType type, String str, Color[] colors, @Nullable Double[] portions, Interpolator interpolator) {
		if (colors.length < 2) {
			return (colors.length == 1 ? "<" + convertColorToHex(colors[0]) + ">" + str : str);
		}
		final Double[] p;
		if (portions == null || portions.length == 0) {
			p = new Double[colors.length - 1];
			Arrays.fill(p, 1 / (double) p.length);
		} else {
			p = portions;
		}
		Preconditions.checkArgument(p.length == colors.length - 1);

		final StringBuilder builder = new StringBuilder();
		int strIndex = 0;
		for (int i = 0; i < colors.length - 1; i++) {
			if (type == GradientType.SIMPLE_GRADIENT_PATTERN)
				builder.append(this.applyGradient(
						str.substring(strIndex, strIndex + (int) (p[i] * str.length())),
						this.getRBGGradient(colors[i], colors[i + 1]),
						interpolator));
			if (type == GradientType.HSV_GRADIENT_PATTERN)
				builder.append(this.applyGradient(
						str.substring(strIndex, strIndex + (int) (p[i] * str.length())),
						this.getHSBGradient(colors[i], colors[i + 1]),
						interpolator));
			strIndex += p[i] * str.length();
		}
		setMultiGradient(type,  interpolator ,str, colors, builder, strIndex);
		return builder.toString();
	}

	private void setMultiGradient(final GradientType type,Interpolator interpolator, final String str, final Color[] colors, final StringBuilder builder, final int strIndex) {
		if (strIndex < str.length()) {
			if (type == GradientType.SIMPLE_GRADIENT_PATTERN)
				builder.append(this.applyGradient(
						str.substring(strIndex),
						this.getRBGGradient(colors[colors.length - 1], colors[colors.length - 1]),
						(from, to, max) -> interpolator.quadratic(from, to, str.length(), true)));
			if (type == GradientType.HSV_GRADIENT_PATTERN)
				builder.append(this.applyGradient(
						str.substring(strIndex),
						this.getHSBGradient(colors[colors.length - 1], colors[colors.length - 1]),
						(from, to, max) -> interpolator.quadratic(from, to, str.length(), true)));
		}
	}

	private String multiHsvQuadraticGradient(String str,Interpolator interpolator, boolean first) {
		final StringBuilder builder = new StringBuilder();

		builder.append(this.applyGradient(
				str.substring(0, (int) (0.2 * str.length())),
				this.getHSBGradient(Color.RED,
				Color.GREEN),
				(from, to, max) -> interpolator.quadratic(from, to, max, first)
		));

		for (int i = (int) (0.2 * str.length()); i < (int) (0.8 * str.length()); i++) {
			builder.append(ChatColors.of(Color.GREEN)).append(str.charAt(i));
		}

		builder.append(this.applyGradient(
				str.substring((int) (0.8 * str.length())),
				this.getHSBGradient(Color.GREEN,
				Color.RED),
				(from, to, max) -> interpolator.quadratic(from, to, max, !first)
		));

		return builder.toString();

	}


	/**
	 * Apply the gradient pattern to the given string using the specified color interpolator.
	 *
	 * @param str               The input string to apply the gradient pattern to.
	 * @param colorInterpolator The color interpolator providing the gradient colors.
	 * @return The string with the gradient colors applied.
	 */
	public String applyGradient(String str, @Nonnull ColorInterpolator colorInterpolator) {
		return this.applyGradient(str, colorInterpolator, new GradientInterpolation());
	}

	/**
	 * Apply the gradient pattern to the given string using the specified color interpolator and interpolator.
	 *
	 * @param str The input string to apply the gradient pattern to.
	 * @param colorInterpolator The color interpolator providing the gradient colors.
	 * @param interpolator The interpolator for color calculation.
	 * @return The string with the gradient colors applied.
	 */
	public String applyGradient(String str, @Nonnull ColorInterpolator colorInterpolator, Interpolator interpolator) {
		// interpolate each component separately
		final double[] red = colorInterpolator.getRedColors(interpolator, str.length());
		final double[] green = colorInterpolator.getGreenColors(interpolator, str.length());
		final double[] blue = colorInterpolator.getBlueColors(interpolator, str.length());


		final StringBuilder builder = new StringBuilder();
		final char[] letters = str.toCharArray();
		// create a string that matches the input-string but has
		// the different color applied to each char
		String lastDecoration = "";

		for (int i = 0; i < letters.length; i++) {
			char letter = letters[i];
			if ((letter == org.bukkit.ChatColor.COLOR_CHAR || letter == COLOR_AMPERSAND) && i + 1 < letters.length) {
				final char decoration = Character.toLowerCase(letters[i + 1]);
				lastDecoration = getColorCode(lastDecoration, decoration);
				continue;
			}

			final String stepColor = colorInterpolator.apply((int) Math.round(red[i]), (int) Math.round(green[i]), (int) Math.round(blue[i]));
			boolean isEmpty = letter == ' ' && lastDecoration.isEmpty();

			builder.append(isEmpty ? "" : "<").append(isEmpty ? "" : stepColor).append(isEmpty ? "" : ">").append(lastDecoration).append(letter);
		}
		return builder.toString();
	}

	private String getColorCode(String lastDecoration, final char decoration) {
		if (decoration == 'k')
			lastDecoration = "&k";

		else if (decoration == 'l')
			lastDecoration = "&l";

		else if (decoration == 'm')
			lastDecoration = "&m";

		else if (decoration == 'n')
			lastDecoration = "&n";

		else if (decoration == 'o')
			lastDecoration = "&o";

		else if (decoration == 'r')
			lastDecoration = "";
		return lastDecoration;
	}

	public int getLastGradientMatch(String message) {
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);

		if (gradientsMatcher.find())
			return gradientsMatcher.end();
		return -1;
	}

	public int getFirstGradientMatch(String message) {
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);

		if (gradientsMatcher.find())
			return gradientsMatcher.start();
		return -1;
	}

	/**
	 * Generate a color interpolator for an RGB gradient between two colors.
	 *
	 * @param from The starting color.
	 * @param to   The ending color.
	 * @return The color interpolator for the RGB gradient.
	 */
	public ColorInterpolator getRBGGradient(Color from, Color to) {
		return new ColorInterpolator() {
			@Override
			public double[] getRedColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(from.getRed(), to.getRed(), stringLength);
			}

			@Override
			public double[] getGreenColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(from.getGreen(), to.getGreen(), stringLength);
			}

			@Override
			public double[] getBlueColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(from.getBlue(), to.getBlue(), stringLength);
			}

			@Override
			public String apply(double red, double green, double blue) {
				return convertColorToHex(new Color((int) red, (int) green, (int) blue));
			}
		};
	}

	/**
	 * Generate a color interpolator for an HSB gradient between two colors.
	 *
	 * @param from The starting color.
	 * @param to   The ending color.
	 * @return The color interpolator for the HSB gradient.
	 */
	public ColorInterpolator getHSBGradient(Color from, Color to) {
		final float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), null);
		final float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), null);

		return new ColorInterpolator() {
			@Override
			public double[] getRedColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(hsvFrom[0], hsvTo[0], stringLength);
			}

			@Override
			public double[] getGreenColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(hsvFrom[1], hsvTo[1], stringLength);
			}

			@Override
			public double[] getBlueColors(@Nonnull final Interpolator interpolator, final int stringLength) {
				return interpolator.interpolate(hsvFrom[2], hsvTo[2], stringLength);
			}

			@Override
			public String apply(double hue, double saturation, double brightness) {
				return convertColorToHex(Color.getHSBColor((float) hue, (float) saturation, (float) brightness));
			}
		};
	}

	/**
	 * Change the default string used to split gradients
	 *
	 * @param deliminator the text string should be used for split the message
	 *                       at the gradients.
	 */
	public static void setDeliminator(final String deliminator) {
		TextGradientUtil.deliminator = deliminator;
	}

}
