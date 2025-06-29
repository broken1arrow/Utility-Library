package org.broken.arrow.library.serialize.utility.converters;

import org.broken.arrow.library.serialize.utility.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class for replacing string placeholders with corresponding values.
 * Placeholders are identified using the format "{index}" where 'index' represents
 * the position of the value in the placeholders array.
 */
public class PlaceholderTranslator {

    private PlaceholderTranslator() {
    }

    /**
     * Translates placeholders in a list of strings by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param listOfText   The list of strings to translate.
     * @param placeholders The values to replace the placeholders with.
     * @return The translated list of lore strings.
     */
    public static List<String> translatePlaceholdersLore(List<String> listOfText, Object... placeholders) {
        return translatePlaceholdersLore(null, listOfText, placeholders);
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
    public static List<String> translatePlaceholdersLore(Pair<String, String> replacements, List<String> listOfText, Object... placeholders) {
        if (listOfText == null) return new ArrayList<>();
        List<String> clonedLore = new ArrayList<>(listOfText);
        List<String> list = new ArrayList<>();
        for (String lore : clonedLore) {
            if (!checkListForPlaceholdersAndTranslate(listOfText, lore, placeholders)) {
                if (replacements != null) {
                    list.add(translatePlaceholders(replacements, lore, placeholders));
                } else {
                    list.add(translatePlaceholders(lore, placeholders));
                }
            }
        }
        return list;
    }

    /**
     * Translates placeholders in a list of strings by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param listOfText   The list of strings to translate.
     * @param placeholders The consumer where you provide the placeholder data.
     * @return The translated list of lore strings.
     */
    public static List<String> translatePlaceholders(List<String> listOfText, Consumer<PlaceholderWrapper> placeholders) {
        if (listOfText == null) return new ArrayList<>();
        List<String> clonedLore = new ArrayList<>(listOfText);
        List<String> list = new ArrayList<>();
        for (String text : clonedLore) {
            final PlaceholderWrapper placeholderWrapper = new PlaceholderWrapper();
            placeholders.accept(placeholderWrapper);
            final Map<String, Object> placeholderMap = placeholderWrapper.getPlaceholders();

            if (!checkListForPlaceholdersAndTranslate(text, listOfText, placeholderMap)) {
                list.add(translatePlaceholders(text, placeholderMap));
            }
        }
        return list;
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param rawText         The raw text to translate.
     * @param wrapperConsumer The consumer where you provide the placeholder data.
     * @return The translated text.
     */
    public static String translatePlaceholder(String rawText, final Consumer<PlaceholderWrapper> wrapperConsumer) {
        final PlaceholderWrapper placeholderWrapper = new PlaceholderWrapper();
        wrapperConsumer.accept(placeholderWrapper);
        final Map<String, Object> placeholderMap = placeholderWrapper.getPlaceholders();

        return translatePlaceholders(rawText, placeholderMap);
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param rawText        The raw text to translate.
     * @param placeholderMap The map with key-value, where key is used to fins the placeholder in the provided text.
     * @return The translated text.
     */
    public static String translatePlaceholders(String rawText, final Map<String, Object> placeholderMap) {
        if (placeholderMap == null || placeholderMap.isEmpty())
            return rawText;

        for (Map.Entry<String, Object> entry : placeholderMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (rawText == null || value instanceof List)
                continue;
            if (rawText.contains("{" + key + "}")) {
                rawText = rawText.replace("{" + key + "}", value != null ? value.toString() : "");
            } else {
                rawText = rawText.replace(key, value != null ? value.toString() : "");
            }
        }
        return rawText;
    }

    /**
     * Checks if the list of strings contains a placeholder from the placeholders array and translates it.
     * If the placeholder is a list, multiple translations are performed and added to the lore list.
     *
     * @param listOfText   The list of  strings.
     * @param rawText      The text string to check and translate.
     * @param placeholders The values to replace the placeholders with.
     * @return True if a placeholder was found and translated, false otherwise.
     */
    public static boolean checkListForPlaceholdersAndTranslate(String rawText, List<String> listOfText, Map<String, Object> placeholders) {
        Map.Entry<String, Object> entry = containsList(placeholders);
        if (entry == null) return false;

        final String key = entry.getKey();
        if (rawText.contains("{" + key + "}")) {
            for (Object text : (List<?>) entry.getValue())
                listOfText.add(rawText.replace(("{" + key + "}"), (String) text));
            return true;
        }
        return false;
    }

    /**
     * Checks if the placeholders array contains a list and returns its index.
     *
     * @param placeholders The values to replace the placeholders with.
     * @return The index of the list in the placeholders array, or -1 if not found.
     */
    public static Map.Entry<String, Object> containsList(final Map<String, Object> placeholders) {
        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                if (entry.getValue() instanceof List) {
                    return entry;
                }
            }
        }
        return null;
    }


    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param rawText      The raw text to translate.
     * @param placeholders The values to replace the placeholders with.
     * @param replacements The pair of replacements for the boolean values.
     *                     The first element will replace 'true' and the second will replace 'false'.
     * @return The translated text.
     */
    public static String translatePlaceholders(Pair<String, String> replacements, String rawText, Object... placeholders) {
        if (placeholders != null)
            for (int i = 0; i < placeholders.length; i++) {
                if (rawText == null || placeholders[i] instanceof List)
                    continue;

                rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
            }
        if (replacements != null)
            return replaceBooleans(rawText, replacements);
        return rawText;
    }


    /**
     * Checks if the list of strings contains a placeholder from the placeholders array and translates it.
     * If the placeholder is a list, multiple translations are performed and added to the lore list.
     *
     * @param listOfText   The list of  strings.
     * @param rawText      The text string to check and translate.
     * @param placeholders The values to replace the placeholders with.
     * @return True if a placeholder was found and translated, false otherwise.
     */
    public static boolean checkListForPlaceholdersAndTranslate(List<String> listOfText, String rawText, Object... placeholders) {
        int number = containsList(placeholders);
        if (number < 0) return false;

        if (rawText.contains("{" + number + "}")) {
            for (Object text : (List<?>) placeholders[number])
                listOfText.add(rawText.replace(("{" + number + "}"), (String) text));
            return true;
        }
        return false;
    }

    /**
     * Translates placeholders in a raw text by replacing them with corresponding values.
     * The placeholders are replaced in the order specified by the placeholders array.
     *
     * @param rawText      The raw text to translate.
     * @param placeholders The values to replace the placeholders with.
     * @return The translated text.
     */
    public static String translatePlaceholders(String rawText, Object... placeholders) {
        return translatePlaceholders(null, rawText, placeholders);
    }


    /**
     * Checks if the placeholders array contains a list and returns its index.
     *
     * @param placeholders The values to replace the placeholders with.
     * @return The index of the list in the placeholders array, or -1 if not found.
     */
    public static int containsList(final Object... placeholders) {
        if (placeholders != null)
            for (int i = 0; i < placeholders.length; i++)
                if (placeholders[i] instanceof List)
                    return i;
        return -1;
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
        if (text != null) {
            if (text.contains("true")) {
                return text.replace("true", replacements.getFirst());
            } else if (text.contains("false")) {
                return text.replace("false", replacements.getSecond());
            } else {
                return text;
            }
        }
        return "";
    }

    public static class PlaceholderWrapper {

        private final Map<String, Object> placeholders = new HashMap<>();

        public PlaceholderWrapper put(final int placeholderKey, final Object value) {
            placeholders.put(placeholderKey + "", value);
            return this;
        }

        public PlaceholderWrapper put(final String placeholderKey, final Object value) {
            placeholders.put(placeholderKey, value);
            return this;
        }

        public void putBooleans(final String replaceTrue, final String replaceFalse) {
            placeholders.put("true", replaceTrue);
            placeholders.put("false", replaceFalse);
        }

        public Map<String, Object> getPlaceholders() {
            return placeholders;
        }
    }


}
