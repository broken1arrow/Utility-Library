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


public class CommandRegister implements CommandRegistering {

	private final List<CommandBuilder> commands = Collections.synchronizedList(new ArrayList<>());
	private String commandLableMessage;
	private String commandLableMessageNoPerms;
	private List<String> helpPrefixMessage;
	private List<String> helpSuffixMessage;
	private boolean registedMainCommand;


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

	@Override
	public String getCommandLableMessage() {
		return commandLableMessage;
	}

	@Override
	public CommandRegister setCommandLableMessage(String commandLableMessage) {
		this.commandLableMessage = commandLableMessage;
		return this;
	}

	@Override
	public String getCommandLableMessageNoPerms() {
		return commandLableMessageNoPerms;
	}

	@Override
	public CommandRegister setCommandLableMessageNoPerms(String commandLableMessage) {
		this.commandLableMessageNoPerms = commandLableMessage;
		return this;
	}

	@Override
	public List<String> getHelpPrefixMessage() {
		return helpPrefixMessage;
	}

	@Override
	public CommandRegister setHelpPrefixMessage(String... helpPrefixMessage) {
		this.helpPrefixMessage = Arrays.asList(helpPrefixMessage);
		return this;
	}

	@Override
	public CommandRegister setHelpPrefixMessage(List<String> helpPrefixMessage) {
		this.helpPrefixMessage = helpPrefixMessage;
		return this;
	}

	@Override
	public List<String> getHelpSuffixMessage() {
		return helpSuffixMessage;
	}

	@Override
	public CommandRegister setHelpSuffixMessage(String... helpSuffixMessage) {
		this.helpSuffixMessage = Arrays.asList(helpSuffixMessage);
		return this;
	}

	@Override
	public CommandRegister setHelpSuffixMessage(List<String> helpSuffixMessage) {
		this.helpSuffixMessage = helpSuffixMessage;
		return this;
	}

	@Override
	public void unregisterSubCommand(String subLable) {
		commands.removeIf(commandBuilder -> commandBuilder.getSubLable().equals(subLable));
	}

	@Override
	public List<CommandBuilder> getCommands() {
		return commands;
	}

	@Override
	public CommandBuilder getCommandBuilder(String lable) {
		return getCommandBuilder(lable, false);
	}

	@Override
	public CommandBuilder getCommandBuilder(String lable, boolean startsWith) {
		for (final CommandBuilder command : commands) {
			if (startsWith && (lable.isEmpty() || command.getSubLable().startsWith(lable)))
				return command;
			if (command.getSubLable().equalsIgnoreCase(lable))
				return command;
		}
		return null;
	}

	@Override
	public CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand) {
		return this.registerMainCommand(fallbackPrefix, mainCommand, "", "", new String[]{});
	}


	@Override
	public CommandRegister registerMainCommand(String fallbackPrefix, String mainCommand, String... aliases) {
		return this.registerMainCommand(fallbackPrefix, mainCommand, "", "", aliases);
	}

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

	@Override
	public boolean collectCommands(CommandBuilder commandBuilder, String[] commandlabels) {
		if (commandlabels.length > 1) {
			for (final String lable : commandlabels) {
				final CommandBuilder newComandBuilder = commandBuilder.getBuilder().setSubLable(lable).build();
				commands.removeIf(oldCommandBuilder -> oldCommandBuilder.getSubLable().equals(lable));
				commands.add(newComandBuilder);
			}
			commands.sort(Comparator.comparing(CommandBuilder::getSubLable));
			return true;
		}
		return false;
	}

	/**
	 * Use registerMainCommand metods to register a command.
	 *
	 * @param fallbackPrefix the prefix to use if could not use the normal command.
	 * @param command        the command you want to register.
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
