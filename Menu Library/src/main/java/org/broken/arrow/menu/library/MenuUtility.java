package org.broken.arrow.menu.library;

import com.google.gson.JsonObject;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.broken.arrow.menu.library.holder.MenuHolder;
import org.broken.arrow.menu.library.holder.MenuHolderPage;
import org.broken.arrow.menu.library.holder.utility.AnimateTitleTask;
import org.broken.arrow.menu.library.holder.utility.InventoryRenderer;
import org.broken.arrow.menu.library.holder.utility.LoadInventoryHandler;
import org.broken.arrow.menu.library.holder.utility.MenuRenderer;
import org.broken.arrow.menu.library.runnable.ButtonAnimation;
import org.broken.arrow.menu.library.utility.Action;
import org.broken.arrow.menu.library.utility.Function;
import org.broken.arrow.menu.library.utility.MenuInteractionChecks;
import org.broken.arrow.menu.library.utility.MetadataPlayer;
import org.broken.arrow.menu.library.utility.SoundUtility;
import org.broken.arrow.menu.library.utility.metadata.MenuMetadataKey;
import org.broken.arrow.title.update.library.UpdateTitle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.broken.arrow.menu.library.utility.ItemCreator.convertMaterialFromString;


/**
 * Contains methods to create menu as you want it. Recommend you extends {@link MenuHolder} or {@link MenuHolderPage} to get all methods needed.
 */

public class MenuUtility<T> {

    private final Logging logger = new Logging(MenuUtility.class);

    private final MenuRenderer<T> menuRenderer;
    private final CheckItemsInsideMenu checkItemsInsideMenu;
    private final List<MenuButton> buttonsToUpdate = new ArrayList<>();
    private final Map<Integer, MenuDataUtility<T>> pagesOfButtonsData = new HashMap<>();
    private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();
    private final MenuInteractionChecks<T> menuInteractionChecks;
    private final InventoryRenderer<T> inventoryRender;
    private final LoadInventoryHandler<T> loadInventoryHandler;

    protected List<Integer> fillSpace;
    protected RegisterMenuAPI menuAPI;
    protected InventoryType inventoryType;
    protected Player player;
    protected Sound menuOpenSound;
    protected Function<String> titleFunction;
    protected Function<JsonObject> titleFunctionJson;
    protected Function<String> animateTitle;
    protected Function<JsonObject> animateTitleJson;

    protected boolean shallCacheItems;
    protected boolean slotsYouCanAddItems;
    protected boolean allowShiftClick;
    protected boolean ignoreValidCheck;
    protected boolean autoClearCache;
    protected boolean ignoreItemCheck;
    protected boolean autoTitleCurrentPage;
    protected boolean useColorConversion;

    protected int animateButtonTime = 20;
    protected int slotIndex;
    protected int inventorySize;
    protected int itemsPerPage = this.inventorySize - 9;
    protected int pageNumber;
    protected int updateTime;
    protected int animateTitleTime = 5;
    protected int highestFillSlot;

    private AnimateTitleTask<T> animateTitleTask;
    private ButtonAnimation<T> buttonAnimation;

    private Inventory inventory;
    private String playerMetadataKey;

    private int manuallySetPages = -1;


    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param shallCacheItems if it shall cache items and slots in this class, other case override {@link #retrieveMenuButtons(int, MenuDataUtility)}  to cache it own class.
     */
    public MenuUtility(@Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        this(RegisterMenuAPI.getMenuAPI(), fillSlots, shallCacheItems);
    }

    /**
     * Creates a menu instance.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin. Use this constructor
     *                        only if you are using the plugin and you don't need provide this when you shaded it.
     * @param fillSlots       The slots you want to fill with items. Can be null if not filling specific slots.
     * @param shallCacheItems Indicates whether items and slots should be cached in this class. If false,
     *                        override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache it own class.
     */
    public MenuUtility(@Nonnull final RegisterMenuAPI menuAPI, @Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        this.fillSpace = fillSlots;
        if (fillSlots != null)
            this.highestFillSlot = fillSlots.stream().mapToInt(Integer::intValue).max().orElse(-1);

        this.menuRenderer = new MenuRenderer<>(this);
        this.inventoryRender = new InventoryRenderer<>(this);
        this.menuInteractionChecks = new MenuInteractionChecks<>(this);
        this.loadInventoryHandler = new LoadInventoryHandler<>(this, menuAPI);

        this.shallCacheItems = shallCacheItems;
        this.allowShiftClick = true;
        this.autoClearCache = true;
        this.ignoreItemCheck = true;
        this.autoTitleCurrentPage = true;
        this.slotIndex = 0;
        this.updateTime = -1;
        this.menuOpenSound = new SoundUtility().getMenuOpenSound();
        this.menuAPI = menuAPI;
        this.checkItemsInsideMenu = new CheckItemsInsideMenu(menuAPI);
    }

    /**
     * Register your buttons you want inside the menu.
     *
     * @param slot will return slot number it will add item.
     * @return MenuButton you have set.
     */
    @Nullable
    public MenuButton getButtonAt(final int slot) {
        return null;
    }


    /**
     * Registers buttons using the list of slots from {@link #getFillSpace()}.
     * This method returns a number from 0 to the highest slot number specified
     * in the list or the inventory's maximum size, whichever is smaller.
     * <p>&nbsp;</p>
     * This method operates differently from the {@link MenuHolderPage#getFillButtonAt(int)}
     * in the {@link MenuHolderPage} class. Therefore, you may need to implement custom logic
     * to make the buttons function as desired.
     *
     * @param slot the slot number to register the button in.
     * @return the {@link MenuButton} set in the specified slot.
     */
    @Nullable
    public MenuButton getFillButtonAt(final int slot) {
        return null;
    }

    /**
     * Override this method if you want to cache the menu buttons in own class.
     *
     * @param pageNumber      the page number
     * @param menuDataUtility the menu cache for the specific page.
     */
    public void retrieveMenuButtons(int pageNumber, MenuDataUtility<T> menuDataUtility) {
        //override this class if you want to cache the buttons self.
    }

    /**
     * Get the set inventory type.
     *
     * @return inventory type.
     */
    @Nullable
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    @Nonnull
    protected Map<Integer, Long> getTimeWhenUpdatesButtons() {
        return timeWhenUpdatesButtons;
    }

    @Nullable
    public Long getTimeWhenUpdatesButton(final MenuButton menuButton) {
        return getTimeWhenUpdatesButtons().getOrDefault(menuButton.getId(), null);
    }

    /**
     * Get if this menu allow shiftclick or not. Defult will
     * it allow shiftclick.
     *
     * @return true if shiftclick shall be allowd.
     */

    public boolean isAllowShiftClick() {
        return allowShiftClick;
    }

    /**
     * If this is set to true, you can then add or remove items in the menu.
     * You need specify the slots with one of this two methods {@link MenuHolder#setFillSpace(String)} or
     * {@link MenuHolder#setFillSpace(List)} for get it work.
     *
     * @return true will you have option add items.
     */
    public boolean isSlotsYouCanAddItems() {
        return slotsYouCanAddItems;
    }

    /**
     * Get update buttons time, this is general time
     * for all buttons.
     * <p>
     * You also need set this to true {@link MenuButton#shouldUpdateButtons()}
     *
     * @return seconds between the updates, default it will return -1 and don't update the buttons.
     */
    public int getUpdateTime() {
        return updateTime;
    }

    /**
     * check if it shall ignore the check
     * if the item match or not.
     *
     * @return true if it shall ignore the set item inside inventory.
     */
    public boolean isIgnoreItemCheck() {
        return ignoreItemCheck;
    }

    /**
     * Check if the cache contains the requested page.
     *
     * @return true if the cache is empty.
     */
    public boolean isAddedButtonsCacheEmpty() {
        return this.pagesOfButtonsData.isEmpty();
    }

    /**
     * Check if the cache contains the requested page.
     *
     * @param pageNumber The pagenumber you want to open.
     * @return true if the page exist.
     */
    public boolean containsPage(final Integer pageNumber) {
        return this.pagesOfButtonsData.containsKey(pageNumber);
    }

    /**
     * Get all slots and items inside the cache, on this page.
     *
     * @param pageNumber of the page you want to get.
     * @return map with slots for every item are placed and items.
     */
    @Nullable
    public MenuDataUtility<T> getMenuData(final int pageNumber) {
        return this.pagesOfButtonsData.get(pageNumber);
    }

    /**
     * Get slots and items inside the cache, on this page.
     *
     * @param pageNumber of the page you want to get.
     * @return map with slots for every item are placed and items.
     */
    public Map<Integer, ButtonData<T>> getMenuButtons(final int pageNumber) {
        final MenuDataUtility<T> utilityMap = this.pagesOfButtonsData.get(pageNumber);
        if (utilityMap != null) return utilityMap.getButtons();
        return new HashMap<>();
    }

    /**
     * Get itemstack for current page and slot.
     *
     * @param pageNumber with page you want to get.
     * @param slotIndex  the slot you want to get both the object and/or the itemstack stored in cache.
     * @return ButtonData with the set data or new instance with no set data if could not find the button data.
     */
    @Nonnull
    public ButtonData<T> getAddedButton(final int pageNumber, final int slotIndex) {
        final Map<Integer, ButtonData<T>> data = getMenuButtons(pageNumber);
        if (data != null) {
            final ButtonData<T> buttonData = data.get(slotIndex);
            if (buttonData != null) return buttonData;
        }
        return new ButtonData<>(null, null, null);
    }

    /**
     * Get slot this menu button is added to, if you want get all slots this button is set to
     * use {@link #getButtonSlots(int, MenuDataUtility, MenuButton)} (MenuButton)}.
     * Because this only return first match.
     *
     * @param page
     * @param menuButton to get first slot connected to this button.
     * @return slot number or -1 if not fund data or if cache is null.
     */
    public int getButtonSlot(int page, final MenuButton menuButton) {
        final Map<Integer, ButtonData<T>> data = this.getMenuButtons(page);

        if (data == null) return -1;
        for (final Entry<Integer, ButtonData<T>> entry : data.entrySet()) {
            if (entry.getValue().getMenuButton().getId() == menuButton.getId())
                return entry.getKey() - (page * this.getInventorySize());
        }
        return -1;
    }

    /**
     * Get all slots this menu button is added to.
     *
     * @param page
     * @param menuDataUtility the cached button.
     * @param menuButton      to get slots connected to this button.
     * @return set of slot number or empty if not find data or if cache is null.
     */
    @Nonnull
    public Set<Integer> getButtonSlots(int page, final MenuDataUtility<T> menuDataUtility, final MenuButton menuButton) {
        final Set<Integer> slots = new HashSet<>();
        if (menuDataUtility == null) return slots;
        final int menuButtonId = menuButton.getId();
        final int inventorySize = this.getInventorySize();

        for (final Entry<Integer, ButtonData<T>> entry : menuDataUtility.getButtons().entrySet()) {
            ButtonData<T> buttonData = entry.getValue();
            if(buttonData == null) continue;

            final MenuButton cacheMenuButton = buttonData.getMenuButton();
            final MenuButton fillMenuButton = buttonData.isFillButton() ? menuDataUtility.getFillMenuButton(menuButton) : null;

            if (fillMenuButton != null && fillMenuButton.getId() == menuButtonId) {
                slots.add(entry.getKey() - (page * inventorySize));
            } else {
                if (cacheMenuButton != null && menuButtonId == cacheMenuButton.getId()) {
                    slots.add(entry.getKey() - (page * inventorySize));
                }
            }
        }
        return slots;
    }

    /**
     * Get all buttons some shall update when menu is open.
     *
     * @return list of buttons some shall be updated when inventory is open.
     */
    public List<MenuButton> getButtonsToUpdate() {
        return buttonsToUpdate;
    }

    /**
     * get player that have open the menu.
     *
     * @return player.
     */
    public Player getViewer() {
        return this.player;
    }

    /**
     * Get if several players to look inside the current inventory. If it's zero
     * then is only one player currently looking inside the inventory.
     *
     * @return amount of players currently looking in the inventory.
     */
    public int getAmountOfViewers() {
        return (int) (this.getMenu() == null ? -1 : this.getMenu().getViewers().stream().filter(entity -> entity instanceof Player).count() - 1);
    }

    /**
     * Get the menu
     *
     * @return menu some are curent created.
     */
    @Nullable
    public Inventory getMenu() {
        return inventory;
    }


    public MenuRenderer<T> getMenuRenderer() {
        return menuRenderer;
    }

    /**
     * Set the number of pages manually. Note that it is important to ensure that the number of
     * pages is appropriate for the size of the items you want too display, setting the wrong
     * number of pages can result in an insufficient or unnecessary amount of pages being displayed.
     * <p>
     * Please be sure to double-check your calculations when setting the number of pages.
     * <p>
     *
     * @param amountOfPages the number of pages to set manually
     */
    public void setManuallyAmountOfPages(final int amountOfPages) {
        this.manuallySetPages = amountOfPages;
    }

    /**
     * get current page.
     *
     * @return curent page you has open.
     */

    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get amount of pages some are needed.
     *
     * @return 1 or amount it need to fit all items.
     */

    public int getRequiredPages() {
        return this.menuRenderer.getRequiredPages();
    }

    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    /**
     * Retrieve the amount of pages forced to be added
     *
     * @return the amount of pages no mater amount of items added.
     */
    public int getManuallySetPages() {
        return manuallySetPages;
    }

    /**
     * Get if this option is on or off.
     *
     * @return true if you want the current page added automatic.
     */
    public boolean isAutoTitleCurrentPage() {
        return autoTitleCurrentPage;
    }

    /**
     * Get metadataKey some are set on player.
     *
     * @return key you has used.
     */

    public String getPlayerMetadataKey() {
        return playerMetadataKey;
    }


    /**
     * Get a array of slots some are used as fillslots.
     *
     * @return list of slots it will fill with items.
     */
    @Nonnull
    public List<Integer> getFillSpace() {
        return fillSpace != null ? fillSpace : new ArrayList<>();
    }

    /**
     * Get list of fill items you added to menu.
     *
     * @return objects you have added to the menu.
     */
    @Nullable
    public List<T> getListOfFillItems() {
        return null;
    }

    /**
     * Get inventory size.
     *
     * @return inventory size.
     */

    public int getInventorySize() {
        return inventorySize;
    }

    /**
     * If this is set to true, it will not check whether the unique key is set and
     * there is a risk of overriding an old menu that some players still have open.
     * <p>
     * This is likely to happen only if you set the location and the player opens
     * another menu that uses the same location.
     *
     * @return true if the check should be ignored.
     */
    public boolean isIgnoreValidCheck() {
        return ignoreValidCheck;
    }

    /**
     * Get if it shall automatic clear cache or not.
     *
     * @return true if it shall clear menu after last viewer close gui.
     */

    public boolean isAutoClearCache() {
        return autoClearCache;
    }

    /**
     * When you close the menu
     *
     * @param event close inventory
     * @param menu  class some are now closed.
     */

    public void menuClose(final InventoryCloseEvent event, final MenuUtility<?> menu) {
        //optional you can override this if you plan to do something when menu is closing.
    }

    /**
     * This event only triggers when player clicking outside of the inventory.
     *
     * @param event the event for clicking outside of the inventory
     * @param menu  class some are active when clicking.
     */
    public void menuClickOutside(final InventoryClickEvent event, final MenuUtility<?> menu) {
        //optional you can override this if you plan to do something when clicking outside of the menu.
    }

    /**
     * Calculates the corresponding slot index based on the current page number and inventory size.
     * <p>
     * The method returns the slot index that should be used to retrieve the button from the cache.
     * This calculation takes into account the page number and the inventory size to ensure that the
     * correct slot is returned for the given slot parameter.
     *
     * @param slot the slot index, from 0 to 53, to calculate.
     * @return the calculated slot index based on the current page number and inventory size.
     */
    public int getSlot(final int slot) {
        return (this.getPageNumber() * this.getInventorySize()) + slot;
    }

    /**
     * Get the menu title
     *
     * @return The title.
     */
    public Object getTitle() {
        Object title = null;
        final int page = getPageNumber();

        if (this.titleFunction != null) {
            title = this.titleFunction.apply();
        }
        if (this.titleFunctionJson != null) {
            title = this.titleFunctionJson.apply();
        }

        if (title == null || title.equals("")) {
            this.titleFunction = () -> "Menu" + (getRequiredPages() > 1 ? " page: " + (page + 1) : "");
            title = this.titleFunction.apply();
        }
        if (this.titleFunctionJson == null)
            title = title + (getRequiredPages() > 1 && this.isAutoTitleCurrentPage() ? "page: " + (page + 1) : "");
        return title;
    }

    /**
     * Retrieves an instance of CheckItemsInsideMenu, which allows you to process the items inside the menu's
     * inventory. You can use this instance, for example, to collect and save the items added by the player
     * to the menu to a cache or for other specific purposes.
     *
     * @return A CheckItemsInsideMenu instance for collect the menu's inventory items.
     */
    @Nonnull
    public CheckItemsInsideMenu getCheckItemsInsideMenu() {
        return this.getCheckItemsInsideMenuByStrings(null);
    }

    /**
     * Retrieves an instance of CheckItemsInsideMenu, which allows you to process the items inside the menu's
     * inventory. You can use this instance, for example, to collect and save the items added by the player
     * to the menu to a cache or for other specific purposes.
     *
     * @param blackListedMaterials list of blacklisted materials as strings.
     * @return A CheckItemsInsideMenu instance for collect the menu's inventory items.
     */
    @Nonnull
    public CheckItemsInsideMenu getCheckItemsInsideMenuByStrings(@Nullable List<String> blackListedMaterials) {
        if (blackListedMaterials != null) {
            List<ItemStack> materials = new ArrayList<>();
            for (String item : blackListedMaterials) {
                Material material = convertMaterialFromString(menuAPI, item);
                materials.add(new ItemStack(material));
            }
            this.checkItemsInsideMenu.setBlacklistedItems(materials);
        }
        return this.checkItemsInsideMenu;
    }

    /**
     * Retrieves an instance of CheckItemsInsideMenu, which allows you to process the items inside the menu's
     * inventory. You can use this instance, for example, to collect and save the items added by the player
     * to the menu to a cache or for other specific purposes.
     *
     * @param blackListedMaterials list of blacklisted ItemStacks.
     * @return A CheckItemsInsideMenu instance for collect the menu's inventory items.
     */
    @Nonnull
    public CheckItemsInsideMenu getCheckItemsInsideMenuByItems(@Nullable List<ItemStack> blackListedMaterials) {
        if (blackListedMaterials != null) {
            this.checkItemsInsideMenu.setBlacklistedItems(blackListedMaterials);
        }
        return this.checkItemsInsideMenu;
    }


    /**
     * Provides access to the internal checks for player interactions
     * with the menu, such as clicking or dragging items.
     *
     * @return the MenuInteractionChecks instance for handling menu interactions.
     */
    public MenuInteractionChecks<T> getMenuInteractionChecks() {
        return menuInteractionChecks;
    }

    public int getHighestFillSlot() {
        return this.highestFillSlot;
    }

    public void cancelAnimateTitle() {
        Function<?> task = getAnimateTitle();
        if (task == null) return;
        this.animateTitle = null;
        this.animateTitleJson = null;
        if (this.animateTitleTask != null && this.animateTitleTask.isRunning()) {
            this.animateTitleTask.stopTask();
        }
        updateTitle();
    }

    public Location getLocation() {
        return this.getLoadInventoryHandler().getLocation();
    }

    /**
     * Get the amount of fill slots for each menu page.
     *
     * @return the number of items could be added on each page.
     */
    public int getNumberOfFillItems() {
        return this.menuRenderer.getNumberOfFillItems();
    }

    public boolean shallCacheItems() {
        return shallCacheItems;
    }

    public LoadInventoryHandler<T> getLoadInventoryHandler() {
        return loadInventoryHandler;
    }

    //========================================================
    // Do not try use methods below if you don't know what you are doing.


    /**
     * Put data to menu cache.
     *
     * @param pageNumber      the page number to set in cache
     * @param menuDataUtility the map with slot and menu data where both button,item and code execution is set.
     */
    public void putAddedButtonsCache(final Integer pageNumber, final MenuDataUtility<T> menuDataUtility) {
        this.pagesOfButtonsData.put(pageNumber, menuDataUtility);
    }

    public Function<?> getAnimateTitle() {
        if (this.animateTitle != null)
            return this.animateTitle;
        if (this.animateTitleJson != null)
            return this.animateTitleJson;
        return null;
    }

    public void updateTitle(Object text) {
        updateTitle(this.player, text);
    }

    public void updateTitle(@Nullable final Player player, final Object text) {
        if (player == null)
            return;
        if (text instanceof String)
            UpdateTitle.update(player, (String) text, useColorConversion);
        if (text instanceof JsonObject)
            UpdateTitle.update(player, (JsonObject) text);
    }

    /**
     * Remove the cached menu. if you use location.
     */
    public void removeMenuCache() {
        this.getLoadInventoryHandler().removeMenuCache();
    }

    /**
     * Set a unique key for the cached menu if you want to have multiple menus on the same location,
     * allowing many players to interact with different menus without limiting them to one menu or
     * risking the override of an old menu when a player opens a new one.
     * <p>
     * You need to use this method in your constructor,so your key gets
     * added it to the cache before player open the new menu.
     *
     * @param uniqueKey will used as part of the key in the cache.
     */
    public void setUniqueKeyMenuCache(final String uniqueKey) {
        this.getLoadInventoryHandler().setUniqueKey(uniqueKey);
    }

    /**
     * This gets triggered when a player clicks on a button inside the inventory.
     *
     * @param menuButton  the MenuButton instance that the player is currently clicking on.
     * @param player      the player who performs the action.
     * @param clickedPos  the actual slot the player is clicking on, excluding the page calculation. You can look at {@link #getSlot(int)}.
     * @param clickType   the type of click the player is performing, such as right-click, left-click, or shift-click.
     * @param clickedItem the item clicked on.
     */
    public void onClick(MenuButton menuButton, Player player, int clickedPos, ClickType clickType, ItemStack clickedItem) {
        if (this.getMenu() != null)
            menuButton.onClickInsideMenu(player, this.getMenu(), clickType, clickedItem);
    }

    /**
     * Check if the close or open Inventory is a valid menu.
     *
     * @param topInventory the inventory player open or close.
     * @param action       the action player does, such as open or close.
     * @return true if valid menu, in other cases this will return false.
     */
    public boolean checkValidMenu(@Nonnull final Inventory topInventory, @Nonnull final Action action) {
        if (action == Action.OPEN)
            return true;
        return topInventory.equals(getMenu());
    }

    /**
     * Update the title while player has the menu open. It will just update the set title
     * rom this method {@link org.broken.arrow.menu.library.holder.HolderUtility#setTitle(Function)}.
     */
    public void updateTitle() {
        this.updateTitle(this.player);
    }

    /**
     * Update the title while player has the menu open. It will just update the set title
     * from this method {@link org.broken.arrow.menu.library.holder.HolderUtility#setTitle(Function)}.
     *
     * @param player the player you want to update the title for.
     */

    public void updateTitle(@Nullable final Player player) {
        Object title = getTitle();
        if (!menuAPI.isNotFoundUpdateTitleClazz())
            this.updateTitle(player, title);
    }

    /**
     * Retrieves the {@link ItemStack} from the provided {@link MenuButton}, based on the update flag.
     * <p>
     * If {@code updateButton} is {@code true}, the method attempts to fetch the item stack directly from
     * the {@code menuButton}, first using the default getter, then using the slot-based getter as fallback.
     * If {@code updateButton} is {@code false} or {@code menuButton} is {@code null}, this returns {@code null}.
     * </p>
     *
     * @param menuButton       the button to retrieve the item stack from.
     * @param cachedButtonData the cached button data for this slot.
     * @param slot             the current inventory slot.
     * @param updateButton     whether the button should be updated and its item retrieved.
     * @return the {@link ItemStack} to display, or {@code null} if not updating or no button is available.
     */
    @Nullable
    public ItemStack getMenuItem(final MenuButton menuButton, final ButtonData<T> cachedButtonData, final int slot, final boolean updateButton) {
        if (menuButton == null) return null;

        if (updateButton) {
            ItemStack itemStack = menuButton.getItem();
            if (itemStack != null) return itemStack;
            itemStack = menuButton.getItem(this.getSlot(slot));
            return itemStack;
        }
        return null;
    }

    /**
     * Sets a menu button in the inventory if one is found at the given slot or using the fill slot index.
     * <p>
     * This method retrieves the {@link MenuButton} for the specified slot and attempts to resolve its
     * {@link ItemStack} either directly or from the {@code fillSlotIndex}. If a valid button is found,
     * it is added to the update list (if marked for updates) and stored in the provided
     * {@link MenuDataUtility} cache.
     * </p>
     *
     * @param pageNumber      the current page number of the inventory.
     * @param menuDataUtility the cache that stores buttons for this page.
     * @param slot            the inventory slot currently being rendered.
     * @param fillSlotIndex   the index within {@link #fillSpace} representing the inventory slot where your fill buttons is located.
     * @param isLastFillSlot  whether this is the final slot in the fill space range.
     */
    public void setButton(final int pageNumber, final MenuDataUtility<T> menuDataUtility, final int slot, final int fillSlotIndex, final boolean isLastFillSlot) {
        final boolean isFillSlot = !this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot);
        final MenuButton menuButton = getMenuButtonAtSlot(slot, fillSlotIndex, isFillSlot);
        final ItemStack result = getItemAtSlot(menuButton, slot, fillSlotIndex, isFillSlot);

        if (menuButton != null) {
            if (menuButton.shouldUpdateButtons()) this.buttonsToUpdate.add(menuButton);

            menuDataUtility.putButton(this.getSlot(slot), menuButton, tButtonDataWrapper -> tButtonDataWrapper.setItemStack(result));

            // final ButtonData<T> buttonData = new ButtonData<>(result, menuButton, null);
            // menuDataUtility.putButton(this.getSlot(slot), buttonData, null);
        }
    }

    public String getUniqueKey() {
        return this.getLoadInventoryHandler().getUniqueKey();
    }

    protected void putTimeWhenUpdatesButtons(final MenuButton menuButton, final Long time) {
        this.getTimeWhenUpdatesButtons().put(menuButton.getId(), time);
    }

    protected void changePage(final boolean nextPage) {
        int page = this.pageNumber;

        if (nextPage) page += 1;
        else page -= 1;
        if (page < 0) {
            page = this.getRequiredPages() - 1;
        } else if (page >= this.getRequiredPages()) {
            page = 0;
        }
        if (page == -1) {
            page = 0;
        }
        this.pageNumber = page;

        this.updateButtons();
        this.updateTitle();
    }

    protected void updateButtons() {
        this.menuRenderer.setMenuItemsToPage(this.getPageNumber());

        this.redrawInventory();
        this.updateTimeButtons();
    }

    protected void updateTimeButtons() {
        boolean cancelTask = false;

        if (this.buttonAnimation != null && this.buttonAnimation.isRunning()) {
            this.buttonAnimation.stopTask();
            cancelTask = true;
        }
        if (cancelTask) {
            updateButtonsInList();
            this.getTimeWhenUpdatesButtons().clear();
        }
    }

    protected void setMetadataKey(final String setPlayerMetadataKey) {
        this.playerMetadataKey = setPlayerMetadataKey;
    }

    protected void onMenuOpenPlaySound() {
        this.onMenuOpenPlaySound(this.player);
    }

    public void onMenuOpenPlaySound(@Nullable final Player player) {
        final Sound sound = this.menuOpenSound;
        if (sound == null) return;
        if (player == null) return;

        player.playSound(player.getLocation(), sound, 1, 1);
    }

    /**
     * This method just remove all associated data to the player with the menu and also stop the animation.
     *
     * @param player The player that currently closing the menu.
     */
    protected void unregister(@Nonnull final Player player) {
        final MetadataPlayer metadataPlayer = this.menuAPI.getPlayerMeta();
        final MenuCache menuCache = this.menuAPI.getMenuCache();

        if (metadataPlayer.hasPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN)) {
            metadataPlayer.removePlayerMenuMetadata(this.player, MenuMetadataKey.MENU_OPEN);
        }
        if (metadataPlayer.hasPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN_LOCATION) &&
                this.isAutoClearCache() && this.getAmountOfViewers() < 1) {
            menuCache.removeMenuCached(metadataPlayer.getPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN_LOCATION));
        }
        closeTasks();
    }

    /**
     * This method close all running tasks, if it is set.
     * <p>
     * Note: this is only for internal use, don't try to override this.
     */
    protected final void closeTasks() {

        if (this.buttonAnimation != null)
            this.buttonAnimation.stopTask();
        if (this.animateTitleTask != null)
            this.animateTitleTask.stopTask();
    }

    protected Inventory loadInventory(@Nonnull final Player player, @Nullable final Location location, final boolean loadToCache) {
        LoadInventoryHandler<T> inventoryHandler = this.getLoadInventoryHandler();
        inventoryHandler.setLocation(location);
        return inventoryHandler.loadInventory(player, loadToCache);
    }

    /**
     * Retrieves the menu button from the specified slot, or from the fill slot if applicable.
     *
     * @param slot       the inventory slot currently being processed.
     * @param fillSlot   the index within {@link #fillSpace} that maps to list of items or objects.
     * @param isFillSlot {@code true} if this slot corresponds to a fill item, otherwise {@code false}.
     * @return the corresponding {@link MenuButton} instance, or {@code null} if none found.
     */
    protected MenuButton getMenuButtonAtSlot(final int slot, final int fillSlot, final boolean isFillSlot) {
        final MenuButton result;
        if (isFillSlot) {
            result = getFillButtonAt(fillSlot);
        } else {
            result = getButtonAt(slot);
        }
        return result;
    }

    /**
     * Retrieves the {@link ItemStack} for the given menu button in the specified slot.
     * <p>
     * This method first tries to return the default item provided by {@link MenuButton#getItem()}.
     * If that returns {@code null}, it attempts to fetch an item using the slot index from the
     * {@link MenuButton#getItem(int)} method, using the {@code fillSlot} argument.
     * <p>
     * The {@code fillSlot} corresponds to the index from the {@link #fillSpace} range.
     *
     * @param menuButton the menu button to retrieve the item from.
     * @param slot       the current inventory slot being rendered.
     * @param fillSlot   the index within {@link #fillSpace} representing the inventory slot.
     * @param isFillSlot {@code true} if this slot corresponds to a fill item, otherwise {@code false}.
     * @return the corresponding {@link ItemStack}, or {@code null} if none is found.
     */
    protected ItemStack getItemAtSlot(final MenuButton menuButton, final int slot, final int fillSlot, final boolean isFillSlot) {
        if (menuButton == null) return null;

        ItemStack result;
        result = menuButton.getItem();
        if (result == null) result = menuButton.getItem(fillSlot);

        return result;
    }

    protected void redrawInventory() {
        this.inventory = this.inventoryRender.redraw();
    }

    protected void updateButtonsInList() {
        if (this.buttonAnimation == null || !this.buttonAnimation.isRunning()) {
            this.buttonAnimation = new ButtonAnimation<>(this);
            this.buttonAnimation.runTask(this.animateButtonTime);
        }
    }

    protected void runAnimateTitle() {
        Function<?> task = getAnimateTitle();
        if (task == null) return;
        if (menuAPI.isNotFoundUpdateTitleClazz()) return;

        if (this.animateTitleTask == null || !this.animateTitleTask.isRunning()) {
            this.animateTitleTask = new AnimateTitleTask<T>(this, this.player);
            this.animateTitleTask.runTask(20L + this.animateTitleTime);
        }

    }

    public Plugin getPlugin() {
        return menuAPI.getPlugin();
    }


}
