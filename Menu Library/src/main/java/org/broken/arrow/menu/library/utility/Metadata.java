package org.broken.arrow.menu.library.utility;

import org.broken.arrow.menu.library.MenuMetadataKey;
import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.RegisterMenuAPI;
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

	private static final Plugin plugin = RegisterMenuAPI.getPLUGIN();


	public static boolean hasPlayerMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		return player.hasMetadata(key + "_" + plugin);
	}

	public static List<MetadataValue> getPlayerMenuMetadataList(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		return player.getMetadata(key + "_" + plugin);
	}

	@Nullable
	public static MenuUtility<?> getPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		final List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
		if (playerMetadata.isEmpty())
			return null;
		return (MenuUtility<?>) playerMetadata.get(0).value();
	}

	@Nullable
	public static Object getPlayerMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		final List<MetadataValue> playerMetadata = player.getMetadata(key + "_" + plugin);
		if (playerMetadata.isEmpty())
			return null;
		return playerMetadata.get(0).value();
	}

	public static void setPlayerMetadata(@Nonnull final Player player, @Nonnull final String key, @Nonnull final Object object) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, object));
	}

	public static void setPlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key, @Nonnull final MenuUtility menu) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, menu));
	}

	public static void setPlayerLocationMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key, @Nonnull final Object location) {
		player.setMetadata(key + "_" + plugin, new FixedMetadataValue(plugin, location));
	}

	public static void removePlayerMenuMetadata(@Nonnull final Player player, @Nonnull final MenuMetadataKey key) {
		player.removeMetadata(key + "_" + plugin, plugin);
	}


}