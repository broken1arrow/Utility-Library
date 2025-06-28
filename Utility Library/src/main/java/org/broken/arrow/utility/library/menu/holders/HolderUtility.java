package org.broken.arrow.utility.library.menu.holders;

import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.utility.library.UtilityLibrary;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A wrapper for {@link org.broken.arrow.menu.library.holder.HolderUtility}, used in the
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
 * {@link org.broken.arrow.menu.library.holder.HolderUtility} directly. It is not recommended
 * to include this module as a dependency when shading, as it may introduce circular dependencies.
 * </p>
 *
 * @param <T> The type of object stored in the button cache and used in this class.
 * @see org.broken.arrow.menu.library.holder.HolderUtility
 */
public abstract class HolderUtility<T> extends org.broken.arrow.menu.library.holder.HolderUtility<T> {


    /**
     * Constructs a menu instance without specifying fill slots and if it you shall cache the button items.
     */
    protected HolderUtility() {
        this(null, false);
    }


    /**
     * Create menu instance.
     *
     * @param shallCacheItems Set this to false items and slots should be cached in this class,
     *                        other case override  {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        this in own implementation.
     */
    protected HolderUtility(final boolean shallCacheItems) {
        this(null, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param shallCacheItems if it shall cache items and slots in this class.
     */
    protected HolderUtility(@Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        super(UtilityLibrary.getInstance().getMenuApi(), fillSlots, shallCacheItems);
    }

}
