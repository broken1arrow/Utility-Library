package org.broken.arrow.yaml.library.utillity;


import org.broken.arrow.yaml.library.SimpleYamlHelper.Valid;
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
 * A library for update your config and keep comments.
 *
 * @author tchristofferson on 2/8/2019.
 * Note broken_arrow modify this code 11/24/2022
 */
public class ConfigUpdater {

	//Used for separating keys in the keyBuilder inside parseComments method
	private static final char SEPARATOR = '.';
	private final Plugin plugin;
	private final List<String> ignoredSections;

	public ConfigUpdater(Plugin plugin, String... ignoredSections) {
		this.plugin = plugin;
		this.ignoredSections = Arrays.asList(ignoredSections);
	}

	public void update(int version, final String resourceName, final File toUpdate) throws IOException {
		Valid.checkBoolean(toUpdate.exists(), "The toUpdate file doesn't exist!");

		final InputStream resource = this.plugin.getResource(resourceName);
		Valid.checkNotNull(resource, "the file " + resourceName + " not exist in plugin jar.");
		final String updatedConfig = updateConfig(version, resourceName, toUpdate);
		writeConfig(updatedConfig, toUpdate);
	}

	private String updateConfig(int version, final String resourceName, final File toUpdate) throws IOException {
		final InputStream resource = this.plugin.getResource(resourceName);
		Valid.checkNotNull(resource, "the file " + resourceName + " not exist in plugin jar.");

		final FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
		final FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
		final Map<String, String> comments = parseComments(plugin.getResource(resourceName), defaultConfig);
		final Map<String, String> ignoredSectionsValues = parseIgnoredSections(toUpdate, currentConfig, comments, ignoredSections == null ? Collections.emptyList() : ignoredSections);
		// will write updated config file "contents" to a string
		final StringWriter writer = new StringWriter();
		this.write(version, defaultConfig, currentConfig, new BufferedWriter(writer), comments, ignoredSectionsValues);
		//final FileConfiguration updatedConfig = YamlConfiguration.loadConfiguration(new StringReader(value));
		return writer.toString();
	}

	private void writeConfig(String value, File file) throws IOException {
		final Path filePath = file.toPath();

		// if updated contents are not the same as current file contents, update
		if (!value.equals(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8))) {
			Files.write(filePath, value.getBytes(StandardCharsets.UTF_8));
		}
	}

	private void write(int version, final FileConfiguration defaultConfig, final FileConfiguration currentConfig, final BufferedWriter writer, final Map<String, String> comments, final Map<String, String> ignoredSectionsValues) throws IOException {
		//Used for converting objects to yaml, then cleared
		final FileConfiguration parserConfig = new YamlConfiguration();

		keyLoop:
		for (final String fullKey : defaultConfig.getKeys(true)) {
			final String indents = KeyBuilder.getIndents(fullKey, SEPARATOR);

			if (ignoredSectionsValues.isEmpty()) {
				writeCommentIfExists(comments, writer, fullKey, indents);
			} else {
				for (final Map.Entry<String, String> entry : ignoredSectionsValues.entrySet()) {
					if (entry.getKey().equals(fullKey)) {
						writer.write(ignoredSectionsValues.get(fullKey) + "\n");
						continue keyLoop;
					} else if (KeyBuilder.isSubKeyOf(entry.getKey(), fullKey, SEPARATOR)) {
						continue keyLoop;
					}
				}

				writeCommentIfExists(comments, writer, fullKey, indents);
			}

			Object currentValue = currentConfig.get(fullKey);

			if (currentValue == null)
				currentValue = defaultConfig.get(fullKey);

			final String[] splitFullKey = fullKey.split("[" + SEPARATOR + "]");
			final String trailingKey = splitFullKey[splitFullKey.length - 1];

			if (currentValue instanceof ConfigurationSection) {
				writer.write(indents + trailingKey + ":");

				if (!((ConfigurationSection) currentValue).getKeys(false).isEmpty())
					writer.write("\n");
				else
					writer.write(" {}\n");

				continue;
			}
			if (version > 0 && trailingKey.equals("Version")) {
				currentValue = version;
			}
			parserConfig.set(trailingKey, currentValue);
			String yaml = parserConfig.saveToString();
			yaml = yaml.substring(0, yaml.length() - 1).replace("\n", "\n" + indents);
			final String toWrite = indents + yaml + "\n";
			parserConfig.set(trailingKey, null);
			writer.write(toWrite);
		}

		final String danglingComments = comments.get(null);

		if (danglingComments != null)
			writer.write(danglingComments);

		writer.close();
	}

	//Returns a map of key comment pairs. If a key doesn't have any comments it won't be included in the map.
	private Map<String, String> parseComments(final InputStream resource, final FileConfiguration defaultConfig) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
		final Map<String, String> comments = new LinkedHashMap<>();
		final StringBuilder commentBuilder = new StringBuilder();
		final KeyBuilder keyBuilder = new KeyBuilder(defaultConfig, SEPARATOR);

		String line;
		while ((line = reader.readLine()) != null) {
			final String trimmedLine = line.trim();

			//Only getting comments for keys. A list/array element comment(s) not supported
			if (trimmedLine.startsWith("-")) {
				continue;
			}

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {//Is blank line or is comment
				commentBuilder.append(trimmedLine).append("\n");
			} else {//is a valid yaml key
				keyBuilder.parseLine(trimmedLine);
				final String key = keyBuilder.toString();

				//If there is a comment associated with the key it is added to comments map and the commentBuilder is reset
				if (commentBuilder.length() > 0) {
					comments.put(key, commentBuilder.toString());
					commentBuilder.setLength(0);
				}

				//Remove the last key from keyBuilder if current path isn't a config section or if it is empty to prepare for the next key
				if (!keyBuilder.isConfigSectionWithKeys()) {
					keyBuilder.removeLastKey();
				}
			}
		}

		reader.close();

		if (commentBuilder.length() > 0)
			comments.put(null, commentBuilder.toString());

		return comments;
	}

	private Map<String, String> parseIgnoredSections(final File toUpdate, final FileConfiguration currentConfig, final Map<String, String> comments, final List<String> ignoredSections) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(toUpdate));
		final Map<String, String> ignoredSectionsValues = new LinkedHashMap<>(ignoredSections.size());
		final KeyBuilder keyBuilder = new KeyBuilder(currentConfig, SEPARATOR);
		final StringBuilder valueBuilder = new StringBuilder();

		String currentIgnoredSection = null;
		String line;
		lineLoop:
		while ((line = reader.readLine()) != null) {
			final String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#"))
				continue;

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

			//If building the value for an ignored section and this line is no longer a part of the ignored section,
			//  write the valueBuilder, reset it, and set the current ignored section to null
			if (currentIgnoredSection != null && !KeyBuilder.isSubKeyOf(currentIgnoredSection, fullKey, SEPARATOR)) {
				ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());
				valueBuilder.setLength(0);
				currentIgnoredSection = null;
			}

			for (final String ignoredSection : ignoredSections) {
				final boolean isIgnoredParent = ignoredSection.equals(fullKey);

				if (isIgnoredParent || keyBuilder.isSubKeyOf(ignoredSection)) {
					if (valueBuilder.length() > 0)
						valueBuilder.append("\n");

					final String comment = comments.get(fullKey);

					if (comment != null) {
						final String indents = KeyBuilder.getIndents(fullKey, SEPARATOR);
						valueBuilder.append(indents).append(comment.replace("\n", "\n" + indents));//Should end with new line (\n)
						valueBuilder.setLength(valueBuilder.length() - indents.length());//Get rid of trailing \n and spaces
					}

					valueBuilder.append(line);

					//Set the current ignored section for future iterations of while loop
					//Don't set currentIgnoredSection to any ignoredSection sub-keys
					if (isIgnoredParent)
						currentIgnoredSection = fullKey;

					break;
				}
			}
		}

		reader.close();

		if (valueBuilder.length() > 0)
			ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());

		return ignoredSectionsValues;
	}

	private void writeCommentIfExists(final Map<String, String> comments, final BufferedWriter writer, final String fullKey, final String indents) throws IOException {
		final String comment = comments.get(fullKey);

		//Comments always end with new line (\n)
		if (comment != null)
			//Replaces all '\n' with '\n' + indents except for the last one
			writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
	}

	//Input: 'key1.key2' Result: 'key1'
	private void removeLastKey(final StringBuilder keyBuilder) {
		if (keyBuilder.length() == 0)
			return;

		final String keyString = keyBuilder.toString();
		//Must be enclosed in brackets in case a regex special character is the separator
		final String[] split = keyString.split("[" + SEPARATOR + "]");
		//Makes sure begin index isn't < 0 (error). Occurs when there is only one key in the path
		final int minIndex = Math.max(0, keyBuilder.length() - split[split.length - 1].length() - 1);
		keyBuilder.replace(minIndex, keyBuilder.length(), "");
	}

	private static void appendNewLine(final StringBuilder builder) {
		if (builder.length() > 0)
			builder.append("\n");
	}

}
