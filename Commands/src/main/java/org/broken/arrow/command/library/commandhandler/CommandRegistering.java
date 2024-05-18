package org.broken.arrow.command.library.commandhandler;

import org.broken.arrow.command.library.command.CommandProperty;
import org.bukkit.command.CommandException;

import java.util.List;
import java.util.Set;

/**
 * A utility interface for registering and managing commands.
 * The {@code CommandRegistering} interface provides methods for registering subcommands,
 * setting command label messages and permissions, retrieving command builders, and
 * registering the main command.
 */
public interface CommandRegistering {

	/**
	 * Registers a subcommand using the labels provided by the {@link CommandProperty#getCommandLabels()} method.
	 * This method ensures that the command labels are neither empty nor null before registering.
	 *
	 * @param commandBuilder The command builder to register. Must not be null and should have valid command labels.
	 * @throws CommandException if the command labels are empty or null.
	 */
	CommandRegistering registerSubCommand(CommandProperty commandBuilder);

	/**
	 * Use {label} to replace it with the command name and {perm} to get permission.
	 *
	 * @param commandLabelMessage the message send for every sub-command.
	 * @return this class.
	 */
	CommandRegistering  setCommandLabelMessage(String commandLabelMessage);

	/**
	 * Help message before the command suggestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegistering  setPrefixMessage(String... helpPrefixMessage);

	/**
	 * Help message before the command suggestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegistering  setPrefixMessage(List<String> helpPrefixMessage);

	/**
	 * Help message after the command suggestions.
	 *
	 * @param suffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegistering  setSuffixMessage(String... suffixMessage);

	/**
	 * Help message after the command suggestions.
	 *
	 * @param helpSuffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegistering  setSuffixMessage(List<String> helpSuffixMessage);

	/**
	 * Sets the description of the main command. The description could provide information about the main command
	 * and/or brief explanation to the subcommands. Player then add a "?" or "help" at the end of the command to
	 * request additional information about the command.
	 *
	 * @param descriptions The description message that explains what the command does.
	 * @return The Builder instance.
	 */
	CommandRegistering setDescriptions(String... descriptions);

	/**
	 * Set the permission used.
	 *
	 * @param commandLabelPermission the permission
	 * @return this class.
	 */
	CommandRegistering setCommandLabelPermission(String commandLabelPermission);

	/**
	 * Use {label} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLabelMessage the message send for every subcommand.
	 * @return this class.
	 */
	CommandRegistering  setCommandLabelMessageNoPerms(String commandLabelMessage);

	/**
	 * Returns the list of prefix messages to display in the command help.
	 *
	 * @return The list of prefix messages.
	 */
	List<String> getPrefixMessage();

	/**
	 * Returns the list of suffix messages to display in the command help.
	 *
	 * @return The list of suffix messages.
	 */
	List<String> getSuffixMessage();

	/**
	 * Get the message if player not have the permission.
	 *
	 * @return the message or null.
	 */
	String getCommandLabelMessageNoPerms();

	/**
	 * Returns the message to display as the command label.
	 *
	 * @return The command label message.
	 */
	String getCommandLabelMessage();

	/**
	 * Get the permission for use the main command.
	 *
	 * @return the permission or null if not set.
	 */
	String getCommandLabelPermission();

	/**
	 * Get all register commands.
	 *
	 * @return list of all commands added.
	 */
	List<CommandProperty> getCommands();

	CommandProperty getCommandBuilder(String label);

	CommandProperty getCommandBuilder(String label, boolean startsWith);

	/**
	 * Unregisters a subcommand with the specified sub-label.
	 *
	 * @param subLabel The sub-label of the subcommand to unregister.
	 */
	void unregisterSubCommand(String subLabel);

	/**
	 * Register main command used for your subcommands.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param mainCommand    the command you want to register.
	 * @return it return CommandRegister instance.
	 */
	CommandRegistering  registerMainCommand(String fallbackPrefix, String mainCommand);

	/**
	 * Register main command used for your subcommands.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param aliases        set alias for your command to use instead of the main command.
	 * @param mainCommand    the command you want to register.
	 * @return it return CommandRegister instance.
	 */
	CommandRegistering  registerMainCommand(String fallbackPrefix, String mainCommand, String... aliases);

	/**
	 * Register main command used for your subcommands.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param mainCommand    the command you want to register.
	 * @param description    description of the command.
	 * @param usageMessage   message how to use the command.
	 * @param aliases        set alias for your command to use instead of the main command.
	 * @return it return CommandRegister instance.
	 */
	CommandRegistering  registerMainCommand(String fallbackPrefix, String mainCommand, String description, String usageMessage, String... aliases);

	boolean collectCommands(CommandProperty commandBuilder, Set<String> commandLabels);
}
