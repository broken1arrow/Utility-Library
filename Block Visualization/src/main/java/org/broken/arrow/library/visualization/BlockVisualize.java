package org.broken.arrow.library.visualization;

import org.broken.arrow.library.visualization.builders.VisualizeData;
import org.broken.arrow.library.visualization.utility.Function;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The BlockVisualize class provides functionality for visualizing blocks in the Minecraft server.
 * It allows you to add text and glow effects to blocks.
 */
public class BlockVisualize {
	private BlockVisualizerCache blockVisualizerCache;
	private final Plugin plugin;
	private final float serverVersion;

	/**
	 * Constructs a BlockVisualize instance.
	 *
	 * @param plugin Your plugin instance.
	 */
	public BlockVisualize(@Nonnull final Plugin plugin) {
		this.plugin = plugin;
		final String[] versionPieces = plugin.getServer().getBukkitVersion().split("\\.");
		final String firstNumber;
		String secondNumber;
		final String firstString = versionPieces[1];
		if (firstString.contains("-")) {
			firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

			secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
			final int index = secondNumber.toUpperCase().indexOf("R");
			if (index >= 0)
				secondNumber = secondNumber.substring(index + 1);
		} else {
			final String secondString = versionPieces[2];
			firstNumber = firstString;
			secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
		}
		this.serverVersion = Float.parseFloat(firstNumber + "." + secondNumber);
	}


	/**
	 * Visualizes a block with the given visualization data.
	 *
	 * @param block            The block to visualize.
	 * @param visualizeData    A function that provides the visualization data.
	 * @param shallBeVisualize Specifies whether the block should be visualized.
	 */
	public void visualizeBlock(@Nonnull final Block block,@Nonnull final Function<VisualizeData> visualizeData, final boolean shallBeVisualize) {
		visualizeBlock(null, block, visualizeData, shallBeVisualize);
	}

	/**
	 * Visualizes a block for a specific player with the given visualization data.
	 *
	 * @param player           The player for whom to visualize the block.
	 * @param block            The block to visualize.
	 * @param visualizeData    A function that provides the visualization data.
	 * @param shallBeVisualize Specifies whether the block should be visualized.
	 */
	public void visualizeBlock(@Nullable final Player player, @Nonnull final Block block, @Nonnull final Function<VisualizeData> visualizeData, final boolean shallBeVisualize) {
		BlockVisualizerCache blockVisualizer = this.blockVisualizerCache;
		if (blockVisualizer == null) {
			blockVisualizer = new BlockVisualizerCache(plugin, this);
			this.blockVisualizerCache = blockVisualizer;
		}
		boolean isVisualized = blockVisualizer.isVisualized(block);
		if (shallBeVisualize) {
			if (!isVisualized)
				blockVisualizer.visualize(player, block, visualizeData.apply());
		} else if (isVisualized) {
			blockVisualizer.stopVisualizing(block, visualizeData.apply());
		}
		blockVisualizer.getVisualTask().start();
	}

	/**
	 * Stops visualizing a block.
	 *
	 * @param block The block to stop visualizing.
	 * @return True if the block was being visualized and it was stopped, false otherwise.
	 */
	public boolean stopVisualizing(final Block block) {
		if (blockVisualizerCache != null && blockVisualizerCache.isVisualized(block)) {
			this.blockVisualizerCache.getVisualTask().removeVisualizeBlock(block.getLocation());
			return true;
		}
		return false;
	}

	/**
	 * Gets the server version associated with this BlockVisualize instance.
	 *
	 * @return The server version.
	 */
	public float getServerVersion() {
		return serverVersion;
	}

	public BukkitTask runTaskLater(long tick, Runnable task, boolean async) {
		if (async)
			return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, tick);
		else
			return Bukkit.getScheduler().runTaskLater(plugin, task, tick);
	}
}
