package org.broken.arrow.library.menu.utility;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a duplicate message that takes three arguments and produces a result.
 *
 */
public interface DuplicateMessage {

    /**
     * Applies this message to the given arguments.
     *
     * @param item the itemStack that is duplicate
     * @param amount the amount of items that is duplicated.
     * @param itemAmount the amount of items you get back.
     * @return the function result
     */
    String apply(final ItemStack item, final int amount, final int itemAmount );

}
