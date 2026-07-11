package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.utility.ChunkDelta;
import org.broken.arrow.library.serialize.utility.converters.world.ChunkKey;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Listener for player-driven chunk relevance changes.
 *
 * <p>This functional interface is used by {@link org.broken.arrow.library.chunk.tracking.chunk.PlayerChunkTracker} to emit
 * updates whenever a player's movement affects the relevance of a chunk.
 * Each invocation represents a single chunk transition event, where the
 * given {@link ChunkDelta} indicates whether the chunk is being loaded
 * into or unloaded from the player's view.</p>
 *
 * <p>Implementations are responsible for handling how these changes are
 * applied, such as updating internal tracking structures, reference counts,
 * or triggering further processing.</p>
 *
 * <p>This interface does not define how chunk data is stored or managed;
 * it only represents the propagation of chunk change events.</p>
 */
@FunctionalInterface
public interface ChunkChangeListener {

    /**
     * Called when a player's movement affects the relevance of a chunk.
     *
     * @param uuid     the unique identifier of the player causing the change
     * @param chunkKey the chunk being affected
     * @param delta    the type of change, indicating whether the chunk is
     *                 entering or leaving the player's view
     */
    void onChunkChange(@Nonnull final UUID uuid, @Nonnull final ChunkKey chunkKey, @Nonnull final ChunkDelta delta);
}
