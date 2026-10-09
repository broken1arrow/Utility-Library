package org.broken.arrow.library.serialize.utility.converters;

import org.bukkit.Sound;
import org.bukkit.block.BlockFace;

import javax.annotation.Nullable;

public class SpigotBlockFace {

	private SpigotBlockFace() {
	}

	/**
	 * Get the block face from string.
	 *
	 * @param blockFace the sound name.
	 * @return bukkit blockFace or {@code null} if not valid blockFace.
	 */
	public static BlockFace getBlockFace(@Nullable String blockFace) {
		if (blockFace == null) return null;
		blockFace = blockFace.toUpperCase();
		try {
			return BlockFace.valueOf(blockFace);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
