package org.broken.arrow.library.color;

import com.google.gson.JsonObject;
import org.broken.arrow.library.color.Component.Builder;
import org.broken.arrow.library.color.utility.ChatFormatParser;

import org.broken.arrow.library.color.utility.FormatParserLegacy;
import java.util.regex.Pattern;

/**
 * Utility class for translating and formatting text with color codes and gradients.
 * <p>
 * Supports:
 * <ul>
 * <li> For vanilla color codes <strong>&amp; or &#167;</strong> and the color code.</li>
 * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
 * <li> For normal gradients <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
 * <li> For hsv use <strong>gradients_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
 * <li> For use multicolor <strong>gradients_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
 * <li> For change balance between colors add this to the end of gradients or gradients_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
 *  Like these <strong>gradients_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
 *  If you not add this it will have even balanced between colors.</li>
 * </ul>
 * <p>
 * Provides methods to translate these formats into Spigot-compatible color codes or
 * into Minecraft component JSON objects suitable for vanilla Minecraft message serialization.
 * </p>
 */
public final class TextTranslator {
    private static final TextTranslator instance = new TextTranslator();

    /**
     * Get the instance of this class.
     *
     * @return Returns the singleton instance of the {@link TextTranslator}.
     */
    public static TextTranslator getInstance() {
        return instance;
    }

    /**
     * Type your message/string text here. you use this format for colors:
     * <ul>
     * <li> For vanilla color codes <strong>&amp; or &#167;</strong> and the color code.</li>
     * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
     * <li> For normal gradients <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
     * <li> For hsv use <strong>gradients_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
     * <li> For use multicolor <strong>gradients_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
     * <li> For change balance between colors add this to the end of gradients or <strong>gradients_hsv_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong>
     *  Like these <strong>gradients_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
     *  If you not add this it will have even balanced between colors.</li>
     * </ul>
     *
     * @param message your string message.
     * @return spigot compatible translation.
     */
    public static String toSpigotFormat(String message) {
        ChatFormatParser chatFormatParser = new ChatFormatParser();
        return chatFormatParser.parseToLegacy(message, "white");
    }

    /**
     * This is for component when you want to send message
     * through vanilla Minecraft MNS for example. DOESN'T SUPPORT SPIGOT API, AS IT LACK TOOLS NEEDED TO  HANDLE JSON. Use {@link #toSpigotFormat(String)}
     * <br> You use this format for colors:<br>
     * <ul>
     * <li> For vanilla color codes <strong>&amp; or &#167;</strong> and the color code.</li>
     * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
     * <li> For normal gradients <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
     * <li> For hsv use <strong>gradients_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
     * <li> For use multicolor <strong>gradients_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
     * <li> For change balance between colors add this to the end of gradients or gradients_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
     *  Like these <strong>gradients_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
     *  If you not add this it will have even balanced between colors.</li>
     * </ul>
     *
     * @param message      your string message.
     * @param defaultColor set default color when colors are not set in the message.
     * @return JSON object with the set colors.
     */
    public static JsonObject toComponent(String message, String defaultColor) {
        return getInstance().componentFormat(message, defaultColor);
    }

    /**
     * This is for component when you want to send message
     * thought vanilla Minecraft MNS for example. DOESN'T SUPPORT SPIGOT API, AS IT LACK TOOLS NEEDED TO  HANDLE JSON. Use {@link #toSpigotFormat(String)}
     * <br> You use this format for colors:<br>
     * <ul>
     * <li> For vanilla color codes <strong>&amp; or &#167;</strong> and the color code.</li>
     * <li> For hex <strong>&lt;#5e4fa2&gt;</strong> </li>
     * <li> For normal gradients <strong>&lt;#5e4fa2:#f79459&gt;</strong> </li>
     * <li> For hsv use <strong>gradients_hsv_&lt;#5e4fa2:...&gt;</strong> add at least 2 colors or more</li>
     * <li> For use multicolor <strong>gradients_&lt;#6B023E:...&gt;</strong>add at least 2 colors or more </li>
     * <li> For change balance between colors add this to the end of gradients or gradients_hsv <strong>_portion&lt;0.2:0.6:0.2&gt;</strong>
     *  Like these <strong>gradients_&lt;#6B023E:#3360B3:#fc9:#e76424&gt;_portion&lt;0.2:0.6:0.2&gt;</strong> ,
     *  If you not add this it will have even balance between colors.</li>
     * </ul>
     *
     * @param message your string message.
     * @return JSON object with the set colors.
     */

    public static JsonObject toComponent(String message) {
        return getInstance().componentFormat(message, null);
    }

    /**
     * Converts a legacy Spigot formatted string to a JSON object, suitable for use with Minecraft's chat serializer.
     * Most usefully for Minecraft version 1.16 and newer, when you want to use gradients or hexadecimal colors,
     * and not want to use my methods to convert colors.
     * <p>&nbsp;</p>
     * <p>
     * Legacy Spigot formatting uses symbols like '&amp;' or '§' and supports two specific formats:
     * </p>
     * <ul>
     *   <li>Hexadecimal format: e.g., '&amp;x&amp;d&amp;4&amp;c&amp;3&amp;1&amp;1'</li>
     *   <li>Color codes: e.g., '&amp;f' for white.</li>
     * </ul>
     *
     * @param message      The input string to check and convert to JSON.
     * @param defaultColor The default color to use if a color is not specified in the message.
     * @return A JSON object representing the formatted text.
     */
    public static JsonObject fromLegacyText(String message, ChatColors defaultColor) {
        return FormatParserLegacy.fromLegacyText(message, defaultColor);
    }


    /**
     * The type of gradients set.
     */
    public enum GradientType {
        /**
         * Simple linear gradients, identified by the prefix {@code "gradients_"}.
         */
        SIMPLE_GRADIENT_PATTERN("gradients_"),
        /**
         * HSV-based gradients, identified by the prefix {@code "hsv_"}.
         */
        HSV_GRADIENT_PATTERN("hsv_"),
        /**
         * HSL-based gradients, identified by the prefix {@code "hsl_"}.
         */
        HSL_GRADIENT_PATTERN("hsl_");
        private final String type;

        GradientType(String type) {
            this.type = type;
        }

        /**
         * Get the gradient start of the string.
         *
         * @return the type of gradients.
         */
        public String getType() {
            return type;
        }
    }

    /**
     * This is for component when you want to send message through vanilla Minecraft MNS for example.
     * DOESN'T SUPPORT SPIGOT API, AS IT LACK TOOLS NEEDED TO  HANDLE JSON. Use {@link #toSpigotFormat(String)}
     *
     * @param message      your string message.
     * @param defaultColor set default color when colors are not set in the message.
     * @return json object with the set colors.
     */
    private JsonObject componentFormat(String message, String defaultColor) {
        ChatFormatParser createComponent = new ChatFormatParser();
        return createComponent.parse(message, defaultColor);
    }


}
