package org.broken.arrow.menu.library.cache;


import org.broken.arrow.menu.library.MenuUtility;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This cache is for when you want to tied the menu to specific location
 * and you also get the benefit several players can use same menu at the
 * same time.
 */
public final class MenuCache {

	private final Map<MenuCacheKey, MenuUtility<?>> menusCached = new HashMap<>();

	/**
	 * Add menu to the cache.
	 *
	 * @param location the location to add.
	 * @param key      the uniqe key if you have several menus on same location. If not set this to null.
	 * @param menu     the menu instance to save.
	 * @return the MenuCacheKey instance created.
	 */
	@Nonnull
	public MenuCacheKey addToCache(@Nonnull final Location location, final String key, @Nonnull final MenuUtility<?> menu) {
		MenuCacheKey menuCacheKey = new MenuCacheKey(location, key);
		this.menusCached.put(menuCacheKey, menu);
		return menuCacheKey;
	}

	/**
	 * The {@link MenuCacheKey} class is used as a key you need
	 * provide right instance of the class.
	 *
	 * @param object the MenuCacheKey object.
	 * @return a cached createMenus.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getMenuInCache(final Object object) {
		return (T) getMenuInCache(object, MenuUtility.class);
	}

	/**
	 * The {@link MenuCacheKey} class is used as a key you need
	 * provide right instance of the class.
	 *
	 * @param object the MenuCacheKey object.
	 * @param clazz  The class for the value you want to retrieve. This class only store
	 *               MenuUtility instances, so no other class will be accepted.
	 * @return a cached createMenus.
	 */
	@Nullable
	public <T> T getMenuInCache(final Object object, Class<T> clazz) {
		if (object instanceof MenuCacheKey && clazz == MenuUtility.class) {
			return clazz.cast(this.menusCached.get(object));
		}
		return null;
	}

	/**
	 * Get {@link MenuCacheKey} class if it exist in the cache.
	 *
	 * @param location the location of the menu.
	 * @param key      unique key of this menu or null if this is only one menu on this location.
	 * @return a cached createMenus.
	 */
	@Nullable
	public MenuCacheKey getMenuCacheKey(@Nonnull Location location, @Nullable String key) {
		for (MenuCacheKey menuCacheKey : this.menusCached.keySet())
			if (menuCacheKey.equals(location, key))
				return menuCacheKey;
		return null;
	}

	/**
	 * The {@link MenuCacheKey} class is used as a key you need
	 * provide right instance of the class.
	 *
	 * @param key the menu key to get the menu.
	 * @return the menu cached or null if it not exist.
	 */
	@Nullable
	public <T> T getMenuInCache(@Nonnull final MenuCacheKey key, Class<T> clazz) {
		MenuUtility<?> cachedMenu = this.menusCached.get(key);
		if (clazz.isInstance(cachedMenu))
			return clazz.cast(cachedMenu);
		return null;
	}

	/**
	 * Remove menu from cache.
	 *
	 * @param object the MenuCacheKey you want to remove.
	 * @return true if it could find the menu.
	 */
	public boolean removeMenuCached(final Object object) {
		if (object instanceof MenuCacheKey)
			return this.menusCached.remove(object) != null;
		return false;
	}

	/**
	 * Remove menu from cache.
	 *
	 * @param key the key you want to remove.
	 * @return true if it could find the menu.
	 */
	public boolean removeMenuCached(@Nonnull final MenuCacheKey key) {
		return this.menusCached.remove(key) != null;
	}

	/**
	 * Remove menu from cache.
	 *
	 * @param location the location of the menu.
	 * @param key      uniqe key of this menu or null if this is not used when create menu.
	 * @return true if it could find the menu.
	 */
	public boolean removeMenuCached(@Nonnull Location location, @Nullable String key) {
		for (MenuCacheKey menuCacheKey : this.menusCached.keySet())
			if (menuCacheKey.equals(location, key))
				return this.menusCached.remove(menuCacheKey) != null;
		return false;
	}

	public Map<Object, MenuUtility<?>> getMenusCached() {
		return Collections.unmodifiableMap(this.menusCached);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof MenuCache)) return false;
		final MenuCache menuCache = (MenuCache) o;

		return menusCached.equals(menuCache.menusCached);
	}

	@Override
	public int hashCode() {
		return menusCached.hashCode();
	}

}