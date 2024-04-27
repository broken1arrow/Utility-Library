package org.broken.arrow.menu.library;

import com.google.common.base.Enums;
import com.google.gson.JsonObject;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.broken.arrow.menu.library.cache.MenuCacheKey;
import org.broken.arrow.menu.library.holder.MenuHolder;
import org.broken.arrow.menu.library.holder.MenuHolderPage;
import org.broken.arrow.menu.library.utility.FillItems;
import org.broken.arrow.menu.library.utility.Function;
import org.broken.arrow.menu.library.utility.MenuInteractionChecks;
import org.broken.arrow.menu.library.utility.ServerVersion;
import org.broken.arrow.title.update.library.UpdateTitle;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import static org.broken.arrow.menu.library.utility.ItemCreator.convertString;


/**
 * Contains methods to create menu as you want it. Recommend you extends {@link MenuHolder} or {@link MenuHolderPage} to get all methods needed.
 */

public class MenuUtility<T> {
    private final Logging logger = new Logging(MenuUtility.class);

    protected MenuCacheKey menuCacheKey;
    private final MenuCache menuCache;
    private final CheckItemsInsideMenu checkItemsInsideMenu;
    private final List<MenuButton> buttonsToUpdate = new ArrayList<>();
    private final Map<Integer, MenuDataUtility<T>> pagesOfButtonsData = new HashMap<>();
    private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();
    private MenuInteractionChecks<T> menuInteractionChecks;

    protected List<Integer> fillSpace;
    private FillItems<T> listOfFillItems;
    protected Location location;
    protected RegisterMenuAPI menuAPI;
    private Inventory inventory;
    protected InventoryType inventoryType;
    protected Player player;
    protected Sound menuOpenSound;
    protected Function<String> titleFunction;
    protected Function<JsonObject> titleFunctionJson;
    protected Function<String> animateTitle;
    protected Function<JsonObject> animateTitleJson;
    private String playerMetadataKey;
    private String uniqueKey;

    protected boolean shallCacheItems;
    protected boolean slotsYouCanAddItems;
    protected boolean allowShiftClick;
    protected boolean ignoreValidCheck;
    protected boolean autoClearCache;
    protected boolean ignoreItemCheck;
    protected boolean autoTitleCurrentPage;
    protected boolean useColorConversion;

    protected int taskid;
    protected int animateButtonTime = 20;

    protected int slotIndex;
    private int numberOfFillItems;
    private int requiredPages;
    private int manuallySetPages = -1;
    protected int inventorySize;
    protected int itemsPerPage = this.inventorySize - 9;
    protected int pageNumber;
    protected int updateTime;

    protected int animateTitleTime = 5;
    private int taskIdAnimateTitle;
    protected int highestFillSlot;

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param fillItems       List of items you want parse inside gui.
     * @param shallCacheItems if it shall cache items and slots in this class, other case override {@link #retrieveMenuButtons(int, Map)} to cache it own class.
     * @deprecated not in use any more.
     */
    @Deprecated
    public MenuUtility(@Nullable final List<Integer> fillSlots, @Nullable final List<T> fillItems, final boolean shallCacheItems) {
        this(RegisterMenuAPI.getMenuAPI(), fillSlots, shallCacheItems);
    }

    /**
     * Create menu instance.
     *
     * @param fillSlots       Witch slots you want fill with items.
     * @param shallCacheItems if it shall cache items and slots in this class, other case override {@link #retrieveMenuButtons(int, Map)} to cache it own class.
     */
    public MenuUtility(@Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        this(RegisterMenuAPI.getMenuAPI(), fillSlots, shallCacheItems);
    }

    /**
     * Creates a menu instance.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin. Use this constructor
     *                        only if you are using the plugin and have not shaded it.
     * @param fillSlots       The slots you want to fill with items. Can be null if not filling specific slots.
     * @param shallCacheItems Indicates whether items and slots should be cached in this class. If false,
     *                        override {@link #retrieveMenuButtons(int, Map)} to cache it own class.
     */
    public MenuUtility(@Nonnull final RegisterMenuAPI menuAPI, @Nullable final List<Integer> fillSlots, final boolean shallCacheItems) {
        this.fillSpace = fillSlots;
        if (fillSlots != null)
            this.highestFillSlot = fillSlots.stream().mapToInt(Integer::intValue).max().orElse(-1);

        this.menuInteractionChecks = new MenuInteractionChecks<>(this);
        this.shallCacheItems = shallCacheItems;
        this.allowShiftClick = true;
        this.autoClearCache = true;
        this.ignoreItemCheck = false;
        this.autoTitleCurrentPage = true;
        this.slotIndex = 0;
        this.updateTime = -1;
        this.menuOpenSound = Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull() == null ? Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BASEDRUM").orNull() : Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull();
        this.uniqueKey = "";
        this.menuAPI = RegisterMenuAPI.getMenuAPI();
        this.menuCache = this.menuAPI.getMenuCache();
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
     * Register your fill buttons.
     *
     * @param object will return object you have added as fillitems.
     * @return MenuButton you have set.
     * @deprecated plan to remove this method and only use {@link #getButtonAt(int)} however
     * when you use {@link MenuHolderPage} you don't use this two methods at all.
     */
    @Deprecated
    @Nullable
    public MenuButton getFillButtonAt(@Nonnull final T object) {
        return null;
    }

    /**
     * Register your fill buttons, this method will return number from 0 to
     * amount you want inside the inventory.
     *
     * @param slot will return current number till will add item.
     * @return MenuButton you have set.
     */
    @Nullable
    public MenuButton getFillButtonAt(final int slot) {
        return null;
    }

    /**
     * Override this method if you want to cache the menu buttons in own class.
     *
     * @param pageNumber the page number
     * @param buttons the menu buttons with the attached slots.
     */
    public void retrieveMenuButtons(int pageNumber, Map<Integer, ButtonData<T>> buttons) {
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
    protected Long getTimeWhenUpdatesButton(final MenuButton menuButton) {
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
     * @return seconds between the updates, defult it will return -1 and don't update the buttons.
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
     * @return ButtonData with the set data or new instance with no set data.
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
     * use {@link #getButtonSlots(MenuDataUtility, MenuButton)} (MenuButton)}.
     * Because this only return first match.
     *
     * @param menuButton to get slots connected to this button.
     * @return slot number or -1 if not find data or if cache is null.
     */
    public int getButtonSlot(final MenuButton menuButton) {
        final Map<Integer, ButtonData<T>> data = this.getMenuButtons(this.getPageNumber());
        if (data == null) return -1;
        for (final Entry<Integer, ButtonData<T>> entry : data.entrySet()) {
            if (entry.getValue().getMenuButton().getId() == menuButton.getId())
                return entry.getKey() - (this.getPageNumber() * this.getInventorySize());
        }
        return -1;
    }

    /**
     * Get all slots this menu button is added to.
     *
     * @param menuButton      to get slots connected to this button.
     * @param menuDataUtility the cached button.
     * @return set of slot number or empty if not find data or if cache is null.
     */
    @Nonnull
    public Set<Integer> getButtonSlots(final MenuDataUtility<T> menuDataUtility, final MenuButton menuButton) {
        final Set<Integer> slots = new HashSet<>();
        if (menuDataUtility == null) return slots;
        final int menuButtonId = menuButton.getId();

        for (final Entry<Integer, ButtonData<T>> entry : menuDataUtility.getButtons().entrySet()) {
            final MenuButton cacheMenuButton = entry.getValue().getMenuButton();
            final MenuButton fillMenuButton = menuDataUtility.getFillMenuButton(menuButton);
            if (cacheMenuButton == null) {
                if (fillMenuButton != null && fillMenuButton.getId() == menuButtonId) {
                    slots.add(entry.getKey() - (this.getPageNumber() * this.getInventorySize()));
                }
            } else {
                if (menuButtonId == cacheMenuButton.getId()) {
                    slots.add(entry.getKey() - (this.getPageNumber() * this.getInventorySize()));
                }
            }
        }
        return slots;
    }

    /**
     * All buttons inside the menu.
     *
     * @return list of buttons some currently are register.
     */
    @Deprecated
    public List<MenuButton> getButtons() {
        return new ArrayList<>();
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
        return requiredPages;
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
     * If you want to cache the items in own class.
     *
     * @return map with slot number (can be more than one inventory in number of buttons) and itemstack.
     */
    @Nonnull
    public Map<Integer, ButtonData<T>> getMenuButtonsCache() {
        return null;
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
     * Get the Object/entity from the @link {@link #listOfFillItems}.
     *
     * @param clickedPos the current pos player clicking on, you need also add the page player currently have open and inventory size.
     * @return Object/entity from the listOfFillItems list.
     * @deprecated not in use, use MenuHolderPage class instead.
     */
    @Deprecated
    public T getObjectFromList(final int clickedPos) {
        return getAddedButton(this.getPageNumber(), clickedPos).getObject();
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
        if (this.titleFunction != null) {
            title = this.titleFunction.apply();
        }
        if (this.titleFunctionJson != null) {
            title = this.titleFunctionJson.apply();
        }
        if (title == null || title.equals("")) {
            this.titleFunction = () -> "Menu" + (getRequiredPages() > 1 ? " page: " + (getPageNumber() + 1) : "");
            title = this.titleFunction.apply();
        }
        if (this.titleFunctionJson == null)
            title = title + (getRequiredPages() > 1 && this.isAutoTitleCurrentPage() ? "page: " + (getPageNumber() + 1) : "");
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
        return this.getCheckItemsInsideMenu(null);
    }

    /**
     * Retrieves an instance of CheckItemsInsideMenu, which allows you to process the items inside the menu's
     * inventory. You can use this instance, for example, to collect and save the items added by the player
     * to the menu to a cache or for other specific purposes.
     *
     * @param blackListedMaterials list of blacklisted materials.
     * @return A CheckItemsInsideMenu instance for collect the menu's inventory items.
     */
    @Nonnull
    public CheckItemsInsideMenu getCheckItemsInsideMenu(@Nullable List<String> blackListedMaterials) {
        if (blackListedMaterials != null) {
            List<Material> materials = new ArrayList<>();
            for (String item : blackListedMaterials) {
                Material material = convertString(menuAPI, item);
                materials.add(material);
            }
            this.checkItemsInsideMenu.setBlacklistedItemsNew(materials);
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


    //========================================================

    /**
     * Do not try use methods below.
     */

    /**
     * Put data to menu cache.
     *
     * @param pageNumber      the page number to set in cache
     * @param menuDataUtility the map with slot and menu data where both button,item and code execution is set.
     */
    protected void putAddedButtonsCache(final Integer pageNumber, final MenuDataUtility<T> menuDataUtility) {
        this.pagesOfButtonsData.put(pageNumber, menuDataUtility);
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
        this.updateTittle();
    }

    protected void updateButtons() {
        this.slotIndex = this.getPageNumber() * numberOfFillItems;
        putMenuItemsToCache(this.getPageNumber());
        this.slotIndex = 0;
        redrawInventory();
        updateTimeButtons();
    }

    protected void updateTimeButtons() {
        boolean cancelTask = false;
        if (this.taskid > 0 && Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid)) {
            Bukkit.getScheduler().cancelTask(this.taskid);
            cancelTask = true;
        }
        if (cancelTask) {
            updateButtonsInList();
            this.getTimeWhenUpdatesButtons().clear();
        }
    }

    protected void updateTittle() {
        Object title = getTitle();
        if (!menuAPI.isNotFoundUpdateTitleClazz())
            this.updateTitle(title);
    }

    /**
     * Get the amount of fill slots for each menu page.
     *
     * @return the number of items could be added on each page.
     */
    public int getNumberOfFillItems() {
        return numberOfFillItems;
    }

    private void saveMenuCache(@Nonnull final Location location) {
        menuCache.addToCache(location, this.uniqueKey, this);
    }

    private MenuUtility<?> getMenuCache() {
        return menuCache.getMenuInCache(this.menuCacheKey, this.getClass());
    }

    /**
     * Remove the cached menu. if you use location.
     */
    public void removeMenuCache() {
        menuCache.removeMenuCached(this.menuCacheKey);
    }

    /**
     * Set a unique key for the cached menu if you want to have multiple menus on the same location,
     * allowing many players to interact with different menus without limiting them to one menu or
     * risking the override of an old menu when a player opens a new one. You need to use this method
     * in your constructor,so your key gets added it to the cache.
     *
     * @param uniqueKey will used as part of the key in the cache.
     */
    public void setUniqueKeyMenuCache(final String uniqueKey) {
        this.uniqueKey = uniqueKey;
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
        //final T object = this.getObjectFromList(this.getSlot(clickedPos));
        if (this.getMenu() != null)
            menuButton.onClickInsideMenu(player, this.getMenu(), clickType, clickedItem);
    }

/*	private boolean checkLastOpenMenu() {
		if (getPreviousMenuholder(this.player) != null) {
			if (hasPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS))
				removePlayerMenuMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
			return false;
		}
		return true;
	}*/

    protected void setLocationMetaOnPlayer(final Player player, final Location location) {
        String key = this.uniqueKey;
        if (key != null && key.isEmpty()) {
            this.uniqueKey = this.getClass().getName();
            key = this.uniqueKey;
        }
        menuCacheKey = this.menuCache.getMenuCacheKey(location, key);
        if (menuCacheKey == null) menuCacheKey = new MenuCacheKey(location, key);
        menuAPI.getPlayerMeta().setPlayerLocationMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, menuCacheKey);
    }

    protected void setMetadataKey(final String setPlayerMetadataKey) {
        this.playerMetadataKey = setPlayerMetadataKey;
    }

    protected void onMenuOpenPlaySound() {
        final Sound sound = this.menuOpenSound;
        if (sound == null) return;

        this.player.playSound(player.getLocation(), sound, 1, 1);
    }

    /**
     * This method close all running tasks, if it is set.
     * <p>
     * Note: this is only for internal use, don't try to override this.
     */
    protected final void closeTasks() {
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid)) {
            Bukkit.getScheduler().cancelTask(this.taskid);
        }
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskIdAnimateTitle) || Bukkit.getScheduler().isQueued(this.taskIdAnimateTitle)) {
            Bukkit.getScheduler().cancelTask(this.taskIdAnimateTitle);
        }
    }

    protected Inventory loadInventory(final Player player, final boolean loadToCahe) {
        Inventory menu = null;
        if (loadToCahe && this.location != null) {
            this.setLocationMetaOnPlayer(player, this.location);
            MenuUtility<?> menuCached = this.getMenuCache();

            if (menuCached == null || menuCached.getMenu() == null) {
                saveMenuCache(this.location);
                menuCached = this.getMenuCache();
            }
            if (!this.isIgnoreValidCheck()) {
                Validate.checkBoolean(!menuCached.getClass().equals(this.getClass()) && (this.uniqueKey == null || this.uniqueKey.isEmpty()), "You need set uniqueKey for this menu " + menuCached.getClass() + " or it will replace the old menu and players left can take items, set method setIgnoreValidCheck() to ignore this or set the uniqueKey");
            } else {
                saveMenuCache(this.location);
                menuCached = this.getMenuCache();
            }
            menu = menuCached.getMenu();
        } else {
            menuAPI.getPlayerMeta().setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, this);
            menuAPI.getPlayerMeta().setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, this);
            final MenuUtility<?> menuUtility = menuAPI.getPlayerMeta().getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
            if (menuUtility != null) menu = menuUtility.getMenu();
        }
        return menu;
    }

    protected double amountOfPages() {
        final List<Integer> fillSlots = this.getFillSpace();
        if (this.itemsPerPage > 0) {
            if (this.itemsPerPage > this.inventorySize)
                this.logger.log(Level.WARNING, () -> Logging.of("Items per page are bigger an Inventory size, items items per page " + this.itemsPerPage + ". Inventory size " + this.inventorySize));
            if (!fillSlots.isEmpty()) {
                return (double) fillSlots.size() / this.itemsPerPage;
            }
        }
        return (double) (fillSlots.isEmpty() ? this.inventorySize - 9 : fillSlots.size()) / this.itemsPerPage;
    }

    protected void setMenuItemsToAllPages() {
        this.requiredPages = Math.max((int) Math.ceil(amountOfPages()), 1);
        if (this.manuallySetPages > 0) this.requiredPages = this.manuallySetPages;

        for (int i = 0; i < this.requiredPages; i++) {
            putMenuItemsToCache(i);
            if (i == 0) numberOfFillItems = this.slotIndex;
        }
        this.slotIndex = 0;
    }

    protected void putMenuItemsToCache(final int pageNumber) {
        final MenuDataUtility<T> menuDataUtility = cacheMenuData(pageNumber);
        if (!this.shallCacheItems) {
            this.putAddedButtonsCache(pageNumber, menuDataUtility);
        }
        retrieveMenuButtons(pageNumber ,menuDataUtility.getButtons());
    }

    @Nonnull
    private MenuDataUtility<T> cacheMenuData(final int pageNumber) {
        final MenuDataUtility<T> menuDataUtility = new MenuDataUtility<>();
        int fillSlot = this.getFillSpace().stream().mapToInt(Integer::intValue).max().orElse(-1);
        for (int slot = 0; slot < this.inventorySize; slot++) {

            boolean isFillButton = !this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot);

            this.setButton(pageNumber, menuDataUtility, slot, this.slotIndex, slot > fillSlot);

            if (isFillButton) {
                this.slotIndex++;
                // fillSlot = this.slotIndex;
            }
        }
        return menuDataUtility;
    }

    protected void setButton(final int pageNumber, final MenuDataUtility<T> menuDataUtility, final int slot, final int fillSlotIndex, final boolean isLastFillSlot) {
        final MenuButton menuButton = getMenuButtonAtSlot(slot, fillSlotIndex);
        final ItemStack result = getItemAtSlot(menuButton, slot, fillSlotIndex);

        if (menuButton != null) {
            if (menuButton.shouldUpdateButtons()) this.buttonsToUpdate.add(menuButton);
            final ButtonData<T> buttonData = new ButtonData<>(result, menuButton, null);

            menuDataUtility.putButton(this.getSlot(slot), buttonData, null);
        }
    }

    protected MenuButton getMenuButtonAtSlot(final int slot, final int fillSlot) {
        final MenuButton result;
        if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
            result = getFillButtonAt(fillSlot);
        } else {
            result = getButtonAt(slot);
        }
        return result;
    }

    protected ItemStack getItemAtSlot(final MenuButton menuButton, final int slot, final int fillSlot) {
        if (menuButton == null) return null;

        ItemStack result;
        result = menuButton.getItem();
        if (result == null) result = menuButton.getItem(fillSlot);

        return result;
    }

    private T getObjectFromListOfFillItems(final int slotIndex) {
        final List<T> fillItems = this.getListOfFillItems();
        if (fillItems != null && fillItems.size() > slotIndex) return fillItems.get(slotIndex);
        else return null;
    }

    protected void redrawInventory() {
        if (this.getMenu() == null || this.inventorySize > this.getMenu().getSize()) this.inventory = createInventory();

        final int fillSlots = !getFillSpace().isEmpty() ? getFillSpace().size() : this.getMenu().getSize();

        for (int i = getFillSpace().stream().findFirst().orElse(0); i < fillSlots; i++) {
            this.getMenu().setItem(i, new ItemStack(Material.AIR));
        }

        final Map<Integer, ButtonData<T>> entity = this.getMenuButtons(this.getPageNumber());
        if (entity != null && !entity.isEmpty()) for (int i = 0; i < this.getMenu().getSize(); i++) {
            final ButtonData<?> buttonData = entity.get(this.getPageNumber() * inventorySize + i);
            ItemStack itemStack = null;
            if (buttonData != null) {
                itemStack = buttonData.getItemStack();
            }
            this.getMenu().setItem(i, itemStack);
        }
    }

    private Inventory createInventory() {
        final Object title = this.getTitle();
        String menuTitle = " ";
        if (title instanceof String)
            menuTitle = String.valueOf(title);
        if (this.getInventoryType() != null)
            return Bukkit.createInventory(null, this.getInventoryType(), menuTitle != null ? menuTitle : "");
        if (!(this.inventorySize == 5 || this.inventorySize % 9 == 0))
            this.logger.log(Level.WARNING, () -> Logging.of("wrong inventory size , you has put in " + this.inventorySize + " it need to be valid number."));
        if (this.inventorySize == 5)
            return Bukkit.createInventory(null, InventoryType.HOPPER, menuTitle != null ? menuTitle : "");
        return Bukkit.createInventory(null, this.inventorySize % 9 == 0 ? this.inventorySize : 9, menuTitle != null ? menuTitle : "");
    }

    private long getUpdateTime(final MenuButton menuButton) {
        if (menuButton.setUpdateTime() == -1) return getUpdateTime();
        return menuButton.setUpdateTime();
    }

    protected void updateButtonsInList() {
        taskid = new RunButtonAnimation().runTask(this.animateButtonTime);
    }

    @Nullable
    private ItemStack getMenuItem(final MenuButton menuButton, final ButtonData<T> cachedButtons, final int slot) {
        return getMenuItem(menuButton, cachedButtons, slot, menuButton.shouldUpdateButtons());
    }

    @Nullable
    protected ItemStack getMenuItem(final MenuButton menuButton, final ButtonData<T> cachedButtons, final int slot, final boolean updateButton) {
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
     * Get all slots same menu button is connected too.
     *
     * @param menuDataMap the map with all slots and menu data
     * @param menuButton  the menu buttons you want to match with.
     * @return set of slots that match same menu button.
     */
    @Nonnull
    private Set<Integer> getItemSlotsMap(final MenuDataUtility<T> menuDataMap, final MenuButton menuButton) {
        final Set<Integer> slotList = new HashSet<>();
        if (menuDataMap == null) return slotList;

        for (int slot = 0; slot < this.inventorySize; slot++) {
            final ButtonData<T> addedButtons = menuDataMap.getButtons().get(this.getSlot(slot));
            if (addedButtons == null) continue;

            final MenuButton cacheMenuButton = addedButtons.getMenuButton();
            final MenuButton fillMenuButton = menuDataMap.getFillMenuButton(this.getSlot(slot));
            final int menuButtonId = menuButton.getId();
            if ((cacheMenuButton == null && fillMenuButton != null && fillMenuButton.getId() == menuButtonId) || (cacheMenuButton != null && Objects.equals(menuButtonId, cacheMenuButton.getId())))
                slotList.add(slot);
        }
        return slotList;
    }

    protected void runAnimateTitle() {
        Function<?> task = getAnimateTitle();
        if (task == null) return;
        this.taskIdAnimateTitle = new BukkitRunnable() {
            @Override
            public void run() {
                Object text = task.apply();
                if (text == null || (ServerVersion.atLeast(ServerVersion.V1_9) && this.isCancelled())) {
                    this.cancel();
                    updateTittle();
                    return;
                }
                if (!text.equals("") && !menuAPI.isNotFoundUpdateTitleClazz()) {
                    updateTitle(text);
                }
            }
        }.runTaskTimerAsynchronously(menuAPI.getPlugin(), 1, 20 + this.animateTitleTime).getTaskId();
    }

    public void cancelAnimateTitle() {
        Function<?> task = getAnimateTitle();
        if (task == null) return;
        this.animateTitle = null;
        this.animateTitleJson = null;
        if (Bukkit.getScheduler().isCurrentlyRunning(this.taskIdAnimateTitle) || Bukkit.getScheduler().isQueued(this.taskIdAnimateTitle)) {
            Bukkit.getScheduler().cancelTask(this.taskIdAnimateTitle);
        }
        updateTittle();
    }

    public Function<?> getAnimateTitle() {
        if (this.animateTitle != null)
            return this.animateTitle;
        if (this.animateTitleJson != null)
            return this.animateTitleJson;
        return null;
    }

    private void updateTitle(Object text) {
        if (text instanceof String)
            UpdateTitle.update(player, (String) text, useColorConversion);
        if (text instanceof JsonObject)
            UpdateTitle.update(player, (JsonObject) text);
    }

    private class RunButtonAnimation extends BukkitRunnable {
        private int counter = 0;

        public int runTask(long delay) {
            return runTaskTimer(menuAPI.getPlugin(), 1L, delay).getTaskId();
        }

        @Override
        public void run() {
            for (final MenuButton menuButton : getButtonsToUpdate()) {

                final Long timeLeft = getTimeWhenUpdatesButton(menuButton);
                if (timeLeft != null && timeLeft == -1) continue;

                if (timeLeft == null || timeLeft == 0)
                    putTimeWhenUpdatesButtons(menuButton, counter + getUpdateTime(menuButton));
                else if (counter >= timeLeft) {
                    getMenuData(getPageNumber());
                    final MenuDataUtility<T> menuDataUtility = getMenuData(getPageNumber());
                    if (menuDataUtility == null) {
                        cancel();
                        return;
                    }

                    final Set<Integer> itemSlots = getItemSlotsMap(menuDataUtility, menuButton);

                    if (updateButtonsData(menuButton, menuDataUtility, itemSlots)) return;
                }
            }
            counter++;
        }

        private boolean updateButtonsData(final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Set<Integer> itemSlots) {
            if (itemSlots.isEmpty())
                putTimeWhenUpdatesButtons(menuButton, counter + getUpdateTime(menuButton));
            else {
                if (getMenu() == null) return true;
                final Iterator<Integer> slotList = itemSlots.iterator();
                setButtons(menuButton, menuDataUtility, slotList);
                putTimeWhenUpdatesButtons(menuButton, counter + getUpdateTime(menuButton));
            }
            return false;
        }

        private void setButtons(final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Iterator<Integer> slotList) {
            while (slotList.hasNext()) {
                final Integer slot = slotList.next();

                final ButtonData<T> buttonData = menuDataUtility.getButton(getSlot(slot));
                if (buttonData == null) continue;

                final ItemStack menuItem = getMenuItem(menuButton, buttonData, slot);
                final ButtonData<T> newButtonData = new ButtonData<>(menuItem, buttonData.getMenuButton(), buttonData.getObject());

                menuDataUtility.putButton(getSlot(slot), newButtonData, menuDataUtility.getFillMenuButton(getSlot(slot)));

                putAddedButtonsCache(getPageNumber(), menuDataUtility);
                getMenu().setItem(slot, menuItem);
                slotList.remove();
            }
        }
    }

}
