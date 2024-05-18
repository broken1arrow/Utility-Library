package org.broken.arrow.command.library.command;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code CommandProperty} class is used to define and manage the properties of a command.
 * It allows you to set various properties for your command, such as permissions, descriptions,
 * permission messages, usage messages, and visibility settings. This class also provides methods
 * to handle the execution of the command and to provide tab completion suggestions.
 *
 * <h2>Properties</h2>
 * <ul>
 *     <li><b>Permission:</b> The required permission node to execute the command.</li>
 *     <li><b>Description:</b> A brief description of what the command does.</li>
 *     <li><b>Permission Message:</b> The message displayed when a user lacks the necessary permission.</li>
 *     <li><b>Usage Messages:</b> Instructions on how to use the command.</li>
 *     <li><b>Help Keyword:</b> The trigger word that provides assistance on how to use the sub-command.</li>
 *     <li><b>Hide Label:</b> A flag to determine whether the command label should be hidden.</li>
 * </ul>
 *
 * <h2>Methods</h2>
 * <ul>
 *     <li><b>execute:</b> The method to be called when the command is executed.</li>
 *     <li><b>tabSuggestions:</b> The method to be called to provide tab completion suggestions.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * CommandProperty myCommand = new CommandProperty();
 * myCommand.setPermission("myplugin.command.use");
 * myCommand.setDescription("This command does something cool.");
 * myCommand.setPermissionMessage("You do not have permission to use this command.");
 * myCommand.setUsageMessages("/mycommand <args>");
 * myCommand.setHelpKeyword("help");
 * myCommand.setHideLabel(false);
 * }
 * </pre>
 *
 */
public class CommandProperty  {

    private final Set<String> commandLabels = new HashSet<>();
    private String description;
    private String permission;
    private String permissionMessage;
    private String helpKeyword;
    private List<String> usageMessages;
    private boolean hideLabel;

    /**
     * Constructs your command with this class.
     *
     * @param commandLabel The different labels for your command. At least one label must be provided.
     * @throws IllegalArgumentException if no command labels are provided.
     */
    public CommandProperty(String... commandLabel) {
        if (commandLabel == null || commandLabel.length == 0) {
            throw new IllegalArgumentException("At least one command label must be provided.");
        }
        commandLabels.addAll(Arrays.asList(commandLabel));
    }

    /**
     * Sets a list of messages to suggest to the player how to use the command. These usage messages provide guidance on how to properly
     * use the command and its arguments.
     * Note: When you use the {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
     * method and set it to false to indicate that the specified usage message or messages should be displayed.
     *
     * @param usageMessages The array of usage messages.
     * @return The class instance.
     */
    public CommandProperty setUsageMessages(final String... usageMessages) {
        this.usageMessages = Arrays.asList(usageMessages);
        return this;
    }

    /**
     * add the command label.
     *
     * @param label The label to execute your sub-command.
     */
    public void addCommandLabel(String label) {
        commandLabels.add(label);
    }

    /**
     * Sets a list of messages to suggest to the player how to use the command. These usage messages provide guidance on how to properly
     * use the command and its arguments.
     * Note: When you use the {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
     * method and it returns false, the specified usage message or messages should be displayed.
     *
     * @param usageMessages The list of usage messages.
     * @return The class instance.
     */
    public CommandProperty setUsageMessages(final List<String> usageMessages) {
        this.usageMessages = usageMessages;
        return this;
    }

    /**
     * Sets the description of the command. The description should provide information about what the command does.
     * Player then add a "?" or "help" at the end of the command to request additional information about the command.
     *
     * @param description The description message that explains what the command does.
     * @return The class instance.
     */
    public CommandProperty setDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the trigger word for suggesting the help message. When players type this string,
     * the help message will be displayed.
     * <p>&nbsp;</p>
     * <p>
     * By default, it will be triggered by "?" or "help".
     * For example, if a player types "/mainCommand sub-label ?" or "/mainCommand sub-label help", it will
     * trigger the {@link #getDescription()} method as long as the description is not empty or null.
     * </p>
     * <p>
     *
     * @param helpKeyword The trigger word for the help message.
     * @return The class instance.
     */
    public CommandProperty setHelpKeyword(final String helpKeyword) {
        this.helpKeyword = helpKeyword;
        return this;
    }

    /**
     * Sets the required permission for the command.
     *
     * @param permission The permission to set.
     * @return The class instance.
     */
    public CommandProperty setPermission(final String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Sets the message to display when the player can't run the command.
     * Use "{perm}" to replace it with the missing permission automatically.
     *
     * @param permissionMessage The permission failure message.
     * @return The class instance.
     */
    public CommandProperty setPermissionMessage(final String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    /**
     * Sets whether to hide the subcommand from tab completion without permission.
     *
     * @param hideLabel Set to true to hide the label from tab completion.
     * @return The class instance.
     */
    public CommandProperty setHideLabel(final boolean hideLabel) {
        this.hideLabel = hideLabel;
        return this;
    }

    /**
     * Returns the description of the command. The description should provide information about what the command does
     * and guidelines how to use it. Players add a "?" or "help" at the end of the command to request the information.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the trigger word for suggesting the help message.
     *
     * @return The help trigger.
     */
    public String getHelpKeyword() {
        return helpKeyword;
    }

    /**
     * Returns the list of usage messages for the command. When use method {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
     * and it return false to indicate that the specified usage message should be displayed.
     *
     * @return The list of usage messages.
     */
    public List<String> getUsageMessages() {
        return usageMessages;
    }

    /**
     * Returns the required permission for the command.
     *
     * @return The permission.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Returns the message to display when the command is executed without the required permission.
     *
     * @return The permission message.
     */
    public String getPermissionMessage() {
        return permissionMessage;
    }

    /**
     * Checks if the command label should be hidden from tab completion without permission.
     *
     * @return True if the label should be hidden, false otherwise.
     */
    public boolean isHideLabel() {
        return hideLabel;
    }

    /**
     * Retrieves the command labels.
     *
     * @return The set of command labels.
     */
    public Set<String> getCommandLabels() {
        return commandLabels;
    }

    /**
     * Checks if the label matches exactly.
     *
     * @param label The command label you want to match.
     * @return The command label if found, or null if not found in the set.
     */
    public String firstLabelMatch(String label) {
        return this.firstLabelMatch(label, false);
    }

    /**
     * Checks if the label matches exactly or starts with the label.
     *
     * @param label     The command label you want to match.
     * @param startWith If true, checks if the label starts with a matching label from the set.
     * @return The command label if found, or null if not found in the set.
     */
    public String firstLabelMatch(String label, boolean startWith) {
        return this.getCommandLabels().stream()
                .filter(listedLabel -> startWith ? listedLabel.startsWith(label) : listedLabel.equals(label))
                .findFirst()
                .orElse(null);
    }

    /**
     * Sorts the labels.
     *
     * @return A list of all labels sorted in alphabetical order.
     */
    public List<String> sortLabels() {
        return commandLabels.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Returns the first label in the sorted list.
     *
     * @return The first label that is first in alphabetical order.
     */
    public String getFirstSortedLabel() {
        List<String> sortedLabels = this.sortLabels();
        return sortedLabels.isEmpty() ? null : sortedLabels.get(0);
    }

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
    public boolean executeCommand(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArg) {
        return false;
    }

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
    public List<String> executeTabComplete(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        CommandProperty that = (CommandProperty) object;

        if (hideLabel != that.hideLabel) return false;
        if (!commandLabels.equals(that.commandLabels)) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(permission, that.permission)) return false;
        if (!Objects.equals(permissionMessage, that.permissionMessage))
            return false;
        if (!Objects.equals(helpKeyword, that.helpKeyword)) return false;
        return Objects.equals(usageMessages, that.usageMessages);
    }

    @Override
    public int hashCode() {
        int result = commandLabels.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        result = 31 * result + (permissionMessage != null ? permissionMessage.hashCode() : 0);
        result = 31 * result + (helpKeyword != null ? helpKeyword.hashCode() : 0);
        result = 31 * result + (usageMessages != null ? usageMessages.hashCode() : 0);
        result = 31 * result + (hideLabel ? 1 : 0);
        return result;
    }
}
