package org.broken.arrow.library.command.builers;

import org.broken.arrow.library.command.command.CommandProperty;

import javax.annotation.Nonnull;

/**
 * Base messages for the command.
 *
 * <p>Defines metadata used when displaying or executing the root command,
 * including description, usage information, and aliases.</p>
 */
public class CommandMessages {
    private String[] description;
    private String[] usageMessage;
    private String permissionMessage;
    private String[] aliases;


    /**
     * Sets the command description.
     *
     * @param description human-readable description of the command
     * @return this builder instance for chaining
     */
    public CommandMessages setDescription(@Nonnull final String... description) {
        this.description = description;
        return this;
    }


    /**
     * Sets the usage message displayed when the command is used incorrectly or help is requested.
     *
     * @param usageMessage usage instruction string
     * @return this builder instance for chaining
     */
    public CommandMessages setUsageMessage(@Nonnull final String... usageMessage) {
        this.usageMessage = usageMessage;
        return this;
    }


    /**
     * Defines alternative aliases for this command.
     *
     * @param aliases alternative command labels
     * @return this builder instance for chaining
     */
    public CommandMessages setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Sets the message to display when the player can't run the command.
     * Use "{perm}" to replace it with the missing permission automatically.
     *
     * @param permissionMessage The permission failure message.
     * @return The class instance.
     */
    public CommandMessages setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    /**
     * Returns the description of the command. The description should provide information about what the command does
     * and guidelines how to use it. Players add a "?" or "help" at the end of the command to request the information.
     *
     * @return The description.
     */
    @Nonnull
    public String[] getDescription() {
        if (description == null)
            return new String[]{""};
        return description;
    }

    /**
     * Returns the usage message.
     *
     * @return usage message or empty string if not set
     */
    @Nonnull
    public String[] getUsageMessage() {
        if (usageMessage == null)
            return new String[]{""};
        return usageMessage;
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
     * Returns command aliases.
     *
     * @return array of aliases or empty array if none are defined
     */
    public String[] getAliases() {
        if (aliases == null)
            return new String[0];
        return aliases;
    }
}
