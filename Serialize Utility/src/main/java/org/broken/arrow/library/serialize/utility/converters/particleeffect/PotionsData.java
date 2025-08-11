package org.broken.arrow.library.serialize.utility.converters.particleeffect;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A wrapper class to handle legacy and modern potion data representations in Bukkit/Spigot APIs.
 * <p>
 * This class encapsulates the legacy {@code org.bukkit.potion.Potion} class, used in
 * earlier Minecraft versions, alongside additional potion-related data such as damage values.
 * It also supports compatibility with newer potion representations like {@code PotionMeta}
 * and {@code PotionEffect}, facilitating smooth transitions between different API versions.
 * </p>
 * <p>
 * Internally, the potion is stored as a generic {@code Object} to avoid
 * compile-time dependency issues when the legacy {@code Potion} class is unavailable.
 * Methods handle potential {@link NoClassDefFoundError} exceptions gracefully.
 * </p>
 */
public class PotionsData {

    private static final boolean OLD_POTION_AVAILABLE;

    static {
        boolean available;
        try {
            Class.forName("org.bukkit.potion.Potion");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
        OLD_POTION_AVAILABLE = available;
    }

    private final Object potion;
    private final int damage;

    /**
     * Constructs a PotionsData wrapper with a potion object and a default damage value of -1.
     *
     * @param potion the potion object, typically an instance of legacy {@code Potion}, {@code PotionEffect}, or related types.
     */
    public PotionsData(@Nonnull Object potion) {
        this(potion, -1);

    }

    /**
     * Constructs a PotionsData wrapper with a potion object and specified damage value.
     *
     * @param potion the potion object, typically an instance of legacy {@code Potion}, {@code PotionEffect}, or related types.
     * @param damage the damage value used by legacy {@code Potion} instances to represent potion variants; -1 if unused.
     */
    public PotionsData(@Nullable Object potion, int damage) {
        this.potion = potion;
        this.damage = damage;
    }

    /**
     * Checks whether the legacy {@code Potion} class is available in the current runtime.
     *
     * @return {@code true} if legacy {@code Potion} class is present, {@code false} otherwise.
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * if (PotionsData.isOldPotionAvailable()) {
     *     // Safe to use legacy Potion API
     * }
     * }</pre>
     */
    public static boolean isOldPotionAvailable() {
        return OLD_POTION_AVAILABLE;
    }

    /**
     * Retrieves the wrapped legacy {@code Potion} object if available.
     * <p>
     * This returns {@code null} if the legacy {@code Potion} class is unavailable
     * or if the wrapped object is not a {@code Potion} instance and damage is not set.
     * </p>
     *
     * @return the {@code Potion} instance, or {@code null} if unavailable
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * Potion legacyPotion = potionsData.getPotion();
     * if (legacyPotion != null) {
     *     // Use legacyPotion as needed
     * }
     * }</pre>
     */
    @Nullable
    public Potion getPotion() {
        if (!OLD_POTION_AVAILABLE) {
            return null;
        }
        if (potion instanceof Potion)
            return (Potion) potion;
        else if (damage > 0)
            return Potion.fromDamage(damage);
        return null;
    }

    /**
     * Retrieves the wrapped {@code PotionEffect} if the internal object is of that type.
     *
     * @return the {@code PotionEffect} instance, or {@code null} if the wrapped object is not a {@code PotionEffect}
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * PotionEffect effect = potionsData.getPotionEffect();
     * if (effect != null) {
     *     // Use effect for modern potion handling
     * }
     * }</pre>
     */
    @Nullable
    public PotionEffect getPotionEffect() {
        if (potion instanceof PotionEffect)
            return (PotionEffect) potion;
        return null;
    }

    /**
     * Checks if the wrapped object represents any form of potion data.
     * <p>
     * This includes legacy {@code Potion}, {@code PotionMeta}, or {@code PotionEffect} types.
     * </p>
     *
     * @return {@code true} if the wrapped object is a potion or potion-related type, {@code false} otherwise.
     */
    public boolean isPotion() {
        try {
            return potion instanceof Potion;
        } catch (NoClassDefFoundError e) {
            if (potion instanceof PotionMeta)
                return true;
            if (potion instanceof PotionEffect)
                return true;
        }
        return false;
    }

    /**
     * Checks if the given {@code dataType} class is compatible with the wrapped potion data.
     * <p>
     * This helps determine if the wrapped data is an instance of or related to the specified class,
     * including handling legacy {@code Potion} availability gracefully.
     * </p>
     *
     * @param dataType the class to check against
     * @return {@code true} if compatible, {@code false} otherwise
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * if (potionsData.checkDataType(PotionEffect.class)) {
     *     // The internal data can be treated as a PotionEffect
     * }
     * }</pre>
     */
    public boolean checkDataType(Class<?> dataType) {
        try {
            dataType.isInstance(Potion.class);
        } catch (NoClassDefFoundError e) {
            if (potion instanceof PotionMeta)
                return true;
            if (potion instanceof PotionEffect)
                return true;
        }
        return false;
    }
}
