package org.broken.arrow.command.library.commandhandler;

import org.broken.arrow.command.library.CommandRegister;
import org.broken.arrow.command.library.command.builders.CommandBuilder;

import java.util.List;

/**
 * A utility class for registering and managing command builders.
 * CommandRegister provides methods for registering subcommands, setting command label messages and permissions,
 * retrieving command builders, and registering the main command.
 */
public interface CommandRegistering {

	/**
	 * Registers a subcommand with the {@link CommandBuilder.Builder}.
	 * If a sublabel is specified in the command builder, the command will be registered under that sublabel;
	 * otherwise, it will be registered under the executor's command label.
	 *
	 * @param commandBuilder The command builder to register.
	 */
	void registerSubCommand(CommandBuilder commandBuilder);

	/**
	 * Returns the message to display as the command label.
	 *
	 * @return The command label message.
	 */
	String getCommandLabelMessage();

	/**
	 * Use {label} to replace it with the command name and {perm} to get permission.
	 *
	 * @param commandLabelMessage the message send for every sub-command.
	 * @return this class.
	 */
	CommandRegister setCommandLabelMessage(String commandLabelMessage);

	/**
	 * Returns the list of prefix messages to display in the command help.
	 *
	 * @return The list of prefix messages.
	 */
	List<String> getPrefixMessage();

	/**
	 * Help message before the command suggestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setPrefixMessage(String... helpPrefixMessage);

	/**
	 * Help message before the command suggestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setPrefixMessage(List<String> helpPrefixMessage);

	/**
	 * Returns the list of suffix messages to display in the command help.
	 *
	 * @return The list of suffix messages.
	 */
	List<String> getSuffixMessage();

	/**
	 * Help message after the command suggestions.
	 *
	 * @param suffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setSuffixMessage(String... suffixMessage);

	/**
	 * Help message after the command suggestions.
	 *
	 * @param helpSuffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setSuffixMessage(List<String> helpSuffixMessage);

	/**
	 * Get the message if player not have the permission.
	 *
	 * @return the message or null.
	 */
	String getCommandLabelMessageNoPerms();

	/**
	 * Use {label} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLabelMessage the message send for every subcommand.
	 * @return this class.
	 */
	CommandRegister setCommandLabelMessageNoPerms(String commandLabelMessage);

	/**
	 * Get the permission for use the main command.
	 *
	 * @return the permission or null if not set.
	 */
	String getCommandLabelPermission();

	/**
	 * Set the permission used.
	 *
	 * @param commandLabelPermission the permission
	 * @return this class.
	 */
	CommandRegister setCommandLabelPermission(String commandLabelPermission);

	/**
	 * Unregisters a subcommand with the specified sublabel.
	 *
	 * @param subLabel The sublabel of the subcommand to unregister.
	 */
	void unregisterSubCommand(String subLabel);

	/**
	 * Get all register commands.
	 *
	 * @return list of all commands added.
	 */
	List<CommandBuilder> getCommands();

	CommandBuilder getCommandBuilder(String label);

	CommandBuilder getCommandBuilder(String label, boolean startsWith);

	/**
	 * Register main command used for your subcommands.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param mainCommand    the command you want to register.
	 * @return it return CommandRegister instance.
	 */
	CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand);

	/**
	 * Register main command used for your subcommands.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param aliases        set alias for your command to use instead of the main command.
	 * @param mainCommand    the command you want to register.
	 * @return it return CommandRegister instance.
	 */
	CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String... aliases);

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
	CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String description, String usageMessage, String... aliases);

	boolean collectCommands(CommandBuilder commandBuilder, String[] commandlabels);
}
