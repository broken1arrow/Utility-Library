package org.broken.arrow.itemcreator.library.utility;

import org.bukkit.plugin.Plugin;

public enum ServerVersion {
	v1_19_4((float) 19.4),
	v1_19((float) 19.0),
	v1_18_2((float) 18.2),
	v1_18_1((float) 18.1),
	v1_18_0((float) 18.0),
	v1_17((float) 17.0),
	v1_16((float) 16.0),
	v1_15((float) 15.0),
	v1_14((float) 14.0),
	v1_13((float) 13.0),
	v1_12((float) 12.0),
	v1_11((float) 11.0),
	v1_10((float) 10.0),
	v1_9((float) 9.0),
	v1_8((float) 8.0),
	v1_7((float) 7.0),
	v1_6((float) 6.0),
	v1_5((float) 5.0),
	v1_4((float) 4.0),
	v1_3_AND_BELOW((float) 3.0);

	private final float version;
	private static float currentServerVersion;

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