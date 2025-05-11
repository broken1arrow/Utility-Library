package org.broken.arrow.yaml.library.utillity;

import org.broken.arrow.serialize.library.SerializeUtility;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.yaml.library.utillity.transform.MapYamlConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class allow you easy set list of data to the file.
 * You then parse the data to {@link org.broken.arrow.yaml.library.YamlFileManager#setData(boolean, ConfigurationWrapper)}
 */
public class ConfigurationWrapper {


    private final String path;
    private final File file;
    private final FileConfiguration configuration;
    private final List<ConfigHelper> configurationCache;
    private final boolean convertNestedSections;

    /**
     * Creates a new ConfigurationWrapper instance with the specified file and an empty path.
     * This method provide option to set path {@link #addSerializableData(String, org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable)}
     * or alternatively set the path when serialize the data and use {@link #addAllSerializableData(List)}.
     * <p>&nbsp;</p>
     * <p><strong>Note:</strong> When using this constructor, nested maps are not modified.
     * If you need to handle nested structures, consider converting them into a list or another appropriate key-value structure.</p>
     *
     * @param file The file where data will be saved.
     */
    public ConfigurationWrapper(@Nullable final File file) {
        this(false, file, "");
    }

    /**
     * Creates a new ConfigurationWrapper instance with the specified file and an empty path.
     * This method provide option to set path {@link #addSerializableData(String, org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable)}
     * or alternatively set the path when serialize the data and use {@link #addAllSerializableData(List)}.
     *
     * @param convertNestedSections {@code true} to convert nested sections into maps, {@code false} to keep them in their original format.
     * @param file                  The file where data will be saved.
     */
    public ConfigurationWrapper(final boolean convertNestedSections, @Nullable final File file) {
        this(convertNestedSections, file, "");
    }

    /**
     * Creates a new ConfigurationWrapper instance with the specified file and path.
     * This method provide option to set path {@link #addSerializableData(String, org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable)}
     * or alternatively set the path when serialize the data and use {@link #addAllSerializableData(List)}.
     *
     * @param convertNestedSections {@code true} to convert nested sections into maps, {@code false} to keep them in their original format.
     * @param path                  the static path for this instance.
     * @param file                  The file where data will be saved.
     */
    public ConfigurationWrapper(final boolean convertNestedSections, @Nullable final File file, @Nonnull final String path) {
        this.convertNestedSections = convertNestedSections;
        this.file = file;
        this.path = path;
        this.configurationCache = new ArrayList<>();
        this.configuration = new YamlConfiguration();
    }


    /**
     * The file to save data to.
     *
     * @return the file or null if file is not set.
     */
    @Nullable
    public File getFile() {
        return file;
    }

    /**
     * The path to set, you you want to set static path.
     *
     * @return the path it should structure data inside the file.
     */
    public String getPath() {
        return path;
    }

    /**
     * Checks if path is set.
     *
     * @return true if path is not null and empty.
     */
    public boolean isPathSet() {
        return path != null && !path.isEmpty();
    }

    /**
     * Checks whether nested {@link ConfigurationSection} objects should be converted into {@code Map<String, Object>},
     * or if they should be kept in their original format (e.g., as a {@link ConfigurationSection}, list, or scalar value).
     *
     * @return {@code true} if nested sections are converted into maps, {@code false} if they remain in their original format.
     */
    public boolean isConvertNestedSections() {
        return convertNestedSections;
    }


    /**
     * The list of configurations cached.
     *
     * @return a list of configurations to be saved to file.
     */
    public List<ConfigHelper> getConfigurationsCache() {
        return configurationCache;
    }

    /**
     * Adds a map of ConfigurationSerializable objects to the internal cache. Each
     * object is wrapped in a ConfigHelper instance with the map key as the path.
     *
     * @param path              Alternative to set a static path for every key.
     * @param configurationsMap The list of ConfigurationSerializable objects to add.
     */
    public void addAllSerializableData(@Nullable String path, @Nonnull Map<String, ConfigurationSerializable> configurationsMap) {
        Valid.checkNotNull(configurationsMap, "The map must not be null, and it should contain keys and values of ConfigurationSerializable data.");
        final String finalAlternativePath = getPath(path);
        configurationsMap.forEach((key, value) -> configurationCache.add(new ConfigHelper(finalAlternativePath + key, value)));
    }

    /**
     * Adds a list of ConfigurationSerializable objects to the internal cache.
     * Each object is wrapped in a ConfigHelper instance with an optional path.
     *
     * @param configurationsList The list of ConfigurationSerializable objects to add.
     */
    public void addAllSerializableData(@Nonnull List<ConfigurationSerializable> configurationsList) {
        Valid.checkNotNull(configurationsList, "The list can't be null, you need provide a list of Serializable data.");

        configurationsList.forEach(config -> configurationCache.add(new ConfigHelper(config)));
    }

    /**
     * Adds a single ConfigurationSerializable object to the internal cache.
     * The object is wrapped in a ConfigHelper instance with an optional path.
     *
     * @param path          The path to set for the serialized data.
     * @param configuration The ConfigurationSerializable object to add.
     */
    public void addSerializableData(String path, ConfigurationSerializable configuration) {
        configurationCache.add(new ConfigHelper(path, configuration));
    }

    /**
     * Applies the changes from the cached configurations to the internal FileConfiguration.
     * Each configuration is serialized and set in the FileConfiguration according to its specified path.
     *
     * @return The FileConfiguration with the data set.
     */
    public FileConfiguration applyToConfiguration() {
        getConfigurationsCache().forEach(helper -> {
            Map<String, Object> serializedData = helper.serialize();
            String basePath = getPath(helper);
            serializedData.forEach((key, value) -> setToConfig(helper, basePath, key, value));
        });
        return configuration;
    }

    private void setToConfig(final ConfigHelper helper, final String basePath, final String key, final Object value) {
        Object data = SerializeUtility.serialize(value);

        if (this.isConvertNestedSections() && data instanceof Map) {
            Map<String, Object> encodeMap = MapYamlConverter.encodeMap((Map<String, Object>) data);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) encodeMap).entrySet()) {
                configuration.set(basePath + (helper.isPathSet() ? "." : "") + key + "." + entry.getKey(), entry.getValue());
            }
        } else
            configuration.set(basePath + (helper.isPathSet() ? "." : "") + key, data);

    }

    /**
     * Returns a valid path for serialization purposes. If the provided path is null or empty,
     * an empty string is returned. If the path doesn't end with a dot, a dot is appended.
     *
     * @param path The input path to be processed.
     * @return A valid path for serialization.
     */
    @Nonnull
    private String getPath(@Nullable final String path) {
        String alternativePath = path == null || path.isEmpty() ? "" : path;
        if (!alternativePath.isEmpty() && !alternativePath.contains("."))
            alternativePath = alternativePath + ".";
        return alternativePath;
    }

    private String getPath(ConfigHelper helper) {
        if (helper.isPathSet()) {
            return helper.path;
        }
        return isPathSet() ? this.getPath() : "";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationWrapper)) return false;

        final ConfigurationWrapper that = (ConfigurationWrapper) o;

        if (!Objects.equals(path, that.path)) return false;
        return Objects.equals(configurationCache, that.configurationCache);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (configurationCache != null ? configurationCache.hashCode() : 0);
        return result;
    }

    protected static class ConfigHelper {
        private final String path;
        private final ConfigurationSerializable configurationSerializable;

        public ConfigHelper(final ConfigurationSerializable configurationSerializable) {
            this("", configurationSerializable);
        }

        public ConfigHelper(final String path, final ConfigurationSerializable configurationSerializable) {
            this.path = path;
            this.configurationSerializable = configurationSerializable;
        }

        /**
         * Checks if path is set.
         *
         * @return true if path is not null and empty.
         */
        public boolean isPathSet() {
            return path != null && !path.isEmpty();
        }

        /**
         * Serialize the data from the ConfigurationSerializable instance.
         * <p>&nbsp;</p>
         *
         * @return the map of data serialized or empty linked map.
         */
        @Nonnull
        public Map<String, Object> serialize() {
            if (configurationSerializable == null) return new LinkedHashMap<>();

            return configurationSerializable.serialize();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof ConfigHelper)) return false;

            final ConfigHelper that = (ConfigHelper) o;

            if (!Objects.equals(path, that.path)) return false;
            return Objects.equals(configurationSerializable, that.configurationSerializable);
        }

        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (configurationSerializable != null ? configurationSerializable.hashCode() : 0);
            return result;
        }
    }
}
