package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.holder.MenuHolderPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class MenuHolderPageU<T> extends MenuHolderPage<T> {

    /**
     * Create paged menu instance.
     *
     * @param menuAPI   The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots Witch slots you want fill with items.
     * @param fillItems List of items you want parse inside gui.
     */
    protected MenuHolderPageU(@Nonnull RegisterMenuAPI menuAPI, @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems) {
        this(menuAPI, fillSlots, fillItems, false);
    }

    /**
     * Create paged menu instance.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots       Witch slots you want fill with items.
     * @param fillItems       List of items you want parse inside gui.
     * @param shallCacheItems if it shall cache items and slots in this class, other case use {@link #getMenuButtonsCache()} to cache it own class.
     */
    protected MenuHolderPageU(@Nonnull RegisterMenuAPI menuAPI, @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(menuAPI, fillSlots, fillItems, shallCacheItems);
    }

}
