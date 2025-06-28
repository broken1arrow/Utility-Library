package org.broken.arrow.logging.library;

public class MapPair {
        private final String key;
        private final Object value;

        public MapPair(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public static MapPair of(String key, Object value) {
            return new MapPair(key, value);
        }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}