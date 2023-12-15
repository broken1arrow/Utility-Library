package org.broken.arrow.yaml.library.utillity;

import org.broken.arrow.serialize.library.DataSerializer;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * This class allow you easy set list of data to the file.
 * You then parse the data to {@link org.broken.arrow.yaml.library.YamlFileManager#setData( boolean, ConfigurationWrapper)}
 */
public class ConfigurationWrapper {


	private final String path;
	private final File file;
	private final FileConfiguration config;
	private final List<ConfigHelper> configurationCache;

	/**
	 * Creates a new ConfigurationWrapper instance with the specified file and an empty path.
	 * This method provide option to set path {@link #addData(String, org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable)}
	 * or alternatively set the path when serialize the data and use {@link #addAllData(java.util.List)}.
	 *
	 * @param file The file to save data to.
	 */
	public ConfigurationWrapper(@Nullable final File file) {
		this(file, "");
	}

	/**
	 * Creates a new ConfigurationWrapper instance with the specified file and path.
	 * This method provide option to set path {@link #addData(String, org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable)}
	 * or alternatively set the path when serialize the data and use {@link #addAllData(java.util.List)}.
	 *
	 * @param path the static path for this instance.
	 * @param file The file to save data to.
	 */
	public ConfigurationWrapper(@Nullable final File file, @Nonnull final String path) {
		this.file = file;
		this.path = path;
		this.configurationCache = new ArrayList<>();
		this.config = new YamlConfiguration();
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
	 * The list of configurations cached.
	 *
	 * @return a list of configurations to me saved to file.
	 */
	public List<ConfigHelper> getConfigurationsCache() {
		return configurationCache;
	}

	/**
	 * Adds a list of ConfigurationSerializable objects to the internal cache.
	 * Each object is wrapped in a ConfigHelper instance with an optional path.
	 *
	 * @param configuration The list of ConfigurationSerializable objects to add.
	 */
	public void addAllData(List<ConfigurationSerializable> configuration) {
		configuration.forEach(config -> configurationCache.add(new ConfigHelper(config)));
	}

	/**
	 * Adds a single ConfigurationSerializable object to the internal cache.
	 * The object is wrapped in a ConfigHelper instance with an optional path.
	 *
	 * @param path          The path to set for the serialized data.
	 * @param configuration The ConfigurationSerializable object to add.
	 */
	public void addData(String path, ConfigurationSerializable configuration) {
		configurationCache.add(new ConfigHelper(path, configuration));
	}

	/**
	 * Applies the changes from the cached configurations to the internal FileConfiguration.
	 * Each configuration is serialized and set in the FileConfiguration according to its specified path.
	 *
	 * @return The FileConfiguration with the data set.
	 */
	public FileConfiguration applyToConfiguration() {
		for (final ConfigHelper helper : getConfigurationsCache()) {
			for (Entry<String, Object> data : helper.configurationSerializable.serialize().entrySet()) {
				config.set((helper.isPathSet() ? helper.path + "." : isPathSet() ? this.getPath() : "") + data.getKey(), DataSerializer.serialize(data.getValue()));
			}
		}
		return config;
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

	private static class ConfigHelper {
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
