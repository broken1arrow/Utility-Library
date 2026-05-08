package org.broken.arrow.library.menu.utility.message;

import org.broken.arrow.library.serialize.utility.converters.PlaceholderTranslator;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper class holding information about duplicated items for placeholder substitution.
 */
public class DuplicatedItemWrapper {
    private final PlaceholderTranslator.PlaceholderWrapper placeholderWrapper;
    private final ItemStack itemStack;
    private final int size;
    private final int itemAmount;

    /**
     * Constructs a new wrapper containing duplicated item data.
     *
     * @param itemStack  the duplicated item stack
     * @param size       the total number of duplicated stacks
     * @param itemAmount the total number of duplicated items
     */
    public DuplicatedItemWrapper(final ItemStack itemStack, final int size, final int itemAmount) {
        this.placeholderWrapper = new PlaceholderTranslator.PlaceholderWrapper();
        this.itemStack = itemStack;
        this.size = size;
        this.itemAmount = itemAmount;
    }

    /**
     * Returns the duplicated item stack.
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Returns the total number of duplicated stacks.
     *
     * @return duplicated stacks count
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the total number of duplicated items.
     *
     * @return duplicated item count
     */
    public int getItemAmount() {
        return itemAmount;
    }

    /**
     * Returns a {@link PlaceholderTranslator.PlaceholderWrapper} for defining custom
     * key-based placeholders instead of using the default indexed placeholder system.
     *
     * <p>If no custom placeholders are provided, the system falls back to
     * {@link #retrieveAsPlaceholderData()}, which uses indexed placeholders such as
     * <code>{0}</code>, <code>{1}</code>, <code>{2}</code>.</p>
     *
     * <p>When this wrapper contains entries, it overrides the default behavior and
     * allows the use of named placeholders.</p>
     *
     * <p><b>Example (default indexed placeholders):</b></p>
     * <pre>{@code
     * &fYou can't add more of this &6{0}&f type. You get back &6{2}&f items.
     * You have added a total of &4{1}&f extra item stacks.
     * }</pre>
     *
     * <p><b>Example (custom named placeholders):</b></p>
     * <pre>{@code
     * wrapper.put("{type}", itemStack.getType())
     *        .put("{addedStacks}", size)
     *        .put("{returnedItems}", itemAmount);
     *
     * "&fYou can't add more of this &6{type}&f type. You get back &6{returnedItems}&f items.
     * You have added a total of &4{addedStacks}&f extra item stacks."
     * }</pre>
     *
     * @return the {@link PlaceholderTranslator.PlaceholderWrapper} used for custom placeholders
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
        return new Object[]{itemStack.getType(), size, itemAmount};
    }
}