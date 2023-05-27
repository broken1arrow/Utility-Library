package org.broken.arrow.menu.library;

import de.tr7zw.changeme.nbtapi.metodes.RegisterNbtAPI;
import org.broken.arrow.menu.library.Item.CreateItemStack;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButtonI;
import org.broken.arrow.menu.library.cache.MenuCache;
import org.brokenarrow.menu.library.utility.ServerVersion;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static org.brokenarrow.menu.library.utility.Metadata.*;
import static org.brokenarrow.menu.library.utility.ServerVersion.setServerVersion;
import static org.brokenarrow.menu.library.utility.ServerVersion.v1_19_4;

public class RegisterMenuAPI {

	private static Plugin PLUGIN;
	private static RegisterNbtAPI nbtApi;

	private RegisterMenuAPI() {
		throw new UnsupportedOperationException("You need specify your main class");
	}

	public RegisterMenuAPI(final Plugin plugin) {
		PLUGIN = plugin;
		if (PLUGIN == null) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "You have not set plugin, becuse plugin is null");
			return;
		}
		setServerVersion(plugin);
		versionCheck();
		registerMenuEvent(plugin);
		nbtApi = new RegisterNbtAPI(plugin, false);
	}

	private void versionCheck() {
		PLUGIN.getLogger().log(Level.INFO, "Now starting MenuApi. Any errors will be shown below.");
		if (ServerVersion.newerThan(v1_19_4)) {
			PLUGIN.getLogger().log(Level.WARNING, "It is not tested on versions beyond 1.19.4");
		}
	}

	public static void getLogger(final Level level, final String messsage) {
		PLUGIN.getLogger().log(level, messsage);
	}

	public static Plugin getPLUGIN() {
		return PLUGIN;
	}

	private void registerMenuEvent(final Plugin plugin) {
		final MenuHolderListener menuHolderListener = new MenuHolderListener();
		//if (!getRegisteredListeners(plugin).stream().allMatch(registeredListener -> registeredListener.getListener().getClass().equals(menuHolderListener.getClass())))
		Bukkit.getPluginManager().registerEvents(menuHolderListener, plugin);

	}

	public static RegisterNbtAPI getNbtApi() {
		return nbtApi;
	}

	private static class MenuHolderListener implements Listener {

		private final MenuCache menuCache = MenuCache.getInstance();
		private final Map<UUID, SwapData> cacheData = new HashMap<>();

		@EventHandler(priority = EventPriority.LOW)
		public void onMenuClicking(final InventoryClickEvent event) {
			final Player player = (Player) event.getWhoClicked();

			if (event.getClickedInventory() == null)
				return;
			ItemStack clickedItem = event.getCurrentItem();
			final ItemStack cursor = event.getCursor();

			final MenuUtility<?> menuUtility = getMenuHolder(player);

			if (menuUtility == null) return;

			if (!event.getView().getTopInventory().equals(menuUtility.getMenu())) return;

			if (!menuUtility.isAddedButtonsCacheEmpty()) {
				final int clickedSlot = event.getSlot();
				final int clickedPos = menuUtility.getSlot(clickedSlot);

				if (!menuUtility.isAllowShiftClick() && event.getClick().isShiftClick()) {
					event.setCancelled(true);
					return;
				}
				if (menuUtility.isSlotsYouCanAddItems()) {
					if (menuUtility.getFillSpace().contains(clickedPos))
						return;
					else if (event.getClickedInventory().getType() != InventoryType.PLAYER)
						event.setCancelled(true);
				} else {
					if (event.getClickedInventory().getType() == InventoryType.PLAYER)
						if (event.getClick().isShiftClick()) {
							event.setCancelled(true);
						} else
							event.setCancelled(true);
					if (cursor != null && cursor.getType() != Material.AIR)
						event.setCancelled(true);
				}
				final MenuButtonI<?> menuButton = getClickedButton(menuUtility, clickedItem, clickedPos);
				if (menuButton != null) {
					event.setCancelled(true);
					final Object object = menuUtility.getObjectFromList(clickedPos);
					final Object objectData = object != null && !object.equals("") ? object : clickedItem;
					if (clickedItem == null)
						clickedItem = CreateItemStack.of("AIR").makeItemStack();
					menuUtility.onClick(menuButton, player, clickedPos, event.getClick(), clickedItem);

					if (ServerVersion.newerThan(ServerVersion.v1_15) && event.getClick() == ClickType.SWAP_OFFHAND) {
						final SwapData data = cacheData.get(player.getUniqueId());
						ItemStack item = null;
						if (data != null) {
							item = data.getItemInOfBeforeOpenMenuHand();
						}
						cacheData.put(player.getUniqueId(), new SwapData(true, item));
					}
				}
			}
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
				if (hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
					removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
				}
				if (hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
					if (menuUtility.isAutoClearCache()) {
						if (menuUtility.getAmountOfViewers() < 1) {
							menuCache.removeMenuCached(getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION));
						}
					}
					removePlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
				}
			}
		}

		@EventHandler(priority = EventPriority.LOW)
		public void onInventoryDragTop(final InventoryDragEvent event) {
			final Player player = (Player) event.getWhoClicked();
			if (event.getView().getType() == InventoryType.PLAYER) return;

			final MenuUtility menuUtility = getMenuHolder(player);
			if (menuUtility == null) return;
			if (menuUtility.getMenu() == null) return;

			if (!menuUtility.isAddedButtonsCacheEmpty()) {
				final int size = event.getView().getTopInventory().getSize();

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


		public MenuButtonI<?> getClickedButton(final MenuUtility<?> menusData, final ItemStack item, final int clickedPos) {
			final MenuDataUtility menuData = menusData.getMenuData(menusData.getPageNumber());
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

			if (hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
				menukey = getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION);
			}

			final MenuUtility<?> menuUtility;
			if (hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
				menuUtility = getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
			} else {
				menuUtility = menuCache.getMenuInCache(menukey);
			}
			return menuUtility;
		}

		private static class SwapData {

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
