package org.broken.arrow.menu.library.utility;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;


/**
 * Utility class for matching ItemStacks based on the criteria you set
 * with the enum class FilterMatch.
 *
 * @see FilterMatch
 */
public class MatchCheckItemStack {

    /**
     * Matches two ItemStacks based on the specified FilterMatch criteria.
     *
     * @param filterMatch the criteria used to match the ItemStacks.
     * @param first       the first ItemStack to compare, can be null.
     * @param second      the second ItemStack to compare, can be null.
     * @return {@code true} if the ItemStacks match based on the criteria, {@code false} otherwise.
     */
    public boolean match(@Nonnull FilterMatch filterMatch, @Nullable ItemStack first, @Nullable ItemStack second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.getType() != second.getType()) {
            return false;
        }

        switch (filterMatch) {
            case TYPE:
                return true;
            case META:
                return first.isSimilar(second);
            case TYPE_NAME_LORE:
                if (first.getType() == second.getType()) {
                    ItemMeta firstMeta = first.getItemMeta();
                    ItemMeta secondMeta = second.getItemMeta();

                    if (firstMeta == null || secondMeta == null) {
                        return firstMeta == secondMeta;
                    }

                    if (!firstMeta.getDisplayName().equals(secondMeta.getDisplayName())) {
                        return false;
                    }
                    List<String> firstLore = firstMeta.getLore();
                    List<String> secondLore = secondMeta.getLore();

                    return Objects.equals(firstLore, secondLore);
                }
                return false;
            default:
                return false;
        }
    }
}
