package org.broken.arrow.library.menu.button.logic;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contains additional, contextual data for a menu button click.
 * Designed to be used alongside primary parameters like Player and ClickType.
 */
public class ClickContext {
    private final Inventory menu;
    private final ItemStack clickedItem;
    private final int slot;

    /**
     * Create the instance for the context of the player click.
     *
     * @param menu        the menu clicked inside.
     * @param clickedItem the item the player clicking on.
     * @param slot        the slot the player clicking inside the menu.
     */
    public ClickContext(@Nonnull final Inventory menu, @Nonnull final ItemStack clickedItem, final int slot) {
        this.menu = menu;
        this.clickedItem = clickedItem;
        this.slot = slot;
    }

    /**
     * Retrieve the menu.
     *
     * @return the menu player currently have open.
     */
    @Nonnull
    public Inventory getMenu() {
        return menu;
    }

    /**
     * Retrieve the item player clicking on.
     *
     * @return the itemStack player clicking on.
     */
    @Nonnull
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    /**
     * Retrieve the clicked slot.
     *
     * @return the slot player clicking on.
     */
    public int getSlot() {
        return slot;
    }
}