package org.broken.arrow.library.menu.button;

import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.button.logic.ClickContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * The base class for a menu button.
 * <p>
 * This class provides functionality to handle player interactions with menu buttons, including
 * managing the display item for the button.</p>
 * <p>
 * Additionally, it supports updating the button's display item at a specified interval. This allows
 * modifications to attributes such as the display name, lore, and other properties of the button
 * without requiring the player to reopen the menu or press a button.
 * </p>
 */
public abstract class MenuButton {

    private static int counter = 0;
    private final int id;

    /**
     * new button instance that sets the id for the button.
     */
    protected MenuButton() {
        this.id = counter++;
    }

    /**
     * Handles the interaction event when a player clicks on this button within a menu.
     * <p>
     * This method acts as the entry point for interactions. It provides a modern {@link ClickContext}
     * containing secondary details (menu, item and more), while primary details are passed explicitly.
     * </p>
     *
     * @param player       the player who performed the click.
     * @param click        the type of click interaction (e.g., LEFT, RIGHT, SHIFT_LEFT).
     * @param clickContext the contextual details regarding the menu and slot interaction.
     */
    public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final ClickType click, @Nonnull final ClickContext clickContext) {
        onClickInsideMenu(player, clickContext.getMenu(), click, clickContext.getClickedItem());
    }

    /**
     * Legacy handler for menu clicks. Will soon be marked as deprecated.
     *
     * @param player      the player who performed the click.
     * @param menu        the menu inventory where the click occurred.
     * @param click       the type of click interaction.
     * @param clickedItem the {@link ItemStack} that was clicked.
     */
    public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem) {
    }

    /**
     * Retrieves the itemstack to be added with this menu button.
     *
     * @return The itemstack to be added with this menu button, or null if not specified.
     */
    @Nullable
    public abstract ItemStack getItem();

    /**
     * Retrieves the itemstack associated with the specified slot or index in your implementation.
     *
     * @param slot The slot number where the item is added. This number can exceed the inventory size.
     * @return The itemstack associated with the specified slot, or null if not the slot match.
     */
    @Nullable
    public ItemStack getItem(final int slot) {
        return null;
    }

    /**
     * Set your own time, if and when it shall update buttons. If this is set to -1
     * It will use the global from {@link MenuUtility#getUpdateTime()}
     * <p>
     * You also need set this to true {@link #shouldUpdateButtons()}
     *
     * @return -1 or seconds between updates.
     */
    public long setUpdateTime() {
        return -1;
    }

    /**
     * Returns true if the buttons should be updated, when menu is open and no buttons are pushed. By default, this method
     * returns false. If you want to update the buttons, override this method and return true in your implementation.
     *
     * @return true if the buttons should be updated, false otherwise.
     */
    public boolean shouldUpdateButtons() {
        return false;
    }

    /**
     * The unique id for this instance.
     *
     * @return the id for this instance.
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MenuButton{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuButton)) return false;
        MenuButton that = (MenuButton) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
