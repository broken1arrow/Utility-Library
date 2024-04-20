package org.broken.arrow.menu.library.holder;

import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.button.GenericMenuButton;
import org.broken.arrow.menu.library.button.MenuButtonI;
import org.broken.arrow.menu.library.button.logic.ButtonUpdateAction;
import org.broken.arrow.menu.library.utility.FillItems;
import org.broken.arrow.menu.library.button.logic.FillMenuButton;
import org.broken.arrow.menu.library.button.logic.OnRetrieveItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Set up one or several pages with special objects tied to a specific button.
 * If you do not override the amount of pages, it will automatically add the needed amount
 * of pages for the number of objects added and use your item/items as a placeholder for
 * the object.
 *
 * @param <T> the class type of the objects added to the list.
 */
public abstract class MenuHolderPage<T> extends HolderUtility<T> {
    @Nullable
    protected List<Integer> fillSpace;
    private FillItems<T> listOfFillItems;

    /**
     * Create menu instance. You have to set {@link #setFillSpace(java.util.List)} or it will as default fill
     * all slots but not 9 on the bottom.
     *
     * @param fillItems List of items you want parse inside gui on one or several pages.
     */

    protected MenuHolderPage(final List<T> fillItems) {
        this(null, fillItems, false);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots Witch slots you want fill with items.
     * @param fillItems List of items you want parse inside gui on one or several pages.
     */
    protected MenuHolderPage(final List<Integer> fillSlots, final List<T> fillItems) {
        this(fillSlots, fillItems, false);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param fillItems       List of items you want parse inside gui.
     * @param shallCacheItems if it shall cache items and slots in this class, other case use {@link #getMenuButtonsCache()} to cache it own class.
     */
    protected MenuHolderPage(@Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(fillSlots, null, shallCacheItems);
        this.fillSpace = fillSlots;
        if (fillItems != null) {
            this.listOfFillItems = new FillItems<>();
            this.listOfFillItems.setFillItems(fillItems);
        }
    }

    /**
     * Provide your logic for incorporating your objects into a specific click and
     * itemstack with this method. Where you get provided with both your object and
     * click action, the player and much more.
     *
     * @return A FillMenuButton instance defining the action to take when a player clicks on the item,
     * the type of item to display for the player, and the item to use when updating the button.
     */
    public abstract FillMenuButton<T> createFillMenuButton();

    /**
     * Register your fill buttons, this method will return number from 0 to
     * amount you want inside the inventory.
     *
     * @param slot will return current number till will add item.
     * @return MenuButtonI you have set.
     */
    @Nullable
    @Override
    public MenuButtonI<T> getFillButtonAt(int slot) {
        FillMenuButton<T> fillMenuButton = createFillMenuButton();

        if (fillMenuButton != null) return new GenericMenuButton<T>() {

            @Override
            public void onClickInsideMenu(@Nonnull Player player, @Nonnull Inventory menu, @Nonnull ClickType click, @Nonnull ItemStack clickedItem,@Nullable T notInUse) {
                ButtonUpdateAction buttonUpdateAction = fillMenuButton.getClick().apply(player, menu, click, clickedItem, notInUse);//.apply(new FillMenuButton.OnClick<>(player, menu, click, clickedItem, object));

                switch (buttonUpdateAction) {
                    case ALL:
                        updateButtons();
                        break;
                    case THIS:
                        updateButton(this);
                        break;
                    case NONE:
                        break;
                }
            }

            @Override
            public long setUpdateTime() {
                return fillMenuButton.getUpdateTime();
            }

            @Override
            public boolean shouldUpdateButtons() {
                return fillMenuButton.isUpdateButtonsTimer();
            }

            @Override
            public ItemStack getItem(int slot, @Nullable T notInUse) {
                final T object = getFillItem(slot);
                OnRetrieveItem<ItemStack, Integer, T> menuItem = fillMenuButton.getMenuFillItem();
                return menuItem.apply(slot, object);
            }

            @Override
            public ItemStack getItem() {
                return null;
            }
        };
        return null;
    }

    @Override
    public void onClick(@Nonnull MenuButtonI<T> menuButton, @Nonnull Player player, int clickedPos, @Nonnull ClickType clickType, @Nonnull ItemStack clickedItem) {
        int slot = clickedPos + (this.getPageNumber() * this.getNumberOfFillItems());
        final T object = this.getFillItem(slot);
        if (this.getMenu() != null) {
            menuButton.onClickInsideMenu(player, this.getMenu(), clickType, clickedItem, object);
        }
    }

    @Nullable
    public FillItems<T> getListOfFillItem() {
        return listOfFillItems;
    }

    @Nullable
    public T getFillItem(int index) {
        FillItems<T> fillItems = getListOfFillItem();
        if (fillItems != null) {
            return fillItems.getFillItem(index);
        }
        return null;
    }

    @Override
    protected final double amountOfPages() {
        if (getListOfFillItem() == null) return getManuallySetPages();

        final List<T> fillItems = this.getListOfFillItem().getFillItems();
        final List<Integer> fillSlots = this.fillSpace == null ? this.getFillSpace() : this.fillSpace;
        if (this.itemsPerPage > 0) {
            if (!fillSlots.isEmpty()) {
                return (double) fillSlots.size() / this.itemsPerPage;
            } else if (fillItems != null && !fillItems.isEmpty()) return (double) fillItems.size() / this.itemsPerPage;
        }
        if (fillItems != null && !fillItems.isEmpty()) {
            return (double) fillItems.size() / (fillSlots.isEmpty() ? this.inventorySize - 9 : fillSlots.size());
        }
        return getManuallySetPages();
    }

    @Override
    @Nullable
    protected ItemStack getMenuItem(final MenuButtonI<T> menuButton, final ButtonData<T> cachedButtons, final int slot, final boolean updateButton) {
        if (menuButton == null) return null;

        if (updateButton) {
            ItemStack itemStack = menuButton.getItem();
            if (itemStack != null) return itemStack;
            itemStack = menuButton.getItem(cachedButtons.getObject());
            if (itemStack != null) return itemStack;

            itemStack = menuButton.getItem(slot + (this.getPageNumber() * this.getNumberOfFillItems()), cachedButtons.getObject());
            return itemStack;
        }
        return null;
    }
}
