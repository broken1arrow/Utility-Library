package org.broken.arrow.library.chunk.tracking.event;

import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.handlers.AsyncChunkEventHandler;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkEventHandler;

/**
 * Utility class providing adapters for filtering player-related chunk events.
 *
 * <p>
 * These adapters wrap existing {@link ChunkEventHandler} and
 * {@link AsyncChunkEventHandler} instances and forward only player-driven
 * events, specifically {@link ChunkStatus#PLAYER_ENTERED} and
 * {@link ChunkStatus#PLAYER_EXITED}.
 *
 * <p>
 * This is useful for tracking player presence or movement within chunks
 * without needing to manually filter {@link ChunkStatus} values.
 *
 * <p>
 * <strong>Recommendation:</strong> When tracking player behavior, rely solely on
 * player-related events rather than mixing them with chunk lifecycle events,
 * as ordering between these event types is not strictly guaranteed.
 */
public final class PlayerChunkEvents {

    /**
     * Wraps a synchronous {@link ChunkEventHandler}, forwarding only player-related events.
     *
     * @param handler the handler to receive filtered player events
     * @return a wrapped handler that only receives
     *         {@link ChunkStatus#PLAYER_ENTERED} and
     *         {@link ChunkStatus#PLAYER_EXITED} events
     */
    public static ChunkEventHandler sync(ChunkEventHandler handler) {
        return (key, status, chunk) -> {
            if (status == ChunkStatus.PLAYER_ENTERED ||
                status == ChunkStatus.PLAYER_EXITED) {
                handler.handle(key, status, chunk);
            }
        };
    }
    /**
     * Wraps an asynchronous {@link AsyncChunkEventHandler}, forwarding only player-related events.
     *
     * @param handler the handler to receive filtered player events
     * @return a wrapped handler that only receives
     *         {@link ChunkStatus#PLAYER_ENTERED} and
     *         {@link ChunkStatus#PLAYER_EXITED} events
     */
    public static AsyncChunkEventHandler async(AsyncChunkEventHandler handler) {
        return (key, status, snapshot) -> {
            if (status == ChunkStatus.PLAYER_ENTERED ||
                status == ChunkStatus.PLAYER_EXITED) {
                handler.handle(key, status, snapshot);
            }
        };
    }
}