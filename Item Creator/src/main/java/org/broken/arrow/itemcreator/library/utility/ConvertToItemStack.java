package org.broken.arrow.itemcreator.library.utility;

import com.google.common.base.Enums;
import org.broken.arrow.itemcreator.library.utility.matrials.Materials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Locale;

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
	public ItemStack checkItem(final Object object) {
		if (object instanceof ItemStack)
			return (ItemStack) object;
		else if (object instanceof Material)
			return new ItemStack((Material) object);
		else if (object instanceof String) {
			final String stringName = ((String) object).toUpperCase(Locale.ROOT);
			return checkString(stringName);
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
	 * @return itemStack instance with your set values.
	 */
	public ItemStack checkItem(final Object object, String color) {
		color = color.toUpperCase(Locale.ROOT);
		final short colorNumber;
		if (object instanceof ItemStack)
			return checkItemStack((ItemStack) object, color);
		else if (object instanceof Material) {
			colorNumber = checkColor(color);
			if (colorNumber > 0)
				return new ItemStack((Material) object, 1, colorNumber);
			return new ItemStack((Material) object, 1);
		} else if (object instanceof String) {
			final String stringName = ((String) object).toUpperCase(Locale.ROOT);
			colorNumber = checkColor(color);
			if (colorNumber > 0)
				return new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName), 1, colorNumber);
			return new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName), 1);
		}
		return null;
	}

	public ItemStack checkItemStack(final ItemStack itemStack, final String color) {
		if (serverVersion < 13.0F && itemStack != null) {
			final ItemStack stack = new ItemStack(itemStack.getType(), itemStack.getAmount(), checkColor(color));
			final ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta != null)
				stack.setItemMeta(itemMeta);
			return stack;
		}
		return itemStack;
	}

	public ItemStack checkString(final String stringName) {
		if (serverVersion < 13.0F) {
			final ItemStack stack = createStack(stringName, 1);
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
	public ItemStack createStack(final String item, int amount) {
		if (amount <= 0)
			amount = 1;
		return Materials.getItemStack(item,  amount);
	}

	public short checkColor(String color) {
		int end;
		if (color.startsWith("LIGHT")) {
			end = color.indexOf("_S");
			if (end < 0)
				end = color.indexOf("_G");
			if (end < 0)
				end = color.indexOf("_P");
			if (end < 0)
				end = color.indexOf("_C");
			if (end < 0)
				end = color.indexOf("_W");
		} else
			end = color.indexOf('_');
		if (end < 0)
			end = color.length();
		color = color.substring(0, end);

		switch (color) {
			case "WHITE":
				return 0;
			case "ORANGE":
				return 1;
			case "MAGENTA":
				return 2;
			case "LIGHT_BLUE":
				return 3;
			case "YELLOW":
				return 4;
			case "LIME":
				return 5;
			case "PINK":
				return 6;
			case "GRAY":
				return 7;
			case "LIGHT_GRAY":
				return 8;
			case "CYAN":
				return 9;
			case "PURPLE":
				return 10;
			case "BLUE":
				return 11;
			case "BROWN":
				return 12;
			case "GREEN":
				return 13;
			case "RED":
				return 14;
			case "BLACK":
				return 15;
			default:
				return -1;
		}
	}
}
