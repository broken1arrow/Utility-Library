package org.broken.arrow.library.serialize.utility.converters.world;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a stable, serializable reference to a Bukkit {@link Location}.
 *
 * <p>
 * Implementations act as a safe indirection layer over {@link Location}.
 * Holding direct references to a {@link Location} can prevent unloaded worlds
 * from being garbage-collected. This resolver avoids that issue by storing
 * the world's {@link UUID} and block coordinates instead.
 *
 * <p>
 * The resolved location may be {@code null} if the underlying world
 * is not currently loaded or available.
 */
public final class BlockKeyResolver implements ConfigurationSerializable {
    private static Logging LOG = new Logging(BlockKeyResolver.class);
    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;
    private volatile Location cachedLocation;

    private BlockKeyResolver(@Nonnull final UUID worldId, final int x, final int y, final int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a block key from an existing {@link Location}.
     *
     * @param loc The location to wrap.
     * @return A new {@code BlockKeyResolver} instance that can be safely stored in a database via {@link #serialize()}.
     * @throws IllegalArgumentException if the location's world is null.
     */
    public static BlockKeyResolver of(@Nonnull final Location loc) {
        Validate.checkNotNull(loc.getWorld(), "World cannot be null");
        return new BlockKeyResolver(
                loc.getWorld().getUID(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    /**
     * Creates a block key from a world UUID and block coordinates.
     *
     * @param worldId The UUID of the world.
     * @param x       The block's X coordinate.
     * @param y       The block's Y coordinate.
     * @param z       The block's Z coordinate.
     * @return A new {@code BlockKeyResolver} instance that can be safely stored in a database via {@link #serialize()}.
     */
    public static BlockKeyResolver of(@Nonnull final UUID worldId, final int x, final int y, final int z) {
        return new BlockKeyResolver(worldId, x, y, z);
    }

    /**
     * Resolves the current {@link Location} of this block.
     *
     * @return The resolved location, or {@code null} if the world is currently unavailable or not loaded.
     */
    @Nullable
    public Location getLocation() {
        Location loc = cachedLocation;
        if (loc != null) return loc;

        World world = Bukkit.getWorld(worldId);
        if (world == null) return null;

        loc = new Location(world, x, y, z);
        cachedLocation = loc;
        return loc;
    }

    /**
     * Gets the block's X coordinate.
     *
     * @return The X coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the block's Y coordinate.
     *
     * @return The Y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the block's Z coordinate.
     *
     * @return The Z coordinate.
     */
    public int getZ() {
        return z;
    }


    /**
     * Gets the unique identifier of the world this block belongs to.
     *
     * @return The world's UUID.
     */
    @Nonnull
    public UUID getWorldId() {
        return worldId;
    }

    /**
     * Indicates whether this resolver has a valid block reference set.
     *
     * @return {@code true} if a block reference is present, otherwise {@code false}.
     */
    public boolean isSet() {
        return cachedLocation != null;
    }

    /**
     * Serializes this block reference into a primitive map representation.
     *
     * <p>
     * The returned map contains the world identifier and block coordinates,
     * making it safe to use for configuration storage or database persistence.
     *
     * @return A serialized map representation of this block key.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> primaryData = new HashMap<>();
        primaryData.put("world_id", getWorldId() != null ? getWorldId().toString() : null);
        primaryData.put("loc_x", x);
        primaryData.put("loc_y", y);
        primaryData.put("loc_z", z);
        return primaryData;
    }

    /**
     * Deserializes a map representation back into a {@code BlockKeyResolver}.
     *
     * <p>
     * This method expects the map to contain the keys {@code "world_id"},
     * {@code "loc_x"}, {@code "loc_y"}, and {@code "loc_z"}. This matches the
     * data structure generated by the {@link #serialize()} method.
     *
     * @param primaryData The serialized data map containing the world identifier and block coordinates.
     * @return A newly constructed {@code BlockKeyResolver} instance based on the provided data.
     * @throws Validate.ValidateExceptions If the {@code world_id} is missing or is not a valid UUID format,
     *                                     or if any of the coordinates cannot be parsed as valid integers.
     */
    public static BlockKeyResolver deserialize(Map<String, Object> primaryData) {
        UUID world;
        try {
            final Object worldId = primaryData.get("world_id");
            world = UUID.fromString(worldId != null ? worldId.toString() : "");
        } catch (IllegalArgumentException exception) {
            throw new Validate.ValidateExceptions(exception, "Cannot create BlockKeyResolver, invalid or missing world UUID.");
        }
        int x;
        int y;
        int z;
        try {
            x = Integer.parseInt(primaryData.get("loc_x") + "");
            y = Integer.parseInt(primaryData.get("loc_y") + "");
            z = Integer.parseInt(primaryData.get("loc_z") + "");
        } catch (NumberFormatException exception) {
            throw new Validate.ValidateExceptions(exception, "Cannot create BlockKeyResolver, one or more coordinates are missing or invalid.");
        }
        return BlockKeyResolver.of(world, x, y, z);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final BlockKeyResolver blockKey = (BlockKeyResolver) o;
        return x == blockKey.x && y == blockKey.y && z == blockKey.z && Objects.equals(worldId, blockKey.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z);
    }

    @Override
    public String toString() {
        return "worldId=" + worldId +
                " x=" + x +
                " y=" + y +
                " z=" + z +
                " cachedLocation=" + cachedLocation;
    }
}