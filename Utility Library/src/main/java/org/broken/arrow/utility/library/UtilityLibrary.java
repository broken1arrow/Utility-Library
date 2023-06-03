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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

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

	public static UtilityLibrary getInstance() {
		return instance;
	}

	public RegisterMenuAPI getMenuApi(Plugin plugin) {
		return new RegisterMenuAPI(plugin);
	}

	public ItemCreator getItemCreator(Plugin plugin) {
		return new ItemCreator(plugin);
	}

	public MySQL createMySQLInstance(MysqlPreferences mysqlPreference) {
		return new MySQL(mysqlPreference);
	}

	public SQLite createSQLiteInstance(String filePath) {
		return new SQLite(filePath);
	}

	public SerializeData getSerializeMethods(Plugin plugin) {
		final String[] version = plugin.getServer().getBukkitVersion().split("\\.");
		float ver = Float.parseFloat(version[1] + "." + version[2].substring(0, version[2].lastIndexOf("-")));
		return new SerializeData(ver);
	}

	public CommandRegistering getCommandRegistry() {
		return new CommandRegister();
	}

	public Builder getCommandBuilder(CommandHolder executor) {
		return new Builder(executor);
	}
}
