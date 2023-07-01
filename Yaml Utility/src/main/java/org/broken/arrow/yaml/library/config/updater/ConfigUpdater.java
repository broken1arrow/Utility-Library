package org.broken.arrow.yaml.library.config.updater;

import org.broken.arrow.yaml.library.config.updater.utility.KeyBuilder;
import org.broken.arrow.yaml.library.config.updater.utility.KeyCache;
import org.broken.arrow.yaml.library.config.updater.utility.KeyUtils;
import org.broken.arrow.yaml.library.utillity.Valid;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A library for updating configuration files while preserving comments.
 * This class is used to update config files and keep track of the associated comments.
 * <p>
 * This class was initially created by tchristofferson on 2/8/2019 and last modified at 06/30/2023 by broken_arrow.
 * </p>
 *
 * @author tchristofferson (original implementation)
 * @author broken_arrow.
 */
public class ConfigUpdater {

	//Used for separating keys in the keyBuilder inside parseComments method
	private static final char SEPARATOR = '.';
	private final Plugin plugin;
	private final List<String> ignoredSections;

	/**
	 * Constructs a new ConfigUpdater with the specified plugin and ignored sections.
	 *
	 * @param plugin          the plugin associated with the configuration file
	 * @param ignoredSections the sections of the configuration file to ignore during updates
	 */
	public ConfigUpdater(Plugin plugin, String... ignoredSections) {
		this.plugin = plugin;
		this.ignoredSections = Arrays.asList(ignoredSections);
	}

	/**
	 * Updates the specified configuration file based on the given version and resource name.
	 * The updated configuration is written back to the file, preserving comments.
	 *
	 * @param version      the version to check against for updates
	 * @param resourceName the name of the resource file containing the default configuration
	 * @param toUpdate     the file to update
	 * @throws IOException if an I/O error occurs while updating the file
	 */
	public void update(int version, final String resourceName, final File toUpdate) throws IOException {
		Valid.checkBoolean(toUpdate.exists(), "The toUpdate file doesn't exist!");

		final InputStream resource = this.plugin.getResource(resourceName);
		Valid.checkNotNull(resource, "the file " + resourceName + " not exist in plugin jar.");
		final String updatedConfig = updateConfig(version, resourceName, toUpdate);

		if (!updatedConfig.isEmpty()) writeConfig(updatedConfig, toUpdate);
	}

	/**
	 * Updates the configuration file based on the specified version and returns the updated contents as a string.
	 *
	 * @param version      The version to compare against for updating the configuration.
	 * @param resourceName The name of the resource file to load from the plugin jar.
	 * @param toUpdate     The file to update with the new configuration.
	 * @return The updated configuration file contents as a string.
	 * @throws IOException If an I/O error occurs while updating the configuration.
	 */
	private String updateConfig(int version, final String resourceName, final File toUpdate) throws IOException {
		final InputStream resource = this.plugin.getResource(resourceName);
		Valid.checkNotNull(resource, "the file " + resourceName + " not exist in plugin jar.");

		final FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
		if (!checkIfShallUpdate(currentConfig, version)) return "";

		final FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));

		final KeyCache comments = parseComments(plugin.getResource(resourceName), defaultConfig);
		final Map<String, String> ignoredSectionsValues = parseIgnoredSections(toUpdate, currentConfig, comments, ignoredSections == null ? Collections.emptyList() : ignoredSections);

		// will write updated config file "contents" to a string
		StringWriter writer = this.write(version, defaultConfig, currentConfig, comments, ignoredSectionsValues);
		return writer.toString();
	}

	/**
	 * Writes the specified value to the given file if the updated contents are different from the current file contents.
	 *
	 * @param value The value to write to the file.
	 * @param file  The file to write the value to.
	 * @throws IOException If an I/O error occurs while writing the value to the file.
	 */
	private void writeConfig(String value, File file) throws IOException {
		final Path filePath = file.toPath();

		// if updated contents are not the same as current file contents, update
		if (!value.equals(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8))) {
			Files.write(filePath, value.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Writes the updated configuration to a string using the provided default configuration, current configuration,
	 * comments, and ignored section values.
	 *
	 * @param version               The version used for updating the configuration.
	 * @param defaultConfig         The default configuration.
	 * @param currentConfig         The current configuration.
	 * @param comments              The key-comment pairs obtained from parsing comments.
	 * @param ignoredSectionsValues The ignored section names and their corresponding values.
	 * @return A StringWriter containing the updated configuration as a string.
	 * @throws IOException If an I/O error occurs while writing the configuration.
	 */
	private StringWriter write(int version, final FileConfiguration defaultConfig, final FileConfiguration currentConfig, final KeyCache comments, final Map<String, String> ignoredSectionsValues) throws IOException {
		final FileConfiguration parserConfig = new YamlConfiguration();
		final StringWriter stringWriter = new StringWriter();
		final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

		for (final String fullKey : defaultConfig.getKeys(true)) {
			final String indents = KeyUtils.getIndents(fullKey, SEPARATOR);

			if (ignoredSectionsValues.isEmpty()) {
				writeCommentIfExists(comments, bufferedWriter, fullKey, indents);
			} else {
				writeIgnoredSectionValueIfExists(ignoredSectionsValues, bufferedWriter, fullKey);
			}

			Object currentValue = this.getCurrentValue(fullKey, defaultConfig, currentConfig);
			final String trailingKey = this.getTrailingKey(fullKey);

			if (currentValue instanceof ConfigurationSection) {
				writeConfigurationSection(bufferedWriter, indents, trailingKey, currentValue);
				continue;
			}

			if (currentValue instanceof Integer && fullKey.endsWith("Version")) {
				currentValue = updateVersionIfNecessary(version, currentValue);
			}

			writeYamlValue(parserConfig, bufferedWriter, indents, trailingKey, currentValue);
		}

		writeDanglingComments(comments, bufferedWriter);

		bufferedWriter.close();
		return stringWriter;
	}

	/**
	 * Parses comments from the provided input stream and returns a map of key-comment pairs.
	 * If a key doesn't have any comments, it won't be included in the map.
	 *
	 * @param resource      The input stream containing the YAML configuration.
	 * @param defaultConfig The default configuration to use for key building.
	 * @return A map containing the keys and their associated comments.
	 * @throws IOException If an I/O error occurs while parsing the comments.
	 */
	private KeyCache parseComments(final InputStream resource, final FileConfiguration defaultConfig) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
		final KeyCache keyCache = new KeyCache();
		KeyBuilder keyBuilder = new KeyBuilder(defaultConfig, SEPARATOR);
		String line;
		while ((line = reader.readLine()) != null) {
			final String trimmedLine = line.trim();

			//Only getting comments for keys. A list/array element comment(s) not supported
			if (trimmedLine.startsWith("-")) {
				continue;
			}

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {//Is blank line or is comment
				keyBuilder.addComment(trimmedLine);
			} else {//is a valid yaml key
				keyBuilder.parseLine(trimmedLine);
				final String key = keyBuilder.toString();

				//Remove the last key from keyBuilder if current path isn't a config section or if it is empty to prepare for the next key
				if (!keyBuilder.isConfigSectionOrEmpty()) {
					keyBuilder.removeLastKey();
				}
				//If there is a comment associated with the key it is added to comments map and the commentBuilder is reset
				if (keyBuilder.getComment().length() > 0) {
					keyCache.putConfigKey(key, keyBuilder);
					keyBuilder = new KeyBuilder(defaultConfig, SEPARATOR);
				}
			}
		}
		reader.close();
		if (keyBuilder.getComment().length() > 0)
			keyCache.putConfigKey(null, keyBuilder);
		return keyCache;
	}

	/**
	 * Parses ignored sections from the specified file and returns a map of ignored section names
	 * along with their corresponding values. The ignored sections are determined based on the
	 * provided ignored section list.
	 *
	 * @param toUpdate        The file to parse for ignored sections.
	 * @param currentConfig   The current configuration to use for key building.
	 * @param comments        The key-comment pairs obtained from parsing comments.
	 * @param ignoredSections The list of ignored section names.
	 * @return A map containing the ignored section names and their corresponding values.
	 * @throws IOException If an I/O error occurs while parsing the ignored sections.
	 */
	private Map<String, String> parseIgnoredSections(final File toUpdate, final FileConfiguration currentConfig, final KeyCache comments, final List<String> ignoredSections) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(toUpdate));
		final Map<String, String> ignoredSectionsValues = new LinkedHashMap<>(ignoredSections.size());
		final KeyBuilder keyBuilder = new KeyBuilder(currentConfig, SEPARATOR);
		final StringBuilder valueBuilder = new StringBuilder();

		String currentIgnoredSection = null;
		String line;
		lineLoop:
		while ((line = reader.readLine()) != null) {
			final String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) continue;

			if (trimmedLine.startsWith("-")) {
				for (final String ignoredSection : ignoredSections) {
					final boolean isIgnoredParent = ignoredSection.equals(keyBuilder.toString());

					if (isIgnoredParent || keyBuilder.isSubKeyOf(ignoredSection)) {
						valueBuilder.append("\n").append(line);
						continue lineLoop;
					}
				}
			}

			keyBuilder.parseLine(trimmedLine);
			final String fullKey = keyBuilder.toString();

			// If building the value for an ignored section and this line is no longer a part of the ignored section,
			//  write the valueBuilder, reset it, and set the current ignored section to null
			if (currentIgnoredSection != null && !KeyUtils.isSubKeyOf(currentIgnoredSection, fullKey, SEPARATOR)) {
				ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());
				valueBuilder.setLength(0);
				currentIgnoredSection = null;
			}

			for (final String ignoredSection : ignoredSections) {
				final boolean isIgnoredParent = ignoredSection.equals(fullKey);

				if (isIgnoredParent || keyBuilder.isSubKeyOf(ignoredSection)) {
					if (valueBuilder.length() > 0) valueBuilder.append("\n");

					final KeyBuilder configKey = comments.getConfigKey(fullKey);

					if (configKey != null) {
						String comment = configKey.getComment();
						final String indents = KeyUtils.getIndents(fullKey, SEPARATOR);
						valueBuilder.append(indents).append(comment.replace("\n", "\n" + indents));//Should end with new line (\n)
						valueBuilder.setLength(valueBuilder.length() - indents.length());//Get rid of trailing \n and spaces
					}

					valueBuilder.append(line);

					//Set the current ignored section for future iterations of while loop
					//Don't set currentIgnoredSection to any ignoredSection sub-keys
					if (isIgnoredParent) currentIgnoredSection = fullKey;

					break;
				}
			}
		}
		reader.close();

		if (valueBuilder.length() > 0) ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());

		return ignoredSectionsValues;
	}

	/**
	 * Writes the comment associated with the specified key to the provided writer,
	 * if it exists in the given KeyCache.
	 *
	 * @param comments The KeyCache containing the key-comment mappings.
	 * @param writer   The writer to write the comment to.
	 * @param fullKey  The full key to search for in the KeyCache.
	 * @param indents  The string representation of the indentation level.
	 * @throws IOException If an I/O error occurs while writing the comment.
	 */
	private void writeCommentIfExists(final KeyCache comments, final BufferedWriter writer, final String fullKey, final String indents) throws IOException {
		final KeyBuilder configKey = comments.getConfigKey(fullKey);
		//final String comment = comments.get(fullKey);

		//Comments always end with new line (\n)
		if (configKey != null) {
			String comment = configKey.getComment();
			if (comment == null) return;
			if (comment.startsWith("#") || comment.isEmpty())
				//Replaces all '\n' with '\n' + indents except for the last one
				writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
		}
	}

	/**
	 * Checks if the current configuration needs to be updated based on the specified version.
	 * If version is set to -1, it will update the file if it not match the file
	 * inside the resorcefolder.
	 *
	 * @param currentConfig The current configuration to check.
	 * @param version       The version to compare against.
	 * @return {@code true} if the current configuration needs to be updated, {@code false} otherwise.
	 */
	private boolean checkIfShallUpdate(FileConfiguration currentConfig, int version) {
		int currentVersion = currentConfig.getInt("Version");

		return version > currentVersion || version == -1;
	}

	/**
	 * Writes the value associated with the ignored section to the provided writer,
	 * if it exists in the ignoredSectionsValues map.
	 *
	 * @param ignoredSectionsValues The map containing the ignored section-value mappings.
	 * @param bufferedWriter        The writer to write the value to.
	 * @param fullKey               The full key to search for in the ignoredSectionsValues map.
	 * @throws IOException If an I/O error occurs while writing the value.
	 */
	private void writeIgnoredSectionValueIfExists(final Map<String, String> ignoredSectionsValues, final BufferedWriter bufferedWriter, final String fullKey) throws IOException {
		for (final Map.Entry<String, String> entry : ignoredSectionsValues.entrySet()) {
			if (entry.getKey().equals(fullKey)) {
				bufferedWriter.write(ignoredSectionsValues.get(fullKey) + "\n");
				return;
			} else if (KeyUtils.isSubKeyOf(entry.getKey(), fullKey, SEPARATOR)) {
				return;
			}
		}
	}

	/**
	 * Retrieves the current value for the specified key from either the current configuration or the default configuration.
	 *
	 * @param fullKey       The key to retrieve the value for.
	 * @param defaultConfig The default configuration to fall back on.
	 * @param currentConfig The current configuration to retrieve the value from.
	 * @return The current value associated with the key.
	 */
	private Object getCurrentValue(final String fullKey, final FileConfiguration defaultConfig, final FileConfiguration currentConfig) {
		Object currentValue = currentConfig.get(fullKey);
		if (currentValue == null) {
			currentValue = defaultConfig.get(fullKey);
		}
		return currentValue;
	}

	/**
	 * Writes a configuration section with the provided trailing key and the current value to the provided writer.
	 *
	 * @param bufferedWriter The writer to write the configuration section to.
	 * @param indents        The string representation of the indentation level.
	 * @param trailingKey    The trailing key for the configuration section.
	 * @param currentValue   The current value of the configuration section.
	 * @throws IOException If an I/O error occurs while writing the configuration section.
	 */
	private void writeConfigurationSection(final BufferedWriter bufferedWriter, final String indents, final String trailingKey, final Object currentValue) throws IOException {
		bufferedWriter.write(indents + trailingKey + ":");
		if (!((ConfigurationSection) currentValue).getKeys(false).isEmpty()) {
			bufferedWriter.write("\n");
		} else {
			bufferedWriter.write(" {}\n");
		}
	}

	/**
	 * Updates the version value if necessary.
	 *
	 * @param version      The desired version value.
	 * @param currentValue The current value to update.
	 * @return The updated value.
	 */
	private Object updateVersionIfNecessary(final int version, final Object currentValue) {
		if (version > (Integer) currentValue) {
			return version;
		}
		return currentValue;
	}

	/**
	 * Writes the current value with the provided trailing key to the provided writer.
	 *
	 * @param parserConfig   The parser configuration to use for writing the YAML value.
	 * @param bufferedWriter The writer to write the value to.
	 * @param indents        The string representation of the indentation.
	 * @param trailingKey    The trailing key for the YAML value.
	 * @param currentValue   The current value to write as YAML.
	 * @throws IOException If an I/O error occurs while writing the YAML value.
	 */
	private void writeYamlValue(final FileConfiguration parserConfig, final BufferedWriter bufferedWriter, final String indents, final String trailingKey, final Object currentValue) throws IOException {
		parserConfig.set(trailingKey, currentValue);
		String yaml = parserConfig.saveToString();
		yaml = yaml.substring(0, yaml.length() - 1).replace("\n", "\n" + indents);
		final String toWrite = indents + yaml + "\n";
		parserConfig.set(trailingKey, null);
		bufferedWriter.write(toWrite);
	}

	/**
	 * Writes the dangling comment to the provided writer.
	 *
	 * @param bufferedWriter The writer to write the dangling comment to.
	 * @param comments       The comment or comments to write to the file.
	 * @throws IOException If an I/O error occurs while writing the dangling comment.
	 */
	private void writeDanglingComments(final KeyCache comments, final BufferedWriter bufferedWriter) throws IOException {
		//final String danglingComments = comments.get(null);
		final KeyBuilder configKey = comments.getConfigKey(null);
		//final String comment = comments.get(fullKey);

		//Comments always end with new line (\n)
		if (configKey != null) {
			String danglingComments = configKey.getComment();
			if (danglingComments == null) return;
			if (danglingComments.startsWith("#") || danglingComments.isEmpty())
				bufferedWriter.write(danglingComments);
		}
	}

	private String getTrailingKey(String fullKey) {
		final String[] splitFullKey = fullKey.split("[" + SEPARATOR + "]");
		return splitFullKey[splitFullKey.length - 1];
	}
}
