package org.broken.arrow.visualization.library;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.broken.arrow.visualization.library.runnable.VisualTask;
import org.broken.arrow.visualization.library.utility.EntityModifications;
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

public final class BlockVisualizerCache {
    private final VisualTask visualTask;
    private final BlockVisualize blockVisualize;
    private final EntityModifications entityModifications;


    public BlockVisualizerCache(Plugin plugin, BlockVisualize blockVisualize) {
        this.visualTask = new VisualTask(plugin, this);
        this.blockVisualize = blockVisualize;
        this.entityModifications = new EntityModifications(blockVisualize.getServerVersion());
    }

    public void visualize(@Nullable final Player viewer, @Nonnull final Block block, @Nonnull final VisualizeData visualizeData) {
        this.throwErrorBlockNull(block );

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

    private void setVisualData(VisualizeData visualizeData, Location location, Iterator<Player> players, Player viewer) {
        if (viewer == null) {
            while (players.hasNext()) {
                final Player player = players.next();
                this.setPlayersInCache(visualizeData, location, player);
            }
        } else {
            while (players.hasNext()) {
                final Player player = players.next();
                if (visualizeData.getPermission() == null || player.hasPermission(visualizeData.getPermission()) || player.getUniqueId().equals(viewer.getUniqueId())) {
                    this.setPlayersInCache(visualizeData, location, player);
                }
            }
        }
    }


    public void stopVisualizing(@Nonnull final Block block, VisualizeData visualizeData) {
        this.throwErrorBlockNull(block );

        Validate.checkBoolean(!isVisualized(block), "Block at " + block.getLocation() + " not visualized");
        visualTask.removeVisualizeBlock(block.getLocation());

        if (visualizeData != null) {
            sendBlockChangePlayers(block, visualizeData, visualizeData::removeFallingBlock);
        }

    }

    public void stopAll() {
        for (final Map.Entry<Location, VisualizeData> dataEntry : visualTask.getVisualizeBlocks().entrySet()) {
            final Block block = dataEntry.getKey().getBlock();
            if (isVisualized(block)) {
                stopVisualizing(block,dataEntry.getValue());
            }
        }

    }

    public EntityModifications getEntityModifications() {
        return entityModifications;
    }

    public boolean isVisualized(@Nonnull final Block block) {
        this.throwErrorBlockNull(block );

        return visualTask.containsVisualizeBlockKey(block.getLocation());
    }

    public void sendBlockChangePlayers(@Nonnull Block block, VisualizeData visualizeData, Runnable runTask) {
        final Set<Player> playersAllowed = visualizeData.getPlayersAllowed();
        final Iterator<Player> players;
        if (!playersAllowed.isEmpty()) players = playersAllowed.iterator();
        else players = block.getWorld().getPlayers().iterator();
        while (players.hasNext()) {
            final Player player = players.next();
            runBlockChange(5, player, block.getLocation().getBlock(),runTask);
        }
    }

    public void runBlockChange(final int delayTicks, final Player player, final Location location, final Material material) {
        if (delayTicks > 0) {
            this.blockVisualize.runTaskLater(delayTicks, () -> {
                sendBlockChange0(player, location, material);
            }, true);
        } else {
            sendBlockChange0(player, location, material);
        }

    }

    public void runBlockChange(final int delayTicks, final Player player, final Block block, Runnable task) {
        if (delayTicks > 0) {
            this.blockVisualize.runTaskLater(delayTicks, () -> {
                this.blockVisualize.runTaskLater (0, task,false);
                sendBlockChange(player, block);
            }, true);
        } else {
            sendBlockChange(player, block);
        }

    }

    private void sendBlockChange(final Player player, final Block block) {
        try {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        } catch (final NoSuchMethodError var3) {
            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
        }

    }

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

    public VisualTask getVisualTask() {
        return this.visualTask;
    }

    public void throwErrorBlockNull(Block b) throws NullPointerException {
        if (b == null)
            throw new ValidateExceptions("Block is marked non-null but is set to null.");
    }
}