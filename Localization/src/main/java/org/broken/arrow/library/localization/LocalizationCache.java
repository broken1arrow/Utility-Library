package org.broken.arrow.library.localization;

import org.broken.arrow.library.localization.builders.Localization;
import org.broken.arrow.library.localization.builders.PlaceholderText;
import org.broken.arrow.library.localization.builders.PluginMessages;
import org.broken.arrow.library.yaml.YamlFileManager;
import org.broken.arrow.library.yaml.utillity.ConfigurationWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the loading and caching of localization data from a YAML file.
 * Extends {@link YamlFileManager} to handle reading localization-related
 * configuration such as placeholders and plugin messages.
 * <p>
 * This cache holds the current {@link Localization} instance and
 * provides access to a {@link MessagesUtility} for message handling.
 */
public class LocalizationCache extends YamlFileManager {

	private Localization localization;
	private final MessagesUtility messagesUtility;

	/**
	 * Creates a new LocalizationCache for the specified plugin and path.
	 *
	 * @param plugin the plugin instance this cache belongs to.
	 * @param path   the path to the YAML localization file.
	 */
	public LocalizationCache(Plugin plugin, String path) {
		super(plugin, path, true, true);
		this.messagesUtility = new MessagesUtility(this, plugin.getName());
	}

	/**
	 * Gets the cached {@link Localization} instance.
	 *
	 * @return the current localization data, or null if not loaded.
	 */
	public Localization getLocalization() {
		return localization;
	}

	/**
	 * Gets the {@link MessagesUtility} instance associated with this cache.
	 *
	 * @return the messages utility instance.
	 */
	public MessagesUtility getMessagesUtility() {
		return messagesUtility;
	}

	/**
	 * This method is overridden to do nothing, as localization data
	 * is only loaded from file, and not saved back.
	 *
	 * @param file                 the file to save to.
	 * @param configurationWrapper the configuration data to save.
	 */
	@Override
	protected void saveDataToFile(final File file, @Nonnull final ConfigurationWrapper configurationWrapper) {
             // we only load data, not set any new data to the file.
	}

	/**
	 * Loads localization data from the given YAML configuration file.
	 * Extracts placeholders and plugin messages, sets plugin metadata,
	 * and updates the cached {@link Localization} instance.
	 *
	 * @param file          the YAML file being loaded.
	 * @param configuration the parsed configuration from the YAML file.
	 */
	@Override
	protected void loadSettingsFromYaml(final File file, FileConfiguration configuration) {
		ConfigurationSection configurationSection = configuration.getConfigurationSection("");

		Map<String, Object> map = new HashMap<>();
		if (configurationSection != null)
			for (final String key : configurationSection.getKeys(false)) {
				if (key.equals("Placeholders"))
					map.put(key, this.getData(key, PlaceholderText.class));
				if (key.equals("MessagesUtility"))
					map.put(key, this.getData(key, PluginMessages.class));
			}
		Object pluginMessage = map.get("MessagesUtility");
		if (pluginMessage instanceof PluginMessages) {
			PluginMessages pluginMessages = (PluginMessages) pluginMessage;
			pluginMessages.setPluginName(configuration.getString("Plugin_name"));
			pluginMessages.setPrefixDecor(configuration.getString("Prefix_decor"));
			pluginMessages.setSuffixDecor(configuration.getString("Suffix_decor"));
		}
		this.localization = Localization.deserialize(map);
	}
}