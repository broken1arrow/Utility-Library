package org.broken.arrow.library.localization.builders;


import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a data structure that holds plugin messages, such as strings or lists of strings,
 * associated with specific keys.
 */
public class PluginMessages implements ConfigurationSerializable {

	private final Map<String, List<String>> messages;
	private final PluginMessages instance;
	private String pluginName;
	private String prefixDecor;
	private String suffixDecor;

	/**
	 * Constructs a new PluginMessages object with the specified messages.
	 *
	 * @param messages the map of messages associated with keys
	 */
	private PluginMessages(Map<String, List<String>> messages) {
		this.messages = messages;
		this.instance = this;
	}

	/**
	 * Retrieves the message associated with the specified key.
	 *
	 * @param key the key associated with the desired message
	 * @return the list of strings representing the message, or an empty list if not found
	 */
	@Nonnull
	public List<String> getMessage(String key) {
		List<String> message = messages.get(key);

		if (message != null) return message;
		return new ArrayList<>();
	}

	/**
	 * Retrieves an unmodifiable view of the messages cache.
	 *
	 * @return an unmodifiable map of messages
	 */
	public Map<String, List<String>> getMessagesCache() {
		return Collections.unmodifiableMap(messages);
	}

	/**
	 * Retrieves the PluginMessages instance associated with the current object.
	 *
	 * @return the PluginMessages instance associated with this object.
	 */
	@Nonnull
	public PluginMessages getInstance() {
		return instance;
	}

	/**
	 * Retrieves the plugin name.
	 *
	 * @return the plugin name, or null if not set.
	 */
	@Nullable
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Retrieves the prefix decoration.
	 *
	 * @return the prefix decoration, or null if not set.
	 */
	@Nullable
	public String getPrefixDecor() {
		return prefixDecor;
	}

	/**
	 * Retrieves the suffix decoration.
	 *
	 * @return the suffix decoration, or null if not set.
	 */
	@Nullable
	public String getSuffixDecor() {
		return suffixDecor;
	}

	/**
	 * Sets the prefix decoration.
	 *
	 * @param prefixDecor the prefix decoration to set
	 */
	public void setPrefixDecor(final String prefixDecor) {
		this.prefixDecor = prefixDecor;
	}

	/**
	 * Sets the suffix decoration.
	 *
	 * @param suffixDecor the suffix decoration to set
	 */
	public void setSuffixDecor(final String suffixDecor) {
		this.suffixDecor = suffixDecor;
	}

	/**
	 * Sets the plugin name.
	 *
	 * @param pluginName the plugin name to set
	 */
	public void setPluginName(final String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * Serializes the PluginMessages object to a map of key-value pairs.
	 *
	 * @return a map containing the serialized data
	 */
	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("Plugin_name", pluginName);
		map.put("Prefix_decor", prefixDecor);
		map.put("Suffix_decor", suffixDecor);
		map.putAll(messages);
		return map;
	}

	/**
	 * Deserializes a PluginMessages object from a map of key-value pairs.
	 *
	 * @param map the map containing the serialized data
	 * @return the deserialized PluginMessages object
	 */
	public static PluginMessages deserialize(Map<String, Object> map) {
		final Map<String, List<String>> messages = new HashMap<>();
		for (Entry<String, Object> entry : map.entrySet())
			messages.put(entry.getKey(), convertToList(entry.getValue() != null ? entry.getValue() : null));
		return new PluginMessages(messages);
	}

	/**
	 * Check if the object is a list or a string value.
	 *
	 * @param object the object to check the type of value.
	 * @return a list with one or several messages.
	 */
	public static List<String> convertToList(Object object) {
		if (object instanceof String) {
			return Collections.singletonList((String) object);
		}
		if (object instanceof List) {
			return (List<String>) object;
		}
		return new ArrayList<>();
	}

}