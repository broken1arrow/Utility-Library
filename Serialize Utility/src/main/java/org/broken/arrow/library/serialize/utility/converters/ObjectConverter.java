package org.broken.arrow.library.serialize.utility.converters;


import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.serialize.utility.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for checking and converting different types of objects to the desired class type.
 */
public class ObjectConverter {

	private ObjectConverter() {
	}

	/**
	 * Casts an object to a specific number type.
	 *
	 * @param object the object to be cast.
	 * @param clazz  the class representing the number type to convert to.
	 * @param <T>    the type for the number you want to convert to.
	 * @return the number with the specified class type.
	 */
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

	/**
	 * Casts a string to a specific number type.
	 *
	 * @param object the string to be cast.
	 * @param clazz  the class representing the number type to convert to.
	 * @param <T>    the type for the number you want to convert to.
	 * @return the number with the specified class type.
	 */
	public static <T> T convertStringToNumber(@Nonnull final String object, final Class<T> clazz) {
		Object obj = object;
		try {
			if (clazz.isAssignableFrom(Long.class)) {
				obj = Long.parseLong(object );
			}
			if (clazz.isAssignableFrom(Integer.class)) {
				obj = Integer.parseInt(object );
			}
			if (clazz.isAssignableFrom(Double.class)) {
				obj = Double.parseDouble(object );
			}
		} catch (NumberFormatException exception) {
			Object obje = Double.parseDouble("0.0");
			return (T) obje;
		}
		return (T) obj;
	}

	/**
	 * Retrieves the boolean value from an object. If the object is already a boolean,
	 * the method returns the boolean value as-is. If the object is a string, it checks
	 * if the string value is equal to "true" (case-sensitive) and returns the corresponding
	 * boolean value. For any other type of object, the method returns false.
	 *
	 * @param object The object from which to retrieve the boolean value.
	 * @return The boolean value from the object. Returns false if the object is not a boolean
	 * or a string equal to "true".
	 */
	public static boolean getBoolean(Object object) {
		if (object instanceof Boolean) {
			return (boolean) object;
		}
		if (object instanceof String) {
			return ((String) object).equalsIgnoreCase("true");
		}
		return false;
	}

	/**
	 * Converts the innermost key from a YAML path to a map with the corresponding key-value pair.
	 *
	 * <p>
	 * Example YAML structure:
	 * <pre>{@code
	 * somekey:
	 *   otherkey:
	 *     keyYouWant: value
	 * }
	 * </pre>
	 *
	 * <p>
	 * If you set the pathStartWith parameter to "somekey.otherkey", it will use the "keyYouWant" as the key in the resulting map.
	 *
	 * @param pathStartWith the path to the data you want to extract, using dots as the delimiter.
	 * @param serializedMap the map containing the key-value pairs.
	 * @return a map with the extracted key-value pair.
	 */
	public static Map<String, Object> convertToMap(@Nonnull final String pathStartWith, @Nonnull final Map<String, Object> serializedMap) {
		final Map<String, Object> values = new HashMap<>();
		for (final Map.Entry<String, Object> entity : serializedMap.entrySet()) {
			if (entity.getKey().startsWith(pathStartWith))
				values.put(entity.getKey().substring(entity.getKey().lastIndexOf('.') + 1), entity.getValue());
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
		for (final Map.Entry<String, Object> entity : serializedMap.entrySet()) {
			if (entity.getKey().startsWith(pathStartWith)) {
				values.add(Pair.of(entity.getKey().substring(entity.getKey().lastIndexOf('.') + 1), entity.getValue()));
			}
		}
		return values;
	}

	/**
	 * Casts an object to a map with specified key and value types.
	 *
	 * @param map        the object to be converted to a map.
	 * @param keyClazz   the class representing the type of keys in the resulting map.
	 * @param valueClazz the class representing the type of values in the resulting map.
	 * @param <K>        the type for the keys in the map.
	 * @param <V>        the type for the values in the map.
	 * @return a map with the specified key and value types; if any entry cannot be cast, returns an empty map.
	 */
	public static <K, V> Map<K, V> castMap(final Map<?, ?> map, final Class<K> keyClazz, final Class<V> valueClazz) {
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

	/**
	 * Converts a list of objects to a list of a specific class.
	 *
	 * @param list  the list to be checked.
	 * @param clazz the class that the elements of the list should be cast to.
	 * @param <L>   the type of the elements in the resulting list.
	 * @return a list of objects cast to the provided class; if any element cannot be cast, returns an empty list.
	 */
	public static <L> List<L> castList(final List<?> list, final Class<L> clazz) {
		if (list == null) return new ArrayList<>();
		return list.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
	}

	/**
	 * Converts a list of maps to a list of {@link java.util.LinkedHashMap}.
	 * This method is useful for converting a YAML structure stored in a list to a list of maps.
	 *
	 * <p>
	 * Example YAML structure:
	 * <pre>{@code
	 * key1:
	 *   - otherkey: value
	 *     otherkey2: value
	 *   - otherkey: value
	 *     otherkey2: value
	 * }
	 * </pre>
	 *
	 * @param list the list to be checked.
	 * @return a list of maps if the provided list contains map instances; otherwise, returns an empty list.
	 */
	public static List<Map<String, Object>> castListOfMaps(final List<?> list) {
		if (list == null) return new ArrayList<>();
		return list.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.map(map -> {
					Map<String, Object> newMap = new LinkedHashMap<>();
					map.forEach((key, value) -> newMap.put(key.toString(), value));
					return newMap;
				})
				.collect(Collectors.toList());
	}


}
