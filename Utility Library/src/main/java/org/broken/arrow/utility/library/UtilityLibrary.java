package org.broken.arrow.utility.library;


import org.broken.arrow.library.command.CommandRegister;
import org.broken.arrow.library.command.commandhandler.CommandRegistering;
import org.broken.arrow.library.database.core.databases.MySQL;
import org.broken.arrow.library.database.core.databases.SQLite;
import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.title.update.library.UpdateTitle;
import org.broken.arrow.library.visualization.BlockVisualize;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * UtilityLibrary is a utility class that provides you with access to all modules.
 * It offers various utility methods and functionalities for accessing all modules
 * when you want to use this library as a standalone plugin, without you needing to
 * compile every module into your plugin.
 */
public final class UtilityLibrary extends JavaPlugin {

	private static UtilityLibrary instance;
	private RegisterMenuAPI menuAPI;

	@Override
	public void onLoad() {
		instance = this;
		UpdateTitle.update(null, "");
		this.menuAPI = new RegisterMenuAPI(this);
	}

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Has started API " + getDescription().getName() + " version=" + getDescription().getVersion());
	}

	/**
	 * Retrieves the instance of the UtilityLibrary plugin.
	 *
	 * @return The instance of UtilityLibrary.
	 */
	public static UtilityLibrary getInstance() {
		return instance;
	}

	/**
	 * Get the RegisterMenuAPI instance for the given plugin.
	 *
	 * @return The RegisterMenuAPI instance.
	 */
	public RegisterMenuAPI getMenuApi() {
		return menuAPI;
	}

	/**
	 * Creates a new ItemCreator instance for the given plugin.
	 *
	 * @param plugin The plugin to create the ItemCreator for.
	 * @return The ItemCreator instance.
	 */
	public ItemCreator getItemCreator(Plugin plugin) {
		return new ItemCreator(plugin);
	}

	/**
	 * Creates a new MySQL instance with the given MySQL preferences.
	 *
	 * @param mysqlPreference The MySQL preferences.
	 * @return The MySQL instance.
	 */
	public MySQL createMySQLInstance(ConnectionSettings mysqlPreference) {
		return new MySQL(mysqlPreference);
	}

	/**
	 * Creates a new SQLite instance with the given parent and child paths.
	 *
	 * @param parent The parent path where file is located.
	 * @param child  The child path where file is located.
	 * @return The SQLite instance.
	 */
	public SQLite createSQLiteInstance(String parent, String child) {
		return new SQLite(parent, child);
	}

	/**
	 * Retrieves a new CommandRegistering instance.
	 *
	 * @return The CommandRegistering instance.
	 */
	public CommandRegistering getCommandRegistry() {
		return new CommandRegister();
	}


	/**
	 * Retrieves a new BlockVisualize instance for the given plugin.
	 *
	 * @param plugin The plugin to create the BlockVisualize for.
	 * @return The BlockVisualize instance.
	 */
	public BlockVisualize getVisualizer(Plugin plugin) {
		return new BlockVisualize(plugin);
	}
}
