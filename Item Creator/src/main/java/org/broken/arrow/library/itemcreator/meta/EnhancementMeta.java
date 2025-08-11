package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.CreateItemStack;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages enhancements (enchantments) for an item, including
 * their levels, restrictions, and visibility.
 * <p>
 * You can add enchantments using the provided methods, control
 * whether enchantments are displayed, and apply all enhancements
 * to a Bukkit {@link org.bukkit.inventory.meta.ItemMeta}.
 * </p>
 */
public class EnhancementMeta {
    private static final Logging logger = new Logging(ColorMeta.class);
    private final Map<Enchantment, EnhancementWrapper> enchantments = new HashMap<>();
    private boolean showEnchantments = true;

    /**
     * Sets or updates an enchantment on this item with a consumer to
     * configure the enchantment wrapper.
     *
     * @param enchantment     the enchantment type to set (non-null)
     * @param wrapperConsumer consumer to configure the {@link EnhancementWrapper}
     * @return this instance for chaining
     */
    public EnhancementMeta setEnchantment(@Nonnull final Enchantment enchantment, @Nonnull final Consumer<EnhancementWrapper> wrapperConsumer) {
        final EnhancementWrapper enhancementWrapper = new EnhancementWrapper(enchantment, 1);
        wrapperConsumer.accept(enhancementWrapper);

        this.enchantments.put(enchantment, enhancementWrapper);
        return this;
    }

    /**
     * Set enchantments on an itemStack. Set {@link #setShowEnchantments(boolean)} to false
     * if you want to hide all enchants (default so will it not hide enchants).
     *
     * @param enchantment The enchantment type you want to set.
     * @return this class.
     */
    public EnhancementWrapper setEnchantment(@Nonnull final Enchantment enchantment) {
        final EnhancementWrapper enhancementWrapper = new EnhancementWrapper(enchantment, 1);
        this.enchantments.put(enchantment, enhancementWrapper);
        return enhancementWrapper;
    }

    /**
     * Add enchantments on an itemStack. Set {@link #setShowEnchantments(boolean)} to false
     * if you want to hide all enchants (default so will it not hide enchants).
     *
     * @param enchantments list of enchantments you want to add.
     * @return this class.
     */
    public EnhancementMeta addEnchantments(final EnhancementWrapper... enchantments) {
        for (final EnhancementWrapper enchant : enchantments) {
            setEnchantment(enchant);
        }
        return this;
    }

    /**
     * Add enchantments on an itemStack. Set {@link #setShowEnchantments(boolean)} to true
     * if you want to hide all enchants (default so will it not hide enchants).
     * <p>
     * This method uses varargs and add it to list, like this enchantment;level;levelRestriction or
     * enchantment;level and it will sett last one to false.
     * <p>
     * Example usage here with an array of enchantments:
     * "PROTECTION_FIRE;1;false","PROTECTION_EXPLOSIONS;15;true","WATER_WORKER;1;false".
     *
     * @param enchantments list of enchantments you want to add.
     * @return this class.
     */
    public EnhancementMeta addEnchantments(final String... enchantments) {
        for (final String enchant : enchantments) {
            final int middle = enchant.indexOf(";");
            final int last = enchant.lastIndexOf(";");
            final int level = Integer.parseInt(enchant.substring(middle + 1, Math.max(last, enchant.length())));
            final boolean ignoreLevel = Boolean.getBoolean(enchant.substring(last + 1));
            final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchant));
            if (enchantment != null) {
                final EnhancementWrapper enhancementWrapper = new EnhancementWrapper(enchantment, level, last > 0 && ignoreLevel);
                setEnchantment(enhancementWrapper);
            } else {
                logger.log(() -> "your enchantment string: " + enchant + " , are not valid. You must build your string like this 'PROTECTION_FIRE;1;false' or 'PROTECTION_FIRE;1'. " +
                        "If everything is correctly setup, then your minecraft version doesn't have that enchantment.");
            }

        }
        return this;
    }

    /**
     * If it shall display the enchantments on the item.
     *
     * @return true if it shall show the enchantments.
     */
    public boolean isShowEnchantments() {
        return showEnchantments;
    }

    /**
     * When use {@link #setEnchantment(Enchantment)},{@link #setEnchantment(Enchantment, Consumer)}  or {@link #addEnchantments(String...)} and
     * want to not show enchants. When set this to false it will ignore what you set in this method
     * {@link CreateItemStack#setGlow(boolean)}.
     *
     * @param showEnchantments {@code true} and will show enchants.
     * @return this class.
     */
    public EnhancementMeta setShowEnchantments(final boolean showEnchantments) {
        this.showEnchantments = showEnchantments;
        return this;
    }

    /**
     * Get enchantments for this item.
     *
     * @return map with enchantment level and if it shall ignore level restriction.
     */
    public Map<Enchantment, EnhancementWrapper> getEnchantments() {
        return enchantments;
    }

    /**
     * Applies all stored enchantments to the given {@link ItemMeta}.
     * <p>
     * If the provided {@code itemMeta} does not support enchantments,
     * this method does nothing.
     * </p>
     *
     * @param itemMeta the {@link ItemMeta} to apply enchantments to (non-null)
     */
    public void applyEnchantments(@Nonnull final ItemMeta itemMeta) {
        Map<Enchantment, EnhancementWrapper> wrapperMap = this.getEnchantments();
        if (!wrapperMap.isEmpty()) {

            for (final Map.Entry<Enchantment, EnhancementWrapper> enchant : wrapperMap.entrySet()) {
                if (enchant == null) {
                    logger.log(() -> "Your enchantment are null.");
                    continue;
                }
                final EnhancementWrapper enchantData = enchant.getValue();
                itemMeta.addEnchant(enchant.getKey(), enchantData.getLevel(), enchantData.isIgnoreLevelRestriction());
            }
            if (!isShowEnchantments()) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            }
        }
    }

    /**
     * Add enchantment on an itemStack. Set {@link #setShowEnchantments(boolean)} to false
     * if you want to hide all enchants (default so will it not hide enchants).
     *
     * @param enhancementWrapper The wrapper class to set the enhancement data.
     */
    private void setEnchantment(@Nonnull final EnhancementWrapper enhancementWrapper) {
        final Enchantment enchantment = enhancementWrapper.getEnchantment();
        this.enchantments.put(enchantment, enhancementWrapper);
    }

    /**
     * Wrapper class around {@link Enchantment} that holds the enchantment,
     * its level, and whether to ignore the usual level restrictions.
     */
    public static class EnhancementWrapper {

        @Nonnull
        private final Enchantment enchantment;
        private int level;
        private boolean ignoreLevelRestriction;

        /**
         * Constructs an {@code EnhancementWrapper} with the specified enchantment and level.
         * Level restrictions are not ignored by default.
         *
         * @param enchantment the enchantment instance (must not be null)
         * @param level       the level of the enchantment
         */
        public EnhancementWrapper(@Nonnull final Enchantment enchantment, final int level) {
            this(enchantment, level, false);
        }

        /**
         * Constructs an {@code EnhancementWrapper} with the specified enchantment, level,
         * and whether to ignore level restrictions.
         *
         * @param enchantment            the enchantment instance (must not be null)
         * @param level                  the level of the enchantment
         * @param ignoreLevelRestriction whether to bypass the enchantment's level restrictions
         */
        public EnhancementWrapper(@Nonnull final Enchantment enchantment, final int level, final boolean ignoreLevelRestriction) {
            this.enchantment = enchantment;
            this.level = level;
            this.ignoreLevelRestriction = ignoreLevelRestriction;
        }

        /**
         * Gets the wrapped enchantment.
         *
         * @return the enchantment instance (never null)
         */
        @Nonnull
        public Enchantment getEnchantment() {
            return enchantment;
        }

        /**
         * Gets the level of this enchantment.
         * Will always return at least 1 even if a lower level was set.
         *
         * @return the enchantment level, minimum 1
         */
        public int getLevel() {
            return Math.max(this.level, 1);
        }

        /**
         * Sets the level of the enchantment.
         *
         * @param level the new level to set
         * @return this instance for chaining
         */
        public EnhancementWrapper setLevel(int level) {
            this.level = level;
            return this;
        }

        /**
         * Checks whether this wrapper is set to ignore level restrictions for the enchantment.
         *
         * @return true if level restrictions are ignored, false otherwise
         */
        public boolean isIgnoreLevelRestriction() {
            return ignoreLevelRestriction;
        }

        /**
         * Sets whether to ignore level restrictions for this enchantment.
         *
         * @param ignoreLevelRestriction true to ignore restrictions, false otherwise
         * @return this instance for chaining
         */
        public EnhancementWrapper setIgnoreLevelRestriction(boolean ignoreLevelRestriction) {
            this.ignoreLevelRestriction = ignoreLevelRestriction;
            return this;
        }

        @Override
        public String toString() {
            return "enchantment= " +
                    enchantment +
                    " level= " +
                    level +
                    " ignoreLevel= " +
                    ignoreLevelRestriction;
        }
    }
}
