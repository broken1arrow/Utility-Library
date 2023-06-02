package org.broken.arrow.library.utility;

import org.broken.arrow.yaml.library.SimpleYamlHelper;
import org.bukkit.plugin.Plugin;

public abstract class SimpleYaml extends SimpleYamlHelper {

	public SimpleYaml(final Plugin plugin, final String name) {
		super(plugin, name);
	}

	public SimpleYaml(final Plugin plugin, final String name, final boolean singleFile, final boolean shallGenerateFiles) {
		super(plugin, name, singleFile, shallGenerateFiles);
	}
}
