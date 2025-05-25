package org.broken.arrow.menu.library.holder.utility;

import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.menu.library.MenuMetadataKey;
import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.broken.arrow.menu.library.cache.MenuCacheKey;
import org.broken.arrow.menu.library.utility.MetadataPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;

public class LoadInventoryHandler<T> {

    private final MenuUtility<T> menuUtility;
    private final MetadataPlayer metadataPlayer;
    private final MenuCache menuCache;
    private Location location;
    private String uniqueKey;
    private MenuCacheKey menuCacheKey;

    public LoadInventoryHandler(MenuUtility<T> menuUtility, RegisterMenuAPI menuAPI) {
        this.menuUtility = menuUtility;
        this.metadataPlayer = menuAPI.getPlayerMeta();
        this.menuCache = menuAPI.getMenuCache();
    }

    public Inventory loadInventory(final Player player, final boolean loadToCache) {
        Inventory menu = null;
        if (loadToCache && this.location != null) {
            this.setLocationMetaOnPlayer(player, this.location);
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
            playerMeta.setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, menuUtility);
            playerMeta.setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, menuUtility);
            final MenuUtility<?> menuUtility = playerMeta.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
            if (menuUtility != null) menu = menuUtility.getMenu();
        }
        return menu;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public MenuCacheKey getMenuCacheKey() {
        return menuCacheKey;
    }

    public void setUniqueKey(final String uniqueKey) {
            this.uniqueKey = uniqueKey;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * Remove the cached menu.
     */
    public void removeMenuCache() {
        menuCache.removeMenuCached(this.menuCacheKey);
    }

    public void saveMenuCache(@Nonnull final Location location) {
        menuCache.addToCache(location, this.uniqueKey, this.menuUtility);
    }

    public MenuUtility<T> getMenuCache() {
        return menuCache.getMenuInCache(this.menuCacheKey, MenuUtility.class);
    }

    public void setLocationMetaOnPlayer(final Player player, final Location location) {
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
