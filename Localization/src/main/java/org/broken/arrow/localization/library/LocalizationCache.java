package org.broken.arrow.localization.library;

import org.broken.arrow.localization.library.builders.Localization;
import org.broken.arrow.localization.library.builders.PlaceholderText;
import org.broken.arrow.localization.library.builders.PluginMessages;
import org.broken.arrow.yaml.library.YamlFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocalizationCache extends YamlFileManager {

	private Localization localization;

	public LocalizationCache(Plugin plugin, String path) {
		super(plugin, path, true, true);
	}

	public Localization getLocalization() {
		return localization;
	}

	@Override
	protected void saveDataToFile(final File file) {

	}

	@Override
	protected void loadSettingsFromYaml(final File file) {
		final FileConfiguration templateConfig = this.getCustomConfig();

		ConfigurationSection configurationSection = templateConfig.getConfigurationSection("");

		Map<String, Object> map = new HashMap<>();
		if (configurationSection != null)
			for (final String key : configurationSection.getKeys(false)) {
				if (key.equals("Placeholders"))
					map.put(key, this.getData(key, PlaceholderText.class));
				if (key.equals("Messages"))
					map.put(key, this.getData(key, PluginMessages.class));
			}
		Object pluginMessage = map.get("Messages");
		if (pluginMessage instanceof PluginMessages) {
			PluginMessages pluginMessages = (PluginMessages) pluginMessage;
			pluginMessages.setPluginName(templateConfig.getString("Plugin_name"));
			pluginMessages.setPrefixDecor(templateConfig.getString("Prefix_decor"));
			pluginMessages.setSuffixDecor(templateConfig.getString("Suffix_decor"));
		}
		this.localization = Localization.deserialize(map);
	}
}