package org.broken.arrow.menu.library.utility;

import org.broken.arrow.menu.library.MenuMetadataKey;
import org.broken.arrow.menu.library.MenuUtility;
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
public final class Metadata {

	private final Plugin plugin;

	public Metadata(Plugin plugin) {
		this.plugin = plugin;
	}

	public boolean hasPlayerMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		return player.hasMetadata(key + "_" + plugin);
	}

	public List<MetadataValue> getPlayerMenuMetadataList(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		return player.getMetadata(key + "_" + plugin);
	}

	@Nullable
	public MenuUtility<?> getPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		final List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
		if (playerMetadata.isEmpty())
			return null;
		if (!(playerMetadata.get(0).value() instanceof MenuUtility))
			return null;
		return (MenuUtility<?>) playerMetadata.get(0).value();
	}

	@Nullable
	public Object getPlayerMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		final List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
		if (playerMetadata.isEmpty())
			return null;
		return playerMetadata.get(0).value();
	}

	public void setPlayerMetadata(@Nonnull final Player player, @Nonnull final String key, @Nonnull final Object object) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, object));
	}

	public void setPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key, @Nonnull final MenuUtility<?> menu) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, menu));
	}

	public void setPlayerLocationMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key, @Nonnull final Object location) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, location));
	}

	public void removePlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		player.removeMetadata(key + "_" + plugin, plugin);
	}

	/**
	 * Get menuholder instance from player metadata.
	 *
	 * @return menuholder instance.
	 */
	@Nullable
	public MenuUtility<?> getMenuholder(final Player player) {
		return getMenuholder(player, MenuMetadataKey.MENU_OPEN);
	}

	/**
	 * Get previous menuholder instance from player metadata.
	 *
	 * @return older menuholder instance.
	 */
	public MenuUtility<?> getPreviousMenuholder(final Player player) {
		return getMenuholder(player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
	}

	/**
	 * Get current menu player has stored or currently open menu.
	 *
	 * @param player      the player that open menu.
	 * @param metadataKey the menu key set for this menu.
	 * @return the menu instance or null if player currently no menu open.
	 */
	private MenuUtility<?> getMenuholder(final Player player, final MenuMetadataKey metadataKey) {

		if (hasPlayerMetadata(player, metadataKey)) return getPlayerMenuMetadata(player, metadataKey);
		return null;
	}

}