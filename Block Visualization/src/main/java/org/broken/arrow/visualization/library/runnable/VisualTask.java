package org.broken.arrow.visualization.library.runnable;

import org.broken.arrow.visualization.library.BlockVisualizerCache;
import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Map.Entry;

public final class VisualTask extends BukkitRunnable {
	private BukkitTask task;
	private int taskID = -1;
	private final Plugin plugin;
	private final BlockVisualizerCache blockVisualizerCache;
	private final Map<Location, VisualizeData> visualizeBlocks;

	public VisualTask(Plugin plugin, BlockVisualizerCache blockVisualizerCache, Map<Location, VisualizeData> visualizeBlocks) {
		this.plugin = plugin;
		this.blockVisualizerCache = blockVisualizerCache;
		this.visualizeBlocks = visualizeBlocks;
	}

	public void start() {
		if (task == null) {
			task = runTaskTimer(plugin, 0L, 40L);
			taskID = task.getTaskId();
		}
	}

	public void stop() {
		this.cancel();
	}

	public boolean isCancel() {
		return this.isCancelled();
	}

	@Override
	public void run() {
		if (visualizeBlocks.isEmpty()) {
			stop();
			return;
		}
		for (final Entry<Location, VisualizeData> visualizeBlocks : visualizeBlocks.entrySet()) {
			final Location location = visualizeBlocks.getKey();
			final VisualizeData visualizeData = visualizeBlocks.getValue();
			if (location.getBlock().getType() == Material.AIR) {
				if (visualizeData.isRemoveIfAir()) {
					if (blockVisualizerCache.isVisualized(location.getBlock()))
						blockVisualizerCache.stopVisualizing(location.getBlock());
				}
				if (visualizeData.isStopIfAir())
					continue;
			}


			if (visualizeData.getViwer() == null)
				for (final Player player : visualizeData.getPlayersAllowed())
					blockVisualizerCache.visualize(player, location.getBlock(), () -> visualizeData);
			else
				blockVisualizerCache.visualize(visualizeData.getViwer(), location.getBlock(), () -> visualizeData);
		}
	}
}