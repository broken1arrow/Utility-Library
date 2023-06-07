package org.broken.arrow.visualization.library;

import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.broken.arrow.visualization.library.utility.Function;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;

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
	 * @param plugin        Your plugin instance.
	 * @param serverVersion The Minecraft server version in a supported format (e.g., 8.0, 8, or 8.8).
	 */
	public BlockVisualize(@Nonnull final Plugin plugin, float serverVersion) {
		this.plugin = plugin;
		this.serverVersion = serverVersion;
	}


	/**
	 * Visualizes a block with the given visualization data.
	 *
	 * @param block            The block to visualize.
	 * @param visualizeData    A function that provides the visualization data.
	 * @param shallBeVisualize Specifies whether the block should be visualized.
	 */
	public void visulizeBlock(final Block block, final Function<VisualizeData> visualizeData, final boolean shallBeVisualize) {
		visulizeBlock(null, block, visualizeData, shallBeVisualize);
	}

	/**
	 * Visualizes a block for a specific player with the given visualization data.
	 *
	 * @param player           The player for whom to visualize the block.
	 * @param block            The block to visualize.
	 * @param visualizeData    A function that provides the visualization data.
	 * @param shallBeVisualize Specifies whether the block should be visualized.
	 */
	public void visulizeBlock(final Player player, final Block block, Function<VisualizeData> visualizeData, final boolean shallBeVisualize) {
		BlockVisualizerCache blockVisualizerCache = this.blockVisualizerCache;
		if (blockVisualizerCache == null)
			blockVisualizerCache = new BlockVisualizerCache(plugin, this);
		if (!blockVisualizerCache.isVisualized(block) && shallBeVisualize)
			blockVisualizerCache.visualize(player, block,
					visualizeData);

		else if (blockVisualizerCache.isVisualized(block) && shallBeVisualize) {
			blockVisualizerCache.stopVisualizing(block);
			blockVisualizerCache.visualize(player, block,
					visualizeData);

		} else if (blockVisualizerCache.isVisualized(block)) {
			blockVisualizerCache.stopVisualizing(block);
		}
		this.blockVisualizerCache = blockVisualizerCache;
		blockVisualizerCache.getVisualTask().start();
	}

	/**
	 * Stops visualizing a block.
	 *
	 * @param block The block to stop visualizing.
	 * @return True if the block was being visualized and it was stopped, false otherwise.
	 */
	public boolean stopVisualizing(final Block block) {
		if (blockVisualizerCache.isVisualized(block)) {
			blockVisualizerCache.stopVisualizing(block);
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
