package org.broken.arrow.library.command.builers;

import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.commandhandler.MainCommandHandler;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.command.CommandException;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Entry builder for defining a command structure, including optional subcommands,
 * a single main command, and shared command metadata such as aliases, usage, and description.
 *
 * <p>This class represents the primary configuration stage of a command before it transitions
 * into execution-specific or display configuration stages.</p>
 */
public class CommandBuilder {
    private final MainCommandHandler mainCommandHandler;

    /**
     * Creates a new command builder tied to the given {@link MainCommandHandler}.
     *
     */
    public CommandBuilder() {
        this.mainCommandHandler = new MainCommandHandler();
    }


    /**
     * Registers a group of subcommands using a configuration wrapper.
     *
     * <p>This method provides a structured way to define multiple subcommands
     * within a single lambda scope, improving readability and reducing repeated calls.</p>
     *
     * <pre>{@code
     * registerSubCommandGroup(wrapper -> {
     *     wrapper.registerSubCommand(new ReloadCommand(...));
     *     wrapper.registerSubCommand(new HelpCommand(...));
     * });
     * }</pre>
     *
     * @param consumer lambda receiving a {@link SubcommandWrapper} used to register subcommands
     * @return a {@link CommandDisplayBuilder} to continue configuration (e.g. display settings)
     * @throws CommandException if any subcommand has invalid or empty command labels
     */
    public CommandDisplayBuilder registerSubCommandGroup(@Nonnull final Consumer<SubcommandWrapper> consumer) {
        final SubcommandWrapper subcommandWrapper = new SubcommandWrapper(mainCommandHandler);
        consumer.accept(subcommandWrapper);
        return new CommandDisplayBuilder(mainCommandHandler);
    }

    /**
     * Registers one or more subcommands directly using varargs input.
     *
     * <p>This is a convenience method for quickly registering multiple subcommands
     * without using a lambda-based configuration block.</p>
     *
     * @param subCommands subcommands to register (must not be null or empty)
     * @return a {@link CommandDisplayBuilder} to continue configuration (e.g. display settings)
     * @throws CommandException            if any subcommand has invalid or empty command labels
     * @throws Validate.ValidateExceptions if a main command has already been configured
     */
    public CommandDisplayBuilder registerSubCommands(final CommandProperty... subCommands) {
        mainCommandHandler.registerSubCommands(subCommands);
        return new CommandDisplayBuilder(mainCommandHandler);
    }

    /**
     * Defines a single main command instead of using subcommands.
     *
     * <p>This mode is mutually exclusive with subcommand registration.</p>
     *
     * @param mainCommand the main command definition
     * @return The provided mainCommand instance for further configuration
     * @throws Validate.ValidateExceptions if subcommands have already been registered
     */
    public CommandProperty setMainCommand(@Nonnull final CommandProperty mainCommand) {
        mainCommandHandler.setMainCommand(mainCommand);
        return mainCommand;
    }

    /**
     * Retrieve the main command handler.
     *
     * @return Returns the main command handler.
     */
    public MainCommandHandler getMainCommandHandler() {
        return this.mainCommandHandler;
    }
}
