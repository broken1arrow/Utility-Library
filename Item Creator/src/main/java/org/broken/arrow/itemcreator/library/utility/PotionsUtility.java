package org.broken.arrow.itemcreator.library.utility;

import org.broken.arrow.logging.library.Logging;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;

public class PotionsUtility {
    private  static Logging logger = new Logging(PotionsUtility.class);
    PotionMeta potionMeta;

    public PotionsUtility(@Nonnull PotionMeta potionMeta) {
        this.potionMeta = potionMeta;
    }

    public void setPotion(PotionType potion) {
        try {
            potionMeta.setBasePotionType(potion);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            try {
                final PotionData potionData = new PotionData(potion);
                potionMeta.setBasePotionData(potionData);
            } catch (NoClassDefFoundError ex) {
                logger.logError(ex,() -> Logging.of("Could not find PotionData class and your Minecraft version missing the setBasePotionType method."));

            }
        }
    }
}
