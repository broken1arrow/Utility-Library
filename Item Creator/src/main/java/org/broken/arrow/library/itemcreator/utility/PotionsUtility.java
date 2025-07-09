package org.broken.arrow.library.itemcreator.utility;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;

/**
 * This class handle the potions. Due to 1.20.5+ does not have Potion and
 * PotionData, this class will handle the errors if it could not set the
 * potion type.
 */
public class PotionsUtility {
    private final Logging logger = new Logging(PotionsUtility.class);
    private final float serverVersion = ItemCreator.getServerVersion();
    private final PotionMeta potionMeta;

    public PotionsUtility(@Nonnull PotionMeta potionMeta) {
        this.potionMeta = potionMeta;
    }

    /**
     * Set the portions to the itemStacks metadata.
     *
     * @param potion   the portion type you want to set for the item.
     * @param extended whether the potion is extended {@link PotionType#isExtendable()} must be true
     * @param upgraded whether the potion is upgraded {@link PotionType#isUpgradeable()} must be true
     */
    public void setPotion(@Nonnull final PotionType potion, final boolean extended, final boolean upgraded) {
        if (serverVersion >= 20.0) {
            potionMeta.setBasePotionType(potion);
            return;
        }

        try {
            boolean couldUpgrade = upgraded;
            boolean couldExtended = extended;
            if (couldUpgrade && !potion.isUpgradeable()) couldUpgrade = false;
            if (couldExtended && !potion.isExtendable()) couldExtended = false;
            final PotionData potionData = new PotionData(potion, couldExtended, couldUpgrade);
            potionMeta.setBasePotionData(potionData);
        } catch (NoClassDefFoundError ex) {
            logger.logError(ex, () -> "Could not find PotionData class as fallback when your Minecraft version missing the setBasePotionType method.");
        }

    }
}
