package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.potion.PotionTypeWrapper;
import org.broken.arrow.library.itemcreator.meta.potion.PotionModifier;
import org.broken.arrow.library.itemcreator.meta.potion.PotionsUtility;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents metadata for a potion bottle item, managing its potion effects,
 * color, base potion data, and bottle type properties.
 * <p>
 * This class allows you to get and set potion effects, configure if the bottle
 * is a water bottle, override existing effects, and manage potion attributes
 * such as whether the potion is extended or upgraded.
 * </p>
 */
public class BottleEffectMeta {
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private boolean waterBottle;
    private boolean override = true;
    private ColorMeta colorMeta;
    private PotionTypeWrapper potionTypeWrapper;
    private boolean extended;
    private boolean upgraded;


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
     * Gets the predefined {@link PotionType} to apply to the potion, if any.
     * <p>
     * If a type is set, it overrides any custom potion effects and color.
     *
     * @return the potion type, or {@code null} if using custom effects instead
     */
    @Nullable
    public PotionType getPotionType() {
        return (this.potionTypeWrapper == null ? null : this.potionTypeWrapper.getPotionType());
    }

    /**
     * Returns the predefined {@link PotionTypeWrapper} associated with this potion, if available.
     *
     * <p>This wrapper can provide additional metadata or configuration not exposed by
     * the standard {@link PotionType} from the Bukkit/Spigot API, useful when legacy
     * values, extended information, or custom behavior is required.</p>
     *
     * @return the associated {@code PotionTypeWrapper}, or {@code null} if none is set.
     */
    @Nullable
    public PotionTypeWrapper getPotionTypeWrapper () {
        return  this.potionTypeWrapper;
    }

    /**
     * Gets the predefined {@link PotionType} to apply to the potion, if any.
     * <p>
     * If a type is set, it overrides any custom potion effects and color.
     *
     * @return the safe potion type name, or {@code null} if using custom effects instead
     */
    @Nullable
    public String getPotionName() {
        return (this.potionTypeWrapper == null ? null : this.potionTypeWrapper.name());
    }

    /**
     * Sets the predefined {@link PotionTypeWrapper} to apply to this potion item.
     * <p>
     * This automatically handles version compatibility. You can specify the desired
     * potion variant (e.g., long, strong, or base version), and it will use the correct
     * {@link org.bukkit.potion.PotionType} for the server version.
     * </p>
     * <p>
     * Alternatively, you may use {@link #setUpgraded(boolean)} or {@link #setExtended(boolean)}
     * if you're working with the older base potion types. However, this approach is not supported
     * on Minecraft 1.20 and newer, as the underlying API no longer includes the older potion classes,
     * and handling has changed.
     * </p>
     * <p>
     * If a potion type is set, it will override any custom effects or colors you
     * might otherwise apply with {@link #addPotionEffects(PotionEffect...)},{@link #addPotionEffect(Consumer)}
     * or {@link #setPotionEffects(List)} and the color {@link #setBottleColor(Consumer)}.
     * </p>
     * <p>
     * If you instead want a custom-colored potion with custom effects, do not set
     * a potion type (or set it to {@code null}) and use the appropriate setters instead.
     * </p>
     *
     * @param potionTypeWrapper the potion type to apply; set {@code null} to allow custom effects
     * @return this instance for method chaining.
     */
    public BottleEffectMeta setPotionData(@Nullable final PotionTypeWrapper potionTypeWrapper) {
        this.potionTypeWrapper = potionTypeWrapper;
        return this;
    }

    /**
     * Checks if the potion is in an upgraded state.
     * This refers to Tier 2 potions (e.g., Potion of Strength II).
     *
     * @return {@code true} if the potion is upgraded.
     */
    public boolean isUpgraded() {
        return upgraded;
    }

    /**
     * Sets the potion to its upgraded (Tier 2) state. If the potion type
     * does not support upgrading, this value will be ignored automatically.
     *
     * <p>
     * <strong>Note:</strong> A potion cannot be both upgraded and extended
     * at the same time. If both are set to {@code true}, an exception will be thrown.
     * </p>
     *
     * @param upgraded {@code true} if the potion shall be upgraded (Tier 2), {@code false} otherwise.
     */
    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }

    /**
     * Checks if the potion is in an extended state.
     * This refers to potions with longer duration (e.g., Potion of Swiftness (8:00)).
     *
     * @return {@code true} if the potion is extended.
     */
    public boolean isExtended() {
        return extended;
    }

    /**
     * Sets the potion to an extended duration state. If the potion type does not
     * support extension, this value will be ignored automatically.
     *
     * <p>
     * <strong>Note:</strong> A potion cannot be both upgraded and extended
     * at the same time. If both are set to {@code true}, an exception will be thrown.
     * </p>
     *
     * @param extended {@code true} to apply extended duration, {@code false} otherwise.
     */
    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    /**
     * Retrieve the portion color.
     *
     * @return the color set.
     */
    @Nullable
    public ColorMeta getColorMeta() {
        return colorMeta;
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
                potionsUtility.setPotion(PotionType.WATER, null);
                return;
            }

            if (this.potionTypeWrapper != null && this.setPotionType(potionMeta)) {
                return;
            }

            final List<PotionEffect> effects = getPotionEffects();

            if (effects != null && !effects.isEmpty()) {
                final ColorMeta colorEffect = this.colorMeta;
                if (ItemCreator.getServerVersion() > 10.2F && colorEffect != null && colorEffect.isColorSet()) {
                    potionMeta.setColor(colorEffect.getColor());
                }
                effects.forEach((portionEffect) -> potionMeta.addCustomEffect(portionEffect, this.override));
            }
        }
    }

    private boolean setPotionType(@Nonnull final PotionMeta potionMeta) {
        final PotionType potionType = this.getPotionType();
        if (potionType != null) {
            PotionsUtility potionsUtility = new PotionsUtility(potionMeta);
            PotionModifier modifier = this.potionTypeWrapper.getModifier();
            potionsUtility.setPotion(potionType, modifier);
            return true;
        }
        return false;
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
         * @return Returns this class for chaining.
         */
        public PotionEffectWrapper add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient, final boolean particles, final boolean icon) {
            if (ItemCreator.getServerVersion() < 13.0F)
                portionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles));
            else
                portionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
            return this;
        }

        /**
         * Adds a potion effect without the icon option.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @param ambient   the ambient status, see {@link PotionEffect#isAmbient()}
         * @param particles whether the effect should display particles.
         * @return Returns this class for chaining.
         */
        public PotionEffectWrapper add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient, final boolean particles) {
            portionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles));
            return this;
        }

        /**
         * Adds a potion effect with customization options excluding particles and icon.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @param ambient   the ambient status, see {@link PotionEffect#isAmbient()}
         * @return Returns this class for chaining.
         */
        public PotionEffectWrapper add(@Nonnull PotionEffectType type, final int duration, final int amplifier, final boolean ambient) {
            portionEffects.add(new PotionEffect(type, duration, amplifier, ambient));
            return this;
        }

        /**
         * Adds a potion effect with only the required parameters.
         *
         * @param type      the {@link PotionEffectType} for the effect; must not be null.
         * @param duration  measured in ticks, see {@link PotionEffect#getDuration()}
         * @param amplifier the amplifier, see {@link PotionEffect#getAmplifier()}
         * @return Returns this class for chaining.
         */
        public PotionEffectWrapper add(@Nonnull PotionEffectType type, final int duration, final int amplifier) {
            portionEffects.add(new PotionEffect(type, duration, amplifier));
            return this;
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
