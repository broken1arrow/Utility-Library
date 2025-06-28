package org.broken.arrow.command.library;


import org.broken.arrow.command.library.command.CommandProperty;
import org.broken.arrow.command.library.commandhandler.CommandExecutor;
import org.broken.arrow.command.library.commandhandler.CommandRegistering;
import org.broken.arrow.logging.library.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class for registering and managing registered commands.The commandRegister provides
 * methods for registering subcommands, setting command label messages and permissions,
 * retrieving sub-commands, and registering the main command label.
 */
public class CommandRegister implements CommandRegistering {
    private Logging log = new Logging(CommandRegister.class);

	private final List<CommandProperty> commands = Collections.synchronizedList(new ArrayList<>());
	private String commandLabelMessage;
	private String commandLabelMessageNoPerms;
	private String commandLabelPermission;
	private List<String> prefixMessage;
	private List<String> suffixMessage;
	private boolean registeredMainCommand;
	private List<String> descriptions;

	@Override
	public CommandRegistering registerSubCommand(final CommandProperty subCommand) {
		Set<String> commandLabels = subCommand.getCommandLabels();

        if (addCommands(subCommand, commandLabels)) {
			return this;
		}

		commands.removeIf(oldCommandBuilder -> oldCommandBuilder.equals(subCommand));
		commands.removeIf(oldCommandBuilder -> oldCommandBuilder.getCommandLabels().equals(subCommand.getCommandLabels()));
		commands.add(subCommand);
		commands.sort(Comparator.comparing(CommandProperty::getFirstSortedLabel, Comparator.nullsLast(String::compareTo)));
		return this;
    }

    @Override
    public CommandRegistering registerSubCommands(final CommandProperty... subCommands) {
        if (subCommands == null)
            return this;
        for (CommandProperty registerSubCommand : subCommands) {
            this.registerSubCommand(registerSubCommand);
        }
        return this;
    }

	/**
	 * Returns the message to display as the command label.
	 *
	 * @return The command label message.
	 */
	@Override
	public String getCommandLabelMessage() {
		return commandLabelMessage;
	}

	/**
	 * Sets the message to display as the command label.
	 * Use {label} to replace it with the command name.
	 *
	 * @param commandLabelMessage The command label message to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegistering setCommandLabelMessage(String commandLabelMessage) {
		this.commandLabelMessage = commandLabelMessage;
		return this;
	}

	/**
	 * Returns the list of prefix messages to display in the command help.
	 *
	 * @return The list of prefix messages.
	 */
	@Override
	public List<String> getPrefixMessage() {
		return prefixMessage;
	}

	/**
	 * Sets the prefix messages to display in the command help using the provided string values.
	 *
	 * @param prefixMessage The prefix messages to set.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering setPrefixMessage(String... prefixMessage) {
		this.prefixMessage = Arrays.asList(prefixMessage);
		return this;
	}

	/**
	 * Sets the prefix messages to display in the command help using the provided list of strings.
	 *
	 * @param prefixMessage The prefix messages to set.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering setPrefixMessage(List<String> prefixMessage) {
		this.prefixMessage = prefixMessage;
		return this;
	}

	/**
	 * Returns the list of suffix messages to display in the command help.
	 *
	 * @return The list of suffix messages.
	 */
	@Override
	public List<String> getSuffixMessage() {
		return suffixMessage;
	}

	/**
	 * Sets the suffix messages to display in the command help using the provided string values.
	 *
	 * @param suffixMessage The suffix messages to set.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering setSuffixMessage(String... suffixMessage) {
		this.suffixMessage = Arrays.asList(suffixMessage);
		return this;
	}

	/**
	 * Sets the suffix messages to display in the command using the provided list of strings.
	 *
	 * @param suffixMessage The suffix messages to set.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering setSuffixMessage(List<String> suffixMessage) {
		this.suffixMessage = suffixMessage;
		return this;
	}

	/**
	 * Returns the description of the command. The description could provide information about the main command
	 * and/or brief explanation to the subcommands. Player then add a "?" or "help" at the end of the command to
	 * request additional information about the command.
	 *
	 * @return The description.
	 */
	public List<String> getDescriptions() {
		return descriptions;
	}

	@Override
	public CommandRegistering setDescriptions(final String... descriptions) {
		this.descriptions = Arrays.asList(descriptions);
		return this;
	}

	/**
	 * Get the message if player not have the permission.
	 *
	 * @return the message or null.
	 */
	@Override
	public String getCommandLabelMessageNoPerms() {
		return commandLabelMessageNoPerms;
	}

	/**
	 * Use {label} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLabelMessage the message send for every subcommand.
	 * @return this class.
	 */
	@Override
	public CommandRegistering setCommandLabelMessageNoPerms(String commandLabelMessage) {
		this.commandLabelMessageNoPerms = commandLabelMessage;
		return this;
	}

	/**
	 * Get the permission for use the main command.
	 *
	 * @return the permission or null if not set.
	 */
	@Override
	public String getCommandLabelPermission() {
		return commandLabelPermission;
	}

	/**
	 * Set the permission used.
	 *
	 * @param commandLabelPermission the permission
	 * @return this class.
	 */
	@Override
	public CommandRegistering setCommandLabelPermission(final String commandLabelPermission) {
		this.commandLabelPermission = commandLabelPermission;
		return this;
	}

	public boolean isRegisteredMainCommand() {
		return registeredMainCommand;
	}

	public CommandRegistering setRegisteredMainCommand(final boolean registeredMainCommand) {
		this.registeredMainCommand = registeredMainCommand;
		return this;
	}

	/**
	 * Unregisters a subcommand with the specified sub-label.
	 *
	 * @param subLabel The sub-label of the subcommand to unregister.
	 */
	@Override
	public void unregisterSubCommand(String subLabel) {
		commands.forEach(commandBuilder -> commandBuilder.getCommandLabels().removeIf(label -> label.equals(subLabel)));
	}

	/**
	 * Returns the list of registered sub commands. You can't change
	 * this list.
	 *
	 * @return The list of sub commands.
	 */
	@Override
	public List<CommandProperty> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	/**
	 * Returns the command builder with the specified sub-label.
	 *
	 * @param label The sub-label of the command builder to retrieve.
	 * @return The command builder with the specified sub-label, or null if not found.
	 */
	@Override
	public CommandProperty getCommandBuilder(String label) {
		return getCommandBuilder(label, false);
	}

	/**
	 * Returns the command builder with the specified sub-label.
	 *
	 * @param label      The sub-label of the command builder to retrieve.
	 * @param startsWith Specifies whether the sub-label should match the beginning of the command builder's sub-label.
	 * @return The command builder with the specified sub-label, or null if not found.
	 */
	@Override
	public CommandProperty getCommandBuilder(String label, boolean startsWith) {
		for (final CommandProperty command : commands) {
			if (startsWith && (label.isEmpty() || command.firstLabelMatch(label, true) != null))
				return command;
			if (command.firstLabelMatch(label) != null)
				return command;
		}
		return null;
	}

	/**
	 * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
	 * If the main command has already been registered, this method does nothing.
	 *
	 * @param fallbackPrefix The prefix to use if the normal command cannot be used.
	 * @param mainCommand    The main command to register.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering registerMainCommand(String fallbackPrefix, String mainCommand) {
		return this.registerMainCommand(fallbackPrefix, mainCommand, "", "", new String[0]);
	}

	/**
	 * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
	 * If the main command has already been registered, this method does nothing.
	 *
	 * @param fallbackPrefix The prefix to use if the normal command cannot be used.
	 * @param mainCommand    The main command to register.
	 * @param aliases        The aliases of the main command.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering registerMainCommand(String fallbackPrefix, String mainCommand, String... aliases) {
		return this.registerMainCommand(fallbackPrefix, mainCommand, "", "", aliases);
	}

	/**
	 * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
	 * If the main command has already been registered, this method does nothing.
	 *
	 * @param fallbackPrefix The prefix to use if the normal command cannot be used.
	 * @param mainCommand    The main command to register.
	 * @param description    The description of the main command.
	 * @param usageMessage   The usage message of the main command.
	 * @param aliases        The aliases of the main command.
	 * @return The CommandRegistering instance.
	 */
	@Override
	public CommandRegistering registerMainCommand(String fallbackPrefix, String mainCommand, String description, String usageMessage, String... aliases) {
		final String[] main = mainCommand.split("\\|");
		if (registeredMainCommand) return this;
		if (main.length > 1)
			for (final String command : main)
				this.register(fallbackPrefix, new CommandExecutor(this, command, description, usageMessage, Arrays.asList(aliases)));
		else
			this.register(fallbackPrefix, new CommandExecutor(this, mainCommand, description, usageMessage, Arrays.asList(aliases)));
		registeredMainCommand = true;

		return this;
	}

	@Override
	public boolean addCommands(CommandProperty subCommand, Set<String> commandLabels) {
		if (!commandLabels.isEmpty()) {
			for (final String label : commandLabels) {
				if (label == null)
					throw new CommandException("&c" + "You can´t register a command with a label set to null.");
			}
			commands.add(subCommand);
			commands.sort(Comparator.comparing(CommandProperty::getFirstSortedLabel, Comparator.nullsLast(String::compareTo)));
			return true;
		} else {
			throw new CommandException("&c" + "You can´t register a command without labels");
		}
	}

	/**
	 * Registers the specified command with the provided fallback prefix.
	 * This method uses reflection to access the commandMap and register the command.
	 *
	 * @param fallbackPrefix The fallback prefix to use if the normal command cannot be used.
	 * @param command        The command to register.
	 */
	public void register(final String fallbackPrefix, final Command command) {
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			final CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			commandMap.register(fallbackPrefix, command);
		} catch (final NoSuchFieldException | IllegalAccessException e) {
			log.log(e,() -> "It failed to register your command");
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof CommandRegister)) return false;
		final CommandRegister that = (CommandRegister) o;
		return registeredMainCommand == that.registeredMainCommand && commands.equals(that.commands) && Objects.equals(commandLabelMessage, that.commandLabelMessage) && Objects.equals(commandLabelMessageNoPerms, that.commandLabelMessageNoPerms) && Objects.equals(prefixMessage, that.prefixMessage) && Objects.equals(suffixMessage, that.suffixMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commands, commandLabelMessage, commandLabelMessageNoPerms, prefixMessage, suffixMessage, registeredMainCommand);
	}
}
