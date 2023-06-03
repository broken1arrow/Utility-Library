package org.broken.arrow.menu.library.utility;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCreator {

	public static ItemStack createItemStackAsOne(final ItemStack itemstacks) {
		ItemStack itemstack = null;
		if (itemstacks != null && !itemstacks.getType().equals(Material.AIR)) {
			itemstack = itemstacks.clone();
			final ItemMeta meta = itemstack.getItemMeta();
			itemstack.setItemMeta(meta);
			itemstack.setAmount(1);
		}
		return itemstack != null ? itemstack : new ItemStack(Material.AIR);
	}

	public static int countItemStacks(ItemStack item, Inventory inventoryItems) {
		return countItemStacks(item, inventoryItems, false);
	}

	public static int countItemStacks(ItemStack item, Inventory inventoryItems, boolean onlyNoFullItems) {
		int countItems = 0;
		for (ItemStack itemStack : inventoryItems.getContents()) {
			if (onlyNoFullItems) {
				if (!(itemStack.getAmount() == itemStack.getMaxStackSize())) {
					if (itemStack != null && itemStack.isSimilar(item) && !(itemStack.getType() == Material.AIR)) {
						countItems += itemStack.getAmount();
					}
				}
			} else if (itemStack != null && itemStack.isSimilar(item) && !(itemStack.getType() == Material.AIR)) {
				countItems += itemStack.getAmount();
			}
		}
		return countItems;
	}
}
