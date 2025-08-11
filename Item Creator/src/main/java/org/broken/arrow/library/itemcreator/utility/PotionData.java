package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a mapping of potion data types to Minecraft's {@link PotionType},
 * supporting both legacy and modern server versions. This enum abstracts potion logic,
 * including special base types (e.g., {@code AWKWARD}, {@code THICK}) and variations with
 * modifiers such as {@link Type#LONG} and {@link Type#STRONG}.
 * <p>
 * Designed to follow the modern style used in Spigot and Paper APIs as of Minecraft 1.20 and above.
 */
public enum PotionData {
    /**
     * Uncraftable potion (special case).
     */
    UNCRAFTABLE(getUncraftable(), Type.NORMAL),
    /**
     * Water potion.
     */
    WATER(PotionType.WATER, Type.NORMAL),
    /**
     * Mundane potion (no special effects).
     */
    MUNDANE(PotionType.MUNDANE, Type.NORMAL),
    /**
     * Thick potion (usually for brewing).
     */
    THICK(PotionType.THICK, Type.NORMAL),
    /**
     * Awkward potion (base potion for many effects).
     */
    AWKWARD(PotionType.AWKWARD, Type.NORMAL),
    /**
     * Night Vision potion, normal duration.
     */
    NIGHT_VISION(PotionType.NIGHT_VISION, Type.NORMAL),
    /**
     * Night Vision potion, long duration.
     */
    LONG_NIGHT_VISION(PotionType.NIGHT_VISION, Type.LONG),
    /**
     * Invisibility potion, normal duration.
     */
    INVISIBILITY(PotionType.INVISIBILITY, Type.NORMAL),
    /**
     * Invisibility potion, long duration.
     */
    LONG_INVISIBILITY(PotionType.INVISIBILITY, Type.LONG),
    /**
     * Jump boost potion, normal duration.
     */
    JUMP(PotionType.JUMP, Type.NORMAL),
    /**
     * Jump boost potion, long duration.
     */
    LONG_LEAPING(PotionType.JUMP, Type.LONG),
    /**
     * Jump boost potion, strong effect.
     */
    STRONG_LEAPING(PotionType.JUMP, Type.STRONG),
    /**
     * Fire Resistance potion, normal duration.
     */
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, Type.NORMAL),
    /**
     * Fire Resistance potion, long duration.
     */
    LONG_FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, Type.LONG),
    /**
     * Speed potion, normal duration.
     */
    SPEED(PotionType.SPEED, Type.NORMAL),
    /**
     * Speed potion, long duration.
     */
    LONG_SWIFTNESS(PotionType.SPEED, Type.LONG),
    /**
     * Speed potion, strong effect.
     */
    STRONG_SWIFTNESS(PotionType.SPEED, Type.STRONG),
    /**
     * Slowness potion, normal duration.
     */
    SLOWNESS(PotionType.SLOWNESS, Type.NORMAL),
    /**
     * Slowness potion, long duration.
     */
    LONG_SLOWNESS(PotionType.SLOWNESS, Type.LONG),
    /**
     * Slowness potion, strong effect.
     */
    STRONG_SLOWNESS(PotionType.SLOWNESS, Type.STRONG),
    /**
     * Water Breathing potion, normal duration.
     */
    WATER_BREATHING(PotionType.WATER_BREATHING, Type.NORMAL),
    /**
     * Water Breathing potion, long duration.
     */
    LONG_WATER_BREATHING(PotionType.WATER_BREATHING, Type.LONG),
    /**
     * Instant Heal potion, normal effect.
     */
    INSTANT_HEAL(PotionType.INSTANT_HEAL, Type.NORMAL),
    /**
     * Instant Heal potion, strong effect.
     */
    STRONG_HEALING(PotionType.INSTANT_HEAL, Type.STRONG),
    /**
     * Instant Damage potion, normal effect.
     */
    INSTANT_DAMAGE(PotionType.INSTANT_DAMAGE, Type.NORMAL),
    /**
     * Instant Damage potion, strong effect.
     */
    STRONG_HARMING(PotionType.INSTANT_DAMAGE, Type.STRONG),
    /**
     * Poison potion, normal duration.
     */
    POISON(PotionType.POISON, Type.NORMAL),
    /**
     * Poison potion, long duration.
     */
    LONG_POISON(PotionType.POISON, Type.LONG),
    /**
     * Poison potion, strong effect.
     */
    STRONG_POISON(PotionType.POISON, Type.STRONG),
    /**
     * Regeneration potion, normal duration.
     */
    REGEN(PotionType.REGEN, Type.NORMAL),
    /**
     * Regeneration potion, long duration.
     */
    LONG_REGENERATION(PotionType.REGEN, Type.LONG),
    /**
     * Regeneration potion, strong effect.
     */
    STRONG_REGENERATION(PotionType.REGEN, Type.STRONG),
    /**
     * Strength potion, normal duration.
     */
    STRENGTH(PotionType.STRENGTH, Type.NORMAL),
    /**
     * Strength potion, long duration.
     */
    LONG_STRENGTH(PotionType.STRENGTH, Type.LONG),
    /**
     * Strength potion, strong effect.
     */
    STRONG_STRENGTH(PotionType.STRENGTH, Type.STRONG),
    /**
     * Weakness potion, normal duration.
     */
    WEAKNESS(PotionType.WEAKNESS, Type.NORMAL),
    /**
     * Weakness potion, long duration.
     */
    LONG_WEAKNESS(PotionType.WEAKNESS, Type.LONG),
    /**
     * Luck potion, normal duration (if applicable).
     */
    LUCK(PotionType.LUCK, Type.NORMAL),
    /**
     * Turtle Master potion, normal duration.
     */
    TURTLE_MASTER(PotionType.TURTLE_MASTER, Type.NORMAL),
    /**
     * Turtle Master potion, long duration.
     */
    LONG_TURTLE_MASTER(PotionType.TURTLE_MASTER, Type.LONG),
    /**
     * Turtle Master potion, strong effect.
     */
    STRONG_TURTLE_MASTER(PotionType.TURTLE_MASTER, Type.STRONG),
    /**
     * Slow Falling potion, normal duration.
     */
    SLOW_FALLING(PotionType.SLOW_FALLING, Type.NORMAL),
    /**
     * Slow Falling potion, long duration.
     */
    LONG_SLOW_FALLING(PotionType.SLOW_FALLING, Type.LONG);;

    private final PotionType potionType;
    private final Type type;
    private final float serverVersion = ItemCreator.getServerVersion();

    /**
     * Constructs a new {@code PotionData} entry.
     *
     * @param potionType The base {@link PotionType} associated with this potion data,
     *                   or {@code null} for special cases like MUNDANE or WATER.
     * @param type       The {@link Type} modifier for the potion, e.g., NORMAL, LONG, or STRONG.
     */
    PotionData(@Nonnull final PotionType potionType, @Nonnull final Type type) {
        this.potionType = potionType;
        this.type = type;
    }

    /**
     * Retrieves the associated {@link PotionType} for this enum constant. Handles version differences
     * and special cases like uncraftable or water potions.
     *
     * @return The correct {@link PotionType} representing this potion data.
     */
    @Nonnull
    public PotionType getPotionType() {
        if (this.serverVersion < 20.0) {
            return this.potionType;
        } else {
            return this.getPotionMapping();
        }
    }

    /**
     * Find the portion mapping from the bukkit potion type.
     *
     * @param bukkitPortionType the type you want to find.
     * @return the PotionData instance or null if it could not find the PotionType.
     */
    @Nullable
    public static PotionData findPotionByType(PotionType bukkitPortionType) {
        PotionData[] potionTypes = values();
        for (PotionData potion : potionTypes) {
            if (potion.getPotionType() == bukkitPortionType)
                return potion;
        }
        return null;
    }

    /**
     * Find the portion from the bukkit potion type.
     *
     * @param bukkitPortionType the type you want to find.
     * @return the PotionData instance or null if it could not find the PotionType.
     */
    @Nullable
    public static PotionType findPotionByName(String bukkitPortionType) {
        PotionType[] potionTypes = PotionType.values();
        String bukkitPortion = bukkitPortionType.toUpperCase();
        for (PotionType potion : potionTypes) {
            if (potion.name().equals(bukkitPortion))
                return potion;
        }
        return null;
    }

    /**
     * Returns the {@link Type} modifier associated with this potion (e.g., LONG, STRONG).
     *
     * @return The {@link Type} modifier.
     */
    @Nonnull
    public Type getModifier() {
        return type;
    }

    /**
     * Resolves the appropriate {@link PotionType} for this enum constant,
     * mapping to the modern potion representation (as introduced in newer Minecraft versions).
     *
     * @return the resolved {@link PotionType}, matching the current enum and modifier.
     */
    @Nonnull
    private PotionType getPotionMapping() {
        switch (this) {
            case LONG_NIGHT_VISION:
                return PotionType.LONG_NIGHT_VISION;
            case LONG_INVISIBILITY:
                return PotionType.LONG_INVISIBILITY;
            case STRONG_LEAPING:
                return PotionType.STRONG_LEAPING;
            case LONG_LEAPING:
                return PotionType.LONG_LEAPING;
            case LONG_FIRE_RESISTANCE:
                return PotionType.LONG_FIRE_RESISTANCE;
            case LONG_SWIFTNESS:
                return PotionType.LONG_SWIFTNESS;
            case STRONG_SWIFTNESS:
                return PotionType.STRONG_SWIFTNESS;
            case LONG_SLOWNESS:
                return PotionType.LONG_SLOWNESS;
            case STRONG_SLOWNESS:
                return PotionType.STRONG_SLOWNESS;
            case LONG_WATER_BREATHING:
                return PotionType.LONG_WATER_BREATHING;
            case STRONG_HEALING:
                return PotionType.STRONG_HEALING;
            case STRONG_HARMING:
                return PotionType.STRONG_HARMING;
            case LONG_POISON:
                return PotionType.LONG_POISON;
            case STRONG_POISON:
                return PotionType.STRONG_POISON;
            case LONG_REGENERATION:
                return PotionType.LONG_REGENERATION;
            case STRONG_REGENERATION:
                return PotionType.STRONG_REGENERATION;
            case LONG_STRENGTH:
                return PotionType.LONG_STRENGTH;
            case STRONG_STRENGTH:
                return PotionType.STRONG_STRENGTH;
            case LONG_WEAKNESS:
                return PotionType.LONG_WEAKNESS;
            case LONG_TURTLE_MASTER:
                return PotionType.LONG_TURTLE_MASTER;
            case STRONG_TURTLE_MASTER:
                return PotionType.STRONG_TURTLE_MASTER;
            case LONG_SLOW_FALLING:
                return PotionType.LONG_SLOW_FALLING;
            default:
                return this.potionType;
        }
    }

    /**
     * Enum for potion modifiers that represent how the potion is enhanced or extended.
     */
    public enum Type {
        /**
         * The default version of the potion with standard duration and potency.
         */
        NORMAL,
        /**
         * A longer-lasting version of the potion, typically increasing the duration
         * from 3 minutes to 8 minutes.
         */
        LONG,
        /**
         * A stronger version of the potion with amplified effects,
         * typically at the cost of half the duration.
         * <p>
         * The only exception is the Turtle Master potion,
         * which retains the same duration as the base potion.
         */
        STRONG,
    }

    /**
     * Attempting to get the uncraftable type, this default back
     * to mundane on newer Minecraft versions like 1.21 and beyond.
     *
     * @return A {@link PotionType#UNCRAFTABLE} if it exist
     * other cases {@link PotionType#MUNDANE}
     */
    @Nonnull
    private static PotionType getUncraftable() {
        PotionType potion = findPotionByName("UNCRAFTABLE");
        return potion != null ? potion : PotionType.MUNDANE;
    }

}
