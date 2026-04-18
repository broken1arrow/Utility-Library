package org.broken.arrow.library.chunk.tracking.event.status;

/**
 * Represents the current relevance state of a chunk.
 *
 * <p>
 * Relevance is derived from volatile, runtime-only signals such as
 * player presence, forced loading, and recent activity. It is used
 * to guide cache behavior and chunk-related logic.
 */
public enum Relevance {
    /**
     * The chunk is currently within at least one player's tracked area.
     */
    PLAYER,
    /**
     * The chunk is explicitly marked as force-loaded.
     */
    FORCED,
    /**
     * The chunk was recently observed but is no longer directly
     * referenced by any player.
     */
    RECENT,
    /**
     * The chunk has no active or recent relevance.
     */
    NONE,
    /**
     * No cache entry exists for the chunk.
     */
    NOT_CACHED,
    /**
     * The chunk's world could not be resolved.
     */
    WORLD_NULL
}