package org.broken.arrow.library.itemcreator.utility.matrials;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a mapping between modern Minecraft material names (post-1.13)
 * and their legacy (pre-1.13) {@link Material} and {@link ItemStack} equivalents.
 * <p>
 * This enum is designed to help maintain backwards compatibility for plugins
 * running on legacy server versions by converting modern material identifiers
 * to the corresponding legacy items with appropriate data values.
 * <p>
 * Each enum constant stores a prebuilt {@link ItemStack} of the legacy material,
 * allowing for quick lookups with minimal performance impact. The provided
 * utility methods support:
 * <ul>
 *   <li>Fetching an {@link ItemStack} from a modern material name</li>
 *   <li>Cloning stacks with custom amounts or data values</li>
 *   <li>Creating specific item types such as wood variants, dyes, and spawn eggs</li>
 * </ul>
 * <p>
 * Typical usage:
 * <pre>{@code
 * ItemStack legacyGlass = Materials.getItemStack("WHITE_STAINED_GLASS");
 * }</pre>
 * <p>
 * This approach avoids repetitive manual conversions and centralizes the
 * modern-to-legacy mapping logic for maintainability.
 */
@SuppressWarnings("deprecation")
public enum Materials {
    /**
     * WHITE_STAINED_GLASS_PANE
     */
    WHITE_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 0)),
    /**
     * WHITE_STAINED_GLASS_PANE
     */
    ORANGE_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 1)),
    /**
     * MAGENTA_STAINED_GLASS_PANE
     */
    MAGENTA_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 2)),
    /**
     * LIGHT_BLUE_STAINED_GLASS_PANE
     */
    LIGHT_BLUE_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 3)),
    /**
     * YELLOW_STAINED_GLASS_PANE
     */
    YELLOW_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 4)),
    /**
     * LIME_STAINED_GLASS_PANE
     */
    LIME_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 5)),
    /**
     * PINK_STAINED_GLASS_PANE
     */
    PINK_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 6)),
    /**
     * GRAY_STAINED_GLASS_PANE
     */
    GRAY_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 7)),
    /**
     * LIGHT_GRAY_STAINED_GLASS_PANE
     */
    LIGHT_GRAY_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 8)),
    /**
     * CYAN_STAINED_GLASS_PANE
     */
    CYAN_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 9)),
    /**
     * WHITE_STAINED_GLASS_PANE
     */
    PURPLE_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 10)),
    /**
     * WHITE_STAINED_GLASS_PANE
     */
    BLUE_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 11)),
    /**
     * BROWN_STAINED_GLASS_PANE
     */
    BROWN_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 12)),
    /**
     * GREEN_STAINED_GLASS_PANE
     */
    GREEN_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 13)),
    /**
     * RED_STAINED_GLASS_PANE
     */
    RED_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 14)),
    /**
     * BLACK_STAINED_GLASS_PANE
     */
    BLACK_STAINED_GLASS_PANE(createStack(Constants.STAINED_GLASS_PANE, (short) 15)),

    /**
     * WHITE_STAINED_GLASS
     */
    WHITE_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 0)),
    /**
     * ORANGE_STAINED_GLASS
     */
    ORANGE_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 1)),
    /**
     * MAGENTA_STAINED_GLASS
     */
    MAGENTA_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 2)),
    /**
     * LIGHT_BLUE_STAINED_GLASS
     */
    LIGHT_BLUE_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 3)),
    /**
     * YELLOW_STAINED_GLASS
     */
    YELLOW_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 4)),
    /**
     * LIME_STAINED_GLASS
     */
    LIME_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 5)),
    /**
     * PINK_STAINED_GLASS
     */
    PINK_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 6)),
    /**
     * WHITE_STAINED_GLASS
     */
    GRAY_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 7)),
    /**
     * LIGHT_GRAY_STAINED_GLASS
     */
    LIGHT_GRAY_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 8)),
    /**
     * CYAN_STAINED_GLASS
     */
    CYAN_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 9)),
    /**
     * PURPLE_STAINED_GLASS
     */
    PURPLE_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 10)),
    /**
     * BLUE_STAINED_GLASS
     */
    BLUE_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 11)),
    /**
     * BROWN_STAINED_GLASS
     */
    BROWN_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 12)),
    /**
     * GREEN_STAINED_GLASS
     */
    GREEN_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 13)),
    /**
     * RED_STAINED_GLASS
     */
    RED_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 14)),
    /**
     * BLACK_STAINED_GLASS
     */
    BLACK_STAINED_GLASS(createStack(Constants.STAINED_GLASS, (short) 15)),

    /**
     * WHITE_WOOL
     */
    WHITE_WOOL(createStack(Constants.WOOL, (short) 0)),
    /**
     * ORANGE_WOOL
     */
    ORANGE_WOOL(createStack(Constants.WOOL, (short) 1)),
    /**
     * MAGENTA_WOOL
     */
    MAGENTA_WOOL(createStack(Constants.WOOL, (short) 2)),
    /**
     * LIGHT_BLUE_WOOL
     */
    LIGHT_BLUE_WOOL(createStack(Constants.WOOL, (short) 3)),
    /**
     * YELLOW_WOOL
     */
    YELLOW_WOOL(createStack(Constants.WOOL, (short) 4)),
    /**
     * LIME_WOOL
     */
    LIME_WOOL(createStack(Constants.WOOL, (short) 5)),
    /**
     * PINK_WOOL
     */
    PINK_WOOL(createStack(Constants.WOOL, (short) 6)),
    /**
     * GRAY_WOOL
     */
    GRAY_WOOL(createStack(Constants.WOOL, (short) 7)),
    /**
     * LIGHT_GRAY_WOOL
     */
    LIGHT_GRAY_WOOL(createStack(Constants.WOOL, (short) 8)),
    /**
     * CYAN_WOOL
     */
    CYAN_WOOL(createStack(Constants.WOOL, (short) 9)),
    /**
     * PURPLE_WOOL
     */
    PURPLE_WOOL(createStack(Constants.WOOL, (short) 10)),
    /**
     * BLUE_WOOL
     */
    BLUE_WOOL(createStack(Constants.WOOL, (short) 11)),
    /**
     * BROWN_WOOL
     */
    BROWN_WOOL(createStack(Constants.WOOL, (short) 12)),
    /**
     * GREEN_WOOL
     */
    GREEN_WOOL(createStack(Constants.WOOL, (short) 13)),
    /**
     * RED_WOOL
     */
    RED_WOOL(createStack(Constants.WOOL, (short) 14)),
    /**
     * BLACK_WOOL
     */
    BLACK_WOOL(createStack(Constants.WOOL, (short) 15)),

    /**
     * White carpet.
     */
    WHITE_CARPET(createStack(Constants.CARPET, (short) 0)),
    /**
     * Orange carpet.
     */
    ORANGE_CARPET(createStack(Constants.CARPET, (short) 1)),
    /**
     * Magenta carpet.
     */
    MAGENTA_CARPET(createStack(Constants.CARPET, (short) 2)),
    /**
     * Light blue carpet.
     */
    LIGHT_BLUE_CARPET(createStack(Constants.CARPET, (short) 3)),
    /**
     * Yellow carpet.
     */
    YELLOW_CARPET(createStack(Constants.CARPET, (short) 4)),
    /**
     * Lime carpet.
     */
    LIME_CARPET(createStack(Constants.CARPET, (short) 5)),
    /**
     * Pink carpet.
     */
    PINK_CARPET(createStack(Constants.CARPET, (short) 6)),
    /**
     * Gray carpet.
     */
    GRAY_CARPET(createStack(Constants.CARPET, (short) 7)),
    /**
     * Light gray carpet.
     */
    LIGHT_GRAY_CARPET(createStack(Constants.CARPET, (short) 8)),
    /**
     * Cyan carpet.
     */
    CYAN_CARPET(createStack(Constants.CARPET, (short) 9)),
    /**
     * Purple carpet.
     */
    PURPLE_CARPET(createStack(Constants.CARPET, (short) 10)),
    /**
     * Blue carpet.
     */
    BLUE_CARPET(createStack(Constants.CARPET, (short) 11)),
    /**
     * Brown carpet.
     */
    BROWN_CARPET(createStack(Constants.CARPET, (short) 12)),
    /**
     * Green carpet.
     */
    GREEN_CARPET(createStack(Constants.CARPET, (short) 13)),
    /**
     * Red carpet.
     */
    RED_CARPET(createStack(Constants.CARPET, (short) 14)),
    /**
     * Black carpet.
     */
    BLACK_CARPET(createStack(Constants.CARPET, (short) 15)),

    /**
     * White concrete.
     */
    WHITE_CONCRETE(createStack(Constants.CONCRETE, (short) 0)),
    /**
     * Orange concrete.
     */
    ORANGE_CONCRETE(createStack(Constants.CONCRETE, (short) 1)),
    /**
     * Magenta concrete.
     */
    MAGENTA_CONCRETE(createStack(Constants.CONCRETE, (short) 2)),
    /**
     * Light blue concrete.
     */
    LIGHT_BLUE_CONCRETE(createStack(Constants.CONCRETE, (short) 3)),
    /**
     * Yellow concrete.
     */
    YELLOW_CONCRETE(createStack(Constants.CONCRETE, (short) 4)),
    /**
     * Lime concrete.
     */
    LIME_CONCRETE(createStack(Constants.CONCRETE, (short) 5)),
    /**
     * Pink concrete.
     */
    PINK_CONCRETE(createStack(Constants.CONCRETE, (short) 6)),
    /**
     * Gray concrete.
     */
    GRAY_CONCRETE(createStack(Constants.CONCRETE, (short) 7)),
    /**
     * Light gray concrete.
     */
    LIGHT_GRAY_CONCRETE(createStack(Constants.CONCRETE, (short) 8)),
    /**
     * Cyan concrete.
     */
    CYAN_CONCRETE(createStack(Constants.CONCRETE, (short) 9)),
    /**
     * Purple concrete.
     */
    PURPLE_CONCRETE(createStack(Constants.CONCRETE, (short) 10)),
    /**
     * Blue concrete.
     */
    BLUE_CONCRETE(createStack(Constants.CONCRETE, (short) 11)),
    /**
     * Brown concrete.
     */
    BROWN_CONCRETE(createStack(Constants.CONCRETE, (short) 12)),
    /**
     * Green concrete.
     */
    GREEN_CONCRETE(createStack(Constants.CONCRETE, (short) 13)),
    /**
     * Red concrete.
     */
    RED_CONCRETE(createStack(Constants.CONCRETE, (short) 14)),
    /**
     * Black concrete.
     */
    BLACK_CONCRETE(createStack(Constants.CONCRETE, (short) 15)),

    /**
     * White concrete powder.
     */
    WHITE_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 0)),
    /**
     * Orange concrete powder.
     */
    ORANGE_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 1)),
    /**
     * Magenta concrete powder.
     */
    MAGENTA_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 2)),
    /**
     * Light blue concrete powder.
     */
    LIGHT_BLUE_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 3)),
    /**
     * Yellow concrete powder.
     */
    YELLOW_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 4)),
    /**
     * Lime concrete powder.
     */
    LIME_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 5)),
    /**
     * Pink concrete powder.
     */
    PINK_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 6)),
    /**
     * Gray concrete powder.
     */
    GRAY_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 7)),
    /**
     * Light gray concrete powder.
     */
    LIGHT_GRAY_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 8)),
    /**
     * Cyan concrete powder.
     */
    CYAN_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 9)),
    /**
     * Purple concrete powder.
     */
    PURPLE_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 10)),
    /**
     * Blue concrete powder.
     */
    BLUE_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 11)),
    /**
     * Brown concrete powder.
     */
    BROWN_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 12)),
    /**
     * Green concrete powder.
     */
    GREEN_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 13)),
    /**
     * Red concrete powder.
     */
    RED_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 14)),
    /**
     * Black concrete powder.
     */
    BLACK_CONCRETE_POWDER(createStack(Constants.CONCRETE_POWDER, (short) 15)),

    /**
     * White stained clay (terracotta).
     */
    WHITE_STAINED_CLAY(createStack(Constants.STAINED_CLAY, (short) 0)),
    /**
     * Orange terracotta.
     */
    ORANGE_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 1)),
    /**
     * Magenta terracotta.
     */
    MAGENTA_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 2)),
    /**
     * Light blue terracotta.
     */
    LIGHT_BLUE_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 3)),
    /**
     * Yellow terracotta.
     */
    YELLOW_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 4)),
    /**
     * Lime terracotta.
     */
    LIME_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 5)),
    /**
     * Pink terracotta.
     */
    PINK_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 6)),
    /**
     * Gray terracotta.
     */
    GRAY_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 7)),
    /**
     * Light gray terracotta.
     */
    LIGHT_GRAY_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 8)),
    /**
     * Cyan terracotta.
     */
    CYAN_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 9)),
    /**
     * Purple terracotta.
     */
    PURPLE_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 10)),
    /**
     * Blue terracotta.
     */
    BLUE_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 11)),
    /**
     * Brown terracotta.
     */
    BROWN_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 12)),
    /**
     * Green terracotta.
     */
    GREEN_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 13)),
    /**
     * Red terracotta.
     */
    RED_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 14)),
    /**
     * Black terracotta.
     */
    BLACK_TERRACOTTA(createStack(Constants.STAINED_CLAY, (short) 15)),

    /**
     * Oak log.
     */
    OAK_LOG(createWoodItemStack("LOG", "OAK")),
    /**
     * Spruce log.
     */
    SPRUCE_LOG(createWoodItemStack("LOG", Constants.SPRUCE)),
    /**
     * Birch log.
     */
    BIRCH_LOG(createWoodItemStack("LOG", Constants.BIRCH)),
    /**
     * Jungle log.
     */
    JUNGLE_LOG(createWoodItemStack("LOG", Constants.JUNGLE)),
    /**
     * Acacia log.
     */
    ACACIA_LOG(createWoodItemStack("LOG_2", (short) 0, Constants.ACACIA)),
    /**
     * Dark oak log.
     */
    DARK_OAK_LOG(createWoodItemStack("LOG_2", (short) 1, Constants.DARK_OAK)),

    /**
     * Oak planks.
     */
    OAK_PLANKS(createWoodItemStack("WOOD", "OAK")),
    /**
     * Spruce planks.
     */
    SPRUCE_PLANKS(createWoodItemStack("WOOD", Constants.SPRUCE)),
    /**
     * Birch planks.
     */
    BIRCH_PLANKS(createWoodItemStack("WOOD", Constants.BIRCH)),
    /**
     * Jungle planks.
     */
    JUNGLE_PLANKS(createWoodItemStack("WOOD", Constants.JUNGLE)),
    /**
     * Acacia planks.
     */
    ACACIA_PLANKS(createWoodItemStack("WOOD", Constants.ACACIA)),
    /**
     * Dark oak planks.
     */
    DARK_OAK_PLANKS(createWoodItemStack("WOOD", Constants.DARK_OAK)),
    /**
     * Oak slab.
     */
    OAK_SLAB(createWoodItemStack(Constants.WOOD_STEP, "OAK")),
    /**
     * Spruce slab.
     */
    SPRUCE_SLAB(createWoodItemStack(Constants.WOOD_STEP, Constants.SPRUCE)),
    /**
     * Birch slab.
     */
    BIRCH_SLAB(createWoodItemStack(Constants.WOOD_STEP, Constants.BIRCH)),
    /**
     * Jungle slab.
     */
    JUNGLE_SLAB(createWoodItemStack(Constants.WOOD_STEP, Constants.JUNGLE)),
    /**
     * Acacia slab.
     */
    ACACIA_SLAB(createWoodItemStack(Constants.WOOD_STEP, Constants.ACACIA)),
    /**
     * Dark oak slab.
     */
    DARK_OAK_SLAB(createWoodItemStack(Constants.WOOD_STEP, Constants.DARK_OAK)),

    /**
     * Oak stairs.
     */
    OAK_STAIRS(createWoodItemStack("WOOD_STAIRS", "OAK")),
    /**
     * Spruce stairs.
     */
    SPRUCE_STAIRS(createWoodItemStack("SPRUCE_WOOD_STAIRS", Constants.SPRUCE)),
    /**
     * Birch stairs.
     */
    BIRCH_STAIRS(createWoodItemStack("BIRCH_WOOD_STAIRS", Constants.BIRCH)),
    /**
     * Jungle stairs.
     */
    JUNGLE_STAIRS(createWoodItemStack("JUNGLE_WOOD_STAIRS", Constants.JUNGLE)),
    /**
     * Acacia stairs.
     */
    ACACIA_STAIRS(createWoodItemStack("ACACIA_STAIRS", Constants.ACACIA)),
    /**
     * Dark oak stairs.
     */
    DARK_OAK_STAIRS(createWoodItemStack("DARK_OAK_STAIRS", Constants.DARK_OAK)),
    /**
     * Oak door.
     */
    OAK_DOOR(createWoodItemStack("WOOD_DOOR", (short) 0, "OAK")),
    /**
     * Spruce door.
     */
    SPRUCE_DOOR(createWoodItemStack("SPRUCE_DOOR_ITEM", (short) 0, Constants.SPRUCE)),
    /**
     * Birch door.
     */
    BIRCH_DOOR(createWoodItemStack("BIRCH_DOOR_ITEM", (short) 0, Constants.BIRCH)),
    /**
     * Jungle door.
     */
    JUNGLE_DOOR(createWoodItemStack("JUNGLE_DOOR_ITEM", (short) 0, Constants.JUNGLE)),
    /**
     * Acacia door.
     */
    ACACIA_DOOR(createWoodItemStack("ACACIA_DOOR_ITEM", (short) 0, Constants.ACACIA)),
    /**
     * Dark oak door.
     */
    DARK_OAK_DOOR(createWoodItemStack("DARK_OAK_DOOR_ITEM", (short) 0, Constants.DARK_OAK)),

    /**
     * Oak fence.
     */
    OAK_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, "OAK")),
    /**
     * Spruce fence.
     */
    SPRUCE_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, Constants.SPRUCE)),
    /**
     * Birch fence.
     */
    BIRCH_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, Constants.BIRCH)),
    /**
     * Jungle fence.
     */
    JUNGLE_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, Constants.JUNGLE)),
    /**
     * Acacia fence.
     */
    ACACIA_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, Constants.ACACIA)),
    /**
     * Dark oak fence.
     */
    DARK_OAK_FENCE(createWoodItemStack(Constants.FENCE, (short) 0, Constants.DARK_OAK)),
    /**
     * Oak fence gate.
     */
    OAK_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, "OAK")),

    /**
     * Spruce fence gate.
     */
    SPRUCE_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, Constants.SPRUCE)),
    /**
     * Birch fence gate.
     */
    BIRCH_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, Constants.BIRCH)),
    /**
     * Jungle fence gate.
     */
    JUNGLE_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, Constants.JUNGLE)),
    /**
     * Acacia fence gate.
     */
    ACACIA_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, Constants.ACACIA)),
    /**
     * Dark oak fence gate.
     */
    DARK_OAK_FENCE_GATE(createWoodItemStack(Constants.FENCE_GATE, (short) 0, Constants.DARK_OAK)),

    /**
     * Oak button.
     */
    OAK_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, "OAK")),
    /**
     * Spruce button.
     */
    SPRUCE_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, Constants.SPRUCE)),
    /**
     * Birch button.
     */
    BIRCH_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, Constants.BIRCH)),
    /**
     * Jungle button.
     */
    JUNGLE_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, Constants.JUNGLE)),
    /**
     * Acacia button.
     */
    ACACIA_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, Constants.ACACIA)),
    /**
     * Dark oak button.
     */
    DARK_OAK_BUTTON(createWoodItemStack(Constants.WOOD_BUTTON, (short) 0, Constants.DARK_OAK)),
    /**
     * Oak leaves.
     */
    OAK_LEAVES(createWoodItemStack(Constants.LEAVES, "OAK")),

    /**
     * Spruce leaves.
     */
    SPRUCE_LEAVES(createWoodItemStack(Constants.LEAVES, Constants.SPRUCE)),
    /**
     * Birch leaves.
     */
    BIRCH_LEAVES(createWoodItemStack(Constants.LEAVES, Constants.BIRCH)),
    /**
     * Jungle leaves.
     */
    JUNGLE_LEAVES(createWoodItemStack(Constants.LEAVES, Constants.JUNGLE)),
    /**
     * Acacia leaves.
     */
    ACACIA_LEAVES(createWoodItemStack("LEAVES_2", (short) 0, Constants.ACACIA)),
    /**
     * Dark oak leaves.
     */
    DARK_OAK_LEAVES(createWoodItemStack("LEAVES_2", (short) 1, Constants.DARK_OAK)),

    /**
     * Oak sapling.
     */
    OAK_SAPLING(createWoodItemStack(Constants.SAPLING, "OAK")),
    /**
     * Spruce sapling.
     */
    SPRUCE_SAPLING(createWoodItemStack(Constants.SAPLING, Constants.SPRUCE)),
    /**
     * Birch sapling.
     */
    BIRCH_SAPLING(createWoodItemStack(Constants.SAPLING, Constants.BIRCH)),
    /**
     * Jungle sapling.
     */
    JUNGLE_SAPLING(createWoodItemStack(Constants.SAPLING, Constants.JUNGLE)),
    /**
     * Acacia sapling.
     */
    ACACIA_SAPLING(createWoodItemStack(Constants.SAPLING, (short) 4, Constants.ACACIA)),
    /**
     * Dark oak sapling.
     */
    DARK_OAK_SAPLING(createWoodItemStack(Constants.SAPLING, (short) 5, Constants.DARK_OAK)),

    /**
     * Oak sign.
     */
    OAK_SIGN(createWoodItemStack(Constants.SIGN, "OAK")),
    /**
     * Spruce sign.
     */
    SPRUCE_SIGN(createWoodItemStack(Constants.SIGN, Constants.SPRUCE)),
    /**
     * Birch sign.
     */
    BIRCH_SIGN(createWoodItemStack(Constants.SIGN, Constants.BIRCH)),
    /**
     * Jungle sign.
     */
    JUNGLE_SIGN(createWoodItemStack(Constants.SIGN, Constants.JUNGLE)),
    /**
     * Acacia sign.
     */
    ACACIA_SIGN(createWoodItemStack(Constants.SIGN, (short) 0, Constants.ACACIA)),
    /**
     * Dark oak sign.
     */
    DARK_OAK_SIGN(createWoodItemStack(Constants.SIGN, (short) 1, Constants.DARK_OAK)),

    /**
     * CLOCK
     */
    CLOCK(createStack("WATCH")),
    /**
     * CRAFTING_TABLE
     */
    CRAFTING_TABLE(createStack("WORKBENCH")),
    /**
     * CHARCOAL
     */
    CHARCOAL(createStack("COAL", (short) 1)),
    /**
     * PLAYER_HEAD
     */
    PLAYER_HEAD(createStack("SKULL_ITEM")),
    /**
     * ENDER_EYE
     */
    ENDER_EYE(createStack("ENDER_PEARL")),
    /**
     * ENCHANTING_TABLE
     */
    ENCHANTING_TABLE(createStack("ENCHANTMENT_TABLE")),
    /**
     * SPAWNER
     */
    SPAWNER(createStack("MOB_SPAWNER")),
    /**
     * FIRE_CHARGE
     */
    FIRE_CHARGE(createStack("FIREBALL")),
    /**
     * FIREWORK_STAR
     */
    FIREWORK_STAR(createStack("FIREWORK_CHARGE")),
    /**
     * GRASS_BLOCK
     */
    GRASS_BLOCK(createStack("GRASS")),

    /**
     * COD
     */
    COD(createStack(Constants.RAW_FISH, (short) 0)),
    /**
     * SALMON
     */
    SALMON(createStack(Constants.RAW_FISH, (short) 1)),
    /**
     * TROPICAL_FISH
     */
    TROPICAL_FISH(createStack(Constants.RAW_FISH, (short) 2)),
    /**
     * PUFFERFISH
     */
    PUFFERFISH(createStack(Constants.RAW_FISH, (short) 3)),
    /**
     * COOKED_COD
     */
    COOKED_COD(createStack("COOKED_FISH", (short) 0)),
    /**
     * COOKED_SALMON
     */
    COOKED_SALMON(createStack("COOKED_FISH", (short) 1)),

    /**
     * SMOOTH_STONE_SLAB
     */
    SMOOTH_STONE_SLAB(createStack("STEP")),
    /**
     * CRACKED_STONE_BRICKS
     */
    CRACKED_STONE_BRICKS(createStack("SMOOTH_BRICK")),

    /**
     * ANDESITE
     */
    ANDESITE(createStack(Constants.STONE, (short) 5)),
    /**
     * POLISHED_ANDESITE
     */
    POLISHED_ANDESITE(createStack(Constants.STONE, (short) 6)),
    /**
     * DIORITE
     */
    DIORITE(createStack(Constants.STONE, (short) 3)),
    /**
     * POLISHED_DIORITE
     */
    POLISHED_DIORITE(createStack(Constants.STONE, (short) 4)),
    /**
     * GRANITE
     */
    GRANITE(createStack(Constants.STONE, (short) 1)),
    /**
     * POLISHED_GRANITE
     */
    POLISHED_GRANITE(createStack(Constants.STONE, (short) 2)),

    /**
     * GOLDEN_HELMET
     */
    GOLDEN_HELMET(createStack("GOLD_HELMET")),
    /**
     * GOLDEN_CHESTPLATE
     */
    GOLDEN_CHESTPLATE(createStack("GOLD_CHESTPLATE")),
    /**
     * GOLDEN_LEGGINGS
     */
    GOLDEN_LEGGINGS(createStack("GOLD_LEGGINGS")),
    /**
     * GOLDEN_BOOTS
     */
    GOLDEN_BOOTS(createStack("GOLD_BOOTS")),

    /**
     * INK_SAC
     */
    INK_SAC(createStack(Constants.INK_SACK, getDye(Constants.BLACK_DYE))),
    /**
     * GLOW_INK_SAC
     */
    GLOW_INK_SAC(createStack(Constants.INK_SACK, getDye(Constants.BLACK_DYE))),
    /**
     * COCOA_BEANS
     */
    COCOA_BEANS(createStack(Constants.INK_SACK, getDye("BROWN_DYE"))),
    /**
     * WHITE_DYE
     */
    WHITE_DYE(createStack(Constants.INK_SACK, getDye("WHITE_DYE"))),
    /**
     * ORANGE_DYE
     */
    ORANGE_DYE(createStack(Constants.INK_SACK, getDye("ORANGE_DYE"))),
    /**
     * MAGENTA_DYE
     */
    MAGENTA_DYE(createStack(Constants.INK_SACK, getDye("MAGENTA_DYE"))),
    /**
     * LIGHT_BLUE_DYE
     */
    LIGHT_BLUE_DYE(createStack(Constants.INK_SACK, getDye("LIGHT_BLUE_DYE"))),
    /**
     * YELLOW_DYE
     */
    YELLOW_DYE(createStack(Constants.INK_SACK, getDye("YELLOW_DYE"))),
    /**
     * LIME_DYE
     */
    LIME_DYE(createStack(Constants.INK_SACK, getDye("LIME_DYE"))),
    /**
     * PINK_DYE
     */
    PINK_DYE(createStack(Constants.INK_SACK, getDye("PINK_DYE"))),
    /**
     * GRAY_DYE
     */
    GRAY_DYE(createStack(Constants.INK_SACK, getDye("GRAY_DYE"))),
    /**
     * LIGHT_GRAY_DYE
     */
    LIGHT_GRAY_DYE(createStack(Constants.INK_SACK, getDye("LIGHT_GRAY_DYE"))),
    /**
     * CYAN_DYE
     */
    CYAN_DYE(createStack(Constants.INK_SACK, getDye("CYAN_DYE"))),
    /**
     * PURPLE_DYE
     */
    PURPLE_DYE(createStack(Constants.INK_SACK, getDye("PURPLE_DYE"))),
    /**
     * BLUE_DYE
     */
    BLUE_DYE(createStack(Constants.INK_SACK, getDye("BLUE_DYE"))),
    /**
     * BROWN_DYE
     */
    BROWN_DYE(createStack(Constants.INK_SACK, getDye("BROWN_DYE"))),
    /**
     * GREEN_DYE
     */
    GREEN_DYE(createStack(Constants.INK_SACK, getDye("GREEN_DYE"))),
    /**
     * RED_DYE
     */
    RED_DYE(createStack(Constants.INK_SACK, getDye("RED_DYE"))),
    /**
     * BLACK_DYE
     */
    BLACK_DYE(createStack(Constants.INK_SACK, getDye(Constants.BLACK_DYE))),
    /**
     * BONE_MEAL
     */
    BONE_MEAL(createStack(Constants.INK_SACK, getDye("WHITE_DYE"))),

    /**
     * BAT_SPAWN_EGG
     */
    BAT_SPAWN_EGG(createSpawnEgg(EntityType.BAT)),
    /**
     * BEE_SPAWN_EGG
     */
    BEE_SPAWN_EGG(createSpawnEgg("BEE")),
    /**
     * BLAZE_SPAWN_EGG
     */
    BLAZE_SPAWN_EGG(createSpawnEgg(EntityType.BLAZE)),
    /**
     * CAT_SPAWN_EGG
     */
    CAT_SPAWN_EGG(createSpawnEgg(EntityType.OCELOT, (short) -2)),
    /**
     * CAVE_SPIDER_SPAWN_EGG
     */
    CAVE_SPIDER_SPAWN_EGG(createSpawnEgg(EntityType.CAVE_SPIDER)),
    /**
     * CHICKEN_SPAWN_EGG
     */
    CHICKEN_SPAWN_EGG(createSpawnEgg(EntityType.CHICKEN)),
    /**
     * COD_SPAWN_EGG
     */
    COD_SPAWN_EGG(createSpawnEgg(Constants.RAW_FISH, (short) 0)),
    /**
     * COW_SPAWN_EGG
     */
    COW_SPAWN_EGG(createSpawnEgg(EntityType.COW)),
    /**
     * CREEPER_SPAWN_EGG
     */
    CREEPER_SPAWN_EGG(createSpawnEgg(EntityType.CREEPER)),
    /**
     * DOLPHIN_SPAWN_EGG
     */
    DOLPHIN_SPAWN_EGG(createSpawnEgg("DOLPHIN")),
    /**
     * DONKEY_SPAWN_EGG
     */
    DONKEY_SPAWN_EGG(createSpawnEgg("DONKEY")),
    /**
     * DROWNED_SPAWN_EGG
     */
    DROWNED_SPAWN_EGG(createSpawnEgg("DROWNED")),
    /**
     * ELDER_GUARDIAN_SPAWN_EGG
     */
    ELDER_GUARDIAN_SPAWN_EGG(createSpawnEgg("ELDER_GUARDIAN")),
    /**
     * ENDERMAN_SPAWN_EGG
     */
    ENDERMAN_SPAWN_EGG(createSpawnEgg("ENDERMAN")),
    /**
     * ENDERMITE_SPAWN_EGG
     */
    ENDERMITE_SPAWN_EGG(createSpawnEgg("ENDERMITE")),
    /**
     * EVOKER_SPAWN_EGG
     */
    EVOKER_SPAWN_EGG(createSpawnEgg("EVOKER")),
    /**
     * FOX_SPAWN_EGG
     */
    FOX_SPAWN_EGG(createSpawnEgg("FOX")),
    /**
     * GHAST_SPAWN_EGG
     */
    GHAST_SPAWN_EGG(createSpawnEgg(EntityType.GHAST)),
    /**
     * GLOW_SQUID_SPAWN_EGG
     */
    GLOW_SQUID_SPAWN_EGG(createSpawnEgg("GLOW_SQUID")),
    /**
     * GOAT_SPAWN_EGG
     */
    GOAT_SPAWN_EGG(createSpawnEgg("GOAT")),
    /**
     * GUARDIAN_SPAWN_EGG
     */
    GUARDIAN_SPAWN_EGG(createSpawnEgg(EntityType.GUARDIAN)),
    /**
     * HOGLIN_SPAWN_EGG
     */
    HOGLIN_SPAWN_EGG(createSpawnEgg("HOGLIN")),
    /**
     * HORSE_SPAWN_EGG
     */
    HORSE_SPAWN_EGG(createSpawnEgg(EntityType.HORSE)),
    /**
     * HUSK_SPAWN_EGG
     */
    HUSK_SPAWN_EGG(createSpawnEgg("HUSK")),
    /**
     * LLAMA_SPAWN_EGG
     */
    LLAMA_SPAWN_EGG(createSpawnEgg("LLAMA")),
    /**
     * MAGMA_CUBE_SPAWN_EGG
     */
    MAGMA_CUBE_SPAWN_EGG(createSpawnEgg(EntityType.MAGMA_CUBE)),
    /**
     * MOOSHROOM_SPAWN_EGG
     */
    MOOSHROOM_SPAWN_EGG(createSpawnEgg("MUSHROOM_COW")),
    /**
     * MULE_SPAWN_EGG
     */
    MULE_SPAWN_EGG(createSpawnEgg("MULE")),
    /**
     * OCELOT_SPAWN_EGG
     */
    OCELOT_SPAWN_EGG(createSpawnEgg(EntityType.OCELOT)),
    /**
     * PANDA_SPAWN_EGG
     */
    PANDA_SPAWN_EGG(createSpawnEgg("PANDA")),
    /**
     * PARROT_SPAWN_EGG
     */
    PARROT_SPAWN_EGG(createSpawnEgg("PARROT")),
    /**
     * PHANTOM_SPAWN_EGG
     */
    PHANTOM_SPAWN_EGG(createSpawnEgg("PHANTOM")),
    /**
     * PIG_SPAWN_EGG
     */
    PIG_SPAWN_EGG(createSpawnEgg(EntityType.PIG)),
    /**
     * PIGLIN_SPAWN_EGG
     */
    PIGLIN_SPAWN_EGG(createSpawnEgg("PIGLIN")),
    /**
     * PIGLIN_BRUTE_SPAWN_EGG
     */
    PIGLIN_BRUTE_SPAWN_EGG(createSpawnEgg("PIGLIN_BRUTE")),
    /**
     * PILLAGER_SPAWN_EGG
     */
    PILLAGER_SPAWN_EGG(createSpawnEgg("PILLAGER")),
    /**
     * POLAR_BEAR_SPAWN_EGG
     */
    POLAR_BEAR_SPAWN_EGG(createSpawnEgg("POLAR_BEAR")),
    /**
     * PUFFERFISH_SPAWN_EGG
     */
    PUFFERFISH_SPAWN_EGG(createSpawnEgg(Constants.RAW_FISH, (short) 3)),
    /**
     * RABBIT_SPAWN_EGG
     */
    RABBIT_SPAWN_EGG(createSpawnEgg(EntityType.RABBIT)),
    /**
     * RAVAGER_SPAWN_EGG
     */
    RAVAGER_SPAWN_EGG(createSpawnEgg("RAVAGER")),
    /**
     * SALMON_SPAWN_EGG
     */
    SALMON_SPAWN_EGG(createSpawnEgg(Constants.RAW_FISH, (short) 1)),
    /**
     * SHEEP_SPAWN_EGG
     */
    SHEEP_SPAWN_EGG(createSpawnEgg(EntityType.SHEEP)),
    /**
     * SHULKER_SPAWN_EGG
     */
    SHULKER_SPAWN_EGG(createSpawnEgg("SHULKER")),
    /**
     * SILVERFISH_SPAWN_EGG
     */
    SILVERFISH_SPAWN_EGG(createSpawnEgg(EntityType.SILVERFISH)),
    /**
     * SKELETON_SPAWN_EGG
     */
    SKELETON_SPAWN_EGG(createSpawnEgg(EntityType.SKELETON)),
    /**
     * SKELETON_HORSE_SPAWN_EGG
     */
    SKELETON_HORSE_SPAWN_EGG(createSpawnEgg("SKELETON_HORSE")),
    /**
     * SLIME_SPAWN_EGG
     */
    SLIME_SPAWN_EGG(createSpawnEgg(EntityType.SLIME)),
    /**
     * SPIDER_SPAWN_EGG
     */
    SPIDER_SPAWN_EGG(createSpawnEgg(EntityType.SPIDER)),
    /**
     * SQUID_SPAWN_EGG
     */
    SQUID_SPAWN_EGG(createSpawnEgg(EntityType.SQUID)),
    /**
     * STRAY_SPAWN_EGG
     */
    STRAY_SPAWN_EGG(createSpawnEgg("STRAY")),
    /**
     * STRIDER_SPAWN_EGG
     */
    STRIDER_SPAWN_EGG(createSpawnEgg("STRIDER")),
    /**
     * TRADER_LLAMA_SPAWN_EGG
     */
    TRADER_LLAMA_SPAWN_EGG(createSpawnEgg("TRADER_LLAMA")),
    /**
     * TROPICAL_FISH_SPAWN_EGG
     */
    TROPICAL_FISH_SPAWN_EGG(createSpawnEgg(Constants.RAW_FISH, (short) 2)),
    /**
     * TURTLE_SPAWN_EGG
     */
    TURTLE_SPAWN_EGG(createSpawnEgg("TURTLE")),
    /**
     * VEX_SPAWN_EGG
     */
    VEX_SPAWN_EGG(createSpawnEgg("VEX")),
    /**
     * VILLAGER_SPAWN_EGG
     */
    VILLAGER_SPAWN_EGG(createSpawnEgg(EntityType.VILLAGER)),
    /**
     * VINDICATOR_SPAWN_EGG
     */
    VINDICATOR_SPAWN_EGG(createSpawnEgg("VINDICATOR")),
    /**
     * WANDERING_TRADER_SPAWN_EGG
     */
    WANDERING_TRADER_SPAWN_EGG(createSpawnEgg("WANDERING_TRADER")),
    /**
     * WITCH_SPAWN_EGG
     */
    WITCH_SPAWN_EGG(createSpawnEgg(EntityType.WITCH)),
    /**
     * WITHER_SKELETON_SPAWN_EGG
     */
    WITHER_SKELETON_SPAWN_EGG(createSpawnEgg("WITHER_SKELETON")),
    /**
     * WOLF_SPAWN_EGG
     */
    WOLF_SPAWN_EGG(createSpawnEgg(EntityType.WOLF)),
    /**
     * ZOGLIN_SPAWN_EGG
     */
    ZOGLIN_SPAWN_EGG(createSpawnEgg("ZOGLIN")),
    /**
     * ZOMBIE_SPAWN_EGG
     */
    ZOMBIE_SPAWN_EGG(createSpawnEgg(EntityType.ZOMBIE)),
    /**
     * ZOMBIE_HORSE_SPAWN_EGG
     */
    ZOMBIE_HORSE_SPAWN_EGG(createSpawnEgg("ZOMBIE_HORSE")),
    /**
     * ZOMBIE_VILLAGER_SPAWN_EGG
     */
    ZOMBIE_VILLAGER_SPAWN_EGG(createSpawnEgg("ZOMBIE_VILLAGER")),
    /**
     * ZOMBIFIED_PIGLIN_SPAWN_EGG
     */
    ZOMBIFIED_PIGLIN_SPAWN_EGG(createSpawnEgg("PIG_ZOMBIE")),
    /**
     * ZOMBIE_PIGMAN_SPAWN
     */
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

    /**
     * Returns the legacy {@link ItemStack} corresponding to the given modern material name.
     *
     * @param materialName The modern material name (e.g., "WHITE_STAINED_GLASS").
     * @return The matching legacy ItemStack, or null if not found.
     */
    @Nonnull
    public static ItemStack getItemStack(String materialName) {
        return getItemStack(materialName, 1);
    }

    /**
     * Returns a clone of the legacy {@link ItemStack} for the given material name,
     * with a specified stack amount.
     *
     * @param materialName The modern material name.
     * @param amount       The desired stack amount.
     * @return A cloned ItemStack with the specified amount, or null if not found.
     */
    @Nonnull
    public static ItemStack getItemStack(String materialName, int amount) {
        return getItemStack(materialName, amount, null);
    }

    /**
     * Creates a legacy {@link ItemStack} for a specific material and data value.
     *
     * @param materialName The legacy {@link Material}.
     * @param amount       The stack amount.
     * @param data         The legacy data value.
     * @return A new ItemStack for the given material and data.
     */
    @Nonnull
    public static ItemStack getItemStack(String materialName, int amount, Byte data) {
        Materials material = STACKS.get(materialName);
        if (material != null) {
            ItemStack stack = material.itemStack;
            if (stack != null) {
                ItemStack clone = stack.clone();
                clone.setAmount(amount);
                return clone;
            }
        } else {
            Material bukkitMaterial = Material.getMaterial(materialName);
            if (bukkitMaterial != null) {
                return new ItemStack(bukkitMaterial, 1, (short) 0, data);
            }
        }
        return defaultStack;
    }

    /**
     * Returns the {@link ItemStack} corresponding to the given modern material name.
     *
     * @param amount The stack amount.
     * @return The matching legacy ItemStack, or null if not found.
     */
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

    private static ItemStack createSpawnEgg(String entityType) {
        return (createSpawnEgg(EntityType.fromName(entityType)));

    }

    private static ItemStack createSpawnEgg(String entityType, short entityDatatype) {
        return (createSpawnEgg(EntityType.fromName(entityType), entityDatatype));
    }

    /**
     * Creates a spawn egg ItemStack.
     *
     * @param entityType the type off egg.
     * @return New ItemStack with specified material and data.
     */
    public static ItemStack createSpawnEgg(EntityType entityType) {
        return createSpawnEgg(entityType, (short) -1);
    }

    /**
     * Creates a spawn egg ItemStack.
     *
     * @param entityType     the type off egg.
     * @param entityDatatype the datatype for the entity.
     * @return New ItemStack with specified material and data.
     */
    public static ItemStack createSpawnEgg(EntityType entityType, short entityDatatype) {
        if (entityType == EntityType.OCELOT && entityDatatype == (short) -2) {
            EntityType type = EntityType.fromName("CAT");
            if (type != null)
                entityType = type;
        }
        Material material = Material.getMaterial("MONSTER_EGG");
        if (material == null) return null;
        ItemStack itemStack;
        if (entityDatatype > 0)
            itemStack = new ItemStack(material, 1, entityDatatype);
        else
            itemStack = new ItemStack(material, 1);

        SpawnEgg spawnEgg = (SpawnEgg) itemStack.getData();
        if (spawnEgg == null) return itemStack;
        if (entityType == null)
            spawnEgg.setSpawnedType(EntityType.SHEEP);
        else
            spawnEgg.setSpawnedType(entityType);
        itemStack.setData(spawnEgg);
        return itemStack;
    }

    private static short getWoodTypeData(final String itemName) {
        if (itemName.startsWith("OAK")) {
            return 0;
        }
        if (itemName.startsWith(Constants.SPRUCE)) {
            return 1;
        }
        if (itemName.startsWith(Constants.BIRCH)) {
            return 2;
        }
        if (itemName.startsWith(Constants.JUNGLE)) {
            return 3;
        }
        if (itemName.startsWith(Constants.ACACIA)) {
            return 4;
        }
        if (itemName.startsWith(Constants.DARK_OAK)) {
            return 5;
        }
        return -1;
    }

    /**
     * Check the color of a item from the string.
     *
     * @param color the text to check if it contains a color.
     * @return the color code or -1 when it does not find one.
     */
    public static short checkColor(String color) {
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

    /**
     * Check the dye color of a item from the string.
     *
     * @param itemName the item enum name to check if it contains a color.
     * @return the color code or -1 when it does not find one.
     */
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

    private static class Constants {
        private static final String STAINED_GLASS_PANE = "STAINED_GLASS_PANE";
        private static final String STAINED_GLASS = "STAINED_GLASS";
        private static final String WOOL = "WOOL";
        private static final String CARPET = "CARPET";
        private static final String CONCRETE = "CONCRETE";
        private static final String CONCRETE_POWDER = "CONCRETE_POWDER";
        private static final String STAINED_CLAY = "STAINED_CLAY";
        private static final String INK_SACK = "INK_SACK";
        private static final String WOOD_STEP = "WOOD_STEP";
        private static final String SPRUCE = "SPRUCE";
        private static final String BIRCH = "BIRCH";
        private static final String JUNGLE = "JUNGLE";
        private static final String ACACIA = "ACACIA";
        private static final String STONE = "STONE";
        private static final String RAW_FISH = "RAW_FISH";
        private static final String FENCE = "FENCE";
        private static final String FENCE_GATE = "FENCE_GATE";
        private static final String WOOD_BUTTON = "WOOD_BUTTON";
        private static final String LEAVES = "LEAVES";
        private static final String SAPLING = "SAPLING";
        private static final String SIGN = "SIGN";
        private static final String BLACK_DYE = "BLACK_DYE";
        private static final String DARK_OAK = "DARK_OAK";
    }
}
