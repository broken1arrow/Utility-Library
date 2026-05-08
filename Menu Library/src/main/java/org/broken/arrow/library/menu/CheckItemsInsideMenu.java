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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * The CheckItemsInsideMenu class provides methods to analyze and validate items added by a player to a menu.
 * It allows you to perform checks for blacklisted items, duplicates, or items added in quantities greater
 * than one. This class assists in ensuring that the menu's contents meet specific criteria or restrictions.
 */
public class CheckItemsInsideMenu {

    private final Map<UUID, Map<ItemStack, Integer>> duplicatedItems = new HashMap<>();
    private final RegisterMenuAPI registerMenuAPI;
    private FilterMatch filterMatch = FilterMatch.TYPE;
    private MatchCheckItemStack matchCheck = new MatchCheckItemStack();
    private boolean sendMsgPlayer = false;
    private List<ItemStack> blacklistedItems;
    private final List<Integer> slotsToCheck = new ArrayList<>();

    private boolean checkDuplicates;

    /**
     * Creates a new instance of CheckItemsInsideMenu.
     *
     * @param registerMenuAPI The RegisterMenuAPI instance used to send messages and interact with item creation.
     */
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

    /**
     * Adds an item to the given map if it is not blacklisted.
     * If the item is blacklisted, it is returned back to the player.
     *
     * @param items  The map collecting items with slot indices as keys.
     * @param slot   The slot index of the item.
     * @param player The player using the menu/inventory.
     * @param item   The ItemStack to check and add.
     */
    public void getInventoryItems(final Map<Integer, ItemStack> items, final int slot, final Player player, ItemStack item) {
        if (item == null) return;

        ItemStack cloneItem = item.clone();
        if (checkItemAreOnBlacklist(cloneItem)) {
            addItemsBackToPlayer(player, cloneItem);
        } else {
            items.put(slot, !isAir(cloneItem.getType()) ? cloneItem : null);
        }

    }

    /**
     * Removes extra quantities of items greater than one and duplicate items,
     * returning unique items and handling returning or dropping extras.
     *
     * @param items      Map of slot indices and items found.
     * @param player     The player using the menu/inventory.
     * @param itemStacks All items in the inventory.
     * @param location   Location to drop items if player is offline or inventory is full.
     * @return Map of slot indices and corresponding unique ItemStacks with quantity set to one.
     */
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

    /**
     * Returns the given item stack back to the player's inventory, or drops it at the player's location if inventory is full.
     * Also sends a blacklist message once per call if an item was rejected.
     *
     * @param player    The player to return the item to.
     * @param itemStack The item stack to return.
     */
    private void addItemsBackToPlayer(final Player player, final ItemStack itemStack) {

        final HashMap<Integer, ItemStack> ifInventorFull = player.getInventory().addItem(itemStack);
        if (!ifInventorFull.isEmpty() && player.getLocation().getWorld() != null)
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), ifInventorFull.get(0));

        if (!this.sendMsgPlayer) {
            this.registerMenuAPI.getMessages().sendBlacklistMessage(player, itemStack);
            this.sendMsgPlayer = true;
        }
    }

    /**
     * Drops back all items currently stored in the duplicated items map for all players.
     *
     * @param location the location to drop the items if it could not add it to the player.
     */
    private void addItemsBackToPlayer(final Location location) {
        if (duplicatedItems.isEmpty()) return;
        final Iterator<Entry<UUID, Map<ItemStack, Integer>>> iterator = duplicatedItems.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<UUID, Map<ItemStack, Integer>> mapEntry = iterator.next();
            for (final Map.Entry<ItemStack, Integer> items : mapEntry.getValue().entrySet()) {
                final ItemStack itemStack = items.getKey();
                final int amount = items.getValue();

                itemStack.setAmount(amount);
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(mapEntry.getKey());
                final Player player = offlinePlayer.getPlayer();
                if (player != null) {
                    final HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);
                    Location playerLocation = player.getLocation();
                    if (!overflow.isEmpty() && playerLocation.getWorld() != null) {
                        overflow.values().forEach(item ->
                                playerLocation.getWorld().dropItemNaturally(playerLocation, item));
                    }
                    this.registerMenuAPI.getMessages().sendDuplicatedMessage(player, new SendMsgDuplicatedItems.DuplicatedItemWrapper(itemStack, mapEntry.getValue().size(), amount));
                } else if (location != null && location.getWorld() != null) {
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
            iterator.remove();
        }
    }

    /**
     * Checks whether the given item is blacklisted.
     *
     * @param itemStack ItemStack to check.
     * @return true if the item is blacklisted, false otherwise.
     */
    private boolean checkItemAreOnBlacklist(final ItemStack itemStack) {
        final List<ItemStack> itemStacks = blacklistedItems;
        if (itemStack != null && itemStacks != null && !itemStacks.isEmpty())
            for (final ItemStack stack : itemStacks) {
                if (matchCheck.match(filterMatch, stack, itemStack))
                    return true;
            }
        return false;

    }

    /**
     * convert items from string to material.
     *
     * @param materialName the enum name for the material
     * @return the material from the enum name or null if it could not find it.
     */
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

    /**
     * Checks if a material is AIR.
     *
     * @param material The material to check.
     * @return true if material is AIR, false otherwise.
     */
    public static boolean isAir(final Material material) {
        return nameEquals(material, "AIR", "CAVE_AIR", "VOID_AIR", "LEGACY_AIR");
    }

    /**
     * Sets the amount of the item in the slot to exactly one.
     *
     * @param inventory The inventory.
     * @param slot      The slot to update.
     * @param item      The ItemStack currently in the slot.
     */
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

    /**
     * Collects and groups {@link ItemStack}s by their {@link Material}.
     *
     * <p>Each material entry contains a {@link MaterialOverflowBatch}, which further groups
     * item variants (based on full {@link ItemStack} metadata).
     *
     * <p>This batch is designed for scenarios where only a single instance of each
     * item variant should be retained, while additional occurrences are treated as overflow.
     *
     * <p>The first occurrence of an item variant is considered the "retained" item,
     * and is therefore not included in the stored amounts. Any additional occurrences
     * are accumulated and can be processed separately (e.g. returned, dropped, or stored).
     */
    public static class ItemOverflowBatch {
        private final Map<Material, MaterialOverflowBatch> items = new HashMap<>();

        /**
         * Returns an unmodifiable view of the batched items grouped by material.
         *
         * @return map of material to its corresponding batch
         */
        public Map<Material, Map<ItemStack, Integer>>  getItems() {
            return items.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getItems()
                    ));
        }

        /**
         * Adds an item stack to this batch.
         *
         * <p>Items with the same metadata are merged, and their amounts are accumulated.</p>
         *
         * <p>Each insertion treats the first item of a new metadata group as the
         * "retained" item. Only the remaining amount is stored as overflow.</p>
         *
         * This method groups items by metadata and handles overflow automatically.
         *
         * @param itemStack the item to add (will be normalized to amount = 1 internally)
         */
        public void putItem(@Nonnull final ItemStack itemStack) {
            items.computeIfAbsent(itemStack.getType(), material -> new MaterialOverflowBatch())
                    .putItem(ItemCreator.createItemStackAsOne(itemStack), itemStack.getAmount());
        }

    }

    /**
     * Represents a batch of item variants for a specific {@link Material}.
     *
     * <p>Each entry maps a normalized {@link ItemStack} (amount = 1) to the number
     * of additional occurrences beyond the first one.
     *
     * <p>This means:
     * <ul>
     *   <li>The first occurrence of a variant is excluded</li>
     *   <li>Only overflow (duplicates) are stored</li>
     * </ul>
     *
     * <p>This is useful for enforcing "one item per variant" rules while still
     * tracking and handling any excess items.
     */
    public static class MaterialOverflowBatch {
        private final Map<ItemStack, Integer> items = new HashMap<>();

        /**
         * Returns an unmodifiable view of the item variants in this batch.
         *
         * @return map of item variants to their amounts
         */
        public Map<ItemStack, Integer> getItems() {
            return Collections.unmodifiableMap(items);
        }
        /**
         * Adds an item stack to this batch.
         *
         * <p>Items with the same metadata are merged, and their amounts are accumulated.</p>
         *
         * <p>The first insertion for a given item variant excludes one item (the retained item),
         * and only the remaining amount is stored as overflow. Subsequent insertions contribute
         * their full amounts.</p>
         *
         * <p>Example:</p>
         * <ul>
         *   <li>First insert with amount = 5 → stores 4</li>
         *   <li>Second insert with amount = 3 → stores 7</li>
         * </ul>
         *
         * @param itemStack the item to add (will be normalized to amount = 1)
         * @param amount the overflow amount derived from the item stack size
         */
        public void putItem(@Nonnull final ItemStack itemStack,final int amount) {
            final ItemStack stackAsOne = ItemCreator.createItemStackAsOne(itemStack);
            items.compute(stackAsOne, (key, currentAmount) -> {
                if (currentAmount == null) {
                    int overflow = amount - 1;
                    return overflow > 0 ? overflow : null;
                }
                return currentAmount + amount;
            });
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MaterialOverflowBatch that = (MaterialOverflowBatch) o;
            return Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(items);
        }

        @Override
        public String toString() {
            return "MaterialOverflowBatch{" +
                    "items=" + items +
                    '}';
        }
    }

}
