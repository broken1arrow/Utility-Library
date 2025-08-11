package org.broken.arrow.library.menu.button.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Functional interface representing a handler for click events on a button.
 *
 * @param <T> the type of the result returned by the click handler, typically an action or status.
 * @param <P> the type representing the player who clicked.
 * @param <M> the type representing the menu or inventory where the click occurred.
 * @param <C> the type representing the click type (e.g., left-click, right-click).
 * @param <I> the type representing the item that was clicked.
 * @param <F> the type of the optional fill object associated with the button, may be {@code null}.
 */
@FunctionalInterface
public interface OnClick<T, P, M, C, I, F> {

    /**
     * Handles a click event on a button.
     *
     * @param player     the player who clicked the button, never {@code null}.
     * @param menu       the menu or inventory where the click occurred, never {@code null}.
     * @param click      the type of the click event, never {@code null}.
     * @param clickedItem the item that was clicked, never {@code null}.
     * @param fillObject an optional associated object for this button, may be {@code null}.
     * @return the result of handling the click, never {@code null}.
     */
    @Nonnull T apply(@Nonnull P player,@Nonnull M menu,@Nonnull C click,@Nonnull I clickedItem,@Nullable F fillObject);

}
