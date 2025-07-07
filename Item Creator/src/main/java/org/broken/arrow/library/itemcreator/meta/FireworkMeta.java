package org.broken.arrow.library.itemcreator.meta;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FireworkMeta {

    private FireworkEffect fireworkEffect;
    private ColorMeta colorMeta;

    /**
     * Get list of firework effects
     *
     * @return effects set on this item.
     */
    public FireworkEffect getFireworkEffect() {
        return fireworkEffect;
    }

    /**
     * Add firework effect meta on this item.
     *
     * @param fireworkEffect a consumer where you set your firework effect data.
     */
    public void setFireworkEffect(@Nonnull final Consumer<FireworkEffect.Builder> fireworkEffect) {
        FireworkEffect.Builder builderFirework = FireworkEffect.builder();
        fireworkEffect.accept(builderFirework);

        this.fireworkEffect = builderFirework.build();
    }

    public void setFireworkEffect(@Nonnull final FireworkEffect fireworkEffect) {
        this.fireworkEffect = fireworkEffect;
    }

    /**
     * This method just allow you set some basic colors only in the firework.
     * Instead of using the builder method options.
     *
     * @param metaConsumer the color you want to set on your item.
     */
    public void setFireworkColor(@Nonnull final Consumer<ColorMeta> metaConsumer) {
        ColorMeta colorData = new ColorMeta();
        metaConsumer.accept(colorData);
        this.colorMeta = colorData;
    }

    public void applyFireworkEffect(@Nonnull final ItemMeta itemMeta) {

        if (itemMeta instanceof FireworkEffectMeta) {

            final FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;
            final FireworkEffect effect = this.getFireworkEffect();

            if (effect != null) {
                fireworkEffectMeta.setEffect(effect);
            } else {
                final FireworkEffect.Builder builder = FireworkEffect.builder();
                final ColorMeta colorEffect = this.colorMeta;

                if (colorEffect.isColorSet())
                    builder.withColor(Color.fromBGR(colorEffect.getBlue(), colorEffect.getGreen(), colorEffect.getRed()));
                fireworkEffectMeta.setEffect(builder.build());
            }
        }
    }
}
