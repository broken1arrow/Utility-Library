package org.broken.arrow.library.version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VersionUtil {

    private double version;

    /**
     * Constructs a ServerVersion instance by extracting the server version
     * from the provided plugin's server or the default Bukkit server if the plugin is null.
     *
     */
    public VersionUtil() {
        this(null);
    }

    /**
     * Constructs a ServerVersion instance by extracting the server version
     * from the provided plugin's server or the default Bukkit server if the plugin is null.
     *
     * @param plugin the plugin instance to get the server version from; may be null
     */
    public VersionUtil(@Nonnull final Plugin plugin) {
        setServerVersion(plugin);
    }

    /**
     * Checks if the current server version is newer than the given version.
     *
     * @param version the server version to compare against
     * @return true if the current server version is newer, false otherwise
     */
    public boolean versionNewer(double version) {
        return version > this.version;
    }

    /**
     * Checks if the current server version is at least the given version.
     * This means it is either equal to or newer than the given version.
     *
     * @param version the server version to compare against
     * @return true if the current server version is equal or newer, false otherwise
     */
    public boolean versionAtLeast(double version) {
        return version >= this.version;
    }

    /**
     * Returns true if version is newer than provided value.
     *
     * @param min the min version
     * @param max the max version
     * @return true if server version is between the numbers.
     */
    public boolean versionBetween(double min, double max) {
        return min > this.version && max < this.version;
    }

    /**
     * Checks if the current server version is older than the given version.
     *
     * @param version the server version to compare against
     * @return true if the current server version is older, false otherwise
     */
    public boolean versionOlder(double version) {
        return version < this.version;
    }

    /**
     * Returns the parsed server version as a float.
     *
     * @return the server version number
     */
    public double getServerVersion() {
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
        version = Double.parseDouble(firstNumber + "." + secondNumber);
    }

}
