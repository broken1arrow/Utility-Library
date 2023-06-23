package org.broken.arrow.command.library.command;


import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CommandHandler {

	/**
	 * Called when the command is executed by the specified sender. It can be either a player or another sender.
	 * Therefore, check if the sender is a player before casting it to a Player instance.
	 *
	 * @param sender       The command sender, could be player or console.
	 * @param commandLabel The command prefix for example this will be /command converted to commandName.
	 * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
	 *                     after the command prefix. For example, if the command used is "/commandName menu 1," the
	 * @return
	 */
	boolean excuteCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg);

	/**
	 * Called when the sender is trying to tab-complete/type the command. This method is used to suggest the next part
	 * of the command after the initial part.
	 *
	 * @param sender       The command sender, could be player or console.
	 * @param commandLabel The command prefix for example this will be /commandName converted to command.
	 * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
	 *                     after the initial part of the command. For example, if the command typed so far is
	 *                     "/commandName menu 1," and the user is currently trying to type the next argument, the
	 *                     `cmdArg` array will contain ["menu", "1"]. You can use these arguments to suggest the next
	 *                     part of the command or provide auto-completion options.
	 * @return A list of command suggestions.
	 */
	@Nullable
	List<String> excuteTabComplete(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArg);
}
