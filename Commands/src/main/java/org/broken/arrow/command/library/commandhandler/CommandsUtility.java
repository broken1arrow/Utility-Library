package org.broken.arrow.command.library.commandhandler;

import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.command.library.CommandRegister;
import org.broken.arrow.command.library.command.CommandHolder;
import org.broken.arrow.command.library.command.builders.CommandBuilder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandsUtility extends Command {

	private final CommandRegister commandRegister;

	public CommandsUtility(@Nonnull CommandRegister commandRegister, @Nonnull final String name, @Nonnull final String description, @Nonnull final String usageMessage, @Nonnull final List<String> aliases) {
		super(name, description, usageMessage, aliases);
		this.commandRegister = commandRegister;
	}

	/**
	 * Executes the command, returning its success
	 *
	 * @param sender       Source object which is executing this command
	 * @param commandLabel The alias of the command used
	 * @param args         All arguments passed to the command, split via ' '
	 * @return true if the command was successful, otherwise false
	 */
	@Override
	public boolean execute(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, commandLabel);
		}
		if (args.length > 0) {
			final CommandBuilder executor = commandRegister.getCommandBuilder(args[0]);
			if (executor != null) {
				if (!checkPermission(sender, executor)) {
					String permissionMessage = executor.getPermissionMessage();
					if (permissionMessage != null)
						sender.sendMessage(colors(placeholders(permissionMessage, commandLabel, executor)));
					return false;
				}
				CommandHolder holder = executor.getExecutor();
				boolean excuteCommand = holder.excuteCommand(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));

				if (executor.getUsageMessages() != null && !excuteCommand)
					for (final String usage : executor.getUsageMessages()) {
						sender.sendMessage(colors(placeholders(usage, commandLabel, executor)));
					}

				if (executor.getDescription() != null && (Arrays.toString(args).endsWith("?") || Arrays.toString(args).endsWith("help")))
					sender.sendMessage(placeholders(executor.getDescription(), commandLabel, executor));
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public List<String> tabComplete(@Nonnull final CommandSender sender, @Nonnull final String alias, @Nonnull final String[] args) throws IllegalArgumentException {
		if (args.length > 0) {
			final CommandBuilder subcommand = commandRegister.getCommandBuilder(args[0], true);
			if (subcommand == null) return new ArrayList<>();
			if (args.length == 1) return tabCompleteSubcommands(sender, args[0], subcommand.isHideLable());
			final List<String> tabComplete = subcommand.getExecutor().excuteTabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
			return tabComplete != null && checkPermission(sender, subcommand) ? tabComplete : new ArrayList<>();
		}
		return new ArrayList<>();
	}


	@Nonnull
	@Override
	public List<String> tabComplete(@Nonnull final CommandSender sender, @Nonnull final String alias, @Nonnull final String[] args, @Nullable final Location location) throws IllegalArgumentException {
		return tabComplete(sender, alias, args);
	}

	private boolean checkPermission(final CommandSender sender, final CommandBuilder commandBuilder) {
		if (commandBuilder.getPermission() == null || commandBuilder.getPermission().isEmpty()) return true;

		return permissionCheck(sender, commandBuilder.getPermission());
	}

	private boolean permissionCheck(CommandSender sender, String permission) {
		if (permission == null) return true;
		if (!(sender instanceof Player)) return true;
		final Player player = (Player) sender;

		return player.isOp() || player.hasPermission(permission);
	}

	private List<String> tabCompleteSubcommands(final CommandSender sender, String param, final boolean overridePermission) {
		param = param.toLowerCase();
		final List<String> tab = new ArrayList<>();
		for (final CommandBuilder subcommand : commandRegister.getCommands()) {
			final String label = subcommand.getSubLable();
			if (!checkPermission(sender, subcommand) && overridePermission) {
				continue;
			}
			if (!label.trim().isEmpty() && label.startsWith(param)) tab.add(label);
		}
		return tab;
	}

	private void sendMessage(final CommandSender sender, String commandLabel) {
		final List<String> helpPrefixMessage = commandRegister.getPrefixMessage();
		if (helpPrefixMessage != null && !helpPrefixMessage.isEmpty())
			for (final String prefixMessage : helpPrefixMessage)
				sender.sendMessage(colors(prefixMessage));
		final String commandLableMessage = commandRegister.getCommandLableMessage();
		final String lableMessageNoPerms = commandRegister.getCommandLableMessageNoPerms();
		if (lableMessageNoPerms != null && !lableMessageNoPerms.isEmpty() && !permissionCheck(sender, commandRegister.getCommandLablePermission())) {
			sender.sendMessage(colors(placeholders(lableMessageNoPerms, commandLabel, null)));

		} else if (commandLableMessage != null && !commandLableMessage.isEmpty()) {
			for (final CommandBuilder subcommand : commandRegister.getCommands()) {
				if (subcommand.isHideLable() && !checkPermission(sender, subcommand)) {
					continue;
				}
				if (!checkPermission(sender, subcommand)) {
					sender.sendMessage(colors(placeholders(commandLableMessage, commandLabel, subcommand)));
				}
			}
		}
		final List<String> helpSuffixMessage = commandRegister.getSuffixMessage();
		if (helpSuffixMessage != null && !helpSuffixMessage.isEmpty())
			for (final String suffixMssage : helpSuffixMessage)
				sender.sendMessage(colors(suffixMssage));
	}


	public String placeholders(final String message, final String commandLabel, final CommandBuilder subcommand) {
		if (message == null) return "";
		String permission = subcommand != null ? subcommand.getPermission() : null;
		if (permission == null)
			permission = "";
		return message
				.replace("{lable}", "/" + commandLabel + (subcommand != null ? " " + subcommand.getSubLable() : ""))
				.replace("{perm}", permission);
	}

	public String colors(final String message) {
		if (message == null) return "";
		return TextTranslator.toSpigotFormat(message);
	}
}
