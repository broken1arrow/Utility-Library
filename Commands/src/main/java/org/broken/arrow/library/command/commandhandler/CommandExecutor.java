package org.broken.arrow.library.command.commandhandler;

import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.command.CommandRegister;
import org.broken.arrow.library.command.builers.CommandMessages;
import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.subcommand.CommandDisplayConfig;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The main command executor that handles subcommands for a base command.
 * <p>
 * This class routes the main command to specific registered subcommands,
 * manages permissions, tab completions, and help/usage messages.
 * </p>
 */
public class CommandExecutor extends Command {

    private final CommandRegister commandRegister;

    /**
     * Constructs the main command executor.
     *
     * @param commandRegister the registry that holds all subcommands
     * @param name            the main command name
     * @param description     the main command description
     * @param usageMessage    usage message shown on invalid usage
     * @param aliases         list of aliases for the main command
     */
    public CommandExecutor(@Nonnull CommandRegister commandRegister, @Nonnull final String name, @Nonnull final String description, @Nonnull final String usageMessage, @Nonnull final List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.commandRegister = commandRegister;
    }

    /**
     * Executes the main command or delegates to a subcommand.
     * <p>
     * If no arguments are given, sends general help or usage information.
     * If the first argument matches a subcommand, delegates execution to it.
     * </p>
     *
     * @param sender       the source who executed the command
     * @param commandLabel the command alias used
     * @param args         the arguments passed to the command
     * @return {@code true} if the command was handled successfully; {@code false} otherwise
     */
    @Override
    public boolean execute(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] args) {
        final MainCommandHandler commandHandler = commandRegister.getCommand(commandLabel);

        if (commandHandler != null) {
            final CommandProperty mainCommand = commandHandler.getMainCommand();
            if (mainCommand != null) {
                final String permission = mainCommand.getPermission();
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    sender.sendMessage((colors(placeholders(mainCommand.getPermissionMessage(), commandLabel, mainCommand))));
                    return false;
                }
                boolean executeCommand = mainCommand.executeCommand(sender, commandLabel, args);
                if (this.sendHelpMessage(commandHandler, sender, commandLabel, args, executeCommand))
                    return true;
                return executeCommand;
            }
            return this.handleSubCommand(sender, commandLabel, commandHandler, args);
        }

        if (args.length == 0) {
            this.sendMessage(sender, commandLabel);
        }
        if (args.length > 0) {
            if (!this.sendDescriptions(sender, commandLabel, args))
                return false;

            final CommandProperty executor = commandRegister.getCommandBuilder(args[0]);
            if (executor != null) {
                if (sendDescription(sender, commandLabel, args, executor))
                    return false;
                if (sendNoPermission(sender, commandLabel, executor)) return false;

                boolean executeCommand = executor.executeCommand(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
                sendUsageMessage(sender, commandLabel, executor, executeCommand);
            }
        }
        return false;
    }

    /**
     * Provides tab completion suggestions for the main command and its subcommands.
     *
     * @param sender the command sender requesting tab completion
     * @param alias  the alias of the command being completed
     * @param args   the current command arguments typed by the sender
     * @return a list of possible completions for the current argument
     * @throws IllegalArgumentException if arguments are invalid
     */
    @Nonnull
    @Override
    public List<String> tabComplete(@Nonnull final CommandSender sender, @Nonnull final String alias, @Nonnull final String[] args) throws IllegalArgumentException {
        final MainCommandHandler commandHandler = commandRegister.getCommand(alias);

        if (commandHandler != null) {
            CommandProperty mainCommand = commandHandler.getMainCommand();
            if (mainCommand != null) {
                final List<String> tabComplete = mainCommand.executeTabComplete(sender, alias, args);
                return tabComplete != null && checkPermission(sender, mainCommand) ? tabComplete : new ArrayList<>();
            }
            if (args.length > 0) {
                final CommandProperty subcommand = commandHandler.getCommandBuilder(args[0], true);
                if (subcommand == null) return new ArrayList<>();
                if (args.length == 1) {
                    return tabCompleteSubcommands(sender, commandHandler, args[0]);
                }
                final List<String> tabComplete = subcommand.executeTabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
                return tabComplete != null && checkPermission(sender, subcommand) ? tabComplete : new ArrayList<>();
            }
            return new ArrayList<>();
        }

        if (args.length > 0) {
            final CommandProperty subcommand = commandRegister.getCommandBuilder(args[0], true);
            if (subcommand == null) return new ArrayList<>();
            if (args.length == 1) return tabCompleteSubcommands(sender, args[0], subcommand.isHideLabel());
            final List<String> tabComplete = subcommand.executeTabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
            return tabComplete != null && checkPermission(sender, subcommand) ? tabComplete : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<String> tabComplete(@Nonnull final CommandSender sender, @Nonnull final String alias, @Nonnull final String[] args, @Nullable final Location location) throws IllegalArgumentException {
        return tabComplete(sender, alias, args);
    }

    /**
     * Send sub command.
     *
     * @param sender       the sender of the command.
     * @param commandLabel the label of the sub command.
     */
    public void sendSubDescription(final CommandSender sender, final String commandLabel) {
        for (final CommandProperty subcommand : commandRegister.getCommands()) {
            if (isSendLabelMessage(sender, subcommand)) continue;
            sender.sendMessage(placeholders(subcommand.getDescription(), commandLabel, subcommand));
        }
    }

    private boolean sendHelpMessage(@NonNull final MainCommandHandler commandHandler, @NonNull final CommandSender sender, @NonNull final String commandLabel, @NonNull final String[] args, final boolean executeCommand) {
        final CommandProperty mainCommand = commandHandler.getMainCommand();
        if (!executeCommand || args.length == 0) {
            final String[] usageMessage = mainCommand.getUsageMessage();
            if (usageMessage.length > 0) {
                sender.sendMessage((colors(placeholders(usageMessage, commandLabel, mainCommand))));
                return true;
            }
            return true;
        }
        return descriptionMessage(sender, commandLabel, args, mainCommand);
    }

    private boolean descriptionMessage(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String[] args, CommandProperty mainCommand) {
        if (args.length == 0)
            return false;
        final String lastArg = args[args.length - 1];
        if (lastArg.endsWith("?") || lastArg.endsWith("help") || hasCustomKeyWorld(mainCommand, lastArg)) {
            String[] commandDescription = mainCommand.getDescription();
            sender.sendMessage(placeholders(commandDescription, commandLabel, mainCommand));
            return true;
        }
        return false;
    }

    private boolean checkPermission(final CommandSender sender, final CommandProperty commandBuilder) {
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
        for (final CommandProperty subcommand : commandRegister.getCommands()) {
            final Set<String> setOfLabels = subcommand.getCommandLabels();
            if (!checkPermission(sender, subcommand) && overridePermission) {
                continue;
            }
            for (String label : setOfLabels) {
                if (!label.trim().isEmpty() && label.startsWith(param)) tab.add(label);
            }
        }
        return tab;
    }

    private List<String> tabCompleteSubcommands(@Nonnull final CommandSender sender, @Nonnull final MainCommandHandler subcommandsList, @Nonnull String param) {
        param = param.toLowerCase();
        final List<String> tab = new ArrayList<>();
        for (final CommandProperty subcommand : subcommandsList.getSubcommands()) {
            final Set<String> setOfLabels = subcommand.getCommandLabels();
            if (!checkPermission(sender, subcommand) && subcommand.isHideLabel()) {
                continue;
            }
            for (String label : setOfLabels) {
                if (!label.trim().isEmpty() && label.startsWith(param)) tab.add(label);
            }
        }
        return tab;
    }

    private boolean handleSubCommand(@NonNull final CommandSender sender, @NonNull final String commandLabel, @NonNull final MainCommandHandler commandHandler, @NonNull final String[] args) {
        if (commandHandler.isSubCommandsSet()) return false;

        if (args.length == 0) {
            this.sendMessage(sender, commandHandler, commandLabel);
            return false;
        }

        final CommandProperty subCommand = commandHandler.getCommandBuilder(args[0]);
        if (subCommand != null) {
            if (this.sendNoPermission(sender, commandLabel, subCommand)) return false;
            if (this.sendDescription(sender, commandLabel, args, subCommand))
                return false;

            final boolean executeCommand = subCommand.executeCommand(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
            this.sendUsageMessage(sender, commandLabel, subCommand, executeCommand);
            return executeCommand;
        }
        return false;
    }

    private boolean isSendLabelMessage(final CommandSender sender, final CommandProperty subcommand) {
        if (subcommand.isHideLabel() && !checkPermission(sender, subcommand)) {
            return true;
        }
        return subcommand.getDescription()[0].isEmpty();
    }

    private void sendMessage(final CommandSender sender, final String commandLabel) {
        final List<String> helpPrefixMessage = commandRegister.getPrefixMessage();
        if (helpPrefixMessage != null && !helpPrefixMessage.isEmpty())
            sender.sendMessage(colors(helpPrefixMessage.toArray(new String[0])));

        final String commandLabelMessage = commandRegister.getCommandLabelMessage();
        final String labelMessageNoPerms = commandRegister.getCommandLabelMessageNoPerms();
        if (labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty() && !permissionCheck(sender, commandRegister.getCommandLabelPermission())) {
            sender.sendMessage(colors(placeholders(labelMessageNoPerms, commandLabel, (CommandProperty) null)));

        } else if (commandLabelMessage != null && !commandLabelMessage.isEmpty()) {
            sendToSender(sender, commandLabel, commandLabelMessage, labelMessageNoPerms);
        }
        final List<String> helpSuffixMessage = commandRegister.getSuffixMessage();
        if (helpSuffixMessage != null && !helpSuffixMessage.isEmpty())
            sender.sendMessage(colors(helpSuffixMessage.toArray(new String[0])));
    }

    private void sendToSender(final CommandSender sender, final String commandLabel, final String commandLabelMessage, final String labelMessageNoPerms) {
        for (final CommandProperty subcommand : commandRegister.getCommands()) {
            if (subcommand.isHideLabel() && !checkPermission(sender, subcommand)) {
                continue;
            }
            if (!checkPermission(sender, subcommand) && labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty()) {
                sender.sendMessage(colors(placeholders(new String[]{labelMessageNoPerms}, commandLabel, subcommand)));
            }
            if (checkPermission(sender, subcommand)) {
                sender.sendMessage(colors(placeholders(new String[]{labelMessageNoPerms}, commandLabel, subcommand)));
            }
        }
    }

    private void sendMessage(@Nonnull final CommandSender sender, final @NonNull MainCommandHandler commandHandler, @Nonnull final String commandLabel) {
        final CommandDisplayConfig commandDisplayConfig = commandHandler.getCommandDisplayConfig();
        final List<String> prefixMessages = commandDisplayConfig.getPrefixMessage();
        final String commandLabelMessage = commandDisplayConfig.getCommandLabelMessage();
        final String labelMessageNoPerms = commandDisplayConfig.getCommandLabelMessageNoPerms();



        if (prefixMessages != null && !prefixMessages.isEmpty()) {
            sender.sendMessage(colors(prefixMessages.toArray(new String[0])));
        }

        if (labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty() && !permissionCheck(sender, commandDisplayConfig.getCommandLabelPermission())) {
            sender.sendMessage(colors(placeholders(labelMessageNoPerms, commandLabel, commandDisplayConfig)));
        } else if (commandLabelMessage != null && !commandLabelMessage.isEmpty()) {
            sendBody(sender, commandLabel, commandHandler, labelMessageNoPerms);
        }

        final List<String> helpSuffixMessage = commandDisplayConfig.getSuffixMessage();
        if (helpSuffixMessage != null && !helpSuffixMessage.isEmpty()) {
            sender.sendMessage(colors(helpSuffixMessage.toArray(new String[0])));
        }
    }


    private void sendBody(final CommandSender sender, final String commandLabel, @NonNull final MainCommandHandler commandHandler, final String labelMessageNoPerms) {
        final CommandDisplayConfig commandDisplayConfig = commandHandler.getCommandDisplayConfig();
        final String commandLabelMessage = commandDisplayConfig.getCommandLabelMessage();

        for (final CommandProperty subcommand : commandHandler.getSubcommands()) {
            if (subcommand.isHideLabel() && !checkPermission(sender, subcommand)) {
                continue;
            }
            if (!checkPermission(sender, subcommand) && labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty()) {
                sender.sendMessage(colors(placeholders(new String[]{labelMessageNoPerms}, commandLabel, subcommand)));
            }
            if (checkPermission(sender, subcommand)) {
                sender.sendMessage(colors(placeholders(new String[]{commandLabelMessage}, commandLabel, subcommand)));
            }
        }
    }

    private boolean sendDescription(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] args, @Nonnull final CommandProperty executor) {
        final String[] executorDescription = executor.getDescription();


        if (executorDescription.length > 0) {
            final String lastArg = args[args.length - 1];
            if (lastArg.endsWith("?") || lastArg.endsWith("help") || hasCustomKeyWorld(executor, lastArg)) {
                sender.sendMessage(placeholders(executorDescription, commandLabel, executor));
                return true;
            }
        }
        return false;
    }

    private boolean hasCustomKeyWorld(CommandProperty executor, String lastArg) {
        final String helpKeyword = executor.getHelpKeyword();
        return helpKeyword != null && !helpKeyword.isEmpty() && lastArg.endsWith(helpKeyword);
    }

    private String[] placeholders(@Nullable final String[] messages, @Nonnull final String commandLabel, @Nullable final CommandProperty subcommand) {
        if (messages == null) return new String[]{""};
        String permission = subcommand != null ? subcommand.getPermission() : null;
        if (permission == null) permission = "";
        String[] string = new String[messages.length];

        for (int i = 0; i < messages.length; i++) {
            string[i] = messages[i].replace("{label}", "/" + commandLabel + (subcommand != null ? " " + this.formatSet(subcommand.getCommandLabels()) : "")).replace("{perm}", permission);
        }

        return string;
    }

    private String[] placeholders(@Nullable final String message, @Nonnull final String commandLabel, @Nullable final CommandProperty subcommand) {
        if (message == null) return new String[]{""};
        String permission = subcommand != null ? subcommand.getPermission() : null;
        if (permission == null) permission = "";

        return new String[]{message.replace("{label}", "/" + commandLabel + (subcommand != null ? " " + this.formatSet(subcommand.getCommandLabels()) : "")).replace("{perm}", permission)};
    }

    private String[] placeholders(@Nullable final String message, @Nonnull final String commandLabel, @Nonnull final CommandDisplayConfig displayConfig) {
        if (message == null) return new String[]{""};
        final String labelPermission = displayConfig.getCommandLabelPermission();
        String permission = labelPermission != null && !labelPermission.isEmpty() ? labelPermission : null;
        if (permission == null) permission = "";

        return new String[]{message.replace("{label}", "/" + commandLabel).replace("{perm}", permission)};
    }

    private String[] placeholders(String[] messages, @NonNull String commandLabel, CommandDisplayConfig commandDisplayConfig) {
        if (messages == null) return new String[]{""};
        String permission = commandDisplayConfig != null ? commandDisplayConfig.getCommandLabelPermission() : null;
        if (permission == null) permission = "";
        String[] string = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            string[i] = messages[i].replace("{label}", "/" + commandLabel).replace("{perm}", permission);
        }
        return string;
    }

    /**
     * Translate colors on a text.
     *
     * @param messages the message to check the colors.
     * @return Arrayy of strings that has formated colors.
     */
    public String[] colors(final String[] messages) {
        if (messages == null) return new String[]{""};
        String[] string = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            string[i] = TextTranslator.toSpigotFormat(messages[i]);
        }
        return string;
    }

    private boolean sendNoPermission(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final CommandProperty executor) {
        if (!checkPermission(sender, executor)) {
            String permissionMessage = executor.getPermissionMessage();
            if (permissionMessage != null)
                sender.sendMessage(colors(placeholders(permissionMessage, commandLabel, executor)));
            return true;
        }
        return false;
    }

    private boolean sendDescriptions(CommandSender sender, String commandLabel, String[] args) {
        final List<String> descriptions = commandRegister.getDescriptions();
        if (args.length == 1 && descriptions != null && !descriptions.isEmpty()) {
            final String lastArg = args[args.length - 1];
            if ((lastArg.endsWith("?") || lastArg.endsWith("help"))) {
                sender.sendMessage(placeholders(descriptions.toArray(new String[0]), commandLabel, (CommandProperty) null));
                return false;
            }
        }
        return true;
    }

    private void sendUsageMessage(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final CommandProperty executor, final boolean executeCommand) {
        String[] message = executor.getUsageMessage();
        if (message.length > 0 && !executeCommand) {
            sender.sendMessage(colors(placeholders(message, commandLabel, executor)));
        }
    }

    @Nonnull
    private String formatSet(Set<String> labels) {
        StringBuilder labelsFormatted = new StringBuilder();
        for (String label : labels) {
            labelsFormatted.append(label)
                    .append(", ");
        }
        labelsFormatted.setLength(labelsFormatted.length() - 2);
        return labelsFormatted.toString();
    }
}
