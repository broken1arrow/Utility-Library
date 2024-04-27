package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.holder.HolderUtility;
import org.broken.arrow.menu.library.holder.MenuHolderPage;

import javax.annotation.Nonnull;
import java.util.List;


/**
 * Contains all needed methods to create menu.
 */

public class MenuHolderU extends HolderUtility<Object> {

    /**
     * Creates a menu instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     *
     * @param menuAPI The instance of RegisterMenuAPI where you have registered your plugin.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI) {
        this(menuAPI, null, false);
    }

    /**
     * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as defult fill
     * all slots but not 9 on the bottom.
     *
     * @param fillItems List of items you want parse inside gui on one or several pages.
     * @deprecated this is not in use any more.
     */
    @Deprecated
    public MenuHolderU(final List<?> fillItems) {
        this(null, null, false);
    }

    /**
     * Creates a menu instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     *
     * @param menuAPI The instance of RegisterMenuAPI where you have registered your plugin.
     * @param shallCacheItems Indicates whether items and slots should be cached in this class. If false,
     *                        use {@link #getMenuButtonsCache()} to cache it in your own implementation.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI, final boolean shallCacheItems) {
        this(menuAPI, null, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots Witch slots you want fill with items.
     * @param fillItems List of items you want parse inside gui on one or several pages.
     * @deprecated this is not in use any more.
     */
    @Deprecated
    public MenuHolderU(final List<Integer> fillSlots, final List<?> fillItems) {
        this(null, fillSlots, false);
    }

    /**
     * Creates a menu instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero. If you wish to fill the menu using methods other than slot numbers,
     * such as filling multiple pages, consider using {@link MenuHolderPage} for better options. Alternatively, you can achieve similar results
     * with this class by setting fill slots using {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually set the number
     * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param shallCacheItems set to true if you want to cache items and slots, use this method {@link org.broken.arrow.menu.library.MenuUtility#getMenuButtonsCache()} to cache it own class.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI, final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(menuAPI,fillSlots, shallCacheItems);
    }

}
