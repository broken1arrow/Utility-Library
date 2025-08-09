package org.broken.arrow.library.visualization;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.visualization.builders.VisualizeData;
import org.broken.arrow.library.visualization.runnable.VisualTask;
import org.broken.arrow.library.visualization.utility.EntityModifications;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A utility class that manages block visualization for players.
 */
public final class BlockVisualizerUtility {
    private final VisualTask visualTask;
    private final BlockVisualize blockVisualize;
    private final EntityModifications entityModifications;

    /**
     * Creates a new visualizer utility bound to the given plugin and visualizer context.
     *
     * @param plugin         the plugin instance used for scheduling tasks.
     * @param blockVisualize the main class that holds the registered data.
     */
    public BlockVisualizerUtility(@Nonnull final Plugin plugin, @Nonnull final BlockVisualize blockVisualize) {
        this.visualTask = new VisualTask(plugin, this);
        this.blockVisualize = blockVisualize;
        this.entityModifications = new EntityModifications(blockVisualize.getServerVersion());
    }

    /**
     * Visualizes the given block for players, spawning a visual entity and caching its state.
     * <p>
     * If the block is already visualized, the previous visualization entity is removed first.
     * Visibility can be restricted to a single {@code viewer} or allowed for all online players
     * who have the required permission (if defined in {@link VisualizeData}).
     * </p>
     *
     * @param viewer        the specific player to visualize for, or {@code null} for all permitted players.
     * @param block         the block to visualize.
     * @param visualizeData visualization metadata including mask, text, and permissions.
     * @throws ValidateExceptions if {@code block} is {@code null}.
     */
    public void visualize(@Nullable final Player viewer, @Nonnull final Block block, @Nonnull final VisualizeData visualizeData) {
        this.throwErrorBlockNull(block);

        boolean visualized = isVisualized(block);
        if (visualized) {
            visualizeData.removeFallingBlock();
        }

        final Location location = block.getLocation();
        visualizeData.setFallingBlock(entityModifications.spawnFallingBlock(location, visualizeData.getMask(), visualizeData.getText()));

        final Iterator<Player> players = block.getWorld().getPlayers().iterator();
        this.setVisualData(visualizeData, location, players, viewer);
        visualTask.addQueuedVisualizeBlock(location, visualizeData);

    }

    private void setVisualData(final VisualizeData visualizeData, final Location location, final Iterator<Player> players, final Player viewer) {
        if (viewer == null) {
            while (players.hasNext()) {
                final Player player = players.next();
                if (this.hasPermission(visualizeData, player, null)) {
                    this.setPlayersInCache(visualizeData, location, player);
                }
            }
        } else {
            while (players.hasNext()) {
                final Player player = players.next();
                if (this.hasPermission(visualizeData, player, viewer)) {
                    this.setPlayersInCache(visualizeData, location, player);
                }
            }
        }
    }

    /**
     * Stops visualizing the given block and removes any associated entities.
     * <p>
     * If {@code visualizeData} is provided, a block change is sent to affected players to
     * revert the block to its original appearance.
     * </p>
     *
     * @param block         the block to stop visualizing.
     * @param visualizeData the associated visualization data, or {@code null} if none.
     * @throws ValidateExceptions if {@code block} is {@code null} or not currently visualized.
     */
    public void stopVisualizing(@Nonnull final Block block, VisualizeData visualizeData) {
        this.throwErrorBlockNull(block);

        Validate.checkBoolean(!isVisualized(block), "Block at " + block.getLocation() + " not visualized");
        visualTask.removeVisualizeBlock(block.getLocation());

        if (visualizeData != null) {
            sendBlockChangePlayers(block, visualizeData, visualizeData::removeFallingBlock);
        }

    }

    /**
     * Stops visualizing all currently visualized blocks.
     * <p>
     * This method iterates over all tracked visualizations and stops them.
     * </p>
     */
    public void stopAll() {
        for (final Map.Entry<Location, VisualizeData> dataEntry : visualTask.getVisualizeBlocks().entrySet()) {
            final Block block = dataEntry.getKey().getBlock();
            if (isVisualized(block)) {
                stopVisualizing(block, dataEntry.getValue());
            }
        }

    }

    /**
     * Gets the entity modifications helper used for spawning visual entities.
     * Properties for the falling block can be set through this class.
     *
     * @return the entity modifications instance.
     */
    public EntityModifications getEntityModifications() {
        return entityModifications;
    }

    /**
     * Checks if the given block is currently visualized.
     *
     * @param block the block to check.
     * @return {@code true} if the block is visualized, otherwise {@code false}.
     * @throws ValidateExceptions if {@code block} is {@code null}.
     */
    public boolean isVisualized(@Nonnull final Block block) {
        this.throwErrorBlockNull(block);

        return visualTask.containsVisualizeBlockKey(block.getLocation());
    }

    /**
     * Sends a block change to all permitted players for the given visualization.
     * <p>
     * Players to notify are determined by {@link VisualizeData#getPlayersAllowed()} or, if empty,
     * all players in the world receive the update.
     * </p>
     *
     * @param block         the block to update.
     * @param visualizeData the visualization data containing affected players and change details.
     * @param runTask       the task to execute after the block change is sent.
     */
    public void sendBlockChangePlayers(@Nonnull Block block, VisualizeData visualizeData, Runnable runTask) {
        final Set<Player> playersAllowed = visualizeData.getPlayersAllowed();
        final Iterator<Player> players;
        if (!playersAllowed.isEmpty()) players = playersAllowed.iterator();
        else players = block.getWorld().getPlayers().iterator();
        while (players.hasNext()) {
            final Player player = players.next();
            runBlockChange(5, player, block.getLocation().getBlock(), runTask);
        }
    }

    /**
     * Schedules a delayed block change for the given player and location.
     *
     * @param delayTicks the delay in ticks before sending the block change.
     * @param player     the player to send the change to.
     * @param location   the block location to update.
     * @param material   the material to display at the location.
     */
    public void runBlockChange(final int delayTicks, final Player player, final Location location, final Material material) {
        if (delayTicks > 0) {
            this.blockVisualize.runTaskLater(delayTicks, () -> {
                sendBlockChange0(player, location, material);
            }, true);
        } else {
            sendBlockChange0(player, location, material);
        }

    }

    /**
     * Schedules a delayed block change and executes an additional task after the change.
     *
     * @param delayTicks the delay in ticks before sending the block change.
     * @param player     the player to send the change to.
     * @param block      the block to update.
     * @param task       the task to execute after sending the change.
     */
    public void runBlockChange(final int delayTicks, final Player player, final Block block, Runnable task) {
        if (delayTicks > 0) {
            this.blockVisualize.runTaskLater(delayTicks, () -> {
                this.blockVisualize.runTaskLater(0, task, false);
                sendBlockChange(player, block);
            }, true);
        } else {
            sendBlockChange(player, block);
        }

    }

    /**
     * The task to send an update to the player.
     *
     * @param player the player to send the change to.
     * @param block  the block to update.
     */
    private void sendBlockChange(final Player player, final Block block) {
        try {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        } catch (final NoSuchMethodError var3) {
            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
        }

    }

    /**
     * The task to send a update to the player.
     *
     * @param player   the player to send the change to.
     * @param location the location of the block to update.
     * @param material the material type to set on the block.
     */
    private void sendBlockChange0(final Player player, final Location location, final Material material) {
        try {
            player.sendBlockChange(location, material.createBlockData());
        } catch (final NoSuchMethodError var4) {
            player.sendBlockChange(location, material, (byte) material.getId());
        }

    }

    private void setPlayersInCache(final VisualizeData visualizeData, final Location location, final Player player) {
        visualizeData.addPlayersAllowed(player);
        runBlockChange(5, player, location, this.blockVisualize.getServerVersion() < 9.0 ? visualizeData.getMask() : Material.BARRIER);
    }

    /**
     * Gets the visualization scheduler task manager.
     *
     * @return the visual task instance.
     */
    public VisualTask getVisualTask() {
        return this.visualTask;
    }

    /**
     * Throws an exception if the given block is {@code null}.
     *
     * @param b the block to check
     * @throws ValidateExceptions if {@code b} is {@code null}
     */
    public void throwErrorBlockNull(Block b) throws ValidateExceptions {
        if (b == null)
            throw new ValidateExceptions("Block is marked non-null but is set to null.");
    }

    private boolean hasPermission(@Nonnull final VisualizeData visualizeData, @Nonnull final Player player, @Nullable final Player viewer) {
        if (visualizeData.getPermission() != null) {
            return player.hasPermission(visualizeData.getPermission()) && (viewer == null || player.getUniqueId().equals(viewer.getUniqueId()));
        }
        return viewer == null || player.getUniqueId().equals(viewer.getUniqueId());
    }

}