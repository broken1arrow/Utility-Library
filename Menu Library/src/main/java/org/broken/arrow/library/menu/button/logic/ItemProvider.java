package org.broken.arrow.library.menu.button.logic;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * A functional interface designed to dynamically generate or provide an {@link ItemStack}
 * for a specific slot within a paginated dataset.
 *
 * @param <T> the type of the contextual object associated with the slot; may be {@code null}.
 */
@FunctionalInterface
public interface ItemProvider<T> {

    /**
     * Generates the display item for a specific global pagination index.
     * <p>
     * The framework automatically calculates the cumulative data position across pages.
     * While {@code fillObject} is the preferred way to access your slot data, the {@code slot}
     * parameter provides the absolute index within the global dataset.
     * </p>
     * <br>
     * <strong>Use Cases for the index:</strong> Displaying dynamic leaderboard ranks (e.g., {@code slot + 1}),
     * performing alternating visual patterns, or executing parallel lookups in external collections.
     * </br>
     *
     * @param slot       the absolute, page-adjusted index of the item across the entire collection.
     * @param fillObject the pre-mapped data object for this index; may be {@code null}.
     * @return the configured {@link ItemStack}, or {@code null} to render an empty slot.
     */
    @Nullable
    ItemStack apply(int slot, @Nullable T fillObject);

}
