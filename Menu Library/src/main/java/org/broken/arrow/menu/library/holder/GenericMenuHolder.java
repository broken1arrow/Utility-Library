package org.broken.arrow.menu.library.holder;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.button.MenuButton;

import java.util.List;

/**
 * This class will be replaced by  {@link MenuHolderPage}.
 * @param <T> the class type of the objects added to the list.
 */
@Deprecated
public abstract class GenericMenuHolder<T> extends HolderUtility<T> {

	/**
	 * Create menu instance with out any aguments. Recomend you set menu size.
	 */
	protected GenericMenuHolder() {
		this(null, null, false);
	}

	/**
	 * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as defult fill
	 * all slots but not 9 on the bottom.
	 *
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */

	protected GenericMenuHolder(final List<T> fillItems) {
		this(null, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	protected GenericMenuHolder(final boolean shallCacheItems) {
		this(null, null, shallCacheItems);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots Witch slots you want fill with items.
	 * @param fillItems List of items you want parse inside gui on one or several pages.
	 */
	protected GenericMenuHolder(final List<Integer> fillSlots, final List<T> fillItems) {
		this(fillSlots, fillItems, false);
	}

	/**
	 * Create menu instance.
	 *
	 * @param fillSlots       Witch slots you want fill with items.
	 * @param fillItems       List of items you want parse inside gui.
	 * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link MenuUtility#getMenuButtonsCache()} to cache it own class.
	 */
	protected GenericMenuHolder(final List<Integer> fillSlots, final List<T> fillItems, final boolean shallCacheItems) {
		super(fillSlots, fillItems, shallCacheItems);
	}

	public MenuButton menuButton(MenuButton menuButton) {
		return menuButton;
	}
}
