package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.bukkit.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Functional interface for handling chunk events on the main server thread.
 *
 * <p>
 * This handler is invoked synchronously and may safely interact with the
 * Bukkit API, including the live {@link Chunk} instance when available.
 *
 */
@FunctionalInterface
public interface ChunkEventHandler {

    /**
     * Handles a chunk-related event synchronously on the main thread.
     *
     * @param chunkKey the affected chunk key
     * @param entry    the storage chunk entity
     * @param state    the chunk event status
     * @param chunk    the live chunk instance, or {@code null} if unavailable
     */
    void handle(@Nonnull final ChunkKey chunkKey, @Nonnull final ChunkEntry entry, @Nonnull final ChunkStatus state, @Nullable final Chunk chunk);

}