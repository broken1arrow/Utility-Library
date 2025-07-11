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
 * Class to get or set metadata and this api use this keys internally {@link MenuMetadataKey } and methods below do you
 * get values or a menu from a player (if you use location in the menu api it will store the menu in cache).
 */
public final class MetadataPlayer {

    private final Plugin plugin;

    public MetadataPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasPlayerMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        return player.hasMetadata(this.getMenuMetadataKey(key));
    }

    public List<MetadataValue> getPlayerMenuMetadataList(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        return player.getMetadata(this.getMenuMetadataKey(key));
    }

    /**
     * Get current menu player has stored or currently open menu.
     *
     * @param player      the player that open menu.
     * @param key the menu key set for this menu.
     * @return the menu instance or null if player currently no menu open.
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

    @Nullable
    public Object getPlayerMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        final List<MetadataValue> playerMetadata = player.getMetadata(this.getMenuMetadataKey(key));
        if (playerMetadata.isEmpty())
            return null;
        return playerMetadata.get(0).value();
    }

    public void setPlayerMetaKey(@Nonnull final Player player, @Nonnull final MetadataKey key) {
        this.setMetadata(player, key, true);
    }

    public void setPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final MenuUtility<?> menu) {
        this.setMetadata(player, key, menu);
    }

    public void setPlayerLocationMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final MenuCacheKey location) {
        this.setMetadata(player, key, location);

    }

    private void setMetadata(@Nonnull final Player player, @Nonnull final MetadataKey key, @Nonnull final Object object) {
        player.setMetadata(this.getMenuMetadataKey(key), new FixedMetadataValue(plugin, object));
    }

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

    private String getMenuMetadataKey(@Nonnull MetadataKey key) {
        String metadataKey = key.getBaseKey().name();
        if (key.getId() > 0)
            metadataKey += ":" + key.getId();

        return metadataKey + ":" + plugin.getName();
    }

}