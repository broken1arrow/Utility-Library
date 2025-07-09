package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum PotionData {
    UNCRAFTABLE(null, Type.NORMAL),
    WATER(null, Type.NORMAL),
    MUNDANE(null, Type.NORMAL),
    THICK(null, Type.NORMAL),
    AWKWARD(null, Type.NORMAL),
    NIGHT_VISION(PotionType.NIGHT_VISION, Type.NORMAL),
    LONG_NIGHT_VISION(PotionType.NIGHT_VISION, Type.LONG),
    INVISIBILITY(PotionType.INVISIBILITY, Type.NORMAL),
    LONG_INVISIBILITY(PotionType.INVISIBILITY, Type.LONG),
    JUMP(PotionType.JUMP, Type.NORMAL),
    LONG_LEAPING(PotionType.JUMP, Type.LONG),
    STRONG_LEAPING(PotionType.JUMP, Type.STRONG),
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, Type.NORMAL),
    LONG_FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, Type.LONG),
    SPEED(PotionType.SPEED, Type.NORMAL),
    LONG_SWIFTNESS(PotionType.SPEED, Type.LONG),
    STRONG_SWIFTNESS(PotionType.SPEED, Type.STRONG),
    SLOWNESS(PotionType.SLOWNESS, Type.NORMAL),
    LONG_SLOWNESS(PotionType.SLOWNESS, Type.LONG),
    STRONG_SLOWNESS(PotionType.SLOWNESS, Type.STRONG),
    WATER_BREATHING(PotionType.WATER_BREATHING, Type.NORMAL),
    LONG_WATER_BREATHING(PotionType.WATER_BREATHING, Type.LONG),
    INSTANT_HEAL(PotionType.INSTANT_HEAL, Type.NORMAL),
    STRONG_HEALING(PotionType.INSTANT_HEAL, Type.STRONG),
    INSTANT_DAMAGE(PotionType.INSTANT_DAMAGE, Type.NORMAL),
    STRONG_HARMING(PotionType.INSTANT_DAMAGE, Type.STRONG),
    POISON(PotionType.POISON, Type.NORMAL),
    LONG_POISON(PotionType.POISON, Type.LONG),
    STRONG_POISON(PotionType.POISON, Type.STRONG),
    REGEN(PotionType.REGEN, Type.NORMAL),
    LONG_REGENERATION(PotionType.REGEN, Type.LONG),
    STRONG_REGENERATION(PotionType.REGEN, Type.STRONG),
    STRENGTH(PotionType.STRENGTH, Type.NORMAL),
    LONG_STRENGTH(PotionType.STRENGTH, Type.LONG),
    STRONG_STRENGTH(PotionType.STRENGTH, Type.STRONG),
    WEAKNESS(PotionType.WEAKNESS, Type.NORMAL),
    LONG_WEAKNESS(PotionType.WEAKNESS, Type.LONG),
    LUCK(PotionType.LUCK, Type.NORMAL),
    TURTLE_MASTER(PotionType.SLOWNESS, Type.NORMAL),
    LONG_TURTLE_MASTER(PotionType.SLOWNESS, Type.LONG),
    STRONG_TURTLE_MASTER(PotionType.SLOWNESS, Type.STRONG),
    SLOW_FALLING(PotionType.SLOW_FALLING, Type.NORMAL),
    LONG_SLOW_FALLING(PotionType.SLOW_FALLING, Type.LONG),
    ;

    private final PotionType potionType;
    private final Type type;
    private final float serverVersion = ItemCreator.getServerVersion();

    PotionData(@Nullable final PotionType potionType, @Nonnull final Type type) {
        this.potionType = potionType;
        this.type = type;
    }

    public PotionType getPotionType() {
        PotionType potion = getSpecialPotion();
        if (potion != null) {
            return potion;
        }
        if (this.serverVersion < 20.0) {
            return this.potionType;
        } else {
            return getPotionMapping();
        }
    }

    @Nonnull
    public Type getModifier() {
        return type;
    }

    private PotionType getSpecialPotion() {
        switch (this) {
            case UNCRAFTABLE:
                return PotionType.UNCRAFTABLE;
            case MUNDANE:
                return PotionType.MUNDANE;
            case THICK:
                return PotionType.THICK;
            case AWKWARD:
                return PotionType.AWKWARD;
            case WATER:
                return PotionType.WATER;
            default:
                return null;
        }
    }

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

    public enum Type {
        NORMAL,
        LONG,
        STRONG,
    }
}
