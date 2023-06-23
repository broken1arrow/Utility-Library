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
	String getCommandLableMessage();

	/**
	 * Use {lable} to replace it with the command name and {perm} to get permission.
	 *
	 * @param commandLableMessage the message send for every subcomnmand.
	 * @return this class.
	 */
	CommandRegister setCommandLableMessage(String commandLableMessage);

	/**
	 * Get the message if player not have the permission.
	 *
	 * @return the message or null.
	 */
	String getCommandLableMessageNoPerms();

	/**
	 * Use {lable} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLableMessage the message send for every subcomnmand.
	 * @return this class.
	 */
	CommandRegister setCommandLableMessageNoPerms(String commandLableMessage);

	/**
	 * Get the permission for use the main command.
	 *
	 * @return the permission or null if not set.
	 */
	String getCommandLablePermission();

	/**
	 * Set the permission used.
	 *
	 * @param commandLablePermission the permission
	 * @return this class.
	 */
	CommandRegister setCommandLablePermission(String commandLablePermission);
	
	/**
	 * Returns the list of prefix messages to display in the command help.
	 *
	 * @return The list of prefix messages.
	 */
	List<String> getHelpPrefixMessage();

	/**
	 * Help message befor the command sugestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setHelpPrefixMessage(String... helpPrefixMessage);

	/**
	 * Help message befor the command sugestions.
	 *
	 * @param helpPrefixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setHelpPrefixMessage(List<String> helpPrefixMessage);

	/**
	 * Returns the list of suffix messages to display in the command help.
	 *
	 * @return The list of suffix messages.
	 */
	List<String> getHelpSuffixMessage();

	/**
	 * Help message after the command sugestions.
	 *
	 * @param helpSuffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setHelpSuffixMessage(String... helpSuffixMessage);

	/**
	 * Help message after the command sugestions.
	 *
	 * @param helpSuffixMessage the message send before.
	 * @return this class.
	 */
	CommandRegister setHelpSuffixMessage(List<String> helpSuffixMessage);

	/**
	 * Unregisters a subcommand with the specified sublabel.
	 *
	 * @param subLabel The sublabel of the subcommand to unregister.
	 */
	void unregisterSubCommand(String subLabel);

	/**
	 * Get all registed commands.
	 *
	 * @return list of all commands added.
	 */
	List<CommandBuilder> getCommands();

	CommandBuilder getCommandBuilder(String lable);

	CommandBuilder getCommandBuilder(String lable, boolean startsWith);

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
	 * @param aliases        set alias for your command to use insted of the main command.
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
	 * @param aliases        set alias for your command to use insted of the main command.
	 * @return it return CommandRegister instance.
	 */
	CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String description, String usageMessage, String... aliases);

	boolean collectCommands(CommandBuilder commandBuilder, String[] commandlabels);
}
