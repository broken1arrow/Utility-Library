package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.color.TextTranslator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtilize {
    private ItemUtilize() {
    }

    public static List<String> translateColors(final List<String> rawLore) {
        final List<String> loreList = new ArrayList<>();
        for (final String lore : rawLore)
            loreList.add(translateHexCodes(lore));
        return loreList;
    }




    public static String translateColors(final String rawSingleLine) {
        return translateHexCodes(rawSingleLine);
    }

    private static String translateHexCodes(final String textTranslate) {
        return TextTranslator.toSpigotFormat(textTranslate);
    }


    public static ItemStack createItemStackAsOne(final Material material) {
        ItemStack itemstack = null;
        if (material != null)
            itemstack = new ItemStack(material);

        return createItemStackAsOne(itemstack != null ? itemstack : new ItemStack(Material.AIR));
    }

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

    public static ItemStack[] createItemStackAsOne(final ItemStack[] itemStacks) {
        ItemStack itemstack = null;
        if (itemStacks != null) {
            for (final ItemStack item : itemStacks)
                if (item.getType() != Material.AIR) {
                    itemstack = item.clone();
                    final ItemMeta meta = itemstack.getItemMeta();
                    itemstack.setItemMeta(meta);
                    itemstack.setAmount(1);
                    return new ItemStack[]{itemstack};
                }
        }
        return new ItemStack[]{new ItemStack(Material.AIR)};
    }

    public static ItemStack createItemStackWhitAmount(final Material material, final int amount) {
        ItemStack itemstack = null;
        if (material != null) {
            itemstack = new ItemStack(material);
            itemstack.setAmount(amount);
        }
        return itemstack != null ? itemstack : new ItemStack(Material.AIR);
    }


}
