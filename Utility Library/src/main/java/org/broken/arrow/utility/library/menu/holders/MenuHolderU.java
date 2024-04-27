package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.holder.HolderUtility;
import org.broken.arrow.menu.library.holder.MenuHolderPage;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;


/**
 * This class handles single-page menus or paged menus if you implement the logic yourself.
 * <p></p>
 * If you wish to fill the menu using methods other than slot numbers,
 * such as filling multiple pages, consider using {@link MenuHolderPage} for better alternative.
 * Such as you can interact with your objects directly without making your own logic.
 * <p></p>
 * Alternatively, you can achieve similar results with this class by setting fill slots using
 * {@link #setFillSpace(String)} or {@link #setFillSpace(List)} and manually setting the number
 * of pages using {@link #setManuallyAmountOfPages(int)}; otherwise, only one page will be used.
 */
public class MenuHolderU extends HolderUtility<Object> {

    /**
     * Constructs a menu instance with specified menuAPI instance. It is recommended to set the menu size
     * using {@link #setMenuSize(int)}, as the default size is set to zero.
     *
     * @param menuAPI The instance of RegisterMenuAPI where your registered your plugin.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI) {
        this(menuAPI, null, false);
    }

    /**
     * Constructs a menu instance with specified fill slots and menuAPI instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero.
     *
     * @param menuAPI The instance of RegisterMenuAPI where your registered your plugin.
     * @param fillSlots The slots you want to fill with items, and you need to set the amount of pages if your plan
     *                  to use mor than one page.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI, final List<Integer> fillSlots) {
        this(menuAPI, fillSlots, false);
    }

    /**
     * Constructs a menu instance with specified caching option and menuAPI instance.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where your registered your plugin.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class.
     *                        Otherwise, override {@link #retrieveMenuButtons(int, Map)} to cache
     *                        them in your own implementation.                       .
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI, final boolean shallCacheItems) {
        this(menuAPI, null, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillItems List of items you want parse inside gui on one or several pages.
     * @deprecated this is not in use any more.
     */
    @Deprecated
    public MenuHolderU(final List<?> fillItems) {
        this(null, null, false);
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
     * Constructs a menu instance with specified fill slots, menuAPI instance and caching option.
     * It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where your registered your plugin.
     * @param fillSlots       The slots you want to fill with items, and you need to set the amount of pages if your plan
     *                        to use mor than one page.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class.
     *                        Otherwise, override {@link #retrieveMenuButtons(int, Map)} to cache
     *                        them in your own implementation.
     */
    public MenuHolderU(@Nonnull RegisterMenuAPI menuAPI, final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(menuAPI, fillSlots, shallCacheItems);
    }

}
