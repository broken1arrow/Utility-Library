package org.broken.arrow.library.menu.holder.utility;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.menu.utility.metadata.MenuMetadataKey;
import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.broken.arrow.library.menu.cache.MenuCache;
import org.broken.arrow.library.menu.cache.MenuCacheKey;
import org.broken.arrow.library.menu.utility.MetadataPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;

/**
 * Handles loading and caching of player inventories associated with menus.
 *
 * <p>This class interacts with {@link MenuUtility} to load inventories,
 * optionally cache them by location, and manage player metadata related to open menus.</p>
 *
 * @param <T> The type parameter used by the associated {@link MenuUtility}.
 */
public class LoadInventoryHandler<T> {

    private final MenuUtility<T> menuUtility;
    private final MetadataPlayer metadataPlayer;
    private final MenuCache menuCache;
    private Location location;
    private String uniqueKey;
    private MenuCacheKey menuCacheKey;

    /**
     * Creates a new LoadInventoryHandler with the given MenuUtility and RegisterMenuAPI.
     *
     * @param menuUtility The MenuUtility instance representing the menu to load.
     * @param menuAPI The API instance providing player metadata and menu cache.
     */
    public LoadInventoryHandler(@Nonnull final MenuUtility<T> menuUtility,@Nonnull final RegisterMenuAPI menuAPI) {
        this.menuUtility = menuUtility;
        this.metadataPlayer = menuAPI.getPlayerMeta();
        this.menuCache = menuAPI.getMenuCache();
    }

    /**
     * Loads the inventory for a player.
     *
     * <p>If {@code loadToCache} is true and a location is set, the menu will be stored and loaded from the cache.
     * Otherwise, it will attempt to load the inventory from the player's metadata.</p>
     *
     * @param player The player whose inventory should be loaded.
     * @param loadToCache Whether to load and save the menu in the cache.
     * @return The loaded {@link Inventory}, or null if none available.
     */
    public Inventory loadInventory(final Player player, final boolean loadToCache) {
        Inventory menu = null;
        if (loadToCache && this.location != null) {
            this.storeMenuAtPlayer(player, this.location);
            MenuUtility<T> menuCached = this.getMenuCache();

            if (menuCached == null || menuCached.getMenu() == null) {
                saveMenuCache(this.location);
                menuCached = this.getMenuCache();
            }
            if (!this.menuUtility.isIgnoreValidCheck()) {
                Validate.checkBoolean(!menuCached.getClass().equals(this.menuUtility.getClass()) && (this.uniqueKey == null || this.uniqueKey.isEmpty()), "You need set uniqueKey for this menu " + menuCached.getClass() + " or it will replace the old menu and players left can take items, set method setIgnoreValidCheck() to ignore this or set the uniqueKey");
            } else {
                saveMenuCache(this.location);
                menuCached = this.getMenuCache();
            }
            menu = menuCached.getMenu();
        } else {
            MetadataPlayer playerMeta = this.metadataPlayer;
            playerMeta.setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, menuUtility);
            final MenuUtility<?> menuCachedAtPlayer = playerMeta.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
            if (menuCachedAtPlayer != null) menu = menuCachedAtPlayer.getMenu();
        }
        return menu;
    }

    /**
     * Gets the location associated with this inventory handler.
     *
     * @return the current {@link Location}, or null if not set.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location associated with this inventory handler.
     *
     * @param location the {@link Location} to set.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Gets the cache key used to identify the cached menu.
     *
     * @return the {@link MenuCacheKey} representing the current cache key, or null if none.
     */
    public MenuCacheKey getMenuCacheKey() {
        return menuCacheKey;
    }
    /**
     * Sets a unique key for this inventory handler to distinguish it in the cache.
     *
     * @param uniqueKey the unique string key to set.
     */
    public void setUniqueKey(final String uniqueKey) {
            this.uniqueKey = uniqueKey;
    }

    /**
     * Gets the unique key for this inventory handler.
     *
     * @return the unique key string, or null if not set.
     */
    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * Removes the cached menu associated with the current cache key.
     */
    public void removeMenuCache() {
        menuCache.removeMenuCached(this.menuCacheKey);
    }

    /**
     * Saves the current menu to the cache at the specified location.
     *
     * @param location The location to associate with the cached menu.
     */
    public void saveMenuCache(@Nonnull final Location location) {
        menuCache.addToCache(location, this.uniqueKey, this.menuUtility);
    }

    /**
     * Retrieves the cached menu associated with the current cache key.
     *
     * @return The cached {@link MenuUtility} instance, or null if none found.
     */
    public MenuUtility<T> getMenuCache() {
        return menuCache.getMenuInCache(this.menuCacheKey, MenuUtility.class);
    }

    /**
     * Stores the current menu cache key in the player's metadata based on location and unique key.
     *
     * @param player The player to store metadata for.
     * @param location The location associated with the menu.
     */
    public void storeMenuAtPlayer(final Player player, final Location location) {
        String key = this.uniqueKey;
        if (key != null && key.isEmpty()) {
            this.uniqueKey = this.getClass().getName();
            key = this.uniqueKey;
        }
        menuCacheKey = this.menuCache.getMenuCacheKey(location, key);
        if (menuCacheKey == null) menuCacheKey = new MenuCacheKey(location, key);
        this.metadataPlayer.setPlayerLocationMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, menuCacheKey);
    }

}
