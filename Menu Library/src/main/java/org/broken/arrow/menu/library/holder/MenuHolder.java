package org.broken.arrow.menu.library.holder;

import org.broken.arrow.menu.library.MenuUtility;

import java.util.List;


/**
 * Contains all needed methods to create menu.
 */

public class MenuHolder extends HolderUtility<Object> {

    /**
     * Create menu instance with out any arguments. Recommend you set menu size.
     */
    protected MenuHolder() {
        this(null, null, false);
    }

    /**
     * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as defult fill
     * all slots but not 9 on the bottom.
     *
     * @param fillItems List of items you want parse inside gui on one or several pages.
     * @deprecated the list of fillSlots and fillItems will be removed, use {@link MenuHolderPage} for set up paged menus.
     */
    @Deprecated
    protected MenuHolder(final List<?> fillItems) {
        this(null, fillItems, false);
    }

    /**
     * Create menu instance.
     *
     * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link MenuUtility#getMenuButtonsCache()} to cache it own class.
     */
    protected MenuHolder(final boolean shallCacheItems) {
        this(null, null, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots Witch slots you want fill with items.
     * @param fillItems List of items you want parse inside gui on one or several pages.
     * @deprecated the list of fillSlots and fillItems will be removed, use {@link MenuHolderPage} for set up paged menus.
     */
    @Deprecated
    protected MenuHolder(final List<Integer> fillSlots, final List<?> fillItems) {
        this(fillSlots, fillItems, false);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param fillItems       List of items you want parse inside gui.
     * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link MenuUtility#getMenuButtonsCache()} to cache it own class.
     * @deprecated the list of fillSlots and fillItems will be removed, use {@link MenuHolderPage} for set up paged menus.
     */
    @Deprecated
    protected MenuHolder(final List<Integer> fillSlots, final List<?> fillItems, final boolean shallCacheItems) {
        super(fillSlots, (List<Object>) fillItems, shallCacheItems);
    }

}
