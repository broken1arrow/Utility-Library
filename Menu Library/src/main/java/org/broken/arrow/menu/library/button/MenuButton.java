package org.broken.arrow.menu.library.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MenuButton {

	private static int counter = 0;
	private final int id;

	protected MenuButton() {
		this.id = counter++;
	}

	/**
	 * when you click inside the menu.
	 *
	 * @param player      player some clicked in the menu.
	 * @param menu        menu some are currently open.
	 * @param click       click type (right,left or shift click)
	 * @param clickedItem item some are clicked on
	 */
	public abstract void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem);

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
	 * It will use the global from {@link org.broken.arrow.menu.library.MenuUtility#getUpdateTime()}
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
	public int getId(){
		return id;
	}
}
