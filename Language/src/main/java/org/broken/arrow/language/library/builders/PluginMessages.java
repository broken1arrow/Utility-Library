package org.broken.arrow.language.library.builders;


import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PluginMessages implements ConfigurationSerializable {

	private final Map<String, List<String>> messages;
	private final PluginMessages pluginMessages;
	private String pluginName;
	private String prefixDecor;
	private String suffixDecor;

	private PluginMessages(Map<String, List<String>> messages) {
		this.messages = messages;
		this.pluginMessages = this;
	}

	@Nonnull
	public List<String> getMessage(String key) {
		List<String> message = messages.get(key);

		if (message != null) return message;
		return new ArrayList<>();
	}


	public Map<String, List<String>> getMessagesCache() {
		return Collections.unmodifiableMap(messages);
	}

	@Nullable
	public PluginMessages getPluginMessages() {
		return pluginMessages;
	}


	@Nullable
	public String getPluginName() {
		return pluginName;
	}


	@Nullable
	public String getPrefixDecor() {
		return prefixDecor;
	}


	@Nullable
	public String getSuffixDecor() {
		return suffixDecor;
	}

	public void setPrefixDecor(final String prefixDecor) {
		this.prefixDecor = prefixDecor;
	}

	public void setSuffixDecor(final String suffixDecor) {
		this.suffixDecor = suffixDecor;
	}

	public void setPluginName(final String pluginName) {
		this.pluginName = pluginName;
	}

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

	public static PluginMessages deserialize(Map<String, Object> map) {
		final Map<String, List<String>> messages = new HashMap<>();
		for (Entry<String, Object> entry : map.entrySet())
			messages.put(entry.getKey(), convertToList(entry.getValue() != null ? entry.getValue() : null));
		return new PluginMessages(messages);
	}

	public static List<String> convertToList(Object object) {
		if (object instanceof String) {
			return Collections.singletonList((String) object);
		}
		if (object instanceof List) {
			return (List<String>) object;
		}
		return null;
	}

}