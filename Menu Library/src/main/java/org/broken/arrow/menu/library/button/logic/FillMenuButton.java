package org.broken.arrow.menu.library.button.logic;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a button within a menu that defines the action to take when clicked and the item to display.
 * This class allows customization of click actions, item display, and button update behavior.
 *
 * @param <T> the type of objects associated with the button.
 */
public class FillMenuButton<T> {


    private OnClick<ButtonUpdateAction, Player, Inventory, ClickType, ItemStack, T> click;
    private OnRetrieveItem<ItemStack, Integer, T> menuFillItem;

    private boolean updateButtonsTimer;
    private long updateTime;

    public FillMenuButton() {
        this((player, menu, click, clickedItem, fillObject) -> ButtonUpdateAction.NONE, (slot, itemStack) -> null);
    }

    public FillMenuButton(OnClick<ButtonUpdateAction, Player, Inventory, ClickType, ItemStack, T> click,
                          OnRetrieveItem<ItemStack, Integer, T> menuFillItem) {
        this.click = click;
        this.menuFillItem = menuFillItem;
    }

    /**
     * This method is used when you don't want to use it in the constructor. It provides information about the player, inventory,
     * the type of click the player performed, the itemstack, and the object associated with the specific slot.
     *
     * @param click the function instance you provide to update the button's type of action. It cannot be set to null.
     */
    public void setClick(OnClick<ButtonUpdateAction, Player, Inventory, ClickType, ItemStack, T> click) {
        this.click = click;
    }

    /**
     * This method is used when you don't want to use it in the constructor. It provides information about the current slot and
     * the object you added to the inventory. Keep in mind that it accepts null as a valid value for the object.
     *
     * @param menuFillItem the function instance you provide to specify the itemstack for a specific slot or to use the same item for all slots.
     */
    public void setMenuFillItem(OnRetrieveItem<ItemStack, Integer, T> menuFillItem) {
        this.menuFillItem = menuFillItem;
    }

    /**
     * Returns true if the buttons should be updated automatically on a timer when the menu is open and no buttons are pushed.
     * By default, this method returns false. Set this to true if you want the buttons to be updated automatically.
     *
     * @return true if the buttons should be updated automatically, false otherwise.
     */
    public boolean isUpdateButtonsTimer() {
        return updateButtonsTimer;
    }

    /**
     * Set this to true if you want this button to be updated automatically on a timer.
     *
     * @param updateButtonsTimer set this to true if the buttons should be updated automatically, false otherwise.
     */
    public void setUpdateButtonsTimer(boolean updateButtonsTimer) {
        this.updateButtonsTimer = updateButtonsTimer;
    }

    /**
     * Retrieve the interval between automatic updates of buttons.
     * If this is set to -1, it will use the global interval set by {@link org.broken.arrow.menu.library.MenuUtility#getUpdateTime()}.
     * <p>
     * Note: You need to set {@link #setUpdateButtonsTimer(boolean)} to true to enable automatic updates.
     *
     * @return -1 or the interval in seconds between updates.
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Set the interval between automatic updates of buttons.
     * If this is set to -1, it will use the global interval set by {@link org.broken.arrow.menu.library.MenuUtility#getUpdateTime()}.
     * <p>
     * Note: You need to set {@link #setUpdateButtonsTimer(boolean)} to true to enable automatic updates.
     *
     * @param updateTime -1 or the interval in seconds between updates.
     */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * Retrieve the click action associated with this button.
     *
     * @return the OnClick instance defining the action to take when the button is clicked and providing
     * how it should update the button.
     * @see ButtonUpdateAction
     */
    public OnClick<ButtonUpdateAction, Player, Inventory, ClickType, ItemStack, T> getClick() {
        return click;
    }

    /**
     * Retrieve the itemstack used for filling the button when the menu is redrawn or updated.
     *
     * @return the OnRetrieveItem instance providing the slot numbers and accounting for several pages for the button,
     *         along with the object associated with it. To display the itemstack, it requires you to provide it.
     */
    public OnRetrieveItem<ItemStack, Integer, T> getMenuFillItem() {
        return menuFillItem;
    }
}
