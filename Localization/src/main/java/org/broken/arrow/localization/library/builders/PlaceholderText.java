package org.broken.arrow.localization.library.builders;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a data structure that holds placeholders and their corresponding values for text manipulation.
 * The `PlaceholderText` class allows you to define and retrieve placeholders for replacing values in strings.
 */
public class PlaceholderText implements ConfigurationSerializable {

	final Map<String, String> placeholders;
	private final PlaceholderText placeholderText;
	private final TimePlaceholders timePlaceholders;

	/**
	 * Constructs a new PlaceholderText object with the specified placeholders and time placeholders.
	 *
	 * @param placeholders     the map of placeholders and their values
	 * @param timePlaceholders the time placeholders associated with this placeholder text, or null if not set
	 */
	public PlaceholderText(@Nonnull Map<String, String> placeholders, @Nullable TimePlaceholders timePlaceholders) {
		this.placeholders = placeholders;
		this.timePlaceholders = timePlaceholders;
		this.placeholderText = this;
	}

	/**
	 * Retrieves the value associated with the specified placeholder key.
	 *
	 * @param key the key of the placeholder
	 * @return the value of the placeholder, or an empty string if not found
	 */
	@Nonnull
	public String getPlaceholder(String key) {
		String message = placeholders.get(key);
		if (message != null) return message;

		return "";
	}

	/**
	 * Retrieves an unmodifiable view of the placeholders map.
	 *
	 * @return an unmodifiable map of placeholders and their values
	 */
	@Nonnull
	public Map<String, String> getPlaceholders() {
		return Collections.unmodifiableMap(placeholders);
	}

	/**
	 * Retrieves the PlaceholderText instance associated with the current object.
	 *
	 * @return the PlaceholderText instance associated with this object
	 */
	public PlaceholderText getPlaceholderText() {
		return placeholderText;
	}

	/**
	 * Retrieves the time placeholders.
	 *
	 * @return the time placeholders, or null if not set
	 */
	public TimePlaceholders getTimePlaceholders() {
		return timePlaceholders;
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>(placeholders);
		if (timePlaceholders != null)
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