package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.version.VersionUtil;

/**
 * Enum representing supported Minecraft server versions with associated numeric values for comparison.
 * <p>
 * Provides utility methods to compare the current server version with any version defined in this enum.
 * The version number is stored as a float for easy numeric comparison.
 * </p>
 * <p>
 * The current server version is initialized statically by parsing the Bukkit version string.
 * </p>
 */
public class ServerVersion {
    private static final VersionUtil versionUtil ;

	/**
	 * Checks if the current server version is at least the given version.
	 * This means it is either equal to or newer than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is equal or newer, false otherwise
	 */
	public static boolean atLeast(final double version) {
		return versionUtil.versionAtLeast(version);
	}

	/**
	 * Checks if the current server version is newer than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is newer, false otherwise
	 */
	public static boolean newerThan(final double version) {
		return versionUtil.versionNewer(version);
	}

	/**
	 * Checks if the current server version is older than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is older, false otherwise
	 */
	public static boolean olderThan(final double version) {
		return versionUtil.versionOlder(version);
	}

	/**
     * Returns the numeric version representation of this enum constant.
     *
     * @return the numeric version as a float
     */
	public double getVersion() {
		return versionUtil.getServerVersion();
	}


	/**
	 * Constructs a ServerVersion enum constant with the specified numeric version.
	 *
	 */
	ServerVersion() {
	}


    // Static initializer to parse Bukkit version and set currentServerVersion accordingly.
	static {
        versionUtil = new VersionUtil();
/*		final String[] strings = Bukkit.getBukkitVersion().split("\\.");
		final String firstNumber;
		String secondNumber;
		final String firstString = strings[1];
		if (firstString.contains("-")) {
			firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

			secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
			final int index = secondNumber.toUpperCase().indexOf("R");
			if (index >= 0)
				secondNumber = secondNumber.substring(index + 1);
		} else {
			final String secondString = strings[2];
			firstNumber = firstString;
			secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
		}
		final float version = Float.parseFloat(firstNumber + "." + secondNumber);
		if (version < 18)
			currentServerVersion = (float) Math.floor(version);
		else
			currentServerVersion = version;*/
	}
}