package org.broken.arrow.menu.button.manager.library;

import org.broken.arrow.itemcreator.library.ItemCreator;
import org.broken.arrow.menu.button.manager.library.utility.MenuButtonData;
import org.broken.arrow.menu.button.manager.library.utility.MenuTemplate;
import org.broken.arrow.yaml.library.YamlFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents a cache for menus, providing methods for retrieving menu templates, menu buttons, and other menu-related data.
 * It extends the YamlFileManager class for handling YAML file operations.
 */
public class MenusSettingsHandler extends YamlFileManager {
	private final Plugin plugin;
	private final int version = 1;
	private final ItemCreator itemCreator;
	private final Map<String, MenuTemplate> templates = new HashMap<>();

	/**
	 * Creates an instance of MenusSettingsHandler.
	 *
	 * @param plugin     Your main plugin instance.
	 * @param name       The path where you want the file located.
	 *                   For example, you can set the path as "menu/menu.yml" or "menus"
	 *                   if you want to have one menu per file. You don't need to specify the plugin folder.
	 * @param singleFile Set to true if you plan to have a single file, or false if you want to have one menu per file.
	 */
	public MenusSettingsHandler(final Plugin plugin, final String name, boolean singleFile) {
		super(plugin, name, singleFile, true);
		itemCreator = new ItemCreator(plugin);
		this.plugin = plugin;
	}

	/**
	 * Get the ItemCreator instance associated with this MenusSettingsHandler.
	 *
	 * @return The ItemCkreator instance.
	 */
	public ItemCreator getItemCreator() {
		return itemCreator;
	}

	/**
	 * Retrieves the menu template for the specified menu name.
	 *
	 * @param menuName The name of the menu.
	 * @return The MenuTemplate instance for the specified menu name, or null if not found.
	 */
	@Nullable
	public MenuTemplate getTemplate(String menuName) {
		return templates.get(menuName);
	}

	/**
	 * Retrieves the menu button for the specified menu name and slot.
	 *
	 * @param menuName The name of the menu.
	 * @param slot     The slot where the menu button is placed.
	 * @return The MenuButtonData instance for the specified menu name and slot, or null if not found.
	 */
	@Nullable
	public MenuButtonData getMenuButton(String menuName, int slot) {
		MenuTemplate menuTemplate = this.getTemplate(menuName);
		if (menuTemplate != null)
			return menuTemplate.getMenuButton(slot);
		return null;
	}


	/**
	 * Retrieves an unmodifiable map of all menu templates.
	 * Note: You cannot modify this map.
	 *
	 * @return The unmodifiable map of menu templates.
	 */
	@Nonnull
	public Map<String, MenuTemplate> getTemplates() {
		return Collections.unmodifiableMap(templates);
	}

	@Override
	public void loadSettingsFromYaml(final File file, FileConfiguration configuration) {
		final FileConfiguration fileConfiguration = configuration;
		ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("Menus");
		if (configurationSection != null)
			for (final String key : configurationSection.getKeys(false)) {
				final ConfigurationSection menuData = fileConfiguration.getConfigurationSection("Menus." + key + ".buttons");
				final Map<List<Integer>, MenuButtonData> menuButtonMap = new HashMap<>();

				final String menuSettings = fileConfiguration.getString("Menus." + key + ".menu_settings.name");
				final List<Integer> fillSpace = parseRange(fileConfiguration.getString("Menus." + key + ".menu_settings.fill-space"));
				final String sound = fileConfiguration.getString("Menus." + key + ".menu_settings.sound");
				if (menuData != null) {
					for (final String menuButtons : menuData.getKeys(false)) {
						final MenuButtonData menuButton = this.getData("Menus." + key + ".buttons." + menuButtons, MenuButtonData.class);
						menuButtonMap.put(parseRange(menuButtons), menuButton);
					}
				}
				final MenuTemplate menuTemplate = new MenuTemplate(menuSettings, fillSpace, menuButtonMap, sound);

				templates.put(key, menuTemplate);
			}
	}

	@Nonnull
	private List<Integer> parseRange(final String range) {
		final List<Integer> slots = new ArrayList<>();

		//Allow empty ranges.
		if (range == null || range.equals("")) return slots;

		try {
			for (final String subRange : range.split(",")) {
				if (Objects.equals(subRange, "")) continue;
				if (subRange.contains("-")) {
					final String[] numbers = subRange.split("-");
					if (numbers[0].isEmpty() || numbers[1].isEmpty()) {
						slots.add(Integer.parseInt(subRange));
						continue;
					}
					final int first = Integer.parseInt(numbers[0]);
					final int second = Integer.parseInt(numbers[1]);
					slots.addAll(IntStream.range(first, second + 1).boxed().collect(Collectors.toList()));
				} else slots.add(Integer.parseInt(subRange));
			}
		} catch (final NumberFormatException e) {
			plugin.getLogger().log(Level.WARNING, "Couldn't parse range " + range);
		}
		return slots;
	}

	@Override
	protected void saveDataToFile(final File file) {

	}


}
