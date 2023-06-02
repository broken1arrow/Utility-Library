package org.broken.arrow.convert.library.utility.converters;


import org.broken.arrow.convert.library.utility.Pair;
import org.broken.arrow.convert.library.utility.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckAndConvertObjects {

	public static <T> T castToPrimaryNumber(@Nonnull final Object object, final Class<T> clazz) {
		Validate.checkNotNull(object, "the number can't be null");
		Object obj = object;
		if (obj instanceof String) {
			return convertStringToNumber((String) object, clazz);
		}
		if (clazz.isInstance(obj))
			return clazz.cast(obj);
		if (obj instanceof Integer) {
			if (clazz.isAssignableFrom(Double.class))
				obj = Double.valueOf(obj.toString());
			else if (clazz.isAssignableFrom(Long.class))
				obj = Long.valueOf(obj.toString());
			else if (clazz.isAssignableFrom(Float.class))
				obj = Float.valueOf(obj.toString());
			else
				obj = Integer.valueOf(obj.toString());
		}
		if (obj instanceof Long)
			obj = Long.valueOf(obj.toString());
		if (obj instanceof Double)
			obj = Double.valueOf(obj.toString());
		if (obj instanceof Float)
			obj = Float.valueOf(obj.toString());

		return (T) obj;
	}

	public static <T> T convertStringToNumber(@Nonnull final String object, final Class<T> clazz) {
		Object obj = object;
		try {
			if (clazz.isAssignableFrom(Long.class)) {
				obj = Long.parseLong(object);
			}
			if (clazz.isAssignableFrom(Integer.class)) {
				obj = Integer.parseInt(object);
			}
			if (clazz.isAssignableFrom(Double.class)) {
				obj = Double.parseDouble(object);
			}
		} catch (NumberFormatException exception) {
			Object obje = Integer.parseInt("0");
			return (T) obje;
		}
		return (T) obj;
	}


	/**
	 * Convert the innerst key from your yaml path
	 * to map with the key and value it find.
	 * <p>
	 * Like this example inside yamlfile.
	 * <pre> {@code
	 * somekey:
	 *   otherkey:
	 *     keyYouWant: value
	 * }
	 * </pre>
	 * <p>
	 * You then set pathStartWith "somekey.otherkey" it will then use keyYouWant as a key for your map.
	 *
	 * @param pathStartWith path to your file, use dots as delimiter.
	 * @param serializedMap the map to key the key and value.
	 * @return map with the set values.
	 */

	public static Map<String, Object> convertToMap(@Nonnull final String pathStartWith, @Nonnull final Map<String, Object> serializedMap) {
		final Map<String, Object> values = new HashMap<>();
		for (final Map.Entry<String, Object> ententy : serializedMap.entrySet()) {
			if (ententy.getKey().startsWith(pathStartWith))
				values.put(ententy.getKey().substring(ententy.getKey().lastIndexOf('.') + 1), ententy.getValue());
		}
		return values;
	}

	/**
	 * Converts the innermost key-value pairs from a serialized map based on the provided YAML path.
	 * <p>
	 * Example YAML structure:
	 * <pre> {@code
	 * somekey:
	 *   otherkey:
	 *     keyYouWant: value
	 * }
	 * </pre>
	 * <p>
	 * If the pathStartWith parameter is set to "somekey.otherkey", the method will extract the key-value pairs
	 * with "keyYouWant" as the key for your map.
	 *
	 * @param pathStartWith The path to the desired key-value pairs from the innermost key.
	 * @param serializedMap The serialized map containing the key-value pairs.
	 * @return A list of pairs representing the extracted key-value pairs.
	 */
	public static List<Pair<String, Object>> convertToPair(@Nonnull final String pathStartWith, @Nonnull final Map<String, Object> serializedMap) {
		final List<Pair<String, Object>> values = new ArrayList<>();
		for (final Map.Entry<String, Object> ententy : serializedMap.entrySet()) {
			if (ententy.getKey().startsWith(pathStartWith)) {
				values.add(Pair.of(ententy.getKey().substring(ententy.getKey().lastIndexOf('.') + 1), ententy.getValue()));
			}
		}
		return values;
	}

	public static <K, V> Map<K, V> castMapTo(final Map<?, ?> map, final Class<K> keyClazz, final Class<V> valueClazz) {
		final Map<K, V> convertMap = new LinkedHashMap<>();
		for (final Map.Entry<?, ?> entry : map.entrySet()) {
			final Object key = entry.getKey();
			final Object value = entry.getValue();
			if (keyClazz.isInstance(key) && valueClazz.isInstance(value)) {
				convertMap.put(keyClazz.cast(key), valueClazz.cast(value));
			}
		}
		return convertMap;
	}

	public static <L> List<L> castListTo(final List<?> list, final Class<L> clazz) {
		if (list == null) return null;
		return list.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
	}
}
