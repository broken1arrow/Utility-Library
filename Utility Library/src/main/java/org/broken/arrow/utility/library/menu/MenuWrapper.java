package org.broken.arrow.utility.library.menu;

import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Wraps the menu logic.
 */
public class MenuWrapper extends RegisterMenuAPI {

    /**
     * Constructs the API instance using the provided plugin.
     * <p>
     * Automatically registers this instance as the singleton instance.
     * Performs version checks and event registration.
     * </p>
     *
     * @param plugin the plugin instance to associate with this API.
     */
    public MenuWrapper(@Nonnull final Plugin plugin) {
        super(plugin);
    }

    /**
     * Constructs the API instance using the provided plugin.
     * <p>
     * Automatically registers this instance as the singleton instance.
     * Performs version checks and event registration.
     * </p>
     *
     * @param plugin        the plugin instance to associate with this API.
     * @param turnOffLogger true to disable version check logging, false to enable.
     */
    public MenuWrapper(@Nonnull final Plugin plugin, boolean turnOffLogger) {
        super(plugin, turnOffLogger);
    }

    @Override
    protected void registerMenuEvent(Plugin plugin) {
    }


}
