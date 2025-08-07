package org.broken.arrow.library.itemcreator.meta;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FireworkMeta {

    private List<FireworkEffect> fireworkEffects;
    private ColorMeta colorMeta;
    private int power;

    /**
     * Get list of firework effects
     *
     * @return effects set on this item.
     */
    @Nonnull
    public List<FireworkEffect> getFireworkEffects() {
        if (this.fireworkEffects == null)
            return new ArrayList<>();
        return fireworkEffects;
    }

    /**
     * Sets one or more firework effects for this item using a builder-style consumer.
     * <p>
     * <strong>Note:</strong> Certain items, such as {@link Material#FIREWORK_STAR},
     * only support a single firework effect. In such cases, only the first effect
     * will be used; additional effects will be ignored.
     * </p>
     *
     * @param fireworkEffectConsumer a consumer to define one or more firework effects
     *                               using a {@link BuildFireworkEffect} builder.
     */
    public void setFireworkEffects(@Nonnull final Consumer<BuildFireworkEffect> fireworkEffectConsumer) {
        BuildFireworkEffect fireworkEffect = new BuildFireworkEffect();
        fireworkEffectConsumer.accept(fireworkEffect);
        this.fireworkEffects.clear();
        this.fireworkEffects.addAll(fireworkEffect.getFireworkEffects());
    }

    /**
     * Adds a single firework effect to this item.
     * <p>
     * <strong>Note:</strong> Certain items, such as {@link Material#FIREWORK_STAR},
     * only support one firework effect. If multiple effects are added, only the first
     * will be applied.
     * </p>
     *
     * @param fireworkEffect the firework effect to add.
     */
    public void addFireworkEffect(@Nonnull final FireworkEffect fireworkEffect) {
        if (this.fireworkEffects == null)
            this.fireworkEffects = new ArrayList<>();
        this.fireworkEffects.add(fireworkEffect);
    }

    /**
     * Replaces all firework effects on this item with the provided list.
     * <p>
     * <strong>Note:</strong> Certain items, such as {@link Material#FIREWORK_STAR},
     * support only a single effect. In such cases, only the first effect in the list
     * will be used.
     * </p>
     *
     * @param fireworkEffect the list of firework effects to set.
     */
    public void setFireworkEffects(@Nonnull final List<FireworkEffect> fireworkEffect) {
        this.fireworkEffects = fireworkEffect;
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

    /**
     * Gets the approximate height the firework will fly.
     *
     * @return approximate flight height of the firework.
     */
    public int getPower() {
        return power;
    }

    /**
     * Sets the approximate power of the firework. Each level of power is half
     * a second of flight time. The typical range is {@literal minimum height > 0 and maximum height < 127}.
     * Refer to the Spigot or PaperMC documentation for the most up-to-date limits and behavior.
     *
     * @param power the power of the firework, from 0–127.
     */
    public void setPower(final int power) {
        this.power = power;
    }

    public void applyFireworkEffect(@Nonnull final ItemMeta itemMeta) {
        final List<FireworkEffect> effects = this.getFireworkEffects();
        if (itemMeta instanceof FireworkEffectMeta) {
            final FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;
            if (!effects.isEmpty()) {
                fireworkEffectMeta.setEffect(effects.stream().findFirst().orElse(null));
            } else {
                final FireworkEffect.Builder builder = FireworkEffect.builder();
                final ColorMeta colorEffect = this.colorMeta;

                if (colorEffect.isColorSet())
                    builder.withColor(Color.fromBGR(colorEffect.getBlue(), colorEffect.getGreen(), colorEffect.getRed()));
                fireworkEffectMeta.setEffect(builder.build());
            }
        }
        if (itemMeta instanceof org.bukkit.inventory.meta.FireworkMeta && !effects.isEmpty()) {
            final org.bukkit.inventory.meta.FireworkMeta fireworkEffectMeta = (org.bukkit.inventory.meta.FireworkMeta) itemMeta;
            fireworkEffectMeta.addEffects(effects);
            if (this.power > 0)
                fireworkEffectMeta.setPower(this.power);
        }
    }


    public static class BuildFireworkEffect {
        private final List<FireworkEffect> fireworkEffects = new ArrayList<>();

        public BuildFireworkEffect add(Consumer<FireworkEffect.Builder> fireworkEffects) {
            FireworkEffect.Builder builderFirework = FireworkEffect.builder();
            fireworkEffects.accept(builderFirework);
            this.fireworkEffects.add(builderFirework.build());
            return this;
        }

        public List<FireworkEffect> getFireworkEffects() {
            return fireworkEffects;
        }
    }
}
