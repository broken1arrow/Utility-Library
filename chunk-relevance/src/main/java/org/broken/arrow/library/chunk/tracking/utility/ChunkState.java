package org.broken.arrow.library.chunk.tracking.utility;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.handlers.AsyncChunkEventHandler;
import org.bukkit.ChunkSnapshot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a deferred chunk state change.
 *
 * <p>
 * A {@code ChunkState} encapsulates all data required to process a chunk
 * update at a later time, including the chunk identifier, an optional
 * snapshot of the chunk, the resulting {@link ChunkStatus}, and the
 * {@link AsyncChunkEventHandler} responsible for handling the change.
 *
 * <p>
 * Instances of this class are typically submitted to a dispatcher (e.g.
 * {@code ChunkChangeDispatcher}) to allow batching or delayed processing
 * of chunk updates. If multiple updates for the same {@link ChunkKey}
 * are submitted, only the most recent state may be applied.
 *
 * <p>
 * The snapshot may be {@code null} if no snapshot is available or if the
 * chunk is no longer loaded at the time of submission.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public class ChunkState {
    private final ChunkKey key;
    private final ChunkEntry entry;
    private final ChunkSnapshot snapshot;
    private final ChunkStatus state;
    private final AsyncChunkEventHandler handler;

    /**
     * Creates a new chunk state.
     *
     * @param key      the chunk key identifying the chunk
     * @param entry    the storage chunk entity
     * @param snapshot an optional snapshot of the chunk, or {@code null}
     * @param state    the resulting chunk status
     * @param handler  the handler that will process the chunk change
     */
    private ChunkState(@Nonnull final ChunkKey key, @Nonnull final ChunkEntry entry, @Nonnull final ChunkStatus state, @Nullable final ChunkSnapshot snapshot, @Nonnull final AsyncChunkEventHandler handler) {
        this.key = key;
        this.entry = entry;
        this.snapshot = snapshot;
        this.state = state;
        this.handler = handler;
    }

    /**
     * Creates a new {@code ChunkState} instance.
     *
     * @param key      the chunk key identifying the chunk
     * @param entry    the storage chunk entity
     * @param snapshot an optional snapshot of the chunk, or {@code null}
     * @param state    the resulting chunk status
     * @param handler  the handler that will process the chunk change
     * @return a new chunk state instance
     */
    public static ChunkState of(@Nonnull final ChunkKey key, @Nonnull final ChunkEntry entry, @Nullable final ChunkSnapshot snapshot, @Nonnull final ChunkStatus state, @Nonnull final AsyncChunkEventHandler handler) {
        return new ChunkState(key, entry, state, snapshot, handler);
    }

    /**
     * Returns the chunk key associated with this state.
     *
     * @return the chunk key
     */
    public ChunkKey getKey() {
        return key;
    }

    /**
     * Applies this chunk state by invoking the associated handler.
     *
     * <p>
     * This method triggers {@link AsyncChunkEventHandler#handle(ChunkKey, ChunkEntry, ChunkStatus, ChunkSnapshot)}
     * with the stored data.
     *
     * <p>
     * This is typically called by a dispatcher and should not be invoked
     * directly unless immediate processing is desired.
     */
    public void apply() {
        handler.handle(key, entry, state, snapshot);
    }
}