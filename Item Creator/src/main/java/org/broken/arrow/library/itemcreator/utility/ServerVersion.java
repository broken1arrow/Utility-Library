package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the server version as a floating-point number parsed from the Bukkit version string.
 */
public class ServerVersion {

    private float version;

    /**
     * Constructs a ServerVersion instance by extracting the server version
     * from the provided plugin's server or the default Bukkit server if the plugin is null.
     *
     * @param plugin the plugin instance to get the server version from; may be null
     */
    public ServerVersion(@Nonnull final Plugin plugin) {
        setServerVersion(plugin);
    }

    /**
     * Returns the parsed server version as a float.
     *
     * @return the server version number
     */
    public float getServerVersion() {
        return version;
    }

    /**
     * Parses and sets the server version from the Bukkit version string.
     * Handles versions with suffixes and release indicators.
     *
     * @param plugin the plugin instance used to retrieve the server's Bukkit version; may be null
     */
    private void setServerVersion(@Nullable final Plugin plugin) {
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
