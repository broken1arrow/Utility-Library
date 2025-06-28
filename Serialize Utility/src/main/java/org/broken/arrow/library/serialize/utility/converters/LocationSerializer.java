package org.broken.arrow.library.serialize.utility.converters;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility class for serializing and deserializing Location objects.
 */
public class LocationSerializer {

	private static final String LOCATION_PATTERN = "[-+]?\\d+";

	private LocationSerializer() {
	}

	/**
	 * Serializes a Location object into a string representation including yaw and pitch.
	 *
	 * @param loc The Location object to serialize.
	 * @return The serialized string representation of the Location.
	 */
	public static String serializeLocYaw(final Location loc) {
		String name = loc.getWorld() + "";
		if (loc.getWorld() != null)
			name = loc.getWorld().getName();
		return name + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()) : "");
	}

	/**
	 * Serializes a Location object into a string representation without yaw and pitch.
	 *
	 * @param loc The Location object to serialize.
	 * @return The serialized string representation of the Location.
	 */
	public static String serializeLoc(final Location loc) {
		String name = loc.getWorld() + "";
		if (loc.getWorld() != null)
			name = loc.getWorld().getName();
		return name + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
	}

	/**
	 * Deserializes a string representation of a Location object. It should be formatted like "world X Y Z".
	 * If the input is already a Location instance, it will be returned directly.
	 *
	 * @param rawLoc The string representation of the Location or a Location instance.
	 * @return The deserialized Location object, or null if the input is invalid.
	 */
	public static Location deserializeLoc(final Object rawLoc) {
		if (rawLoc == null) return null;

		final String[] parts;
		if (rawLoc instanceof Location) {
			return (Location) rawLoc;
		} else if (!rawLoc.toString().contains(" ")) {
			return null;
		} else {
			parts = rawLoc.toString().split(" ");
			final int length = parts.length;
			if (length == 4) {
				final String world = parts[0];
				final World bukkitWorld = Bukkit.getWorld(world);
				if (bukkitWorld == null)
					return null;
				if (!parts[1].matches(LOCATION_PATTERN) && !parts[2].matches(LOCATION_PATTERN) && !parts[3].matches(LOCATION_PATTERN))
					return null;
				else {
					final int x = Integer.parseInt(parts[1]);
					final int y = Integer.parseInt(parts[2]);
					final int z = Integer.parseInt(parts[3]);
					return new Location(bukkitWorld, x, y, z);
				}
			}
		}
		return null;
	}
}
