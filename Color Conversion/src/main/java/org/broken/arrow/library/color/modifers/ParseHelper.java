package org.broken.arrow.library.color.modifers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.color.Component;
import org.broken.arrow.library.color.gradient.GradientChar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Internal utility class for parsing and transforming formatted text.
 *
 * <p>Provides helper methods used during parsing for:
 * <ul>
 * <li>Handling legacy format codes</li>
 * <li>Managing gradient output</li>
 *
 * <li>Building JSON chat components</li>
 *
 * <li>Extracting and buffering text segments</li>
 * </ul>
 *
 */
public final class ParseHelper {

    private ParseHelper() {
    }

    /**
     * Removes legacy formatting codes from the given content while updating the
     * provided modifier state.
     *
     * <p>Formatting codes (e.g. {@code &l}, {@code &m}) are not included in the
     * returned string but are applied to {@link ActiveModifiers}.
     *
     * @param content   the input string containing formatting codes
     * @param modifiers the modifier state to update
     * @return the cleaned text without formatting codes
     */
    public static String stripFormatsAndUpdateState(@Nonnull final String content, @Nonnull final ActiveModifiers modifiers) {
        StringBuilder cleanText = new StringBuilder();
        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);

            if ((c == '&' || c == '§') && i + 1 < content.length()) {
                char nextChar = Character.toLowerCase(content.charAt(i + 1));
                if (ChatColors.isFormatCode(nextChar)) {
                    String formatName = getChatColor(nextChar);
                    if (formatName != null) {
                        modifiers.update(formatName);
                    }
                    i += 2;
                    continue;
                }
            }
            cleanText.append(c);
            i++;
        }
        return cleanText.toString();
    }

    /**
     * Appends a gradient as JSON components, grouping characters by color.
     *
     * <p>Characters sharing the same color are buffered together to minimize
     * the number of generated components.
     *
     * @param parts     the target JSON array
     * @param chars     gradient characters with color information
     * @param modifiers active formatting modifiers
     * @return the next component builder after flushing
     */
    public static Component.Builder appendGradient(final JsonArray parts, final List<GradientChar> chars, final ActiveModifiers modifiers) {
        Component.Builder comp = new Component.Builder();
        modifiers.applyTo(comp);

        StringBuilder buffer = new StringBuilder();
        String currentColor = null;

        for (GradientChar gc : chars) {
            if (!gc.getHex().equals(currentColor)) {
                comp = flush(buffer, comp, parts, modifiers);
                modifiers.applyTo(comp);
                comp.colorCode(gc.getHex());
                currentColor = gc.getHex();
            }
            buffer.append(gc.getCharacter());
        }
        return flush(buffer, comp, parts, modifiers);
    }

    /**
     * Appends a gradient to a legacy string using Spigot hex formatting.
     *
     * <p>Color transitions are emitted only when the color changes to reduce
     * redundant formatting codes.
     *
     * @param finalString the output string builder
     * @param chars       gradient characters
     * @param modifiers   active formatting modifiers
     */
    public static void appendLegacyGradient(StringBuilder finalString, List<GradientChar> chars, ActiveModifiers modifiers) {
        String currentColor = null;
        for (GradientChar gc : chars) {
            if (!gc.getHex().equals(currentColor)) {
                finalString.append(toSpigotHex(gc.getHex()));
                finalString.append(modifiers.getLegacyFormatCodes());
                currentColor = gc.getHex();
            }
            finalString.append(gc.getCharacter());
        }
    }

    /**
     * Extracts a substring starting at the given index until the next formatting
     * directive or tag is encountered.
     *
     * <p>Stops when encountering:
     * <ul>
     *     <li>Gradient directives ({@code gradients_}, {@code hsv_})</li>
     *     <li>Hex color tags ({@code <#RRGGBB>})</li>
     *     <li>Invalid or non-format legacy sequences</li>
     * </ul>
     *
     * @param message the full input string
     * @param start   the starting index
     * @return the extracted substring
     */
    public static String extractUntilNextTag(final String message, final int start) {
        int i = start;
        while (i < message.length()) {
            char c = message.charAt(i);
            if (message.startsWith("gradients_", i) || message.startsWith("hsv_", i)) break;
            if (c == '<' && i + 1 < message.length() && message.charAt(i + 1) == '#') break;
            if ((c == '&' || c == '§') && i + 1 < message.length()) {
                char nextChar = Character.toLowerCase(message.charAt(i + 1));
                if (!ChatColors.isFormatCode(nextChar)) {
                    break;
                }
            }
            i++;
        }
        return message.substring(start, i);
    }

    /**
     * Builds the final JSON component output.
     *
     * <p>If only one component exists, it is returned directly. Otherwise,
     * a root object with an {@code extra} array is created.
     *
     * @param array     collected components
     * @param component current component (unused if array has multiple entries)
     * @return the resulting JSON object
     */
    public static JsonObject build(final JsonArray array, final Component.Builder component) {
        if (array.size() == 1) {
            return array.get(0).getAsJsonObject();
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("text", "");
        obj.add("extra", array);
        return obj;
    }

    /**
     * Flushes buffered text into the current component and appends it to the parts list.
     *
     * <p>After flushing, a new component builder is created with the current modifiers applied.
     *
     * @param buffer    the text buffer
     * @param component the current component builder
     * @param parts     the output JSON parts
     * @param modifiers active modifiers
     * @return a new component builder with modifiers applied
     */
    public static Component.Builder flush(final StringBuilder buffer, final Component.Builder component, final JsonArray parts, final ActiveModifiers modifiers) {
        if (buffer.length() > 0) {
            component.message(buffer.toString());
            parts.add(component.build().toJson());
            buffer.setLength(0);
        }

        Component.Builder nextComponent = new Component.Builder();
        modifiers.applyTo(nextComponent);
        return nextComponent;
    }

    /**
     * Resolves a legacy format character to its corresponding color or format name.
     *
     * @param c the format character
     * @return the format name, or {@code null} if invalid
     */
    public static String getChatColor(final char c) {
        try {
            return ChatColors.getByChar(c).getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a hex color string (e.g. {@code #RRGGBB}) into Spigot's legacy hex format.
     *
     * <p>Example:
     * <pre>
     * #ff0000 -> §x§f§f§0§0§0§0
     * </pre>
     *
     * @param hex the hex color string
     * @return the formatted legacy string, or empty if invalid
     */
    public static String toSpigotHex(@Nullable final String hex) {
        if (hex == null || hex.length() < 7) return "";
        char[] c = hex.toLowerCase().toCharArray();
        return "§x§" + c[1] + "§" + c[2] + "§" + c[3] + "§" + c[4] + "§" + c[5] + "§" + c[6];
    }

}
