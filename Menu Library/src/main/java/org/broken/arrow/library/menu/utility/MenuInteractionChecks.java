package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.builders.ButtonData;
import org.broken.arrow.library.menu.builders.MenuDataUtility;
import org.broken.arrow.library.menu.button.MenuButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static org.broken.arrow.library.menu.utility.ItemCreator.isItemSimilar;

/**
 * Handles interaction checks and event processing for menu GUIs.
 * <p>
 * This class manages player click and drag events on inventory menus,
 * verifying if clicks are allowed, identifying clicked buttons,
 * and cancelling events when necessary to enforce menu behavior.
 *
 * @param <T> the type of the menu data or context handled by {@link MenuUtility}
 */
public class MenuInteractionChecks<T> {
    private final MenuUtility<T> menuUtility;

    /**
     * Creates a new instance with the given menu utility to handle menu interactions.
     *
     * @param menuUtility the {@link MenuUtility} instance associated with this menu
     */
    public MenuInteractionChecks(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
    }

    /**
     * Handles an inventory click event and determines if the click interacts with a menu button.
     * If the clicked item corresponds to a registered button, the event is cancelled and the button's action is executed.
     *
     * @param event       the {@link InventoryClickEvent} triggered by the player
     * @param player      the {@link Player} who clicked
     * @param clickedItem the {@link ItemStack} that was clicked on
     * @return true if the click was handled by a menu button, false otherwise
     */
    public boolean whenPlayerClick(final InventoryClickEvent event, final Player player, ItemStack clickedItem) {

        if (!this.menuUtility.isAddedButtonsCacheEmpty()) {
            final int clickedSlot = event.getSlot();
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == null) return false;
            if (checkClickIsAllowed(event, clickedSlot, clickedInventory)) return false;

            final MenuButton menuButton = getClickedButton(player,clickedItem, clickedSlot);
            if (menuButton != null) {
                event.setCancelled(true);
                if (clickedInventory.getType() == InventoryType.PLAYER ) {
                    return false;
                }
                if (clickedItem == null)
                    clickedItem = new ItemStack(Material.AIR);
                this.menuUtility.onClick(menuButton, player, clickedSlot, event.getClick(), clickedItem);
                return true;
            }
        }
        return false;
    }

    /**
     * Handles inventory drag events within the menu.
     * Cancels the event if dragging occurs over protected slots or disallowed areas,
     * respecting the menu's fill space and item addition permissions.
     *
     * @param event the {@link InventoryDragEvent} triggered by the player
     * @param size  the size limit of the inventory/menu slots
     */
    public void whenPlayerDrag(final InventoryDragEvent event, final int size) {
        for (final int clickedSlot : event.getRawSlots()) {
            if (clickedSlot > size)
                continue;

            final ItemStack cursor = checkIfNull(event.getCursor(), event.getOldCursor());
            if (this.menuUtility.isSlotsYouCanAddItems()) {
                if (this.menuUtility.getFillSpace().contains(clickedSlot)) {
                    return;
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
            if (getClickedButton((Player) event.getWhoClicked(), cursor, clickedSlot) == null)
                event.setCancelled(true);
        }
    }

    /**
     * Retrieves the {@link MenuButton} corresponding to a clicked slot and item,
     * if one exists and passes item similarity checks.
     *
     * @param player     the {@link Player} who clicked
     * @param item       the {@link ItemStack} clicked
     * @param clickedPos the slot index that was clicked
     * @return the associated {@link MenuButton}, or null if none matches
     */
    public MenuButton getClickedButton(@Nonnull final Player player, final ItemStack item, final int clickedPos) {
        final MenuDataUtility<T> menuData = this.menuUtility.getMenuData(player);
        if (menuData != null) {
            final ButtonData<?> buttonData = menuData.getButton(clickedPos);
            if (buttonData == null) return null;
            if (this.menuUtility.isIgnoreItemCheck()) {
                return menuData.getMenuButton(clickedPos);
            }
            if (isItemSimilar(buttonData.getItemStack(), item)) {
                return menuData.getMenuButton(clickedPos);
            }
        }
        return null;
    }

    /**
     * Checks whether a click on a given slot and inventory is allowed according to menu rules.
     * Cancels the event if the click violates conditions like shift-click or restricted slots.
     *
     * @param event            the {@link InventoryClickEvent} to check
     * @param clickedPos       the slot index clicked
     * @param clickedInventory the inventory that was clicked
     * @return true if the click is disallowed and the event was cancelled, false otherwise
     */
    private boolean checkClickIsAllowed(final InventoryClickEvent event, final int clickedPos, final Inventory clickedInventory) {
        final ItemStack cursor = event.getCursor();
        MenuUtility<?> menu = this.menuUtility;
        if (!menu.isAllowShiftClick() && event.getClick().isShiftClick()) {
            event.setCancelled(true);
            return true;
        }
        boolean isPlayerInventory = isPlayerInventory(clickedInventory);
        if (menu.isSlotsYouCanAddItems()) {
            if (menu.getFillSpace().contains(clickedPos) || menu.getFillSpace().contains(event.getSlot())) {
                return true;
            } else {
                if (!isPlayerInventory) {
                    event.setCancelled(true);
                }
            }
            return isPlayerInventory;
        } else {
            if (isPlayerInventory || hasNotItemOnCursor(cursor)) {
                event.setCancelled(true);
            }
            return false;
        }
    }

    /**
     * Returns the current cursor {@link ItemStack}, or falls back to an old cursor if current is null,
     * or AIR if both are null.
     *
     * @param currentCursor the current cursor {@link ItemStack}
     * @param oldCursor     the previous cursor {@link ItemStack}
     * @return a non-null {@link ItemStack} representing the cursor item
     */
    public ItemStack checkIfNull(final ItemStack currentCursor, final ItemStack oldCursor) {
        if (currentCursor != null) {
            return currentCursor;
        }
        return oldCursor != null ? oldCursor : new ItemStack(Material.AIR);
    }

    /**
     * Checks if the given inventory is a player inventory.
     *
     * @param clickedInventory the inventory to check
     * @return true if the inventory type is {@link InventoryType#PLAYER}, false otherwise
     */
    private boolean isPlayerInventory(final Inventory clickedInventory) {
        return clickedInventory.getType() == InventoryType.PLAYER;
    }

    /**
     * Checks if the cursor {@link ItemStack} is empty or AIR.
     *
     * @param cursor the cursor {@link ItemStack}
     * @return true if cursor is null or of type AIR, false otherwise
     */
    private boolean hasNotItemOnCursor(ItemStack cursor) {
        return cursor == null || cursor.getType() == Material.AIR;
    }
}

