package org.broken.arrow.library.chunk.tracking.event;

import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.handlers.AsyncChunkEventHandler;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkEventHandler;

/**
 * Utility class providing adapters for filtering chunk lifecycle events.
 *
 * <p>
 * These adapters wrap existing {@link ChunkEventHandler} and
 * {@link AsyncChunkEventHandler} instances and forward only lifecycle-related
 * events, specifically {@link ChunkStatus#LOADED} and
 * {@link ChunkStatus#UNLOADED}.
 *
 * <p>
 * This allows consumers to focus exclusively on chunk load and unload behavior
 * without manually filtering {@link ChunkStatus} values.
 *
 * <p>
 * <strong>Note:</strong> Event ordering between lifecycle and player-related
 * events is not strictly guaranteed. If consistent player tracking is required,
 * prefer using {@link PlayerChunkEvent}.
 */
public final class LifecycleChunkEvent {

    private LifecycleChunkEvent() {}

    /**
     * Wraps a synchronous {@link ChunkEventHandler}, forwarding only lifecycle events.
     *
     * @param handler the handler to receive filtered lifecycle events
     * @return a wrapped handler that only receives {@link ChunkStatus#LOADED}
     *         and {@link ChunkStatus#UNLOADED} events
     */
    public static ChunkEventHandler sync(ChunkEventHandler handler) {
        return (key, entry, status, chunk) -> {
            if (status == ChunkStatus.LOADED ||
                    status == ChunkStatus.UNLOADED) {
                handler.handle(key, entry, status, chunk);
            }
        };
    }

    /**
     * Wraps an asynchronous {@link AsyncChunkEventHandler}, forwarding only lifecycle events.
     *
     * @param handler the handler to receive filtered lifecycle events
     * @return a wrapped handler that only receives {@link ChunkStatus#LOADED}
     *         and {@link ChunkStatus#UNLOADED} events
     */
    public static AsyncChunkEventHandler async(AsyncChunkEventHandler handler) {
        return (key, entry, status, snapshot) -> {
            if (status == ChunkStatus.LOADED ||
                    status == ChunkStatus.UNLOADED) {
                handler.handle(key, entry, status, snapshot);
            }
        };
    }

}
