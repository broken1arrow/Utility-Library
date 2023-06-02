package org.broken.arrow.convert.library.utility.converters;

import org.bukkit.Sound;

/**
 * Get the sound from string.
 */

public class SpigotSound {

	/**
	 * Get the sound from string.
	 *
	 * @param sound the sound name.
	 * @return bukkit sound.
	 */
	public static Sound getSound(String sound) {
		if (sound == null) return null;
		final Sound[] sounds = Sound.values();
		sound = sound.toUpperCase();

		for (final Sound sound1 : sounds) {
			if (sound1.name().equals(sound))
				return sound1;
		}
		return null;
	}
}
