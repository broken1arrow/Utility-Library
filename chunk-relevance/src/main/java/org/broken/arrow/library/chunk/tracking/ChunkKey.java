package org.broken.arrow.library.chunk.tracking;

import org.broken.arrow.library.logging.Validate;
import org.bukkit.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an immutable identifier for a Minecraft chunk.
 *
 * <p>
 * A {@code ChunkKey} stores the chunk X/Z coordinates together with the
 * unique identifier of the associated world. It is intended to be used
 * as a stable key in caches, maps, and other lookup structures.
 *
 * <p>
 * The world is resolved lazily via {@link #getWorld()}, which may return
 * {@code null} if the world is not currently loaded or cannot be found.
 */
public class ChunkKey {
    private final UUID worldId;
    private final int x;
    private final int z;

    /**
     * Creates a new chunk key.
     *
     * @param worldId the unique identifier of the world this chunk belongs to
     * @param x the chunk X coordinate
     * @param z the chunk Z coordinate
     */
    public ChunkKey(@Nonnull final UUID worldId, final int x, final int z) {
        this.worldId = worldId;
        this.x = x;
        this.z = z;
    }

    /**
     * Creates a chunk key from a {@link Chunk}.
     *
     * @param chunk the chunk
     * @return a new chunk key representing the given chunk
     */
    public static ChunkKey of(final Chunk chunk) {
        return of(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * Creates a chunk key from a {@link ChunkSnapshot}.
     *
     * <p>
     * The world is resolved using the snapshot's world name.
     *
     * @param snapshot the chunk snapshot
     * @return a new chunk key representing the given snapshot
     * @throws IllegalArgumentException if the world cannot be resolved
     */
    public static ChunkKey of(final ChunkSnapshot snapshot) {
        final World world = Bukkit.getWorld(snapshot.getWorldName());
        Validate.checkNotNull(world, "World can't be null for the chunk key.");
        return of(world, snapshot.getX(), snapshot.getZ());
    }

    /**
     * Creates a chunk key from a {@link Location}.
     *
     * <p>
     * The chunk coordinates are derived from the block coordinates.
     *
     * @param loc the location
     * @return a new chunk key representing the chunk containing the location
     * @throws IllegalArgumentException if the location's world is {@code null}
     */
    public static ChunkKey of(final Location loc) {
        Validate.checkNotNull(loc.getWorld(), "World can't be null for the chunk key.");
        return of(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    /**
     * Creates a chunk key from a {@link World} and chunk coordinates.
     *
     * @param world the world
     * @param x the chunk X coordinate
     * @param z the chunk Z coordinate
     * @return a new chunk key
     */
    public static ChunkKey of(@Nonnull final World world, final int x, final int z) {
        return new ChunkKey(world.getUID(), x, z);
    }

    /**
     * Creates a chunk key from a world UUID and chunk coordinates.
     *
     * @param worldId the world UUID
     * @param x the chunk X coordinate
     * @param z the chunk Z coordinate
     * @return a new chunk key
     */
    public static ChunkKey of(@Nonnull final UUID worldId, final int x, final int z) {
        return new ChunkKey(worldId, x, z);
    }

    /**
     * Returns the world associated with this chunk key.
     *
     * <p>
     * The world may be {@code null} if it is not currently loaded
     * or cannot be resolved.
     *
     * @return the world, or {@code null} if unavailable
     */
    @Nullable
    public World getWorld() {
        return Bukkit.getWorld(worldId);
    }

    /**
     * Returns the chunk X coordinate.
     *
     * @return the chunk X coordinate
     */
    public int getChunkX() {
        return x;
    }

    /**
     * Returns the chunk Z coordinate.
     *
     * @return the chunk Z coordinate
     */
    public int getChunkZ() {
        return z;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final ChunkKey key = (ChunkKey) o;
        return x == key.x && z == key.z && Objects.equals(worldId, key.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, z);
    }

    @Override
    public String toString() {
        return "worldId=" + worldId + " x=" + x + " z=" + z;
    }
}
