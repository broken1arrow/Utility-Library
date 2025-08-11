package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.cache.MenuCacheKey;
import org.broken.arrow.library.menu.utility.metadata.MenuMetadataKey;
import org.broken.arrow.library.menu.utility.metadata.MetadataKey;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Utility class for managing player-related metadata within the menu API.
 * <p>
 * This class provides methods to get, set, and remove metadata keys associated with players.
 * It internally uses keys defined by {@link MenuMetadataKey} and stores menu-related data,
 * including caching menus linked to player locations.
 * </p>
 * <p>
 * The metadata values typically include {@link MenuUtility} instances or other relevant objects.
 * </p>
 *
 */
public final class MetadataPlayer {

    private final Plugin plugin;

    /**
     * Creates a new MetadataPlayer utility bound to the specified plugin.
     *
     * @param plugin the plugin instance used for metadata ownership
     */
    public MetadataPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the specified player has metadata set for the given metadata key.
     *
     * @param player the player to check metadata for
     * @param key    the metadata key to look for
     * @return true if the player has metadata with the specified key, false otherwise
     */
    public boolean hasPlayerMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        return player.hasMetadata(this.getMenuMetadataKey(key));
    }

    /**
     * Retrieves the list of metadata values associated with the specified key for the player.
     *
     * @param player the player whose metadata to retrieve
     * @param key    the metadata key to fetch values for
     * @return a list of {@link MetadataValue} objects for the given key, or an empty list if none found
     */
    public List<MetadataValue> getPlayerMenuMetadataList(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        return player.getMetadata(this.getMenuMetadataKey(key));
    }

    /**
     * Retrieves the current menu utility instance stored for the player under the specified key.
     * This typically represents the menu currently open for the player.
     *
     * @param player the player whose menu metadata to retrieve
     * @param key    the metadata key associated with the menu
     * @return the {@link MenuUtility} instance if present, or null if no menu is open or stored
     */
    @Nullable
    public MenuUtility<?> getPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        final List<MetadataValue> playerMetadata = player.getMetadata(this.getMenuMetadataKey(key));
        if (playerMetadata.isEmpty())
            return null;
        Object value = playerMetadata.get(0).value();
        if (!(value instanceof MenuUtility))
            return null;
        return (MenuUtility<?>) value;
    }

    /**
     * Retrieves player metadata associated with the specified key and casts it to the given class if possible.
     *
     * @param <T>   the expected type of the metadata value
     * @param player the player whose metadata to retrieve
     * @param key    the metadata key to look for
     * @param clazz  the class object to cast the metadata value to
     * @return the casted metadata value if present and of the correct type, or null otherwise
     */
    @Nullable
    public <T> T getPlayerMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key,  @Nonnull final  Class<T> clazz) {
        final List<MetadataValue> playerMetadata = player.getMetadata(this.getMenuMetadataKey(key));
        if (playerMetadata.isEmpty())
            return null;
        Object value = playerMetadata.get(0).value();
        if (clazz.isInstance(value))
            return clazz.cast(value);
        else
            return null;
    }

    /**
     * Retrieves raw player metadata value associated with the given key.
     *
     * @param player the player whose metadata to retrieve
     * @param key    the metadata key
     * @return the raw metadata value if present, or null if none found
     */
    @Nullable
    public Object getPlayerMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        final List<MetadataValue> playerMetadata = player.getMetadata(this.getMenuMetadataKey(key));
        if (playerMetadata.isEmpty())
            return null;
        return playerMetadata.get(0).value();
    }

    /**
     * Sets a boolean metadata flag as placeholder, with the given key for the player.
     *
     * @param player the player to set metadata for
     * @param key    the metadata key to set
     */
    public void setPlayerMetaKey(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        this.setMetadata(player, key, true);
    }

    /**
     * Sets a menu utility instance as player metadata under the specified key.
     *
     * @param player the player to associate the menu with
     * @param key    the metadata key
     * @param menu   the menu utility instance to store
     */
    public void setPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final MenuUtility<?> menu) {
        this.setMetadata(player, key, menu);
    }

    /**
     * Sets a location-based menu cache key as metadata for the player.
     *
     * @param player   the player to set metadata for
     * @param key      the metadata key
     * @param location the location cache key representing a menu location
     */
    public void setPlayerLocationMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final MenuCacheKey location) {
        this.setMetadata(player, key, location);

    }

    /**
     * Internal helper method to set metadata on a player with a specific key and value.
     *
     * @param player the player to set metadata on
     * @param key    the metadata key
     * @param object the metadata value to store
     */
    private void setMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final Object object) {
        player.setMetadata(this.getMenuMetadataKey(key), new FixedMetadataValue(plugin, object));
    }

    /**
     * Removes the metadata associated with the given key from the player.
     *
     * @param player the player whose metadata to remove
     * @param key    the metadata key to remove
     */
    public void removePlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        player.removeMetadata(this.getMenuMetadataKey(key), plugin);
    }

    /**
     * Get previous menuHolder instance from player metadata.
     *
     * @param player the player you want to get metadata on.
     * @return older menuHolder instance.
     */
    public MenuUtility<?> getPreviousMenuHolder(final Player player) {
        return getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
    }

    /**
     * Constructs a unique metadata key string based on the base key, optional id, and plugin name.
     *
     * @param key the metadata key object
     * @return a unique string key used internally for player metadata storage
     */
    private String getMenuMetadataKey(@Nonnull MetadataKey key) {
        String metadataKey = key.getBaseKey().name();
        if (key.getId() > 0)
            metadataKey += ":" + key.getId();

        return metadataKey + ":" + plugin.getName();
    }

}