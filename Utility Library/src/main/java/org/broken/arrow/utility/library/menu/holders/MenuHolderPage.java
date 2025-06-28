package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.utility.library.UtilityLibrary;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A wrapper for {@link org.broken.arrow.menu.library.holder.MenuHolderPage}, used in the
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
 * {@link org.broken.arrow.menu.library.holder.MenuHolderPage} directly. It is not recommended
 * to include this module as a dependency when shading, as it may introduce circular dependencies.
 * </p>
 *
 * @param <T> The type of object stored in the button cache and used in this class.
 * @see org.broken.arrow.menu.library.holder.MenuHolderPage
 */
public abstract class MenuHolderPage<T> extends org.broken.arrow.menu.library.holder.MenuHolderPage<T> {

    /**
     * Constructs a paged menu instance specified list of objects. You need to
     * set {@link #setFillSpace(List)} or {@link #setFillSpace(String)}; otherwise, it will automatically use
     * all slots beside the last 9 slots in your menu.
     *
     * @param fillItems The list of items to be displayed inside the GUI on one or several pages.
     */
    protected MenuHolderPage(final List<T> fillItems) {
        this( null, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified fill slots and list of objects.
     *
     * @param fillSlots The list of items to be displayed inside the GUI.
     * @param fillItems The slots to be filled with items on each page.
     */
    protected MenuHolderPage(@Nullable List<Integer> fillSlots, @Nullable List<T> fillItems) {
        this(fillSlots, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified parameters.
     *
     * @param fillSlots       The slots to be filled with items on each page.
     * @param fillItems       The list of items to be displayed inside the GUI.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class;
     *                        otherwise, override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        them in your own implementation.
     */
    protected MenuHolderPage(@Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, fillItems, shallCacheItems);
    }

}
