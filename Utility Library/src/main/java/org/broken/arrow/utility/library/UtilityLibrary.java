package org.broken.arrow.utility.library;


import org.broken.arrow.command.library.CommandRegister;
import org.broken.arrow.command.library.command.CommandHolder;
import org.broken.arrow.command.library.command.builders.CommandBuilder.Builder;
import org.broken.arrow.command.library.commandhandler.CommandRegistering;
import org.broken.arrow.convert.library.SerializeData;
import org.broken.arrow.database.library.MySQL;
import org.broken.arrow.database.library.SQLite;
import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.itemcreator.library.ItemCreator;
import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.visualization.library.BlockVisualize;
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

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Has started API " + getDescription().getName() + " version=" + getDescription().getVersion());
	}

	@Override
	public void onDisable() {
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
	 * Creates a new RegisterMenuAPI instance for the given plugin.
	 *
	 * @param plugin The plugin to create the RegisterMenuAPI for.
	 * @return The RegisterMenuAPI instance.
	 */
	public RegisterMenuAPI getMenuApi(Plugin plugin) {
		return new RegisterMenuAPI(plugin);
	}

	/**
	 * Creates a new RegisterMenuAPI instance for the given plugin, with an option to turn off logging.
	 *
	 * @param plugin        The plugin to create the RegisterMenuAPI for.
	 * @param turnOffLogger True to turn off logging, true otherwise.
	 * @return The RegisterMenuAPI instance.
	 */
	public RegisterMenuAPI getMenuApi(Plugin plugin, boolean turnOffLogger) {
		return new RegisterMenuAPI(plugin, turnOffLogger);
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
	public MySQL createMySQLInstance(MysqlPreferences mysqlPreference) {
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
	 * Retrieves a new SerializeData instance for the given plugin.
	 *
	 * @param plugin The plugin to retrieve the SerializeData for.
	 * @return The SerializeData instance.
	 */
	public SerializeData getSerializeMethods(Plugin plugin) {
		final String[] version = plugin.getServer().getBukkitVersion().split("\\.");
		float ver = Float.parseFloat(version[1] + "." + version[2].substring(0, version[2].lastIndexOf("-")));
		return new SerializeData(ver);
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
	 * Retrieves a new Builder instance for the given CommandHolder.
	 *
	 * @param executor The CommandHolder executor.
	 * @return The Builder instance.
	 */
	public Builder getCommandBuilder(CommandHolder executor) {
		return new Builder(executor);
	}

	/**
	 * Retrieves a new BlockVisualize instance for the given plugin.
	 *
	 * @param plugin The plugin to create the BlockVisualize for.
	 * @return The BlockVisualize instance.
	 */
	public BlockVisualize getVisualizer(Plugin plugin) {
		final String[] versionPieces = plugin.getServer().getBukkitVersion().split("\\.");
		float ver = Float.parseFloat(versionPieces[1] + "." + versionPieces[2].substring(0, versionPieces[2].lastIndexOf("-")));
		return new BlockVisualize(plugin, ver);
	}
}
