package org.broken.arrow.library.command.builers;

/**
 * Base configuration for the main command.
 *
 * <p>Defines metadata used when displaying or executing the root command,
 * including description, usage information, and aliases.</p>
 */
public class CommandOptions {
    private String description;
    private String usageMessage;
    private String[] aliases;

    /**
     * Sets the description of the main command.
     *
     * <p>This description is used when displaying general command help
     * for the root command entry point.</p>
     *
     * @param description main command description shown in help output
     * @return this builder instance for chaining
     */
    public CommandOptions setMainDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the usage message for the command.
     *
     * @param usageMessage usage instruction text
     * @return this instance for chaining
     */
    public CommandOptions setMainUsageMessage(String usageMessage) {
        this.usageMessage = usageMessage;
        return this;
    }

    /**
     * Sets alternative command aliases.
     *
     * @param aliases alternative command labels
     * @return this instance for chaining
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
    public String getMainDescription() {
        if (description == null)
            return "";
        return description;
    }

    /**
     * Returns the usage message.
     *
     * @return usage message or empty string if not set
     */
    public String getMainUsageMessage() {
        if (usageMessage == null)
            return "";
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
