package org.broken.arrow.command.library.commandhandler;

import org.broken.arrow.command.library.CommandRegister;
import org.broken.arrow.command.library.command.builders.CommandBuilder;

import java.util.List;

public interface CommandRegistering {

	/**
	 * Register subcommand use {@link CommandBuilder.Builder} to build your
	 * command. Don't forget you also need create 1 command class for every
	 * sub command.
	 *
	 * @param commandBuilder register your build command.
	 */
	void registerSubCommand(CommandBuilder commandBuilder);


	String getCommandLableMessage();

	/**
	 * Use {lable} to replace it with the command name and {perm} to get permission.
	 *
	 * @param commandLableMessage the message send for every subcomnmand.
	 * @return this class.
	 */
	CommandRegister setCommandLableMessage(String commandLableMessage);

	String getCommandLableMessageNoPerms();

	/**
	 * Use {lable} to replace it with the command name and {perm} to get permission. Used if you not have permission.
	 *
	 * @param commandLableMessage the message send for every subcomnmand.
	 * @return this class.
	 */
	CommandRegister setCommandLableMessageNoPerms(String commandLableMessage);

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
	 * Unregister your added command. You can't then run the command when is removed.
	 *
	 * @param subLable your command used and it will be unregisted.
	 */
	void unregisterSubCommand(String subLable);

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
