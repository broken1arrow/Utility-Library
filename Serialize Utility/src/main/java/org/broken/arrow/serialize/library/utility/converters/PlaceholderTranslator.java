package org.broken.arrow.serialize.library.utility.converters;

import org.broken.arrow.serialize.library.utility.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for replacing string placeholders with corresponding values.
 * Placeholders are identified using the format "{index}" where 'index' represents
 * the position of the value in the placeholders array.
 */
public class PlaceholderTranslator {

	private PlaceholderTranslator() {
	}

	/**
	 * Translates placeholders in a list of lore strings by replacing them with corresponding values.
	 * The placeholders are replaced in the order specified by the placeholders array.
	 *
	 * @param lores        The list of lore strings to translate.
	 * @param placeholders The values to replace the placeholders with.
	 * @return The translated list of lore strings.
	 */
	public static List<String> translatePlaceholdersLore(List<String> lores, Object... placeholders) {
		return translatePlaceholdersLore(null, lores, placeholders);
	}

	/**
	 * Translates placeholders in a list of lore strings by replacing them with corresponding values.
	 * The placeholders are replaced in the order specified by the placeholders array.
	 *
	 * @param lores        The list of lore strings to translate.
	 * @param placeholders The values to replace the placeholders with.
	 * @param replacements The pair of replacements for the boolean values.
	 *                     The first element will replace 'true' and the second will replace 'false'.
	 * @return The translated list of lore strings.
	 */
	public static List<String> translatePlaceholdersLore(Pair<String, String> replacements, List<String> lores, Object... placeholders) {
		if (lores == null) return new ArrayList<>();
		List<String> clonedlores = new ArrayList<>(lores);
		List<String> list = new ArrayList<>();
		for (String lore : clonedlores) {
			if (!checkListForPlaceholdersAndTranslate(lores, lore, placeholders)) {
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
			return replaceBoolens(rawText, replacements);
		return rawText;
	}

	/**
	 * Checks if the lore string contains a placeholder from the placeholders array and translates it.
	 * If the placeholder is a list, multiple translations are performed and added to the lores list.
	 *
	 * @param lores        The list of lore strings.
	 * @param lore         The lore string to check and translate.
	 * @param placeholders The values to replace the placeholders with.
	 * @return True if a placeholder was found and translated, false otherwise.
	 */
	public static boolean checkListForPlaceholdersAndTranslate(List<String> lores, String lore, Object... placeholders) {
		int number = containsList(placeholders);
		if (number < 0) return false;

		if (lore.contains("{" + number + "}")) {
			for (Object text : (List<?>) placeholders[number])
				lores.add(lore.replace(("{" + number + "}"), (String) text));
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
	public static int containsList(Object... placeholders) {
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
	private static String replaceBoolens(String text, Pair<String, String> replacements) {
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

}
