package org.broken.arrow.yaml.library.config.updater.utility;

import java.util.Map;

public class KeyUtils {

	/**
	 * Checks if the subKey is a sub path of the parentKey.
	 *
	 * @param parentKey the parent key to check against.
	 * @param subKey    the part of the key to check if it is a sub path of the parent key.
	 * @param separator the separator between each part of the key. The default is a dot.
	 * @return true if the subKey is a sub path of the parentKey; returns false if the parentKey is empty or does not contain the subKey.
	 */
	public static boolean isSubKeyOf(final String parentKey, final String subKey, final char separator) {
		if (parentKey.isEmpty())
			return false;

		return subKey.startsWith(parentKey)
				&& subKey.substring(parentKey.length()).startsWith(String.valueOf(separator));
	}

	/**
	 * Gets the amount of indentation spaces for the provided key.
	 *
	 * @param key       the key to check for the amount of indentation spaces.
	 * @param separator the separator used in the nested path. The default separator is a dot.
	 * @return a string contains only the amount of indentation spaces to add.
	 */
	public static String getIndents(final String key, final char separator) {
		final String[] splitKey = key.split("[" + separator + "]");
		final StringBuilder builder = new StringBuilder();

		for (int i = 1; i < splitKey.length; i++) {
			builder.append("  ");
		}
		return builder.toString();
	}
	
	/**
	 * Adds the specified number of indents to each line of the given string.
	 *
	 * @param s       the string to which indents are added.
	 * @param indents the indents to add to each line.
	 * @return the provided string with the correct number of indentations applied.
	 */
	public static String addIndentation(String s, String indents) {
		StringBuilder builder = new StringBuilder();
		String[] split = s.split("\n");

		for (String value : split) {
			if (builder.length() > 0)
				builder.append("\n");

			builder.append(indents).append(value);
		}

		return builder.toString();
	}

	/**
	 * Attempts to find the correct key in the sectionContext using the provided key and section context.
	 *
	 * @param key            the YAML key to be searched for in the section.
	 * @param sectionContext the configuration section (Map) from the YAML file.
	 * @return the value associated with the correct key in the configuration section, or null if not found.
	 */
	public static Object getKeyAsObject(String key, Map<Object, Object> sectionContext) {
		if (sectionContext.containsKey(key))
			return key;

		try {
			Float keyFloat = Float.parseFloat(key);

			if (sectionContext.containsKey(keyFloat))
				return keyFloat;
		} catch (NumberFormatException ignored) {
		}

		try {
			Double keyDouble = Double.parseDouble(key);

			if (sectionContext.containsKey(keyDouble))
				return keyDouble;
		} catch (NumberFormatException ignored) {
		}

		try {
			Integer keyInteger = Integer.parseInt(key);

			if (sectionContext.containsKey(keyInteger))
				return keyInteger;
		} catch (NumberFormatException ignored) {
		}

		try {
			Long longKey = Long.parseLong(key);

			if (sectionContext.containsKey(longKey))
				return longKey;
		} catch (NumberFormatException ignored) {
		}

		return null;
	}
}
