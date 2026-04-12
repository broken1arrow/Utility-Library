package org.broken.arrow.library.version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VersionUtil {

    private Version serverVersion;
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
        int[] v = split(version);
        final int major = compare(v[0], v[1], v[1]);
        return major > 0;
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
        final int major = compare(v[0], v[1], v[1]);
        return major >= 0;
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
        final int major = compare(v[0], v[1], v[1]);
        return major < 0;
    }

    /**
     * Returns the parsed server version as a float.
     *
     * @return the server version number
     * @deprecated should no longer be used.
     */
    @Deprecated
    public double getServerVersion() {
        return version;
    }

    /**
     * Get the version for this Minecraft version
     *
     * @return The version instance.
     */
    public Version getVersion() {
        return serverVersion;
    }

    /**
     * Represents the parsed Minecraft server version.
     *
     * <p>This class provides a simplified view of the server version using
     * major, minor, and patch components.</p>
     *
     * <p><strong>Version format handling:</strong></p>
     * <ul>
     *   <li>For legacy versions (e.g. 1.21.5): major = 1, minor = 21, patch = 5</li>
     *   <li>For newer versions (e.g. 26.0.1): major = 26, minor = 0, patch = 1</li>
     * </ul>
     *
     * <p>Note: The meaning of minor and patch differs between legacy and newer
     * version formats, but their positions remain consistent.
     * </p>
     */
    public static class Version {
        private final int major;
        private final int minor;
        private final int patch;

        /**
         * Create new version instance.
         *
         * @param major the major version.
         * @param minor the minor version.
         * @param patch the path version, where smaller changes is made.
         */
        public Version(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        /**
         * Returns the major version number.
         *
         * <p>Examples:</p>
         * <ul>
         *   <li>1.21.5 → 1</li>
         *   <li>26.0.1 → 26</li>
         * </ul>
         *
         * @return the major version
         */
        public int getMajor() {
            return major;
        }

        /**
         * Returns the minor version number.
         *
         * <p>Examples:</p>
         * <ul>
         *   <li>1.21.5 → 21</li>
         *   <li>26.0.1 → 0</li>
         * </ul>
         *
         * @return the minor version
         */
        public int getMinor() {
            return minor;
        }

        /**
         * Returns the patch version number.
         *
         * <p>Examples:</p>
         * <ul>
         *   <li>1.21.5 → 5</li>
         *   <li>26.0.1 → 1</li>
         * </ul>
         *
         * <p>If the patch version cannot be parsed, this may return 0.</p>
         *
         * @return the patch version
         */
        public int getPatch() {
            return patch;
        }

        /**
         * If its legacy version where it's using the 1.21.5 version instead of
         * 26.0.1.
         *
         * @return {@code true if legacy} other cases false.
         */
        public boolean isLegacy() {
            return this.major < 25;
        }
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

        final String firstString = versionPieces[1];
        final String mainVersionString = versionPieces[0];
        int majorVersion = Integer.parseInt(mainVersionString);
        if (majorVersion > 21) {
            int minor = Integer.parseInt(firstString);
            String patchString = versionPieces[2];
            if (!patchString.isEmpty() && !Character.isDigit(patchString.charAt(0))) {
                patchString = patchString.replaceAll("\\D.*", "");
            }
            int patch = 0;
            try {
                patch = Integer.parseInt(patchString);
            } catch (NumberFormatException ignore) {
            }
            this.serverVersion = new Version(majorVersion, minor, patch);
            version = Double.parseDouble(majorVersion + "." + minor);
            return;
        }
        setVersionLegacy(firstString, versionPieces);
    }

    private void setVersionLegacy(String firstString, String[] versionPieces) {
        final String firstNumber;
        String secondNumber;

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
        int major = 1;
        int minor = Integer.parseInt(firstNumber);
        int patch = Integer.parseInt(secondNumber);
        this.serverVersion = new Version(major, minor, patch);
    }

    private int[] split(final double value) {
        int major = (int) value;
        int minor = (int) Math.round((value - major) * 100);
        return new int[]{major, minor};
    }

    private int compare(final int major, final int minor, final int patch) {
        final Version version = getVersion();
        if (version == null)
            return -1;
        if (!version.isLegacy())
            return compareModern(version, major, minor, patch);
        else
            return compareLegacy(version, major, minor);
    }

    private int compareLegacy(@Nonnull final Version version, final int major, final int minor) {
        int versionMinor = version.getMinor();
        if (versionMinor != major)
            return Integer.compare(versionMinor, major);
        return Integer.compare(version.getPatch(), minor);
    }

    private int compareModern(@Nonnull final Version version, final int major, final int minor, final int patch) {
        int versionMajor = version.getMinor();
        int versionMinor = version.getMinor();
        int versionPatch = version.getPatch();
        if (versionMajor != major)
            return Integer.compare(versionMajor, major);
        if (versionMinor != minor)
            return Integer.compare(versionMinor, minor);

        return Integer.compare(versionPatch, patch);
    }

}
