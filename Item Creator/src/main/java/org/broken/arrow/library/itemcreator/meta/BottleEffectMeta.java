package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.utility.PotionsUtility;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BottleEffectMeta {
    private static final Logging logger = new Logging(ColorMeta.class);

    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private boolean waterBottle;
    private boolean override = true;
    private ColorMeta colorMeta;


    /**
     * Returns all potion effects applied to this item.
     *
     * @return list of potion effects.
     */
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    /**
     * Replaces any existing potion effects with the given list.
     *
     * @param potionEffects the list of effects to set.
     * @return this instance for chaining.
     */
    public BottleEffectMeta setPotionEffects(final List<PotionEffect> potionEffects) {
        if (potionEffects == null || potionEffects.isEmpty()) {
            //logger.log(() -> "This list of portion effects is empty so no values will be added");
            return this;
        }
        this.potionEffects.clear();
        this.potionEffects.addAll(potionEffects);
        return this;
    }

    /**
     * Adds one or more potion effects using a builder-style consumer.
     *
     * @param potionEffects consumer that builds the potion effects to add.
     * @return this instance for chaining.
     */
    public BottleEffectMeta addPotionEffect(final Consumer<PotionEffectWrapper> potionEffects) {
        if (potionEffects == null) return this;
        PotionEffectWrapper potionEffectWrapper = new PotionEffectWrapper();
        potionEffects.accept(potionEffectWrapper);

        this.potionEffects.addAll(potionEffectWrapper.getPotionEffects());
        return this;
    }

    /**
     * Adds one or more potion effects to the current list.
     *
     * @param potionEffects the effects to add.
     * @return this instance for chaining.
     */
    public BottleEffectMeta addPotionEffects(final PotionEffect... potionEffects) {
        if (potionEffects == null || potionEffects.length == 0) return this;
        this.potionEffects.addAll(Arrays.asList(potionEffects));
        return this;
    }

    /**
     * Sets the color of the potion bottle.
     *
     * @param metaConsumer consumer that configures the color metadata.
     */
    public void setBottleColor(@Nonnull final Consumer<ColorMeta> metaConsumer) {
        ColorMeta colorData = new ColorMeta();
        metaConsumer.accept(colorData);
        this.colorMeta = colorData;
    }

    /**
     * Sets whether custom effects should override existing ones.
     *
     * @param override {@code true} to override existing effects, {@code false} to keep them.
     */
    public void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * Checks if this item represents a water bottle.
     *
     * @return {@code true} if it is a water bottle; {@code false} otherwise.
     */
    public boolean isWaterBottle() {
        return waterBottle;
    }

    /**
     * Sets whether this item is a water bottle. This is not the same as a potion.
     *
     * @param waterBottle {@code true} to mark this as a water bottle; {@code false} for a regular potion.
     * @return this instance for chaining.
     */
    public BottleEffectMeta setWaterBottle(final boolean waterBottle) {
        this.waterBottle = waterBottle;
        return this;
    }

    /**
     * Applies all potion and visual effects to the given {@link ItemMeta},
     * if it is an instance of {@link PotionMeta}.
     *
     * @param itemMeta the item metadata to apply the effects to.
     */
    public void applyBottleEffects(@Nonnull final ItemMeta itemMeta) {
        if (itemMeta instanceof PotionMeta) {
            final PotionMeta potionMeta = (PotionMeta) itemMeta;

            if (isWaterBottle()) {
                PotionsUtility potionsUtility = new PotionsUtility(potionMeta);
                potionsUtility.setPotion(PotionType.WATER);
                return;
            }
            final List<PotionEffect> effects = getPotionEffects();

            if (effects != null && !effects.isEmpty()) {
                final ColorMeta colorEffect = this.colorMeta;
                if (colorEffect != null && colorEffect.isColorSet()) {
                    potionMeta.setColor(Color.fromBGR(colorEffect.getBlue(), colorEffect.getGreen(), colorEffect.getRed()));
                }
                effects.forEach((portionEffect) -> potionMeta.addCustomEffect(portionEffect, this.override));
            }
        }
    }

    /**
     * A helper class for building a list of {@link PotionEffect} instances.
     * <p>
     * This builder-style wrapper allows for the convenient addition of potion effects using
     * various overloaded {@code add} methods that accept differing sets of parameters. Once all
     * desired potion effects have been added, they can be retrieved via {@link #getPotionEffects()}.
     * </p>
     *
     * <p><strong>Usage example:</strong></p>
     * <pre>
     * PotionEffectWrapper wrapper = new PotionEffectWrapper();
     * wrapper.add(PotionEffectType.SLOW, 120, 1);
     * wrapper.add(PotionEffectType.BLINDNESS, 80, 2, true, false);
     * List&lt;PotionEffect&gt; effects = wrapper.getPotionEffects();
     * </pre>
     *
     * @see PotionEffect
     * @see PotionEffectType
     */
    public static class PotionEffectWrapper {
        private final List<PotionEffect> portionEffects = new ArrayList<>();

        /**
         * Adds a potion effect with full customization.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @param ambient   the ambient status, see {@link PotionEffect#isAmbient()}
         * @param particles whether the effect should display particles.
         * @param icon      whether the effect icon is shown.
         */
        public void add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient, final boolean particles, final boolean icon) {
            portionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
        }

        /**
         * Adds a potion effect without the icon option.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @param ambient   the ambient status, see {@link PotionEffect#isAmbient()}
         * @param particles whether the effect should display particles.
         */
        public void add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient, final boolean particles) {
            portionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles));
        }

        /**
         * Adds a potion effect with customization options excluding particles and icon.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @param ambient   the ambient status, see {@link PotionEffect#isAmbient()}
         */
        public void add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient) {
            portionEffects.add(new PotionEffect(type, duration, amplifier, ambient));
        }

        /**
         * Adds a potion effect with only the required parameters.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         */
        public void add(@Nonnull PotionEffectType type, final int duration, final int amplifier) {
            portionEffects.add(new PotionEffect(type, duration, amplifier));
        }

        /**
         * Retrieves the list of potion effects that have been added.
         *
         * @return an unmodifiable list of {@link PotionEffect} instances built by this wrapper.
         */
        public List<PotionEffect> getPotionEffects() {
            return portionEffects;
        }
    }

}
