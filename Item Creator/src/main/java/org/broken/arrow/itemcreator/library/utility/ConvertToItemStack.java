package org.broken.arrow.itemcreator.library.utility;

import com.google.common.base.Enums;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This class convert object to itemstack (if the object can be translated), It also check if you
 * input string enum of a item. First check and translate it to right item depending
 * on minecraft version and then convert to material and last to itemstack.
 */
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
	 * Check the object if it ether ItemStack,Material or String
	 * last one need the name be same as the Material name
	 * (upper case or not do not mater (this method convert it to upper case auto)).
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
		ItemStack fish = this.getFish(item, amount);
		if (fish != null)
			return fish;
		final int color = checkColor(item);
		if (item.endsWith("STAINED_GLASS_PANE")) {
			Material material = Material.getMaterial("STAINED_GLASS_PANE");
			if (material != null)
				return new ItemStack(material, amount, (short) color);
		}
		if (item.endsWith("STAINED_GLASS")) {
			Material material = Material.getMaterial("STAINED_GLASS");
			if (material != null)
				return new ItemStack(material, amount, (short) color);
		}
		if (item.endsWith("_WOOL")) {
			Material material = Material.getMaterial("WOOL");
			if (material != null)
				return new ItemStack(material, amount, (short) color);
		}
		if (item.endsWith("_CARPET")) {
			Material material = Material.getMaterial("CARPET");
			if (material != null)
				return new ItemStack(material, amount, (short) color);
		}
		if (item.equals("GRASS_BLOCK")) {
			Material material = Material.getMaterial("GRASS");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("FIREWORK_STAR")) {
			Material material = Material.getMaterial("FIREWORK_CHARGE");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("FIRE_CHARGE")) {
			Material material = Material.getMaterial("FIREBALL");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.endsWith("_SPAWN_EGG")) {
			return getSpawnEgg(item, amount);
		}
		if (item.equals("SPAWNER")) {
			Material material = Material.getMaterial("MOB_SPAWNER");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (serverVersion > 11.0F) {
			if (item.contains("CONCRETE_POWDER")) {
				Material material = Material.getMaterial("CONCRETE_POWDER");
				if (material != null)
					return new ItemStack(material, amount, (short) color);
			}
			if (item.endsWith("_CONCRETE")) {
				Material material = Material.getMaterial("CONCRETE");
				if (material != null)
					return new ItemStack(material, amount, (short) color);
			}
		}
		if ((item.endsWith("_TERRACOTTA") || item.endsWith("_STAINED_CLAY")) && !item.endsWith("GLAZED_TERRACOTTA")) {
			Material material = Material.getMaterial("STAINED_CLAY");
			if (material != null)
				return new ItemStack(material, amount, (short) color);
		}
		if (item.equals("ENCHANTING_TABLE")) {
			Material enchantment_table = Material.getMaterial("ENCHANTMENT_TABLE");
			if (enchantment_table != null)
				return new ItemStack(enchantment_table, amount);
		}
		if (item.equals("TERRACOTTA")) {
			Material material = Material.getMaterial("HARD_CLAY");
			if (material != null)
				return new ItemStack(material, amount, (short) 0);
		}
		if (item.equals("ENDER_EYE")) {
			Material material = Material.getMaterial("ENDER_PEARL");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("CRACKED_STONE_BRICKS")) {
			Material material = Material.getMaterial("SMOOTH_BRICK");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("SMOOTH_STONE")) {
			Material material = Material.getMaterial("STEP");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("COCOA_BEANS") || item.endsWith("_DYE") || item.equals("BONE_MEAL") || item.equals("INK_SACK")) {
			Material material = Material.getMaterial("INK_SACK");
			if (material != null) {
				return new ItemStack(material, amount, this.getDye(item));
			}
		}
		if (item.equals("SMOOTH_STONE_SLAB")) {
			Material material = Material.getMaterial("STEP");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.startsWith("GOLDEN_")) {
			String goldTool = item.substring(item.indexOf("_"));
			Material material = Material.getMaterial("GOLD" + goldTool);
			if (material == null) {
				String tool = goldTool;
				if (tool.equals("_SHOVEL"))
					tool = "_SPADE";
				material = Material.getMaterial("GOLD" + tool);
			}
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("CLOCK")) {
			Material material = Material.getMaterial("WATCH");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("CRAFTING_TABLE")) {
			Material material = Material.getMaterial("WORKBENCH");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (item.equals("PLAYER_HEAD")) {
			Material material = Material.getMaterial("SKULL_ITEM");
			if (material != null)
				return new ItemStack(material, amount);
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
	 * @param itemName the material name from 1.13+ versions.
	 * @param amount   the amount of items you want to make.
	 * @return itemstack or null.
	 */
	@Nullable
	public ItemStack checkAndGetWood(final String itemName, final int amount) {
		if (itemName == null) return null;
		ItemStack itemStack = null;
		if (itemName.equals("OAK_FENCE")) {
			final Material material = Material.getMaterial("FENCE");
			if (material != null)
				return new ItemStack(material, amount);
		}
		if (itemName.equals("OAK_FENCE_GATE")) {
			final Material material = Material.getMaterial("FENCE_GATE");
			if (material != null)
				return new ItemStack(material, amount);
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
		short stoneType = getStoneTypeData(itemName);
		if (stoneType == -1)
			return new ItemStack(material, amount);
		if (stoneType >= 0)
			return new ItemStack(material, amount, stoneType);
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

	public ItemStack getFish(String item, int amount) {
		if (item.equals("COD")) {
			return new ItemStack(Material.valueOf("RAW_FISH"), amount, (short) 0);
		}
		if (item.equals("SALMON")) {
			return new ItemStack(Material.valueOf("RAW_FISH"), amount, (short) 1);
		}
		if (item.equals("TROPICAL_FISH")) {
			return new ItemStack(Material.valueOf("RAW_FISH"), amount, (short) 2);
		}
		if (item.equals("PUFFERFISH")) {
			return new ItemStack(Material.valueOf("RAW_FISH"), amount, (short) 3);
		}
		if (item.equals("COOKED_COD")) {
			return new ItemStack(Material.valueOf("COOKED_FISH"), amount, (short) 0);
		}
		if (item.equals("COOKED_SALMON")) {
			return new ItemStack(Material.valueOf("COOKED_FISH"), amount, (short) 1);
		}
		return null;
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

	public short getDye(final String itemName) {

		if (itemName.equals("INK_SACK")) {
			return 0;
		}
		if (itemName.equals("BONE_MEAL")) {
			return 15;
		}
		if (itemName.equals("COCOA_BEANS")) {
			return 3;
		}
		int lastIndex = itemName.lastIndexOf("_");
		if (lastIndex < 0) return 15;

		String color = itemName.substring(0, lastIndex);
		switch (color) {
			case "WHITE":
				return 15;
			case "ORANGE":
				return 14;
			case "MAGENTA":
				return 13;
			case "LIGHT_BLUE":
				return 12;
			case "YELLOW":
				return 11;
			case "LIME":
				return 10;
			case "PINK":
				return 9;
			case "GRAY":
				return 8;
			case "LIGHT_GRAY":
				return 7;
			case "CYAN":
				return 6;
			case "PURPLE":
				return 5;
			case "BLUE":
				return 4;
			case "BROWN":
				return 3;
			case "GREEN":
				return 2;
			case "RED":
				return 1;
			case "BLACK":
				return 0;
			default:
				return 15;
		}
	}

	public ItemStack getSpawnEgg(String itemName, int amount) {
		Material material = Material.getMaterial("MONSTER_EGG");
		if (material == null) return null;
		ItemStack itemStack = new ItemStack(material, amount);
		SpawnEgg spawnEgg = (SpawnEgg) itemStack.getData();
		spawnEgg.setSpawnedType(EntityType.SHEEP);

		if (itemName.startsWith("CREEPER_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.CREEPER);
		}
		if (itemName.startsWith("SKELETON_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SKELETON);
		}
		if (itemName.startsWith("SPIDER_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SPIDER);
		}
		if (itemName.startsWith("GIANT_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.GIANT);
		}
		if (itemName.startsWith("ZOMBIE_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.ZOMBIE);
		}
		if (itemName.startsWith("SLIME_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SLIME);
		}
		if (itemName.startsWith("GHAST_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SLIME);
		}
		if (itemName.startsWith("ZOMBIFIED_PIGLIN_SPAWN") || itemName.startsWith("ZOMBIE_PIGMAN_SPAWN")) {
			EntityType entityType = EntityType.fromName("PIG_ZOMBIE");
			if (entityType != null)
				spawnEgg.setSpawnedType(entityType);
		}
		if (itemName.startsWith("ENDERMAN_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.ENDERMAN);
		}
		if (itemName.startsWith("CAVE_SPIDER_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.CAVE_SPIDER);
		}
		if (itemName.startsWith("SILVERFISH_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SILVERFISH);
		}
		if (itemName.startsWith("BLAZE_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.BLAZE);
		}
		if (itemName.startsWith("MAGMA_CUBE_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.MAGMA_CUBE);
		}
		if (itemName.startsWith("ENDER_DRAGON_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.ENDER_DRAGON);
		}
		if (itemName.startsWith("WITHER_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.WITHER);
		}
		if (itemName.startsWith("BAT_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.BAT);
		}
		if (itemName.startsWith("WITCH_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.BAT);
		}
		if (itemName.startsWith("ENDERMITE_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.ENDERMITE);
		}
		if (itemName.startsWith("GUARDIAN_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.GUARDIAN);
		}
		if (itemName.startsWith("PIG_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.PIG);
		}
		if (itemName.startsWith("COW_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.COW);
		}
		if (itemName.startsWith("CHICKEN_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.CHICKEN);
		}
		if (itemName.startsWith("SQUID_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SQUID);
		}
		if (itemName.startsWith("WOLF_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.WOLF);
		}
		if (itemName.startsWith("MOOSHROOM_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.MUSHROOM_COW);
		}
		if (itemName.startsWith("SNOW_GOLEM_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.SNOWMAN);
		}
		if (itemName.startsWith("OCELOT_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.OCELOT);
		}
		if (itemName.startsWith("CAT_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.CAT);
		}
		if (itemName.startsWith("IRON_GOLEM_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.IRON_GOLEM);
		}
		if (itemName.startsWith("HORSE_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.HORSE);
		}
		if (itemName.startsWith("RABBIT_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.RABBIT);
		}
		if (itemName.startsWith("VILLAGER_SPAWN")) {
			spawnEgg.setSpawnedType(EntityType.VILLAGER);
		}
		return spawnEgg.toItemStack(amount);
	}

}
