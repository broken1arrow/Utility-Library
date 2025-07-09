package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.CreateItemStack;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EnhancementMeta {
    private static final Logging logger = new Logging(ColorMeta.class);
    private final Map<Enchantment, EnhancementWrapper> enchantments = new HashMap<>();
    private boolean showEnchantments = true;

    /**
     * Set enchantments on an itemStack. Set {@link #setShowEnchantments(boolean)} to true
     * if you want to hide all enchants (default so will it not hide enchants).
     *
     * @param enchantment     The enchantment type you want to set.
     * @param wrapperConsumer The wrapper class to set the enhancement data.
     * @return this class.
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

    public static class EnhancementWrapper {

        @Nonnull
        private final Enchantment enchantment;
        private int level;
        private boolean ignoreLevelRestriction;

        public EnhancementWrapper(@Nonnull final Enchantment enchantment, final int level) {
            this(enchantment, level, false);
        }

        public EnhancementWrapper(@Nonnull final Enchantment enchantment, final int level, final boolean ignoreLevelRestriction) {
            this.enchantment = enchantment;
            this.level = level;
            this.ignoreLevelRestriction = ignoreLevelRestriction;
        }

        @Nonnull
        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getLevel() {
            return Math.max(this.level, 1);
        }

        public EnhancementWrapper setLevel(int level) {
            this.level = level;
            return this;
        }

        public boolean isIgnoreLevelRestriction() {
            return ignoreLevelRestriction;
        }

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
