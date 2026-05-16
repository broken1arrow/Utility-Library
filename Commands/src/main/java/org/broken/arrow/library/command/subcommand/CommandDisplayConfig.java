package org.broken.arrow.library.command.subcommand;

import java.util.Arrays;
import java.util.List;

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
     * Use {label} to replace it with the command name.
     *
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
     * Returns the description of the command. The description could provide information about the main command
     * and/or brief explanation to the subcommands. Player then add a "?" or "help" at the end of the command to
     * request additional information about the command.
     *
     * @return The description.
     */
    public List<String> getDescriptions() {
        return descriptions;
    }

    /**
     * Set the description for your command.
     *
     * @param descriptions the description for your command.
     * @return this class for chaining.
     */
    public CommandDisplayConfig setDescription(final String... descriptions) {
        this.descriptions = Arrays.asList(descriptions);
        return this;
    }

    /**
     * Get the message if player not have the permission.
     *
     * @return the message or null.
     */
    public String getCommandLabelMessageNoPerms() {
        return commandLabelMessageNoPerms;
    }

    /**
     * Use {label} to replace it with the command name and {perm} to get permission. Used if you not have permission.
     *
     * @param commandLabelMessage the message send for every subcommand.
     * @return this class for chaining.
     */
    public CommandDisplayConfig setCommandLabelMessageNoPerms(String commandLabelMessage) {
        this.commandLabelMessageNoPerms = commandLabelMessage;
        return this;
    }

    /**
     * Get the permission for use the main command.
     *
     * @return the permission or null if not set.
     */
    public String getCommandLabelPermission() {
        return commandLabelPermission;
    }

    /**
     * Set the permission used.
     *
     * @param commandLabelPermission the permission
     * @return this class.
     */
    public CommandDisplayConfig setCommandLabelPermission(final String commandLabelPermission) {
        this.commandLabelPermission = commandLabelPermission;
        return this;
    }
}
