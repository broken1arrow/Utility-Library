package org.broken.arrow.menu.library.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MenuButtonPage<T> extends MenuButton {

    protected MenuButtonPage() {}

    @Override
    public void onClickInsideMenu(@Nonnull Player player, @Nonnull Inventory menu, @Nonnull ClickType click, @Nonnull ItemStack clickedItem) {
        this.onClickInsideMenu(player, menu, click, clickedItem, null);
    }

    /**
     * when you click inside the menu.
     *
     * @param player      player some clicked in the menu.
     * @param menu        menu some are currently open.
     * @param click       click type (right,left or shift click)
     * @param clickedItem item some are clicked on
     * @param object      object that ios connected to the item.
     */
    public abstract void onClickInsideMenu(@Nonnull Player player, @Nonnull Inventory menu, @Nonnull ClickType click, @Nonnull ItemStack clickedItem, @Nullable T object);


    /**
     * Retrieves the item that is associated with the specified fill item.
     *
     * @param fillItem The fill item for which the itemstack will presents the object in your implementation.
     * @return The itemstack associated with the fill item, or null if not match your preferences.
     */
    @Nullable
    public ItemStack getItem(@Nonnull final T fillItem) {
        return null;
    }

    /**
     * Retrieves the item that is associated with the specified fill item and slot.
     *
     * @param slot     The slot number where the item is added. This number can exceed the inventory size.
     * @param fillItem The fill item for which the itemstack will presents the object in your implementation.
     * @return The itemstack associated with the fill item and slot, or null if not found.
     */
    @Nullable
    public ItemStack getItem(int slot, @Nullable T fillItem) {
        return null;
    }

    @Nullable
    @Override
    public ItemStack getItem() {
        return null;
    }


}
