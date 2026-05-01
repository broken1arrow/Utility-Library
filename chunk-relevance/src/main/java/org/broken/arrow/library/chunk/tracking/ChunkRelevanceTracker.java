package org.broken.arrow.library.chunk.tracking;

import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.event.status.Relevance;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkEventHandler;
import org.broken.arrow.library.chunk.tracking.handlers.AsyncChunkEventHandler;
import org.broken.arrow.library.chunk.tracking.tasks.ChunkChangeDispatcher;
import org.broken.arrow.library.chunk.tracking.tasks.TickClock;
import org.broken.arrow.library.chunk.tracking.chunk.PlayerChunkTracker;
import org.broken.arrow.library.chunk.tracking.utility.ChunkDelta;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Aggregates chunk relevance based on player activity and chunk lifecycle events.
 *
 * <p>This class maintains a cache of {@link ChunkEntry} instances representing
 * the current relevance state of chunks. It combines player-driven updates and
 * chunk lifecycle signals into a unified {@link Relevance} model.</p>
 *
 * <p>Updates are received from internal systems such as {@link PlayerChunkTracker}
 * and processed into the cache, where relevance is computed and optionally
 * dispatched to registered listeners.</p>
 *
 * <p>This class acts as the central coordination point for chunk relevance and
 * provides both synchronous and asynchronous access to chunk state.</p>
 */
public class ChunkRelevanceTracker {
    private final PlayerChunkTracker playerChunkTracker;
    private final Map<ChunkKey, ChunkEntry> chunksTracked = new ConcurrentHashMap<>();
    private final ChunkChangeDispatcher chunkDispatcher;
    private AsyncChunkEventHandler chunkChange;
    private ChunkEventHandler chunkAccess;

    /**
     * Creates and initializes the chunk relevance tracker.
     *
     * <p>This sets up internal trackers, registers event listeners, and starts
     * the background dispatcher responsible for propagating chunk updates.</p>
     *
     * @param plugin the owning plugin instance
     */
    protected ChunkRelevanceTracker(@Nonnull final Plugin plugin) {
        this.playerChunkTracker = new PlayerChunkTracker(this::handlePlayerChunkChange);

        this.registerListener(plugin);
        new TickClock(plugin).start();
        this.chunkDispatcher = new ChunkChangeDispatcher(plugin);
        this.chunkDispatcher.start();
    }

    /**
     * Registers an asynchronous handler for chunk state changes.
     *
     * <p>
     * This handler is invoked off the main server thread and must not interact with
     * Bukkit API methods that are not thread-safe.
     *
     * <p>
     * <strong>Important:</strong> When processed asynchronously, the order between
     * chunk lifecycle events (load/unload) and player-related events is not guaranteed.
     * A player entering a chunk may be observed before or after the corresponding
     * chunk load event.
     *
     * <p>
     * Because of this, mixing chunk lifecycle states with player-driven states may
     * result in inconsistent logic.
     *
     * <p>
     * <strong>Recommendation:</strong> For player tracking, rely exclusively on
     * {@link ChunkStatus#PLAYER_ENTERED} and {@link ChunkStatus#PLAYER_EXITED}.
     *
     * @param chunkChange the handler to receive chunk change updates
     */
    public void onChunkEventAsynchronous(@Nonnull final AsyncChunkEventHandler chunkChange) {
        this.chunkChange = chunkChange;
    }

    /**
     * Registers a synchronous handler for chunk events.
     *
     * <p>
     * This handler is invoked on the main server thread and may safely interact with
     * Bukkit API methods.
     *
     * <p>
     * <strong>Important:</strong> Even when processed synchronously, the order between
     * chunk lifecycle events (load/unload) and player-related events is not guaranteed
     * to be perfectly aligned. A player entering or leaving a chunk may not occur in
     * strict sequence with the corresponding chunk state changes.
     *
     * <p>
     * Because of this, mixing chunk lifecycle states with player-driven states may
     * result in inconsistent logic.
     *
     * <p>
     * <strong>Recommendation:</strong> For player tracking, rely exclusively on
     * {@link ChunkStatus#PLAYER_ENTERED} and {@link ChunkStatus#PLAYER_EXITED}.
     *
     * @param chunkAccess the handler to receive chunk event updates
     */
    public void onChunkEventSynchronous(@Nonnull final ChunkEventHandler chunkAccess) {
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
        final ChunkKey key = ChunkKey.of(world, chunkX, chunkZ);
        final ChunkEntry entry = chunksTracked.get(key);

        if (entry != null) {
            return entry.getRelevance();
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
        return Collections.unmodifiableMap(chunksTracked);
    }

    /**
     * Retrieves the tracked chunk entry for the given location.
     *
     * @param location the location
     * @return the corresponding {@link ChunkEntry}, or null if not tracked
     */
    @Nullable
    public ChunkEntry getTrackedChunk(final @Nonnull Location location) {
        return this.chunksTracked.get(ChunkKey.of(location));
    }

    /**
     * Retrieves the tracked chunk entry for the given key.
     *
     * @param chunkKey the chunk key
     * @return the corresponding {@link ChunkEntry}, or null if not tracked
     */
    @Nullable
    public ChunkEntry getTrackedChunk(final @Nonnull ChunkKey chunkKey) {
        return this.chunksTracked.get(chunkKey);
    }

    /**
     * Checks whether a chunk is currently tracked.
     *
     * @param location the location to check
     * @return true if the chunk exists in the internal cache
     */
    public boolean isTracked(final @Nonnull Location location) {
        return this.chunksTracked.containsKey(ChunkKey.of(location));
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
        chunksTracked.remove(ChunkKey.of(chunk));
    }

    /**
     * Removes a chunk from the tracker.
     *
     * @param location the location representing the chunk
     */
    public void removeChunk(final @Nonnull Location location) {
        chunksTracked.remove(ChunkKey.of(location));
    }

    /**
     * Removes a chunk from the tracker.
     *
     * @param chunkKey the chunk key wrapper.
     */
    public void removeChunk(final @Nonnull ChunkKey chunkKey) {
        chunksTracked.remove(chunkKey);
    }

    /**
     * Clears all tracked chunks.
     */
    public void clearCachedChunks() {
        this.chunksTracked.clear();
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

    /**
     * Registers the internal Bukkit listener responsible for handling
     * player movement and chunk lifecycle events (load/unload).
     *
     * <p>This method is invoked automatically when the tracker is created
     * with listener lifecycle management enabled. Subclasses may override
     * this method to customize how and where event listeners are registered,
     * for example when delegating to a shared or centralized listener system.</p>
     *
     * <p>Overriding this method implies that the caller is responsible for
     * ensuring that all relevant events are correctly forwarded to this tracker.</p>
     *
     * @param plugin the owning plugin instance used for event registration
     */
    protected void registerListener(@Nonnull final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new BukkitChunkListener(), plugin);
    }

    /**
     * Processes a chunk state transition using a live {@link Chunk} reference.
     *
     * <p>This method updates the internal {@link ChunkEntry} state, resolves the
     * effective {@link ChunkStatus}, and propagates the change to all registered
     * handlers such as the chunk dispatcher and access listeners.</p>
     *
     * <p>This is the primary entry point used by the internal Bukkit listener for
     * chunk load and unload events. Subclasses may call this method when integrating
     * with a custom or centralized event system.</p>
     *
     * @param chunkKey   the unique key identifying the chunk
     * @param chunk      the live chunk instance, or {@code null} if not available
     * @param chunkStatus the new chunk status, or {@code null} to infer from existing state
     * @param callback   a mutator applied to the {@link ChunkEntry} before propagation
     */
    protected void processChunkState(@Nonnull final ChunkKey chunkKey, @Nullable final Chunk chunk, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkEntry entry = updateChunkEntry(chunkKey, chunkStatus, callback);
        final ChunkStatus status = getChunkStatus(chunkStatus, entry);

        if (this.chunkChange != null) {
            ChunkSnapshot snapshot = chunk != null ? chunk.getChunkSnapshot(true, false, false) : null;
            chunkDispatcher.submit(ChunkState.of(chunkKey, entry, snapshot, status, this.chunkChange));
        }
        if (this.chunkAccess != null) {
            this.chunkAccess.handle(chunkKey, entry, status, chunk);
        }
    }

    /**
     * Processes a chunk state transition using a precomputed {@link ChunkSnapshot}.
     *
     * <p>This variant is useful when chunk data has already been captured or when
     * operating outside the main thread where direct access to a live {@link Chunk}
     * instance may not be safe or available.</p>
     *
     * <p>The method updates the internal {@link ChunkEntry}, resolves the effective
     * {@link ChunkStatus}, and dispatches the resulting state to all registered
     * handlers.</p>
     *
     * @param chunkKey    the unique key identifying the chunk
     * @param snapshot    a snapshot of the chunk state, or {@code null} if not available
     * @param chunkStatus the new chunk status, or {@code null} to infer from existing state
     * @param callback    a mutator applied to the {@link ChunkEntry} before propagation
     */
    protected void processChunkState(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkSnapshot snapshot, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkEntry entry = updateChunkEntry(chunkKey, chunkStatus, callback);
        final ChunkStatus status = getChunkStatus(chunkStatus, entry);
        if (this.chunkChange != null) {
            chunkDispatcher.submit(ChunkState.of(chunkKey, entry, snapshot, status, this.chunkChange));
        }

        if (this.chunkAccess != null) {
            this.chunkAccess.handle(chunkKey, entry, status, null);
        }
    }

    @Nonnull
    private ChunkEntry updateChunkEntry(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkStatus chunkStatus, @Nonnull final Consumer<ChunkEntry> callback) {
        final ChunkEntry entry = chunksTracked.computeIfAbsent(chunkKey, k -> new ChunkEntry());
        callback.accept(entry);
        return entry;
    }

    @Nonnull
    private ChunkStatus getChunkStatus(@Nullable final ChunkStatus chunkStatus, @Nonnull final ChunkEntry entry) {
        final boolean playerInChunk = isPlayerInChunk(entry);
        if (chunkStatus != null) {
            return chunkStatus;
        }
        return playerInChunk ? ChunkStatus.PLAYER_ENTERED : ChunkStatus.PLAYER_EXITED;
    }

    private boolean isPlayerInChunk(final ChunkEntry entry) {
        final Relevance relevance = entry.getRelevance();
        return relevance == Relevance.FORCED || relevance == Relevance.RECENT || relevance == Relevance.PLAYER;
    }

    private void handlePlayerChunkChange(@Nonnull final UUID uuid, @Nonnull final ChunkKey chunkKey, @Nonnull final ChunkDelta delta) {
        updateChunk(chunkKey, cacheEntry -> {
            cacheEntry.addPlayerRefs(uuid, delta.getDelta());
            cacheEntry.markSeen();
        });
    }

    private class BukkitChunkListener implements Listener {

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
                cacheEntry.markSeen();
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
                    ChunkKey.of(e.getTo())
            );

        }

    }

}

