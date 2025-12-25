package org.broken.arrow.library.itemcreator.meta.potion;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class handle the potions. Due to 1.20.5+ does not have Potion and PotionData, this class will handle the errors
 * if it could not set the potion type.
 */
public class PotionsUtility {
    private final Logging logger = new Logging(PotionsUtility.class);
    private final double serverVersion = ItemCreator.getServerVersion();
    private final PotionMeta potionMeta;

    /**
     * Create PotionsUtility instance.
     *
     * @param potionMeta the bukkit PotionMeta to set the potion effects too.
     */
    public PotionsUtility(@Nonnull PotionMeta potionMeta) {
        this.potionMeta = potionMeta;
    }

    /**
     * Sets the potion type on the item's metadata using default type.
     * <p>
     * This is a convenience method that avoids specifying a {@link PotionModifier}. Safety checks from
     * {@link #setPotion(PotionType, PotionModifier)} still apply.
     * </p>
     * <p>
     * <strong>
     * Compatible with server versions from 1.8.8 and up.
     * </strong>
     * </p>
     * <ul>
     *   <li>On versions before 1.20.2, this method uses {@link PotionData} and applies the base potion type.</li>
     *   <li>On versions 1.20.2 and later, it uses {@link PotionType}, and potion modifiers are handled automatically.</li>
     * </ul>
     *
     * @param potion the potion type to set on the item
     */
    public void setPotion(@Nonnull final PotionType potion) {
        this.setPotion(potion, null);
    }

    /**
     * Sets the potion type on the item's metadata with a specific potion modifier.
     * <p>
     * This method includes safety checks to prevent both {@code extended} and {@code upgraded} from being applied at
     * the same time, as that is not supported.
     * </p>
     * <p>
     * <strong>
     * Compatible with server versions from 1.8.8 and up.
     * </strong>
     * </p>
     *
     * <ul>
     * <li> On versions before 1.20.2, this method uses {@link PotionData}. If the potion type does not support
     * {@link PotionModifier#LONG LONG} or
     * {@link PotionModifier#STRONG STRONG}, it will safely fall back to the base potion. </li>
     * <li> On versions 1.20.2 and later, it uses {@link PotionType}, and the {@code type} parameter is ignored.</li>
     * </ul>
     *
     * @param potion         the potion type to set on the item
     * @param potionModifier the potion modifier to apply ({@link PotionModifier#NORMAL NORMAL},
     *                       {@link PotionModifier#LONG LONG}, or {@link PotionModifier#STRONG STRONG}). If
     *                       {@code null}, defaults to {@link PotionModifier#NORMAL}.
     */
    public void setPotion(@Nonnull final PotionType potion, @Nullable final PotionModifier potionModifier) {
        if (serverVersion > 20.1) {
            potionMeta.setBasePotionType(potion);
            return;
        }

        if (serverVersion < 9.0) {
            return;
        }

        try {
            boolean couldUpgrade = potionModifier == PotionModifier.STRONG;
            boolean couldExtended = potionModifier == PotionModifier.LONG;
            if (couldUpgrade && !potion.isUpgradeable()) couldUpgrade = false;
            if (couldExtended && !potion.isExtendable()) couldExtended = false;
            final PotionData potionData = new PotionData(potion, couldExtended, couldUpgrade);
            potionMeta.setBasePotionData(potionData);
        } catch (NoClassDefFoundError ex) {
            logger.logError(ex, () -> "PotionData class not found — this fallback prevents your plugin from breaking on server versions before 1.20.2. " + "This is likely due to using a non-standard implementation API built on Spigot or PaperMC. " + "The potion will remain unchanged.");
        }
    }

}
