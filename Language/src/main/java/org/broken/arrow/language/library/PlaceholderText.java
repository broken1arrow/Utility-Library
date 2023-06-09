package org.broken.arrow.language.library;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlaceholderText implements ConfigurationSerializable {

	final Map<String, String> placeholders;
	private final PlaceholderText placeholderText;
	private final TimePlaceholders timePlaceholders;

	private PlaceholderText(Map<String, String> placeholders, TimePlaceholders timePlaceholders) {
		this.placeholders = placeholders;
		this.timePlaceholders = timePlaceholders;
		this.placeholderText = this;
	}

	@Nonnull
	public String getPlaceholder(String key) {
		String message = placeholders.get(key);
		if (message != null) return message;

		return "";
	}

	public Map<String, String> getPlaceholders() {
		return Collections.unmodifiableMap(placeholders);
	}

	public PlaceholderText getPlaceholderText() {
		return placeholderText;
	}

	public TimePlaceholders getTimePlaceholders() {
		return timePlaceholders;
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.putAll(placeholders);
		map.putAll(timePlaceholders.serialize());

		return map;
	}

	public static PlaceholderText deserialize(Map<String, Object> map) {
		final Map<String, String> messages = new HashMap<>();
		for (Entry<String, Object> entry : map.entrySet())
			messages.put(entry.getKey(), entry.getValue() != null ? (String) entry.getValue() : null);
		return new PlaceholderText(messages, TimePlaceholders.deserialize(map));
	}
}