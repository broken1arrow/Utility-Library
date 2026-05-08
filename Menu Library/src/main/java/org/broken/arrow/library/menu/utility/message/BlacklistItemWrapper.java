package org.broken.arrow.library.menu.utility.message;

import org.broken.arrow.library.serialize.utility.converters.PlaceholderTranslator;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper class holding information about blacklisted items for placeholder substitution.
 */
public class BlacklistItemWrapper {
    private final PlaceholderTranslator.PlaceholderWrapper placeholderWrapper;
    private final ItemStack itemStack;
    private final int amount;

    /**
     * Constructs a new wrapper containing blacklisted items data.
     *
     * @param itemStack the blacklisted item stack
     * @param amount      the total number of blacklisted items
     */
    public BlacklistItemWrapper(final ItemStack itemStack, final int amount) {
        this.placeholderWrapper = new PlaceholderTranslator.PlaceholderWrapper();
        this.itemStack = itemStack;
        this.amount = amount;
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
    public int getAmount() {
        return amount;
    }


    /**
     * Returns a {@link PlaceholderTranslator.PlaceholderWrapper} for defining custom
     * key-based placeholders instead of using the default indexed placeholder system.
     *
     * <p>If no custom placeholders are provided, the system falls back to
     * {@link #retrieveAsPlaceholderData()}, which uses indexed placeholders such as
     * <code>{0}</code>, <code>{1}</code>.</p>
     *
     * <p>When this wrapper contains entries, it overrides the default behavior and
     * allows the use of named placeholders.</p>
     *
     * <p><b>Example (default indexed placeholders):</b></p>
     * <pre>{@code
     * "&fThis item&6 {0}&f are blacklisted and you get {1} back."
     * }</pre>
     *
     * <p><b>Example (custom named placeholders):</b></p>
     * <pre>{@code
     * wrapper.put("{type}", itemStack.getType())
     *        .put("{amount}", amount);
     *
     * "&fThis item&6 {type}&f are blacklisted and you get {amount} back."
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
        return new Object[]{itemStack.getType(), amount};
    }
}