package org.broken.arrow.library.menu.button;

import org.broken.arrow.library.menu.button.logic.ClickContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class extends {@link MenuButton} to enhance its functionality by including
 * the fill object provided in your menu.
 * <p>&nbsp;</p>
 * <p>It provides easy access to your class object both when a player interacts with the item
 * (e.g., clicks on it) and when you set the item. For example, you can save the fill object
 * that a player clicks on to a cache or perform actions based on whether the fill object exists
 * in the cache. Additionally, when setting the item, you can alter its properties based on the
 * fill object.</p>
 *
 * @param <T> the type of the fill item for the menu.
 * @see MenuButton
 */
public abstract class MenuButtonPage<T> extends MenuButton {

    protected MenuButtonPage() {
    }

    @Override
    public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final ClickType click, @Nonnull final ClickContext clickContext) {
        this.onClickInsideMenu(player, click, null, clickContext);
    }

    /**
     * when you click inside the menu.
     * <p>
     * This method acts as the entry point for interactions. It provides a modern {@link ClickContext}
     * containing secondary details (menu, item and more), while primary details are passed explicitly.
     * </p>
     *
     * @param player       player some clicked in the menu.
     * @param click        click type (right,left or shift click)
     * @param object       object that is connected to the menu button item.
     * @param clickContext the contextual details regarding the menu and slot interaction
     */
    public abstract void onClickInsideMenu(@Nonnull final Player player, @Nonnull final ClickType click, @Nullable final T object, @Nonnull final ClickContext clickContext);

    /**
     * when you click inside the menu.
     *
     * @param player      player some clicked in the menu.
     * @param menu        menu some are currently open.
     * @param click       click type (right,left or shift click)
     * @param clickedItem item some are clicked on
     * @param object      object that is connected to the menu button item.
     * @deprecated use the new {@link #onClickInsideMenu(Player, ClickType, Object, ClickContext)} as it gives cleaner usage pattern with more context and
     * this is not invoked in the code any longer.
     */
    @Deprecated
    public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem, @Nullable final T object) {
    }


    /**
     * Retrieves the item that is associated with the specified fill item.
     *
     * @param fillItem The fill item for which the itemStack will present the object in your implementation.
     * @return The itemStack associated with the fill item, or null if not match your preferences.
     */
    @Nullable
    public ItemStack getItem(@Nonnull final T fillItem) {
        return null;
    }

    /**
     * Retrieves the item that is associated with the specified fill item and slot.
     *
     * @param slot     The slot number where the item is added. This number can exceed the inventory size.
     * @param fillItem The fill item for which the itemstack will present the object in your implementation.
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
