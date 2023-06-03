package org.broken.arrow.menu.library.builders;

import org.broken.arrow.menu.library.button.MenuButtonI;
import org.bukkit.inventory.ItemStack;

public class ButtonData<T> {

	private final ItemStack itemStack;
	private final MenuButtonI<T> menuButtonLinkedToThisItem;
	private final int id;
	private final T object;

	public ButtonData(final ItemStack itemStack, final MenuButtonI<T> menuButton, final T object) {
		this.itemStack = itemStack;
		this.menuButtonLinkedToThisItem = menuButton;
		this.id = menuButton != null ? menuButton.getId() : 0;
		this.object = object;
	}

	/**
	 * the itemstack you want to be displayed in the menu.
	 *
	 * @return the itemstack you added in the menu.
	 */
	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * The button linked to this item.
	 *
	 * @return menuButton.
	 */
	public MenuButtonI<T> getMenuButton() {
		return menuButtonLinkedToThisItem;
	}

	/**
	 * Get the uniqe id for this button.
	 *
	 * @return the id or 0 if not set.
	 */

	public int getId() {
		return id;
	}

	/**
	 * get the data linked to this item.
	 *
	 * @return object data you want this item contains.
	 */
	public T getObject() {
		return object;
	}
}

