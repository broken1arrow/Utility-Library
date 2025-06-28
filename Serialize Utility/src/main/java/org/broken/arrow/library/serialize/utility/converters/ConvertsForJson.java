package org.broken.arrow.library.serialize.utility.converters;

import com.google.gson.Gson;
import org.broken.arrow.library.serialize.SerializeUtility;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Utility class for converting lists to JSON strings and vice versa.
 */
public class ConvertsForJson {

	private ConvertsForJson() {
	}

	/**
	 * Serializes a list to JSON format for easy storage in a database.
	 * Use {@link #convertFromJsonList(Class, String)} to deserialize the JSON string back to a list.
	 *
	 * @param key       The key to identify the JSON data.
	 * @param arrayList The list to be converted.
	 * @param <T>       The type of the list elements.
	 * @return The JSON string representation of the list.
	 */
	public static <T> String convertToJsonList(final String key, final List<T> arrayList) {
		final Map<String, List<Object>> maps = new HashMap<>();
		final Gson gson = new Gson();
		final List<Object> serializeList = new ArrayList<>();
		if (arrayList != null)
			for (final T value : arrayList) {
				if (value instanceof UUID)
					serializeList.add(String.valueOf(value));
				else
					serializeList.add(SerializeUtility.serialize(value));
			}
		maps.put(key, serializeList);

		return gson.toJson(maps);
	}

	/**
	 * Deserializes data from a JSON string and returns a list.
	 *
	 * @param classof  The class of the values to be retrieved.
	 * @param inputMap The JSON string containing the map of values.
	 * @param <T>      The type of the list elements.
	 * @return A list with the deserialized values from the JSON.
	 */
	public static <T> List<T> convertFromJsonList(final Class<T> classof, final String inputMap) {
		final ArrayList<T> arrayList = new ArrayList<>();
		final Gson gson = new Gson();
		final Map<String, List<Object>> map = gson.fromJson(inputMap, (Type) Map.class);
		if (map != null) {
			final Map<String, List<Object>> mapList = new HashMap<>(map);
			for (final Map.Entry<String, List<Object>> entry : mapList.entrySet())
				for (final Object deserilizedObject : entry.getValue()) {
					final Location loc = LocationSerializer.deserializeLoc(deserilizedObject);
					if (classof == Location.class && loc != null)
						arrayList.add(classof.cast(loc));
					else if (classof == UUID.class)
						arrayList.add(classof.cast(UUID.fromString(deserilizedObject.toString())));
					else if (classof.isInstance(deserilizedObject))
						arrayList.add(classof.cast(deserilizedObject));
				}
		}
		return arrayList;
	}

	/**
	 * Serializes a map to JSON format for easy storage in a database.
	 * Use {@link #convertFromJson(String)} to deserialize the JSON string back to a map.
	 *
	 * @param map The map to be converted to JSON.
	 * @return The JSON string representation of the map.
	 */
	public static String convertToJson(final Map<String, Object> map) {
		if (map == null) return "";
		final Map<String, Object> objectMap = new HashMap<>();
		final Gson gson = new Gson();

		for (final Entry<String, Object> value : map.entrySet()) {
			objectMap.put(value.getKey(), SerializeUtility.serialize(value.getValue()));
		}
		return gson.toJson(objectMap);
	}

	/**
	 * Deserializes data from a JSON string map and returns a map with keys and values
	 * as set in the JSON.
	 *
	 * @param json The JSON string containing the map of values.
	 * @return A map with the deserialized values from the JSON string.
	 */
	public static Map<String, Object> convertFromJson(final String json) {
		final Gson gson = new Gson();
		final Map<String, Object> objectMap = new HashMap<>();
		final Map<String, Object> map = gson.fromJson(json, (Type) Map.class);
		if (map == null) return objectMap;

		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			final Object deserializeObject = entry.getValue();
			final Location loc = LocationSerializer.deserializeLoc(deserializeObject);
			if (loc != null)
				objectMap.put(entry.getKey(), loc);
			else
				objectMap.put(entry.getKey(), deserializeObject);
		}
		return objectMap;
	}
}
