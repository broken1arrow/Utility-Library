package org.broken.arrow.menu.library.holder;

import org.broken.arrow.menu.library.MenuUtility;

import java.util.List;


/**
 * Contains all needed methods to create menu.
 */

public class MenuHolder extends HolderUtility<Object> {

    /**
     * Creates a menu instance without any arguments. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     */
    protected MenuHolder() {
        this(null, false);
    }

    /**
     * Creates a menu instance without any arguments. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     *
     * @param fillSlots Witch slots you want fill with items and you need to set the amount of pages.
     */

    protected MenuHolder(final List<Integer> fillSlots) {
        this(fillSlots, false);
    }

    /**
     * Creates a menu instance without any arguments. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     *
     * @param fillSlots       Witch slots you want fill with items and you need to set the amount of pages.
     * @param shallCacheItems Indicates whether items and slots should be cached in this class. If false,
     *                              use {@link #getMenuButtonsCache()} to cache it in your own implementation.
     */
    protected MenuHolder(final List<Integer> fillSlots, boolean shallCacheItems) {
        super(fillSlots, shallCacheItems);
    }


    /**
     * Create menu instance.
     *
     * @param shallCacheItems Indicates whether items and slots should be cached in this class. If false,
     *                             use {@link #getMenuButtonsCache()} to cache it in your own implementation.
     */
    protected MenuHolder(final boolean shallCacheItems) {
        this(null,  shallCacheItems);
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
        this(fillSlots,null,  false);
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
        super(fillSlots,  shallCacheItems);
    }

}
