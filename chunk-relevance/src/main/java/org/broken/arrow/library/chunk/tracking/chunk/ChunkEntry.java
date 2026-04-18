package org.broken.arrow.library.chunk.tracking.chunk;

import org.broken.arrow.library.chunk.tracking.event.status.Relevance;


import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the tracked state of a single chunk.
 *
 * <p>This class maintains a lightweight model of chunk relevance based on:
 * <ul>
 *     <li>Active player presence</li>
 *     <li>Forced loading state</li>
 *     <li>Recent activity (decay-based)</li>
 * </ul>
 *
 * <p>Relevance is derived dynamically using {@link #getRelevance(long)},
 * allowing consumers to determine how "active" a chunk is without relying
 * on Bukkit's internal chunk state.</p>
 *
 * <p>This implementation is thread-safe for concurrent updates, using
 * atomic counters and volatile fields where necessary.</p>
 */
public class ChunkEntry {
    private static final long DECAY_TICKS = 80;
    private final Set<UUID> playersChunk = ConcurrentHashMap.newKeySet();
    private final AtomicInteger playerRefs = new AtomicInteger();
    private volatile boolean forceLoaded;
    private volatile long lastSeenTick;

    /**
     * Updates the number of players affecting this chunk.
     *
     * <p>When {@code delta > 0}, the player is added if not already present
     * and the reference count is incremented. When {@code delta < 0}, the
     * player is removed and the reference count is decremented.</p>
     *
     * <p>The reference count is clamped to zero to prevent negative values.</p>
     *
     * @param uuid  the player UUID
     * @param delta the change to apply (+1 for load, -1 for unload)
     */
    public void addPlayerRefs(@Nonnull final UUID uuid, int delta) {
        if (delta > 0) {
            if (!playersChunk.add(uuid)) return;
            playerRefs.incrementAndGet();
            return;
        }
        if (playersChunk.remove(uuid)) {
            int updated = playerRefs.updateAndGet(v -> Math.max(0, v - 1));
            if (updated < 0) playerRefs.set(0);
        }
    }

    /**
     * Updates the last seen tick for this chunk.
     *
     * <p>This is used to determine whether the chunk should be considered
     * {@link Relevance#RECENT}.</p>
     *
     * @param now the current tick
     */
    public void seen(long now) {
        lastSeenTick = now;
    }

    /**
     * Sets whether this chunk is forcefully kept relevant.
     *
     * <p>Forced chunks are considered {@link Relevance#FORCED} unless
     * overridden by active player presence.</p>
     *
     * @param forceLoaded true to force relevance, false otherwise
     */
    public void setForceLoaded(boolean forceLoaded) {
        this.forceLoaded = forceLoaded;
    }

    /**
     * Returns the number of players currently affecting this chunk.
     *
     * @return the player reference count
     */
    public int getPlayerRefs() {
        return playerRefs.get();
    }

    /**
     * Returns whether this chunk is forcefully kept relevant.
     *
     * @return true if forced, false otherwise
     */
    public boolean isForceLoaded() {
        return forceLoaded;
    }

    /**
     * Returns the last tick this chunk was marked as seen.
     *
     * @return the last seen tick
     */
    public long getLastSeenTick() {
        return lastSeenTick;
    }

    /**
     * Returns an unmodifiable view of players currently affecting this chunk.
     *
     * <p>This reflects players contributing to {@link Relevance#PLAYER}.</p>
     *
     * @return a set of player UUIDs
     */
    public @Nonnull Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(playersChunk);
    }

    /**
     * Computes the current relevance of this chunk.
     *
     * <p>Relevance is determined in the following priority order:
     * <ol>
     *     <li>{@link Relevance#PLAYER} if one or more players are present</li>
     *     <li>{@link Relevance#FORCED} if the chunk is force-loaded</li>
     *     <li>{@link Relevance#RECENT} if recently seen within a decay window</li>
     *     <li>{@link Relevance#NONE} otherwise</li>
     * </ol>
     *
     * @param now the current tick
     * @return the computed relevance
     */
    public @Nonnull Relevance getRelevance(long now) {
        if (playerRefs.get() > 0) return Relevance.PLAYER;
        if (forceLoaded) return Relevance.FORCED;
        if (now - lastSeenTick < DECAY_TICKS) return Relevance.RECENT;
        return Relevance.NONE;
    }
}