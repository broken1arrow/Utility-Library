package org.broken.arrow.language.library;

import org.broken.arrow.language.library.builders.PlaceholderText;
import org.broken.arrow.language.library.builders.PlaceholderText.Language;
import org.broken.arrow.language.library.builders.PluginMessages;
import org.broken.arrow.yaml.library.SimpleYamlHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageCache extends SimpleYamlHelper {

	//private final Map<String, Language> language = new HashMap<>();
	private Language language;

	public LanguageCache(Plugin plugin, String path) {
		super(plugin, path, true, true);
	}

	public Language getLanguage() {
		return language;
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
		this.language = PlaceholderText.Language.deserialize(map);
	}
}