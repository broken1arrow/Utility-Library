package org.broken.arrow.yaml.library.config.updater.utility;

import java.util.Map;

public class KeyUtils {

	public static boolean isSubKeyOf(final String parentKey, final String subKey, final char separator) {
		if (parentKey.isEmpty())
			return false;

		return subKey.startsWith(parentKey)
				&& subKey.substring(parentKey.length()).startsWith(String.valueOf(separator));
	}

	public static String getIndents(final String key, final char separator) {
		final String[] splitKey = key.split("[" + separator + "]");
		final StringBuilder builder = new StringBuilder();

		for (int i = 1; i < splitKey.length; i++) {
			builder.append("  ");
		}
		return builder.toString();
	}

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

	//Will try to get the correct key by using the sectionContext
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
