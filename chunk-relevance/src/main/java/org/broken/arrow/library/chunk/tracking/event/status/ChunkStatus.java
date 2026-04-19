package org.broken.arrow.library.chunk.tracking.event.status;

/**
 * Represents the state or cause of a chunk event.
 *
 * <p>
 * {@code ChunkStatus} describes why a chunk-related event was triggered,
 * such as loading, unloading, or player interaction. It is used to
 * distinguish the type of change when handling chunk updates.
 */
public enum ChunkStatus {

    /**
     * Indicates that the chunk has been loaded into memory.
     */
    LOADED,

    /**
     * Indicates that the chunk has been unloaded from memory.
     */
    UNLOADED,

    /**
     * Indicates that a player has entered the chunk.
     * <p>
     * This does not necessarily mean the chunk was newly loaded,
     * only that a player is now present within it.
     */
    PLAYER_ENTERED,

    /**
     * Indicates that a player has left the chunk.
     * <p>
     * This may occur before the chunk is actually unloaded if no
     * players remain nearby.
     */
    PLAYER_EXITED

}