package org.broken.arrow.library.command.builers;

import org.broken.arrow.library.command.commandhandler.MainCommandHandler;
import org.broken.arrow.library.command.subcommand.CommandDisplayConfig;


import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Final configuration stage for applying display-related settings
 * to a command before returning to the main configuration flow.
 *
 * <p>This stage is typically entered after subcommand registration.</p>
 */
public class CommandDisplayBuilder {
    private final CommandBuilder commandRegister;
    private final MainCommandHandler mainCommandHandler;

    /**
     * Creates a new display builder.
     *
     * @param commandRegister    parent command builder
     * @param mainCommandHandler internal command handler
     */
    public CommandDisplayBuilder(@Nonnull final CommandBuilder commandRegister, @Nonnull final MainCommandHandler mainCommandHandler) {
        this.commandRegister = commandRegister;
        this.mainCommandHandler = mainCommandHandler;
    }

    /**
     * Applies display configuration to the command.
     *
     * <pre>{@code
     * display(config -> {
     *     config.setDescription(
     *                 "Brief explanation of the command and its usage",
     *                 "including subcommand permissions.")
     *            .setCommandLabelMessageNoPerms("&fUsage: &6<{label}>&f (perm: &6{perm}&f)")
     *            .setCommandLabelMessage("&fUsage: &6<{label}>")
     *            .setPrefixMessage("&f---------------&8<&6Command info&8>&f---------------")
     *            .setSuffixMessage("&f---------------&8<&6Command info&8>&f---------------");
     * });
     * }</pre>
     *
     * @param callback consumer used to configure {@link CommandDisplayConfig}
     * @return the original {@link MainCommandHandler} builder to continue configuration
     */
    public MainCommandHandler display(final Consumer<CommandDisplayConfig> callback) {
        callback.accept(mainCommandHandler.getCommandDisplayConfig());
        return mainCommandHandler;
    }

}
