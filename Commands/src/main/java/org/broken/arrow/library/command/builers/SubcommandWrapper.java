package org.broken.arrow.library.command.builers;

import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.commandhandler.MainCommandHandler;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.command.CommandException;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Lightweight wrapper used to register multiple subcommands within a scoped configuration block.
 *
 * <p>This class is intended to be used exclusively inside
 * {@link CommandBuilder#registerSubCommandGroup(Consumer)}.</p>
 */
public class SubcommandWrapper {
    private final MainCommandHandler mainCommandHandler;

    /**
     * Creates a new wrapper bound to the given command handler.
     *
     * @param mainCommandHandler internal handler managing subcommand registration
     */
    public SubcommandWrapper( @Nonnull final MainCommandHandler mainCommandHandler) {
        this.mainCommandHandler = mainCommandHandler;
    }
    /**
     * Registers a single subcommand.
     *
     * @param subCommand the subcommand to register (must not be null and must define valid labels)
     * @throws CommandException if the command labels are invalid or empty
     */
    public void registerSubCommand(final CommandProperty subCommand) {
        this.mainCommandHandler.registerSubCommand(subCommand);
    }

    /**
     * Registers multiple subcommands in a single call.
     *
     * @param subCommands subcommands to register (must not be null or empty)
     * @throws CommandException if the command labels are invalid or empty
     * @throws Validate.ValidateExceptions if a main command has already been configured
     */
    public void registerSubCommands(final CommandProperty... subCommands){
        this.mainCommandHandler.registerSubCommands(subCommands);
    }
}
