package org.broken.arrow.library.menu;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.menu.cache.MenuCache;
import org.broken.arrow.library.menu.cache.MenuCacheKey;
import org.broken.arrow.library.menu.messages.SendMsgDuplicatedItems;
import org.broken.arrow.library.menu.utility.Action;
import org.broken.arrow.library.menu.utility.MetadataPlayer;
import org.broken.arrow.library.menu.utility.ServerVersion;
import org.broken.arrow.library.menu.utility.metadata.MenuMetadataKey;
import org.broken.arrow.library.title.update.UpdateTitle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Main API class for managing custom menu registration and interaction.
 * <p>
 * This class provides access to core components such as menu caching,
 * player metadata, item creation utilities, and message handling related
 * to duplicated or blacklisted items in menus.
 * </p>
 * <p>
 * It also handles version checks, event registration, and integration with
 * optional modules such as dynamic title updates and item creators.
 * </p>
 * <p>
 * This class is a singleton and can be accessed globally via {@link #getMenuAPI()}.
 * </p>
 */
public class RegisterMenuAPI {
    private static RegisterMenuAPI menuAPI;
    private final Logging logger = new Logging(RegisterMenuAPI.class);
    private final MenuCache menuCache;
    private final Plugin plugin;
    private MetadataPlayer playerMeta;
    private ItemCreator itemCreator;
    private CheckItemsInsideMenu checkItemsInsideMenu;
    private SendMsgDuplicatedItems messages;
    private boolean notFoundItemCreator;
    private boolean notFoundUpdateTitle;

    /**
     * Private default constructor used internally for singleton instance initialization.
     * Initializes plugin and menuCache fields to null.
     */
    private RegisterMenuAPI() {
        menuCache = null;
        plugin = null;
    }

    /**
     * Constructs the API instance using the provided plugin.
     * <p>
     * Automatically registers this instance as the singleton instance.
     * Performs version checks and event registration.
     * </p>
     *
     * @param plugin the plugin instance to associate with this API.
     */
    public RegisterMenuAPI(final Plugin plugin) {
        this(plugin, false);
    }

    /**
     * Constructs the API instance with an option to turn off logging.
     * <p>
     * Automatically registers this instance as the singleton instance.
     * Performs version checks and event registration.
     * </p>
     *
     * @param plugin        the plugin instance to associate with this API.
     * @param turnOffLogger true to disable version check logging; false to enable.
     */
    public RegisterMenuAPI(final Plugin plugin, boolean turnOffLogger) {
        registerInstance(this);
        this.plugin = plugin;
        this.menuCache = new MenuCache();
        versionCheck(turnOffLogger);
        if (this.plugin == null) {
            logger.log(Level.WARNING, () -> "You have not set a plugin.");
            logger.log(Level.WARNING, () -> "If you're unsure how to use this library, " +
                    "contact the developer for assistance.");
            return;
        }
        try {
            UpdateTitle.update(null, "");
        } catch (NoClassDefFoundError ignore) {
            logger.log(() -> "Important: Dynamic change menu titles not available.");
            logger.log(() -> "To enable the option to change the menu title while the menu is open,");
            logger.log(() -> "please make sure you have imported the Title Update module into your plugin.");
            logger.log(() -> "Without the Title Update module, you won't be able to dynamically update");
            logger.log(() -> "the menu title while the menu is open.");
            logger.log(() -> "If you're unsure how to import the module, please refer to the documentation");
            logger.log(() -> "or contact the developer for assistance.");
            notFoundUpdateTitle = true;
        }
        registerMenuEvent(plugin);
        this.checkItemsInsideMenu = new CheckItemsInsideMenu(this);
        this.playerMeta = new MetadataPlayer(plugin);
        this.messages = new SendMsgDuplicatedItems();
        try {
            this.itemCreator = new ItemCreator(plugin);
        } catch (NoClassDefFoundError ignore) {
            notFoundItemCreator = true;
        }
    }

    /**
     * Registers the provided {@link RegisterMenuAPI} instance as the singleton API instance.
     *
     * @param registerMenu the instance to register
     */
    private static void registerInstance(@Nonnull final RegisterMenuAPI registerMenu) {
        menuAPI = registerMenu;
    }

    /**
     * Retrieves the globally registered singleton instance of the {@link RegisterMenuAPI}.
     *
     * @return the singleton instance of RegisterMenuAPI.
     */
    public static RegisterMenuAPI getMenuAPI() {
        return menuAPI;
    }

    /**
     * Performs a server version check and logs status messages.
     *
     * @param turnOffLogger if true, disables the startup logging.
     */
    private void versionCheck(boolean turnOffLogger) {
        if (!turnOffLogger)
            logger.log(() -> "Now starting MenuApi.. Will check server version and what modules is included.");
        ServerVersion.atLeast(1);
    }

    /**
     * Logs a message with the given log level.
     *
     * @param level   the logging level (e.g., INFO, WARNING)
     * @param message the message to log
     */
    public void getLogger(final Level level, final String message) {
        logger.log(level, () -> message);
    }

    /**
     * Gets the plugin instance associated with this API.
     *
     * @return the plugin instance or null if not set
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the player metadata utility instance.
     *
     * @return the {@link MetadataPlayer} instance
     */
    public MetadataPlayer getPlayerMeta() {
        return playerMeta;
    }

    /**
     * Gets the {@link ItemCreator} utility instance.
     *
     * @return the item creator, or null if not available
     */
    @Nullable
    public ItemCreator getItemCreator() {
        return itemCreator;
    }

    /**
     * Gets the {@link CheckItemsInsideMenu} utility instance used to check items inside menus.
     *
     * @return the CheckItemsInsideMenu instance
     */
    public CheckItemsInsideMenu getCheckItemsInsideInventory() {
        return checkItemsInsideMenu;
    }

    /**
     * Gets the menu cache which holds cached menus.
     *
     * @return the {@link MenuCache} instance
     */
    public MenuCache getMenuCache() {
        return menuCache;
    }

    /**
     * Returns whether the dynamic update title class was not found.
     * <p>
     * Indicates if the title update module is missing and menu titles cannot be updated dynamically.
     * </p>
     *
     * @return true if the update title module was not found; false otherwise.
     */
    public boolean isNotFoundUpdateTitleClazz() {
        return notFoundUpdateTitle;
    }

    /**
     * Returns whether the item creator class was not found.
     * <p>
     * Indicates if the item creator module is missing.
     * </p>
     *
     * @return true if the item creator module was not found; false otherwise.
     */
    public boolean isNotFoundItemCreator() {
        return notFoundItemCreator;
    }

    /**
     * Gets the message handler for sending duplicated or blacklisted item messages.
     *
     * @return the {@link SendMsgDuplicatedItems} instance
     */
    public SendMsgDuplicatedItems getMessages() {
        return messages;
    }

    private void registerMenuEvent(final Plugin plugin) {
        final MenuHolderListener menuHolderListener = new MenuHolderListener();
        Bukkit.getPluginManager().registerEvents(menuHolderListener, plugin);

    }

    private class MenuHolderListener implements Listener {

        private final MenuCache menuCache = getMenuCache();
        private final Map<UUID, SwapData> cacheData = new HashMap<>();

        @EventHandler(priority = EventPriority.LOW)
        public void onMenuClicking(final InventoryClickEvent event) {
            final Player player = (Player) event.getWhoClicked();

            if (event.getClickedInventory() == null) return;
            ItemStack clickedItem = event.getCurrentItem();

            final MenuUtility<?> menuUtility = getMenuHolder(player);
            if (menuUtility == null) return;

            if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) menuUtility.menuClickOutside(event, menuUtility);

            if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.CLICKED)) {
                return;
            }

            if (menuUtility.getMenuInteractionChecks().whenPlayerClick(event, player, clickedItem)) {
                onOffHandClick(event, player);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onMenuOpen(final InventoryOpenEvent event) {
            final Player player = (Player) event.getPlayer();

            final MenuUtility<?> menuUtility = getMenuHolder(player);
            if (menuUtility == null) return;

            if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.OPEN)) {
                return;
            }
            if (ServerVersion.olderThan(15.0)) return;

            this.cacheData.put(player.getUniqueId(), new SwapData(false, player.getInventory().getItemInOffHand()));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onMenuClose(final InventoryCloseEvent event) {
            final Player player = (Player) event.getPlayer();

            final MenuUtility<?> menuUtility = getMenuHolder(player);
            if (menuUtility == null) return;

            final SwapData data = cacheData.get(player.getUniqueId());
            if (data != null && data.isPlayerUseSwapoffhand()) {
                if (data.getItemInOfBeforeOpenMenuHand() != null && data.getItemInOfBeforeOpenMenuHand().getType() != Material.AIR) {
                    player.getInventory().setItemInOffHand(data.getItemInOfBeforeOpenMenuHand());
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }
            cacheData.remove(player.getUniqueId());
            if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.CLOSE)) {
                return;
            }
            try {
                menuUtility.menuClose(event);
            } finally {
                menuUtility.unregister(player);
            }
        }


        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryDragTop(final InventoryDragEvent event) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getView().getType() == InventoryType.PLAYER) return;

            final MenuUtility<?> menuUtility = getMenuHolder(player);
            if (menuUtility == null) return;
            if (menuUtility.getMenu() == null) return;

            if (!menuUtility.isAddedButtonsCacheEmpty()) {
                final int size = event.getView().getTopInventory().getSize();
                menuUtility.getMenuInteractionChecks().whenPlayerDrag(event, size);
            }
        }


        @Nullable
        private MenuUtility<?> getMenuHolder(final Player player) {

            MenuCacheKey menukey = null;

            MetadataPlayer metadataPlayer = getPlayerMeta();
            if (metadataPlayer.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
                menukey = metadataPlayer.getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, MenuCacheKey.class);
            }

            final MenuUtility<?> menuUtility;
            if (metadataPlayer.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
                menuUtility = metadataPlayer.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
            } else {
                menuUtility = menuCache.getMenuInCache(menukey, MenuUtility.class);
            }
            return menuUtility;
        }

        private void onOffHandClick(final InventoryClickEvent event, final Player player) {
            if (ServerVersion.newerThan(15.0) && event.getClick() == ClickType.SWAP_OFFHAND) {
                final SwapData data = cacheData.get(player.getUniqueId());
                ItemStack item = null;
                if (data != null) {
                    item = data.getItemInOfBeforeOpenMenuHand();
                }
                cacheData.put(player.getUniqueId(), new SwapData(true, item));
            }
        }

        private class SwapData {

            boolean playerUseSwapoffhand;
            ItemStack itemInOfBeforeOpenMenuHand;

            public SwapData(final boolean playerUseSwapoffhand, final ItemStack itemInOfBeforeOpenMenuHand) {
                this.playerUseSwapoffhand = playerUseSwapoffhand;
                this.itemInOfBeforeOpenMenuHand = itemInOfBeforeOpenMenuHand;
            }

            public boolean isPlayerUseSwapoffhand() {
                return playerUseSwapoffhand;
            }

            public ItemStack getItemInOfBeforeOpenMenuHand() {
                return itemInOfBeforeOpenMenuHand;
            }
        }
    }
}
