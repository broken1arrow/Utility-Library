package org.broken.arrow.library.command.commandhandler;

import org.broken.arrow.library.command.builers.CommandBuilder;
import org.broken.arrow.library.command.command.CommandProperty;
import org.bukkit.command.CommandException;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A utility interface for registering and managing commands.
 * The {@code CommandRegistering} interface provides methods for registering subcommands,
 * setting command label messages and permissions, retrieving command builders, and
 * registering the main command.
 */
public interface CommandRegistering {

	/**
	 * Registers a new command entry point for this plugin.
	 *
	 * <p>This is the primary entry method for creating and configuring a command.
	 * It initializes the internal command handler and returns a {@link CommandBuilder}
	 * used to define aliases, subcommands, execution behavior, and display settings.</p>
	 *
	 * <p>The plugin name is used as a fallback namespace for command registration.</p>
	 *
	 * @param plugin      the owning plugin instance (used for namespace and registration context)
	 * @param mainCommand the root command label (e.g. "plugin" in "/plugin menu")
	 * @return a {@link CommandBuilder} used to configure the command structure
	 */
	CommandBuilder registerCommand(Plugin plugin, String mainCommand);

	/**
	 * Registers a new command entry point for this plugin.
	 *
	 * <p>This is the primary entry method for creating and configuring a command.
	 * It initializes the internal command handler and returns a {@link CommandBuilder}
	 * used to define aliases, subcommands, execution behavior, and display settings.</p>
	 *
	 * <p>The plugin name is used as a fallback namespace for command registration.</p>
	 *
	 * @param plugin      the owning plugin instance (used for namespace and registration context)
	 * @param mainCommand the root command label (e.g. "plugin" in "/plugin menu")
	 * @param callback    The builder to set the command.
	 */
	void registerCommand(@Nonnull Plugin plugin, @Nonnull String mainCommand, @Nonnull Consumer<CommandBuilder> callback);

	/**
	 * Returns the main command set.
	 *
	 * @param command Your main command label that you register your command with.
	 * @return the returns the settings set for the main command.
	 */
	MainCommandHandler getCommand(@Nonnull String command);
}
