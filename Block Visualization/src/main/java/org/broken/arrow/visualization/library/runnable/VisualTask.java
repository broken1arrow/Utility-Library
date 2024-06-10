package org.broken.arrow.visualization.library.runnable;

import org.broken.arrow.visualization.library.BlockVisualizerCache;
import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class VisualTask extends BukkitRunnable {
    private final Map<Location, VisualizeData> visualizeBlocks = new ConcurrentHashMap<>();
    private final Set<Location> remove = new HashSet<>();

    private final Plugin plugin;
    private final BlockVisualizerCache blockVisualizerCache;

    private BukkitTask task;
    private int taskID = -1;
    private volatile boolean runningVisualTask;
    private int seconds;

    public VisualTask(Plugin plugin, BlockVisualizerCache blockVisualizerCache) {
        this.plugin = plugin;
        this.blockVisualizerCache = blockVisualizerCache;
    }

    public void start() {
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(plugin, (Runnable) this, 0L, 20L);
            taskID = task.getTaskId();
        }
    }

    public void stop() {
        this.task.cancel();
    }

    public boolean isCancel() {
        return this.task.isCancelled();
    }

    @Override
    public void run() {
        if (visualizeBlocks.isEmpty()) {
            stop();
            this.task = null;
            return;
        }

        if (seconds >= 5) {
            runningVisualTask = true;
            visualTask();
            runningVisualTask = false;
            seconds = 0;
        }
        seconds++;

        if (!remove.isEmpty()) {
            remove.forEach(location -> {
                final VisualizeData visualizeData = visualizeBlocks.remove(location);
                final Block block = location.getBlock();
                blockVisualizerCache.sendBlockChangePlayers(block, visualizeData, visualizeData::removeFallingBlock);
            });
            remove.clear();
        }
    }

    private void visualTask() {
        for (final Entry<Location, VisualizeData> visualizeBlock : visualizeBlocks.entrySet()) {
            final Location location = visualizeBlock.getKey();
            final VisualizeData visualizeData = visualizeBlock.getValue();
            Block block = location.getBlock();

            if (!checkIfBlockIsAir(visualizeData, block) || visualizeData.isStopVisualizeBlock()) {
                remove.add(location);
                continue;
            }

            if (visualizeData.getViewer() == null) {
                for (final Player player : visualizeData.getPlayersAllowed())
                    blockVisualizerCache.visualize(player, block, visualizeData);
            } else {
                blockVisualizerCache.visualize(visualizeData.getViewer(), block, visualizeData);
            }
        }
    }

    public void addQueuedVisualizeBlock(Location location, VisualizeData visualizeData) {
        visualizeBlocks.put(location, visualizeData);
    }

    public void removeVisualizeBlock(Location location) {
        remove.add(location);
    }

    public boolean containsVisualizeBlockKey(Location location) {
        return visualizeBlocks.containsKey(location);
    }

    public Map<Location, VisualizeData> getVisualizeBlocks() {
        return Collections.unmodifiableMap(visualizeBlocks);
    }

    private boolean checkIfBlockIsAir(VisualizeData visualizeData, Block block) {
        if (block.getType() == Material.AIR) {
            return !visualizeData.isStopIfAir();
        }
        return true;
    }

    public boolean isRunningVisualTask() {
        return runningVisualTask;
    }
}