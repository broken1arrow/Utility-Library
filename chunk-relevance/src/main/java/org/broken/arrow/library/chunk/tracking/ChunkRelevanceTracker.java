package org.broken.arrow.library.chunk.tracking;

import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.event.status.Relevance;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkAccessHandler;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkChangeHandler;
import org.broken.arrow.library.chunk.tracking.tasks.ChunkChangeDispatcher;
import org.broken.arrow.library.chunk.tracking.tasks.TickTask;
import org.broken.arrow.library.chunk.tracking.chunk.PlayerChunkTracker;
import org.broken.arrow.library.chunk.tracking.utility.ChunkState;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tracks chunk relevance by combining chunk lifecycle events and player activity.
 *
 * <p>This class maintains a lightweight cache of {@link ChunkEntry} objects representing
 * the current known state of chunks without forcing them to load. It provides a higher-level
 * abstraction over raw Bukkit chunk events by incorporating player presence and recent activity
 * into a unified {@link Relevance} model.</p>
 *
 * <p>The tracker supports both synchronous access callbacks and asynchronous change notifications,
 * allowing efficient integration without impacting server performance.</p>
 */
public class ChunkRelevanceTracker {
    private final PlayerChunkTracker playerChunkTracker = new PlayerChunkTracker(this);
    private final Map<ChunkKey, ChunkEntry> cache = new ConcurrentHashMap<>();
    private final ChunkChangeDispatcher chunkDispatcher;
    private ChunkChangeHandler chunkChange;
    private ChunkAccessHandler chunkAccess;

    /**
     * Creates and initializes the chunk relevance tracker.
     *
     * <p>This registers internal listeners for chunk and player events and starts
     * the background dispatcher responsible for asynchronous updates.</p>
     *
     * @param plugin the owning plugin instance
     */
    public ChunkRelevanceTracker(@Nonnull final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new ChunkEvent(), plugin);
        new TickTask(plugin).start();
        this.chunkDispatcher = new ChunkChangeDispatcher(plugin);
        this.chunkDispatcher.start();
    }

    /**
     * Registers an asynchronous handler for chunk state changes.
     *
     * <p>This handler is invoked off the main thread and should not interact with
     * Bukkit API methods that require the main thread.</p>
     *
     * @param chunkChange the handler to receive chunk change updates
     */
    public void onChunkChangeAsynchronous(@Nonnull final ChunkChangeHandler chunkChange) {
        this.chunkChange = chunkChange;
    }

    /**
     * Registers a synchronous handler for chunk access events.
     *
     * <p>This handler is invoked on the main thread and may safely interact with
     * Bukkit API methods.</p>
     *
     * @param chunkAccess the handler to receive chunk access updates
     */
    public void onChunkAccessSynchronous(@Nonnull final ChunkAccessHandler chunkAccess) {
        this.chunkAccess = chunkAccess;
    }

    /**
     * Retrieves the relevance of the chunk at the given location.
     *
     * @param location the location to check
     * @return the computed {@link Relevance}, or {@link Relevance#WORLD_NULL} if the world is null
     */
    @Nonnull
    public Relevance getRelevance(@Nonnull final Location location) {
        if (location.getWorld() == null) return Relevance.WORLD_NULL;
        return getRelevance(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * Retrieves the relevance of a specific chunk.
     *
     * @param world  the world
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return the computed {@link Relevance}, or {@link Relevance#NOT_CACHED} if not tracked
     */
    @Nonnull
    public Relevance getRelevance(@Nonnull final World world, final int chunkX, final int chunkZ) {
        final long now = TickTask.getTick();
        final ChunkKey key = ChunkKey.of(world, chunkX, chunkZ);
        final ChunkEntry entry = cache.get(key);

        if (entry != null) {
            return entry.getRelevance(now);
        }
        return Relevance.NOT_CACHED;
    }

    /**
     * Determines whether the chunk at the given location is considered loaded.
     *
     * <p>This does not strictly reflect Bukkit's loaded state, but instead uses
     * the internal {@link Relevance} model.</p>
     *
     * @param location the location to check
     * @return true if the chunk is considered active/relevant, false otherwise
     */
    public boolean isLocationLoaded(@Nonnull final Location location) {
        return isLocationLoaded(location, null);
    }

    /**
     * Determines whether the chunk at the given location is considered loaded,
     * optionally exposing the computed relevance.
     *
     * @param location the location to check
     * @param observer optional consumer to receive the computed relevance
     * @return true if the chunk is considered active/relevant, false otherwise
     */
    public boolean isLocationLoaded(@Nonnull final Location location, @Nullable final Consumer<Relevance> observer) {
        final Relevance relevance = this.getRelevance(location);
        if (observer != null) observer.accept(relevance);
        switch (relevance) {
            case NONE:
            case NOT_CACHED:
            case WORLD_NULL:
                return false;
            default:
                return true;
        }
    }

    /**
     * Returns an unmodifiable view of all currently tracked chunks.
     *
     * @return a map of tracked chunk keys to their entries
     */
    public Map<ChunkKey, ChunkEntry> getTrackedChunks() {
        return Collections.unmodifiableMap(cache);
    }

    /**
     * Retrieves the tracked chunk entry for the given location.
     *
     * @param location the location
     * @return the corresponding {@link ChunkEntry}, or null if not tracked
     */
    @Nullable
    public ChunkEntry getTrackedChunk(final @Nonnull Location location) {
        return this.cache.get(ChunkKey.of(location));
    }

    /**
     * Retrieves the tracked chunk entry for the given key.
     *
     * @param chunkKey the chunk key
     * @return the corresponding {@link ChunkEntry}, or null if not tracked
     */
    @Nullable
    public ChunkEntry getTrackedChunk(final @Nonnull ChunkKey chunkKey) {
        return this.cache.get(chunkKey);
    }

    /**
     * Checks whether a chunk is currently tracked.
     *
     * @param location the location to check
     * @return true if the chunk exists in the internal cache
     */
    public boolean isTracked(final @Nonnull Location location) {
        return this.cache.containsKey(ChunkKey.of(location));
    }

    /**
     * Returns the internal player chunk tracker.
     *
     * <p>This provides direct access to player chunk centers and allows
     * manual updates of player positions if needed.</p>
     *
     * @return the player chunk tracker instance
     */
    @Nonnull
    public PlayerChunkTracker getPlayerChunkTracker() {
        return playerChunkTracker;
    }

    /**
     * Removes a chunk from the tracker.
     *
     * @param chunk the chunk to remove
     */
    public void removeChunk(final Chunk chunk) {
        cache.remove(ChunkKey.of(chunk));
    }

    /**
     * Removes a chunk from the tracker.
     *
     * @param location the location representing the chunk
     */
    public void removeChunk(final @Nonnull Location location) {
        cache.remove(ChunkKey.of(location));
    }

    /**
     * Removes a chunk from the tracker.
     *
     * @param chunkKey the chunk key wrapper.
     */
    public void removeChunk(final @Nonnull ChunkKey chunkKey) {
        cache.remove(chunkKey);
    }

    /**
     * Clears all tracked chunks.
     */
    public void clearCachedChunks() {
        this.cache.clear();
    }

    /**
     * Updates the state of a chunk using a live {@link org.bukkit.Chunk}.
     *
     * <p>The provided callback allows mutation of the associated {@link ChunkEntry}
     * before relevance and status are evaluated.</p>
     *
     * @param chunk    the chunk
     * @param callback callback to modify the chunk entry
     */
    public void updateChunk(@Nonnull final Chunk chunk, final Consumer<ChunkEntry> callback) {
        this.processChunkState(ChunkKey.of(chunk), chunk, null, callback);
    }

    /**
     * Updates the state of a chunk based on a location.
     *
     * @param location the location
     * @param callback callback to modify the chunk entry
     */
    public void updateChunk(@Nonnull final Location location, @Nonnull final Consumer<ChunkEntry> callback) {
        this.processChunkState(ChunkKey.of(location), (Chunk) null, null, callback);
    }

    /**
     * Updates the state of a chunk using a chunk key.
     *
     * @param chunkKey the chunk key
     * @param callback callback to modify the chunk entry
     */
    public void updateChunk(@Nonnull final ChunkKey chunkKey, @Nonnull final Consumer<ChunkEntry> callback) {
        this.processChunkState(chunkKey, (Chunk) null, null, callback);
    }

    /**
     * Updates the state of a chunk using a snapshot and optional explicit status.
     *
     * <p>This is typically used for asynchronous or pre-captured chunk data.</p>
     *
     * @param chunk       the chunk snapshot
     * @param chunkStatus optional explicit status, or null to infer
     * @param callback    callback to modify the chunk entry
     */
    public void updateChunk(@Nonnull final ChunkSnapshot chunk, @Nullable final ChunkStatus chunkStatus, final Consumer<ChunkEntry> callback) {
        this.processChunkState(ChunkKey.of(chunk), chunk, chunkStatus, callback);
    }

    private void processChunkState(@Nonnull final ChunkKey chunkKey, @Nullable final Chunk chunk, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkStatus status = updateChunkEntry(chunkKey, chunkStatus, callback);

        if (this.chunkChange != null) {
            ChunkSnapshot snapshot = chunk != null ? chunk.getChunkSnapshot(true, false, false) : null;
            chunkDispatcher.submit(ChunkState.of(chunkKey, snapshot, status, this.chunkChange));
            //Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {this.chunkChange.onChunkChange(chunkKey, snapshot, status);}, 1);
        }
        if (this.chunkAccess != null) {
            this.chunkAccess.onChunkAccess(chunkKey, chunk, status);
        }
    }


    private void processChunkState(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkSnapshot snapshot, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkStatus status = updateChunkEntry(chunkKey, chunkStatus, callback);

        if (this.chunkChange != null) {
            chunkDispatcher.submit(ChunkState.of(chunkKey, snapshot, status, this.chunkChange));
            //Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {this.chunkChange.onChunkChange(chunkKey, snapshot, status);}, 1);
        }

        if (this.chunkAccess != null) {
            this.chunkAccess.onChunkAccess(chunkKey, null, status);
        }
    }

    @Nonnull
    private ChunkStatus updateChunkEntry(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkEntry entry = cache.computeIfAbsent(chunkKey, k -> new ChunkEntry());
        callback.accept(entry);
        return getChunkStatus(chunkStatus, entry);
    }

    @Nonnull
    private ChunkStatus getChunkStatus(@Nullable final ChunkStatus chunkStatus, @Nonnull final ChunkEntry entry) {
        final boolean playerInChunk = isPlayerInChunk(entry);
        return this.getChunkStatus(chunkStatus, playerInChunk);
    }

    @Nonnull
    private ChunkStatus getChunkStatus(@Nullable final ChunkStatus chunkStatus, final boolean playerInChunk) {
        if (chunkStatus != null) {
            return chunkStatus;
        }
        return playerInChunk ? ChunkStatus.PLAYER_LOADED : ChunkStatus.PLAYER_LEFT;
    }

    private boolean isPlayerInChunk(final ChunkEntry entry) {
        final Relevance relevance = entry.getRelevance(TickTask.getTick());
        return relevance == Relevance.FORCED || relevance == Relevance.RECENT || relevance == Relevance.PLAYER;
    }

    private class ChunkEvent implements Listener {

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void chunkUnLoad(final ChunkUnloadEvent event) {
            final Chunk chunk = event.getChunk();
            processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.UNLOADED, cacheEntry -> {
                cacheEntry.setForceLoaded(event.getChunk().isForceLoaded());
            });
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void chunkLoad(final ChunkLoadEvent event) {
            final Chunk chunk = event.getChunk();
            processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.LOADED, cacheEntry -> {
                cacheEntry.setForceLoaded(chunk.isForceLoaded());
                cacheEntry.seen(TickTask.getTick());
            });
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void onJoin(final PlayerJoinEvent e) {
            playerChunkTracker.trackPlayer(e.getPlayer());
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void onMove(final PlayerMoveEvent e) {
            playerChunkTracker.onPlayerChunkChange(
                    e.getPlayer(),
                    ChunkKey.of(e.getFrom()),
                    ChunkKey.of(e.getTo())
            );
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void onQuit(final PlayerQuitEvent e) {
            playerChunkTracker.untrackPlayer(e.getPlayer());
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
        public void onTeleport(final PlayerTeleportEvent e) {
            playerChunkTracker.onPlayerChunkChange(
                    e.getPlayer(),
                    ChunkKey.of(e.getFrom()),
                    ChunkKey.of(e.getTo())
            );

        }

    }

}

