package org.broken.arrow.serialize.library.utility.converters;

import org.bukkit.block.BlockFace;

public class SpigotBlockFace {

	/**
	 * Get the block face from string.
	 *
	 * @param blockFace the sound name.
	 * @return bukkit sound.
	 */
	public static BlockFace getBlockFace(String blockFace) {
		if (blockFace == null) return null;
		final BlockFace[] blockFaces = BlockFace.values();
		blockFace = blockFace.toUpperCase();

		for (final BlockFace face : blockFaces) {
			if (face.name().equals(blockFace))
				return face;
		}
		return null;
	}
}
