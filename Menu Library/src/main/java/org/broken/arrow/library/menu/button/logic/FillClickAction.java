package org.broken.arrow.library.menu.button.logic;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines the action to be executed when a contextual or paginated "fill" button is clicked.
 * <p>
 * This functions identically to {@link ClickAction}, but includes an additional
 * context object representing the specific data bound to the clicked slot.
 * </p>
 *
 * @param <T> the type of the contextual fill object associated with the button; may be {@code null}.
 */
@FunctionalInterface
public interface FillClickAction<T> {

    /**
     * Handles a contextual click event on a fill button.
     *
     * @param player      the player who clicked the button.
     * @param menu        the menu inventory where the click occurred.
     * @param click       the type of click (e.g., LEFT, RIGHT, SHIFT_LEFT).
     * @param clickedItem the actual {@link ItemStack} that was clicked.
     * @param fillObject  the contextual data object bound to this specific button.
     * @return the {@link ButtonUpdateAction} detailing how the menu should update.
     */
    @Nonnull ButtonUpdateAction apply(@Nonnull Player player, @Nonnull Inventory menu, @Nonnull ClickType click, @Nonnull ItemStack clickedItem, @Nullable T fillObject);

}
