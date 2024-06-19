package org.broken.arrow.itemcreator.library.utility;

import com.google.common.base.Enums;
import org.broken.arrow.itemcreator.library.utility.matrials.Materials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Locale;

import static org.broken.arrow.itemcreator.library.utility.matrials.Materials.checkColor;

/**
 * This class convert object to itemStack (if the object can be translated), It also check if you
 * input string enum of a item. First check and translate it to right item depending
 * on minecraft version and then convert to material and last to itemStack.
 */
@SuppressWarnings("deprecation")
public class ConvertToItemStack {

    private final float serverVersion;

    public ConvertToItemStack(float serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * Check the object if it ether ItemStack,Material or String
     * last one need the name be same as the Material name
     * (upper case or not do not mater (this method convert it to upper case auto)).
     *
     * @param object of ether ItemStack,Material or String.
     * @return itemStack instance.
     */
    @Nullable
    public ItemStack checkItem(@Nullable final Object object) {
        if (object instanceof ItemStack)
            return (ItemStack) object;
        if (object instanceof Material)
            return new ItemStack((Material) object);
        if (object instanceof String) {
            final String stringName = ((String) object).toUpperCase(Locale.ROOT);
            return checkString(stringName, null);
        }
        return null;
    }

    /**
     * Check the object if it ether ItemStack,Material or String
     * last one need the name be same as the Material name
     * (upper case or not do not mater (this method convert it to upper case auto)).
     * <p>
     * This is a help method for older minecraft versions some not have easy way to use colors.
     * This option only work on items some can use colors.
     *
     * @param object of ether ItemStack,Material or String.
     * @param color  of your item (if it like glass,wool or concrete as example).
     * @return itemStack instance with your set values or null if the object is null or not ItemStack, Material or String.
     */
    @Nullable
    public ItemStack checkItem(@Nullable final Object object,@Nullable final String color) {
        return this.checkItem(object, (short) 0, color, null);
    }

    /**
     * Check the object if it ether ItemStack,Material or String
     * last one need the name be same as the Material name
     * (upper case or not do not mater (this method convert it to upper case auto)).
     * <p>
     * This is a help method for older minecraft versions some not have easy way to use colors.
     * This option only work on items some can use colors.
     *
     * @param object of ether ItemStack,Material or String.
     * @param damage the item damage, for older versions it also used to set data on some items.
     * @param color  of your item (if it like glass,wool or concrete as example).
     * @param data the the item data, should only be used for minecraft versions below 1.13.
     * @return itemStack instance with your set values or null if the object is null or not ItemStack, Material or String.
     */
    @Nullable
    public ItemStack checkItem(@Nullable final Object object, short damage,@Nullable final String color,@Nullable final Byte data) {
        ItemStack result = null;

        if (object == null) {
            return null;
        }

        String colorUpcast = color != null ? color.toUpperCase(Locale.ROOT) : "";
        if (color != null)
            damage = checkColor(colorUpcast);

        if (object instanceof ItemStack) {
            result = checkItemStack((ItemStack) object, damage);
        }
        if (object instanceof Material) {
            result = new ItemStack((Material) object, 1, damage, data);
        }
        if (object instanceof String) {
            String stringName = ((String) object).toUpperCase(Locale.ROOT);
            if (color != null || data != null) {
                result = checkString(stringName, data);
                if (result != null) {
                    result.setDurability(damage);
                }
            } else {
                Material material = Material.getMaterial(stringName);
                result = new ItemStack(material != null ? material : Material.AIR, 1);
            }
        }
        return result;
    }
    public ItemStack checkItemStack(final ItemStack itemStack, final short damage) {
        if (serverVersion < 13.0F && itemStack != null) {
            final ItemStack stack = new ItemStack(itemStack.getType(), itemStack.getAmount(), damage);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null)
                stack.setItemMeta(itemMeta);
            return stack;
        }
        return itemStack;
    }

    public ItemStack checkString(final String stringName, Byte data) {
        if (serverVersion < 13.0F) {
            final ItemStack stack = createStack(stringName, 1, data);
            if (stack != null)
                return stack;
        }
        return new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName));
    }

    /**
     * This method check the item name and convert item name from 1.13+ to 1.12 and older versions item names.
     *
     * @param item   the 1.13+ item name.
     * @param amount the amount you want to create.
     * @return ItemStack with the amount or null.
     */
    @Nullable
    public ItemStack createStack(final String item, int amount, Byte data) {
        if (amount <= 0)
            amount = 1;
        return Materials.getItemStack(item, amount, data);
    }

}
