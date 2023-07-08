package org.broken.arrow.yaml.library.config.updater.utility;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a key builder used for parsing and manipulating configuration keys.
 * This class allows for building hierarchical configuration keys.
 * <p>
 * This class was initially created by tchristofferson on 2/8/2019 and last modified at 06/30/2023 by broken_arrow.
 * </p>
 *
 * @author tchristofferson (original implementation)
 * @author broken_arrow.
 */

public class KeyBuilder implements Cloneable {

	private final FileConfiguration config;
	private final char separator;
	private final StringBuilder key;
	private final List<String> defaultConfKeys;


	/**
	 * Constructs a new KeyBuilder with the specified FileConfiguration and separator.
	 *
	 * @param config    The FileConfiguration associated with the KeyBuilder.
	 * @param separator The character used as a separator for building hierarchical keys.
	 */
	public KeyBuilder(final FileConfiguration config, final char separator) {
		this.config = config;
		this.separator = separator;
		this.key = new StringBuilder();
		this.defaultConfKeys = new ArrayList<>(config.getKeys(true));
	}

	private KeyBuilder(final KeyBuilder keyBuilder) {
		this.config = keyBuilder.config;
		this.separator = keyBuilder.separator;
		this.key = new StringBuilder(keyBuilder.toString());
		this.defaultConfKeys = keyBuilder.defaultConfKeys;
	}

	/**
	 * Processes the key and checks if the key is still valid. This method creates the key
	 * by adding each part of the key parsed from the line to the {@link #key}.
	 * <p>
	 * For example, if the first key is 'key1' and the next valid key is 'key2', it will be combined
	 * to 'key1.key2'. Furthermore, it will remove the last key if it is not a valid YAML path or set it
	 * back to the first non-nested key from currentMappingKey.
	 *
	 * @param line              The line to process from the input scanned file.
	 * @param currentMappingKey The non-nested key in the YAML key.
	 * @return A valid YAML key for the current path.
	 */
	public String processKey(String line, String currentMappingKey) {
		final StringBuilder lastCheckedKey = this.getKey();
		final FileConfiguration defaultConfig = this.getConfig();
		int lastIndex = line.lastIndexOf(":");
		final String substring = line.substring(0, Math.min(lastIndex, line.length()));

		if (this.isEmpty() || defaultConfig.contains(lastCheckedKey.toString() + separator + substring)) {
			parseLine(line);
		} else {
			int nextKeyIndex = this.defaultConfKeys.indexOf(lastCheckedKey.toString()) + 1;
			if (nextKeyIndex < this.defaultConfKeys.size()) {
				String nextKey = this.defaultConfKeys.get(nextKeyIndex);
				if (!this.isEmpty() && !nextKey.equals(lastCheckedKey.toString())) {
					this.removeLastKey();
					if (defaultConfig.contains(lastCheckedKey.toString() + separator + substring))
						return lastCheckedKey.toString() + separator + substring;
				}
				parseLine(currentMappingKey);
			}
			return null;
		}
		return lastCheckedKey.toString();
	}

	/**
	 * Parses the given line and updates the KeyBuilder accordingly.
	 *
	 * @param line The line to parse and extract key information from.
	 */
	public void parseLine(String line) {
		line = line.trim();
		String[] currentSplitLine = line.split(":");

		if (currentSplitLine.length > 2)
			currentSplitLine = line.split(": ");

		String key = currentSplitLine[0].replace("'", "").replace("\"", "");

		//Checks keyBuilder path against config to see if the path is valid.
		//If the path doesn't exist in the config it keeps removing last key in keyBuilder.
		while (this.key.length() > 0 && !config.contains(this.key.toString() + separator + key)) {
			removeLastKey();
		}

		//Add the separator if there is already a key inside keyBuilder
		//If currentSplitLine[0] is 'key2' and keyBuilder contains 'key1' the result will be 'key1.' if '.' is the separator
		if (this.key.length() > 0)
			this.key.append(separator);

		//Appends the current key to keyBuilder
		//If keyBuilder is 'key1.' and currentSplitLine[0] is 'key2' the resulting keyBuilder will be 'key1.key2' if separator is '.'
		this.key.append(key);
	}

	/**
	 * Retrieves the last key in the KeyBuilder.
	 *
	 * @return The last key in the KeyBuilder.
	 */
	public String getLastKey() {
		if (this.key.length() == 0)
			return "";

		return this.key.toString().split("[" + separator + "]")[0];
	}

	/**
	 * Checks if the KeyBuilder is empty.
	 *
	 * @return {@code true} if the KeyBuilder is empty, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return this.key.length() == 0;
	}

	/**
	 * Clear the builder.
	 */
	public void clear() {
		this.key.setLength(0);
	}

	/**
	 * Get the yaml key.
	 *
	 * @return the key.
	 */
	public StringBuilder getKey() {
		return this.key;
	}

	public FileConfiguration getConfig() {
		return config;
	}


	/**
	 * Checks if the KeyBuilder's path is a sub-key of the specified parent key.
	 *
	 * @param parentKey The parent key to check against.
	 * @return {@code true} if the KeyBuilder's path is a sub-key of the parent key, {@code false} otherwise.
	 */
	public boolean isSubKeyOf(final String parentKey) {
		return KeyUtils.isSubKeyOf(parentKey, this.key.toString(), separator);
	}

	/**
	 * Checks if the specified subKey is a sub-key of the KeyBuilder's path.
	 *
	 * @param subKey The sub-key to check.
	 * @return {@code true} if the subKey is a sub-key of the KeyBuilder's path, {@code false} otherwise.
	 */
	public boolean isSubKey(final String subKey) {
		return KeyUtils.isSubKeyOf(this.key.toString(), subKey, separator);
	}

	/**
	 * Checks if the KeyBuilder's path represents a configuration section.
	 *
	 * @return {@code true} if the KeyBuilder's path represents a configuration section, {@code false} otherwise.
	 */
	public boolean isConfigSection() {
		final String key = this.key.toString();
		return config.isConfigurationSection(key);
	}

	/**
	 * Checks if the KeyBuilder's path represents a configuration section or is empty.
	 *
	 * @return {@code true} if the KeyBuilder's path represents a configuration section or is empty, {@code false} otherwise.
	 */
	public boolean isConfigSectionOrEmpty() {
		final String key = this.key.toString();
		ConfigurationSection configurationSection = config.getConfigurationSection(key);
		return config.isConfigurationSection(key) && (configurationSection == null || !configurationSection.getKeys(false).isEmpty());
	}

	/**
	 * Removes the last key from the KeyBuilder's path.
	 *
	 * <p>
	 * For example, if the KeyBuilder's path is 'key1.key2', calling this method will result in the path 'key1'.
	 * </p>
	 */
	public void removeLastKey() {
		if (this.key.length() == 0)
			return;

		final String keyString = this.key.toString();
		//Must be enclosed in brackets in case a regex special character is the separator
		final String[] split = keyString.split("[" + separator + "]");
		//Makes sure begin index isn't < 0 (error). Occurs when there is only one key in the path
		final int minIndex = Math.max(0, this.key.length() - split[split.length - 1].length() - 1);
		this.key.replace(minIndex, this.key.length(), "");
	}

	/**
	 * Appends a new line character to the provided StringBuilder.
	 */
	public void appendNewLine() {
		if (this.key.length() > 0) this.key.append("\n");
	}

	/**
	 * Returns a string representation of the KeyBuilder.
	 *
	 * @return The string representation of the KeyBuilder.
	 */
	@Override
	public String toString() {
		return this.key.toString();
	}

	/**
	 * Creates a copy of the KeyBuilder object.
	 *
	 * @return A new KeyBuilder object with the same state as the current KeyBuilder.
	 */
	@Override
	public KeyBuilder clone() {
		return new KeyBuilder(this);
	}
}
