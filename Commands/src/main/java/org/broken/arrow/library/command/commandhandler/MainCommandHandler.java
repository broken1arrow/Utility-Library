package org.broken.arrow.library.command.commandhandler;

import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.subcommand.CommandDisplayConfig;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.command.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MainCommandHandler {
    private final List<CommandProperty> commands = Collections.synchronizedList(new ArrayList<>());
    private final CommandDisplayConfig commandDisplayConfig;
    private CommandProperty mainCommand;

    /**
     * Creates a new command handler that store your subcommands or your main command instance.
     *
     * @param commandDisplayConfig  internal handler to configure {@link CommandDisplayConfig} for sub commands.
     */
    public MainCommandHandler(@Nonnull final CommandDisplayConfig commandDisplayConfig ) {
        this.commandDisplayConfig = commandDisplayConfig;
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
        if (addCommands(subCommand, commandLabels)) {
            return this;
        }
        commands.removeIf(oldCommandBuilder -> oldCommandBuilder.equals(subCommand));
        commands.removeIf(oldCommandBuilder -> oldCommandBuilder.getCommandLabels().equals(subCommand.getCommandLabels()));
        commands.add(subCommand);
        commands.sort(Comparator.comparing(CommandProperty::getFirstSortedLabel, Comparator.nullsLast(String::compareTo)));
        return this;
    }

    /**
     * Registers all subcommands using the labels provided by the {@link CommandProperty#getCommandLabels()} method.
     * This method ensures that the command labels are neither empty nor null before registering.
     *
     * @param subCommands The sub-commands to register. Must not be null and should have valid command labels.
     * @return Returns the class instance for method chaining.
     * @throws CommandException if the command labels are empty or null.
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
        commands.forEach(commandBuilder -> commandBuilder.getCommandLabels().removeIf(label -> label.equals(subLabel)));
    }

    /**
     * Get the set values for assist the player how use the command and also permissions that
     * checks before the sub commands is checked.
     * <p>
     * Note: Only used when at least one sub command is registern.
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
        for (final CommandProperty command : commands) {
            if (startsWith && (label.isEmpty() || command.firstLabelMatch(label, true) != null))
                return command;
            if (command.firstLabelMatch(label) != null)
                return command;
        }
        return null;
    }

    /**
     * Retrieve the status if any subcommands is registered or not.
     *
     * @return Returns {@code true} if no subcommands is registered.
     */
    public boolean isSubCommandsSet(){
        return commands.isEmpty();
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
            commands.add(subCommand);
            commands.sort(Comparator.comparing(CommandProperty::getFirstSortedLabel, Comparator.nullsLast(String::compareTo)));
            return true;
        } else {
            throw new CommandException("&c" + "You can´t register a command without labels");
        }
    }

    public  List<CommandProperty> getSubcommands() {
        return this.commands;
    }
}
