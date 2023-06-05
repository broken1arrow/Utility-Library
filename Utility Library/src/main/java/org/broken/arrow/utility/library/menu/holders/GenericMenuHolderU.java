package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.button.MenuButtonI;

import java.util.List;

public abstract class GenericMenuHolderU<T> extends HolderUtilityU<T> {

	/**
	 * Create menu instance with out any aguments. Recomend you set menu size.
	 */
	public GenericMenuHolderU() {
		this(null, null, false);
	}

	/**
	 * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as defult fill
	 * all slots but not 9 on the bottom.
	 *
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */

	public GenericMenuHolderU(final List<T> fillItems) {
		this(null, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link org.broken.arrow.menu.library.MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	public GenericMenuHolderU(final boolean shallCacheItems) {
		this(null, null, shallCacheItems);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots Witch slots you want fill with items.
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */
	public GenericMenuHolderU(final List<Integer> fillSlots, final List<T> fillItems) {
		this(fillSlots, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots       Witch slots you want fill with items.
	 * @param fillItems       List of items you want parse inside gui.
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link org.broken.arrow.menu.library.MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	public GenericMenuHolderU(final List<Integer> fillSlots, final List<T> fillItems, final boolean shallCacheItems) {
		super(fillSlots, fillItems, shallCacheItems);
	}

	public MenuButtonI<T> menuButton(MenuButtonI<T> menuButton) {
		return menuButton;
	}
}
