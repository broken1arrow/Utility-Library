package org.broken.arrow.itemcreator.library.utility;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;

public class PotionsUtility {

    PotionMeta potionMeta;

    public PotionsUtility(@Nonnull PotionMeta potionMeta) {
        this.potionMeta = potionMeta;
    }

    public void setPotion(PotionType potion) {
        try {
            potionMeta.setBasePotionType(potion);
        } catch (Exception e) {
            final PotionData potionData = new PotionData(potion);
            potionMeta.setBasePotionData(potionData);
        }
    }
}
