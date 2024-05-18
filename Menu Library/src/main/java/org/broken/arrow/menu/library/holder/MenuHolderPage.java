package org.broken.arrow.menu.library.holder;

import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.button.MenuButtonPage;
import org.broken.arrow.menu.library.button.logic.ButtonUpdateAction;
import org.broken.arrow.menu.library.button.logic.FillMenuButton;
import org.broken.arrow.menu.library.button.logic.OnRetrieveItem;
import org.broken.arrow.menu.library.utility.FillItems;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a utility class for setting up one or several pages with special objects tied to specific buttons
 * in a paged menu system. It also supports adding custom buttons in non-fill slots, which will be automatically
 * replicated across all pages.
 * <p>&nbsp;</p>
 * <p>
 * If the number of pages is not explicitly overridden, this class will automatically add the necessary amount
 * of pages based on the number of objects added, using the specified items as placeholders.
 * </p>
 *
 * @param <T> the class type of the objects added to the list.
 */
public abstract class MenuHolderPage<T> extends HolderUtility<T> {

    private FillItems<T> listOfFillItems;
    private Map<Integer, Integer> fillSlots = new HashMap<>();

    /**
     * Constructs a paged menu instance specified list of objects. You need to
     * set {@link #setFillSpace(List)} or {@link #setFillSpace(String)}; otherwise, it will automatically use
     * all slots beside the last 9 slots in your menu.
     *
     * @param fillItems The list of items to be displayed inside the GUI on one or several pages.
     */
    protected MenuHolderPage(final List<T> fillItems) {
        this(null, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified fill slots and list of objects.
     *
     * @param fillSlots The slots to be filled with items on each page. Must not exceed the inventory size.
     * @param fillItems The list of items to be displayed inside the GUI on one or several pages.
     */
    protected MenuHolderPage(final List<Integer> fillSlots, final List<T> fillItems) {
        this(fillSlots, fillItems, false);
    }

    /**
     * Constructs a paged menu instance with specified fill slots, items, and caching option.
     *
     * @param fillSlots       The slots to be filled with items on each page.
     * @param fillItems       The list of items to be displayed inside the GUI.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class;
     *                        otherwise, override {@link #retrieveMenuButtons(int, Map)} to cache
     *                        them in your own implementation.
     */
    protected MenuHolderPage(@Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        this(RegisterMenuAPI.getMenuAPI(), fillSlots, fillItems, shallCacheItems);
    }

    /**
     * Constructs a paged menu instance with specified parameters.
     * <p>&nbsp;</p>
     * <strong>Note:</strong> Use this constructor if you are not shading this library into your plugin.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots       The slots to be filled with items on each page.
     * @param fillItems       The list of items to be displayed inside the GUI.
     * @param shallCacheItems Set this to false if items and slots should be cached in this class;
     *                        otherwise, override {@link #retrieveMenuButtons(int, Map)} to cache
     *                        them in your own implementation.
     */
    protected MenuHolderPage(@Nonnull RegisterMenuAPI menuAPI, @Nullable List<Integer> fillSlots, @Nullable List<T> fillItems, boolean shallCacheItems) {
        super(menuAPI, fillSlots, shallCacheItems);
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
     * the type of item to display for the player, and the item to use when updating the button
     * depending on the object you provide.
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
    public MenuButton getFillButtonAt(int slot) {
        if (slot == -1) return null;

        FillMenuButton<T> fillMenuButton = createFillMenuButton();
        if (fillMenuButton != null) return new MenuButtonPage<T>() {
            @Override
            public void onClickInsideMenu(@Nonnull Player player, @Nonnull Inventory menu, @Nonnull ClickType click, @Nonnull ItemStack clickedItem, @Nullable T fillItem) {
                ButtonUpdateAction buttonUpdateAction = fillMenuButton.getClick().apply(player, menu, click, clickedItem, fillItem);

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
            public ItemStack getItem(int slot, @Nullable T fillItem) {
                //final T object = getFillItem(slot);

                OnRetrieveItem<ItemStack, Integer, T> menuItem = fillMenuButton.getMenuFillItem();
                return menuItem.apply(slot, fillItem);
            }
        };
        return null;
    }

    @Override
    public void onClick(@Nonnull MenuButton menuButton, @Nonnull Player player, int clickedPos, @Nonnull ClickType clickType, @Nonnull ItemStack clickedItem) {
        //int slot = fillSlots.getOrDefault(clickedPos, -1) + (this.getPageNumber() * this.getNumberOfFillItems());
        int slot = fillSlots.getOrDefault(clickedPos, -1);
        if (this.getMenu() != null) {
            if (menuButton instanceof MenuButtonPage) {
                final T object = this.getFillItem(slot);
                ((MenuButtonPage<T>) menuButton).onClickInsideMenu(player, this.getMenu(), clickType, clickedItem, object);
            } else {
                menuButton.onClickInsideMenu(player, this.getMenu(), clickType, clickedItem);
            }
        }
    }

    @Nullable
    public FillItems<T> getListOfFillItem() {
        return listOfFillItems;
    }

    @Override
    @Nullable
    public List<T> getListOfFillItems() {
        if (getListOfFillItem() != null)
            return getListOfFillItem().getFillItems();
        return new ArrayList<>();
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
        final List<Integer> fillSlots = this.getFillSpace();
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
    protected void setButton(final int pageNumber, final MenuDataUtility<T> menuDataUtility, final int slot, final int fillSlotIndex, final boolean isLastFillSlot) {
        final int fillSlot = isLastFillSlot ? -1 : fillSlotIndex;

        final MenuButton menuButton = getMenuButtonAtSlot(slot, fillSlot);
        final ItemStack result = getItemAtSlot(menuButton, slot, fillSlot);

        if (pageNumber == getPageNumber() && fillSlot >= 0) {
            this.fillSlots.put(slot, fillSlot);
        }

        if (menuButton != null) {
            T fillItem = getFillItem(fillSlot);
            boolean shallAddMenuButton = !isLastFillSlot && isFillSlot(slot) && this.getListOfFillItems() != null && !this.getListOfFillItems().isEmpty();
            if (menuButton.shouldUpdateButtons()) this.getButtonsToUpdate().add(menuButton);

            final ButtonData<T> buttonData = new ButtonData<>(result, shallAddMenuButton ? null : menuButton, fillItem);
            menuDataUtility.putButton(this.getSlot(slot), buttonData, shallAddMenuButton ? menuButton : null);
        }
    }

    @Override
    protected ItemStack getItemAtSlot(final MenuButton menuButton, final int slot, final int fillSlot) {
        if (menuButton == null) return null;

        final List<Integer> fillSlots = this.getFillSpace();
        ItemStack result = null;

        if (fillSlots.contains(slot)) {
            MenuButtonPage<T> menuButtonPage = getPagedMenuButton(menuButton);
            T fillItem = getFillItem(fillSlot);

            if (menuButtonPage != null) {
                if (fillItem != null) result = menuButtonPage.getItem(fillItem);
                if (result == null) result = menuButtonPage.getItem(fillSlot, fillItem);
            }

        }
        if (result == null) result = menuButton.getItem();
        if (result == null) result = menuButton.getItem(fillSlot);

        return result;
    }

    @Override
    @Nullable
    protected ItemStack getMenuItem(final MenuButton menuButton, final ButtonData<T> cachedButtons, final int slot, final boolean updateButton) {
        if (menuButton == null) return null;

        if (updateButton) {
            ItemStack itemStack = menuButton.getItem();
            if (itemStack != null) return itemStack;
            MenuButtonPage<T> menuButtonPage = getPagedMenuButton(menuButton);
            if (menuButtonPage != null) {
                itemStack = menuButtonPage.getItem(cachedButtons.getObject());
                if (itemStack != null) return itemStack;
                itemStack = menuButtonPage.getItem(slot + (this.getPageNumber() * this.getNumberOfFillItems()), cachedButtons.getObject());
            }
            return itemStack;
        }
        return null;
    }

    @Nullable
    private MenuButtonPage<T> getPagedMenuButton(MenuButton menuButton) {
        if (menuButton instanceof MenuButtonPage)
            return (MenuButtonPage<T>) menuButton;
        return null;
    }

    private boolean isFillSlot(final int slot) {
        final List<Integer> fillSlots = this.fillSpace == null ? this.getFillSpace() : this.fillSpace;
        return !fillSlots.isEmpty() && fillSlots.contains(slot);
    }
}
