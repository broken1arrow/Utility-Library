package org.broken.arrow.library.itemcreator;

import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/**
 * Manages the registration and access of the NBT API for handling
 * NBT data in Itemstack.
 * <p>
 * Attempts to initialize the {@link RegisterNbtAPI} if the required
 * classes are available and the plugin reference is not null.
 * </p>
 */
public class NBTManger {
    private RegisterNbtAPI nbtApi;

    /**
     * Constructs an {@code NBTManger} instance and attempts to initialize
     * the NBT API.
     *
     * @param plugin        The plugin instance required for API registration.
     *                      If {@code null}, the manager will not initialize.
     * @param turnOffLogger Whether to disable the NBT API internal logging.
     */
    public NBTManger(@Nullable final Plugin plugin, final boolean turnOffLogger) {
        if (plugin == null)
            return;
        try {
            nbtApi = new RegisterNbtAPI(plugin, turnOffLogger);
        } catch (NoClassDefFoundError ignore) {
            nbtApi = null;
        }
    }

    /**
     * Returns the {@link RegisterNbtAPI} instance if successfully initialized,
     * or {@code null} if the API is unavailable.
     *
     * @return The NBT API instance or {@code null}.
     */
    @Nullable
    public RegisterNbtAPI getNbtApi() {
        return nbtApi;
    }
}
