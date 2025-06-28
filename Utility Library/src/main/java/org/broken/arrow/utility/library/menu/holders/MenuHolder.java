package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.holder.HolderUtility;
import org.broken.arrow.utility.library.UtilityLibrary;

import java.util.List;

/**
 * A wrapper for {@link org.broken.arrow.menu.library.holder.MenuHolder}, used in the
 * shaded version of this plugin/module.
 *
 * <p>This class exists to unify the API for:</p>
 * <ul>
 *   <li>The standalone shaded plugin version (all-in-one JAR)</li>
 * </ul>
 *
 * <p>The main purpose of this wrapper is to simplify usage:</p>
 * <ul>
 *   <li>No need for users to manually provide the plugin instance</li>
 *   <li>Keeps method signatures consistent regardless of setup</li>
 * </ul>
 *
 * <p><b>Note:</b> If you are shading the library manually, you should use
 * {@link org.broken.arrow.menu.library.holder.MenuHolder} directly. It is not recommended
 * to include this module as a dependency when shading, as it may introduce circular dependencies.
 * </p>
 *
 * @see org.broken.arrow.menu.library.holder.MenuHolder
 */
public class MenuHolder extends HolderUtility<Object> {

    /**
     * Constructs a menu instance with specified menuAPI instance. It is recommended to set the menu size
     * using {@link #setMenuSize(int)}, as the default size is set to zero.
     *
     */
    public MenuHolder() {
        this( null, false);
    }

    /**
     * Constructs a menu instance with specified fill slots and menuAPI instance. It is recommended to set the menu size using {@link #setMenuSize(int)},
     * as the default size is set to zero.
     *
     * @param fillSlots The slots you want to fill with items, and you need to set the amount of pages if your plan
     *                  to use mor than one page.
     */
    public MenuHolder(final List<Integer> fillSlots) {
        this( fillSlots, false);
    }

    /**
     * Constructs a menu instance with specified caching option and menuAPI instance.
     *
     * @param shallCacheItems Set this to false if items and slots should be cached in this class.
     *                        Otherwise, override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        them in your own implementation.                       .
     */
    public MenuHolder(final boolean shallCacheItems) {
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
    public MenuHolder(final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, shallCacheItems);
    }

}
