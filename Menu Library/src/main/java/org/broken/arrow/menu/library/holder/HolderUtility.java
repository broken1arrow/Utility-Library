package org.broken.arrow.menu.library.holder;

import com.google.gson.JsonObject;
import org.broken.arrow.menu.library.MenuMetadataKey;
import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.RegisterMenuAPI;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.cache.MenuCacheKey;
import org.broken.arrow.menu.library.utility.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This utility class is used to create a menu from a Minecraft inventory. It extends MenuUtility and is parameterized by
 * the type of object stored in the button cache and used throughout the class.
 *
 * @param <T> The type of object stored in the button cache and used in this class, including in {@link MenuHolderPage}.
 */
public abstract class HolderUtility<T> extends MenuUtility<T> {


    /**
     * Constructs a menu instance without specifying fill slots and if it you shall cache the button items.
     */
    protected HolderUtility() {
        super(null, false);
    }


    /**
     * Create menu instance.
     *
     * @param shallCacheItems Set this to false items and slots should be cached in this class,
     *                        other case override  {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        this in own implementation.
     */
    protected HolderUtility(final boolean shallCacheItems) {
        super(null, shallCacheItems);
    }

    /**
     * Constructs a menu instance with specified fill slots and an option to cache items and slots.
     *
     * @param fillSlots       The slots to be filled with items.
     * @param shallCacheItems Set this to false items and slots should be cached in this class,
     *                        other case override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        this in own implementation.
     */
    protected HolderUtility(List<Integer> fillSlots, boolean shallCacheItems) {
        super(fillSlots, shallCacheItems);
    }


    /**
     * Constructs a menu instance with specified parameters.
     * <p>&nbsp;</p>
     * <strong>Note:</strong> Use this constructor only if you are not shading this library into your plugin.
     *
     * @param menuAPI         The instance of RegisterMenuAPI where you have registered your plugin.
     * @param fillSlots       The slots to be filled with items. Can be null if not filling specific slots.
     * @param shallCacheItems Set this to false items and slots should be cached in this class,
     *                        other case override {@link #retrieveMenuButtons(int, MenuDataUtility)} to cache
     *                        this in own implementation.
     */
    protected HolderUtility(RegisterMenuAPI menuAPI, List<Integer> fillSlots, boolean shallCacheItems) {
        super(menuAPI, fillSlots, shallCacheItems);
    }

    /**
     * When you close the menu
     *
     * @param event close inventory
     * @param menu  class some are now closed.
     */

    @Override
    public void menuClose(final InventoryCloseEvent event, final MenuUtility<?> menu) {
    }

    /**
     * open menu and make one instance in cache.
     * Will be cleared on server restart.
     *
     * @param player   some open menu.
     * @param location location you open menu.
     */
    public void menuOpen(@Nonnull final Player player, @Nullable final Location location) {
        menuOpen(player, location, true);
    }

    /**
     * open menu and make one instance, will be removed
     * when you close menu.
     *
     * @param player some open menu.
     */
    public void menuOpen(@Nonnull final Player player) {
        menuOpen(player, null, false);
    }

    /**
     * open menu and make one instance. If you set location to null, it will be removed
     * when you close menu.
     *
     * @param player     some open menu.
     * @param location   location you open menu.
     * @param loadToCahe if it shall load menu to cache.
     */
    public void menuOpen(@Nonnull final Player player, @Nullable final Location location, final boolean loadToCahe) {
        this.player = player;
        this.location = location;
        player.closeInventory();

        if (!shallCacheItems) {
            setMenuItemsToAllPages();
        }
        redrawInventory();

        final Inventory menu = loadInventory(player, loadToCahe);
        if (menu == null) return;

        player.openInventory(menu);


        onMenuOpenPlaySound();
        setMetadataKey(MenuMetadataKey.MENU_OPEN.name());

        if (!getButtonsToUpdate().isEmpty())
            updateButtonsInList();

        Bukkit.getScheduler().runTaskLater(menuAPI.getPlugin(), ()->this.updateTitle(), 1);
    }

    /**
     * set invetory size
     *
     * @param inventorySize size of this menu
     */

    public void setMenuSize(final int inventorySize) {
        this.inventorySize = inventorySize;
    }

    /**
     * Set menu title inside your menu. If you want to use
     * placeholders in the text use {@link #setTitle(Function)}.
     *
     * @param title you want to show inside the menu.
     */
    public void setTitle(final String title) {
        this.setTitle(() -> title);
    }

    /**
     * Sets the title of the menu using the specified
     * function to automatically update any placeholders.
     *
     * @param function a function that takes a String input, used to correctly update placeholders in the menu title.
     */
    public void setTitle(final Function<String> function) {
        this.titleFunction = function;
    }

    /**
     * Sets the title of the menu using the specified
     * function to automatically update any placeholders.
     * <p>
     * You need to ensure proper formatting, follow this structure:
     * For titles with multiple colors set in the same text, use the "extra" key and
     * an empty "text" element outside the array at the end of the JSON.
     * </p>
     * <p>
     * Example with multiple components:
     * </p>
     * <pre>
     * {
     *   "extra":[
     *      {
     *        "color":"gold",
     *        "text":"Test "
     *      },
     *      {
     *        "color":"dark_red",
     *        "bold":true,
     *        "text":"this"
     *       }
     *    ],
     *    "text":""
     * }
     * </pre>
     * Example with a single color set:
     * <pre>
     * {
     *   "color": "gold",
     *   "text": "Test this"
     * }
     * </pre>
     *
     * @param function a function that takes a String input, used to correctly update placeholders in the menu title.
     */
    public void setTitleJson(final Function<JsonObject> function) {
        this.titleFunctionJson = function;
    }

    /**
     * Sets the title of the menu using the specified
     * function to animate title. You can
     *
     * @param time     set how often it shall update, in seconds.
     * @param function a function that takes a String, for animate title.
     */
    public void setAnimateTitle(final int time, final Function<String> function) {
        this.animateTitleTime = time;
        this.animateTitle = function;
        this.runAnimateTitle();
    }

    /**
     * Sets the title of the menu using the specified
     * function to animate title.
     * <p>
     * You need to ensure proper formatting, follow this structure:
     * For titles with multiple colors set in the same text, use the "extra" key and
     * an empty "text" element outside the array at the end of the JSON.
     * </p>
     * <p>
     * Example with multiple components:
     * </p>
     * <pre>
     * {
     *   "extra":[
     *      {
     *        "color":"gold",
     *        "text":"Test "
     *      },
     *      {
     *        "color":"dark_red",
     *        "bold":true,
     *        "text":"this"
     *       }
     *    ],
     *    "text":""
     * }
     * </pre>
     * Example with a single color set:
     * <pre>
     * {
     *   "color": "gold",
     *   "text": "Test this"
     * }
     * </pre>
     *
     * @param time     set how often it shall update, in seconds.
     * @param function a function that takes a JsonObject, for animate title.
     */
    public void setAnimateTitleJson(final int time, final Function<JsonObject> function) {
        this.animateTitleTime = time;
        this.animateTitleJson = function;
        this.runAnimateTitle();
    }

    /**
     * Set type of inventory, defult will it use chest or hopper. If you set
     * the type you can´t change size.
     *
     * @param inventoryType set type of inventory.
     */
    public void setInventoryType(final InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    /**
     * Sets the number of items to be displayed on each page of the inventory. If the specified value is zero or negative,
     * or if it exceeds the size of the inventory, the value will be set to the inventory size minus nine slots,
     * which is the default value.
     *
     * @param itemsPerPage The number of items to be displayed on each page.
     */
    public void setItemsPerPage(final int itemsPerPage) {
        if (itemsPerPage <= 0 || itemsPerPage > this.inventorySize)
            this.itemsPerPage = this.inventorySize - 9;
        else
            this.itemsPerPage = itemsPerPage;
    }

    /**
     * Supports "1-5,12-18","1-5","1,8,9,20" or only one number like this "14".
     * <p>
     * So for example, if you make a string of numbers like this "1-5,12-18",
     * it will add all numbers between 1 to 5 and 12 to 18. Numbers between
     * 5 and 12 will not be added, nor will numbers outside the range of 1-18.
     * <p>
     * Furthermore, this string '1,8,9,20' will only add the numbers 1, 8, 9,
     * and 20, and no other number will be added.
     * <p>
     * You use also this method or {@link #setFillSpace(java.util.List)} to tell the slots
     * player allowed to add and remove items, you need set this to true also {@link #setSlotsYouCanAddItems(boolean)}.
     *
     * @param fillSpace the string of slots you want to use as fill slots or slots you allow players add and remove items.
     */
    public void setFillSpace(final String fillSpace) {
        final List<Integer> slotList = new ArrayList<>();
        try {
            for (final String slot : fillSpace.split(",")) {
                if (slot.equals("")) {
                    continue;
                }
                if (slot.contains("-")) {
                    final int firstSlot = Integer.parseInt(slot.split("-")[0]);
                    final int lastSlot = Integer.parseInt(slot.split("-")[1]);
                    slotList.addAll(IntStream.rangeClosed(firstSlot, lastSlot).boxed().collect(Collectors.toList()));
                } else slotList.add(Integer.valueOf(slot));
            }
        } catch (final NumberFormatException e) {
            throw new NumberFormatException("can not parse this " + fillSpace + " as numbers.");
        }
        this.setFillSpace(slotList);
    }

    /**
     * witch slot you want to fill with items.
     * Recomend use {@link java.util.stream.IntStream#rangeClosed} to automatic convert
     * a range like first number 0 and last 26.
     * <p>
     * Like this IntStream.rangeClosed(0, 26).boxed().collect(Collectors.toList());
     * for a menu some are for example size 36 will it not add items to last 9 slots.
     * <p>
     * You use also this method or {@link #setFillSpace(String)} to tell the slots
     * player allowed to add and remove items, you neeed set this to true also {@link #setSlotsYouCanAddItems(boolean)}.
     *
     * @param fillSpace the list of slots you want to use as fill slots or slots you allow players add and remove items.
     */
    public void setFillSpace(final List<Integer> fillSpace) {
        this.fillSpace = fillSpace;
        if (fillSpace != null)
            this.highestFillSlot = fillSpace.stream().mapToInt(Integer::intValue).max().orElse(-1);
    }

    /**
     * Set sound when open menu.
     * Defult it is BLOCK_NOTE_BLOCK_BASEDRUM , if you set this
     * to null it will not play any sound.
     *
     * @param sound set open sound iin menu or null to disable.
     */

    public void setMenuOpenSound(final Sound sound) {
        this.menuOpenSound = sound;
    }

    /**
     * set this to true if you whant players has option to add or remove items. however you also need set the slots
     * you allow player remove and add items {@link MenuHolder#setFillSpace(String)} or {@link MenuHolder#setFillSpace(java.util.List)} for get it work.
     *
     * @param slotsYouCanAddItems true and it will give option to add and remove items on fill slots.
     */

    public void setSlotsYouCanAddItems(final boolean slotsYouCanAddItems) {
        this.slotsYouCanAddItems = slotsYouCanAddItems;
    }

    /**
     * Set to false if you want to deny shift-click.
     * You dont need set this to true, becuse it allow
     * shiftclick as defult.
     *
     * @param allowShiftClick set to false if you want to deny shiftclick
     */

    public void setAllowShiftClick(final boolean allowShiftClick) {
        this.allowShiftClick = allowShiftClick;
    }

    /**
     * set the page you want to open.
     *
     * @param page set the page number.
     * @return true if it could set the page.
     */
    public boolean setPage(final int page) {
        if (!this.containsPage(page))
            return false;

        this.pageNumber = page;
        updateButtons();
        updateTitle();
        return true;
    }

    /**
     * get previous page if this menu has several pages
     */
    public void previousPage() {
        changePage(false);
    }

    /**
     * get next page if this menu has several pages
     */
    public void nextPage() {
        changePage(true);
    }


    /**
     * Update only one button. Set this inside the {@link MenuButton#onClickInsideMenu(Player, Inventory, ClickType, ItemStack)}
     * method and use this to tell what button some shall be updated.
     * <p>
     *
     * @param menuButton the current button.
     */
    public void updateButton(final MenuButton menuButton) {
        final MenuDataUtility<T> menuDataUtility = getMenuData(getPageNumber());
        final Set<Integer> buttonSlots = this.getButtonSlots(menuDataUtility, menuButton);

        Inventory menu = this.getMenu();
        if (menuDataUtility != null && menu != null) {
            if (!buttonSlots.isEmpty()) {
                for (final int slot : buttonSlots) {

                    int menuSlot = this.getSlot(slot);
                    final ButtonData<T> buttonData = menuDataUtility.getButton(menuSlot);
                    if (buttonData == null) return;

                    final ItemStack menuItem = getMenuItem(menuButton, buttonData, slot, true);
                    menu.setItem(slot, menuItem);
                    menuDataUtility.putButton(menuSlot, buttonData.copy(menuItem));
                }
            } else {
                final int buttonSlot = this.getButtonSlot(menuButton);
                int slot = this.getSlot(buttonSlot);
                final ButtonData<T> buttonData = menuDataUtility.getButton(slot);
                if (buttonData == null) return;

                final ItemStack itemStack = getMenuItem(menuButton, buttonData, buttonSlot, true);
                menu.setItem(buttonSlot, itemStack);
                menuDataUtility.putButton(slot, new ButtonData<>(itemStack, menuButton, buttonData.getObject()));
            }
            this.putAddedButtonsCache(this.getPageNumber(), menuDataUtility);
        }
    }

    /**
     * Update all buttons inside the menu.
     */
    @Override
    public void updateButtons() {
        super.updateButtons();
    }

    /**
     * Set if it shall ignore the set item in the slot and deny
     * player from remove items no mater if the item match set item
     * inside inventory and the item you set in getItem() method.
     *
     * @param ignoreItemCheck set to true and it will deny player from take items
     *                        , even if the item in inventory not match item you has set.
     */
    public void setIgnoreItemCheck(final boolean ignoreItemCheck) {
        this.ignoreItemCheck = ignoreItemCheck;
    }


    /**
     * Set time it shall update the buttons.
     *
     * @param updateTime the seconds between updates.
     */
    public void setUpdateTime(final int updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * Set this to false if you not want it to auto clear from
     * the cache when last player close inventory. It defult will clear the menu so you not have
     * to set this to true.
     *
     * @param autoClearCache set to false if you not want it to clear menu cache.
     */
    public void setAutoClearCache(final boolean autoClearCache) {
        this.autoClearCache = autoClearCache;
    }

    /**
     * If set to true, the menu will not perform a validity check when it is opened with a location
     * that already has a menu open. This could result in the old menu being overridden, and if a player
     * is still viewing that old menu, they will be able to take all items from it.
     * <p>
     * Set unique key with this method {@link #setUniqueKeyMenuCache(String)} instead.
     *
     * @param ignoreValidCheck true if the validity check should be ignored.
     */
    public void setIgnoreValidCheck(final boolean ignoreValidCheck) {
        this.ignoreValidCheck = ignoreValidCheck;
    }

    /**
     * Get if several players to look inside the current inventory. If it's zero
     * then is only one player currently looking inside the inventory.
     *
     * @return amount of players curently looking in the inventory.
     */
    @Override
    public int getAmountOfViewers() {
        return (int) (this.getMenu() == null ? -1 : this.getMenu().getViewers().stream().filter(entity -> entity instanceof Player).count() - 1);
    }

    /**
     * Turn off this option if you not want it to auto fill in curent page.
     * This option will be set to true automatic.
     *
     * @param autoTitleCurrentPage Set this to false to turn this off.
     */
    public void setAutoTitleCurrentPage(final boolean autoTitleCurrentPage) {
        this.autoTitleCurrentPage = autoTitleCurrentPage;

    }

    /**
     * Sets the flag indicating whether to use the Color Conversion module for translating colors, hex values, and gradients.
     * Check the Color Conversion module for details on formatting hex values and gradients.
     *
     * @param useColorConversion Set to true if you want this API to translate colors using the Color Conversion module.
     *                           In other cases, it will use the method {@link org.bukkit.ChatColor#translateAlternateColorCodes(char, String)}.
     *                           Note that hex colors may not work in the title.
     */
    public void setUseColorConversion(final boolean useColorConversion) {
        this.useColorConversion = useColorConversion;
    }

    /**
     * Get the key for the cached menu.
     *
     * @return the cached menu instance or null.
     */
    @Nullable
    public MenuCacheKey getMenuCacheKey() {
        return this.menuCacheKey;
    }
}
