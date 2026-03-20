package org.broken.arrow.library.serialize.utility.converters;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.serialize.utility.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Utility class for replacing string placeholders with corresponding values.
 * Placeholders are identified using the format "{index}" where 'index' represents
 * the position of the value in the placeholders array.
 */
public class PlaceholderTranslator {
    private static final Logging log = new Logging(PlaceholderTranslator.class);
    private static final String FALSE = "false";
    private static final String TRUE = "true";

    private PlaceholderTranslator() {
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param text            The raw text to translate.
     * @param wrapperConsumer The consumer where you provide the placeholder data.
     * @return The translated text.
     */
    public static String translateText(final String text, final Consumer<PlaceholderWrapper> wrapperConsumer) {
        final PlaceholderWrapper placeholderWrapper = new PlaceholderWrapper();
        wrapperConsumer.accept(placeholderWrapper);
        final Map<String, Object> placeholderMap = placeholderWrapper.getPlaceholders();

        return applyPlaceholderMap(text, placeholderMap);
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param text         The raw text to translate.
     * @param placeholders The values to replace the placeholders with.
     * @return The translated text.
     */
    public static String translateText(String text, final Object... placeholders) {
        return translateText(text, null, placeholders);
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param text         The raw text to translate.
     * @param replacement  The pair of replacements for the boolean values.
     *                     The first element will replace 'true' and the second will replace 'false'
     * @param placeholders The values to replace the placeholders with.
     * @return The translated text.
     */
    public static String translateText(String text, final Pair<String, String> replacement, final Object... placeholders) {
        if (text == null) return "";
        if (replacement != null) {
            text = replaceBooleans(text, replacement);
        }
        for (int i = 0; i < placeholders.length; i++) {
            Object value = placeholders[i];
            if (value instanceof Collection) continue;

            text = text.replace("{" + i + "}", value != null ? value.toString() : "");
        }
        return text;
    }

    /**
     * Translates placeholders in a list of strings by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param listOfText   The list of strings to translate.
     * @param placeholders The consumer where you provide the placeholder data.
     * @return The translated list of lore strings.
     */
    public static List<String> translateList(final List<String> listOfText, final Consumer<PlaceholderWrapper> placeholders) {
        if (listOfText == null) return new ArrayList<>();
        final List<String> result = new ArrayList<>();
        final PlaceholderWrapper wrapper = new PlaceholderWrapper();
        placeholders.accept(wrapper);
        for (String text : listOfText) {
            final Map<String, Object> placeholderMap = wrapper.getPlaceholders();
            if (placeholderMap.isEmpty()) {
                result.add(text);
                continue;
            }
            List<String> lines = new ArrayList<>();
            lines.add(text);
            for (Map.Entry<String, Object> entry : placeholderMap.entrySet()) {
                lines = applyPlaceholders(lines, entry);
            }
            result.addAll(lines);
        }
        return result;
    }

    /**
     * Translates placeholders in a list of strings by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param listOfText   The list of strings to translate.
     * @param placeholders The values to replace the placeholders with.
     * @return The translated list of lore strings.
     */
    public static List<String> translateList(final List<String> listOfText, final Object... placeholders) {
        return translateList(null, listOfText, placeholders);
    }


    /**
     * Translates placeholders in a list of strings by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param listOfText   The list of strings to translate.
     * @param placeholders The values to replace the placeholders with.
     * @param replacements The pair of replacements for the boolean values.
     *                     The first element will replace 'true' and the second will replace 'false'.
     * @return The translated list of lore strings.
     */
    public static List<String> translateList(final Pair<String, String> replacements, final List<String> listOfText, final Object... placeholders) {
        if (listOfText == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String text : listOfText) {
            List<String> expandedLines = applyPlaceholders(text, placeholders);
            for (String line : expandedLines) {
                if (replacements != null) {
                    line = replaceBooleans(line, replacements);
                }
                result.add(line);
            }
        }
        return result;
    }

    /**
     * Replaces boolean values in the text with the provided replacements.
     *
     * @param text         The text to perform the replacement on.
     * @param replacements The pair of replacements for the boolean values.
     *                     The first element will replace 'true' and the second will replace 'false'.
     * @return The text with boolean replacements.
     */
    private static String replaceBooleans(String text, Pair<String, String> replacements) {
        if (text == null) return "";
        return text.replace(TRUE, replacements.getFirst())
                .replace(FALSE, replacements.getSecond());
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param rawText        The raw text to translate.
     * @param placeholderMap The map with key-value, where key is used to fins the placeholder in the provided text.
     * @return The translated text.
     */
    public static String applyPlaceholderMap(String rawText, final Map<String, Object> placeholderMap) {
        if (placeholderMap == null || placeholderMap.isEmpty())
            return rawText;

        for (Map.Entry<String, Object> entry : placeholderMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (rawText == null || value instanceof Collection)
                continue;
            rawText = rawText.replace(key, value != null ? value.toString() : "");
        }
        return rawText;
    }


    private static List<String> applyPlaceholders(String text, Object... placeholders) {
        List<String> result = new ArrayList<>();
        result.add(text);
        for (int i = 0; i < placeholders.length; i++) {
            Object value = placeholders[i];
            String key = "{" + i + "}";
            List<String> newResult = new ArrayList<>();
            for (String current : result) {
                if (value instanceof Collection && current.contains(key)) {
                    for (Object element : (Collection<?>) value) {
                        newResult.add(current.replace(key, element.toString()));
                    }
                } else {
                    newResult.add(current.replace(key, value != null ? value.toString() : ""));
                }
            }
            result = newResult;
        }
        return result;
    }

    private static List<String> applyPlaceholders(List<String> input, Map.Entry<String, Object> entry) {
        List<String> result = new ArrayList<>();

        String key = entry.getKey();
        Object value = entry.getValue();

        for (String current : input) {
            if (value instanceof Collection && current.contains(key)) {
                for (Object element : (Collection<?>) value) {
                    result.add(current.replace(key, element != null ? element.toString() : ""));
                }
            } else {
                result.add(current.replace(key, value != null ? value.toString() : ""));
            }
        }
        return result;
    }


    /**
     * A helper class for managing placeholder key-value pairs.
     * <p>
     * This wrapper simplifies storing placeholders where keys can be either integers or strings,
     * converting integer keys to strings internally for uniformity.
     * It also supports convenient handling of boolean text replacements.
     * </p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * PlaceholderWrapper wrapper = new PlaceholderWrapper()
     *      .put(1, "Value for 1")
     *      .put("username", "Steve")
     *      .putBooleans("Yes", "No");
     * Map<String, Object> placeholders = wrapper.getPlaceholders();
     * }</pre>
     */
    public static class PlaceholderWrapper {
        private final Map<String, Object> placeholders = new LinkedHashMap<>();
        private static final Set<String> alreadyWarn = new HashSet<>();

        /**
         * Adds a placeholder with an integer key.
         * <p>
         * The integer key is converted to a string internally and will
         * automatically be wrapped around {}.
         * </p>
         *
         * @param placeholderKey the integer key for the placeholder
         * @param value          the value associated with the placeholder
         * @return this {@code PlaceholderWrapper} instance for chaining
         */
        public PlaceholderWrapper put(final int placeholderKey, final Object value) {
            placeholders.put("{" + placeholderKey + "}", value);
            return this;
        }

        /**
         * Adds a placeholder with a string key.
         *
         * @param placeholderKey the string key for the placeholder
         * @param value          the value associated with the placeholder
         * @return this {@code PlaceholderWrapper} instance for chaining
         */
        public PlaceholderWrapper put(final String placeholderKey, final Object value) {
            this.warnAndSuggest(placeholderKey);
            placeholders.put(placeholderKey, value);
            return this;
        }

        /**
         * Adds boolean replacement text placeholders for {@code TRUE_TEXT} and {@code FALSE_TEXT} keys.
         * <p>
         * This is useful when replacing boolean values with custom string representations.
         * </p>
         *
         * @param replaceTrue  the string to use for {@code true} values
         * @param replaceFalse the string to use for {@code false} values
         */
        public void putBooleans(final String replaceTrue, final String replaceFalse) {
            placeholders.put(TRUE, replaceTrue);
            placeholders.put(FALSE, replaceFalse);
        }

        /**
         * Returns the internal map of placeholder keys and their associated values.
         *
         * @return an unmodifiable view of the placeholders map
         */
        public Map<String, Object> getPlaceholders() {
            return placeholders;
        }

        private void warnAndSuggest(String key) {
            if (!alreadyWarn.add(key)) return;

            boolean wrapped = (key.startsWith("{") && key.endsWith("}"))
                    || (key.startsWith("<") && key.endsWith(">"))
                    || (key.startsWith("[") && key.endsWith("]"))
                    || (key.startsWith("%") && key.endsWith("%"));

            if (!wrapped) {
                String suggested = "{" + key + "}";
                log.log(Level.WARNING, () -> "Warning: placeholder '" + key
                        + "' is not wrapped in {}, <>, [], or %%. If you use other wrapper symbols, you can safely ignore this. " +
                        "It is recommended you wrap your key like this: '" + suggested + "'.");
            }
        }

        public static void clearPlaceholderWarnings() {
            alreadyWarn.clear();
        }
    }
}
