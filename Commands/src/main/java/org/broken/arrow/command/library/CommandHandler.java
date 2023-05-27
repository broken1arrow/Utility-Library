package org.broken.arrow.command.library;


import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public interface CommandHandler {

	/**
	 * Calling while command executed by specified sender. So it can be ether player or other sender.
	 * So check if it is a player before cast it to player.
	 *
	 * @param sender       The command sender but will automatically convert to specified instance
	 * @param commandLabel The command prefix (/cch = cch)
	 * @param cmdArg       The arguments (/cch menu will will open menu)
	 */
	void onCommand(final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg);

	/**
	 * Calling while sender trying to tab-complete/type the command. This method is uesed to to
	 * suggest next part of the command. After first part.
	 *
	 * @param sender       The command sender but will automatically convert to specified instance
	 * @param commandLabel The command prefix (/cch = cch)
	 * @param cmdArg       The arguments (/cch menu [TAB] will open menu)
	 * @return list of command suggestions.
	 */
	@Nullable
	default List<String> onTabComplete(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg) {
		return Collections.emptyList();
	}
}
