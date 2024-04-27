package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.holder.MenuHolderPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class MenuHolderPageU<T> extends MenuHolderPage<T> {

    /**
     * Constructs a paged menu instance specified list of objects. You need to
     * set {@link #setFillSpace(List)} or {@link #setFillSpace(String)}; otherwise, it will automatically use
     * all slots beside the last 9 slots in your menu.
     *
     * @param menuAPI   The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillItems The list of items to be displayed inside the GUI on one or several pages.
     */
    protected MenuHolderPageU(@Nonnull RegisterMenuAPI menuAPI, final List<T> fillItems) {
        this(menuAPI, null, fillItems, false);
    }

    /**
     * Create paged menu instance.
     *
     * @param menuAPI   The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots The list of items to be displayed inside the GUI.
     * @param fillItems The slots to be filled with items on each page.
     */
    protected MenuHolderPageU(@Nonnull RegisterMenuAPI menuAPI, @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems) {
        this(menuAPI, fillSlots, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified parameters.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots       The slots to be filled with items on each page.
     * @param fillItems       The list of items to be displayed inside the GUI.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class;
     *                        otherwise, override {@link #retrieveMenuButtons(int, Map)} to cache
     *                        them in your own implementation.
     */
    protected MenuHolderPageU(@Nonnull RegisterMenuAPI menuAPI, @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(menuAPI, fillSlots, fillItems, shallCacheItems);
    }

}
