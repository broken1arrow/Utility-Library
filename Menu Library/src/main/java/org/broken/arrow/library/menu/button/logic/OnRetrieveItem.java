package org.broken.arrow.library.menu.button.logic;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Functional interface representing a method to retrieve or generate an item for a specific slot,
 * optionally based on an associated fill object.
 *
 * @param <T> the type of the optional fill object associated with the slot, may be {@code null}.
 */
@FunctionalInterface
public interface OnRetrieveItem<T> {

    /**
     * Retrieves or generates an item for the given slot, optionally using the fill object.
     *
     * @param slot       the slot or position for which to retrieve the item.
     * @param fillObject an optional fill object to influence item retrieval, may be {@code null}.
     * @return the item for the specified slot.
     */
    ItemStack apply(int slot, @Nullable T fillObject);

}
