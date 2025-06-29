package org.broken.arrow.itemcreator.library.utility;

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
    PotionMeta potionMeta;

    public PotionsUtility(@Nonnull PotionMeta potionMeta) {
        this.potionMeta = potionMeta;
    }

    /**
     * Set the portions to the itemstacks metadata.
     *
     * @param potion the portion type you want to set for the item.
     */
    public void setPotion(PotionType potion) {
        try {
            potionMeta.setBasePotionType(potion);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            try {
                final PotionData potionData = new PotionData(potion);
                potionMeta.setBasePotionData(potionData);
            } catch (NoClassDefFoundError ex) {
                logger.logError(ex,() -> "Could not find PotionData class and your Minecraft version missing the setBasePotionType method.");

            }
        }
    }
}
