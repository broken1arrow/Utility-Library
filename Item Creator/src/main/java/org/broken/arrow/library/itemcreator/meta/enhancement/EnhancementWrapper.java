package org.broken.arrow.library.itemcreator.meta.enhancement;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

/**
 * Wrapper class around {@link Enchantment} that holds the enchantment,
 * its level, and whether to ignore the usual level restrictions.
 */
public class EnhancementWrapper {

    @Nonnull
    private final String enhancementName;
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
        this.enhancementName = ItemCreator.getEnchantmentName(enchantment);
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
        return ItemCreator.getEnchantment(enhancementName);
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
        return "name: " + enhancementName +
                ",\nlevel: " + level +
                ",\nignoreLevel: " + ignoreLevelRestriction + "";
    }
}
