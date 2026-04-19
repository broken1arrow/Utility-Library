package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
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
 * <p>
 * Events may represent chunk lifecycle changes (load/unload) or player
 * interactions (enter/exit), depending on the provided {@link ChunkStatus}.
 *
 * <p>
 * <strong>Important:</strong> Ordering between chunk lifecycle events and player
 * movement events is not strictly guaranteed. Do not assume perfect
 * sequencing between them.
 *
 * <p>
 * For player tracking, rely exclusively on
 * {@link ChunkStatus#PLAYER_ENTERED} and {@link ChunkStatus#PLAYER_EXITED}.
 */
@FunctionalInterface
public interface ChunkEventHandler {

    /**
     * Handles a chunk-related event synchronously on the main thread.
     *
     * @param chunkKey the affected chunk
     * @param chunk the live chunk instance, or {@code null} if unavailable
     * @param state the chunk event status
     */
    void handle(@Nonnull final ChunkKey chunkKey, @Nullable final Chunk chunk, @Nonnull final ChunkStatus state);

}