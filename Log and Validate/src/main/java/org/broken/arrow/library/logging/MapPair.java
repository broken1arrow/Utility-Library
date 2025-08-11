package org.broken.arrow.library.logging;

/**
 * A simple key-value pair container.
 * <p>
 * This class holds an immutable pair consisting of a String key and an Object value.
 */
public class MapPair {
    private final String key;
    private final Object value;

    /**
     * Constructs a MapPair with the given key and value.
     *
     * @param key   the key for this pair, should not be null
     * @param value the value associated with the key, can be null
     */
    public MapPair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Factory method to create a new MapPair instance.
     *
     * @param key   the key for the pair, should not be null
     * @param value the value associated with the key, can be null
     * @return a new MapPair instance
     */
    public static MapPair of(String key, Object value) {
        return new MapPair(key, value);
    }

    /**
     * Gets the key of this pair.
     *
     * @return the key string
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value of this pair.
     *
     * @return the value object
     */
    public Object getValue() {
        return value;
    }
}