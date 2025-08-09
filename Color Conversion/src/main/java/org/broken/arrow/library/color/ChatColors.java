package org.broken.arrow.library.color;

import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A utility class that provides a consistent and stable set of chat colors and formatting codes,
 * inspired by Bukkit's old {@link org.bukkit.ChatColor}, with some added improvements and extended functionality.
 * <p>
 * This class ensures that your chat color handling remains consistent, even if the original
 * Bukkit/Minecraft color definitions change in the future. It supports both predefined named
 * colors (e.g., {@link #RED}, {@link #GREEN}) and custom RGB hex colors, along with text formatting
 * such as bold, italic, and underline.
 * </p>
 *
 * <p><strong>Note:</strong> This class is immutable and thread-safe.</p>
 */
public final class ChatColors {

	/**
	 * The special character which prefixes all chat colour codes. Use this if
	 * you need to dynamically convert colour codes from your custom format.
	 */
	public static final char COLOR_CHAR = '\u00A7';
	/**
	 * The special character which prefixes all chat colour codes. Use this if
	 * you need to dynamically convert colour codes from your custom format.
	 */
	public static final char COLOR_AMPERSAND = '\u0026';
	private  static final char[] ALL_CODES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'R', 'r', 'X', 'x'};
	private static final char[] ALL_CHAR_COLOR_CODES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f', 'R', 'r', 'X', 'x'};
	private static final char[] SPECIAL_SIGN = {'l', 'n', 'o', 'k', 'm', 'r'};
	/**
	 * Pattern to remove all colour codes.
	 */
	public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");
	/**
	 * Colour instances keyed by their active character.
	 */
	private static final Map<Character, ChatColors> BY_CHAR = new HashMap<>();
	/**
	 * Colour instances keyed by their name.
	 */
	private static final Map<String, ChatColors> BY_NAME = new HashMap<>();
	/**
	 * Represents black.
	 */
	public static final ChatColors BLACK = new ChatColors('0', "black", new Color(0x000000));
	/**
	 * Represents dark blue.
	 */
	public static final ChatColors DARK_BLUE = new ChatColors('1', "dark_blue", new Color(0x0000AA));
	/**
	 * Represents dark green.
	 */
	public static final ChatColors DARK_GREEN = new ChatColors('2', "dark_green", new Color(0x00AA00));
	/**
	 * Represents dark blue (aqua).
	 */
	public static final ChatColors DARK_AQUA = new ChatColors('3', "dark_aqua", new Color(0x00AAAA));
	/**
	 * Represents dark red.
	 */
	public static final ChatColors DARK_RED = new ChatColors('4', "dark_red", new Color(0xAA0000));
	/**
	 * Represents dark purple.
	 */
	public static final ChatColors DARK_PURPLE = new ChatColors('5', "dark_purple", new Color(0xAA00AA));
	/**
	 * Represents gold.
	 */
	public static final ChatColors GOLD = new ChatColors('6', "gold", new Color(0xFFAA00));
	/**
	 * Represents gray.
	 */
	public static final ChatColors GRAY = new ChatColors('7', "gray", new Color(0xAAAAAA));
	/**
	 * Represents dark gray.
	 */
	public static final ChatColors DARK_GRAY = new ChatColors('8', "dark_gray", new Color(0x555555));
	/**
	 * Represents blue.
	 */
	public static final ChatColors BLUE = new ChatColors('9', "blue", new Color(0x5555FF));
	/**
	 * Represents green.
	 */
	public static final ChatColors GREEN = new ChatColors('a', "green", new Color(0x55FF55));
	/**
	 * Represents aqua.
	 */
	public static final ChatColors AQUA = new ChatColors('b', "aqua", new Color(0x55FFFF));
	/**
	 * Represents red.
	 */
	public static final ChatColors RED = new ChatColors('c', "red", new Color(0xFF5555));
	/**
	 * Represents light purple.
	 */
	public static final ChatColors LIGHT_PURPLE = new ChatColors('d', "light_purple", new Color(0xFF55FF));
	/**
	 * Represents yellow.
	 */
	public static final ChatColors YELLOW = new ChatColors('e', "yellow", new Color(0xFFFF55));
	/**
	 * Represents white.
	 */
	public static final ChatColors WHITE = new ChatColors('f', "white", new Color(0xFFFFFF));
	/**
	 * Represents magical characters that change around randomly.
	 */
	public static final ChatColors MAGIC = new ChatColors('k', "obfuscated");
	/**
	 * Makes the text bold.
	 */
	public static final ChatColors BOLD = new ChatColors('l', "bold");
	/**
	 * Makes a line appear through the text.
	 */
	public static final ChatColors STRIKETHROUGH = new ChatColors('m', "strikethrough");
	/**
	 * Makes the text appear underlined.
	 */
	public static final ChatColors UNDERLINE = new ChatColors('n', "underline");
	/**
	 * Makes the text italic.
	 */
	public static final ChatColors ITALIC = new ChatColors('o', "italic");
	/**
	 * Resets all previous chat colors or formats.
	 */
	public static final ChatColors RESET = new ChatColors('r', "reset");
	/**
	 * Count used for populating legacy ordinal.
	 */
	private static int count = 0;
	/**
	 * This colour's colour char prefixed by the {@link #COLOR_CHAR}.
	 */
	private final String toString;
	private final String name;
	/**
	 * The RGB color of the ChatColors. null for non-colors (formatting)
	 */
	private final Color color;
	private final char code;

	/**
	 * Creates a new chat color/formatting with the given code and name.
	 *
	 * @param code  the single-character code
	 * @param name  the human-readable name
	 */
	public ChatColors(char code, String name) {
		this(code, name, null);
	}

	/**
	 * Creates a new chat color/formatting with the given code, name, and RGB value.
	 *
	 * @param code   the single-character code
	 * @param name   the human-readable name
	 * @param color  the RGB color, or {@code null} if this represents formatting only
	 */
	public ChatColors(char code, String name, Color color) {
		this.name = name;
		this.toString = new String(new char[]{
				COLOR_CHAR, code
		});
		this.color = color;
		this.code = code;

		BY_CHAR.put(code, this);
		BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
	}

	private ChatColors(String name, String toString, int rgb) {
		this.name = name;
		this.toString = toString;
		this.color = new Color(rgb);
		this.code = ' ';
	}

	/**
	 * Get the colour represented by the specified code.
	 *
	 * @param code the code to search for
	 * @return the mapped colour, or null if non exists
	 */
	public static ChatColors getByChar(char code) {
		return BY_CHAR.get(code);
	}

	/**
	 * Returns the special formatting codes (bold, italic, underline, etc.).
	 *
	 * @return an array of special formatting characters
	 */
	public char[] getSpecialSign() {
		return SPECIAL_SIGN;
	}

	/**
	 * Creates a new {@link ChatColors} from a {@link Color} instance.
	 *
	 * @param color the RGB color
	 * @return the corresponding {@link ChatColors}
	 */
	public static ChatColors of(Color color) {
		return of("#" + String.format("%08x", color.getRGB()).substring(2));
	}

	/**
	 * Parses a string into a {@link ChatColors} instance.
	 * <ul>
	 *     <li>Hex colors in the format {@code #RRGGBB} are supported.</li>
	 *     <li>Named colors (e.g., "RED") are matched case-insensitively.</li>
	 * </ul>
	 *
	 * @param string the string to parse
	 * @return the corresponding {@link ChatColors}
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static ChatColors of(String string) {
		if (string == null)
			throw new Validate.ValidateExceptions("String can't be null");

		if (string.startsWith("#") && string.length() == 7) {
			int rgb;
			try {
				rgb = Integer.parseInt(string.substring(1), 16);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Illegal hex string " + string);
			}

			StringBuilder magic = new StringBuilder(COLOR_CHAR + "x");
			for (char c : string.substring(1).toCharArray()) {
				magic.append(COLOR_CHAR).append(c);
			}

			return new ChatColors(string, magic.toString(), rgb);
		}

		ChatColors defined = BY_NAME.get(string.toUpperCase(Locale.ROOT));
		if (defined != null) {
			return defined;
		}

		throw new IllegalArgumentException("Could not parse ChatColors " + string);
	}

	/**
	 * Translates alternate color code prefixes into the standard {@link #COLOR_CHAR}.
	 *
	 * @param altColorChar   the alternate prefix character (e.g., '&amp;')
	 * @param textToTranslate the text containing color codes
	 * @return the translated text
	 */
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if (b[i] == altColorChar && getColorCode(b[i]) > -1) {
				b[i] = ChatColor.COLOR_CHAR;
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			}
		}
		return new String(b);
	}

	/**
	 * Strips the given message of all color codes
	 *
	 * @param input String to strip of color
	 * @return A copy of the input string, without any coloring
	 */
	@Nullable
	public static String stripColor(@Nullable final String input) {
		if (input == null) {
			return null;
		}
		return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
	}

	/**
	 * Checks if a character represents a valid color code.
	 *
	 * @param letter the character to check
	 * @return {@code 1} if valid, {@code -1} otherwise
	 */
	public static int getColorCode(char letter) {
		for (char color : ChatColors.ALL_CHAR_COLOR_CODES)
			if (color == letter)
				return 1;
		return -1;
	}

	/**
	 * Returns the number of colors and formatting codes registered.
	 *
	 * @return the count
	 */
	public static int getCount() {
		return count;
	}

	/**
	 * Returns the formatted string representation of this color/formatting.
	 *
	 * @return the color/formatting code string
	 */
	public String getToString() {
		return toString;
	}

	/**
	 * Returns the name of this color/formatting.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the RGB color value for this {@link ChatColors}, or {@code null} for formatting.
	 *
	 * @return the color or {@code null}
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the single-character code for this color/formatting.
	 *
	 * @return the code
	 */
	public char getCode() {
		return code;
	}

	/**
	 * Returns all valid color and formatting codes.
	 *
	 * @return the codes
	 */
	public static char[] getAllColorCodes(){
		return ALL_CODES;
	}

	/**
	 * Returns all valid color codes (excluding formatting).
	 *
	 * @return the color codes
	 */
	public static char[] getAllCharColorCodes(){
		return ALL_CHAR_COLOR_CODES;
	}

	@Override
	public String toString() {
		return toString;
	}

}
