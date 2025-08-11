package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.color.TextTranslator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with {@link ItemStack} objects and color translations.
 * <p>
 * This class provides helper methods to:
 * <ul>
 *     <li>Translate color codes (including hex codes) in strings and lists of strings.</li>
 *     <li>Create single-amount {@link ItemStack} objects from {@link Material} or existing stacks.</li>
 *     <li>Safely handle {@link Material#AIR} or {@code null} values when creating items.</li>
 * </ul>
 * <p>
 * This class is non-instantiable and all methods are static.
 */
public class ItemUtilize {
    private ItemUtilize() {
    }

    /**
     * Translates all color codes, including hex codes, in a list of lore strings.
     *
     * @param rawLore the list of raw lore lines to translate; may not be null
     * @return a new list containing the translated lore lines
     */
    public static List<String> translateColors(final List<String> rawLore) {
        final List<String> loreList = new ArrayList<>();
        for (final String lore : rawLore)
            loreList.add(translateHexCodes(lore));
        return loreList;
    }

    /**
     * Translates all color codes, including hex codes, in a single string.
     *
     * @param rawSingleLine the raw string to translate; may not be null
     * @return the translated string
     */
    public static String translateColors(final String rawSingleLine) {
        return translateHexCodes(rawSingleLine);
    }

    /**
     * Converts color codes in the given string to Spigot-compatible formatting.
     *
     * @param textTranslate the text containing color codes; may not be null
     * @return the formatted string
     */
    private static String translateHexCodes(final String textTranslate) {
        return TextTranslator.toSpigotFormat(textTranslate);
    }

    /**
     * Creates a single-amount {@link ItemStack} from the given material.
     * If the material is null, an {@link Material#AIR} item will be returned.
     *
     * @param material the material to create the item from
     * @return a single-amount item stack of the given material, or AIR if null
     */
    public static ItemStack createItemStackAsOne(final Material material) {
        ItemStack itemstack = null;
        if (material != null)
            itemstack = new ItemStack(material);

        return createItemStackAsOne(itemstack != null ? itemstack : new ItemStack(Material.AIR));
    }

    /**
     * Creates a single-amount {@link ItemStack} from an existing item stack.
     * If the input is null or AIR, an {@link Material#AIR} item will be returned.
     *
     * @param itemStack the item stack to clone
     * @return a single-amount clone of the given item stack, or AIR if null or AIR
     */
    public static ItemStack createItemStackAsOne(final ItemStack itemStack) {
        ItemStack itemstack = null;
        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
            itemstack = itemStack.clone();
            final ItemMeta meta = itemstack.getItemMeta();
            itemstack.setItemMeta(meta);
            itemstack.setAmount(1);
        }
        return itemstack != null ? itemstack : new ItemStack(Material.AIR);
    }

    /**
     * Creates a single-amount {@link ItemStack} from the first non-AIR item in the array.
     * If all items are null or AIR, an AIR stack is returned.
     *
     * @param itemStacks the array of item stacks to search through
     * @return an array containing a single-amount clone of the first non-AIR item found,
     * or an array containing AIR if none are found
     */
    public static ItemStack[] createItemStackAsOne(final ItemStack[] itemStacks) {
        if (itemStacks != null) {
            for (final ItemStack item : itemStacks)
                if (item.getType() != Material.AIR) {
                    final ItemStack itemstack = item.clone();
                    final ItemMeta meta = itemstack.getItemMeta();
                    itemstack.setItemMeta(meta);
                    itemstack.setAmount(1);
                    return new ItemStack[]{itemstack};
                }
        }
        return new ItemStack[]{new ItemStack(Material.AIR)};
    }

    /**
     * Creates an {@link ItemStack} with a specified material and amount.
     * If the material is null, an AIR item will be returned.
     *
     * @param material the material for the item
     * @param amount   the amount of the item stack
     * @return the created item stack, or AIR if material is null
     */
    public static ItemStack createItemStackWhitAmount(final Material material, final int amount) {
        ItemStack itemstack = null;
        if (material != null) {
            itemstack = new ItemStack(material);
            itemstack.setAmount(amount);
        }
        return itemstack != null ? itemstack : new ItemStack(Material.AIR);
    }


}
