package org.broken.arrow.library.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serialize and deserialize the map.
 */
public class SerializeUtility {

    private SerializeUtility() {
    }

    /**
     * Deserialize a map of generic key-value pairs using provided functions.
     *
     * @param dataMap the map containing data to be deserialized.
     * @param mapKey a function to deserialize map keys from String to type K.
     * @param serializingData a function to deserialize map values from Map&#60;String, Object&#62; to type V.
     * @return a deserialized map with converted keys and values.
     * @param <K> the type of keys in the original map.
     * @param <V> the type of values in the original map.
     */
    public static <K, V> Map<K, V> deSerialize(Map<String, Object> dataMap, Function<String, K> mapKey, Function<Map<String, Object>, V> serializingData) {
        return new DeSerialize<>(dataMap, mapKey, serializingData).deserialize();
    }

    /**
     * Serialize a map of generic key-value pairs using the provided function.
     *
     * @param dataMap the map containing data to be serialized.
     * @param serializingData a function to serialize map values from type V to Map&#60;String, Object&#62;.
     * @return a serialized map with converted keys and values.
     * @param <K> the type of keys in the original map.
     * @param <V> the type of values in the original map.
     */
    public static <K, V> Map<String, Object> serialize(Map<K, V> dataMap, Function<V, Map<String, Object>> serializingData) {
        return new Serialize<>(dataMap, serializingData).serialize();
    }

    /**
     * Serializes an object into a format suitable for storage or database usage.
     *
     * @param obj The object to serialize.
     * @return The serialized representation of the object.
     * @throws DataSerializer.SerializeFailedException If the serialization fails due to an unsupported data type or serialization error.
     */
    public static Object serialize(final Object obj) {
        return DataSerializer.serialize(obj);
    }

    private static class Serialize<K, V> {
        private final Map<K, V> map;
        private final Function<V, Map<String, Object>> serializingData;

        public Serialize(Map<K, V> map, Function<V, Map<String, Object>> serializingData) {
            this.map = map;
            this.serializingData = serializingData;
        }

        public Map<String, Object> serialize() {
            Map<String, Object> objectMap = new HashMap<>();
            this.getMap().forEach((key, value) -> {
                if (value != null) {
                    objectMap.put(key.toString(), this.getSerializingData().apply(value));
                }
            });
            return objectMap;
        }

        private Map<K, V> getMap() {
            return map;
        }

        private Function<V, Map<String, Object>> getSerializingData() {
            return serializingData;
        }
    }

    private  static class DeSerialize<K, V>  {
        private final Map<String, Object> dataMap;
        private final Function<Map<String, Object>, V> serializeFunction;
        private final Function<String, K> keyMapper;

        public DeSerialize(Map<String, Object> dataMap, Function<String, K> keyMapper, Function<Map<String, Object>, V> serializeFunction) {
            this.dataMap = dataMap;
            this.keyMapper = keyMapper;
            this.serializeFunction = serializeFunction;
        }

        public Map<K, V> deserialize() {
            Map<K, V> deserializedMap = new HashMap<>();
            dataMap.forEach((key, value) -> {
                if (value instanceof Map) {
                    K mappedKey = mapKey(key);
                    V deserializedValue = deserializeValue((Map<String, Object>) value);
                    deserializedMap.put(mappedKey, deserializedValue);
                }
            });
            return deserializedMap;
        }

        private V deserializeValue(Map<String, Object> value) {
            return serializeFunction.apply(value);
        }

        private K mapKey(String key) {
            return keyMapper.apply(key);
        }
    }
}