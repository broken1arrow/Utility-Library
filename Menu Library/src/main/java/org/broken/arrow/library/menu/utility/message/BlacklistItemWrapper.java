package org.broken.arrow.library.menu.utility.message;

import org.broken.arrow.library.serialize.utility.converters.PlaceholderTranslator;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper class holding information about blacklisted items for placeholder substitution.
 */
public class BlacklistItemWrapper {
    private final PlaceholderTranslator.PlaceholderWrapper placeholderWrapper;
    private final ItemStack itemStack;
    private final int size;

    /**
     * Constructs a new wrapper containing blacklisted items data.
     *
     * @param itemStack the blacklisted item stack
     * @param size      the total number of blacklisted items
     */
    public BlacklistItemWrapper(final ItemStack itemStack, final int size) {
        this.placeholderWrapper = new PlaceholderTranslator.PlaceholderWrapper();
        this.itemStack = itemStack;
        this.size = size;
    }

    /**
     * Returns the blacklisted item stack.
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Returns the total number of items returned back.
     *
     * @return duplicated stacks count
     */
    public int getSize() {
        return size;
    }


    /**
     * Returns a {@link PlaceholderTranslator.PlaceholderWrapper} for defining custom
     * key-based placeholders instead of using the default indexed placeholder system.
     *
     * <p>If no custom placeholders are provided, the system falls back to
     * {@link #retrieveAsPlaceholderData()}, which uses ordered placeholders such as
     * {@code {0}}, {@code {1}}, {@code {2}}.</p>
     *
     * <p>When this wrapper contains entries, it is used instead and allows named
     * placeholders.</p>
     *
     * <p><b>Example (default indexed placeholders):</b></p>
     * <pre>
     * {@code "&fYou can't add more if this &6 {0} &ftype, you get back &6 {2} &fitems. You have added totally &4 {1} &fextra itemstacks"}
     * </pre>
     *
     * <p><b>Example (custom named placeholders):</b></p>
     * <pre>
     * {@code
     * wrapper.put("{type}", itemStack.getType())
     * .put("{addedStacks}", size)
     * .put("{returnedItems}", itemAmount);
     * }
     * {@code "&fYou can't add more if this &6 {type} &ftype, you get back &6 {returnedItems} &fitems. You have added totally &4 {addedStacks} &fextra itemstacks"}
     * </pre>
     *
     * @return the {@link PlaceholderTranslator.PlaceholderWrapper} for custom placeholders
     */
    public PlaceholderTranslator.PlaceholderWrapper getPlaceholderWrapper() {
        return placeholderWrapper;
    }

    /**
     * Returns an array of objects to be used as placeholders in messages:
     * item type, duplicated stacks count, and duplicated items count.
     *
     * @return an array of placeholder data objects
     */
    public Object[] retrieveAsPlaceholderData() {
        return new Object[]{itemStack.getType(), size};
    }
}