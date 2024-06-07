package org.broken.arrow.command.library.commandhandler;

import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.command.library.CommandRegister;
import org.broken.arrow.command.library.command.CommandProperty;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandExecutor extends Command {

    private final CommandRegister commandRegister;

    public CommandExecutor(@Nonnull CommandRegister commandRegister, @Nonnull final String name, @Nonnull final String description, @Nonnull final String usageMessage, @Nonnull final List<String> aliases) {
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

            if (!this.sendDescriptions(sender, commandLabel, args))
                return false;
            final CommandProperty executor = commandRegister.getCommandBuilder(args[0]);
            if (executor != null) {
                if (sendDescription(sender, commandLabel, args, executor)) return false;

                if (sendNoPermission(sender, commandLabel, executor)) return false;

                boolean executeCommand = executor.executeCommand(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
                sendUsageMessage(sender, commandLabel, executor, executeCommand);
            }
        }
        return false;
    }


    @Nonnull
    @Override
    public List<String> tabComplete(@Nonnull final CommandSender sender, @Nonnull final String alias, @Nonnull final String[] args) throws IllegalArgumentException {
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

    public void sendSubDescription(final CommandSender sender, final String commandLabel) {
        for (final CommandProperty subcommand : commandRegister.getCommands()) {
            if (isSendLabelMessage(sender, subcommand)) continue;
            sender.sendMessage(placeholders(subcommand.getDescription(), commandLabel, subcommand));
        }
    }

    private boolean isSendLabelMessage(final CommandSender sender, final CommandProperty subcommand) {
        if (subcommand.isHideLabel() && !checkPermission(sender, subcommand)) {
            return true;
        }
        return subcommand.getDescription() == null || subcommand.getDescription().isEmpty();
    }

    private void sendMessage(final CommandSender sender, final String commandLabel) {

        final List<String> helpPrefixMessage = commandRegister.getPrefixMessage();
        if (helpPrefixMessage != null && !helpPrefixMessage.isEmpty())
            for (final String prefixMessage : helpPrefixMessage)
                sender.sendMessage(colors(prefixMessage));

        final String commandLabelMessage = commandRegister.getCommandLabelMessage();
        final String labelMessageNoPerms = commandRegister.getCommandLabelMessageNoPerms();
        if (labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty() && !permissionCheck(sender, commandRegister.getCommandLabelPermission())) {
            sender.sendMessage(colors(placeholders(labelMessageNoPerms, commandLabel, null)));

        } else if (commandLabelMessage != null && !commandLabelMessage.isEmpty()) {
            sendToSender(sender, commandLabel, commandLabelMessage, labelMessageNoPerms);
        }
        final List<String> helpSuffixMessage = commandRegister.getSuffixMessage();
        if (helpSuffixMessage != null && !helpSuffixMessage.isEmpty())
            for (final String suffixMessage : helpSuffixMessage)
                sender.sendMessage(colors(suffixMessage));
    }

    private void sendToSender(final CommandSender sender, final String commandLabel, final String commandLabelMessage, final String labelMessageNoPerms) {
        for (final CommandProperty subcommand : commandRegister.getCommands()) {
            if (subcommand.isHideLabel() && !checkPermission(sender, subcommand)) {
                continue;
            }
            if (!checkPermission(sender, subcommand) && labelMessageNoPerms != null && !labelMessageNoPerms.isEmpty()) {
                sender.sendMessage(colors(placeholders(labelMessageNoPerms, commandLabel, subcommand)));
            }
            if (checkPermission(sender, subcommand)) {
                sender.sendMessage(colors(placeholders(commandLabelMessage, commandLabel, subcommand)));
            }
        }
    }

    private boolean sendDescription(@Nonnull CommandSender sender, @Nonnull String commandLabel,@Nonnull String[] args, CommandProperty executor) {
        if (executor.getDescription() != null) {
            String arguments = Arrays.toString(args);

            if (arguments.endsWith("?]") || arguments.endsWith("help]")) {
                sender.sendMessage(placeholders(executor.getDescription(), commandLabel, executor));
                return true;
            }
        }
        return false;
    }

    public String placeholders(final String message, final String commandLabel, final CommandProperty subcommand) {
        if (message == null) return "";
        String permission = subcommand != null ? subcommand.getPermission() : null;
        if (permission == null) permission = "";
        return message.replace("{label}", "/" + commandLabel + (subcommand != null ? " " + this.formatSet(subcommand.getCommandLabels()) : "")).replace("{perm}", permission);
    }

    public String colors(final String message) {
        if (message == null) return "";
        return TextTranslator.toSpigotFormat(message);
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
        if (args.length == 1 && descriptions != null && !descriptions.isEmpty() &&
                (Arrays.toString(args).endsWith("?]") || Arrays.toString(args).endsWith("help]"))) {
            for (String description : descriptions)
                sender.sendMessage(placeholders(description, commandLabel, null));
            return false;
        }
        return true;
    }

    private void sendUsageMessage(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final CommandProperty executor, final boolean executeCommand) {
        if (executor.getUsageMessages() != null && !executeCommand)
            for (final String usage : executor.getUsageMessages()) {
                sender.sendMessage(colors(placeholders(usage, commandLabel, executor)));
            }
    }

    @Nonnull
    private String formatSet(Set<String> labels) {
        StringBuilder labelsFormatted = new StringBuilder();
        for (String label : labels) {
            labelsFormatted.append(label)
                    .append(", ");
        }
        labelsFormatted.setLength(labelsFormatted.length()-2);
        return labelsFormatted.toString();
    }
}
