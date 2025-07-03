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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


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

    private RegisterMenuAPI() {
        menuCache = null;
        plugin = null;
    }

    public RegisterMenuAPI(final Plugin plugin) {
        this(plugin, false);
    }

    public RegisterMenuAPI(final Plugin plugin, boolean turnOffLogger) {
        menuAPI = this;
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

    public static RegisterMenuAPI getMenuAPI() {
        return menuAPI;
    }

    private void versionCheck(boolean turnOffLogger) {
        if (!turnOffLogger)
            logger.log(() -> "Now starting MenuApi.. Will check server version and what modules is included.");
        ServerVersion.getCurrentServerVersion();
    }

    public void getLogger(final Level level, final String message) {
        logger.log(level, () -> message);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public MetadataPlayer getPlayerMeta() {
        return playerMeta;
    }

    @Nullable
    public ItemCreator getItemCreator() {
        return itemCreator;
    }

    public CheckItemsInsideMenu getCheckItemsInsideInventory() {
        return checkItemsInsideMenu;
    }

    public MenuCache getMenuCache() {
        return menuCache;
    }

    public boolean isNotFoundUpdateTitleClazz() {
        return notFoundUpdateTitle;
    }

    public boolean isNotFoundItemCreator() {
        return notFoundItemCreator;
    }

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
            if (ServerVersion.olderThan(ServerVersion.V1_15)) return;

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
                menuUtility.menuClose(event, menuUtility);
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
                menukey = metadataPlayer.getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION,MenuCacheKey.class);
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
            if (ServerVersion.newerThan(ServerVersion.V1_15) && event.getClick() == ClickType.SWAP_OFFHAND) {
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
