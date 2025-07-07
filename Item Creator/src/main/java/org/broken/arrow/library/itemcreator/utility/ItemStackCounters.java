package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackCounters {
    private ItemStackCounters() {
    }

    /**
     * Counts the total amount of matching items between two item arrays.
     *
     * @param itemStacks the first array of item stacks to compare
     * @param items      the second array of item stacks to compare against
     * @return the total amount of similar items in both arrays
     */
    public static int countItemStacks(ItemStack[] itemStacks, ItemStack[] items) {
        int countItems = 0;

        for (ItemStack itemStack : itemStacks)
            for (ItemStack item : items)
                if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
                    countItems += itemStack.getAmount();
                }
        return countItems;
    }

    /**
     * Counts the number of item stacks in the inventory that match the given item.
     *
     * @param inventory the inventory to search
     * @param item      the item to compare against
     * @return the number of matching stacks
     */
    public static int countItemStacks(Inventory inventory, ItemStack item) {
        int countItems = 0;
        for (ItemStack itemStack : inventory.getContents())
            if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
                countItems++;
            }
        return countItems;
    }

    /**
     * Counts the total amount of a specific item inside the inventory.
     *
     * @param inventoryItems the inventory to search
     * @param item           the item to count
     * @return the total amount of matching items
     */
    public static int countItemStacks(ItemStack item, Inventory inventoryItems) {
        return countItemStacks(item, inventoryItems, false);
    }

    /**
     * Counts the total amount of a specific item inside the inventory.
     * Optionally ignores full stacks.
     *
     * @param item            the item to count
     * @param inventory       the inventory to search
     * @param onlyNoFullItems if {@code true}, only counts non-full stacks
     * @return the total amount of matching items
     */
    public static int countItemStacks(ItemStack item, Inventory inventory, boolean onlyNoFullItems) {
        int countItems = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (onlyNoFullItems) {
                if (itemStack != null && itemStack.getType() != Material.AIR
                        && itemStack.getAmount() != itemStack.getMaxStackSize()
                        && itemStack.isSimilar(item)) {
                    countItems += itemStack.getAmount();

                }
            } else if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
                countItems += itemStack.getAmount();
            }
        }
        return countItems;
    }

    /**
     * Counts the total amount of all given items inside the inventory.
     *
     * @param inventoryItems the inventory to search
     * @param itemStacks     the items to match against
     * @return the total amount of matching items
     */
    public static int countItemStacks(ItemStack[] itemStacks, Inventory inventoryItems) {
        int countItems = 0;
        for (ItemStack itemStack : inventoryItems.getContents())
            for (ItemStack item : itemStacks)
                if (itemStack != null && itemStack.isSimilar(item) && itemStack.getType() != Material.AIR) {
                    countItems += itemStack.getAmount();
                }
        return countItems;
    }

    /**
     * Counts the total amount of items in the given item stack array.
     *
     * @param itemStacks the array of item stacks to count
     * @return the total amount of items
     */
    public static int countItemStacks(ItemStack[] itemStacks) {
        int countItems = 0;
        for (ItemStack item : itemStacks)
            if (item != null && item.getType() != Material.AIR) {
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
