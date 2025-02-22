package org.broken.arrow.yaml.library.utillity.transform;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the transformation of maps into YAML-compatible structures and vice versa.
 * Provides methods for encoding maps (adding YAML type markers) and decoding maps (removing type markers and restoring nesting).
 */
public class MapYamlConverter {

    /**
     * Transforms a given map into a YAML-friendly format by adding "_type: map" to all nested maps.
     *
     * @param originalMap The original map to process.
     * @return A new map with "_type: map" added to nested maps where necessary.
     */
    public static Map<String, Object> encodeMap(Map<String, Object> originalMap) {
        return processNestedMapForSave(originalMap);
    }

    /**
     * Reconstructs a map from a YAML structure by removing "_type: map" entries and restoring proper nesting.
     *
     * @param path The YAML path to load from.
     * @param config The file configuration to load data from.
     * @return A map without "_type" entries, with restored nesting.
     */
    public static Map<String, Object> decodeConfig(String path, FileConfiguration config) {
        return processNestedMapForLoad( path,config);
    }

    // --- Helper Methods --- //

    private static Map<String, Object> processNestedMapForSave(Map<String, Object> inputMap) {
        Map<String, Object> processedMap = new LinkedHashMap<>();
        processedMap.put("_type", "map");

        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Recursively process nested maps
            if (value instanceof Map) {
                value = processNestedMapForSave((Map<String, Object>) value);
            }

            processedMap.put(key, value);
        }
        return processedMap;
    }

    private static Map<String, Object> processNestedMapForLoad(String path, FileConfiguration config) {
        final Map<String, Object> nestedMap = new HashMap<>();
        final ConfigurationSection configurationSection = config.getConfigurationSection(path);
        if (configurationSection != null) {
            for (String yamlKey : configurationSection.getKeys(false)) {
                String fullPath = path + "." + yamlKey;

                if ("_type".equals(yamlKey)) continue;

                Object nestedValue = config.get(fullPath);
                if (nestedValue instanceof ConfigurationSection) {
                    nestedMap.put(yamlKey, processNestedMapForLoad(fullPath, config));
                } else {
                    nestedMap.put(yamlKey, nestedValue);
                }
            }
        }
        return nestedMap;
    }

}
