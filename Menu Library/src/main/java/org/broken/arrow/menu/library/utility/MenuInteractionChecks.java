package org.broken.arrow.menu.library.utility;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static org.broken.arrow.menu.library.utility.ItemCreator.isItemSimilar;

public class MenuInteractionChecks<T> {
    private final MenuUtility<T> menuUtility;

    public MenuInteractionChecks(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
    }

    public boolean whenPlayerClick(final InventoryClickEvent event, final Player player, ItemStack clickedItem) {

        if (!this.menuUtility.isAddedButtonsCacheEmpty()) {
            final int clickedSlot = event.getSlot();
            MenuUtility<T> menu = this.menuUtility;
            final int clickedPos = menu.getSlot(clickedSlot);
            Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory == null) return false;
            if (checkClickIsAllowed(event,  clickedPos, clickedInventory)) return false;

            final MenuButton menuButton = getClickedButton(clickedItem, clickedPos);
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

    public void whenPlayerDrag(final InventoryDragEvent event, final int size) {
        for (final int clickedSlot : event.getRawSlots()) {
            if (clickedSlot > size)
                continue;

            final int clickedPos = this.menuUtility.getSlot(clickedSlot);

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
            if (getClickedButton(cursor, clickedPos) == null)
                event.setCancelled(true);
        }
    }

    public MenuButton getClickedButton(final ItemStack item, final int clickedPos) {
        final MenuDataUtility<T> menuData = this.menuUtility.getMenuData(this.menuUtility.getPageNumber());
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
            return !isPlayerInventory || hasNotItemOnCursor(cursor);
        }
    }

    public ItemStack checkIfNull(final ItemStack currentCursor, final ItemStack oldCursor) {
        if (currentCursor != null) {
            return currentCursor;
        }
        return oldCursor != null ? oldCursor : new ItemStack(Material.AIR);
    }

    private boolean isPlayerInventory(final Inventory clickedInventory) {
        return clickedInventory.getType() == InventoryType.PLAYER;
    }

    private boolean hasNotItemOnCursor(ItemStack cursor) {
        return cursor == null || cursor.getType() == Material.AIR;
    }
}

