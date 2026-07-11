package org.broken.arrow.library.chunk.tracking.chunk;

import org.broken.arrow.library.chunk.tracking.handlers.ChunkChangeListener;
import org.broken.arrow.library.chunk.tracking.utility.ChunkDelta;
import org.broken.arrow.library.serialize.utility.converters.world.ChunkKey;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player movement across chunks and emits chunk relevance updates.
 *
 * <p>This class maintains each player's current chunk center and detects when
 * movement affects the set of chunks within their view distance. Instead of
 * recalculating the entire area on every movement, it performs optimized
 * incremental updates when players move between adjacent chunks.</p>
 *
 * <p>For each affected chunk, a {@link ChunkChangeListener} is invoked to signal
 * whether the chunk is entering or leaving the player's view.</p>
 *
 * <p>This class does not interact with chunk storage or lifecycle systems and
 * does not depend on Bukkit chunk load events. It derives all updates purely
 * from player movement.</p>
 */
public class PlayerChunkTracker {
    private final Map<UUID, ChunkKey> playerCenter = new ConcurrentHashMap<>();
    private final ChunkChangeListener playerChunkEvent;
    private final int viewDistance;


    /**
     * Creates a new player chunk tracker.
     *
     * @param playerChunkEvent the listener that will receive chunk relevance updates
     */
    public PlayerChunkTracker(@Nonnull final ChunkChangeListener playerChunkEvent) {
        this.playerChunkEvent = playerChunkEvent;
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
     * @param p  the player
     * @param to to the new chunk the player enters.
     */
    public void onPlayerChunkChange(final @Nonnull Player p, final @Nonnull ChunkKey to) {
        final UUID uuid = p.getUniqueId();
        final ChunkKey currentCenter = playerCenter.get(uuid);
        if (currentCenter == null || to.equals(currentCenter)) return;

        final int dx = to.getChunkX() - currentCenter.getChunkX();
        final int dz = to.getChunkZ() - currentCenter.getChunkZ();

        if (Math.abs(dx) > 1 || Math.abs(dz) > 1 || !currentCenter.getWorldUUID().equals(to.getWorldUUID())) {
            applyArea(uuid, ChunkDelta.UNLOAD, currentCenter);
            applyArea(uuid, ChunkDelta.LOAD, to);
            playerCenter.put(uuid, to);
            return;
        }
        ChunkKey stepFrom = currentCenter;
        if (dx != 0) {
            final ChunkKey intermediate = ChunkKey.of(currentCenter.getWorldUUID(),
                    currentCenter.getChunkX() + dx,
                    currentCenter.getChunkZ());

            applyBorderDiff(uuid, currentCenter, intermediate, dx, 0);
            stepFrom = intermediate;
        }
        if (dz != 0) {
            applyBorderDiff(uuid, stepFrom, to, 0, dz);
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
        final ChunkKey chunkKey = ChunkKey.of(world, chunkX, chunkZ);
        playerChunkEvent.onChunkChange(uuid, chunkKey, delta);
    }
}