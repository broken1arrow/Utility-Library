package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
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
 */
@FunctionalInterface
public interface AsyncChunkEventHandler {

    /**
     * Handles a chunk-related event asynchronously.
     *
     * @param chunkKey      the affected chunk key
     * @param entry         the storage chunk entity
     * @param state         the chunk event status
     * @param chunkSnapshot an optional snapshot of the chunk, or {@code null}
     */
    void handle(@Nonnull final ChunkKey chunkKey, @Nonnull final ChunkEntry entry, @Nonnull final ChunkStatus state, @Nullable final ChunkSnapshot chunkSnapshot);

}
