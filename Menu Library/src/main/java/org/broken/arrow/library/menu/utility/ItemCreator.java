package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCreator {

	private ItemCreator() {
		throw new ValidateExceptions("should not use a constructor for a utility class");
	}
	public static ItemStack createItemStackAsOne(final ItemStack stack) {
		ItemStack itemstack = null;
		if (stack != null && !stack.getType().equals(Material.AIR)) {
			itemstack = stack.clone();
			final ItemMeta meta = itemstack.getItemMeta();
			itemstack.setItemMeta(meta);
			itemstack.setAmount(1);
		}
		return itemstack != null ? itemstack : new ItemStack(Material.AIR);
	}

	public static int countItemStacks(ItemStack item, ItemStack[] inventoryItems) {
		return countItemStacks(item, inventoryItems, false);
	}

	public static int countItemStacks(ItemStack item, ItemStack[] inventoryItems, boolean onlyNoFullItems) {
		int countItems = 0;
		for (ItemStack itemStack : inventoryItems) {
			if (onlyNoFullItems) {
				if (itemStack != null && itemStack.getAmount() != itemStack.getMaxStackSize() &&
						itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
						countItems += itemStack.getAmount();
				}
			} else if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
				countItems += itemStack.getAmount();
			}
		}
		return countItems;
	}

	public static Material convertMaterialFromString(RegisterMenuAPI registerMenuAPI, final String name) {
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

	public static boolean  isItemSimilar(final ItemStack item, final ItemStack clickedItem) {
		if (item != null && clickedItem != null) {
			if (item.isSimilar(clickedItem)) {
				return true;
			} else {
				return itemIsSimilar(item, clickedItem);
			}
		}
		return false;
	}

	public static boolean itemIsSimilar(final ItemStack firstItem, final ItemStack secondItemStack) {

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

	public static short getDurability(final ItemStack itemstack, final ItemMeta itemMeta) {
		if (ServerVersion.atLeast(ServerVersion.V1_13))
			return (itemMeta == null) ? 0 : (short) ((Damageable) itemMeta).getDamage();
		return itemstack.getDurability();
	}
}
