package org.broken.arrow.library.itemcreator.meta.potion;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mapping of potion data types to Minecraft's {@link PotionType},
 * supporting both legacy and modern server versions. This enum abstracts potion logic,
 * including special base types (e.g., {@code AWKWARD}, {@code THICK}) and variations with
 * modifiers such as {@link PotionModifier#LONG} and {@link PotionModifier#STRONG}.
 * <p>
 * Designed to follow the modern style used in Spigot and Paper APIs as of Minecraft 1.20 and above.
 */
public enum PotionTypeWrapper {
    /**
     * Uncraftable potion (special case).
     */
    UNCRAFTABLE(getUncraftable(), PotionModifier.NORMAL),
    /**
     * Water potion.
     */
    WATER(PotionType.WATER, PotionModifier.NORMAL),
    /**
     * Mundane potion (no special effects).
     */
    MUNDANE(PotionType.MUNDANE, PotionModifier.NORMAL),
    /**
     * Thick potion (usually for brewing).
     */
    THICK(PotionType.THICK, PotionModifier.NORMAL),
    /**
     * Awkward potion (base potion for many effects).
     */
    AWKWARD(PotionType.AWKWARD, PotionModifier.NORMAL),
    /**
     * Night Vision potion, normal duration.
     */
    NIGHT_VISION(PotionType.NIGHT_VISION, PotionModifier.NORMAL),
    /**
     * Night Vision potion, long duration.
     */
    LONG_NIGHT_VISION(PotionType.NIGHT_VISION, PotionModifier.LONG),
    /**
     * Invisibility potion, normal duration.
     */
    INVISIBILITY(PotionType.INVISIBILITY, PotionModifier.NORMAL),
    /**
     * Invisibility potion, long duration.
     */
    LONG_INVISIBILITY(PotionType.INVISIBILITY, PotionModifier.LONG),
    /**
     * Jump boost potion, normal duration.
     */
    JUMP(PotionType.JUMP, PotionModifier.NORMAL),
    /**
     * Jump boost potion, long duration.
     */
    LONG_LEAPING(PotionType.JUMP, PotionModifier.LONG),
    /**
     * Jump boost potion, strong effect.
     */
    STRONG_LEAPING(PotionType.JUMP, PotionModifier.STRONG),
    /**
     * Fire Resistance potion, normal duration.
     */
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, PotionModifier.NORMAL),
    /**
     * Fire Resistance potion, long duration.
     */
    LONG_FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, PotionModifier.LONG),
    /**
     * Speed potion, normal duration.
     */
    SPEED(PotionType.SPEED, PotionModifier.NORMAL),
    /**
     * Speed potion, long duration.
     */
    LONG_SWIFTNESS(PotionType.SPEED, PotionModifier.LONG),
    /**
     * Speed potion, strong effect.
     */
    STRONG_SWIFTNESS(PotionType.SPEED, PotionModifier.STRONG),
    /**
     * Slowness potion, normal duration.
     */
    SLOWNESS(PotionType.SLOWNESS, PotionModifier.NORMAL),
    /**
     * Slowness potion, long duration.
     */
    LONG_SLOWNESS(PotionType.SLOWNESS, PotionModifier.LONG),
    /**
     * Slowness potion, strong effect.
     */
    STRONG_SLOWNESS(PotionType.SLOWNESS, PotionModifier.STRONG),
    /**
     * Water Breathing potion, normal duration.
     */
    WATER_BREATHING(PotionType.WATER_BREATHING, PotionModifier.NORMAL),
    /**
     * Water Breathing potion, long duration.
     */
    LONG_WATER_BREATHING(PotionType.WATER_BREATHING, PotionModifier.LONG),
    /**
     * Instant Heal potion, normal effect.
     */
    INSTANT_HEAL(PotionType.INSTANT_HEAL, PotionModifier.NORMAL),
    /**
     * Instant Heal potion, strong effect.
     */
    STRONG_HEALING(PotionType.INSTANT_HEAL, PotionModifier.STRONG),
    /**
     * Instant Damage potion, normal effect.
     */
    INSTANT_DAMAGE(PotionType.INSTANT_DAMAGE, PotionModifier.NORMAL),
    /**
     * Instant Damage potion, strong effect.
     */
    STRONG_HARMING(PotionType.INSTANT_DAMAGE, PotionModifier.STRONG),
    /**
     * Poison potion, normal duration.
     */
    POISON(PotionType.POISON, PotionModifier.NORMAL),
    /**
     * Poison potion, long duration.
     */
    LONG_POISON(PotionType.POISON, PotionModifier.LONG),
    /**
     * Poison potion, strong effect.
     */
    STRONG_POISON(PotionType.POISON, PotionModifier.STRONG),
    /**
     * Regeneration potion, normal duration.
     */
    REGEN(PotionType.REGEN, PotionModifier.NORMAL),
    /**
     * Regeneration potion, long duration.
     */
    LONG_REGENERATION(PotionType.REGEN, PotionModifier.LONG),
    /**
     * Regeneration potion, strong effect.
     */
    STRONG_REGENERATION(PotionType.REGEN, PotionModifier.STRONG),
    /**
     * Strength potion, normal duration.
     */
    STRENGTH(PotionType.STRENGTH, PotionModifier.NORMAL),
    /**
     * Strength potion, long duration.
     */
    LONG_STRENGTH(PotionType.STRENGTH, PotionModifier.LONG),
    /**
     * Strength potion, strong effect.
     */
    STRONG_STRENGTH(PotionType.STRENGTH, PotionModifier.STRONG),
    /**
     * Weakness potion, normal duration.
     */
    WEAKNESS(PotionType.WEAKNESS, PotionModifier.NORMAL),
    /**
     * Weakness potion, long duration.
     */
    LONG_WEAKNESS(PotionType.WEAKNESS, PotionModifier.LONG),
    /**
     * Luck potion, normal duration (if applicable).
     */
    LUCK(PotionType.LUCK, PotionModifier.NORMAL),
    /**
     * Turtle Master potion, normal duration.
     */
    TURTLE_MASTER(PotionType.TURTLE_MASTER, PotionModifier.NORMAL),
    /**
     * Turtle Master potion, long duration.
     */
    LONG_TURTLE_MASTER(PotionType.TURTLE_MASTER, PotionModifier.LONG),
    /**
     * Turtle Master potion, strong effect.
     */
    STRONG_TURTLE_MASTER(PotionType.TURTLE_MASTER, PotionModifier.STRONG),
    /**
     * Slow Falling potion, normal duration.
     */
    SLOW_FALLING(PotionType.SLOW_FALLING, PotionModifier.NORMAL),
    /**
     * Slow Falling potion, long duration.
     */
    LONG_SLOW_FALLING(PotionType.SLOW_FALLING, PotionModifier.LONG);

    private static final Map<String, PotionTypeWrapper> POTION_TYPE_NAME = new HashMap<>();
    private final PotionType potionType;
    private final PotionModifier potionModifier;
    private final float serverVersion = ItemCreator.getServerVersion();

    /**
     * Constructs a new {@code PotionData} entry.
     *
     * @param potionType     The base {@link PotionType} associated with this potion data,
     *                       or {@code null} for special cases like MUNDANE or WATER.
     * @param potionModifier The {@link PotionModifier} modifier for the potion, e.g., NORMAL, LONG, or STRONG.
     */
    PotionTypeWrapper(@Nonnull final PotionType potionType, @Nonnull final PotionModifier potionModifier) {
        this.potionType = potionType;
        this.potionModifier = potionModifier;
    }

    static {
        for (PotionTypeWrapper data : values()) {
            POTION_TYPE_NAME.put(data.getPotionType().name(), data);
        }
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
     * Finds the {@link PotionTypeWrapper} corresponding to a Bukkit {@link PotionType}.
     * <p>
     * This method provides a fast lookup for modern potion mappings and is the recommended
     * way to resolve wrappers when working with Bukkit's potion system on 1.20.2 and newer.
     * Older Minecraft versions do not offer the same variety of potion types, if you need
     * compatibility from 1.8.8 and up, use {@link #findPotionByName(String)} instead.
     * </p>
     *
     * @param bukkitPotionType the Bukkit {@link PotionType} to find.
     * @return the matching {@link PotionTypeWrapper}, or {@code null} if no mapping exists.
     */
    @Nullable
    public static PotionTypeWrapper findPotionByType(final PotionType bukkitPotionType) {
        return POTION_TYPE_NAME.get(bukkitPotionType.name());
    }

    /**
     * Finds the {@link PotionTypeWrapper} associated with the given potion name.
     * <p>
     * This method supports both modern and legacy potion naming. If the provided name matches
     * an entry in {@code POTION_TYPE_NAME}, that entry will be returned first.
     * Otherwise, it attempts to resolve the name using {@link PotionTypeWrapper#valueOf(String)}.
     * </p>
     *
     * <p><strong>Note:</strong> Using legacy potion names is discouraged, as it prevents
     * easy retrieval of potion effect types. Prefer modern {@link PotionType} values
     * when possible (see {@link PotionTypeWrapper}), and then use {@link #getModifier()}
     * to obtain the correct potion modifier.</p>
     *
     * @param name the potion name to find (case-insensitive).
     * @return the corresponding {@link PotionTypeWrapper}, or {@code null} if no match is found.
     */
    @Nullable
    public static PotionTypeWrapper findPotionByName(final String name) {
        if (name == null) return null;

        String bukkitPortion = name.toUpperCase();
        PotionTypeWrapper potionTypeWrapper = POTION_TYPE_NAME.get(bukkitPortion);
        if (potionTypeWrapper != null)
            return potionTypeWrapper;
        try {
            return PotionTypeWrapper.valueOf(bukkitPortion);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resolves the Bukkit {@link PotionType} from its string name.
     * <p>
     * This method internally uses {@link #findPotionByName(String)} to obtain the
     * corresponding {@link PotionTypeWrapper}, and then returns its underlying
     * {@link PotionType}.
     * </p>
     *
     * <p><strong>Note:</strong> On Minecraft versions below 1.20.2, many potions
     * do not have distinct {@link PotionType}s. This method may therefore return a
     * less specific type or {@code null} if the enum name is not valid.</p>
     *
     * @param bukkitPotionType the name of the Bukkit potion type (case-insensitive).
     * @return the corresponding {@link PotionType}, or {@code null} if not found.
     */
    @Nullable
    public static PotionType findPotionTypeByName(final String bukkitPotionType) {
        String bukkitPotion = bukkitPotionType.toUpperCase();
        PotionTypeWrapper potionByName = findPotionByName(bukkitPotion);
        if (potionByName != null)
            return potionByName.getPotionType();
        return null;
    }

    /**
     * Returns the {@link PotionModifier} modifier associated with this potion (e.g., LONG, STRONG).
     *
     * @return The {@link PotionModifier} modifier.
     */
    @Nonnull
    public PotionModifier getModifier() {
        return potionModifier;
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
     * Attempting to get the uncraftable type, this default back
     * to mundane on newer Minecraft versions like 1.21 and beyond.
     *
     * @return A {@link PotionType#UNCRAFTABLE} if it exists
     * other cases {@link PotionType#MUNDANE}
     */
    @Nonnull
    private static PotionType getUncraftable() {
        PotionType potion = findPotionTypeByName("UNCRAFTABLE");
        return potion != null ? potion : PotionType.MUNDANE;
    }

}
