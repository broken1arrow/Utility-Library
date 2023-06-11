package org.broken.arrow.localization.library.builders;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Localization implements ConfigurationSerializable {
	private final PlaceholderText placeholderText;
	private final PluginMessages pluginMessages;
	private final Builder builder;

	private Localization(Builder builder) {
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


		public Localization build() {
			return new Localization(this);
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

	public static Localization deserialize(Map<String, Object> map) {

		Object placeholders = map.getOrDefault("Placeholders", null);
		Object messages = map.getOrDefault("Messages", null);
		if (!(placeholders instanceof PlaceholderText))
			placeholders = null;
		if (!(messages instanceof PluginMessages))
			messages = null;

		Builder builder = new Builder()
				.setPlaceholderText((PlaceholderText) placeholders)
				.setPluginMessages((PluginMessages) messages);
		return builder.build();
	}

}
