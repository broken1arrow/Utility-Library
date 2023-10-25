package org.broken.arrow.menu.library.utility;

import org.bukkit.plugin.Plugin;

public enum ServerVersion {
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

	public static boolean equals(final ServerVersion version) {
		return serverVersion(version) == 0;
	}

	public static boolean atLeast(final ServerVersion version) {
		return equals(version) || newerThan(version);
	}

	public static boolean newerThan(final ServerVersion version) {
		return serverVersion(version) > 0;
	}

	public static boolean olderThan(final ServerVersion version) {
		return serverVersion(version) < 0;
	}

	public static double serverVersion(final ServerVersion version) {
		return currentServerVersion - version.getVersion();
	}

	public static void setServerVersion(final Plugin plugin) {
		if (currentServerVersion > 0) return;
		final String[] strings = plugin.getServer().getBukkitVersion().split("\\.");
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

	public float getVersion() {
		return version;
	}

	public static float getCurrentServerVersion() {
		return currentServerVersion;
	}

	ServerVersion(final float version) {
		this.version = version;

	}
}