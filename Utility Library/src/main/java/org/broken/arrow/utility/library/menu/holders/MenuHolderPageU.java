package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.holder.MenuHolderPage;
import org.broken.arrow.utility.library.UtilityLibrary;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MenuHolderPageU<T> extends MenuHolderPage<T> {

    /**
     * Constructs a paged menu instance specified list of objects. You need to
     * set {@link #setFillSpace(List)} or {@link #setFillSpace(String)}; otherwise, it will automatically use
     * all slots beside the last 9 slots in your menu.
     *
     * @param fillItems The list of items to be displayed inside the GUI on one or several pages.
     */
    protected MenuHolderPageU( final List<T> fillItems) {
        this( null, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified fill slots and list of objects.
     *
     * @param fillSlots The list of items to be displayed inside the GUI.
     * @param fillItems The slots to be filled with items on each page.
     */
    protected MenuHolderPageU( @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems) {
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
    protected MenuHolderPageU( @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, fillItems, shallCacheItems);
    }

}
