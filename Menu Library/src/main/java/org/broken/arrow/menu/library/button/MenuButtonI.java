package org.broken.arrow.menu.library.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface  MenuButtonI<T> {


	/**
	 * when you click inside the menu.
	 *
	 * @param player      player some clicked in the menu.
	 * @param menu        menu some are currently open.
	 * @param click       click type (right,left or shift click)
	 * @param clickedItem item some are clicked on
	 * @param object      object some are clicked on (default is it itemstack).
	 */
	void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem,@Nullable final T object);

	/**
	 * get the item some are added in your menu
	 *
	 * @return itemstack
	 */
	ItemStack getItem();

	/**
	 * get the item some are added in your menu
	 *
	 * @param object is your list of fill items some get returned
	 * @return itemstack
	 */
	default ItemStack getItem(@Nonnull final T object) {
		return null;
	}

	/**
	 * get the item some are added in your menu
	 *
	 * @param slot   curent slot it add item too.
	 * @param object is your list of fill items some get returned
	 * @return itemstack
	 */
	default ItemStack getItem(final int slot, @Nullable final T object) {
		return null;
	}

	/**
	 * Set your own time, if and when it shall update buttons. If this is set to -1
	 * It will use the global from {@link org.broken.arrow.menu.library.MenuUtility#getUpdateTime()}
	 * <p>
	 * You also need set this to true {@link #shouldUpdateButtons()}
	 *
	 * @return -1 or seconds between updates.
	 */
	default long setUpdateTime() {
		return -1;
	}

	/**
	 * Returns true if the buttons should be updated, when menu is open and no buttons are pushed. By default, this method
	 * returns false. If you want to update the buttons, override this method and return true in your implementation.
	 *
	 * @return true if the buttons should be updated, false otherwise.
	 */
	default boolean shouldUpdateButtons() {
		return false;
	}

	/**
	 * The unique id for this instance.
	 *
	 * @return the id for this instance.
	 */
	int getId();
}
