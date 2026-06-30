package org.broken.arrow.library.command;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.command.command.CommandProperty;
import org.broken.arrow.library.command.commandhandler.CommandExecutor;
import org.broken.arrow.library.command.commandhandler.CommandRegistering;
import org.broken.arrow.library.command.commandhandler.MainCommandHandler;
import org.broken.arrow.library.command.builers.CommandBuilder;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.print.attribute.standard.ColorSupported;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A utility class for registering and managing registered commands.The commandRegister provides
 * methods for registering subcommands, setting command label messages and permissions,
 * retrieving sub-commands, and registering the main command label.
 */
public class CommandRegister implements CommandRegistering {
    private final Logging log = new Logging(CommandRegister.class);
    private final Map<String, MainCommandHandler> commands = new ConcurrentHashMap<>();
    private static boolean hasColorLib;
    private boolean registeredMainCommand;

    /**
     * Create new instance where it also checks if the color translator is included
     * when shaded
     */
    public CommandRegister() {
        try {
            TextTranslator.getInstance();
            hasColorLib = true;
        } catch (NoClassDefFoundError | NoSuchMethodError exception) {
            hasColorLib = false;
        }
    }

    @Override
    public CommandBuilder registerCommand(@Nonnull final Plugin plugin, @Nonnull final String mainCommand) {
        final CommandBuilder commandBuilder = new CommandBuilder();
        final String[] mainCommands = mainCommand.split("\\|");

        if (mainCommands.length > 0) {
            for (String main : mainCommands) {
                if (this.isCommandRegistered(main)) continue;
                this.registerCommand(plugin, main, commandBuilder);
            }
            return commandBuilder;
        }
        if (this.isCommandRegistered(mainCommand)) return commandBuilder;
        this.registerCommand(plugin, mainCommand, commandBuilder);
        return commandBuilder;
    }


    @Override
    public void registerCommand(@Nonnull final Plugin plugin, @Nonnull final String mainCommand, @Nonnull final Consumer<CommandBuilder> callback) {
        final String[] mainCommands = mainCommand.split("\\|");
        final CommandBuilder commandBuilder = new CommandBuilder();
        callback.accept(commandBuilder);
        if (mainCommands.length > 0) {
            for (String main : mainCommands) {
                if (this.isCommandRegistered(main)) continue;
                this.registerCommand(plugin, main, commandBuilder);
            }
            return;
        }
        if (this.isCommandRegistered(mainCommand)) return;
        this.registerCommand(plugin, mainCommand, commandBuilder);
    }

    @Override
    public MainCommandHandler getCommand(@Nonnull final String command) {
        return commands.get(command.toLowerCase(Locale.ROOT));
    }

    /**
     * Set if you want to prevent creation of commands after you register your commands.
     *
     * @param registeredMainCommand set to {@code true} if you want to block new commands be set after you set yours.
     */
    public void setRegisteredMainCommand(final boolean registeredMainCommand) {
        this.registeredMainCommand = registeredMainCommand;
    }

    /**
     * Translate colors on a text.
     *
     * @param message the message to translate the color codes.
     * @return A string that has formated colors.
     */
    public static String translateColors(final String message) {
        if (message == null) return "";
        if (hasColorLib)
            return TextTranslator.toSpigotFormat(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Register the command both to both cache and server.
     *
     * @param plugin         the plugin to register
     * @param main           The main command to register.
     * @param commandBuilder The CommandRegistering instance.
     */
    private void registerCommand(@Nonnull final Plugin plugin, @Nonnull final String main, @Nonnull final CommandBuilder commandBuilder) {
        commands.compute(main, (commandLabel, mainCommandHandler) -> {
            if (mainCommandHandler != null) {
                return null;
            }
            this.registerMainCommand(plugin.getName().toLowerCase(Locale.ROOT), main);
            return commandBuilder.getMainCommandHandler();
        });
    }

    /**
     * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
     * If the main command has already been registered, this method does nothing.
     *
     * @param fallbackPrefix The prefix to use if the normal command cannot be used.
     * @param mainCommand    The main command to register.
     * @return The CommandRegistering instance.
     */
    private CommandRegistering registerMainCommand(@Nonnull final String fallbackPrefix, @Nonnull final String mainCommand) {
        return this.registerMainCommand(fallbackPrefix, mainCommand, new String[0]);
    }

    /**
     * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
     * If the main command has already been registered, this method does nothing.
     *
     * @param fallbackPrefix The prefix to use if the normal command cannot be used.
     * @param mainCommand    The main command to register.
     * @param aliases        The aliases of the main command.
     * @return The CommandRegistering instance.
     */
    private CommandRegistering registerMainCommand(@Nonnull final String fallbackPrefix, @Nonnull final String mainCommand, @Nonnull final String... aliases) {
        final String description = "This is the command registered: " + mainCommand;
        final String usageMessage = "usage for command/" + mainCommand;
        return this.registerMainCommand(fallbackPrefix, mainCommand, description, usageMessage, aliases);
    }

    /**
     * Registers the main command with the specified fallback prefix, command, description, usage message, and aliases.
     * If the main command has already been registered, this method does nothing.
     *
     * @param fallbackPrefix The prefix to use if the normal command cannot be used.
     * @param mainCommand    The main command to register.
     * @param description    The description of the main command.
     * @param usageMessage   The usage message of the main command.
     * @param aliases        The aliases of the main command.
     * @return The CommandRegistering instance.
     */
    private CommandRegistering registerMainCommand(@Nonnull final String fallbackPrefix, @Nonnull final String mainCommand, @Nonnull final String description, @Nonnull final String usageMessage, @Nonnull final String... aliases) {
        if (registeredMainCommand) return this;

        this.register(fallbackPrefix, new CommandExecutor(this, mainCommand, description, usageMessage, Arrays.asList(aliases)));
        return this;
    }


    /**
     * Registers the specified command with the provided fallback prefix.
     * This method uses reflection to access the commandMap and register the command.
     *
     * @param fallbackPrefix The fallback prefix to use if the normal command cannot be used.
     * @param command        The command to register.
     */
    private void register(@Nonnull final String fallbackPrefix, @Nonnull final Command command) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            final CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(fallbackPrefix, command);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            log.log(e, () -> "It failed to register your command");
        }
    }

    private boolean isCommandRegistered(@NonNull final String mainCommand) {
        final MainCommandHandler commandHandler = commands.get(mainCommand);
        if (commandHandler != null) {
            final CommandProperty command = commandHandler.getMainCommand();
            final Collection<CommandProperty> commands = commandHandler.getSubcommands();
            if (command != null)
                log.log(() -> "The command is already registered: '" + mainCommand + "' and have this command registered:" + command);
            if (commands != null)
                log.log(() -> "The command is already registered: '" + mainCommand + "' and have this sub commands registered: '" + commands + "'");
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CommandRegister that = (CommandRegister) o;
        return registeredMainCommand == that.registeredMainCommand && Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commands, registeredMainCommand);
    }
}
