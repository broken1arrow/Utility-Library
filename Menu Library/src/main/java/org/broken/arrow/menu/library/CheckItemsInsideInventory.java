package org.broken.arrow.menu.library;


import org.broken.arrow.menu.library.messages.SendMsgDuplicatedItems;
import org.broken.arrow.menu.library.utility.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Simple check if player add items some are ether blacklisted or add more an 1 item or duplicated item.
 */

public class CheckItemsInsideInventory {

	//todo fix this to only create on instance? and add player to cache.
	private final Map<UUID, Map<ItemStack, Integer>> duplicatedItems = new HashMap<>();
	private boolean sendMsgPlayer = false;
	private final List<Material> blacklistedItems = new ArrayList<>();
	private final List<Integer> slotsToCheck = new ArrayList<>();
	private final RegisterMenuAPI registerMenuAPI;

	public CheckItemsInsideInventory(RegisterMenuAPI registerMenuAPI) {
		this.registerMenuAPI = registerMenuAPI;
	}

	/**
	 * set blacklisted items player not shall add to inventory/menu.
	 *
	 * @param blacklistedItems list of items some are not allowed.
	 */
	public void setBlacklistedItems(final List<String> blacklistedItems) {
		if (blacklistedItems == null) return;
		for (String item : blacklistedItems) {
			Material material = convertString(item);
			this.blacklistedItems.add(material);
		}
	}

	/**
	 * Array of slots you want to check.
	 *
	 * @return list of slots it will check.
	 */

	public List<Integer> getSlotsToCheck() {
		return slotsToCheck;
	}

	/**
	 * You can't check slots outside inventory size.
	 *
	 * @param slotsToCheck slots you want to check.
	 */
	public void setSlotsToCheck(final int... slotsToCheck) {
		if (slotsToCheck != null)
			for (final int slot : slotsToCheck)
				this.slotsToCheck.add(slot);
	}

	/**
	 * You can't check slots outside inventory size.
	 *
	 * @param slotsToCheck slots you want to check.
	 */
	public void setSlotsToCheck(final List<Integer> slotsToCheck) {
		if (slotsToCheck != null)
			this.slotsToCheck.addAll(slotsToCheck);
	}

	/**
	 * Method to check items inside inventory and remove items it it more
	 * an 1 (giv rest back to player or drop it on grund if inventory is full).
	 * Will use {@link #getSlotsToCheck()}  method will be used if it's not empty,
	 * otherwise it will return all items inside the inventory, except the bottom row.
	 *
	 * @param inv                  inventory you want to check
	 * @param player               player some use menu/inventory
	 * @param shallCheckDuplicates if it shall check if added items are dublicates or more 1 one item.
	 * @return all items add in menu on the specified or defult slots, but only 1 of each if shallCheckDuplicates is true.
	 */
	public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(final Inventory inv, final Player player, final boolean shallCheckDuplicates) {
		return getItemsOnSpecifiedSlots(inv, player, null, shallCheckDuplicates);
	}

	/**
	 * Method to check items inside inventory and remove items it it more
	 * an 1 (giv rest back to player or drop it on grund if inventory is full).
	 * Will use {@link #getSlotsToCheck()}  method will be used if it's not empty,
	 * otherwise it will return all items inside the inventory, except the bottom row.
	 *
	 * @param inv    inventory you want to check
	 * @param player player some use menu/inventory
	 * @return all items add in menu on the specified or defult slots, but only 1 of each if shallCheckDuplicates is true.
	 */
	public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(final Inventory inv, final Player player) {
		return getItemsOnSpecifiedSlots(inv, player, null, true);
	}

	/**
	 * Method to check items inside inventory and remove items it it more
	 * an 1 (giv rest back to player or drop it on grund if inventory is full).
	 * Will use {@link #getSlotsToCheck()}  method will be used if it's not empty,
	 * otherwise it will return all items inside the inventory, except the bottom row.
	 *
	 * @param inv      inventory you want to check
	 * @param player   player some use menu/inventory
	 * @param location if player is offline or null, can you return a location where items shall drop.
	 * @return all items add in menu on the specified or defult slots, but only 1 of each if shallCheckDuplicates is true.
	 */
	public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(final Inventory inv, final Player player, final Location location) {
		return getItemsOnSpecifiedSlots(inv, player, location, true);
	}

	/**
	 * Method to check items inside inventory and remove items it it more
	 * an 1 (giv rest back to player or drop it on grund if inventory is full).
	 * Will use {@link #getSlotsToCheck()}  method will be used if it's not empty,
	 * otherwise it will return all items inside the inventory, except the bottom row.
	 *
	 * @param inv                  inventory you want to check
	 * @param player               player some use menu/inventory
	 * @param location             if player is offline or null, can you return a location where items shall drop.
	 * @param shallCheckDuplicates if it shall check if added items are dublicates or more 1 one item.
	 * @return all items add in menu on the specified or defult slots, but only 1 of each if shallCheckDuplicates is true.
	 */

	public Map<Integer, ItemStack> getItemsOnSpecifiedSlots(final Inventory inv, final Player player, final Location location, final boolean shallCheckDuplicates) {
		final Map<Integer, ItemStack> items = new HashMap<>();
		if (!this.getSlotsToCheck().isEmpty()) {
			for (final int slot : this.getSlotsToCheck()) {
				getInventoryItems(items, inv, player, slot);
			}
		} else {
			for (int i = 0; i < inv.getSize() - 9; i++) {
				getInventoryItems(items, inv, player, i);
			}
		}
		if (shallCheckDuplicates)
			return addToMuchItems(items, player, inv, location);
		else return items;
	}

	public Map<Integer, ItemStack> getInventoryItems(final Map<Integer, ItemStack> items, final Inventory inv, final Player player, final int slot) {
		if (slot > inv.getSize()) return items;
		final ItemStack item = inv.getItem(slot);
		if (chekItemAreOnBlacklist(item)) {
			addItemsBackToPlayer(player, item);
		} else {
			items.put(slot, item != null && !isAir(item.getType()) ? item : null);
		}
		return items;
	}

	private Map<Integer, ItemStack> addToMuchItems(final Map<Integer, ItemStack> items, final Player player, final Inventory inventory, final Location location) {
		final Map<Integer, ItemStack> itemStacksNoDubbleEntity = new HashMap<>();
		final Map<ItemStack, Integer> chachedDuplicatedItems = new HashMap<>();
		final Set<ItemStack> set = new HashSet<>();
		this.sendMsgPlayer = false;
		for (final Map.Entry<Integer, ItemStack> entitys : items.entrySet()) {

			if (entitys.getValue() != null) {

				if (entitys.getValue().getAmount() > 1) {
					chachedDuplicatedItems.put(ItemCreator.createItemStackAsOne(entitys.getValue()), (ItemCreator.countItemStacks(entitys.getValue(), inventory)) - 1);
					duplicatedItems.put(player.getUniqueId(), chachedDuplicatedItems);
				}
				if (!set.add(ItemCreator.createItemStackAsOne(entitys.getValue()))) {
					chachedDuplicatedItems.put(ItemCreator.createItemStackAsOne(entitys.getValue()), (ItemCreator.countItemStacks(entitys.getValue(), inventory)) - 1);
					duplicatedItems.put(player.getUniqueId(), chachedDuplicatedItems);
				} else {
					itemStacksNoDubbleEntity.put(entitys.getKey(), ItemCreator.createItemStackAsOne(entitys.getValue()));
				}
			}
		}
		addItemsBackToPlayer(location);
		return itemStacksNoDubbleEntity;
	}

	private void addItemsBackToPlayer(final Player player, final ItemStack itemStack) {

		final HashMap<Integer, ItemStack> ifInventorFull = player.getInventory().addItem(itemStack);
		if (!ifInventorFull.isEmpty() && player.getLocation().getWorld() != null)
			player.getLocation().getWorld().dropItemNaturally(player.getLocation(), ifInventorFull.get(0));

		if (!this.sendMsgPlayer) {
			SendMsgDuplicatedItems.sendBlacklistMessage(player, itemStack.getType().name().toLowerCase());
			this.sendMsgPlayer = true;
		}
	}

	private void addItemsBackToPlayer(final Location location) {

		for (final UUID playerUUID : this.duplicatedItems.keySet()) {
			for (final Map.Entry<ItemStack, Integer> items : duplicatedItems.get(playerUUID).entrySet()) {
				final ItemStack itemStack = items.getKey();
				final int amount = items.getValue();

				itemStack.setAmount(amount);
				final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
				if (offlinePlayer.getPlayer() != null) {
					final HashMap<Integer, ItemStack> ifInventorFull = offlinePlayer.getPlayer().getInventory().addItem(itemStack);
					if (!ifInventorFull.isEmpty() && offlinePlayer.getPlayer().getLocation().getWorld() != null)
						offlinePlayer.getPlayer().getLocation().getWorld().dropItemNaturally(offlinePlayer.getPlayer().getLocation(), ifInventorFull.get(0));

					SendMsgDuplicatedItems.sendDublicatedMessage(offlinePlayer.getPlayer(), itemStack.getType(), duplicatedItems.size(), amount);
				} else if (location != null && location.getWorld() != null)
					location.getWorld().dropItemNaturally(location, itemStack);
			}
			this.duplicatedItems.remove(playerUUID);
		}

	}

	private boolean chekItemAreOnBlacklist(final ItemStack itemStack) {
		final List<Material> itemStacks = blacklistedItems;
		if (itemStack != null && !itemStacks.isEmpty())
			for (final Material item : itemStacks) {
				if (item == itemStack.getType() && new ItemStack(item).isSimilar(itemStack))
					return true;
			}
		return false;

	}

	public Material convertString(final String name) {
		if (name == null) return null;
		Material material = Material.getMaterial(name.toUpperCase());
		if (material != null) {
			return material;
		} else {
			if (!registerMenuAPI.isNotFoundItemCreator())
				return registerMenuAPI.getItemCreator().of(name).makeItemStack().getType();
		}
		return null;
	}

	public static boolean isAir(final Material material) {
		return nameEquals(material, "AIR", "CAVE_AIR", "VOID_AIR", "LEGACY_AIR");
	}

	private static boolean nameEquals(final Material mat, final String... names) {
		final String matName = mat.toString();

		for (final String name : names)
			if (matName.equals(name))
				return true;

		return false;
	}
}
