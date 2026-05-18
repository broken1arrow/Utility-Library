package org.broken.arrow.library.command.builers;

import javax.annotation.Nonnull;

/**
 * Base configuration for the main command.
 *
 * <p>Defines metadata used when displaying or executing the root command,
 * including description, usage information, and aliases.</p>
 */
public class CommandOptions {
    private String[] description;
    private String[] usageMessage;
    private String[] aliases;

    /**
     * Sets the command description.
     *
     * @param description human-readable description of the command
     * @return this builder instance for chaining
     */
    public CommandOptions setMainDescription(@Nonnull final String... description) {
        this.description = description;
        return this;
    }


    /**
     * Sets the usage message displayed when the command is used incorrectly or help is requested.
     *
     * @param usageMessage usage instruction string
     * @return this builder instance for chaining
     */
    public CommandOptions setMainUsageMessage(@Nonnull final String... usageMessage) {
        this.usageMessage = usageMessage;
        return this;
    }

    /**
     * Defines alternative aliases for this command.
     *
     * @param aliases alternative command labels
     * @return this builder instance for chaining
     */
    public CommandOptions setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Returns the command description.
     *
     * @return description or empty string if not set
     */
    @Nonnull
    public String[] getMainDescription() {
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
    public String[] getMainUsageMessage() {
        if (usageMessage == null)
            return new String[]{""};
        return usageMessage;
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
