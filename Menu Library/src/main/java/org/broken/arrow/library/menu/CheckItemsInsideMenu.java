package org.broken.arrow.library.menu;


import org.broken.arrow.library.menu.messages.SendMsgDuplicatedItems;
import org.broken.arrow.library.menu.utility.FilterMatch;
import org.broken.arrow.library.menu.utility.ItemCreator;
import org.broken.arrow.library.menu.utility.MatchCheckItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;


/**
 * The CheckItemsInsideMenu class provides methods to analyze and validate items added by a player to a menu.
 * It allows you to perform checks for blacklisted items, duplicates, or items added in quantities greater
 * than one. This class assists in ensuring that the menu's contents meet specific criteria or restrictions.
 */
public class CheckItemsInsideMenu {

	private final Map<UUID, Map<ItemStack, Integer>> duplicatedItems = new HashMap<>();
	private final RegisterMenuAPI registerMenuAPI;
	private FilterMatch filterMatch = FilterMatch.TYPE;
	private MatchCheckItemStack matchCheck =  new MatchCheckItemStack();
	private boolean sendMsgPlayer = false;
	private List<ItemStack> blacklistedItems;
	private final List<Integer> slotsToCheck = new ArrayList<>();

	private boolean checkDuplicates;

	public CheckItemsInsideMenu(RegisterMenuAPI registerMenuAPI) {
		this.registerMenuAPI = registerMenuAPI;
	}

	/**
	 * Set blacklisted items player not shall add to inventory/menu.
	 *
	 * @param blacklistedItems list of items some are not allowed.
	 */
	public void setBlacklistedItems(final List<ItemStack> blacklistedItems) {
		if (blacklistedItems == null) return;
		this.blacklistedItems = blacklistedItems;
	}

	/**
	 * Array of slots you want to check.
	 *
	 * @return list of slots it will check.
	 */

	public List<Integer> getSlotsToCheck() {
		return Collections.unmodifiableList(slotsToCheck);
	}

	/**
	 * You can't check slots outside inventory size.
	 *
	 * @param slotsToCheck slots you want to check.
	 */
	public void setSlotsToCheck(final int... slotsToCheck) {
		this.slotsToCheck.clear();
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
		this.slotsToCheck.clear();
		if (slotsToCheck != null)
			this.slotsToCheck.addAll(slotsToCheck);
	}

	/**
	 * Sets the type of match criteria for item comparisons. If none of the provided matches
	 * are satisfied, use the {@link #setMatchCheck(MatchCheckItemStack)} method to set your
	 * own custom condition in the match method.
	 *
	 * @param filterMatch the type of match to be used for item comparisons for matching the blacklist.
	 */
	public void setFilterMatch(@Nonnull FilterMatch filterMatch) {
		this.filterMatch = filterMatch;
	}

	/**
	 * Sets a custom match check for item comparisons. This method allows you to define
	 * your own condition for matching items if the default criteria do not suffice.
	 *
	 * @param checkItemStack an instance of class that extends {@code MatchCheckItemStack} with a custom match method.
	 */
	public void setMatchCheck(@Nonnull MatchCheckItemStack checkItemStack) {
		this.matchCheck = checkItemStack;
	}


	/**
	 * Checks the items inside the inventory and removes duplicate items if the shallCheckDuplicates parameter is set
	 * set to true. It will also give the rest back to the player or dropping them on the ground if the inventory is
	 * full. The {@link #getSlotsToCheck()} method will be used if it's not empty, otherwise, it will use the
	 * inventory size, except the bottom row.
	 *
	 * @param inv                  The inventory to check.
	 * @param player               The player using the menu/inventory.
	 * @param shallCheckDuplicates If true, the method checks for duplicate items and keeps only one of each.
	 *                             If false, all items in the specified or default slots will be returned in the map.
	 * @return A map containing the collected items from the list or all slots except the bottom row, with slot numbers as
	 * keys and the corresponding non-null ItemStacks as values.
	 */
	public Map<Integer, ItemStack> getItemsFromSetSlots(final Inventory inv, final Player player, final boolean shallCheckDuplicates) {
		return getItemsFromSetSlots(inv, player, null, shallCheckDuplicates);
	}

	/**
	 * Method to check items inside the inventory and remove items if there are more than one (giving
	 * the rest back to the player or dropping them on the ground if the inventory is full). The
	 * {@link #getSlotsToCheck()} method will be used if it's not empty, otherwise, it will use the
	 * inventory size, except the bottom row.
	 *
	 * @param inv    The inventory to check.
	 * @param player The player using the menu/inventory.
	 * @return A map containing the collected items from the list or all slots except the bottom row, with slot numbers as
	 * keys and the corresponding non-null ItemStacks as values.
	 */
	public Map<Integer, ItemStack> getItemsFromSetSlots(final Inventory inv, final Player player) {
		return getItemsFromSetSlots(inv, player, null, true);
	}

	/**
	 * Method to check items inside the inventory and remove items if there are more than one (giving
	 * the rest back to the player or dropping them on the ground if the inventory is full). The
	 * {@link #getSlotsToCheck()} method will be used if it's not empty, otherwise, it will use
	 * the inventory size, except the bottom row.
	 *
	 * @param inv      The inventory to check.
	 * @param player   The player using the menu/inventory.
	 * @param location If the player is offline or null, the location is used to point where items shall drop.
	 * @return A map containing the collected items from the list or all slots except the bottom row, with slot numbers as
	 * keys and the corresponding non-null ItemStacks as values.
	 */

	public Map<Integer, ItemStack> getItemsFromSetSlots(final Inventory inv, final Player player, final Location location) {
		return getItemsFromSetSlots(inv, player, location, true);
	}

	/**
	 * Checks the items inside the inventory and removes duplicate items if the shallCheckDuplicates parameter is set
	 * set to true. It will also give the rest back to the player or dropping them on the ground if the inventory is
	 * full. The {@link #getSlotsToCheck()} method will be used if it's not empty, otherwise, it will use the
	 * inventory size, except the bottom row.
	 *
	 * @param inv                  The inventory to check.
	 * @param player               The player using the menu/inventory.
	 * @param location             If the player is offline or null, the location is used to point where items shall drop.
	 * @param shallCheckDuplicates If true, the method checks for duplicate items and keeps only one of each.
	 *                             If false, all items in the specified or default slots will be returned in the map.
	 * @return A map containing the collected items from the list or all slots except the bottom row, with slot numbers as
	 * keys and the corresponding non-null ItemStacks as values.
	 */
	public Map<Integer, ItemStack> getItemsFromSetSlots(final Inventory inv, final Player player, final Location location, final boolean shallCheckDuplicates) {
		this.checkDuplicates = shallCheckDuplicates;
		final Map<Integer, ItemStack> inventoryItems = new HashMap<>();
		ItemStack[] itemStacks = inv.getContents();
		int inventorySize = this.getSlotsToCheck().isEmpty() ? inv.getSize() - 9 : inv.getSize();

		for (int slot = 0; slot < inventorySize; slot++) {
			if (!this.getSlotsToCheck().isEmpty() && !this.getSlotsToCheck().contains(slot)) {
				continue;
			}
			final ItemStack item = inv.getItem(slot);
			this.getInventoryItems(inventoryItems, slot, player, item);
			this.setToOneItem(inv, slot, item);
		}
		if (shallCheckDuplicates)
			return addToMuchItems(inventoryItems, player, itemStacks, location);
		else return inventoryItems;
	}

	public void getInventoryItems(final Map<Integer, ItemStack> items, final int slot, final Player player, ItemStack item) {
		if (item == null) return;

		ItemStack cloneItem = item.clone();
		if (checkItemAreOnBlacklist(cloneItem)) {
			addItemsBackToPlayer(player, cloneItem);
		} else {
			items.put(slot, !isAir(cloneItem.getType()) ? cloneItem : null);
		}

	}

	private Map<Integer, ItemStack> addToMuchItems(final Map<Integer, ItemStack> items, final Player player, final ItemStack[] itemStacks, final Location location) {
		final Map<Integer, ItemStack> itemStacksNoDoubleEntity = new HashMap<>();
		final Map<ItemStack, Integer> cachedDuplicatedItems = new HashMap<>();
		final Set<ItemStack> set = new HashSet<>();
		this.sendMsgPlayer = false;
		for (final Map.Entry<Integer, ItemStack> entity : items.entrySet()) {

			if (entity.getValue() != null) {

				if (entity.getValue().getAmount() > 1) {
					cachedDuplicatedItems.put(ItemCreator.createItemStackAsOne(entity.getValue()), (ItemCreator.countItemStacks(entity.getValue(), itemStacks)) - 1);
					duplicatedItems.put(player.getUniqueId(), cachedDuplicatedItems);
				}
				if (!set.add(ItemCreator.createItemStackAsOne(entity.getValue()))) {
					cachedDuplicatedItems.put(ItemCreator.createItemStackAsOne(entity.getValue()), (ItemCreator.countItemStacks(entity.getValue(), itemStacks)) - 1);
					duplicatedItems.put(player.getUniqueId(), cachedDuplicatedItems);
				} else {
					itemStacksNoDoubleEntity.put(entity.getKey(), ItemCreator.createItemStackAsOne(entity.getValue()));
				}
			}
		}
		addItemsBackToPlayer(location);
		return itemStacksNoDoubleEntity;
	}

	private void addItemsBackToPlayer(final Player player, final ItemStack itemStack) {

		final HashMap<Integer, ItemStack> ifInventorFull = player.getInventory().addItem(itemStack);
		if (!ifInventorFull.isEmpty() && player.getLocation().getWorld() != null)
			player.getLocation().getWorld().dropItemNaturally(player.getLocation(), ifInventorFull.get(0));

		if (!this.sendMsgPlayer) {
			this.registerMenuAPI.getMessages().sendBlacklistMessage(player, itemStack);
			this.sendMsgPlayer = true;
		}
	}

	private void addItemsBackToPlayer(final Location location) {

		for (final Entry<UUID, Map<ItemStack, Integer>> mapEntry : this.duplicatedItems.entrySet()) {
			for (final Map.Entry<ItemStack, Integer> items : mapEntry.getValue().entrySet()) {
				final ItemStack itemStack = items.getKey();
				final int amount = items.getValue();

				itemStack.setAmount(amount);
				final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(mapEntry.getKey());
				if (offlinePlayer.getPlayer() != null) {
					final HashMap<Integer, ItemStack> ifInventorFull = offlinePlayer.getPlayer().getInventory().addItem(itemStack);
					if (!ifInventorFull.isEmpty() && offlinePlayer.getPlayer().getLocation().getWorld() != null)
						offlinePlayer.getPlayer().getLocation().getWorld().dropItemNaturally(offlinePlayer.getPlayer().getLocation(), ifInventorFull.get(0));

					this.registerMenuAPI.getMessages().sendDuplicatedMessage(offlinePlayer.getPlayer(), new SendMsgDuplicatedItems.DuplicatedItemWrapper(itemStack, mapEntry.getValue().size(), amount));
				} else if (location != null && location.getWorld() != null)
					location.getWorld().dropItemNaturally(location, itemStack);
			}
			this.duplicatedItems.remove(mapEntry.getKey());
		}

	}

	private boolean checkItemAreOnBlacklist(final ItemStack itemStack) {
		final List<ItemStack> itemStacks = blacklistedItems;
		if (itemStack != null && itemStacks != null && !itemStacks.isEmpty())
			for (final ItemStack stack : itemStacks) {
				if (matchCheck.match(filterMatch , stack ,itemStack))
					return true;
			}
		return false;

	}

	public Material convertMaterialFromString(final String materialName) {
		if (materialName == null) return null;
		Material material = Material.getMaterial(materialName.toUpperCase());
		if (material != null) {
			return material;
		} else {
			if (!registerMenuAPI.isNotFoundItemCreator())
				return registerMenuAPI.getItemCreator().of(materialName).makeItemStack().getType();
		}
		return null;
	}

	public static boolean isAir(final Material material) {
		return nameEquals(material, "AIR", "CAVE_AIR", "VOID_AIR", "LEGACY_AIR");
	}

	public void setToOneItem(final Inventory inventory, final int slot, final ItemStack item) {
		if (checkDuplicates && item != null) {
			ItemStack clone = new ItemStack(item);
			clone.setAmount(1);
			inventory.setItem(slot, clone);
		}
	}

	private static boolean nameEquals(final Material mat, final String... names) {
		final String matName = mat.toString();

		for (final String name : names)
			if (matName.equals(name))
				return true;

		return false;
	}
}
