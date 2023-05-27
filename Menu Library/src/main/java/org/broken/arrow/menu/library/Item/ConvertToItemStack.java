package org.broken.arrow.menu.library.Item;

import com.google.common.base.Enums;
import org.brokenarrow.menu.library.utility.ServerVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This class convert object to itemstack (if the object can be translated), It also check if you
 * input string enum of a item. First check and translate it to right item depending
 * on minecraft version and then convert to matrial and last to itemstack.
 */
public class ConvertToItemStack {

	protected ConvertToItemStack() {
	}

	/**
	 * Check the objekt if it ether ItemStack,Material or String
	 * last one need the name be same as the Material name
	 * (upper case or not do not mater (this method convert it to upper case auto)).
	 *
	 * @param object of ether ItemStack,Material or String.
	 * @return itemstack.
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
	 * Check the objekt if it ether ItemStack,Material or String
	 * last one need the name be same as the Material name
	 * (upper case or not do not mater (this method convert it to upper case auto)).
	 * <p>
	 * <p>
	 * This is a help method for older minecraft versions some not have easy way to use colors.
	 * This option only work on items some can use colors.
	 *
	 * @param object of ether ItemStack,Material or String.
	 * @param color  of your item (if it like glass,wool or concrete as example).
	 * @return itemstack.
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
		if (ServerVersion.olderThan(ServerVersion.v1_13) && itemStack != null) {
			final ItemStack stack = new ItemStack(itemStack.getType(), itemStack.getAmount(), checkColor(color));
			final ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta != null)
				stack.setItemMeta(itemMeta);
			return stack;
		}
		return itemStack;
	}

	public ItemStack checkString(final String stringName) {
		if (ServerVersion.olderThan(ServerVersion.v1_13)) {
			final ItemStack stack = createStack(stringName, 1);
			if (stack != null)
				return stack;
		}
		return new ItemStack(Enums.getIfPresent(Material.class, stringName).orNull() == null ? Material.AIR : Material.valueOf(stringName));
	}

	/**
	 * This method check the itemname and convert item name from 1.13+ to 1.12 and older versions item names.
	 *
	 * @param item   the 1.13+ item name.
	 * @param amount the amount you want to create.
	 * @return Itemstack with the amount or null.
	 */
	@Nullable
	public ItemStack createStack(final String item, int amount) {
		if (amount <= 0)
			amount = 1;
		final int color = checkColor(item);
		if (item.endsWith("STAINED_GLASS_PANE")) {
			return new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), amount, (short) color);
		}
		if (item.endsWith("STAINED_GLASS")) {
			return new ItemStack(Material.valueOf("STAINED_GLASS"), amount, (short) color);
		}
		if (item.endsWith("_WOOL")) {
			return new ItemStack(Material.valueOf("WOOL"), amount, (short) color);
		}
		if (item.endsWith("_CARPET")) {
			return new ItemStack(Material.valueOf("CARPET"), amount, (short) color);
		}
		if (ServerVersion.newerThan(ServerVersion.v1_8)) {
			if (item.contains("CONCRETE_POWDER")) {
				return new ItemStack(Material.valueOf("CONCRETE_POWDER"), amount, (short) color);
			}

			if (item.endsWith("_CONCRETE")) {
				return new ItemStack(Material.valueOf("CONCRETE"), amount, (short) color);
			}
		}
		if ((item.endsWith("_TERRACOTTA") || item.endsWith("_STAINED_CLAY")) && !item.endsWith("GLAZED_TERRACOTTA")) {
			return new ItemStack(Material.valueOf("STAINED_CLAY"), amount, (short) color);
		}

		if (item.equals("TERRACOTTA")) {
			return new ItemStack(Material.valueOf("HARD_CLAY"), amount, (short) 0);
		}
		if (item.equals("ENDER_EYE")) {
			return new ItemStack(Material.valueOf("ENDER_PEARL"), amount);
		}
		if (item.equals("CRACKED_STONE_BRICKS")) {
			return new ItemStack(Material.valueOf("SMOOTH_BRICK"), amount);
		}
		if (item.equals("SMOOTH_STONE")) {
			return new ItemStack(Material.valueOf("STEP"), amount);
		}
		if (item.equals("SMOOTH_STONE_SLAB")) {
			return new ItemStack(Material.valueOf("STEP"), amount);
		}
		if (item.startsWith("GOLDEN_")) {
			final Material material = Material.getMaterial("GOLD" + item.substring(item.indexOf("_")));
			return new ItemStack(material == null ? Material.AIR : material, amount);
		}
		if (item.equals("CLOCK")) {
			return new ItemStack(Material.valueOf("WATCH"), amount);
		}
		if (item.equals("CRAFTING_TABLE")) {
			return new ItemStack(Material.valueOf("WORKBENCH"), amount);
		}
		if (item.equals("PLAYER_HEAD")) {
			return new ItemStack(Material.valueOf("SKULL_ITEM"), amount);
		}
		if (item.contains("ANDESITE") || item.contains("DIORITE") || item.contains("GRANITE")) {
			return getStoneTypes(Material.STONE, item, amount);
		}
		if (item.equals("CHARCOAL")) {
			return new ItemStack(Material.valueOf("COAL"), amount, (short) 1);
		} else {
			Material material = null;
			if (!item.contains("_DOOR"))
				material = Material.getMaterial(item);
			if (material != null && color != -1)
				return new ItemStack(material, amount, (short) color);
			else if (material != null)
				return new ItemStack(material, amount);
			else
				return checkAndGetWood(item, amount);
		}
	}

	/**
	 * Check the type of wood you want to get.
	 *
	 * @param itemName the matrial name from 1.13+ versions.
	 * @param amount   the amount of items you want to make.
	 * @return itemstack or null.
	 */
	@Nullable
	public ItemStack checkAndGetWood(final String itemName, final int amount) {
		if (itemName == null) return null;
		ItemStack itemStack = null;
		if (itemName.equals("OAK_FENCE")) {
			return new ItemStack(Material.valueOf("FENCE"), amount);
		}
		if (itemName.equals("OAK_FENCE_GATE")) {
			return new ItemStack(Material.valueOf("FENCE_GATE"), amount);
		}

		if (itemName.contains("_PLANKS")) {
			final Material material = Material.getMaterial("WOOD");
			itemStack = getWoodItemStack(material, itemName, amount);
		}
		if (itemName.contains("_LOG")) {
			Material material = Material.getMaterial("LOG");
			if (material == null) return null;
			short woodTypeData = getWoodTypeData(itemName);
			if (woodTypeData >= 0) {
				if (woodTypeData == 4) {
					material = Material.getMaterial("LOG_2");
					woodTypeData = 0;
				}
				if (woodTypeData == 5) {
					material = Material.getMaterial("LOG_2");
					woodTypeData = 1;
				}
				itemStack = getWoodItemStack(material, woodTypeData, itemName, amount);
			}
		}
		if (itemName.contains("_SLAB")) {
			final Material material = Material.getMaterial("WOOD_STEP");
			itemStack = getWoodItemStack(material, itemName, amount);
		}
		if (itemName.contains("_STAIRS")) {
			final short woodTypeData = getWoodTypeData(itemName);
			Material material = null;

			if (woodTypeData == 0)
				material = Material.getMaterial("WOOD_STAIRS");
			if (woodTypeData == 1)
				material = Material.getMaterial("SPRUCE_WOOD_STAIRS");
			if (woodTypeData == 2)
				material = Material.getMaterial("BIRCH_WOOD_STAIRS");
			if (woodTypeData == 3)
				material = Material.getMaterial("JUNGLE_WOOD_STAIRS");
			if (woodTypeData == 4)
				material = Material.getMaterial("ACACIA_STAIRS");
			if (woodTypeData == 5)
				material = Material.getMaterial("DARK_OAK_STAIRS");

			itemStack = getWoodItemStack(material, woodTypeData, itemName, amount);
		}
		if (itemName.contains("_DOOR")) {
			final short woodTypeData = getWoodTypeData(itemName);
			Material material = null;
			if (woodTypeData > 0) {
				material = Material.getMaterial(itemName + "_ITEM");
			}
			if (woodTypeData == 0) {
				material = Material.getMaterial("WOOD_DOOR");
			}
			itemStack = getWoodItemStack(material, (short) 0, itemName, amount);
		}
		if (itemName.contains("_BUTTON")) {
			final Material material = Material.getMaterial("WOOD_BUTTON");
			itemStack = getWoodItemStack(material, itemName, amount);
		}
		if (itemName.contains("_LEAVES")) {
			Material material = Material.getMaterial("LEAVES");
			short woodTypeData = getWoodTypeData(itemName);
			if (woodTypeData >= 0) {
				if (woodTypeData == 4) {
					material = Material.getMaterial("LEAVES_2");
					woodTypeData = 0;
				}
				if (woodTypeData == 5) {
					material = Material.getMaterial("LEAVES_2");
					woodTypeData = 1;
				}
				itemStack = getWoodItemStack(material, woodTypeData, itemName, amount);
			}
		}
		if (itemName.contains("_SAPLING")) {
			final Material material = Material.getMaterial("SAPLING");
			itemStack = getWoodItemStack(material, itemName, amount);
		}
		if (itemName.endsWith("_SIGN")) {
			final Material material = Material.getMaterial("SIGN");
			itemStack = getWoodItemStack(material, (short) 0, itemName, amount);
		}

		if (itemStack != null)
			return itemStack;
		final Material material = Material.getMaterial(itemName);
		if (material != null) return new ItemStack(material, amount);
		return null;
	}

	public ItemStack getStoneTypes(final Material material, final String itemName, final int amount) {
		if (material == null) return null;
		short stonetype = getStoneTypeData(itemName);
		if (stonetype == -1)
			return new ItemStack(material, amount);
		if (stonetype >= 0)
			return new ItemStack(material, amount, stonetype);
		return null;
	}

	public ItemStack getWoodItemStack(final Material material, final String itemName, final int amount) {
		return getWoodItemStack(material, (short) -1, itemName, amount);
	}

	public ItemStack getWoodItemStack(final Material material, short woodTypeData, final String itemName, final int amount) {
		if (material == null) return null;
		if (woodTypeData == -1)
			woodTypeData = getWoodTypeData(itemName);
		if (woodTypeData >= 0)
			return new ItemStack(material, amount, woodTypeData);
		return null;
	}

	public short getStoneTypeData(final String itemName) {
		if (itemName.equals("GRANITE")) {
			return 1;
		}
		if (itemName.equals("POLISHED_GRANITE")) {
			return 2;
		}
		if (itemName.equals("DIORITE")) {
			return 3;
		}
		if (itemName.equals("POLISHED_DIORITE")) {
			return 4;
		}
		if (itemName.equals("ANDESITE")) {
			return 5;
		}
		if (itemName.equals("POLISHED_ANDESITE")) {
			return 6;
		}
		return -1;
	}

	public short getWoodTypeData(final String itemName) {
		if (itemName.startsWith("DARK_OAK_")) {
			return 5;
		}
		if (itemName.startsWith("OAK_")) {
			return 0;
		}
		if (itemName.startsWith("SPRUCE_")) {
			return 1;
		}
		if (itemName.startsWith("BIRCH_")) {
			return 2;
		}
		if (itemName.startsWith("JUNGLE_")) {
			return 3;
		}
		if (itemName.startsWith("ACACIA_")) {
			return 4;
		}
		return -1;
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

		if (color.equals("WHITE"))
			return 0;
		if (color.equals("ORANGE"))
			return 1;
		if (color.equals("MAGENTA"))
			return 2;
		if (color.equals("LIGHT_BLUE"))
			return 3;
		if (color.equals("YELLOW"))
			return 4;
		if (color.equals("LIME"))
			return 5;
		if (color.equals("PINK"))
			return 6;
		if (color.equals("GRAY"))
			return 7;
		if (color.equals("LIGHT_GRAY"))
			return 8;
		if (color.equals("CYAN"))
			return 9;
		if (color.equals("PURPLE"))
			return 10;
		if (color.equals("BLUE"))
			return 11;
		if (color.equals("BROWN"))
			return 12;
		if (color.equals("GREEN"))
			return 13;
		if (color.equals("RED"))
			return 14;
		if (color.equals("BLACK"))
			return 15;
		return -1;
	}
}
