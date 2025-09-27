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

/**
 * Represents metadata for a firework item, including its visual effects,
 * colors, and flight power.
 * <p>
 * This class allows you to define multiple firework effects, customize
 * colors via a simple color metadata wrapper, and set the flight duration
 * (power) of the firework.
 * </p>
 * <p>
 * Some items, such as {@link Material#FIREWORK_STAR}, support only a single
 * firework effect. In such cases, if multiple effects are set, only the first
 * effect will be applied.
 * </p>
 * <p>
 * Firework effects can be set either by adding individual effects, setting a
 * list of effects, or using a builder-style consumer for more complex effect
 * construction.
 * </p>
 * <p>
 * The power value controls the approximate flight height of the firework,
 * with each increment representing about half a second of flight time.
 * </p>
 */
public class FireworkMeta {

    private List<FireworkEffect> fireworkEffects;
    private ColorMeta colorMeta;
    private int power;

    /**
     * Gets the list of firework effects applied to this item.
     * Returns an empty list if no effects are set.
     *
     * @return a non-null list of {@link FireworkEffect}s
     */
    @Nonnull
    public List<FireworkEffect> getFireworkEffects() {
        if (this.fireworkEffects == null)
            return new ArrayList<>();
        return fireworkEffects;
    }

    /**
     * Sets one or more firework effects on this item using a builder-style consumer.
     * <p>
     * <strong>Note:</strong> Certain items, such as {@link Material#FIREWORK_STAR},
     * only support a single firework effect. In such cases, only the first effect
     * will be used; additional effects will be ignored.
     * </p>
     *
     * @param fireworkEffectConsumer a consumer that defines one or more firework effects
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
     * only support a single firework effect. If multiple effects are added, only the first
     * will be applied.
     * </p>
     *
     * @param fireworkEffect the firework effect to add (must not be null).
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
     * @param fireworkEffect the list of firework effects to set (must not be null).
     */
    public void setFireworkEffects(@Nonnull final List<FireworkEffect> fireworkEffect) {
        this.fireworkEffects = fireworkEffect;
    }

    /**
     * Sets basic color metadata for the firework.
     * This allows setting colors without using the builder pattern.
     *
     * @param metaConsumer a consumer to configure {@link ColorMeta} color data (must not be null).
     */
    public void setFireworkColor(@Nonnull final Consumer<ColorMeta> metaConsumer) {
        ColorMeta colorData = new ColorMeta();
        metaConsumer.accept(colorData);
        this.colorMeta = colorData;
    }

    /**
     * Gets the approximate power (flight duration) of the firework.
     *
     * @return the flight power of the firework
     */
    public int getPower() {
        return power;
    }

    /**
     * Sets the approximate power of the firework.
     * Each power level corresponds to roughly half a second of flight time.
     * Typical values range from greater than 0 to less than 127.
     * See Spigot or PaperMC documentation for precise limits.
     *
     * @param power the power level of the firework, between 0 and 127 inclusive
     */
    public void setPower(final int power) {
        this.power = power;
    }

    /**
     * Applies this {@code FireworkMeta} data to the given {@link ItemMeta}.
     * Supports both custom {@link FireworkEffectMeta} and Bukkit's native
     * {@link org.bukkit.inventory.meta.FireworkMeta} implementations.
     *
     * @param itemMeta the item meta to apply effects and power to (must not be null)
     */
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
                    builder.withColor(colorEffect.getColor());
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
    /**
     * Builder class to help create multiple {@link FireworkEffect}s using
     * a fluent consumer-based API.
     */
    public static class BuildFireworkEffect {
        private final List<FireworkEffect> fireworkEffects = new ArrayList<>();

        /**
         * Adds a firework effect by applying the given consumer to a new
         * {@link FireworkEffect.Builder}.
         *
         * @param fireworkEffects a consumer to configure the firework effect builder
         * @return this builder instance for chaining
         */
        public BuildFireworkEffect add(Consumer<FireworkEffect.Builder> fireworkEffects) {
            FireworkEffect.Builder builderFirework = FireworkEffect.builder();
            fireworkEffects.accept(builderFirework);
            this.fireworkEffects.add(builderFirework.build());
            return this;
        }

        /**
         * Gets the list of built firework effects.
         *
         * @return a list of firework effects
         */
        public List<FireworkEffect> getFireworkEffects() {
            return fireworkEffects;
        }
    }
}
