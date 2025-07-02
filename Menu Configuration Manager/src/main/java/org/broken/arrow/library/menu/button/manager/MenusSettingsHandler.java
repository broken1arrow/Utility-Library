package org.broken.arrow.library.menu.button.manager;

import org.broken.arrow.library.menu.button.manager.utility.MenuButtonData;
import org.broken.arrow.library.menu.button.manager.utility.MenuTemplate;
import org.broken.arrow.library.yaml.YamlFileManager;
import org.broken.arrow.library.yaml.utillity.ConfigurationWrapper;
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
	private final Plugin pluginInstance;
	private final Map<String, MenuTemplate> templates = new HashMap<>();
	private static final String MENUS = "Menus.";

	/**
	 * Creates an instance of MenusSettingsHandler.
	 *
	 * @param plugin     Your main plugin instance.
	 * @param name       The path where you want the file located.
	 *                   For example, you can set the path as "menu/menu.yml" or "menus"
	 *                   if you want to have one menu per file. You don't need to specify the file name here, only the folder path.
	 * @param singleFile Set to true if you plan to have a single file, or false if you want to have one menu per file.
	 */
	public MenusSettingsHandler(final Plugin plugin, final String name, boolean singleFile) {
		super(plugin, name, singleFile, true);
		this.pluginInstance = plugin;
	}

	/**
	 * Retrieves the menu template for the specified menu name.
	 *
	 * @param menuName The name of the menu or if you use one menu for every file
	 *                 it is the filename you use.
	 * @return The MenuTemplate instance for the specified menu name, or null if not found.
	 */
	@Nullable
	public MenuTemplate getTemplate(String menuName) {
		return templates.get(menuName);
	}

	/**
	 * Retrieves the menu button for the specified menu name and slot.
	 *
	 * @param menuName The name of the menu or if you use one menu for every file
	 *                 it is the filename you use.
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
	 * Note: You can't modify this map.
	 *
	 * @return The unmodifiable map of menu templates.
	 */
	@Nonnull
	public Map<String, MenuTemplate> getTemplates() {
		return Collections.unmodifiableMap(templates);
	}

	@Override
	public void loadSettingsFromYaml(final File file, FileConfiguration configuration) {
		ConfigurationSection configurationSection = configuration.getConfigurationSection("Menus");
		MenuTemplate menuTemplate = null;

		if (configurationSection != null) {
			for (final String key : configurationSection.getKeys(false)) {
				final ConfigurationSection menuData = configuration.getConfigurationSection(MENUS + key + ".buttons");

				String menuTitle = configuration.getString(MENUS + key + ".menu_settings.title");
				if (menuTitle == null || menuTitle.isEmpty())
					menuTitle = configuration.getString(MENUS + key + ".menu_settings.name");
				final List<Integer> fillSpace = parseRange(configuration.getString(MENUS + key + ".menu_settings.fill-space"));
				final String sound = configuration.getString(MENUS + key + ".menu_settings.sound");

				final Map<List<Integer>, MenuButtonData> menuButtonMap = getButtons(menuData, key);

				menuTemplate = new MenuTemplate(menuTitle, fillSpace, menuButtonMap, sound);
				if (this.isSingleFile())
					templates.put(key, menuTemplate);
			}
		}
		if (!this.isSingleFile() && menuTemplate != null)
			templates.put(this.getNameOfFile(file.getName()), menuTemplate);
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
					setSlots(slots, subRange, numbers);
				} else slots.add(Integer.parseInt(subRange));
			}
		} catch (final NumberFormatException e) {
			pluginInstance.getLogger().log(Level.WARNING, "Couldn't parse range " + range);
		}
		return slots;
	}

	private void setSlots(final List<Integer> slots, final String subRange, final String[] numbers) {
		if (numbers[0].isEmpty() || numbers[1].isEmpty()) {
			slots.add(Integer.parseInt(subRange));
			return;
		}
		final int first = Integer.parseInt(numbers[0]);
		final int second = Integer.parseInt(numbers[1]);
		slots.addAll(IntStream.range(first, second + 1).boxed().collect(Collectors.toList()));
	}

	private Map<List<Integer>, MenuButtonData> getButtons(ConfigurationSection menuData, String key) {
		final Map<List<Integer>, MenuButtonData> menuButtonMap = new HashMap<>();
		if (menuData == null) return menuButtonMap;

		for (final String menuButtons : menuData.getKeys(false)) {
			final MenuButtonData menuButton = this.getData(MENUS + key + ".buttons." + menuButtons, MenuButtonData.class);
			menuButtonMap.put(parseRange(menuButtons), menuButton);
		}

		return menuButtonMap;
	}

	@Override
	protected void saveDataToFile(final File file, @Nonnull final ConfigurationWrapper configurationWrapper) {
		// Not in use, because no data need to be set back to file.
	}


}
