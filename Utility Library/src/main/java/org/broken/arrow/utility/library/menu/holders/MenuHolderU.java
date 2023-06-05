package org.broken.arrow.utility.library.menu.holders;

import java.util.List;


/**
 * Contains all needed methods to create menu.
 */

public class MenuHolderU extends HolderUtilityU<Object> {

	/**
	 * Create menu instance with out any aguments. Recomend you set menu size.
	 */
	public MenuHolderU() {
		this(null, null, false);
	}

	/**
	 * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as defult fill
	 * all slots but not 9 on the bottom.
	 *
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */

	public MenuHolderU(final List<?> fillItems) {
		this(null, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link org.broken.arrow.menu.library.MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	public MenuHolderU(final boolean shallCacheItems) {
		this(null, null, shallCacheItems);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots Witch slots you want fill with items.
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */
	public MenuHolderU(final List<Integer> fillSlots, final List<?> fillItems) {
		this(fillSlots, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots       Witch slots you want fill with items.
	 * @param fillItems       List of items you want parse inside gui.
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link org.broken.arrow.menu.library.MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	public MenuHolderU(final List<Integer> fillSlots, final List<?> fillItems, final boolean shallCacheItems) {
		super(fillSlots, (List<Object>) fillItems, shallCacheItems);
	}

}
