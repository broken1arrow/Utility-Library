package org.broken.arrow.utility.library.listner;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.chunk.PlayerChunkTracker;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.utility.library.chunk.tracker.ChunkRelevanceTrackerWrapper;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import javax.annotation.Nonnull;

/**
 * Central listener that delegates events to module-specific bukkit events.
 */
public class UtilityListener implements Listener {
    private final ChunkRelevanceTrackerWrapper chunkRelevanceTracker;
    private final PlayerChunkTracker playerChunkTracker;


    /**
     * Creates a new listener instance.
     *
     * @param chunkRelevanceTracker main chunk relevance handler
     */
    public UtilityListener(@Nonnull final ChunkRelevanceTrackerWrapper chunkRelevanceTracker) {
        this.chunkRelevanceTracker = chunkRelevanceTracker;
        this.playerChunkTracker = chunkRelevanceTracker.getPlayerChunkTracker();
    }

    /**
     * Event called when chunk unloading.
     *
     * @param event the unload event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void chunkUnLoad(final ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        this.chunkRelevanceTracker.processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.UNLOADED, cacheEntry -> {
            cacheEntry.setForceLoaded(chunk.isForceLoaded());
        });
    }

    /**
     *  Handles chunk load events.
     *
     * @param event the load event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void chunkLoad(final ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        this.chunkRelevanceTracker.processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.LOADED, cacheEntry -> {
            cacheEntry.setForceLoaded(chunk.isForceLoaded());
            cacheEntry.markSeen();
        });
    }

    /**
     * Event called when player join server.
     *
     * @param e the load event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent e) {
        this.playerChunkTracker.trackPlayer(e.getPlayer());
    }

    /**
     * Updates player chunk on movement.
     *
     * @param e the player move event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onMove(final PlayerMoveEvent e) {
        this.playerChunkTracker.onPlayerChunkChange(
                e.getPlayer(),
                ChunkKey.of(e.getTo())
        );
    }

    /**
     * Untracks player on quit.
     *
     * @param e the player move event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent e) {
        this.playerChunkTracker.untrackPlayer(e.getPlayer());
    }

    /**
     * Updates player chunk on teleport.
     *
     * @param e the player teleport.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onTeleport(final PlayerTeleportEvent e) {
        this.playerChunkTracker.onPlayerChunkChange(
                e.getPlayer(),
                ChunkKey.of(e.getTo())
        );

    }
}
