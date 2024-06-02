package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PotionsData {

    private Object potion;
    private final int damage;

    public PotionsData(@Nonnull Object potion) {
        this(potion, -1);

    }

    public PotionsData(@Nullable Object potion, int damage) {
        this.potion = potion;
        this.damage = damage;
    }

    public Potion getPotion() throws NoClassDefFoundError {
        if (potion instanceof Potion)
            return (Potion) potion;
        else if (damage > 0)
            return Potion.fromDamage(damage);
        return null;
    }

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
