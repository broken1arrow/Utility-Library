package org.broken.arrow.library.menu.utility;

import org.bukkit.Bukkit;
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
public enum ServerVersion {
	V1_20_1((float) 21.1),
	V1_20_0((float) 20.0),
	V1_19_4((float) 19.4),
	V1_19((float) 19.0),
	V1_18_2((float) 18.2),
	V1_18_1((float) 18.1),
	V1_18_0((float) 18.0),
	V1_17((float) 17.0),
	V1_16((float) 16.0),
	V1_15((float) 15.0),
	V1_14((float) 14.0),
	V1_13((float) 13.0),
	V1_12((float) 12.0),
	V1_11((float) 11.0),
	V1_10((float) 10.0),
	V1_9((float) 9.0),
	V1_8((float) 8.0),
	V1_7((float) 7.0),
	V1_6((float) 6.0),
	V1_5((float) 5.0),
	V1_4((float) 4.0),
	V1_3_AND_BELOW((float) 3.0);

	private final float version;
	private static float currentServerVersion = -1;

	/**
	 * Checks if the current server version exactly equals the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is equal to the given version, false otherwise
	 */
	public static boolean equals(final ServerVersion version) {
		return serverVersion(version) == 0;
	}

	/**
	 * Checks if the current server version is at least the given version.
	 * This means it is either equal to or newer than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is equal or newer, false otherwise
	 */
	public static boolean atLeast(final ServerVersion version) {
		return equals(version) || newerThan(version);
	}

	/**
	 * Checks if the current server version is newer than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is newer, false otherwise
	 */
	public static boolean newerThan(final ServerVersion version) {
		return serverVersion(version) > 0;
	}

	/**
	 * Checks if the current server version is older than the given version.
	 *
	 * @param version the server version to compare against
	 * @return true if the current server version is older, false otherwise
	 */
	public static boolean olderThan(final ServerVersion version) {
		return serverVersion(version) < 0;
	}

	/**
	 * Returns the difference between the current server version and the given version.
	 * Positive means current version is newer, negative means older, zero means equal.
	 *
	 * @param version the server version to compare against
	 * @return the numeric difference between current and given version
	 */
	public static double serverVersion(final ServerVersion version) {
		return currentServerVersion - version.getVersion();
	}


	/**
	 * Returns the numeric version representation of this enum constant.
	 *
	 * @return the numeric version as a float
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * Returns the cached current server version as a float.
	 *
	 * @return the current server version
	 */
	public static float getCurrentServerVersion() {
		return currentServerVersion;
	}

	/**
	 * Constructs a ServerVersion enum constant with the specified numeric version.
	 *
	 * @param version the numeric version for this enum constant
	 */
	ServerVersion(final float version) {
		this.version = version;

	}

	// Static initializer to parse Bukkit version and set currentServerVersion accordingly.
	static {
		final String[] strings = Bukkit.getBukkitVersion().split("\\.");
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
			currentServerVersion = version;
	}
}