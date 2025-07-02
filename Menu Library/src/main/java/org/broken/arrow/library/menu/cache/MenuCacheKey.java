package org.broken.arrow.library.menu.cache;

import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This create the unicqe key for the cached menu. This method allow
 * to have several menus on same location.
 */
public final class MenuCacheKey {

	private final Location location;
	private final String key;

	/**
	 * Create new key for the cache.
	 *
	 * @param location the location you want to connect the menu to.
	 * @param key      you can also set unique key, but only needed if you want
	 *                 have several menus on same location.
	 */
	public MenuCacheKey(@Nonnull final Location location, @Nullable final String key) {
		this.location = location;
		this.key = key == null || key.isEmpty() ? null : key;
	}

	/**
	 * Get the location set.
	 *
	 * @return the location.
	 */
	@Nonnull
	public Location getLocation() {
		return location;
	}

	/**
	 * Get the unicqe key is set.
	 *
	 * @return the key or null if not set.
	 */
	@Nullable
	public String getKey() {
		return key;
	}

	/**
	 * Checks if both match or if it shall use only location. Set key to null
	 * if you want to use only location, however if the key is set before in
	 * this instance it will then return false. Likewise, if key is set to non-null value
	 * and the key set in this instance is null then this will also return false.
	 *
	 * @param location location of the menu.
	 * @param key      set this if this menu shall have unique key besides location.
	 * @return true if either location or location and key match.
	 */
	public boolean equals(@Nonnull final Location location, @Nullable final String key) {
		if (getKey() != null) {
			return getLocation().equals(location) && getKey().equals(key);
		}
		if (key != null) {
			return false;
		}
		return getLocation().equals(location);
	}

	/**
	 * Checks if the object equals this instance and if not
	 * it will then check if the values match.
	 *
	 * Recommend you use {@link #equals(org.bukkit.Location, String)} if
	 * you only set a location or the key is not set.
	 *
	 *
	 * @param obj the MenuCacheKey instance to check if it match this instance.
	 * @return This return true if either the instance match or key and location match.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MenuCacheKey)) {
			return false;
		}
		final MenuCacheKey other = (MenuCacheKey) obj;
		return location.equals(other.location)
				&& Objects.equals(key, other.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(location, key);
	}

	@Override
	public String toString() {
		return key != null ? key + ":" + location : location + "";
	}

}
