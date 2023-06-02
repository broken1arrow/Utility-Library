package org.broken.arrow.menu.library;

import com.google.common.base.Enums;
import org.broken.arrow.menu.library.NMS.UpdateTittleContainers;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButtonI;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.broken.arrow.menu.library.cache.MenuCacheKey;
import org.broken.arrow.menu.library.utility.Function;
import org.broken.arrow.menu.library.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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

import static org.broken.arrow.menu.library.RegisterMenuAPI.getPLUGIN;
import static org.broken.arrow.menu.library.utility.Metadata.*;

/**
 * Contains methods to create menu as you want it. Recomend you extends #MenuHolder to get all methods needed.
 */

public class MenuUtility<T> {


	/**
	 * Create menu instance.
	 *
	 * @param fillSlots       Witch slots you want fill with items.
	 * @param fillItems       List of items you want parse inside gui.
	 * @param shallCacheItems if it shall cache items and slots in this class, other case use {@link #getMenuButtonsCache()} to cache it own class.
	 */
	public MenuUtility(@Nullable final List<Integer> fillSlots, @Nullable final List<T> fillItems, final boolean shallCacheItems) {
		this.fillSpace = fillSlots;
		this.listOfFillItems = fillItems;
		this.shallCacheItems = shallCacheItems;
		this.allowShiftClick = true;
		this.autoClearCache = true;
		this.ignoreItemCheck = false;
		this.autoTitleCurrentPage = true;
		this.slotIndex = 0;
		this.updateTime = -1;
		this.menuOpenSound = Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull() == null ? Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BASEDRUM").orNull() : Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull();
		this.uniqueKey = "";
	}

	protected MenuCacheKey menuCacheKey;
	private final MenuCache menuCache = MenuCache.getInstance();
	private final List<MenuButtonI<T>> buttonsToUpdate = new ArrayList<>();
	private final Map<Integer, MenuDataUtility> pagesOfButtonsData = new HashMap<>();
	private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();

	protected List<Integer> fillSpace;
	private final List<T> listOfFillItems;
	protected final Plugin plugin = getPLUGIN();
	private Inventory inventory;
	protected InventoryType inventoryType;
	protected int taskid;
	protected boolean shallCacheItems;
	protected boolean slotsYouCanAddItems;
	protected boolean allowShiftClick;
	protected boolean ignoreValidCheck;
	protected boolean autoClearCache;
	protected boolean ignoreItemCheck;
	protected boolean autoTitleCurrentPage;
	protected int slotIndex;
	private int numberOfFillitems;
	private int requiredPages;
	private int manuallySetPages = -1;
	protected int inventorySize;
	protected int itemsPerPage = this.inventorySize;
	protected int pageNumber;
	protected int updateTime;

	protected Player player;
	protected Sound menuOpenSound;
	protected String title;
	protected Function<String> function;
	private String playermetadataKey;
	private String uniqueKey;
	protected Location location;

	/**
	 * Set the item you want in a slot.
	 *
	 * @param slot will return current number till will add item.
	 * @return one itemstack;
	 */
	public ItemStack getItemAt(final int slot) {
		throw new Validate.CatchExceptions("WARN not in use");
	}

	/**
	 * Set the items you want in fill slots.
	 *
	 * @param o will return object you have added as fillitems.
	 * @return one itemstack;
	 */

	public ItemStack getFillItemsAt(final Object o) {
		throw new Validate.CatchExceptions("WARN not in use");
	}

	/**
	 * Set the items you want in fill slots.
	 *
	 * @param slot will return current number till will add item.
	 * @return one itemstack;
	 */

	public ItemStack getFillItemsAt(final int slot) {
		throw new Validate.CatchExceptions("WARN not in use");
	}

	/**
	 * Register your buttons you want inside the menu.
	 *
	 * @param slot will return slot number it will add item.
	 * @return MenuButtonI you have set.
	 */
	@Nullable
	public MenuButtonI<T> getButtonAt(final int slot) {
		return null;
	}

	/**
	 * Register your fill buttons.
	 *
	 * @param object will return object you have added as fillitems.
	 * @return MenuButtonI you have set.
	 */
	@Nullable
	public MenuButtonI<T> getFillButtonAt(@Nonnull final T object) {
		return null;
	}

	/**
	 * Register your fill buttons, this method will return number from 0 to
	 * amount you want inside the inventory.
	 *
	 * @param slot will return current number till will add item.
	 * @return MenuButtonI you have set.
	 */
	@Nullable
	public MenuButtonI<T> getFillButtonAt(final int slot) {
		return null;
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
	protected Long getTimeWhenUpdatesButton(final MenuButtonI<?> menuButton) {
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
	 * You need spcify the slots with one of this two methods {@link org.broken.arrow.menu.library.holder.MenuHolder#setFillSpace(String)} or
	 * {@link org.broken.arrow.menu.library.holder.MenuHolder#setFillSpace(java.util.List)} for get it work.
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
	 * You also need set this to true {@link MenuButtonI#shouldUpdateButtons()}
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
	public MenuDataUtility getMenuData(final int pageNumber) {
		return this.pagesOfButtonsData.get(pageNumber);
	}

	/**
	 * Get slots and items inside the cache, on this page.
	 *
	 * @param pageNumber of the page you want to get.
	 * @return map with slots for every item are placed and items.
	 */
	public Map<Integer, ButtonData<?>> getMenuButtons(final int pageNumber) {
		final MenuDataUtility utilityMap = this.pagesOfButtonsData.get(pageNumber);
		if (utilityMap != null) return utilityMap.getButtons();
		return new HashMap<>();
	}

	/**
	 * Get both object and itemstack for current page and slot.
	 * If you set @link {@link #getListOfFillItems()} in the constructor super,
	 * can you get the objects from the list too.
	 *
	 * @param pageNumber with page you want to get.
	 * @param slotIndex  the slot you want to get both the object and/or the itemstack stored in cache.
	 * @return Menudata with itemstack and/or object
	 */
	@Nonnull
	public ButtonData<T> getAddedButtons(final int pageNumber, final int slotIndex) {
		final Map<Integer, ButtonData<?>> data = getMenuButtons(pageNumber);
		if (data != null) {
			final ButtonData<T> buttonData = (ButtonData<T>) data.get(slotIndex);
			if (buttonData != null) return buttonData;
		}
		return new ButtonData<>(null, null, null);
	}

	/**
	 * Get slot this menu button is added to, if you want get fillslots
	 * will this only return first slot. Use {@link #getButtonSlots(MenuDataUtility, MenuButtonI)} (MenuButtonI)}
	 * if you want to get all slots this button are connected to.
	 *
	 * @param menuButton to get slots connectet to this button.
	 * @return slot number or -1 if not find data or if cache is null.
	 */
	public int getButtonSlot(final MenuButtonI<?> menuButton) {
		final Map<Integer, ButtonData<?>> data = this.getMenuButtons(this.getPageNumber());
		if (data == null) return -1;
		for (final Entry<Integer, ButtonData<?>> entry : data.entrySet()) {
			if (entry.getValue().getMenuButton().getId() == menuButton.getId())
				return entry.getKey() - (this.getPageNumber() * this.getInventorySize());
		}
		return -1;
	}

	/**
	 * Get all slots this menu button is added to.
	 *
	 * @param menuButton to get slots connected to this button.
	 * @return list of slot number or empty if not find data or if cache is null.
	 */
	@Nonnull
	public Set<Integer> getButtonSlots(final MenuDataUtility menuDataUtility, final MenuButtonI<?> menuButton) {
		final Set<Integer> slots = new HashSet<>();
		if (menuDataUtility == null) return slots;
		final int menuButtonId = menuButton.getId();

		for (final Entry<Integer, ButtonData<?>> entry : menuDataUtility.getButtons().entrySet()) {
			final MenuButtonI<?> chacheMenuButton = entry.getValue().getMenuButton();
			final MenuButtonI<?> fillMenuButton = menuDataUtility.getFillMenuButton(menuButton);
			if (chacheMenuButton == null) {
				if (fillMenuButton != null && fillMenuButton.getId() == menuButtonId) {
					slots.add(entry.getKey() - (this.getPageNumber() * this.getInventorySize()));
				}
			} else {
				if (menuButtonId == chacheMenuButton.getId()) {
					slots.add(entry.getKey() - (this.getPageNumber() * this.getInventorySize()));
				}
			}
		}
		return slots;
	}

	/**
	 * All buttons inside the menu.
	 *
	 * @return list of buttons some currently are registed.
	 */
	@Deprecated
	public List<MenuButtonI<T>> getButtons() {
		return null;
	}

	/**
	 * Get all buttons some shal update when menu is open.
	 *
	 * @return list of buttons some shall be updated when invetory is open.
	 */
	public List<MenuButtonI<T>> getButtonsToUpdate() {
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
	 * @return amount of players curently looking in the inventory.
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
	 * pages is appropriate for the size of the list of fill items {@link #getListOfFillItems()},
	 * setting the wrong number of pages can result in an insufficient or unnecessary amount of
	 * pages being displayed.
	 * <p>
	 * <p>
	 * <p>
	 * Please be sure to double-check your calculations when setting the number of pages.
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
	public Map<Integer, ButtonData<?>> getMenuButtonsCache() {
		return addItemsToCache();
	}


	/**
	 * Get metadataKey some are set on player.
	 *
	 * @return key you has used.
	 */

	public String getPlayermetadataKey() {
		return playermetadataKey;
	}

	/**
	 * Get menuholder instance from player metadata.
	 *
	 * @return menuholder instance.
	 */
	@Nullable
	public MenuUtility<?> getMenuholder(final Player player) {
		return getMenuholder(player, MenuMetadataKey.MENU_OPEN);
	}

	/**
	 * Get previous menuholder instance from player metadata.
	 *
	 * @return older menuholder instance.
	 */

	public MenuUtility<?> getPreviousMenuholder(final Player player) {
		return getMenuholder(player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
	}

	/**
	 * Get current menu player has stored or currently open menu.
	 *
	 * @param player      the player that open menu.
	 * @param metadataKey the menu key set for this menu.
	 * @return the menu instance or null if player currently no menu open.
	 */
	private MenuUtility<?> getMenuholder(final Player player, final MenuMetadataKey metadataKey) {

		if (hasPlayerMetadata(player, metadataKey)) return getPlayerMenuMetadata(player, metadataKey);
		return null;
	}

	/**
	 * Get the Object/entity from the @link {@link #listOfFillItems}.
	 *
	 * @param clickedPos the curent pos player clicking on, you need also add the page player currently have open and inventory size.
	 * @return Object/entity from the listOfFillItems list.
	 */
	public T getObjectFromList(final int clickedPos) {
		return getAddedButtons(this.getPageNumber(), clickedPos).getObject();
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
	 * @return items you have added.
	 */
	@Nullable
	public List<T> getListOfFillItems() {
		return listOfFillItems;
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
		return this.getPageNumber() * this.getInventorySize() + slot;
	}

	public String getTitle() {
		String title = this.title;
		if (function != null) {
			title = function.apply();
			if (title == null) title = this.title;
		}
		return title;
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
	protected void putAddedButtonsCache(final Integer pageNumber, final MenuDataUtility menuDataUtility) {
		this.pagesOfButtonsData.put(pageNumber, menuDataUtility);
	}

	protected void putTimeWhenUpdatesButtons(final MenuButtonI menuButton, final Long time) {
		this.getTimeWhenUpdatesButtons().put(menuButton.getId(), time);
	}

	protected void changePage(final boolean nextPage) {
		int pageNumber = this.pageNumber;

		if (nextPage) pageNumber += 1;
		else pageNumber -= 1;
		if (pageNumber < 0) {
			pageNumber = this.getRequiredPages() - 1;
		} else if (pageNumber >= this.getRequiredPages()) {
			pageNumber = 0;
		}
		if (pageNumber == -1) {
			pageNumber = 0;
		}
		this.pageNumber = pageNumber;

		updateButtons();
		updateTittle();
	}

	protected void updateButtons() {
		this.slotIndex = this.getPageNumber() * numberOfFillitems;
		addItemsToCache(this.getPageNumber());
		this.slotIndex = 0;
		reddrawInventory();
		updateTimeButtons();
	}

	protected void updateTimeButtons() {
		boolean cancelTask = false;
		if (this.taskid > 0)
			if (Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid)) {
				Bukkit.getScheduler().cancelTask(this.taskid);
				cancelTask = true;
			}
		if (cancelTask) {
			updateButtonsInList();
			this.getTimeWhenUpdatesButtons().clear();
		}
	}

	protected void updateTittle() {
		String title = getTitle();
		if (title == null || title.equals("")) {
			this.title = "Menu" + (getRequiredPages() > 1 ? " page: " : "");
			title = getTitle();
		}
		UpdateTittleContainers.update(player, title + (getRequiredPages() > 1 && this.isAutoTitleCurrentPage() ? " " + (getPageNumber() + 1) + "" : ""));
	}

	private Object toMenuCache(final Player player, final Location location) {
		Object obj = null;
		if (player != null && location != null) {
			obj = location;
		}
		if (player != null && location == null) {
			obj = player;
		}
		return obj;
	}

	private void saveMenuCache(@Nonnull final Location location) {
		menuCache.addToCache(location, this.uniqueKey, this);
	}

	private MenuUtility<?> getMenuCache() {
		return menuCache.getMenuInCache(this.menuCacheKey);
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

	public void onClick(MenuButtonI<?> menuButton, Player player, int clickedPos, ClickType clickType, ItemStack clickedItem) {
		final T object = this.getObjectFromList(clickedPos);
		MenuButtonI<T> menuButtonCast = (MenuButtonI<T>) menuButton;
		if (this.getMenu() != null)
			menuButtonCast.onClickInsideMenu(player, this.getMenu(), clickType, clickedItem, object);
	}

	private boolean checkLastOpenMenu() {
		if (getPreviousMenuholder(this.player) != null) {
			if (hasPlayerMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS))
				removePlayerMenuMetadata(this.player, MenuMetadataKey.MENU_OPEN_PREVIOUS);
			return false;
		}
		return true;
	}

	protected void setLocationMetaOnPlayer(final Player player, final Location location) {
		String uniqueKey = this.uniqueKey;
		if (uniqueKey != null && uniqueKey.isEmpty()) {
			this.uniqueKey = this.getClass().getName();
			uniqueKey = this.uniqueKey;
		}
		menuCacheKey = this.menuCache.getMenuCacheKey(location, uniqueKey);
		if (menuCacheKey == null) menuCacheKey = new MenuCacheKey(location, uniqueKey);
		setPlayerLocationMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, menuCacheKey);
	}

	protected void setMetadataKey(final String setPlayerMetadataKey) {
		this.playermetadataKey = setPlayerMetadataKey;
	}

	protected void onMenuOpenPlaySound() {
		final Sound sound = this.menuOpenSound;
		if (sound == null) return;

		this.player.playSound(player.getLocation(), sound, 1, 1);
	}

	/**
	 * Do not use this method, use {@link #menuClose}
	 *
	 * @param event some get fierd.
	 * @deprecated is only for internal use, do not override this.
	 */
	@Deprecated
	protected void onMenuClose(final InventoryCloseEvent event) {
		if (Bukkit.getScheduler().isCurrentlyRunning(this.taskid) || Bukkit.getScheduler().isQueued(this.taskid)) {
			Bukkit.getScheduler().cancelTask(this.taskid);

		}
	}

	protected Inventory loadInventory(final Player player, final boolean loadToCahe) {
		Inventory menu = null;
		if (loadToCahe && this.location != null) {
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
			/*final MenuUtility previous = getMenuholder(this.player);
			if (previous != null && !hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
				setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, this);
				setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, this);
				menu = getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN).getMenu();
			} else {
				setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, this);
				setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, this);
				menu = getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN).getMenu();
			}*/
			setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_PREVIOUS, this);
			setPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN, this);
			final MenuUtility<?> menuUtility = getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
			if (menuUtility != null) menu = menuUtility.getMenu();

		}
		return menu;
	}

	private double amountOfpages() {
		final List<?> fillItems = this.getListOfFillItems();
		final List<Integer> fillSpace = this.getFillSpace();
		if (this.itemsPerPage > 0) {
			if (this.itemsPerPage > this.inventorySize)
				this.plugin.getLogger().log(Level.SEVERE, "Items per page are biger an Inventory size, items items per page " + this.itemsPerPage + ". Inventory size " + this.inventorySize, new Throwable().fillInStackTrace());
			if (!fillSpace.isEmpty()) {
				return (double) fillSpace.size() / this.itemsPerPage;
			} else if (fillItems != null && !fillItems.isEmpty()) return (double) fillItems.size() / this.itemsPerPage;
			else return (double) this.pagesOfButtonsData.size() / this.itemsPerPage;
		}
		if (fillItems != null && !fillItems.isEmpty()) {
			return (double) fillItems.size() / (fillSpace.isEmpty() ? this.inventorySize - 9 : fillSpace.size());
		} else return (double) this.pagesOfButtonsData.size() / this.inventorySize;
	}

	protected Map<Integer, ButtonData<?>> addItemsToCache(final int pageNumber) {
		final MenuDataUtility menuDataUtility = cacheMenuData(pageNumber);
		if (!this.shallCacheItems) {
			this.putAddedButtonsCache(pageNumber, menuDataUtility);
		}
		return menuDataUtility.getButtons();
	}

	protected Map<Integer, ButtonData<?>> addItemsToCache() {
		Map<Integer, ButtonData<?>> addedButtons = new HashMap<>();
		this.requiredPages = Math.max((int) Math.ceil(amountOfpages()), 1);
		if (this.manuallySetPages > 0) this.requiredPages = this.manuallySetPages;

		for (int i = 0; i < this.requiredPages; i++) {
			addedButtons = addItemsToCache(i);
			if (i == 0) numberOfFillitems = this.slotIndex;
		}
		this.slotIndex = 0;
		return addedButtons;
	}

	@Nonnull
	private MenuDataUtility cacheMenuData(final int pageNumber) {
		//final Map<Integer, ButtonData> addedButtons = new HashMap<>();
		final MenuDataUtility menuDataUtility = MenuDataUtility.of();
		for (int slot = 0; slot < this.inventorySize; slot++) {

			T objectFromlistOfFillItems = null;
			final int slotIndexOld = this.slotIndex;
			boolean isFillButton = false;
			if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
				objectFromlistOfFillItems = getObjectFromlistOfFillItems(slotIndexOld);
				this.slotIndex++;
				isFillButton = true;
			}
			final MenuButtonI<T> menuButton = getMenuButtonAtSlot(slot, slotIndexOld, objectFromlistOfFillItems);
			final ItemStack result = getItemAtSlot(menuButton, slot, slotIndexOld, objectFromlistOfFillItems);

			if (menuButton != null) {
				boolean shallAddMenuButton = isFillButton && this.getListOfFillItems() != null && !this.getListOfFillItems().isEmpty();
				if (menuButton.shouldUpdateButtons()) this.buttonsToUpdate.add(menuButton);
				final ButtonData<?> buttonData = new ButtonData<>(result, shallAddMenuButton ? null : menuButton, objectFromlistOfFillItems);

				menuDataUtility.putButton(pageNumber * this.getInventorySize() + slot, buttonData, shallAddMenuButton ? menuButton : null);
				//addedButtons.put(pageNumber * this.getInventorySize() + slot, new ButtonData(result, menuButton, objectFromlistOfFillItems));
			}
		}
		return menuDataUtility;
	}

	private MenuButtonI<T> getMenuButtonAtSlot(final int slot, final int oldSlotIndex, final T objectFromlistOfFillItems) {
		final MenuButtonI<T> result;
		if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
			if (objectFromlistOfFillItems != null && !objectFromlistOfFillItems.equals("")) {
				result = getFillButtonAt(objectFromlistOfFillItems);
			} else result = getFillButtonAt(oldSlotIndex);
		} else {
			result = getButtonAt(slot);
		}
		return result;
	}

	private ItemStack getItemAtSlot(final MenuButtonI<T> menuButton, final int slot, final int oldSlotIndex, final T objectFromlistOfFillItems) {
		if (menuButton == null) return null;

		ItemStack result;
		if (!this.getFillSpace().isEmpty() && this.getFillSpace().contains(slot)) {
			if (objectFromlistOfFillItems != null && !objectFromlistOfFillItems.equals("")) {
				result = menuButton.getItem(objectFromlistOfFillItems);
				if (result == null) result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
			} else {
				result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
			}
			if (result == null) result = menuButton.getItem();
		} else {
			result = menuButton.getItem();
			if (result == null) result = menuButton.getItem(oldSlotIndex, objectFromlistOfFillItems);
		}
		return result;
	}

	private T getObjectFromlistOfFillItems(final int slotIndex) {
		final List<T> fillItems = this.getListOfFillItems();
		if (fillItems != null && fillItems.size() > slotIndex) return fillItems.get(slotIndex);
		else return null;
	}

	protected void reddrawInventory() {
		if (this.getMenu() == null || this.inventorySize > this.getMenu().getSize()) this.inventory = createInventory();

		final int fillSpace = !getFillSpace().isEmpty() ? getFillSpace().size() : this.getMenu().getSize();

		for (int i = getFillSpace().stream().findFirst().orElse(0); i < fillSpace; i++) {
			this.getMenu().setItem(i, new ItemStack(Material.AIR));
		}

		final Map<Integer, ButtonData<?>> entity = this.getMenuButtons(this.getPageNumber());
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
		final String title = this.getTitle();

		if (this.getInventoryType() != null)
			return Bukkit.createInventory(null, this.getInventoryType(), title != null ? title : "");
		if (!(this.inventorySize == 5 || this.inventorySize % 9 == 0))
			plugin.getLogger().log(Level.WARNING, "wrong inverntory size , you has put in " + this.inventorySize + " it need to be valid number.");
		if (this.inventorySize == 5)
			return Bukkit.createInventory(null, InventoryType.HOPPER, title != null ? title : "");
		return Bukkit.createInventory(null, this.inventorySize % 9 == 0 ? this.inventorySize : 9, title != null ? title : "");
	}

	private long getupdateTime(final MenuButtonI menuButton) {
		if (menuButton.setUpdateTime() == -1) return getUpdateTime();
		return menuButton.setUpdateTime();
	}

	protected void updateButtonsInList() {
		taskid = new BukkitRunnable() {
			private int counter = 0;

			@Override
			public void run() {
				for (final MenuButtonI<?> menuButton : getButtonsToUpdate()) {

					final Long timeleft = getTimeWhenUpdatesButton(menuButton);
					if (timeleft != null && timeleft == -1) continue;

					if (timeleft == null || timeleft == 0)
						putTimeWhenUpdatesButtons(menuButton, counter + getupdateTime(menuButton));
					else if (counter >= timeleft) {
						getMenuData(getPageNumber());
						final MenuDataUtility menuDataUtility = getMenuData(getPageNumber());
						if (menuDataUtility == null) {
							cancel();
							return;
						}

						final Set<Integer> itemSlots = getItemSlotsMap(menuDataUtility, menuButton);

						if (itemSlots.isEmpty())
							putTimeWhenUpdatesButtons(menuButton, counter + getupdateTime(menuButton));
						else {
							final Iterator<Integer> slotList = itemSlots.iterator();
							while (slotList.hasNext()) {
								final Integer slot = slotList.next();

								final ButtonData buttonData = menuDataUtility.getButton(getSlot(slot));
								if (buttonData == null) continue;

								final ItemStack menuItem = getMenuItem(menuButton, buttonData, slot);
								final ButtonData newButtonData = new ButtonData(menuItem, buttonData.getMenuButton(), buttonData.getObject());

								menuDataUtility.putButton(getSlot(slot), newButtonData, menuDataUtility.getFillMenuButton(getSlot(slot)));
								//	menuDataMap.put(getSlot(slot), new ButtonData(menuItem, buttonData.getMenuButton(), buttonData.getObject()));

								putAddedButtonsCache(getPageNumber(), menuDataUtility);
								getMenu().setItem(slot, menuItem);
								slotList.remove();
							}
							putTimeWhenUpdatesButtons(menuButton, counter + getupdateTime(menuButton));
						}
					}
				}
				counter++;
			}
		}.runTaskTimer(plugin, 1L, 20L).getTaskId();

	}

	@Nullable
	private ItemStack getMenuItem(final MenuButtonI menuButton, final ButtonData cachedButtons, final int slot) {
		return getMenuItem(menuButton, cachedButtons, slot, menuButton.shouldUpdateButtons());
	}

	@Nullable
	protected ItemStack getMenuItem(final MenuButtonI menuButton, final ButtonData cachedButtons, final int slot, final boolean updateButton) {
		if (menuButton == null) return null;

		if (updateButton) {
			ItemStack itemStack = menuButton.getItem();
			if (itemStack != null) return itemStack;
			itemStack = menuButton.getItem(cachedButtons.getObject());
			if (itemStack != null) return itemStack;
			itemStack = menuButton.getItem(getSlot(slot), cachedButtons.getObject());
			return itemStack;
		}
		return null;
	}

	/**
	 * Get all slots same menu button is conected too.
	 *
	 * @param menuDataMap the map with all slots and menu data
	 * @param menuButton  the menu buttons you want to match with.
	 * @return set of slots that match same menu button.
	 */
	@Nonnull
	private Set<Integer> getItemSlotsMap(final MenuDataUtility menuDataMap, final MenuButtonI menuButton) {
		final Set<Integer> slotList = new HashSet<>();
		if (menuDataMap == null) return slotList;

		for (int slot = 0; slot < this.inventorySize; slot++) {
			final ButtonData addedButtons = menuDataMap.getButtons().get(this.getSlot(slot));
			if (addedButtons == null) continue;

			final MenuButtonI chacheMenuButton = addedButtons.getMenuButton();
			final MenuButtonI fillMenuButton = menuDataMap.getFillMenuButton(this.getSlot(slot));
			final int menuButtonId = menuButton.getId();
			if ((chacheMenuButton == null && fillMenuButton != null && fillMenuButton.getId() == menuButtonId) || (chacheMenuButton != null && Objects.equals(menuButtonId, chacheMenuButton.getId())))
				slotList.add(slot);
			//if ((addedButtons.getMenuButton() == null && menuDataMap.getFillMenuButton() != null && menuDataMap.getFillMenuButton().getId() == menuButton.getId()) || (addedButtons.getMenuButton().getId() == menuButton.getId()))
			//slotList.add(slot);
		}
		return slotList;
	}

}
