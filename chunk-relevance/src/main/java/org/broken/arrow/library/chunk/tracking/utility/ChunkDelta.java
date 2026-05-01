package org.broken.arrow.library.chunk.tracking.utility;

/**
 * Represents a change in a chunk's relevance caused by player activity.
 *
 * <p>This is typically used for reference counting, where each value
 * contributes to the number of players affecting a chunk:</p>
 *
 * <ul>
 *     <li>{@link #LOAD} → increments the reference count</li>
 *     <li>{@link #UNLOAD} → decrements the reference count</li>
 * </ul>
 */
public enum ChunkDelta {
    LOAD(+1),
    UNLOAD(-1);
    private final int delta;

    /**
     * Creates a new delta value.
     *
     * @param delta the numeric representation of the change
     */
    ChunkDelta(final int delta) {
        this.delta = delta;
    }

    /**
     * Returns the numeric value of this delta.
     *
     * @return +1 for load, -1 for unload
     */
    public int getDelta() {
        return delta;
    }
}