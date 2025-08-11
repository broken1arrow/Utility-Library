package org.broken.arrow.library.localization.builders;

import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents localization data for the plugin, including placeholder texts
 * and plugin messages. Supports serialization and deserialization
 * to allow saving and loading from configuration formats.
 */
public class Localization implements ConfigurationSerializable {
    private final PlaceholderText placeholderText;
    private final PluginMessages pluginMessages;
    private final Builder builder;

    private Localization(Builder builder) {
        this.placeholderText = builder.placeholderText;
        this.pluginMessages = builder.pluginMessages;
        this.builder = builder;
    }

    /**
     * Retrieves the placeholder text handler.
     *
     * @return the PlaceholderText instance or null if not set.
     */
    @Nullable
    public PlaceholderText getPlaceholderText() {
        return placeholderText;
    }

    /**
     * Retrieves the plugin messages handler.
     *
     * @return the PluginMessages instance or null if not set.
     */
    @Nullable
    public PluginMessages getPluginMessages() {
        return pluginMessages;
    }

    /**
     * Retrieves the builder used to create this Localization instance.
     *
     * @return the Builder instance.
     */
    public Builder getBuilder() {
        return builder;
    }

    /**
     * Builder class to construct immutable {@link Localization} instances.
     * <p>
     * Use this builder to set the placeholder texts and plugin messages
     * before creating a {@link Localization} object.
     */
    public static class Builder {

        private PlaceholderText placeholderText;
        private PluginMessages pluginMessages;

        /**
         * Sets the placeholder text instance for localization.
         *
         * @param placeholderText the placeholder text instance to set
         * @return this builder instance for method chaining
         */
        public Builder setPlaceholderText(final PlaceholderText placeholderText) {
            this.placeholderText = placeholderText;
            return this;
        }

        /**
         * Sets the plugin messages instance for localization.
         *
         * @param pluginMessages the plugin messages instance to set
         * @return this builder instance for method chaining
         */
        public Builder setPluginMessages(final PluginMessages pluginMessages) {
            this.pluginMessages = pluginMessages;
            return this;
        }

        /**
         * Builds the {@link Localization} instance with the current builder data.
         *
         * @return a new {@link Localization} instance configured by this builder
         */
        public Localization build() {
            return new Localization(this);
        }
    }

    /**
     * Serializes this Localization instance into a Map for configuration storage.
     *
     * @return a Map representation of this Localization.
     */
    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Placeholders", placeholderText);
        map.put("MessagesUtility", pluginMessages);
        return map;
    }

    /**
     * Deserializes a Localization instance from the provided Map.
     *
     * @param map the Map containing serialized localization data.
     * @return a new Localization instance built from the Map.
     */
    public static Localization deserialize(Map<String, Object> map) {

        Object placeholders = map.getOrDefault("Placeholders", null);
        Object messages = map.getOrDefault("MessagesUtility", null);
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
