package org.broken.arrow.library.serialize.utility.converters;

import org.bukkit.Sound;

import javax.annotation.Nullable;

/**
 * Get the sound from string.
 */

public class SpigotSound {

	private SpigotSound() {
	}

	/**
	 * Get the sound from string.
	 *
	 * @param sound the sound name.
	 * @return bukkit sound or {@code null} if was not valid sound type.
	 */
	@Nullable
	public static Sound getSound(@Nullable String sound) {
		if (sound == null) return null;
		sound = sound.toUpperCase();
		try {
			return Sound.valueOf(sound);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
