package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.CreateItemStack;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemFlagsMeta {
    private List<ItemFlag> itemFlags;

    /**
     * Hide one or several metadata values on a itemstack.
     *
     * @param itemFlags add one or several flags you not want to hide.
     */
    public void setItemFlags(final ItemFlag... itemFlags) {
        this.setItemFlags(Arrays.asList(itemFlags));
    }

    /**
     * Hide one or several metadata values on a itemstack.
     *
     * @param itemFlags add one or several flags you not want to hide.
     */
    public void setItemFlags(final List<ItemFlag> itemFlags) {
        Validate.checkNotNull(itemFlags, "flags list should not be null");
        this.itemFlags = itemFlags;
    }

    /**
     * Get the list of flags set on this item.
     *
     * @return list of flags.
     */
    @Nonnull
    public List<ItemFlag> getItemFlags() {
        if (itemFlags == null) return new ArrayList<>();
        return itemFlags;
    }

    /**
     * Apply the metadata if it set.
     * @param itemMeta the metadata from the item to modify.
     */
    public void applyFlagsMenta(ItemMeta itemMeta) {
        final List<ItemFlag> flags = getItemFlags();
        if (flags.isEmpty()) return;

        itemMeta.addItemFlags(flags.toArray(new ItemFlag[0]));
    }
}
