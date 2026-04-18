package org.broken.arrow.library.chunk.tracking;

import org.bukkit.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class ChunkKey {
    private final UUID worldId;
    private final int x;
    private final int z;

    public ChunkKey(@Nonnull final World world, final int x, final int z) {
        this.worldId = world.getUID();
        this.x = x;
        this.z = z;
    }

    public static ChunkKey of(final Chunk chunk) {
        return of(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static ChunkKey of(final ChunkSnapshot snapshot) {
        final World world = Bukkit.getWorld(snapshot.getWorldName());
        //Validate.checkNotNull(world, "World can't be null for the chunk key.");
        return of(world, snapshot.getX(), snapshot.getZ());
    }

    public static ChunkKey of(final Location loc) {
        //Validate.checkNotNull(loc.getWorld(), "World can't be null for the chunk key.");
        return of(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static ChunkKey of(@Nonnull final World world, final int x, final int z) {
        return new ChunkKey(world, x, z);
    }


    @Nullable
    public World getWorld() {
        return Bukkit.getWorld(worldId);
    }

    public int getChunkX() {
        return x;
    }


    public int getChunkZ() {
        return z;
    }

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
