package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.holder.HolderUtility;
import org.broken.arrow.utility.library.UtilityLibrary;

import javax.annotation.Nullable;
import java.util.List;

public abstract class HolderUtilityU<T> extends HolderUtility<T> {


    /**
     * Constructs a menu instance without specifying fill slots and if it you shall cache the button items.
     */
    protected HolderUtilityU() {
        this(null, false);
    }


    /**
     * Create menu instance.
     *
     * @param shallCacheItems Set this to false items and slots should be cached in this class,
     *                        other case override  {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        this in own implementation.
     */
    protected HolderUtilityU(final boolean shallCacheItems) {
        this(null, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param shallCacheItems if it shall cache items and slots in this class.
     */
    protected HolderUtilityU(@Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, shallCacheItems);
    }

}
