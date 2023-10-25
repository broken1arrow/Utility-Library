package org.broken.arrow.itemcreator.library;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackCounters {
	/**
	 * Count the amount of matched items in two item arrys.
	 *
	 * @param itemStacks compere this array with items array
	 * @param items      compere this array with itemStacks array
	 * @return amount of simular items in the array.
	 */
	public static int countItemStacks(ItemStack[] itemStacks, ItemStack[] items) {
		int countItems = 0;

		for (ItemStack itemStack : itemStacks)
			for (ItemStack item : items)
				if (itemStack != null && itemStack.isSimilar(item) && !(itemStack.getType() == Material.AIR)) {
					countItems += itemStack.getAmount();
				}
		return countItems;
	}

	/**
	 * Count amount of stacks inside the inventry.
	 *
	 * @param inventoryItems the inventory you whant to check items.
	 * @param item           the item you whant to check for.
	 * @return amount of stacks.
	 */

	public static int countItemStacks(Inventory inventoryItems, ItemStack item) {
		int countItems = 0;
		for (ItemStack itemStack : inventoryItems.getContents())
			if (itemStack != null && itemStack.isSimilar(item) && !(itemStack.getType() == Material.AIR)) {
				countItems++;
			}
		return countItems;
	}

	/**
	 * Count amount of items inside the inventry.
	 *
	 * @param inventoryItems the inventory you whant to check items.
	 * @param item           the item you whant to check for.
	 * @return amount of items.
	 */
	public static int countItemStacks(ItemStack item, Inventory inventoryItems) {
		return countItemStacks(item, inventoryItems, false);
	}

	public static int countItemStacks(ItemStack item, Inventory inventoryItems, boolean onlyNoFullItems) {
		int countItems = 0;
		for (ItemStack itemStack : inventoryItems.getContents()) {
			if (onlyNoFullItems) {
				if (itemStack.getAmount() != itemStack.getMaxStackSize()) {
					if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
						countItems += itemStack.getAmount();
					}
				}
			} else if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
				countItems += itemStack.getAmount();
			}
		}
		return countItems;
	}

	/**
	 * Count amount of all items inside the inventry.
	 *
	 * @param inventoryItems the inventory you whant to check items.
	 * @param itemStacks     you whant to count.
	 * @return amount of items.
	 */
	public static int countItemStacks(ItemStack[] itemStacks, Inventory inventoryItems) {
		int countItems = 0;
		for (ItemStack itemStack : inventoryItems.getContents())
			for (ItemStack item : itemStacks)
				if (itemStack != null && itemStack.isSimilar(item) && !(itemStack.getType() == Material.AIR)) {
					countItems += itemStack.getAmount();
				}
		return countItems;
	}

	/**
	 * Count amount of all items inside one ItemStack array.
	 *
	 * @param itemStacks you whant to count.
	 * @return amount of items.
	 */
	public static int countItemStacks(ItemStack[] itemStacks) {
		int countItems = 0;
		for (ItemStack item : itemStacks)
			if (item != null && !(item.getType() == Material.AIR)) {
				countItems += item.getAmount();
			}
		return countItems;
	}

	public static boolean isPlaceLeftInventory(Inventory inventory, int amount, Material materialToMatch) {

		for (ItemStack item : inventory) {
			if (item != null) {
				if (item.getType() == materialToMatch && amount > countItemStacks(item, inventory, true)) {
					continue;
				}
				if (item.getType() == materialToMatch && amount + item.getAmount() <= item.getMaxStackSize()) {
					return true;
				}
			}
		}
		return false;
	}
}
