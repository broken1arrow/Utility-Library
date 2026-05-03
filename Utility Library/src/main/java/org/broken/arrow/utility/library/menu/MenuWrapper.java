package org.broken.arrow.utility.library.menu;

import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class MenuWrapper extends RegisterMenuAPI {

    public MenuWrapper(@Nonnull final Plugin plugin) {
        super(plugin);
    }

    public MenuWrapper(@Nonnull final Plugin plugin, boolean turnOffLogger) {
        super(plugin, turnOffLogger);
    }

    @Override
    protected void registerMenuEvent(Plugin plugin) {
    }


}
