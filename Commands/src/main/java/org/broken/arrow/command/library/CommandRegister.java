package org.broken.arrow.command.library;


import org.broken.arrow.command.library.command.builders.CommandBuilder;
import org.broken.arrow.command.library.commandhandler.CommandRegistering;
import org.broken.arrow.command.library.commandhandler.CommandsUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A utility class for registering and managing command builders.
 * CommandRegister provides methods for registering subcommands, setting command label messages and permissions,
 * retrieving command builders, and registering the main command.
 */
public class CommandRegister implements CommandRegistering {

	private final List<CommandBuilder> commands = Collections.synchronizedList(new ArrayList<>());
	private String commandLableMessage;
	private String commandLableMessageNoPerms;
	private String commandLablePermission;
	private List<String> helpPrefixMessage;
	private List<String> helpSuffixMessage;
	private boolean registedMainCommand;

	/**
	 * Registers a subcommand with the {@link CommandBuilder.Builder}.
	 * If a sublabel is specified in the command builder, the command will be registered under that sublabel;
	 * otherwise, it will be registered under the executor's command label.
	 *
	 * @param commandBuilder The command builder to register.
	 */
	@Override
	public void registerSubCommand(final CommandBuilder commandBuilder) {
		final String[] lableSplit;
		if (commandBuilder.getSubLable() == null) {
			lableSplit = commandBuilder.getExecutor().getCommandLable().split("\\|");
		} else {
			lableSplit = commandBuilder.getSubLable().split("\\|");
		}
		if (collectCommands(commandBuilder, lableSplit)) {
			return;
		}
		commands.removeIf(oldCommandBuilder -> oldCommandBuilder.equals(commandBuilder));
		commands.removeIf(oldCommandBuilder -> oldCommandBuilder.getSubLable().equals(commandBuilder.getSubLable()));
		commands.add(commandBuilder);
		commands.sort(Comparator.comparing(CommandBuilder::getSubLable));
	}

	/**
	 * Returns the message to display as the command label.
	 *
	 * @return The command label message.
	 */
	@Override
	public String getCommandLableMessage() {
		return commandLableMessage;
	}

	/**
	 * Sets the message to display as the command label.
	 *
	 * @param commandLableMessage The command label message to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister setCommandLableMessage(String commandLableMessage) {
		this.commandLableMessage = commandLableMessage;
		return this;
	}

	/**
	 * Get the message if player not have the permission.
	 *
	 * @return the message or null.
	 */
	@Override
	public String getCommandLableMessageNoPerms() {
		return commandLableMessageNoPerms;
	}

	/**
	 * Use {lable} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLableMessage the message send for every subcomnmand.
	 * @return this class.
	 */
	@Override
	public CommandRegister setCommandLableMessageNoPerms(String commandLableMessage) {
		this.commandLableMessageNoPerms = commandLableMessage;
		return this;
	}

	/**
	 * Get the permission for use the main command.
	 *
	 * @return the permission or null if not set.
	 */
	@Override
	public String getCommandLablePermission() {
		return commandLablePermission;
	}

	/**
	 * Set the permission used.
	 *
	 * @param commandLablePermission the permission
	 * @return this class.
	 */
	@Override
	public CommandRegister setCommandLablePermission(final String commandLablePermission) {
		this.commandLablePermission = commandLablePermission;
		return this;
	}

	public boolean isRegistedMainCommand() {
		return registedMainCommand;
	}

	public CommandRegister setRegistedMainCommand(final boolean registedMainCommand) {
		this.registedMainCommand = registedMainCommand;
		return this;
	}

	/**
	 * Returns the list of prefix messages to display in the command help.
	 *
	 * @return The list of prefix messages.
	 */
	@Override
	public List<String> getHelpPrefixMessage() {
		return helpPrefixMessage;
	}

	/**
	 * Sets the prefix messages to display in the command help using the provided string values.
	 *
	 * @param helpPrefixMessage The prefix messages to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister setHelpPrefixMessage(String... helpPrefixMessage) {
		this.helpPrefixMessage = Arrays.asList(helpPrefixMessage);
		return this;
	}

	/**
	 * Sets the prefix messages to display in the command help using the provided list of strings.
	 *
	 * @param helpPrefixMessage The prefix messages to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister setHelpPrefixMessage(List<String> helpPrefixMessage) {
		this.helpPrefixMessage = helpPrefixMessage;
		return this;
	}

	/**
	 * Returns the list of suffix messages to display in the command help.
	 *
	 * @return The list of suffix messages.
	 */
	@Override
	public List<String> getHelpSuffixMessage() {
		return helpSuffixMessage;
	}

	/**
	 * Sets the suffix messages to display in the command help using the provided string values.
	 *
	 * @param helpSuffixMessage The suffix messages to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister setHelpSuffixMessage(String... helpSuffixMessage) {
		this.helpSuffixMessage = Arrays.asList(helpSuffixMessage);
		return this;
	}
	
	/**
	 * Sets the suffix messages to display in the command help using the provided list of strings.
	 *
	 * @param helpSuffixMessage The suffix messages to set.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister setHelpSuffixMessage(List<String> helpSuffixMessage) {
		this.helpSuffixMessage = helpSuffixMessage;
		return this;
	}

	/**
	 * Unregisters a subcommand with the specified sublabel.
	 *
	 * @param subLabel The sublabel of the subcommand to unregister.
	 */
	@Override
	public void unregisterSubCommand(String subLabel) {
		commands.removeIf(commandBuilder -> commandBuilder.getSubLable().equals(subLabel));
	}

	/**
	 * Returns the list of registered command builders.
	 *
	 * @return The list of command builders.
	 */
	@Override
	public List<CommandBuilder> getCommands() {
		return commands;
	}

	/**
	 * Returns the command builder with the specified sublabel.
	 *
	 * @param label The sublabel of the command builder to retrieve.
	 * @return The command builder with the specified sublabel, or null if not found.
	 */
	@Override
	public CommandBuilder getCommandBuilder(String label) {
		return getCommandBuilder(label, false);
	}

	/**
	 * Returns the command builder with the specified sublabel.
	 *
	 * @param label      The sublabel of the command builder to retrieve.
	 * @param startsWith Specifies whether the sublabel should match the beginning of the command builder's sublabel.
	 * @return The command builder with the specified sublabel, or null if not found.
	 */
	@Override
	public CommandBuilder getCommandBuilder(String label, boolean startsWith) {
		for (final CommandBuilder command : commands) {
			if (startsWith && (label.isEmpty() || command.getSubLable().startsWith(label)))
				return command;
			if (command.getSubLable().equalsIgnoreCase(label))
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
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand) {
		return this.registerMainCommand(fallbackPrefix, mainCommand, "", "", new String[]{});
	}

	/**
	 * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
	 * If the main command has already been registered, this method does nothing.
	 *
	 * @param fallbackPrefix The prefix to use if the normal command cannot be used.
	 * @param mainCommand    The main command to register.
	 * @param aliases        The aliases of the main command.
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String... aliases) {
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
	 * @return The CommandRegister instance.
	 */
	@Override
	public CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String description, String usageMessage, String... aliases) {
		final String[] main = mainCommand.split("\\|");
		if (registedMainCommand) return this;
		if (main.length > 1)
			for (final String command : main)
				this.register(fallbackPrefix, new CommandsUtility(this, command, description, usageMessage, Arrays.asList(aliases)));
		else
			this.register(fallbackPrefix, new CommandsUtility(this, mainCommand, description, usageMessage, Arrays.asList(aliases)));
		registedMainCommand = true;

		return this;
	}

	/**
	 * Collects subcommands from the specified command builder with the given command labels.
	 * If the command labels contain multiple labels separated by '|', the command builder will be registered
	 * with each sublabel separately.
	 *
	 * @param commandBuilder The command builder to collect subcommands from.
	 * @param commandLabels  The command labels to assign to the subcommands.
	 * @return {@code true} if subcommands were collected, {@code false} otherwise.
	 */
	@Override
	public boolean collectCommands(CommandBuilder commandBuilder, String[] commandLabels) {
		if (commandLabels.length > 1) {
			for (final String lable : commandLabels) {
				final CommandBuilder newComandBuilder = commandBuilder.getBuilder().setSubLabel(lable).build();
				commands.removeIf(oldCommandBuilder -> oldCommandBuilder.getSubLable().equals(lable));
				commands.add(newComandBuilder);
			}
			commands.sort(Comparator.comparing(CommandBuilder::getSubLable));
			return true;
		}
		return false;
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
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof CommandRegister)) return false;
		final CommandRegister that = (CommandRegister) o;
		return registedMainCommand == that.registedMainCommand && commands.equals(that.commands) && Objects.equals(commandLableMessage, that.commandLableMessage) && Objects.equals(commandLableMessageNoPerms, that.commandLableMessageNoPerms) && Objects.equals(helpPrefixMessage, that.helpPrefixMessage) && Objects.equals(helpSuffixMessage, that.helpSuffixMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commands, commandLableMessage, commandLableMessageNoPerms, helpPrefixMessage, helpSuffixMessage, registedMainCommand);
	}
}
