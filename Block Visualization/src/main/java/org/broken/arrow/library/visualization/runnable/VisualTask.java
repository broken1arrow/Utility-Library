package org.broken.arrow.library.visualization.runnable;

import org.broken.arrow.library.visualization.BlockVisualizerUtility;
import org.broken.arrow.library.visualization.builders.VisualizeData;
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
/**
 * Handles scheduled visualization of blocks for players.
 * <p>
 * This task runs every second and:
 * <ul>
 *     <li>Refreshes visualized blocks every 5 seconds.</li>
 *     <li>Removes expired or stopped visualizations.</li>
 *     <li>Stops automatically if there are no visualized blocks left.</li>
 * </ul>
 * Thread-safety:
 * <ul>
 *     <li>{@link #visualizeBlocks} is a ConcurrentHashMap to allow safe iteration and modification.</li>
 *     <li>{@link #remove} is not thread-safe and should only be modified from the main thread.</li>
 * </ul>
 */
public final class VisualTask extends BukkitRunnable {
    private final Map<Location, VisualizeData> visualizeBlocks = new ConcurrentHashMap<>();
    private final Set<Location> remove = new HashSet<>();

    private final Plugin plugin;
    private final BlockVisualizerUtility blockVisualizerCache;

    private BukkitTask task;
    private int taskID = -1;
    private volatile boolean runningVisualTask;
    private int seconds;

    /**
     * Creates a new visual task.
     *
     * @param plugin the plugin instance
     * @param blockVisualizerUtility the block visualizer utility
     */
    public VisualTask(Plugin plugin, BlockVisualizerUtility blockVisualizerUtility) {
        this.plugin = plugin;
        this.blockVisualizerCache = blockVisualizerUtility;
    }

    /**
     * Starts the visualization task if it is not already running.
     */
    public void start() {
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(plugin, (Runnable) this, 0L, 20L);
            taskID = task.getTaskId();
        }
    }
    /**
     *
     * Stops the visualization task if it is running.
     */
    public void stop() {
        this.task.cancel();
    }

    /**
     * Checks if the visualization task is cancelled.
     *
     * @return {@code true} if the task is cancelled, {@code false } otherwise.
     */
    public boolean isCancel() {
        return this.task.isCancelled();
    }

    /**
     * Main tick loop.
     * <p>
     * - Stops the task if there are no active visualizations.<br>
     * - Refreshes block visualizations every 5 seconds.<br>
     * - Removes any scheduled visualizations.
     */
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

    /**
     * Queues a block for visualization.
     *
     * @param location the block location
     * @param visualizeData the visualization data
     */
    public void addQueuedVisualizeBlock(Location location, VisualizeData visualizeData) {
        visualizeBlocks.put(location, visualizeData);
    }

    /**
     * Schedules a visualized block for removal in the next tick.
     *
     * @param location the block location to remove
     */
    public void removeVisualizeBlock(Location location) {
        remove.add(location);
    }

    /**
     * Checks if a given location is currently being visualized.
     *
     * @param location the block location
     * @return true if the block is being visualized, false otherwise
     */
    public boolean containsVisualizeBlockKey(Location location) {
        return visualizeBlocks.containsKey(location);
    }

    /**
     * Gets an unmodifiable view of all active visualized blocks.
     *
     * @return unmodifiable map of locations to visualization data
     */
    public Map<Location, VisualizeData> getVisualizeBlocks() {
        return Collections.unmodifiableMap(visualizeBlocks);
    }

    /**
     * Checks if a block should continue being visualized based on air state.
     *
     * @param visualizeData the visualization data
     * @param block the block
     * @return true if the block is valid for visualization, false otherwise
     */
    private boolean checkIfBlockIsAir(VisualizeData visualizeData, Block block) {
        if (block.getType() == Material.AIR) {
            return !visualizeData.isStopIfAir();
        }
        return true;
    }

    /**
     * Processes all active visualized blocks and sends updates to their viewers.
     * <p>
     * If a block is no longer valid or its visualization is stopped,
     * it is scheduled for removal.
     */
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

    /**
     * Checks if the visual task is currently processing blocks.
     *
     * @return true if the task is running its main visualization loop
     */
    public boolean isRunningVisualTask() {
        return runningVisualTask;
    }
}