package org.broken.arrow.library.color.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.color.Component;
import org.broken.arrow.library.color.TextTranslator;

/**
 * Utility class for converting legacy-formatted Minecraft chat text into a
 * JSON-based text component compatible with Minecraft's modern chat system.
 * <p>
 * This is intended for legacy support, translating traditional color codes
 * into structured JSON for use with advanced chat features.
 * </p>
 * <p>
 * Legacy Spigot formatting uses symbols like '&amp;' or '§' and supports two specific formats:
 * </p>
 * <ul>
 *   <li>Hexadecimal format: e.g., '&amp;x&amp;d&amp;4&amp;c&amp;3&amp;1&amp;1'</li>
 *   <li>Color codes: e.g., '&amp;f' for white.</li>
 * </ul>
 * <p>
 * This class takes a raw text message, parses it for color codes (including
 * hex gradients), formatting codes, and translates them into a structured
 * JSON format that Minecraft can use for advanced chat messages.
 * </p>
 */
public class CreateFromLegacyText {

    private CreateFromLegacyText() {
    }

    /**
     * Processes a legacy formatted chat message and converts it into a JSON object
     * representing a Minecraft-compatible chat component.
     * <p>
     * This method:
     * <ul>
     *     <li>Parses Minecraft-style formatting codes such as '&amp;' or '§'</li>
     *     <li>Handles hexadecimal color codes including the legacy Spigot hex format</li>
     *     <li>Builds a component tree using {@link Component.Builder}</li>
     * </ul>
     *
     * @param message      the raw chat message containing legacy formatting codes
     * @param defaultColor the default text color to use if none is specified in the message
     * @return the constructed chat component as a {@link JsonObject}
     */
    public static JsonObject fromLegacyText(String message, ChatColors defaultColor) {
        if (message == null) {
            return createEmptyJsonObject();
        }
        int i = 0;
        StringBuilder builder = new StringBuilder();
        Component.Builder component = new Component.Builder();
        JsonArray jsonArray = new JsonArray();

        while (i < message.length()) {
            char c = message.charAt(i);
            if (i + 1 < message.length() && (c == ChatColors.COLOR_CHAR || c == ChatColors.COLOR_AMPERSAND)) {
                i++;

                c = message.charAt(i);
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                ChatColors format = parseColorCode(message, i, c);
                if (format != null) {
                    i += handleFormat(builder, component, jsonArray, defaultColor, format, c);
                    continue;
                }
            }
            builder.append(c);
            i++;
        }
        finalizeComponent(builder, component, jsonArray);
        return buildJsonObject(jsonArray, component);
    }

    /**
     * Creates an empty JSON object representing an empty Minecraft chat component.
     *
     * @return a JsonObject with an empty "text" property
     */
    private static JsonObject createEmptyJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", "");
        return jsonObject;
    }

    /**
     * Extracts a ChatColors format from the message at the given position.
     * <p>
     * Supports hexadecimal colors using the legacy format:
     * {@code &x&r&g&b&r&g&b} and standard single-character color codes.
     * </p>
     *
     * @param message the full legacy text message
     * @param i       the current index of the color code character in the message
     * @param c       the color code character to parse
     * @return the corresponding ChatColors instance, or null if invalid or unsupported
     */
    private static ChatColors parseColorCode(String message, int i, char c) {
        if (c == 'x' && i + 12 < message.length()) {
            StringBuilder hex = new StringBuilder("#");
            for (int j = 0; j < 6; j++) {
                hex.append(message.charAt(i + 2 + (j * 2)));
            }
            return parseHexColor(hex.toString());
        }
        return ChatColors.getByChar(c);
    }

    /**
     * Parses a hexadecimal color string into a ChatColors instance.
     *
     * @param hex a string representing a hex color (e.g., "#ff0011")
     * @return the corresponding ChatColors instance if valid, otherwise null
     */
    private static ChatColors parseHexColor(String hex) {
        try {
            return ChatColors.of(hex);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Handles the formatting code by applying the corresponding color/style
     * to the current component, adding completed text segments to the JSON array.
     *
     * @param builder      the StringBuilder containing the current text segment
     * @param component    the Component.Builder being constructed
     * @param jsonArray    the JsonArray holding completed components
     * @param defaultColor the default ChatColors to apply if none specified
     * @param format       the ChatColors format to apply
     * @param c            the character representing the color code (used to determine length)
     * @return the number of characters to advance in the message after processing this code
     */
    private static int handleFormat(StringBuilder builder, Component.Builder component, JsonArray jsonArray,
                                    ChatColors defaultColor, ChatColors format, char c) {
        if (builder.length() > 0) {
            addComponentToJsonArray(builder, component, jsonArray);
        }
        TextTranslator.getInstance().setColor(
                defaultColor != null ? defaultColor.getName() : "",
                component,
                format.getName()
        );
        return c == 'x' ? 13 : 1;
    }

    /**
     * Adds the current text in the builder as a new component to the JSON array,
     * resetting the builder afterward.
     *
     * @param builder   the StringBuilder holding the text to add
     * @param component the Component.Builder to finalize
     * @param jsonArray the JsonArray to add the finalized component's JSON to
     */
    private static void addComponentToJsonArray(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        builder.setLength(0);
        jsonArray.add(component.build().toJson());
    }

    /**
     * Finalizes the last component with remaining text and adds it to the JSON array.
     *
     * @param builder   the StringBuilder containing remaining text
     * @param component the Component.Builder being finalized
     * @param jsonArray the JsonArray to which the finalized component JSON is added
     */
    private static void finalizeComponent(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        jsonArray.add(component.build().toJson());
    }

    /**
     * Builds the final JSON object for the entire message.
     * <p>
     * If the message consists of multiple components, they are combined in an
     * "extra" array with an empty root text. Otherwise, returns the single component's JSON.
     * </p>
     *
     * @param jsonArray the JsonArray of all components
     * @param component the last Component.Builder used
     * @return the final JsonObject representing the full Minecraft chat component
     */
    private static JsonObject buildJsonObject(JsonArray jsonArray, Component.Builder component) {
        if (jsonArray.size() > 1) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("extra", jsonArray);
            jsonObject.addProperty("text", "");
            return jsonObject;
        }
        return component.build().toJson();
    }

}
