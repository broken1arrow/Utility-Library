package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class ServerVersion {

    private float version;

    public ServerVersion(@Nonnull final Plugin plugin) {
        setServerVersion(plugin);
    }

    public float getServerVersion() {
        return version;
    }

    private void setServerVersion(final Plugin plugin) {
        final String[] versionPieces;
        if (plugin == null)
            versionPieces = Bukkit.getServer().getBukkitVersion().split("\\.");
        else
            versionPieces = plugin.getServer().getBukkitVersion().split("\\.");

        final String firstNumber;
        String secondNumber;
        final String firstString = versionPieces[1];
        if (firstString.contains("-")) {
            firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

            secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
            final int index = secondNumber.toUpperCase().indexOf("R");
            if (index >= 0)
                secondNumber = secondNumber.substring(index + 1);
        } else {
            final String secondString = versionPieces[2];
            firstNumber = firstString;
            secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
        }
        version = Float.parseFloat(firstNumber + "." + secondNumber);
    }

}
