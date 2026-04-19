package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.bukkit.ChunkSnapshot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Functional interface for handling chunk events asynchronously.
 *
 * <p>
 * Implementations are invoked off the main server thread and must not call
 * Bukkit API methods that are not thread-safe.
 *
 * <p>
 * This handler is typically used for background processing such as caching,
 * persistence, or analysis where direct world interaction is not required.
 *
 * <p>
 * <strong>Important:</strong> Chunk lifecycle events and player-related events are not
 * strictly ordered when processed asynchronously. A player entering a chunk
 * may be observed before or after the corresponding chunk load event.
 *
 * <p>
 * Because of this, mixing lifecycle and player-driven states may lead to
 * inconsistent behavior.
 *
 * <p>
 * For player tracking, rely exclusively on
 * {@link ChunkStatus#PLAYER_ENTERED} and {@link ChunkStatus#PLAYER_EXITED}.
 */
@FunctionalInterface
public interface AsyncChunkEventHandler {

    /**
     * Handles a chunk-related event asynchronously.
     *
     * @param chunkKey the affected chunk
     * @param chunkSnapshot an optional snapshot of the chunk, or {@code null}
     * @param state the chunk event status
     */
    void handle(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkSnapshot chunkSnapshot, @Nonnull final ChunkStatus state);

}
