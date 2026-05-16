package org.broken.arrow.library.command.subcommand;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for command display output.
 *
 * <p>This class defines all visual and informational elements shown when a command
 * or its subcommands are displayed to the player (e.g. help output, labels, headers).</p>
 */
public class CommandDisplayConfig {

    private String commandLabelMessage;
    private String commandLabelMessageNoPerms;
    private String commandLabelPermission;
    private List<String> prefixMessage;
    private List<String> suffixMessage;
    private List<String> descriptions;
    /**
     * Returns the message to display as the command label.
     *
     * @return The command label message.
     */
    public String getCommandLabelMessage() {
        return commandLabelMessage;
    }

    /**
     * Sets the message to display as the command label.
     * <p>Supports placeholders:
     * <ul>
     *     <li>{label} - command label</li>
     *     <li>{perm} - required permission</li>
     * </ul>
     * </p>
     * @param commandLabelMessage The command label message to set.
     * @return The CommandRegister instance.
     */
    public CommandDisplayConfig setCommandLabelMessage(String commandLabelMessage) {
        this.commandLabelMessage = commandLabelMessage;
        return this;
    }

    /**
     * Returns the list of prefix messages to display in the command help.
     *
     * @return The list of prefix messages.
     */
    public List<String> getPrefixMessage() {
        return prefixMessage;
    }

    /**
     * Sets the prefix messages to display in the command help using the provided string values.
     *
     * @param prefixMessage The prefix messages to set.
     * @return The CommandConfig instance.
     */
    public CommandDisplayConfig setPrefixMessage(String... prefixMessage) {
        this.prefixMessage = Arrays.asList(prefixMessage);
        return this;
    }

    /**
     * Sets the prefix messages to display in the command help using the provided list of strings.
     *
     * @param prefixMessage The prefix messages to set.
     * @return The CommandConfig instance.
     */
    public CommandDisplayConfig setPrefixMessage(List<String> prefixMessage) {
        this.prefixMessage = prefixMessage;
        return this;
    }

    /**
     * Returns the list of suffix messages to display in the command help.
     *
     * @return The list of suffix messages.
     */
    public List<String> getSuffixMessage() {
        return suffixMessage;
    }

    /**
     * Sets the suffix messages to display in the command help using the provided string values.
     *
     * @param suffixMessage The suffix messages to set.
     * @return this class for chaining.
     */
    public CommandDisplayConfig setSuffixMessage(String... suffixMessage) {
        this.suffixMessage = Arrays.asList(suffixMessage);
        return this;
    }

    /**
     * Sets the suffix messages to display in the command using the provided list of strings.
     *
     * @param suffixMessage The suffix messages to set.
     * @return this class for chaining.
     */
    public CommandDisplayConfig setSuffixMessage(List<String> suffixMessage) {
        this.suffixMessage = suffixMessage;
        return this;
    }

    /**
     * Returns the descriptions displayed in command help output.
     *
     * <p>These describe the command or its subcommands depending on context.</p>
     *
     * @return list of description lines
     */
    public List<String> getDescription() {
        return descriptions;
    }

    /**
     * Sets the description lines for the command display.
     *
     * @param descriptions description text lines
     * @return this configuration instance for chaining
     */
    public CommandDisplayConfig setDescription(final String... descriptions) {
        this.descriptions = Arrays.asList(descriptions);
        return this;
    }

    /**
     * Returns the message displayed when a player lacks permission.
     *
     * @return no-permission message format
     */
    public String getCommandLabelMessageNoPerms() {
        return commandLabelMessageNoPerms;
    }

    /**
     * Sets the message shown when a player lacks permission.
     *
     * <p>Supports placeholders:
     * <ul>
     *     <li>{label} - command label</li>
     *     <li>{perm} - required permission</li>
     * </ul>
     * </p>
     *
     * @param commandLabelMessage message format for missing permission
     * @return this configuration instance for chaining
     */
    public CommandDisplayConfig setCommandLabelMessageNoPerms(String commandLabelMessage) {
        this.commandLabelMessageNoPerms = commandLabelMessage;
        return this;
    }

    /**
     * Returns the permission required for the command label.
     *
     * @return permission string or null if not set
     */
    public String getCommandLabelPermission() {
        return commandLabelPermission;
    }

    /**
     * Sets the permission required to use the command.
     *
     * @param commandLabelPermission permission node
     * @return this configuration instance for chaining
     */
    public CommandDisplayConfig setCommandLabelPermission(final String commandLabelPermission) {
        this.commandLabelPermission = commandLabelPermission;
        return this;
    }
}
