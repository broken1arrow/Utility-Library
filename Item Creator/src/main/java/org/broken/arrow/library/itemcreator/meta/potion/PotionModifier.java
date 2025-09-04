package org.broken.arrow.library.itemcreator.meta.potion;

/**
 * Enum for potion modifiers that represent how the potion is enhanced or extended.
 */
public enum PotionModifier {
    /**
     * The default version of the potion with standard duration and potency.
     */
    NORMAL,
    /**
     * A longer-lasting version of the potion, typically increasing the duration
     * from 3 minutes to 8 minutes.
     */
    LONG,
    /**
     * A stronger version of the potion with amplified effects,
     * typically at the cost of half the duration.
     * <p>
     * The only exception is the Turtle Master potion,
     * which retains the same duration as the base potion.
     */
    STRONG,
}