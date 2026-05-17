package org.broken.arrow.library.command.commandhandler;

import org.broken.arrow.library.command.builers.CommandBuilder;
import org.broken.arrow.library.command.builers.CommandOptions;
import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.subcommand.CommandDisplayConfig;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.command.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainCommandHandler {
    private final Map<String, CommandProperty> commands = new ConcurrentHashMap<>();
    private final CommandDisplayConfig commandDisplayConfig;
    private final CommandBuilder commandBuilder;
    private CommandProperty mainCommand;

    /**
     * Creates a new command handler that store your subcommands or your main command instance.
     *
     * @param commandBuilder The settings for the main command outside for what you set for {@link CommandProperty}
     */
    public MainCommandHandler(@Nonnull final CommandBuilder commandBuilder) {
        this.commandDisplayConfig = new CommandDisplayConfig();
        this.commandBuilder = commandBuilder;
    }

    /**
     * Registers a subcommand using the labels provided by the {@link CommandProperty#getCommandLabels()} method.
     * This method ensures that the command labels are neither empty nor null before registering.
     *
     * @param subCommand The sub-command to register. Must not be null and should have valid command labels.
     * @return Returns the class instance.
     * @throws CommandException if the command labels are empty or null.
     */
    public MainCommandHandler registerSubCommand(final CommandProperty subCommand) {
        Set<String> commandLabels = subCommand.getCommandLabels();
        commandLabels.forEach(label -> commands.put(label, subCommand));
        return this;
    }

    /**
     * Registers all subcommands using the labels provided by the {@link CommandProperty#getCommandLabels()} method.
     * This method ensures that the command labels are neither empty nor null before registering.
     *
     * @param subCommands The sub-commands to register. Must not be null and should have valid command labels.
     * @return Returns the class instance for method chaining.
     * @throws CommandException            if the command labels are empty or null.
     * @throws Validate.ValidateExceptions if a main command has already been set.
     */
    public MainCommandHandler registerSubCommands(final CommandProperty... subCommands) {
        Validate.checkBoolean(mainCommand != null,
                "Cannot register subcommands because a main command has already been set. " +
                        "Remove the setMainCommand call to use subcommands.");
        if (subCommands == null)
            return this;
        for (CommandProperty registerSubCommand : subCommands) {
            this.registerSubCommand(registerSubCommand);
        }
        return this;
    }

    /**
     * Retrieve the main command.
     *
     * @return the main command.
     */
    @Nullable
    public CommandProperty getMainCommand() {
        return mainCommand;
    }

    /**
     * Sets a single main command to be used instead of registering multiple subcommands.
     *
     * @param mainCommand The main command to set.
     * @throws Validate.ValidateExceptions if subcommands have already been registered.
     */
    public void setMainCommand(@Nonnull final CommandProperty mainCommand) {
        Validate.checkBoolean(!commands.isEmpty(),
                "Cannot set the main command because subcommands are already registered. " +
                        "Remove registerSubCommands calls to use a single main command.");
        this.mainCommand = mainCommand;
    }

    /**
     * Unregisters a subcommand with the specified sub-label.
     *
     * @param subLabel The sub-label of the subcommand to unregister.
     */
    public void unregisterSubCommand(String subLabel) {
        commands.remove(subLabel);
    }

    /**
     * Get the set values for assist the player how use the command and also permissions that
     * checks before the sub commands is checked.
     * <p>
     * Note: Only used when at least one sub command is registern.
     *
     * @return instance of the CommandDisplayConfig
     */
    @Nonnull
    public CommandDisplayConfig getCommandDisplayConfig() {
        return commandDisplayConfig;
    }

    /**
     * Returns the command builder with the specified sub-label.
     *
     * @param label The sub-label of the command builder to retrieve.
     * @return The command builder with the specified sub-label, or null if not found.
     */
    @Nullable
    public CommandProperty getCommandBuilder(String label) {
        return getCommandBuilder(label, false);
    }

    /**
     * Returns the command builder with the specified sub-label.
     *
     * @param label      The sub-label of the command builder to retrieve.
     * @param startsWith Specifies whether the sub-label should match the beginning of the command builder's sub-label.
     * @return The command builder with the specified sub-label, or null if not found.
     */
    @Nullable
    public CommandProperty getCommandBuilder(String label, boolean startsWith) {
        CommandProperty commandProperty = commands.get(label);
        if (commandProperty != null)
            return commandProperty;

        for (final Map.Entry<String, CommandProperty> command : commands.entrySet()) {
            CommandProperty value = command.getValue();
            if (startsWith && (label.isEmpty() || value.firstLabelMatch(label, true) != null))
                return value;
            if (value.firstLabelMatch(label) != null)
                return value;
        }
        return null;
    }

    /**
     * Retrieve the status if any subcommands is registered or not.
     *
     * @return Returns {@code true} if no subcommands is registered.
     */
    public boolean isSubCommandsSet() {
        return commands.isEmpty();
    }


    /**
     * Retrieve the subcommands set if you're not using the main command.
     *
     * @return The list of sub commands or empty list if non is set.
     */
    public Collection<CommandProperty> getSubcommands() {
        return this.commands.values();
    }

    /**
     * Retrieve the set options for the command, outside the {@link CommandProperty}.
     *
     * @return Returns the instance of messages set for the main part of the command.
     */
    public CommandOptions getCommandBuilder() {
        return commandBuilder;
    }

    /**
     * Checks and add the subcommand from the specified command builder with the given command labels.
     *
     * @param subCommand    The command builder to collect subcommands from.
     * @param commandLabels The command labels to assign to the subcommands.
     * @return {@code true} if subcommands were collected, {@code false} otherwise.
     */
    private boolean addCommands(CommandProperty subCommand, Set<String> commandLabels) {
        if (!commandLabels.isEmpty()) {
            for (final String label : commandLabels) {
                if (label == null)
                    throw new CommandException("&c" + "You can´t register a command with a label set to null.");
            }
            return true;
        } else {
            throw new CommandException("&c" + "You can´t register a command without labels");
        }
    }
}
