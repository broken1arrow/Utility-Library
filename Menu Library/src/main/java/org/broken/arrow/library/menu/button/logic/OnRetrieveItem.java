package org.broken.arrow.library.menu.button.logic;

import javax.annotation.Nullable;

/**
 * Functional interface representing a method to retrieve or generate an item for a specific slot,
 * optionally based on an associated fill object.
 *
 * @param <I> the type of the item to be returned.
 * @param <S> the type representing the slot or position.
 * @param <T> the type of the optional fill object associated with the slot, may be {@code null}.
 */
@FunctionalInterface
public interface OnRetrieveItem<I, S, T> {

    /**
     * Retrieves or generates an item for the given slot, optionally using the fill object.
     *
     * @param slot       the slot or position for which to retrieve the item.
     * @param fillObject an optional fill object to influence item retrieval, may be {@code null}.
     * @return the item for the specified slot.
     */
    I apply(S slot,@Nullable T fillObject);

}
