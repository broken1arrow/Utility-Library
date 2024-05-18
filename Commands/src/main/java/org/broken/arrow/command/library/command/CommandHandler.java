package org.broken.arrow.command.library.command;


import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This interface contains the executables when a command get triggered.
 * @deprecated not in use any more. This interface doesn't make any sense, it is not used in the code.
 */
@Deprecated
public interface CommandHandler {

    /**
     * Called when the command is executed by the specified sender. The sender can be either a player or, for example, the console.
     * Therefore, check if the sender is a player before casting it to a Player instance. Alternatively, use {@link CommandHolder#getPlayer()}
     * to get the player without needing to cast the sender, or {@link CommandHolder#checkConsole()} to prevent something other than a
     * player from executing the command.
     *
     * @param sender       The command sender, which could be a player or the console.
     * @param commandLabel The command prefix. For example, if the command is "/command", it will be converted to commandName.
     * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
     *                     after the command prefix. For example, if the command used is "/commandName menu main 5," the
     *                     `cmdArg` array will contain ["main", "5"]. You can use these arguments to execute the next
     *                     part of the command.
     * @return True if the command execution is successful, false otherwise. If the method returns false, it could then send the {@link CommandProperty#getUsageMessages()}
     * if the message is set.
     */
    boolean executeCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg);

    /**
     * Called when the sender is trying to tab-complete/type the command. This method is used to suggest the next part
     * of the command after the initial part.
     *
     * @param sender       The command sender, could be player or console.
     * @param commandLabel The command prefix for example this will be /commandName converted to command.
     * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
     *                     after the initial part of the command. For example, if the command typed so far is
     *                     "/commandName menu 1," and the user is currently trying to type the next argument, the
     *                     `cmdArg` array will contain ["1"]. You can use these arguments to suggest the next
     *                     part of the command or provide auto-completion options.
     * @return A list of command suggestions.
     */
    @Nullable
    List<String> executeTabComplete(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArg);
}
