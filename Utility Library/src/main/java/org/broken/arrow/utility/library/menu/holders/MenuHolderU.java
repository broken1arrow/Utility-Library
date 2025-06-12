package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.holder.HolderUtility;
import org.broken.arrow.menu.library.holder.MenuHolderPage;
import org.broken.arrow.utility.library.UtilityLibrary;

import java.util.List;


/**
 * This class handles single-page menus or paged menus if you implement the logic yourself.
 * <p>&nbsp;</p>
 * If you wish to fill the menu using methods other than slot numbers,
 * such as filling multiple pages, consider using {@link MenuHolderPage} for better alternative.
 * Such as you can interact with your objects directly without making your own logic.
 * <p>&nbsp;</p>
 * Alternatively, you can achieve similar results with this class by setting fill slots using
 * {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually setting the number
 * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
 */
public class MenuHolderU extends HolderUtility<Object> {

    /**
     * Constructs a menu instance with specified menuAPI instance. It is recommended to set the menu size
     * using {@link #setMenuSize(int)}, as the default size is set to zero.
     *
     */
    public MenuHolderU() {
        this( null, false);
    }

    /**
     * Constructs a menu instance with specified fill slots and menuAPI instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero.
     *
     * @param fillSlots The slots you want to fill with items, and you need to set the amount of pages if your plan
     *                  to use mor than one page.
     */
    public MenuHolderU( final List<Integer> fillSlots) {
        this( fillSlots, false);
    }

    /**
     * Constructs a menu instance with specified caching option and menuAPI instance.
     *
     * @param shallCacheItems Set this to false if items and slots should be cached in this class.
     *                        Otherwise, override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        them in your own implementation.                       .
     */
    public MenuHolderU( final boolean shallCacheItems) {
        this( null, shallCacheItems);
    }

    /**
     * Constructs a menu instance with specified fill slots, menuAPI instance and caching option.
     * It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero.
     *
     * @param fillSlots       The slots you want to fill with items, and you need to set the amount of pages if your plan
     *                        to use mor than one page.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class.
     *                        Otherwise, override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        them in your own implementation.
     */
    public MenuHolderU( final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, shallCacheItems);
    }

}
