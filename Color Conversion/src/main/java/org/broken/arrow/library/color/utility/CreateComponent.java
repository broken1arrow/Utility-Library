package org.broken.arrow.library.color.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.color.Component;
import org.broken.arrow.library.color.TextTranslator;

import javax.annotation.Nonnull;

import static org.broken.arrow.library.color.utility.StringUtility.checkIfColor;

/**
 * A utility for creating JSON-based text components compatible with Minecraft's
 * chat formatting system.
 * <p>
 * This class takes a raw text message, parses it for color codes (including
 * hex gradients), formatting codes, and translates them into a structured
 * JSON format that Minecraft can use for advanced chat messages.
 * </p>
 */
public class CreateComponent {

    private final TextTranslator textTranslator;
    private String text = "";

    /**
     * Constructs a new {@code CreateComponent} instance.
     *
     * @param textTranslator the text translator responsible for gradient and color handling
     * @param message        the raw message to format and translate color codes
     */
    public CreateComponent(final TextTranslator textTranslator,final String message) {
        this.textTranslator = textTranslator;
        this.text = message;
    }

    /**
     * Processes the stored chat message and converts it into a JSON object
     * representing a Minecraft-compatible chat component.
     * <p>
     * This method:
     * <ul>
     *     <li>Parses Minecraft-style formatting codes and hex colors</li>
     *     <li>Builds a component tree using {@link Component.Builder}</li>
     *     <li>Handles gradients via {@link TextTranslator#checkStringForGradient(String)}</li>
     * </ul>
     *
     * @param defaultColor the default text color name to fall back to if none is specified
     * @return the constructed chat component as a {@link JsonObject}
     */
    public JsonObject componentFormat(String defaultColor) {
        int i = 0;
        JsonArray jsonArray = new JsonArray();
        Component.Builder component = new Component.Builder();
        this.text = textTranslator.checkStringForGradient(this.text);

        defaultColor = (defaultColor == null || defaultColor.isEmpty()) ? "white" : defaultColor;

        StringBuilder builder = new StringBuilder(this.text.length());
        StringBuilder hex = new StringBuilder();

        while (i < this.text.length()) {
            char letter = this.text.charAt(i);
            boolean checkChar = false;

            if (isPotentialColorCode(this.text, i, letter)) {
                char msg = this.text.charAt(i + 1);
                checkChar = checkIfColor(msg);

                if (msg == '#') {
                    String hexString = getHexColorFromText(this.text, i);
                    checkChar = isValidHexCode(hexString);
                    if (checkChar) {
                        hex = new StringBuilder(hexString);
                    }
                }
            }

            if (checkChar) {
                i += processColorCode( builder, component, jsonArray, defaultColor, i, hex);
                hex.setLength(0);
                continue;
            }
            builder.append(letter);
            i++;
        }

        finalizeComponent(builder, component, jsonArray);

        return buildJsonObject(jsonArray, component);
    }

    /**
     * Checks if the current character at index {@code i} might be the start
     * of a color or formatting code.
     *
     * @param message the chat message being parsed
     * @param i the current index
     * @param letter the character at the current index
     * @return {@code true} if this could be a color/formatting code
     */
    private boolean isPotentialColorCode(String message, int i, char letter) {
        return i + 1 < message.length() && (letter == ChatColors.COLOR_CHAR || letter == ChatColors.COLOR_AMPERSAND || letter == '<');
    }

    /**
     * Extracts a hex color string starting at index {@code i} in the message.
     * <p>
     * Example: From {@code <#FF0000>} it extracts {@code #FF0000}.
     * </p>
     *
     * @param message the chat message
     * @param i the start index of the hex color code
     * @return the extracted hex string
     */
    @Nonnull
    private String getHexColorFromText(String message, int i) {
        String hexString = message.substring(i + 1, i + 8);
        int lastIndex = hexString.indexOf('>');
        if (lastIndex != -1) {
            hexString = hexString.substring(0, lastIndex);
        }
        return hexString;
    }

    /**
     * Checks if a given string is a valid hex color code.
     *
     * @param hex the string to check
     * @return {@code true} if valid, {@code false} otherwise
     */
    private boolean isValidHexCode(String hex) {
        return StringUtility.isValidHexCode(hex);
    }

    /**
     * Handles processing of a detected color or formatting code.
     * <p>
     * This method:
     * <ul>
     *     <li>Builds the previous text segment into a JSON component</li>
     *     <li>Updates the {@link Component.Builder} with the new color/formatting</li>
     * </ul>
     *
     * @param builder the current text buffer
     * @param component the current component builder
     * @param jsonArray the array of JSON components
     * @param defaultColor the default color name
     * @param i the current parsing index
     * @param hex the hex color buffer, if present
     * @return how many characters to advance in the parsing loop
     */
    private int processColorCode( final StringBuilder builder,final  Component.Builder component,final  JsonArray jsonArray,
                                  final  String defaultColor,final int i,final StringBuilder hex) {
        final int index = i + 1;
        if (index >= this.text.length()) return index;
        char letter = this.text.charAt(index);

        if (Character.isUpperCase(letter)) {
            letter = Character.toLowerCase(letter);
        }
        boolean isHex = hex.length() > 0;
        String format = isHex ? hex.toString() : getChatColorByChar(letter);
        if (format == null) return index;

        if (builder.length() > 0) {
            jsonArray.add(buildComponentToJson(builder, component));
        }
        textTranslator.setColor(defaultColor, component, format);
        return isHex  ? format.length() + 2 : 1;
    }

    /**
     * Attempts to map a single-character chat code to its color name.
     *
     * @param letter the chat code character
     * @return the corresponding color name, or {@code null} if unknown
     */
    private String getChatColorByChar(char letter) {
        try {
            return ChatColors.getByChar(letter).getName();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Finalizes the current text buffer into a JSON component object.
     *
     * @param builder the text buffer
     * @param component the component builder
     * @return the built JSON component
     */
    private JsonObject buildComponentToJson(StringBuilder builder, Component.Builder component) {
        component.message(builder.toString());
        builder.setLength(0);
        return component.build().toJson();
    }

    /**
     * Adds the remaining text in the buffer as the last component to the array.
     *
     * @param builder the remaining text buffer
     * @param component the component builder
     * @param jsonArray the array of JSON components
     */
    private void finalizeComponent(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        jsonArray.add(component.build().toJson());
    }

    /**
     * Builds the final JSON object to return, either as a single component
     * or as a composite with an {@code extra} array.
     *
     * @param jsonArray the array of components
     * @param component the last component builder
     * @return the final JSON object
     */
    private JsonObject buildJsonObject(JsonArray jsonArray, Component.Builder component) {
        if (jsonArray.size() > 1) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("extra", jsonArray);
            jsonObject.addProperty("text", "");
            return jsonObject;
        }
        return component.build().toJson();
    }
}
