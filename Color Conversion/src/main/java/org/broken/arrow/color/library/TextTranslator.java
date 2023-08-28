package org.broken.arrow.color.library;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.broken.arrow.color.library.ChatColors.COLOR_AMPERSAND;

public final class TextTranslator implements Interpolator {
	private static final Pattern HEX_PATTERN = Pattern.compile("(?<!\\\\\\\\)(<#[a-fA-F0-9]{6}>)|(?<!\\\\\\\\)(<#[a-fA-F0-9]{3}>)");
	private static final Pattern GRADIENT_PATTERN = Pattern.compile("(<#[a-fA-F0-9]{6}:#[a-fA-F0-9]{6}>)");
	private static final TextTranslator instance = new TextTranslator();

	public static TextTranslator getInstance() {
		return instance;
	}

	/**
	 * Type your message/string text here. you use this format for colors:
	 * <ul>
	 * <li> For vanilja color codes <strong>&amp; or &#167;</strong> and the color code.</li>
	 * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
	 * <li> For normal gradiens <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
	 * <li> For hsv use <strong>gradiens_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
	 * <li> For use multicolors <strong>gradiens_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
	 * <li> For change balance beetween colors add this to the end of gradiens or gradiens_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
	 *  Like this <strong>gradiens_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
	 *  If you not add this it will have even balance beteen colors.</li>
	 * </ul>
	 *
	 * @param message your string message.
	 * @return spigot compatible translation.
	 */
	public static String toSpigotFormat(String message) {
		return getInstance().spigotFormat(message);
	}

	/**
	 * This is for component when you want to send message
	 * thru vanilla minecraft MNS for example. DO NOT WORK IN SPIGOT API. Use {@link #toSpigotFormat(String)}
	 * <br> You use this format for colors:<br>
	 * <ul>
	 * <li> For vanilja color codes <strong>&amp; or &#167;</strong> and the color code.</li>
	 * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
	 * <li> For normal gradiens <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
	 * <li> For hsv use <strong>gradiens_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
	 * <li> For use multicolors <strong>gradiens_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
	 * <li> For change balance beetween colors add this to the end of gradiens or gradiens_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
	 *  Like this <strong>gradiens_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
	 *  If you not add this it will have even balance beteen colors.</li>
	 * </ul>
	 *
	 * @param message      your string message.
	 * @param defaultColor set default color when colors are not set in the message.
	 * @return json object with the set colors.
	 */
	public static JsonObject toComponent(String message, String defaultColor) {
		return getInstance().componentFormat(message, defaultColor);
	}

	/**
	 * This is for component when you want to send message
	 * thru vanilla minecraft MNS for example. DO NOT WORK IN SPIGOT API. Use {@link #toSpigotFormat(String)}
	 * <br> You use this format for colors:<br>
	 * <ul>
	 * <li> For vanilja color codes <strong>&amp; or &#167;</strong> and the color code.</li>
	 * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
	 * <li> For normal gradiens <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
	 * <li> For hsv use <strong>gradiens_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
	 * <li> For use multicolors <strong>gradiens_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
	 * <li> For change balance beetween colors add this to the end of gradiens or gradiens_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
	 *  Like this <strong>gradiens_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
	 *  If you not add this it will have even balance beteen colors.</li>
	 * </ul>
	 *
	 * @param message your string message.
	 * @return json object with the set colors.
	 */

	public static JsonObject toComponent(String message) {
		return getInstance().componentFormat(message, null);
	}

	/**
	 * This is for component when you want to send message
	 * thru vanilla minecraft MNS for example..DO NOT WORK IN SPIGOT API. Use {@link #toSpigotFormat(String)}
	 *
	 * @param message      your string message.
	 * @param defaultColor set default color when colors are not set in the message.
	 * @return json object with the set colors.
	 */

	private JsonObject componentFormat(String message, String defaultColor) {
		JsonArray jsonArray = new JsonArray();
		Component.Builder component = new Component.Builder();
		message = checkStringForGradient(message);

		if (defaultColor == null || defaultColor.equals(""))
			defaultColor = "white";

		StringBuilder builder = new StringBuilder(message.length());
		StringBuilder hex = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			char letter = message.charAt(i);
			boolean checkChar;
			boolean checkHex = false;

			if (i + 1 < message.length() && letter == ChatColors.COLOR_CHAR || letter == '&' || letter == '<') {
				char msg = message.charAt(i + 1);

				if (checkIfColor(msg)) {
					checkChar = true;
				} else if (msg == '#') {
					hex = new StringBuilder();
					for (int j = 0; j < 7; j++) {
						hex.append(message.charAt(i + 1 + j));
					}
					boolean isHexCode = isValidHexCode(hex.toString());
					checkChar = isHexCode;
					checkHex = isHexCode;
				} else checkChar = false;
			} else checkChar = false;

			if (checkChar) {
				if (++i >= message.length()) {
					break;
				}
				letter = message.charAt(i);

				if (letter >= 'A' && letter <= 'Z') {
					letter += 32;
				}
				String format;
				if (checkHex) {
					format = hex.toString();
					i += 7;
				} else {
					try {
						format = ChatColors.getByChar(letter).getName();
					} catch (Exception ignore) {
						format = null;
					}
				}
				if (format == null) {
					continue;
				}
				if (builder.length() > 0) {
					component.message(builder.toString());
					builder = new StringBuilder();
					jsonArray.add(component.build().toJson());
					component = new Component.Builder();

				}
				if (format.equals(ChatColors.BOLD.getName())) {
					component.bold(true);
				} else if (format.equals(ChatColors.ITALIC.getName())) {
					component.italic(true);
				} else if (format.equals(ChatColors.UNDERLINE.getName())) {
					component.underline(true);
				} else if (format.equals(ChatColors.STRIKETHROUGH.getName())) {
					component.strikethrough(true);
				} else if (format.equals(ChatColors.MAGIC.getName())) {
					component.obfuscated(true);
				} else if (format.equals(ChatColors.RESET.getName())) {
					format = defaultColor;
					component.reset(true);
					component.colorCode(format);
				} else {
					component.colorCode(format);
				}
				continue;
			}
			builder.append(letter);
		}

		component.message(builder.toString());
		jsonArray.add(component.build().toJson());

		if (jsonArray.size() > 1) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("extra", jsonArray);
			jsonObject.addProperty("text", "");
			return jsonObject;
		}
		return component.build().toJson();
	}

	/**
	 * Type your message/string text here. you use
	 * <p>
	 * §/& colorcode or <#55F758> for normal hex and
	 * <#5e4fa2:#f79459> for gradient (use anny colorcode to stop gradient).
	 *
	 * @param message your string message.
	 * @return spigot compatible translation.
	 */

	private String spigotFormat(String message) {
		String messageCopy = checkStringForGradient(message);
		Matcher matcher = HEX_PATTERN.matcher(messageCopy);

		while (matcher.find()) {
			String match = matcher.group(0);
			int firstPos = match.indexOf("#");
			if (!(match.length() >= 9))
				messageCopy = messageCopy.replace(match, "&x&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 3) + "&" + match.charAt(firstPos + 3));
			else
				messageCopy = messageCopy.replace(match, "&x&" + match.charAt(firstPos + 1) + "&" + match.charAt(firstPos + 2) + "&" + match.charAt(firstPos + 3) + "&" + match.charAt(firstPos + 4) + "&" + match.charAt(firstPos + 5) + "&" + match.charAt(firstPos + 6));
		}
		return ChatColor.translateAlternateColorCodes('&', messageCopy);
	}

	/**
	 * Check the string if it contains gradiens.
	 * <ul>
	 * <li> For vanilja color codes <strong>& or §</strong> and the color code.</li>
	 * <li> For hex <strong><#5e4fa2></strong> </li>
	 * <li> For normal gradiens <strong><#5e4fa2:#f79459></strong> </li>
	 * <li> For hsv use <strong>gradiens_hsv_<#5e4fa2:...></strong> add at least 2 colors or more</li>
	 * <li> For use multicolors <strong>gradiens_<#6B023E:...></strong>add at least 2 colors or more </li>
	 * <li> For change balance beetween colors add this to the end of gradiens or gradiens_hsv <strong>_portion<0.2:0.6:0.2></strong>
	 *  Like this <strong>gradiens_<#6B023E:#3360B3:#fc9:#e76424>_portion<0.2:0.6:0.2></strong> ,
	 *  If you not add this it will have even balance beteen colors.</li>
	 * </ul>
	 *
	 * @param message to check.
	 * @return the message translated if has any gradiens or untouched.
	 */
	private String checkStringForGradient(final String message) {
		String messageCopy = message;
		GradientType type = null;
		if (message.contains(GradientType.HSV_GRADIENT_PATTERN.getType()))
			type = GradientType.HSV_GRADIENT_PATTERN;
		if (message.contains(GradientType.SIMPLE_GRADIENT_PATTERN.getType()))
			type = GradientType.SIMPLE_GRADIENT_PATTERN;
		if (type != null) {
			StringBuilder builder = new StringBuilder();
			for (String messag : splitOnGradient(type, message))
				builder.append(convertToMulitGradiens(type, messag));
			messageCopy = builder.toString();
		}
		if (messageCopy == null)
			messageCopy = message;
		Matcher matcherGradient = GRADIENT_PATTERN.matcher(messageCopy);
		if (matcherGradient.find()) {
			messageCopy = convertGradiens(messageCopy, GradientType.SIMPLE_GRADIENT_PATTERN);
		}
		return messageCopy;
	}

	/**
	 * Create a string with colors added on every letter
	 * (some are after the color code and to message length or to &amp;r).
	 *
	 * @param type    Gradient type you want to apply on the text.
	 * @param message message you want to check and translate
	 * @return string with added gradient and rest unaffected some are outside the gradient range.
	 */

	public String convertToMulitGradiens(GradientType type, String message) {
		if (type != null) {
			Double[] portionsList = null;

			int startIndex = message.indexOf(type.getType());
			String subcolor = message.substring(startIndex);
			int subIndex = subcolor.indexOf("<");
			int multi_balance = subcolor.indexOf("_portion");
			int endOfColor = subcolor.indexOf(">");

			message = getStringStriped(message, startIndex, endOfColor);
			if (multi_balance > 0) {
				String portion = subcolor.substring(multi_balance);
				int end = portion.indexOf(">");
				portionsList = Arrays.stream(getValuesInside(portion, end)).map(Double::parseDouble).toArray(Double[]::new);
				message = message.replace(portion.substring(0, end + 1), "");
			}
			Color[] colorList = Arrays.stream(getMultiColors(subcolor, subIndex)).map(TextTranslator::hexToRgb).toArray(Color[]::new);
			message = message.replace(subcolor.substring(0, message.length()), "");


			//MultiGradients multiGradients = new MultiGradients(striped, colorList, portionsList);
			StringBuilder builder = new StringBuilder();
			int end = getNextColor(message);
			int nextEnd = getNextColor(message.substring(end + 1));
			if (startIndex > 0)
				builder.append(message, 0, startIndex);
			builder.append(multiRgbGradient(type, message.substring(Math.max(startIndex, 0), end > 0 ? end : message.length()), colorList, checkportions(colorList, portionsList)));
			if (end > 0)
				builder.append(message, Math.max(end, 0), message.length());
			return builder.toString();


		}
		return message;
	}

	public String convertGradiens(String message, GradientType type) {
		StringBuilder gradient = new StringBuilder();
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);
		String subMessages = null;
		while (gradientsMatcher.find()) {
			String match = gradientsMatcher.group(0);
			String hexRaw = match.substring(1, match.length() - 1);
			int splitPos = hexRaw.indexOf(":");

			int nextGrads = getLastGradientMatch(message, type);
			int nextGradientMatch = getFirstGradientMatch(message.substring(nextGrads + 1), type) + match.length() + 1;

			String subMessag = message.substring(gradientsMatcher.start() >= 0 ? (gradientsMatcher.start() + match.length()) : 0, nextGradientMatch > 0 && gradientsMatcher.start() < nextGradientMatch && nextGrads != nextGradientMatch ? nextGradientMatch : message.length());
			int nextGrad = getNextColor(subMessag);
			if (nextGrad > 0) {
				subMessages = subMessag.substring(nextGrad);
				subMessag = subMessag.substring(0, nextGrad);
			}
			gradient.append(rgbGradient(subMessag, hexToRgb(getHexFromString(hexRaw, 0, splitPos)), hexToRgb(getHexFromString(hexRaw, splitPos + 1))));
		}
		if (subMessages != null)
			gradient.append(subMessages);
		return gradient.toString();
	}


	private String multiHsvQuadraticGradient(String str, boolean first) {
		final StringBuilder builder = new StringBuilder();

		builder.append(hsvGradient(
				str.substring(0, (int) (0.2 * str.length())),
				Color.RED,
				Color.GREEN,
				(from, to, max) -> quadratic(from, to, max, first)
		));

		for (int i = (int) (0.2 * str.length()); i < (int) (0.8 * str.length()); i++) {
			builder.append(ChatColors.of(Color.GREEN)).append(str.charAt(i));
		}

		builder.append(hsvGradient(
				str.substring((int) (0.8 * str.length())),
				Color.GREEN,
				Color.RED,
				(from, to, max) -> quadratic(from, to, max, !first)
		));

		return builder.toString();

	}

	public String hsvGradient(String str, Color from, Color to) {
		return hsvGradient(str, from, to, this);
	}

	public String hsvGradient(String str, Color from, Color to, Interpolator interpolator) {
		// returns a float-array where hsv[0] = hue, hsv[1] = saturation, hsv[2] = value/brightness
		final float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), null);
		final float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), null);

		final double[] h = interpolator.interpolate(hsvFrom[0], hsvTo[0], str.length());
		final double[] s = interpolator.interpolate(hsvFrom[1], hsvTo[1], str.length());
		final double[] v = interpolator.interpolate(hsvFrom[2], hsvTo[2], str.length());

		final StringBuilder builder = new StringBuilder();
		final char[] letters = str.toCharArray();
		// create a string that matches the input-string but has
		// the different color applied to each char
		String lastDecoration = "";

		for (int i = 0; i < letters.length; i++) {
			char letter = letters[i];
			if ((letter == org.bukkit.ChatColor.COLOR_CHAR || letter == COLOR_AMPERSAND) && i + 1 < letters.length) {
				final char decoration = Character.toLowerCase(letters[i + 1]);

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
				i++;
				continue;
			}
			builder.append("<").append(convertColortoHex(Color.getHSBColor((float) h[i], (float) s[i], (float) v[i]))).append(">").append(lastDecoration).append(letter);
		}
		return builder.toString();
	}

	public String multiRgbGradient(GradientType type, String str, Color[] colors, @Nullable Double[] portions) {
		return multiRgbGradient(type, str, colors, portions, this);
	}

	public String multiRgbGradient(GradientType type, String str, Color[] colors, @Nullable Double[] portions, Interpolator interpolator) {
		if (colors.length < 2) {
			return (colors.length == 1 ? "<" + convertColortoHex(colors[0]) + ">" + str : str);
		}
		final Double[] p;
		if (portions == null) {
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
				builder.append(rgbGradient(
						str.substring(strIndex, strIndex + (int) (p[i] * str.length())),
						colors[i],
						colors[i + 1],
						interpolator));
			if (type == GradientType.HSV_GRADIENT_PATTERN)
				builder.append(hsvGradient(
						str.substring(strIndex, strIndex + (int) (p[i] * str.length())),
						colors[i],
						colors[i + 1],
						interpolator));
			strIndex += p[i] * str.length();
		}
		if (strIndex < str.length()) {
			if (type == GradientType.SIMPLE_GRADIENT_PATTERN)
				builder.append(rgbGradient(
						str.substring(strIndex),
						colors[colors.length - 1],
						colors[colors.length - 1],
						(from, to, max) -> quadratic(from, to, str.length(), true)));
			if (type == GradientType.HSV_GRADIENT_PATTERN)
				builder.append(hsvGradient(
						str.substring(strIndex),
						colors[colors.length - 1],
						colors[colors.length - 1],
						(from, to, max) -> quadratic(from, to, str.length(), true)));
		}
		return builder.toString();
	}

	public String rgbGradient(String str, Color from, Color to) {
		return rgbGradient(str, from, to, this);
	}

	public String rgbGradient(String str, Color from, Color to, Interpolator interpolator) {

		// interpolate each component separately
		final double[] red = interpolator.interpolate(from.getRed(), to.getRed(), str.length());
		final double[] green = interpolator.interpolate(from.getGreen(), to.getGreen(), str.length());
		final double[] blue = interpolator.interpolate(from.getBlue(), to.getBlue(), str.length());

		final StringBuilder builder = new StringBuilder();
		final char[] letters = str.toCharArray();
		// create a string that matches the input-string but has
		// the different color applied to each char
		String lastDecoration = "";

		for (int i = 0; i < letters.length; i++) {
			char letter = letters[i];
			if ((letter == org.bukkit.ChatColor.COLOR_CHAR || letter == COLOR_AMPERSAND) && i + 1 < letters.length) {
				final char decoration = Character.toLowerCase(letters[i + 1]);

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
				i++;
				continue;
			}

			final Color stepColor = new Color((int) Math.round(red[i]), (int) Math.round(green[i]), (int) Math.round(blue[i]));
			boolean isEmpty = letter == ' ' && lastDecoration.isEmpty();

			builder.append(isEmpty ? "" : "<").append(isEmpty ? "" : convertColortoHex(stepColor)).append(isEmpty ? "" : ">").append(lastDecoration).append(letter);
		}
		return builder.toString();
	}

	@Override
	public double[] interpolate(double from, double to, int max) {
		final double[] res = new double[max];
		for (int i = 0; i < max; i++) {
			res[i] = from + i * ((to - from) / (max - 1));
		}
		return res;
	}

	public double[] quadratic(double from, double to, int max, boolean mode) {
		final double[] results = new double[max];
		if (mode) {
			double a = (to - from) / (max * max);
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + from;
			}
		} else {
			double a = (from - to) / (max * max);
			double b = -2 * a * max;
			for (int i = 0; i < results.length; i++) {
				results[i] = a * i * i + b * i + from;
			}
		}
		return results;
	}

	/**
	 * Convert RGB to hex.
	 *
	 * @param R red color.
	 * @param G green color.
	 * @param B blue color.
	 * @return hex color or 0 if RGB values are over 255 or below 0.
	 */
	private static String convertRGBtoHex(int R, int G, int B) {
		if ((R >= 0 && R <= 255)
				&& (G >= 0 && G <= 255)
				&& (B >= 0 && B <= 255)) {

			Color color = new Color(R, G, B);
			StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffff));
			while (hex.length() < 6) {
				hex.insert(0, "0");
			}
			hex.insert(0, "#");
			return hex.toString();
		}
		// The hex color code doesn't exist
		else
			return "0";
	}

	private Double[] checkportions(Color[] colorList, Double[] portionsList) {
		if (colorList == null || portionsList == null) return null;
		if (colorList.length == portionsList.length) return null;
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

	/**
	 * This method will split on every gradient match, beside the first match
	 * (it is not needed split up on first match).
	 *
	 * @param message you want to split to an array.
	 * @param type    of gradient you want to check for.
	 * @return array siome is splited up on every gradient match, beside the first match.
	 */
	public String[] splitOnGradient(GradientType type, String message) {
		boolean firstMatch = false;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			char mess = message.charAt(i);
			if (mess == 'g' && i + 8 < message.length()) {
				StringBuilder build = new StringBuilder();
				boolean isgradient = false;
				for (int check = (i > 0 ? i - 1 : 0); check < message.length(); check++) {
					build.append(message.charAt(check));
					isgradient = build.indexOf(type.getType() + "<") >= 0;
					if (isgradient)
						break;
					if (check > 8 + i)
						break;
				}
				if (isgradient)
					if (!firstMatch)
						firstMatch = true;
					else
						builder.append("_,_");
			}
			builder.append(mess);
		}
		return builder.toString().split("_,_");
	}

	/**
	 * convert hex to RGB.
	 *
	 * @param colorStr hex you want to transform.
	 * @return RBGcolors.
	 */
	private static Color hexToRgb(String colorStr) {
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
			System.out.println("[RBG-Gradients] This hex color is not vaild " + colorStr);
			return new Color(Color.WHITE.getRGB());
		}
		return new Color(
				Integer.valueOf(colorStr.substring(1, 3), 16),
				Integer.valueOf(colorStr.substring(3, 5), 16),
				Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	/**
	 * Convert RGB to hex.
	 *
	 * @return hex color or 0 if RGB values are over 255 or below 0.
	 */
	private static String convertColortoHex(Color color) {
		StringBuilder hex = new StringBuilder(String.format("%06X", color.getRGB() & 0xffffff));
		hex.insert(0, "#");
		return hex.toString();
	}

	/**
	 * Check if it valid color symbol.
	 *
	 * @param message check color symbol.
	 * @return true if it is valid color symbol.
	 */
	private static boolean checkIfColor(char message) {

		for (String color : ChatColors.ALL_CODES)
			if (color.equals(String.valueOf(message)))
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

		// Compile the ReGex
		Pattern pattern = Pattern.compile(regex);

		// If the string is empty
		// return false
		if (str == null) {
			return false;
		}

		// Pattern class contains matcher() method
		// to find matching between given string
		// and regular expression.
		Matcher matcher = pattern.matcher(str);

		// Return if the string
		// matched the ReGex
		return matcher.matches();
	}

	private static int checkIfContainsColor(String message) {
		int index = message.indexOf(ChatColors.COLOR_CHAR);
		if (index < 0)
			index = message.indexOf(COLOR_AMPERSAND);
		if (index < 0) return -1;
		if (index + 1 > message.length()) return -1;

		char charColor = message.charAt(index + 1);
		for (char color : ChatColors.ALL_COLOR_CODES)
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

	public static String getStringStriped(String message, int startIndex, int endIndex) {
		String subcolor = message.substring(startIndex);
		final String substring = subcolor.substring(0, endIndex > 0 ? endIndex + 1 : message.length());
		return message.replace(substring, "");
	}

	public static String[] getMultiColors(String message, int startIndex) {
		String subcolor = message.substring(startIndex);
		int endOfColor = subcolor.indexOf(">");
		final String substring = subcolor.substring(0, endOfColor > 0 ? endOfColor + 1 : subcolor.length());
		return substring.substring(1, substring.length() - 1).split(":");
	}

	public int getLastGradientMatch(String message, GradientType type) {
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);

		if (gradientsMatcher.find())
			return gradientsMatcher.end();
		return -1;
	}

	public int getFirstGradientMatch(String message, GradientType type) {
		Matcher gradientsMatcher = GRADIENT_PATTERN.matcher(message);

		if (gradientsMatcher.find())
			return gradientsMatcher.start();
		return -1;
	}

	public enum GradientType {
		SIMPLE_GRADIENT_PATTERN("gradiens_"),
		HSV_GRADIENT_PATTERN("gradiens_hsv_");
		private final String type;

		GradientType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}
}
