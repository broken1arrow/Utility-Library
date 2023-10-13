package org.broken.arrow.menu.library;

import org.broken.arrow.itemcreator.library.ItemCreator;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButtonI;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.broken.arrow.menu.library.messages.SendMsgDuplicatedItems;
import org.broken.arrow.menu.library.utility.Metadata;
import org.broken.arrow.menu.library.utility.ServerVersion;
import org.broken.arrow.nbt.library.RegisterNbtAPI;
import org.broken.arrow.title.update.library.UpdateTitle;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.broken.arrow.menu.library.utility.ServerVersion.v1_19_4;


public class RegisterMenuAPI {
	private static RegisterMenuAPI menuAPI;
	private final MenuCache menuCache;
	private final Plugin plugin;
	private Metadata playerMeta;
	private RegisterNbtAPI nbtApi;
	private ItemCreator itemCreator;
	private CheckItemsInsideMenu checkItemsInsideMenu;
	private SendMsgDuplicatedItems messages;
	private boolean notFoundItemCreator;
	private boolean notFoundUpdateTitle;
	private final Logger logger = Logger.getLogger("Register_MenuAPI");

	private RegisterMenuAPI() {
		throw new UnsupportedOperationException("You need specify your main class");
	}

	public RegisterMenuAPI(final Plugin plugin) {
		this(plugin, false);
	}

	public RegisterMenuAPI(final Plugin plugin, boolean turnOffLogger) {
		this.plugin = plugin;
		menuAPI = this;
		menuCache = new MenuCache();
		if (this.plugin == null) {
			logger.log(Level.WARNING, "You have not set a plugin.");
			logger.log(Level.WARNING, "If you're unsure how to use this library, " +
					"contact plugin developer for assistance.");
			return;
		}
		try {
			UpdateTitle.update(null, "");
		} catch (NoClassDefFoundError ignore) {
			logger.log(Level.INFO, "Important: Dynamic change menu titles not available.");
			logger.log(Level.INFO, "To enable the option to change the menu title while the menu is open,");
			logger.log(Level.INFO, "please make sure you have imported the Title Update module into your plugin.");
			logger.log(Level.INFO, "Without the Title Update module, you won't be able to dynamically update");
			logger.log(Level.INFO, "the menu title while the menu is open.");
			logger.log(Level.INFO, "If you're unsure how to import the module, please refer to the documentation");
			logger.log(Level.INFO, "or contact plugin developer for assistance.");
			notFoundUpdateTitle = true;
		}
		//ServerVersion.setServerVersion(plugin);
		versionCheck();
		registerMenuEvent(plugin);
		this.checkItemsInsideMenu = new CheckItemsInsideMenu(this);
		this.playerMeta = new Metadata(plugin);
		this.messages = new SendMsgDuplicatedItems();
		try {
			this.nbtApi = new RegisterNbtAPI(plugin, turnOffLogger);
			this.itemCreator = new ItemCreator(plugin);
		} catch (NoClassDefFoundError ignore) {
			notFoundItemCreator = true;
		}
	}

	public static RegisterMenuAPI getMenuAPI() {
		return menuAPI;
	}

	private void versionCheck() {
		logger.log(Level.INFO, "Now starting MenuApi. Any errors will be shown below.");
		if (ServerVersion.newerThan(v1_19_4)) {
			logger.log(Level.WARNING, "It is not tested on versions beyond 1.19.4");
		}
	}

	public void getLogger(final Level level, final String messsage) {
		logger.log(level, messsage);
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public Metadata getPlayerMeta() {
		return playerMeta;
	}

	public ItemCreator getItemCreator() {
		return itemCreator;
	}

	public CheckItemsInsideMenu getCheckItemsInsideInventory() {
		return checkItemsInsideMenu;
	}

	public RegisterNbtAPI getNbtApi() {
		return nbtApi;
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
		//if (!getRegisteredListeners(plugin).stream().allMatch(registeredListener -> registeredListener.getListener().getClass().equals(menuHolderListener.getClass())))
		Bukkit.getPluginManager().registerEvents(menuHolderListener, plugin);

	}

	private class MenuHolderListener implements Listener {

		private final MenuCache menuCache = getMenuCache();
		private final Map<UUID, SwapData> cacheData = new HashMap<>();

		@EventHandler(priority = EventPriority.LOW)
		public void onMenuClicking(final InventoryClickEvent event) {
			final Player player = (Player) event.getWhoClicked();

			if (event.getClickedInventory() == null)
				return;
			ItemStack clickedItem = event.getCurrentItem();

			final MenuUtility<?> menuUtility = getMenuHolder(player);
			if (menuUtility == null) return;

			if (!event.getView().getTopInventory().equals(menuUtility.getMenu())) return;

			whenPlayerClick(event, player, clickedItem, menuUtility);
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onMenuOpen(final InventoryOpenEvent event) {
			final Player player = (Player) event.getPlayer();

			final MenuUtility<?> menuUtility = getMenuHolder(player);
			if (menuUtility == null) return;
			if (ServerVersion.olderThan(ServerVersion.v1_15)) return;

			this.cacheData.put(player.getUniqueId(), new SwapData(false, player.getInventory().getItemInOffHand()));
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onMenuClose(final InventoryCloseEvent event) {
			final Player player = (Player) event.getPlayer();

			final MenuUtility<?> menuUtility = getMenuHolder(player);
			if (menuUtility == null) return;

			final SwapData data = cacheData.get(player.getUniqueId());
			if (data != null && data.isPlayerUseSwapoffhand())
				if (data.getItemInOfBeforeOpenMenuHand() != null && data.getItemInOfBeforeOpenMenuHand().getType() != Material.AIR)
					player.getInventory().setItemInOffHand(data.getItemInOfBeforeOpenMenuHand());
				else
					player.getInventory().setItemInOffHand(null);
			cacheData.remove(player.getUniqueId());

			if (!event.getView().getTopInventory().equals(menuUtility.getMenu()))
				return;

			menuUtility.onMenuClose(event);
			try {
				menuUtility.menuClose(event, menuUtility);
			} finally {
				if (getPlayerMeta().hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
					getPlayerMeta().removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
				}
				if (getPlayerMeta().hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
					if (menuUtility.isAutoClearCache()) {
						if (menuUtility.getAmountOfViewers() < 1) {
							menuCache.removeMenuCached(getPlayerMeta().getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION));
						}
					}
					getPlayerMeta().removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
				}
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

				checkMenuForDrag(event, menuUtility, size);
			}
		}

		public MenuButtonI<?> getClickedButton(final MenuUtility<?> menusData, final ItemStack item, final int clickedPos) {
			final MenuDataUtility<?> menuData = menusData.getMenuData(menusData.getPageNumber());
			if (menuData != null) {
				final ButtonData<?> buttonData = menuData.getButton(clickedPos);
				if (buttonData == null) return null;
				if (menusData.isIgnoreItemCheck()) {
					return menuData.getMenuButton(clickedPos);
				}
				if (isItemSimilar(buttonData.getItemStack(), item)) {
					return menuData.getMenuButton(clickedPos);
				}
			}
			return null;
		}

		public boolean isItemSimilar(final ItemStack item, final ItemStack clickedItem) {
			if (item != null && clickedItem != null)
				if (itemIsSimilar(item, clickedItem)) {
					return true;
				} else {
					return item.isSimilar(clickedItem);
				}

			return false;
		}

		public boolean itemIsSimilar(final ItemStack firstItem, final ItemStack secondItemStack) {

			if (firstItem.getType() == secondItemStack.getType()) {
				if (firstItem.hasItemMeta() && firstItem.getItemMeta() != null) {
					final ItemMeta itemMeta1 = firstItem.getItemMeta();
					final ItemMeta itemMeta2 = secondItemStack.getItemMeta();
					if (!itemMeta1.equals(itemMeta2))
						return false;
					return getDurability(firstItem, itemMeta1) == getDurability(secondItemStack, itemMeta2);
				}
				return true;
			}
			return false;
		}

		public short getDurability(final ItemStack itemstack, final ItemMeta itemMeta) {
			if (ServerVersion.atLeast(ServerVersion.v1_13))
				return (itemMeta == null) ? 0 : (short) ((Damageable) itemMeta).getDamage();
			return itemstack.getDurability();
		}

		public ItemStack checkIfNull(final ItemStack curentCursor, final ItemStack oldCursor) {
			return curentCursor != null ? curentCursor : oldCursor != null ? oldCursor : new ItemStack(Material.AIR);
		}

		@Nullable
		private MenuUtility<?> getMenuHolder(final Player player) {

			Object menukey = null;

			if (getPlayerMeta().hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
				menukey = getPlayerMeta().getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
			}

			final MenuUtility<?> menuUtility;
			if (getPlayerMeta().hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
				menuUtility = getPlayerMeta().getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
			} else {
				menuUtility = menuCache.getMenuInCache(menukey,MenuUtility.class);
			}
			return menuUtility;
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

		private void whenPlayerClick(final InventoryClickEvent event, final Player player, ItemStack clickedItem, final MenuUtility<?> menuUtility) {
			if (!menuUtility.isAddedButtonsCacheEmpty()) {
				final int clickedSlot = event.getSlot();
				final int clickedPos = menuUtility.getSlot(clickedSlot);
				Inventory clickedInventory = event.getClickedInventory();
				if (checkMenuIsValid(event, menuUtility, clickedPos, clickedInventory)) return;
				final MenuButtonI<?> menuButton = getClickedButton(menuUtility, clickedItem, clickedPos);
				if (menuButton != null) {
					event.setCancelled(true);
					if (clickedItem == null)
						clickedItem = new ItemStack(Material.AIR);
					menuUtility.onClick(menuButton, player, clickedPos, event.getClick(), clickedItem);

					onOffHandClick(event, player);
				}
			}
		}

		private void onOffHandClick(final InventoryClickEvent event, final Player player) {
			if (ServerVersion.newerThan(ServerVersion.v1_15) && event.getClick() == ClickType.SWAP_OFFHAND) {
				final SwapData data = cacheData.get(player.getUniqueId());
				ItemStack item = null;
				if (data != null) {
					item = data.getItemInOfBeforeOpenMenuHand();
				}
				cacheData.put(player.getUniqueId(), new SwapData(true, item));
			}
		}

		private boolean checkMenuIsValid(final InventoryClickEvent event, final MenuUtility<?> menuUtility, final int clickedPos, final Inventory clickedInventory) {
			final ItemStack cursor = event.getCursor();
			if (!menuUtility.isAllowShiftClick() && event.getClick().isShiftClick()) {
				event.setCancelled(true);
				return true;
			}
			if (menuUtility.isSlotsYouCanAddItems()) {
				if (menuUtility.getFillSpace().contains(clickedPos))
					return true;
				else if (clickedInventory.getType() != InventoryType.PLAYER)
					event.setCancelled(true);
			} else {
				if (clickedInventory.getType() == InventoryType.PLAYER)
					if (event.getClick().isShiftClick()) {
						event.setCancelled(true);
					} else
						event.setCancelled(true);
				if (cursor != null && cursor.getType() != Material.AIR)
					event.setCancelled(true);
			}
			return false;
		}

		private void checkMenuForDrag(final InventoryDragEvent event, final MenuUtility<?> menuUtility, final int size) {
			for (final int clickedSlot : event.getRawSlots()) {
				if (clickedSlot > size)
					continue;

				final int clickedPos = menuUtility.getSlot(clickedSlot);

				final ItemStack cursor = checkIfNull(event.getCursor(), event.getOldCursor());
				if (menuUtility.isSlotsYouCanAddItems()) {
					if (menuUtility.getFillSpace().contains(clickedSlot))
						return;
					else
						event.setCancelled(true);
				} else {
					event.setCancelled(true);
				}
				if (getClickedButton(menuUtility, cursor, clickedPos) == null)
					event.setCancelled(true);
			}
		}
	}
}
