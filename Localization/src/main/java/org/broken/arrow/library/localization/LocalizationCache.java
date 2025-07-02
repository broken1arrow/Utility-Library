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

public class LocalizationCache extends YamlFileManager {

	private Localization localization;
	private final MessagesUtility messagesUtility;

	public LocalizationCache(Plugin plugin, String path) {
		super(plugin, path, true, true);
		this.messagesUtility = new MessagesUtility(this, plugin.getName());
	}

	public Localization getLocalization() {
		return localization;
	}

	public MessagesUtility getMessagesUtility() {
		return messagesUtility;
	}

	@Override
	protected void saveDataToFile(final File file, @Nonnull final ConfigurationWrapper configurationWrapper) {
             // we only load data, not set any new data to the file.
	}

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