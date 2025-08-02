package org.broken.arrow.library.itemcreator;

import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class NBTManger {
    private RegisterNbtAPI nbtApi;

    public NBTManger(@Nullable final Plugin plugin, final boolean turnOffLogger) {
        if (plugin == null)
            return;
        try {
            nbtApi = new RegisterNbtAPI(plugin, turnOffLogger);
        } catch (NoClassDefFoundError ignore) {
            nbtApi = null;
        }
    }

    @Nullable
    public RegisterNbtAPI getNbtApi() {
        return nbtApi;
    }
}
