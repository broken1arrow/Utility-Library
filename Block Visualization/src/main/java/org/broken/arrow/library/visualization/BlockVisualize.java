package org.broken.arrow.library.visualization;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.version.VersionUtil;
import org.broken.arrow.library.visualization.builders.VisualizeData;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * The BlockVisualize class provides functionality for visualizing blocks in the Minecraft server.
 * It allows you to add text and glow effects to blocks.
 */
public class BlockVisualize {
	private BlockVisualizerUtility blockVisualizerCache;
	private final Plugin plugin;
	private final VersionUtil serverVersion;

	/**
	 * Constructs a BlockVisualize instance.
	 *
	 * @param plugin Your plugin instance.
	 */
	public BlockVisualize(@Nonnull final Plugin plugin) {
		this.plugin = plugin;
        this.serverVersion = new VersionUtil();
	}


	/**
	 * Visualizes a block with the given visualization data.
	 * <p>
	 * This will attempt to visualize the block for the players defined in the {@link VisualizeData}.
	 * </p>
	 *
	 * @param block            The block to visualize, must not be {@code null}.
	 * @param visualizeData    A supplier that provides the {@link VisualizeData} to use for visualization.
	 * @param shallBeVisualize {@code true} if the block should be visualized; {@code false} to remove or skip visualization.
	 */
	public void visualizeBlock(@Nonnull final Block block, @Nonnull final Supplier<VisualizeData> visualizeData, final boolean shallBeVisualize) {
		visualizeBlock(null, block, visualizeData, shallBeVisualize);
	}

	/**
	 * Visualizes a block for a specific player or using the player(s) provided in the {@link VisualizeData}.
	 * <p>
	 * If the player is {@code null}, the method will fall back to the players defined in the {@link VisualizeData}.
	 * </p>
	 *
	 * @param player           the player for whom the block will be visualized; may be {@code null}.
	 * @param block            the block to visualize; must not be {@code null}.
	 * @param visualizeData    A supplier that provides the {@link VisualizeData} to use for visualization.
	 * @param shallBeVisualize {@code true} if the block should be visualized; {@code false} to remove or skip visualization.
	 */
	public void visualizeBlock(@Nullable final Player player, @Nonnull final Block block, @Nonnull final Supplier<VisualizeData> visualizeData, final boolean shallBeVisualize) {
        Validate.checkBoolean(checkAtLeastOnePlayerProvided(player,visualizeData.get()),"You must provide at least one player to visualize the block.");

		BlockVisualizerUtility blockVisualizer = this.blockVisualizerCache;
		if (blockVisualizer == null) {
			blockVisualizer = new BlockVisualizerUtility(plugin, this);
			this.blockVisualizerCache = blockVisualizer;
		}
		boolean isVisualized = blockVisualizer.isVisualized(block);
		if (shallBeVisualize) {
			if (!isVisualized)
				blockVisualizer.visualize(player, block, visualizeData.get());
		} else if (isVisualized) {
			blockVisualizer.stopVisualizing(block, visualizeData.get());
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
	public double getServerVersion() {
		return serverVersion.getServerVersion();
	}

	/**
	 * Run a task later, using the bukkit scheduler to run the task.
	 *
	 * @param tick amount of ticks before run your code.
	 * @param task the task to execute.
	 * @param async if the task shall be asynchronous set it to {@code true}.
	 *                 Don't use asynchronous on most of the bukkits methods,
	 *              if it not explicit says it works.
	 * @return a BukkitTask instance.
	 */
	public BukkitTask runTaskLater(long tick, Runnable task, boolean async) {
		if (async)
			return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, tick);
		else
			return Bukkit.getScheduler().runTaskLater(plugin, task, tick);
	}


	private boolean checkAtLeastOnePlayerProvided(final Player player, final VisualizeData visualizeData) {
		if (player == null){
			return visualizeData.getPlayersAllowed().isEmpty() || visualizeData.getViewer() == null;
		}
        return false;
    }

}
