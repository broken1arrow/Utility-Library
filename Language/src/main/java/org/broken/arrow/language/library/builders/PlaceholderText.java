package org.broken.arrow.language.library.builders;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	public static class Language implements ConfigurationSerializable {
		private final PlaceholderText placeholderText;
		private final PluginMessages pluginMessages;
		private final Builder builder;

		public Language(Builder builder) {
			this.placeholderText = builder.placeholderText;
			this.pluginMessages = builder.pluginMessages;
			this.builder = builder;
		}


		@Nullable
		public PlaceholderText getPlaceholderText() {
			return placeholderText;
		}


		@Nullable
		public PluginMessages getPluginMessages() {
			return pluginMessages;
		}


		public Builder getBuilder() {
			return builder;
		}

		public static class Builder {

			private PlaceholderText placeholderText;
			private PluginMessages pluginMessages;

			public Builder setPlaceholderText(final PlaceholderText placeholderText) {
				this.placeholderText = placeholderText;
				return this;
			}

			public Builder setPluginMessages(final PluginMessages pluginMessages) {
				this.pluginMessages = pluginMessages;
				return this;
			}


			public Language build() {
				return new Language(this);
			}
		}

		@Nonnull
		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("Placeholders", placeholderText);
			map.put("Messages", pluginMessages);
			return map;
		}

		public static Language deserialize(Map<String, Object> map) {

			Object placeholders = map.getOrDefault("Placeholders", null);
			Object messages = map.getOrDefault("Messages", null);
			if (!(placeholders instanceof PlaceholderText))
				placeholders = null;
			if (!(messages instanceof PluginMessages))
				messages = null;

			Builder builder = new Builder()
					.setPlaceholderText((PlaceholderText) placeholders)
					.setPluginMessages((PluginMessages) messages);
			return new Language(builder);
		}

	}
}