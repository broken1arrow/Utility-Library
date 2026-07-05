package org.broken.arrow.library.color.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.library.color.Component.Builder;
import org.broken.arrow.library.color.gradient.GradientChar;
import org.broken.arrow.library.color.gradient.GradientMatch;
import org.broken.arrow.library.color.gradient.GradientPattern;
import org.broken.arrow.library.color.gradient.patterns.MultiGradientPattern;
import org.broken.arrow.library.color.gradient.patterns.SimpleGradientPattern;
import org.broken.arrow.library.color.modifers.ActiveModifiers;
import org.broken.arrow.library.color.modifers.ParseHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Parses a formatted string containing gradient directives, hex colors, and legacy
 * formatting codes into Spigot-compatible legacy text or JSON component structures.
 *
 * <p>This parser supports a flexible mini-formatting language that allows combining:
 * <ul>
 *   <li>RGB gradients ({@code gradients_<#color1:#color2>})</li>
 *   <li>HSV gradients ({@code gradients_hsv_<#color1:#color2>} or {@code hsv_<#color1:#color2>})</li>
 *   <li>RGB hex simple ({@code <#0000ff:#ff00ff>})</li>
 *   <li>Hex colors ({@code <#RRGGBB>})</li>
 *   <li>Legacy color and format codes ({@code &a}, {@code &l}, {@code &m}, etc.)</li>
 * </ul>
 *
 * <h3>Gradient Syntax</h3>
 * Gradients are defined using the following pattern:
 * <pre>{@code
 * gradients_<#color1:#color2[:#colorN...]>[optional_text_modifier]Text
 * }</pre>
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code <#ff0000:#00ff00>Hello}</li>
 *   <li>{@code gradients_<#ff0000:#00ff00>Hello}</li>
 *   <li>{@code gradients_<#0000ff:#ff00ff:#ff00ff>Hello}</li>
 *   <li>{@code hsv_<#BFD16B:#E786C5:#D3FB5F>Hello}</li>
 * </ul>
 *
 * <h3>Portion Control (Optional)</h3>
 * A gradient may include an optional {@code _portion<...>} suffix to control how much
 * each color contributes to the gradient:
 * <pre>{@code
 * gradients_<#color1:#color2:#color3>_portion<0.2:0.6:0.2>Text
 * hsv_<#color1:#color2:#color3>_portion<0.2:0.6:0.2>Text
 * }</pre>
 * <p>The number of portion values should match the number of colors. Values represent
 * relative distribution and do not need to sum exactly to 1.0.</p>
 *
 * <h3>Modifiers and Styling Inheritance</h3>
 * Legacy format modifiers alter the typography of the text:
 * <ul>
 *   <li>{@code &l} or {@code §l} - Bold</li>
 *   <li>{@code &m} or {@code §m} - Strikethrough</li>
 *   <li>{@code &o} or {@code §o} - Italic</li>
 *   <li>{@code &n} or {@code §n} - Underline</li>
 *   <li>{@code &r} or {@code §r} - Reset</li>
 * </ul>
 * <p>
 * <strong>Gradient Modifier Rules:</strong> If a style modifier is placed directly after a gradient tag
 * or anywhere in the middle of the gradient text, it is applied retroactively to the <b>entire</b> gradient
 * block.
 * </p>
 * <p>
 * <strong>Standard Text Rules:</strong> Outside of gradients, standard text, standard hex codes
 * ({@code <#RRGGBB>}), and legacy color codes follow standard Vanilla Minecraft behavior, where
 * modifiers only affect characters after the point they are declared.
 * </p>
 */
public class ChatFormatParser {
    private final List<GradientPattern> patterns = Arrays.asList(
            new SimpleGradientPattern(),
            new MultiGradientPattern()
    );

    /**
     * Parses a formatted string containing gradients, hex colors, and legacy formatting
     * into a JSON chat component structure.
     * <p>
     * The output is a structured JSON object compatible with Minecraft's text component system,
     * where gradients are split into multiple components with per-character coloring.
     *
     * @param message      the formatted input string
     * @param defaultColor the default fallback color (e.g., "white") to apply when none is specified
     * @return a {@link JsonObject} representing the parsed chat component structure
     * @see ChatFormatParser
     */
    public JsonObject parse(final String message, String defaultColor) {
        if (defaultColor == null || defaultColor.isEmpty()) defaultColor = "white";

        final TextGradientUtil gradientUtil = new TextGradientUtil();
        final JsonArray parts = new JsonArray();
        final ActiveModifiers modifiers = new ActiveModifiers(defaultColor);
        Builder component = new Builder();
        //modifiers.applyTo(component);

        final StringBuilder textBuffer = new StringBuilder();
        int i = 0;

        while (i < message.length()) {
            char c = message.charAt(i);
            boolean patternMatched = false;

            for (GradientPattern pattern : patterns) {
                final GradientMatch directive = pattern.tryParse(message, i);
                if (directive == null) continue;
                ParseHelper.flush(textBuffer, component, parts, modifiers);
                final int start = i + directive.getTagLength();

                final String rawContent = ParseHelper.extractUntilNextTag(message, start);
                final String cleanContent = ParseHelper.stripFormatsAndUpdateState(rawContent, modifiers);

                final List<GradientChar> gradient = gradientUtil.multiRgbGradientRaw(
                        directive.getType(),
                        cleanContent,
                        directive.getColors(),
                        directive.getPortions()
                );

                component = ParseHelper.appendGradient(parts, gradient, modifiers);
                modifiers.reset();
                i = start + rawContent.length();
                patternMatched = true;
                break;
            }
            if (patternMatched) continue;

            if (c == '<' && i + 1 < message.length() && message.charAt(i + 1) == '#') {
                int end = message.indexOf('>', i);
                if (end != -1) {
                    String hex = message.substring(i + 1, end);
                    if (StringUtility.isValidHexCode(hex)) {
                        component = ParseHelper.flush(textBuffer, component, parts, modifiers);
                        modifiers.update(hex);
                        modifiers.applyTo(component);
                        modifiers.update("");
                        i = end + 1;
                        continue;
                    }
                }
            }

            if ((c == '&' || c == '§') && i + 1 < message.length()) {
                char code = Character.toLowerCase(message.charAt(i + 1));
                String color = ParseHelper.getChatColor(code);

                if (color != null) {
                    component = ParseHelper.flush(textBuffer, component, parts, modifiers);
                    modifiers.update(color);
                    modifiers.applyTo(component);
                    modifiers.update("");
                    i += 2;
                    continue;
                }
            }

            textBuffer.append(c);
            i++;
        }
        ParseHelper.flush(textBuffer, component, parts, modifiers);
        return ParseHelper.build(parts, component);
    }

    /**
     * Parses a formatted string into a Spigot-compatible legacy text string containing section sign ({@code §}) codes.
     * <p>
     * Custom hex colors inside this parser are converted into Spigot's native legacy hex format ({@code §x§R§R§G§G§B§B}).
     *
     * <h3>Parsing Behavior</h3>
     * <ul>
     *   <li>Gradient sections consume text until another formatting directive is encountered.</li>
     *   <li>Formatting codes inside gradient sections are stripped from the visible text but still affect styling.</li>
     *   <li>After a gradient is applied, modifiers are reset to avoid unintended carryover.</li>
     * </ul>
     *
     * @param message      the input string containing formatting and gradient directives
     * @param defaultColor the fallback color name (e.g. "white") if no color is specified; defaults to "white" if null/empty
     * @return a legacy-formatted string using {@code §} codes compatible with Spigot
     */
    public String parseToLegacy(final String message, String defaultColor) {
        if (defaultColor == null || defaultColor.isEmpty()) defaultColor = "white";

        final TextGradientUtil gradientUtil = new TextGradientUtil();
        final StringBuilder finalString = new StringBuilder();
        final ActiveModifiers modifiers = new ActiveModifiers(defaultColor);

        int i = 0;

        while (i < message.length()) {
            char c = message.charAt(i);
            boolean patternMatched = false;

            for (GradientPattern pattern : patterns) {
                final GradientMatch directive = pattern.tryParse(message, i);
                if (directive == null) continue;

                final int start = i + directive.getTagLength();
                String rawContent = ParseHelper.extractUntilNextTag(message, start);
                String cleanContent = ParseHelper.stripFormatsAndUpdateState(rawContent, modifiers);

                final List<GradientChar> gradient = gradientUtil.multiRgbGradientRaw(
                        directive.getType(),
                        cleanContent,
                        directive.getColors(),
                        directive.getPortions()
                );

                ParseHelper.appendLegacyGradient(finalString, gradient, modifiers);
                modifiers.reset();
                i = start + rawContent.length();
                patternMatched = true;
                break;
            }
            if (patternMatched) continue;

            if (c == '<' && i + 1 < message.length() && message.charAt(i + 1) == '#') {
                int end = message.indexOf('>', i);
                if (end != -1) {
                    String hex = message.substring(i + 1, end);
                    if (StringUtility.isValidHexCode(hex)) {
                        modifiers.update(hex);
                        finalString.append(ParseHelper.toSpigotHex(hex));
                        finalString.append(modifiers.getLegacyFormatCodes());
                        modifiers.update("");
                        i = end + 1;
                        continue;
                    }
                }
            }

            if ((c == '&' || c == '§') && i + 1 < message.length()) {
                char code = Character.toLowerCase(message.charAt(i + 1));
                String formatName = ParseHelper.getChatColor(code);
                if (formatName != null) {
                    modifiers.update(formatName);
                    finalString.append("§").append(code);
                    modifiers.update("");
                    i += 2;
                    continue;
                }
            }
            finalString.append(c);
            i++;
        }
        return finalString.toString();
    }

}