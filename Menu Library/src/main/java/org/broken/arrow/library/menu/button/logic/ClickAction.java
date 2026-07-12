package org.broken.arrow.library.menu.button.logic;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Defines the action to be executed when a standard menu button is clicked.
 * <p>
 * Implementations of this functional interface dictate the logic that runs
 * upon a click and must return a {@link ButtonUpdateAction} to signal
 * how the menu inventory should refresh afterward.
 * </p>
 */
@FunctionalInterface
public interface ClickAction {

    /**
     * Handles a click event on a button.
     *
     * @param player      the player who clicked the button.
     * @param click       the type of click (e.g., LEFT, RIGHT, SHIFT_LEFT).
     * @param clickContext the context for the click.
     * @return the {@link ButtonUpdateAction} detailing how the menu should update.
     */
    @Nonnull
    ButtonUpdateAction apply(@Nonnull final Player player,  @Nonnull final ClickType click, @Nonnull final ClickContext clickContext);
}