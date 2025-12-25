package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility class for creating and comparing {@link ItemStack} objects.
 * <p>
 * This class provides static helper methods to create item stacks with amount one,
 * count items in inventories, convert material names to {@link Material} types,
 * and check for item similarity including metadata and durability.
 * <p>
 * This is a utility class and should not be instantiated.
 */
public class ItemCreator {

	/**
	 * Private constructor to prevent instantiation.
	 * Throws an exception if called.
	 *
	 * @throws ValidateExceptions always thrown to prevent usage of constructor
	 */
	private ItemCreator() {
		throw new ValidateExceptions("should not use a constructor for a utility class");
	}

	/**
	 * Creates a new {@link ItemStack} from the given stack with the amount set to one.
	 * If the input stack is null or AIR, returns a new AIR {@link ItemStack}.
	 *
	 * @param stack the original {@link ItemStack} to clone
	 * @return a cloned {@link ItemStack} with amount 1, or AIR if input is null or AIR
	 */
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

	/**
	 * Counts the total amount of items similar to the given {@link ItemStack} in the given inventory.
	 *
	 * @param item           the {@link ItemStack} to compare against
	 * @param inventoryItems array of {@link ItemStack} representing the inventory
	 * @return the total count of similar items in the inventory
	 */
	public static int countItemStacks(ItemStack item, ItemStack[] inventoryItems) {
		return countItemStacks(item, inventoryItems, false);
	}

	/**
	 * Counts the total amount of items similar to the given {@link ItemStack} in the given inventory.
	 * Can optionally count only items that are not at full stack size.
	 *
	 * @param item              the {@link ItemStack} to compare against
	 * @param inventoryItems    array of {@link ItemStack} representing the inventory
	 * @param onlyNoFullItems   if true, only count items that are not at their max stack size
	 * @return the total count of similar items in the inventory, filtered if specified
	 */
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

	/**
	 * Converts a string name to a {@link Material} instance.
	 * Attempts to resolve using Bukkit's Material enum first,
	 * then uses the provided {@link RegisterMenuAPI} ItemCreator if enabled.
	 *
	 * @param registerMenuAPI the {@link RegisterMenuAPI} instance for fallback material resolution
	 * @param name            the string name of the material to convert (case-insensitive)
	 * @return the corresponding {@link Material}, or null if not found
	 */
	public static Material convertMaterialFromString(RegisterMenuAPI registerMenuAPI, final String name) {
		if (name == null) return null;
		Material material = Material.getMaterial(name.toUpperCase());
		if (material != null) {
			return material;
		} else {
			if (!registerMenuAPI.isNotFoundItemCreator()) {
				final org.broken.arrow.library.itemcreator.ItemCreator itemCreator = registerMenuAPI.getItemCreator();
				if (itemCreator != null)
					return itemCreator.of(name).makeItemStack().getType();
			}
		}
		return null;
	}

	/**
	 * Checks whether two {@link ItemStack} instances are similar.
	 * First checks Bukkit's {@code isSimilar}, then performs an extended similarity check including durability.
	 *
	 * @param item        the first {@link ItemStack} to compare
	 * @param clickedItem the second {@link ItemStack} to compare
	 * @return true if items are similar, false otherwise
	 */
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

	/**
	 * Performs a detailed similarity check between two {@link ItemStack} objects,
	 * comparing type, metadata, and durability.
	 *
	 * @param firstItem       the first {@link ItemStack}
	 * @param secondItemStack the second {@link ItemStack}
	 * @return true if items are considered similar, false otherwise
	 */
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

	/**
	 * Gets the durability (damage) value of an {@link ItemStack}, using the ItemMeta if available
	 * and the server version supports it.
	 *
	 * @param itemstack the {@link ItemStack} to get durability from
	 * @param itemMeta  the {@link ItemMeta} associated with the itemstack
	 * @return the durability value as a short
	 */
	public static short getDurability(final ItemStack itemstack, final ItemMeta itemMeta) {
		if (ServerVersion.atLeast(1.13))
			return (itemMeta == null) ? 0 : (short) ((Damageable) itemMeta).getDamage();
		return itemstack.getDurability();
	}
}
