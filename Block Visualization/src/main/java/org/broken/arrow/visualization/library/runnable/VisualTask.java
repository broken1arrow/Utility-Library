package org.broken.arrow.visualization.library.runnable;

import org.broken.arrow.visualization.library.BlockVisualizerCache;
import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
			Block block = location.getBlock();
			if (!checkIfBlockIsAir(visualizeData,block)) {
					continue;
			}

			if (visualizeData.getViwer() == null)
				for (final Player player : visualizeData.getPlayersAllowed())
					blockVisualizerCache.visualize(player, block, () -> visualizeData);
			else
				blockVisualizerCache.visualize(visualizeData.getViwer(), block, () -> visualizeData);
		}
	}

	private boolean checkIfBlockIsAir(VisualizeData visualizeData,Block block){
		if (block.getType() == Material.AIR) {
			if (visualizeData.isRemoveIfAir()) {
				if (blockVisualizerCache.isVisualized(block))
					blockVisualizerCache.stopVisualizing(block);
			}
			return !visualizeData.isStopIfAir();
		}
		return true;
	}
}