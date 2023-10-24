package org.broken.arrow.itemcreator.library.utility.matrials;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public enum Materials {

	WHITE_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 0)),
	ORANGE_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 1)),
	MAGENTA_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 2)),
	LIGHT_BLUE_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 3)),
	YELLOW_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 4)),
	LIME_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 5)),
	PINK_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 6)),
	GRAY_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 7)),
	LIGHT_GRAY_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 8)),
	CYAN_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 9)),
	PURPLE_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 10)),
	BLUE_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 11)),
	BROWN_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 12)),
	GREEN_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 13)),
	RED_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 14)),
	BLACK_STAINED_GLASS_PANE(createStack("STAINED_GLASS_PANE", (short) 15)),

	WHITE_STAINED_GLASS(createStack("STAINED_GLASS", (short) 0)),
	ORANGE_STAINED_GLASS(createStack("STAINED_GLASS", (short) 1)),
	MAGENTA_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 2)),
	LIGHT_BLUE_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 3)),
	YELLOW_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 4)),
	LIME_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 5)),
	PINK_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 6)),
	GRAY_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 7)),
	LIGHT_GRAY_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 8)),
	CYAN_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 9)),
	PURPLE_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 10)),
	BLUE_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 11)),
	BROWN_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 12)),
	GREEN_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 13)),
	RED_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 14)),
	BLACK_STAINED_GLASS(createStack("STAINED_GLASS_PANE", (short) 15)),

	WHITE_WOOL(createStack( "WOOL","WHITE")),
	ORANGE_WOOL(createStack( "WOOL","ORANGE")),
	MAGENTA_WOOL(createStack("WOOL","MAGENTA")),
	LIGHT_BLUE_WOOL(createStack( "WOOL","LIGHT_BLUE")),
	YELLOW_WOOL(createStack( "WOOL","YELLOW")),
	LIME_WOOL(createStack("WOOL","LIME")),
	PINK_WOOL(createStack( "WOOL","PINK")),
	GRAY_WOOL(createStack("WOOL","GRAY")),
	LIGHT_GRAY_WOOL(createStack("WOOL","LIGHT_GRAY")),
	CYAN_WOOL(createStack("WOOL","CYAN")),
	PURPLE_WOOL(createStack("WOOL","PURPLE" )),
	BLUE_WOOL(createStack("WOOL","BLUE")),
	BROWN_WOOL(createStack("WOOL","BROWN")),
	GREEN_WOOL(createStack("WOOL","GREEN")),
	RED_WOOL(createStack("WOOL","RED")),
	BLACK_WOOL(createStack("WOOL","BLACK")),

	WHITE_CARPET(createStack("CARPET","WHITE")),
	ORANGE_CARPET(createStack("CARPET","ORANGE")),
	MAGENTA_CARPET(createStack("CARPET","MAGENTA")),
	LIGHT_BLUE_CARPET(createStack("CARPET","LIGHT_BLUE")),
	YELLOW_CARPET(createStack("CARPET","YELLOW")),
	LIME_CARPET(createStack("CARPET","LIME")),
	PINK_CARPET(createStack("CARPET","PINK")),
	GRAY_CARPET(createStack("CARPET","GRAY")),
	LIGHT_GRAY_CARPET(createStack("CARPET","LIGHT_GRAY")),
	CYAN_CARPET(createStack("CARPET","CYAN")),
	PURPLE_CARPET(createStack("CARPET","PURPLE")),
	BLUE_CARPET(createStack("CARPET","BLUE")),
	BROWN_CARPET(createStack("CARPET","BROWN")),
	GREEN_CARPET(createStack("CARPET","GREEN")),
	RED_CARPET(createStack("CARPET","RED")),
	BLACK_CARPET(createStack("CARPET","BLACK")),

	WHITE_CONCRETE(createStack("CONCRETE","WHITE")),
	ORANGE_CONCRETE(createStack("CONCRETE","ORANGE")),
	MAGENTA_CONCRETE(createStack("CONCRETE","MAGENTA")),
	LIGHT_BLUE_CONCRETE(createStack("CONCRETE","LIGHT_BLUE")),
	YELLOW_CONCRETE(createStack("CONCRETE","YELLOW")),
	LIME_CONCRETE(createStack("CONCRETE","LIME")),
	PINK_CONCRETE(createStack("CONCRETE","PINK")),
	GRAY_CONCRETE(createStack("CONCRETE","GRAY")),
	LIGHT_GRAY_CONCRETE(createStack("CONCRETE","LIGHT_GRAY")),
	CYAN_CONCRETE(createStack("CONCRETE","CYAN")),
	PURPLE_CONCRETE(createStack("CONCRETE","PURPLE")),
	BLUE_CONCRETE(createStack("CONCRETE","BLUE")),
	BROWN_CONCRETE(createStack("CONCRETE","BROWN")),
	GREEN_CONCRETE(createStack("CONCRETE","GREEN")),
	RED_CONCRETE(createStack("CONCRETE","RED")),
	BLACK_CONCRETE(createStack("CONCRETE","BLACK")),

	WHITE_CONCRETE_POWDER(createStack("CONCRETE_POWDER","WHITE")),
	ORANGE_CONCRETE_POWDER(createStack("CONCRETE_POWDER","ORANGE")),
	MAGENTA_CONCRETE_POWDER(createStack("CONCRETE_POWDER","MAGENTA")),
	LIGHT_BLUE_CONCRETE_POWDER(createStack("CONCRETE_POWDER","LIGHT_BLUE")),
	YELLOW_CONCRETE_POWDER(createStack("CONCRETE_POWDER","YELLOW")),
	LIME_CONCRETE_POWDER(createStack("CONCRETE_POWDER","LIME")),
	PINK_CONCRETE_POWDER(createStack("CONCRETE_POWDER","PINK")),
	GRAY_CONCRETE_POWDER(createStack("CONCRETE_POWDER","GRAY")),
	LIGHT_GRAY_CONCRETE_POWDER(createStack("CONCRETE_POWDER","LIGHT_GRAY")),
	CYAN_CONCRETE_POWDER(createStack("CONCRETE_POWDER","CYAN")),
	PURPLE_CONCRETE_POWDER(createStack("CONCRETE_POWDER","PURPLE")),
	BLUE_CONCRETE_POWDER(createStack("CONCRETE_POWDER","BLUE")),
	BROWN_CONCRETE_POWDER(createStack("CONCRETE_POWDER","BROWN")),
	GREEN_CONCRETE_POWDER(createStack("CONCRETE_POWDER","GREEN")),
	RED_CONCRETE_POWDER(createStack("CONCRETE_POWDER","RED")),
	BLACK_CONCRETE_POWDER(createStack("CONCRETE_POWDER","BLACK")),

	WHITE_STAINED_CLAY(createStack("STAINED_CLAY","WHITE")),
	ORANGE_TERRACOTTA(createStack("STAINED_CLAY","ORANGE")),
	MAGENTA_TERRACOTTA(createStack("STAINED_CLAY","MAGENTA")),
	LIGHT_BLUE_TERRACOTTA(createStack("STAINED_CLAY","LIGHT_BLUE")),
	YELLOW_TERRACOTTA(createStack("STAINED_CLAY","YELLOW")),
	LIME_TERRACOTTA(createStack("STAINED_CLAY","LIME")),
	PINK_TERRACOTTA(createStack("STAINED_CLAY","PINK")),
	GRAY_TERRACOTTA(createStack("STAINED_CLAY","GRAY")),
	LIGHT_GRAY_TERRACOTTA(createStack("STAINED_CLAY","LIGHT_GRAY")),
	CYAN_TERRACOTTA(createStack("STAINED_CLAY","CYAN")),
	PURPLE_TERRACOTTA(createStack("STAINED_CLAY","PURPLE")),
	BLUE_TERRACOTTA(createStack("STAINED_CLAY","BLUE")),
	BROWN_TERRACOTTA(createStack("STAINED_CLAY","BROWN")),
	GREEN_TERRACOTTA(createStack("STAINED_CLAY","GREEN")),
	RED_TERRACOTTA(createStack("STAINED_CLAY","RED")),
	BLACK_TERRACOTTA(createStack("STAINED_CLAY","BLACK")),

/*	WHITE_GLAZED_TERRACOTTA(createStack("WHITE", "STAINED_CLAY")),
	ORANGE_GLAZED_TERRACOTTA(createStack("ORANGE", "STAINED_CLAY")),
	MAGENTA_GLAZED_TERRACOTTA(createStack("MAGENTA", "STAINED_CLAY")),
	LIGHT_BLUE_GLAZED_TERRACOTTA(createStack("LIGHT_BLUE", "STAINED_CLAY")),
	YELLOW_GLAZED_TERRACOTTA(createStack("YELLOW", "STAINED_CLAY")),
	LIME_GLAZED_TERRACOTTA(createStack("LIME", "STAINED_CLAY")),
	PINK_GLAZED_TERRACOTTA(createStack("PINK", "STAINED_CLAY")),
	GRAY_GLAZED_TERRACOTTA(createStack("GRAY", "STAINED_CLAY")),
	LIGHT_GRAY_GLAZED_TERRACOTTA(createStack("LIGHT_GRAY", "STAINED_CLAY")),
	CYAN_GLAZED_TERRACOTTA(createStack("CYAN", "STAINED_CLAY")),
	PURPLE_GLAZED_TERRACOTTA(createStack("PURPLE", "STAINED_CLAY")),
	BLUE_GLAZED_TERRACOTTA(createStack("BLUE", "STAINED_CLAY")),
	BROWN_GLAZED_TERRACOTTA(createStack("BROWN", "STAINED_CLAY")),
	GREEN_GLAZED_TERRACOTTA(createStack("GREEN", "STAINED_CLAY")),
	RED_GLAZED_TERRACOTTA(createStack("RED", "STAINED_CLAY")),
	BLACK_GLAZED_TERRACOTTA(createStack("BLACK", "STAINED_CLAY")),*/

	OAK_LOG(createWoodItemStack("LOG", "OAK")),
	SPRUCE_LOG(createWoodItemStack("LOG", "SPRUCE")),
	BIRCH_LOG(createWoodItemStack("LOG", "BIRCH")),
	JUNGLE_LOG(createWoodItemStack("LOG", "JUNGLE")),
	ACACIA_LOG(createWoodItemStack("LOG_2", (short) 0, "ACACIA")),
	DARK_OAK_LOG(createWoodItemStack("LOG_2", (short) 1, "DARK_OAK")),

	OAK_PLANKS(createWoodItemStack("WOOD", "OAK")),
	SPRUCE_PLANKS(createWoodItemStack("WOOD", "SPRUCE")),
	BIRCH_PLANKS(createWoodItemStack("WOOD", "BIRCH")),
	JUNGLE_PLANKS(createWoodItemStack("WOOD", "JUNGLE")),
	ACACIA_PLANKS(createWoodItemStack("WOOD", "ACACIA")),
	DARK_OAK_PLANKS(createWoodItemStack("WOOD", "DARK_OAK")),

	OAK_SLAB(createWoodItemStack("WOOD_STEP", "OAK")),
	SPRUCE_SLAB(createWoodItemStack("WOOD_STEP", "SPRUCE")),
	BIRCH_SLAB(createWoodItemStack("WOOD_STEP", "BIRCH")),
	JUNGLE_SLAB(createWoodItemStack("WOOD_STEP", "JUNGLE")),
	ACACIA_SLAB(createWoodItemStack("WOOD_STEP", "ACACIA")),
	DARK_OAK_SLAB(createWoodItemStack("WOOD_STEP", "DARK_OAK")),

	OAK_STAIRS(createWoodItemStack("WOOD_STAIRS", "OAK")),
	SPRUCE_STAIRS(createWoodItemStack("SPRUCE_WOOD_STAIRS", "SPRUCE")),
	BIRCH_STAIRS(createWoodItemStack("BIRCH_WOOD_STAIRS", "BIRCH")),
	JUNGLE_STAIRS(createWoodItemStack("JUNGLE_WOOD_STAIRS", "JUNGLE")),
	ACACIA_STAIRS(createWoodItemStack("ACACIA_STAIRS", "ACACIA")),
	DARK_OAK_STAIRS(createWoodItemStack("DARK_OAK_STAIRS", "DARK_OAK")),

	OAK_DOOR(createWoodItemStack("WOOD_DOOR", (short) 0, "OAK")),
	SPRUCE_DOOR(createWoodItemStack("SPRUCE_DOOR_ITEM", (short) 0, "SPRUCE")),
	BIRCH_DOOR(createWoodItemStack("BIRCH_DOOR_ITEM", (short) 0, "BIRCH")),
	JUNGLE_DOOR(createWoodItemStack("JUNGLE_DOOR_ITEM", (short) 0, "JUNGLE")),
	ACACIA_DOOR(createWoodItemStack("ACACIA_DOOR_ITEM", (short) 0, "ACACIA")),
	DARK_OAK_DOOR(createWoodItemStack("DARK_OAK_DOOR_ITEM", (short) 0, "DARK_OAK")),

	OAK_FENCE(createWoodItemStack("FENCE", (short) 0, "OAK")),
	SPRUCE_FENCE(createWoodItemStack("FENCE", (short) 0, "SPRUCE")),
	BIRCH_FENCE(createWoodItemStack("FENCE", (short) 0, "BIRCH")),
	JUNGLE_FENCE(createWoodItemStack("FENCE", (short) 0, "JUNGLE")),
	ACACIA_FENCE(createWoodItemStack("FENCE", (short) 0, "ACACIA")),
	DARK_OAK_FENCE(createWoodItemStack("FENCE", (short) 0, "DARK_OAK")),

	OAK_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "OAK")),
	SPRUCE_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "SPRUCE")),
	BIRCH_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "BIRCH")),
	JUNGLE_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "JUNGLE")),
	ACACIA_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "ACACIA")),
	DARK_OAK_FENCE_GATE(createWoodItemStack("FENCE_GATE", (short) 0, "DARK_OAK")),

	OAK_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "OAK")),
	SPRUCE_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "SPRUCE")),
	BIRCH_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "BIRCH")),
	JUNGLE_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "JUNGLE")),
	ACACIA_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "ACACIA")),
	DARK_OAK_BUTTON(createWoodItemStack("WOOD_BUTTON", (short) 0, "DARK_OAK")),

	OAK_LEAVES(createWoodItemStack("LEAVES", "OAK")),
	SPRUCE_LEAVES(createWoodItemStack("LEAVES", "SPRUCE")),
	BIRCH_LEAVES(createWoodItemStack("LEAVES", "BIRCH")),
	JUNGLE_LEAVES(createWoodItemStack("LEAVES", "JUNGLE")),
	ACACIA_LEAVES(createWoodItemStack("LEAVES_2", (short) 0, "ACACIA")),
	DARK_OAK_LEAVES(createWoodItemStack("LEAVES_2", (short) 1, "DARK_OAK")),

	OAK_SAPLING(createWoodItemStack("SAPLING", "OAK")),
	SPRUCE_SAPLING(createWoodItemStack("SAPLING", "SPRUCE")),
	BIRCH_SAPLING(createWoodItemStack("SAPLING", "BIRCH")),
	JUNGLE_SAPLING(createWoodItemStack("SAPLING", "JUNGLE")),
	ACACIA_SAPLING(createWoodItemStack("SAPLING", (short) 0, "ACACIA")),
	DARK_OAK_SAPLING(createWoodItemStack("SAPLING", (short) 1, "DARK_OAK")),

	OAK_SIGN(createWoodItemStack("SIGN", "OAK")),
	SPRUCE_SIGN(createWoodItemStack("SIGN", "SPRUCE")),
	BIRCH_SIGN(createWoodItemStack("SIGN", "BIRCH")),
	JUNGLE_SIGN(createWoodItemStack("SIGN", "JUNGLE")),
	ACACIA_SIGN(createWoodItemStack("SIGN", (short) 0, "ACACIA")),
	DARK_OAK_SIGN(createWoodItemStack("SIGN", (short) 1, "DARK_OAK")),

	CLOCK(createStack("WATCH")),
	CRAFTING_TABLE(createStack("WORKBENCH")),
	CHARCOAL(createStack("COAL", (short) 1)),
	PLAYER_HEAD(createStack("SKULL_ITEM")),
	ENDER_EYE(createStack("ENDER_PEARL")),
	ENCHANTING_TABLE(createStack("ENCHANTMENT_TABLE")),
	SPAWNER(createStack("MOB_SPAWNER")),
	FIRE_CHARGE(createStack("FIREBALL")),
	FIREWORK_STAR(createStack("FIREWORK_CHARGE")),
	GRASS_BLOCK(createStack("GRASS")),

	COD(createStack("RAW_FISH", (short) 0)),
	SALMON(createStack("RAW_FISH", (short) 1)),
	TROPICAL_FISH(createStack("RAW_FISH", (short) 2)),
	PUFFERFISH(createStack("RAW_FISH", (short) 3)),
	COOKED_COD(createStack("COOKED_FISH", (short) 0)),
	COOKED_SALMON(createStack("COOKED_FISH", (short) 1)),

	SMOOTH_STONE_SLAB(createStack("STEP")),
	CRACKED_STONE_BRICKS(createStack("SMOOTH_BRICK")),

	ANDESITE(createStack("STONE", (short) 5)),
	POLISHED_ANDESITE(createStack("STONE", (short) 6)),
	DIORITE(createStack("STONE", (short) 3)),
	POLISHED_DIORITE(createStack("STONE", (short) 4)),
	GRANITE(createStack("STONE", (short) 1)),
	POLISHED_GRANITE(createStack("STONE", (short) 2)),

	GOLDEN_HELMET(createStack("GOLD_HELMET")),
	GOLDEN_CHESTPLATE(createStack("GOLD_CHESTPLATE")),
	GOLDEN_LEGGINGS(createStack("GOLD_LEGGINGS")),
	GOLDEN_BOOTS(createStack("GOLD_BOOTS")),

	INK_SAC(createStack("INK_SACK", getDye("BLACK_DYE"))),
	GLOW_INK_SAC(createStack("INK_SACK", getDye("BLACK_DYE"))),
	COCOA_BEANS(createStack("INK_SACK", getDye("BROWN_DYE"))),
	WHITE_DYE(createStack("INK_SACK", getDye("WHITE_DYE"))),
	ORANGE_DYE(createStack("INK_SACK", getDye("ORANGE_DYE"))),
	MAGENTA_DYE(createStack("INK_SACK", getDye("MAGENTA_DYE"))),
	LIGHT_BLUE_DYE(createStack("INK_SACK", getDye("LIGHT_BLUE_DYE"))),
	YELLOW_DYE(createStack("INK_SACK", getDye("YELLOW_DYE"))),
	LIME_DYE(createStack("INK_SACK", getDye("LIME_DYE"))),
	PINK_DYE(createStack("INK_SACK", getDye("PINK_DYE"))),
	GRAY_DYE(createStack("INK_SACK", getDye("GRAY_DYE"))),
	LIGHT_GRAY_DYE(createStack("INK_SACK", getDye("LIGHT_GRAY_DYE"))),
	CYAN_DYE(createStack("INK_SACK", getDye("CYAN_DYE"))),
	PURPLE_DYE(createStack("INK_SACK", getDye("PURPLE_DYE"))),
	BLUE_DYE(createStack("INK_SACK", getDye("BLUE_DYE"))),
	BROWN_DYE(createStack("INK_SACK", getDye("BROWN_DYE"))),
	GREEN_DYE(createStack("INK_SACK", getDye("GREEN_DYE"))),
	RED_DYE(createStack("INK_SACK", getDye("RED_DYE"))),
	BLACK_DYE(createStack("INK_SACK", getDye("BLACK_DYE"))),
	BONE_MEAL(createStack("INK_SACK", getDye("WHITE_DYE"))),

	BAT_SPAWN_EGG(createSpawnEgg(EntityType.BAT)),
	BEE_SPAWN_EGG(createSpawnEgg("BEE")),
	BLAZE_SPAWN_EGG(createSpawnEgg(EntityType.BLAZE)),
	CAT_SPAWN_EGG(createSpawnEgg(EntityType.CAT)),
	CAVE_SPIDER_SPAWN_EGG(createSpawnEgg(EntityType.CAVE_SPIDER)),
	CHICKEN_SPAWN_EGG(createSpawnEgg(EntityType.CHICKEN)),
	COD_SPAWN_EGG(createSpawnEgg(EntityType.COD)),
	COW_SPAWN_EGG(createSpawnEgg(EntityType.COW)),
	CREEPER_SPAWN_EGG(createSpawnEgg(EntityType.CREEPER)),
	DOLPHIN_SPAWN_EGG(createSpawnEgg("DOLPHIN")),
	DONKEY_SPAWN_EGG(createSpawnEgg("DONKEY")),
	DROWNED_SPAWN_EGG(createSpawnEgg("DROWNED ")),
	ELDER_GUARDIAN_SPAWN_EGG(createSpawnEgg("ELDER_GUARDIAN")),
	ENDERMAN_SPAWN_EGG(createSpawnEgg("ENDERMAN")),
	ENDERMITE_SPAWN_EGG(createSpawnEgg("ENDERMITE")),
	EVOKER_SPAWN_EGG(createSpawnEgg("EVOKER")),
	FOX_SPAWN_EGG(createSpawnEgg("FOX")),
	GHAST_SPAWN_EGG(createSpawnEgg(EntityType.GHAST)),
	GLOW_SQUID_SPAWN_EGG(createSpawnEgg("GLOW_SQUID")),
	GOAT_SPAWN_EGG(createSpawnEgg("GOAT")),
	GUARDIAN_SPAWN_EGG(createSpawnEgg(EntityType.GUARDIAN)),
	HOGLIN_SPAWN_EGG(createSpawnEgg("HOGLIN")),
	HORSE_SPAWN_EGG(createSpawnEgg(EntityType.HORSE)),
	HUSK_SPAWN_EGG(createSpawnEgg("HUSK")),
	LLAMA_SPAWN_EGG(createSpawnEgg("LLAMA")),
	MAGMA_CUBE_SPAWN_EGG(createSpawnEgg(EntityType.MAGMA_CUBE)),
	MOOSHROOM_SPAWN_EGG(createSpawnEgg("MUSHROOM_COW")),
	MULE_SPAWN_EGG(createSpawnEgg("MULE")),
	OCELOT_SPAWN_EGG(createSpawnEgg("OCELOT")),
	PANDA_SPAWN_EGG(createSpawnEgg("PANDA")),
	PARROT_SPAWN_EGG(createSpawnEgg("PARROT")),
	PHANTOM_SPAWN_EGG(createSpawnEgg("PHANTOM")),
	PIG_SPAWN_EGG(createSpawnEgg(EntityType.PIG)),
	PIGLIN_SPAWN_EGG(createSpawnEgg("PIGLIN")),
	PIGLIN_BRUTE_SPAWN_EGG(createSpawnEgg("PIGLIN_BRUTE")),
	PILLAGER_SPAWN_EGG(createSpawnEgg("PILLAGER")),
	POLAR_BEAR_SPAWN_EGG(createSpawnEgg("POLAR_BEAR")),
	PUFFERFISH_SPAWN_EGG(createSpawnEgg(EntityType.PUFFERFISH)),
	RABBIT_SPAWN_EGG(createSpawnEgg(EntityType.RABBIT)),
	RAVAGER_SPAWN_EGG(createSpawnEgg("RAVAGER")),
	SALMON_SPAWN_EGG(createSpawnEgg(EntityType.SALMON)),
	SHEEP_SPAWN_EGG(createSpawnEgg(EntityType.SHEEP)),
	SHULKER_SPAWN_EGG(createSpawnEgg("SHULKER")),
	SILVERFISH_SPAWN_EGG(createSpawnEgg(EntityType.SILVERFISH)),
	SKELETON_SPAWN_EGG(createSpawnEgg(EntityType.SKELETON)),
	SKELETON_HORSE_SPAWN_EGG(createSpawnEgg(EntityType.SKELETON_HORSE)),
	SLIME_SPAWN_EGG(createSpawnEgg(EntityType.SLIME)),
	SPIDER_SPAWN_EGG(createSpawnEgg(EntityType.SPIDER)),
	SQUID_SPAWN_EGG(createSpawnEgg(EntityType.SQUID)),
	STRAY_SPAWN_EGG(createSpawnEgg("STRAY")),
	STRIDER_SPAWN_EGG(createSpawnEgg("STRIDER")),
	TRADER_LLAMA_SPAWN_EGG(createSpawnEgg("TRADER_LLAMA ")),
	TROPICAL_FISH_SPAWN_EGG(createSpawnEgg("TROPICAL_FISH")),
	TURTLE_SPAWN_EGG(createSpawnEgg("TURTLE")),
	VEX_SPAWN_EGG(createSpawnEgg("VEX")),
	VILLAGER_SPAWN_EGG(createSpawnEgg(EntityType.VILLAGER)),
	VINDICATOR_SPAWN_EGG(createSpawnEgg("VINDICATOR")),
	WANDERING_TRADER_SPAWN_EGG(createSpawnEgg("WANDERING_TRADER ")),
	WITCH_SPAWN_EGG(createSpawnEgg(EntityType.WITCH)),
	WITHER_SKELETON_SPAWN_EGG(createSpawnEgg("WITHER_SKELETON")),
	WOLF_SPAWN_EGG(createSpawnEgg(EntityType.WOLF)),
	ZOGLIN_SPAWN_EGG(createSpawnEgg("ZOGLIN")),
	ZOMBIE_SPAWN_EGG(createSpawnEgg(EntityType.ZOMBIE)),
	ZOMBIE_HORSE_SPAWN_EGG(createSpawnEgg(EntityType.ZOMBIE_HORSE)),
	ZOMBIE_VILLAGER_SPAWN_EGG(createSpawnEgg("ZOMBIE_VILLAGER")),
	ZOMBIFIED_PIGLIN_SPAWN_EGG(createSpawnEgg("PIG_ZOMBIE")),
	ZOMBIE_PIGMAN_SPAWN(createSpawnEgg("PIG_ZOMBIE")),
	;
	private static final Map<String, Materials> STACKS = new HashMap<>();
	private final ItemStack itemStack;
	private static final ItemStack defaultStack = new ItemStack(Material.AIR);

	static {
		for (Materials material : values()) {
			STACKS.put(material.name(), material);
		}
	}

	Materials(final ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Nonnull
	public static ItemStack getItemStack(String materialName) {
		return getItemStack(materialName, 1);
	}

	@Nonnull
	public static ItemStack getItemStack(String materialName, int amount) {
		Materials material = STACKS.get(materialName);
		if (material != null) {
			ItemStack stack = material.itemStack;
			if (stack != null) {
				ItemStack clone = stack.clone();
				clone.setAmount(amount);
				return clone;
			}
		}
		return defaultStack;
	}

	public ItemStack getItemStack(int amount) {
		if (itemStack == null) return null;
		ItemStack stack = itemStack.clone();
		stack.setAmount(amount);
		return stack;
	}

	private static ItemStack createStack(@Nonnull String materialName) {
		return createStack(materialName, (short) 0);
	}

	private static ItemStack createStack(@Nonnull String materialName, short damage) {
		Material material = Material.getMaterial(materialName);
		if (material != null) {
			return new ItemStack(material, 1, damage);
		}
		return null;
	}

	private static ItemStack createStack(@Nonnull String materialName, @Nonnull String colorName) {
		final short color = checkColor(colorName);
		return createStack(materialName, color);
	}

	private static ItemStack createWoodItemStack(final String materialName, final String itemName) {
		return createWoodItemStack(materialName, (short) -1, itemName);
	}

	private static ItemStack createWoodItemStack(final String materialName, short woodTypeData, final String itemName) {
		final Material material = Material.getMaterial(materialName);
		if (material == null) return null;
		if (woodTypeData == -1)
			woodTypeData = getWoodTypeData(itemName);
		if (woodTypeData >= 0)
			return new ItemStack(material, 1, woodTypeData);
		return null;
	}

	public static ItemStack createSpawnEgg(String entityType) {
		return (createSpawnEgg(EntityType.fromName(entityType)));

	}

	public static ItemStack createSpawnEgg(EntityType entityType) {
		Material material = Material.getMaterial("MONSTER_EGG");
		if (material == null) return null;
		ItemStack itemStack = new ItemStack(material, 1);
		SpawnEgg spawnEgg = (SpawnEgg) itemStack.getData();
		if (spawnEgg == null) return itemStack;
		if (entityType == null)
			spawnEgg.setSpawnedType(EntityType.SHEEP);
		return itemStack;
	}

	private static short getWoodTypeData(final String itemName) {
		if (itemName.startsWith("OAK")) {
			return 0;
		}
		if (itemName.startsWith("SPRUCE")) {
			return 1;
		}
		if (itemName.startsWith("BIRCH")) {
			return 2;
		}
		if (itemName.startsWith("JUNGLE")) {
			return 3;
		}
		if (itemName.startsWith("ACACIA")) {
			return 4;
		}
		if (itemName.startsWith("DARK_OAK")) {
			return 5;
		}
		return -1;
	}

	private static short checkColor(String color) {
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

	public static short getDye(final String itemName) {

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

}
