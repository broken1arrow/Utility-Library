package org.broken.arrow.visualization.library;

import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.logging.library.Validate.ValidateExceptions;
import org.broken.arrow.visualization.library.builders.VisualizeData;
import org.broken.arrow.visualization.library.runnable.VisualTask;
import org.broken.arrow.visualization.library.utility.EntityModifications;
import org.broken.arrow.visualization.library.utility.Function;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockVisualizerCache {
	private final Map<Location, VisualizeData> visualizedBlocks = new ConcurrentHashMap<>();
	private final VisualTask visualTask;
	private final BlockVisualize blockVisualize;
	private final EntityModifications entityModifications;


	public BlockVisualizerCache(Plugin plugin, BlockVisualize blockVisualize) {
		this.visualTask = new VisualTask(plugin, this, visualizedBlocks);
		this.blockVisualize = blockVisualize;
		this.entityModifications = new EntityModifications(blockVisualize.getServerVersion());
	}

	public void visualize(@Nullable final Player viewer, @Nonnull final Block block, @Nonnull final Function<VisualizeData> visualizeDataFunction) {
		if (block == null) {
			this.throwErrorBlockNull();
		} else {
			if (isVisualized(block)) {
				stopVisualizing(block);
				getVisualTask().stop();
			}
			Validate.checkBoolean(isVisualized(block), "Block at " + block.getLocation() + " already visualized");
			final Location location = block.getLocation();
			VisualizeData visualizeData = visualizeDataFunction.apply();
			visualizeData.setFallingBlock(entityModifications.spawnFallingBlock(location, visualizeData.getMask(), visualizeData.getText()));

			final Iterator<Player> players = block.getWorld().getPlayers().iterator();
			this.setVisualData(visualizeData, location, players, viewer);
			visualizedBlocks.put(location, visualizeData);
		}
	}

	public EntityModifications getEntityModifications() {
		return entityModifications;
	}

	public void stopVisualizing(@Nonnull final Block block) {
		if (block == null) {
			this.throwErrorBlockNull();
		} else {
			Validate.checkBoolean(!isVisualized(block), "Block at " + block.getLocation() + " not visualized");
			final VisualizeData visualizeData = visualizedBlocks.remove(block.getLocation());
			final FallingBlock fallingBlock = visualizeData.getFallingBlock();

			if (fallingBlock != null) {
				fallingBlock.remove();
			}
			final Set<Player> playersAllowed = visualizeData.getPlayersAllowed();
			final Iterator<Player> players;
			if (playersAllowed != null && !playersAllowed.isEmpty()) players = playersAllowed.iterator();
			else players = block.getWorld().getPlayers().iterator();
			while (players.hasNext()) {
				final Player player = players.next();
				sendBlockChange(1, player, block.getLocation().getBlock());
			}
		}

	}

	public void stopAll() {
		for (final Location location : visualizedBlocks.keySet()) {
			final Block block = location.getBlock();
			if (isVisualized(block)) {
				stopVisualizing(block);
			}
		}

	}

	public boolean isVisualized(@Nonnull final Block block) {
		if (block == null) {
			this.throwErrorBlockNull();
		} else {
			return visualizedBlocks.containsKey(block.getLocation());
		}
		return false;
	}


	public void sendBlockChange(final int delayTicks, final Player player, final Location location, final Material material) {
		if (delayTicks > 0) {
			this.blockVisualize.runTaskLater(delayTicks, () -> {
				sendBlockChange0(player, location, material);
			}, false);
		} else {
			sendBlockChange0(player, location, material);
		}

	}

	public void sendBlockChange(final int delayTicks, final Player player, final Block block) {
		if (delayTicks > 0) {
			this.blockVisualize.runTaskLater(delayTicks, () -> {
				sendBlockChange0(player, block);
			}, false);
		} else {
			sendBlockChange0(player, block);
		}

	}

	private void sendBlockChange0(final Player player, final Block block) {
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

	private void setPlayersInCache(final VisualizeData visualizeData, final Location location, final Player player) {
		visualizeData.addPlayersAllowed(player);
		sendBlockChange(2, player, location, this.blockVisualize.getServerVersion() < 9.0 ? visualizeData.getMask() : Material.BARRIER);
	}

	public VisualTask getVisualTask() {
		return this.visualTask;
	}

	public void throwErrorBlockNull() throws NullPointerException {
		throw new ValidateExceptions("block is marked non-null but is null");
	}
}