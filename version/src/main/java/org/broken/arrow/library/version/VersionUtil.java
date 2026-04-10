package org.broken.arrow.library.version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VersionUtil {

    private double version;
    int major;
    int minor;
    int patch;

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
        int[] v = split(version);
        return compare(v[0], v[1]) > 0;
    }

    /**
     * Checks if the current server version is at least the given version.
     * This means it is either equal to or newer than the given version.
     *
     * @param version the server version to compare against
     * @return true if the current server version is equal or newer, false otherwise
     */
    public boolean versionAtLeast(double version) {
        int[] v = split(version);
        return compare(v[0], v[1]) >= 0;
    }

    /**
     * Returns true if version is newer than provided value.
     *
     * @param min the min version
     * @param max the max version
     * @return true if server version is between the numbers.
     */
    public boolean versionBetween(double min, double max) {
        return versionAtLeast(min) && versionOlder(max);
    }

    /**
     * Checks if the current server version is older than the given version.
     *
     * @param version the server version to compare against
     * @return true if the current server version is older, false otherwise
     */
    public boolean versionOlder(double version) {
        int[] v = split(version);
        return compare(v[0], v[1]) < 0;
    }

    /**
     * Returns the parsed server version as a float.
     *
     * @deprecated  should no longer be used.
     * @return the server version number
     */
    @Deprecated
    public double getServerVersion() {
        return version;
    }

    /**
     * Get the version for this Minecraft version
     *
     * @return a array of the version from major to patch.
     */
    public int[] getVersion() {
        return new int[]{major, minor, patch};
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
        final String mainVersionString = versionPieces[0];
        int majorVersion = Integer.parseInt(mainVersionString);
        if (majorVersion > 21) {
            major = majorVersion;
            minor = Integer.parseInt(firstString);
            String patchString = versionPieces[2];
            if (!patchString.isEmpty() && !Character.isDigit(patchString.charAt(0))) {
                patchString = patchString.replaceAll("\\D.*", "");
            }
            try {
                patch = Integer.parseInt(patchString);
            } catch (NumberFormatException ignore) {
                patch = 0;
            }
            version = Double.parseDouble(major + "." +  minor);
            return;
        }
        if (firstString.contains("-")) {
            int endIndex = firstString.lastIndexOf("-");
            firstNumber = firstString.substring(0, Math.max(endIndex, 1));
            secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
            final int index = secondNumber.toUpperCase().indexOf("R");
            if (index >= 0)
                secondNumber = secondNumber.substring(index + 1);
        } else {
            final String secondString = versionPieces[2];
            firstNumber = firstString;
            int endIndex = secondString.lastIndexOf("-");
            secondNumber = secondString.substring(0, Math.max(endIndex, 1));
        }
        version = Double.parseDouble(firstNumber + "." + secondNumber);
        major = 1;
        minor = Integer.parseInt(firstNumber);
        patch = Integer.parseInt(secondNumber);
    }

    private int[] split(double value) {
        int major = (int) value;
        int minor = (int) Math.round((value - major) * 10);
        return new int[]{major, minor};
    }

    private int compare(int major, int minor) {
        if (this.major != major)
            return Integer.compare(this.major, major);

        return Integer.compare(this.minor, minor);
    }
}
