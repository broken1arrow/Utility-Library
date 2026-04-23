package org.broken.arrow.library.chunk.tracking.chunk;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.ChunkRelevanceTracker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks player movement across chunks and updates chunk relevance accordingly.
 *
 * <p>This class maintains each player's current chunk center and applies incremental
 * updates to surrounding chunks based on movement. Instead of recalculating the full
 * view distance area on every movement, it performs optimized border-diff updates
 * when players move between adjacent chunks.</p>
 *
 * <p>Chunk updates are propagated to the {@link ChunkRelevanceTracker}, where
 * player references are incremented or decremented using {@link ChunkDelta}.</p>
 *
 * <p>This tracker does not directly depend on Bukkit chunk load events and instead
 * derives chunk activity purely from player movement, allowing more efficient and
 * controlled tracking.</p>
 */
public class PlayerChunkTracker {
    private final Map<UUID, ChunkKey> playerCenter = new HashMap<>();
    private final ChunkRelevanceTracker chunkLoadLogic;
    private final int viewDistance;

    /**
     * Creates a new player chunk tracker.
     *
     * @param chunkLoadLogic the chunk relevance tracker used to apply updates
     */
    public PlayerChunkTracker(@Nonnull final ChunkRelevanceTracker chunkLoadLogic) {
        this.chunkLoadLogic = chunkLoadLogic;
        this.viewDistance = Bukkit.getViewDistance();
    }

    /**
     * Starts tracking a player and marks all chunks within view distance as loaded.
     *
     * <p>This initializes the player's chunk center and applies a full area update
     * using {@link ChunkDelta#LOAD}.</p>
     *
     * @param p the player to track
     */
    public void trackPlayer(final @Nonnull Player p) {
        ChunkKey center = ChunkKey.of(p.getLocation());
        playerCenter.put(p.getUniqueId(), center);
        applyArea(p.getUniqueId(), ChunkDelta.LOAD, center);
    }

    /**
     * Stops tracking a player and marks all previously relevant chunks as unloaded.
     *
     * <p>If the player was tracked, a full area update is applied using
     * {@link ChunkDelta#UNLOAD}.</p>
     *
     * @param p the player to untrack
     */
    public void untrackPlayer(final @Nonnull Player p) {
        ChunkKey center = playerCenter.remove(p.getUniqueId());
        if (center != null) applyArea(p.getUniqueId(), ChunkDelta.UNLOAD, center);
    }

    /**
     * Handles player movement between chunks and updates affected areas.
     *
     * <p>If the movement is larger than one chunk in any direction, a full unload/load
     * cycle is performed. Otherwise, only the border difference between the previous
     * and new positions is updated for optimal performance.</p>
     *
     * @param p    the player
     * @param from the previous chunk
     * @param to   the new chunk
     */
    public void onPlayerChunkChange(final @Nonnull Player p, final @Nonnull ChunkKey from, final @Nonnull ChunkKey to) {
        if (from.equals(to)) return;

        final int dx = to.getChunkX() - from.getChunkX();
        final int dz = to.getChunkZ() - from.getChunkZ();
        final UUID uuid = p.getUniqueId();

        if (Math.abs(dx) > 1 || Math.abs(dz) > 1 || !from.getWorldUUID().equals(to.getWorldUUID())) {
            applyArea(uuid, ChunkDelta.UNLOAD, from);
            applyArea(uuid, ChunkDelta.LOAD, to);
            playerCenter.put(uuid, to);
            return;
        }
        ChunkKey current = from;
        if (dx != 0) {
            final ChunkKey intermediate = ChunkKey.of(from.getWorld(),
                    from.getChunkX() + dx,
                    from.getChunkZ());

            applyBorderDiff(uuid, from, intermediate, dx, 0);
            current = intermediate;
        }
        if (dz != 0) {
            applyBorderDiff(uuid, current, to, 0, dz);
        }
        playerCenter.put(uuid, to);
    }

    /**
     * Returns an unmodifiable view of tracked player chunk centers.
     *
     * @return a map of player UUIDs to their current chunk center
     */
    public Map<UUID, ChunkKey> getPlayerCenter() {
        return Collections.unmodifiableMap(playerCenter);
    }

    /**
     * Applies a full area update around a center chunk.
     *
     * <p>All chunks within view distance are updated using the given delta.</p>
     *
     * @param uuid   the player UUID
     * @param delta  the change to apply
     * @param center the center chunk
     */
    private void applyArea(final @Nonnull UUID uuid, final @Nonnull ChunkDelta delta, final ChunkKey center) {
        final World world = center.getWorld();
        if (world == null) return;

        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                update(uuid, world, center.getChunkX() + dx, center.getChunkZ() + dz, delta);
            }
        }
    }

    /**
     * Applies incremental updates between two adjacent chunk positions.
     *
     * <p>Only the outer border of chunks entering and leaving the player's view
     * distance is updated, significantly reducing the number of operations compared
     * to a full area update.</p>
     *
     * @param uuid the player UUID
     * @param from the previous chunk
     * @param to   the new chunk
     * @param dx   movement along the X axis (-1, 0, 1)
     * @param dz   movement along the Z axis (-1, 0, 1)
     */
    private void applyBorderDiff(final UUID uuid, final ChunkKey from, final ChunkKey to, final int dx, final int dz) {
        final World world = from.getWorld();
        if (world == null) return;

        int fx = from.getChunkX();
        int fz = from.getChunkZ();
        int tx = to.getChunkX();
        int tz = to.getChunkZ();

        // EAST
        if (dx == 1) {
            int unloadX = fx - viewDistance;
            int loadX = tx + viewDistance;

            for (int z = -viewDistance; z <= viewDistance; z++) {
                update(uuid, world, unloadX, fz + z, ChunkDelta.UNLOAD);
                update(uuid, world, loadX, tz + z, ChunkDelta.LOAD);

            }
        }
        // WEST
        if (dx == -1) {
            int unloadX = fx + viewDistance;
            int loadX = tx - viewDistance;

            for (int z = -viewDistance; z <= viewDistance; z++) {

                update(uuid, world, unloadX, fz + z, ChunkDelta.UNLOAD);
                update(uuid, world, loadX, tz + z, ChunkDelta.LOAD);

            }
        }
        // SOUTH
        if (dz == 1) {
            int unloadZ = fz - viewDistance;
            int loadZ = tz + viewDistance;

            for (int x = -viewDistance; x <= viewDistance; x++) {

                update(uuid, world, fx + x, unloadZ, ChunkDelta.UNLOAD);
                update(uuid, world, tx + x, loadZ, ChunkDelta.LOAD);

            }
        }
        // NORTH
        if (dz == -1) {
            int unloadZ = fz + viewDistance;
            int loadZ = tz - viewDistance;

            for (int x = -viewDistance; x <= viewDistance; x++) {
                update(uuid, world, fx + x, unloadZ, ChunkDelta.UNLOAD);
                update(uuid, world, tx + x, loadZ, ChunkDelta.LOAD);

            }
        }
    }

    /**
     * Applies a single chunk update to the relevance tracker.
     *
     * <p>This increments or decrements the player reference count and updates
     * the last seen tick.</p>
     *
     * @param uuid   the player UUID
     * @param world  the world
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @param delta  the change to apply
     */
    private void update(@Nonnull final UUID uuid, @Nonnull final World world, final int chunkX, final int chunkZ, @Nonnull final ChunkDelta delta) {
        final ChunkKey key = ChunkKey.of(world, chunkX, chunkZ);

        chunkLoadLogic.updateChunk(key, cacheEntry -> {
            cacheEntry.addPlayerRefs(uuid, delta.getDelta());
            cacheEntry.markSeen();

        });
    }

    /**
     * Represents a change in player presence for a chunk.
     *
     * <p>This is used to increment or decrement the number of players
     * affecting a chunk.</p>
     */
    enum ChunkDelta {
        LOAD(+1),
        UNLOAD(-1);
        private final int delta;

        /**
         * Construct instance with the set delta.
         *
         * @param delta the delta value to set.
         */
        ChunkDelta(final int delta) {
            this.delta = delta;
        }

        /**
         * Returns the numeric value of this delta.
         *
         * @return +1 for load, -1 for unload
         */
        public int getDelta() {
            return delta;
        }
    }
}