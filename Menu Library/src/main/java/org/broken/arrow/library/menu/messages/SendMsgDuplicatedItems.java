package org.broken.arrow.library.menu.messages;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.menu.utility.DuplicateMessage;
import org.broken.arrow.library.serialize.utility.converters.PlaceholderTranslator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Set messages when player add duplicated items or items you have blacklisted.
 */
public class SendMsgDuplicatedItems {
    private Function<ItemStack, String> blacklistMessage;
    private DuplicateMessage duplicatedMessage;
    private boolean notFoundTextTranslator;

    /**
     * When create instance of this class it will check if the
     * color translation exists.
     */
    public SendMsgDuplicatedItems() {
        try {
            TextTranslator.getInstance();
        } catch (NoClassDefFoundError ignore) {
            notFoundTextTranslator = true;
        }
    }

    /**
     * Set message for when player have added item some are blacklisted.
     * Support both hex and &amp; color codes.
     *
     * <p><b>Supported color formats:</b></p>
     * <ul>
     *   <li>Hex colors: {@code <#8000ff>} or gradients like {@code <#8000ff:#ff0080>} (requires color conversion module).</li>
     *   <li>Fallback to Spigot's legacy format: {@code &x&6&6&6&6&6&6}, or for white: {@code &x&F&F&F&F&F&F}, if color conversion is not enabled.</li>
     * </ul>
     *
     * <p>&nbsp;</p>
     * The placeholders to use:
     * &ndash; {0} = item type
     *
     * @param blacklistMessage set a message.
     */
    public void setBlacklistMessage(final String blacklistMessage) {
        this.blacklistMessage = itemStack -> blacklistMessage;
    }

    /**
     * Sets the message to display when a player attempts to add an item that is blacklisted.
     *
     * <p>
     * Supports both hex color codes and legacy {@code &} codes. This function allows you to handle placeholder
     * replacement yourself instead of using a predefined message format, giving you more dynamic control, particularly useful
     * if you're using a localization file or want the text to be updated externally with minimal code changes.
     * </p>
     *
     * <p><b>Supported color formats:</b></p>
     * <ul>
     *   <li>Hex colors: {@code <#8000ff>} or gradients like {@code <#8000ff:#ff0080>} (requires the color conversion module).</li>
     *   <li>Fallback to Spigot's legacy format: {@code &x&6&6&6&6&6&6}, or for white: {@code &x&F&F&F&F&F&F}, if color conversion is not enabled.</li>
     * </ul>
     *
     * <p><b>Available placeholders:</b></p>
     * <ul>
     *   <li>{@code {0}} – The item type (e.g., {@code DIAMOND_SWORD})</li>
     * </ul>
     *
     * <p>
     * The returned string will be further processed after this function is called — including color conversion
     * and placeholder replacement (e.g., {@code {0}}). You only need to provide the
     * base message with the desired format and placeholders.
     * </p>
     *
     * <p>
     * You may also choose to handle placeholder translation yourself by including resolved values directly
     * in the returned string. If you want full control over the output, including color formatting, simply return
     * an empty string or {@code null} to skip the default message generation and formatting.
     * </p>
     *
     * @param blacklistMessage a function that receives the {@link ItemStack} and returns the base message string to process.
     */
    public void setBlacklistMessage(final Function<ItemStack, String> blacklistMessage) {
        this.blacklistMessage = blacklistMessage;
    }

    /**
     * Sets a static message format for duplicated item handling.
     *
     * <p>This is a convenience method equivalent to:</p>
     * <pre>
     * setDuplicatedMessage(wrapper -> "your message");
     * </pre>
     *
     * <p>The message supports placeholders and color codes and will be processed automatically.</p>
     *
     * <p><b>Formatting support:</b></p>
     * <ul>
     *   <li>Legacy color codes: {@code &a}, {@code &f}, etc.</li>
     *   <li>Legacy hex format (Spigot-compatible): {@code &x&R&R&G&G&B&B}</li>
     *   <li>Hex colors: {@code <#RRGGBB>} and gradients like {@code <#RRGGBB:#RRGGBB>}
     *       (supported when the internal formatter is available)</li>
     * </ul>
     *
     * <p>Formatting is handled automatically using the best available implementation
     * (Spigot-compatible or internal formatter).</p>
     *
     * <p><b>Available indexed placeholders:</b></p>
     * <ul>
     *   <li>{@code {0}} – Item type (e.g. {@code DIAMOND_SWORD})</li>
     *   <li>{@code {1}} – Total duplicated stacks</li>
     *   <li>{@code {2}} – Total duplicated item amount</li>
     * </ul>
     *
     * <p><b>Note:</b> If {@link DuplicatedItemWrapper#getPlaceholderWrapper()} is used, those take priority over indexed placeholders.</p>
     *
     * @param duplicatedMessage message format string
     */
    public void setDuplicatedMessage(String duplicatedMessage) {
        this.duplicatedMessage = (wrapper) -> duplicatedMessage;
    }

    /**
     * Sets a custom message generator for duplicated item handling.
     *
     * <p>This allows full control over the final message, including placeholders and formatting.
     * The provided function receives a {@link DuplicatedItemWrapper}, and the returned string
     * is processed automatically.</p>
     *
     * <p>If not set, a default message format is used.</p>
     *
     * <p><b>Formatting support:</b></p>
     * <ul>
     *   <li>Legacy color codes: {@code &a}, {@code &f}, etc.</li>
     *   <li>Legacy hex format (Spigot-compatible): {@code &x&R&R&G&G&B&B}</li>
     *   <li>Hex colors: {@code <#RRGGBB>} and gradients like {@code <#RRGGBB:#RRGGBB>}
     *       (supported when the internal formatter is available)</li>
     * </ul>
     *
     * <p>Formatting is handled automatically using the best available implementation
     * (Spigot-compatible or internal formatter).</p>
     *
     * <p><b>Available indexed placeholders:</b></p>
     * <ul>
     *   <li>{@code {0}} – Item type (e.g. {@code DIAMOND_SWORD})</li>
     *   <li>{@code {1}} – Total duplicated stacks</li>
     *   <li>{@code {2}} – Total duplicated item amount</li>
     * </ul>
     *
     * <p><b>Note:</b> If {@link DuplicatedItemWrapper#getPlaceholderWrapper()} is used, those take priority over indexed placeholders.</p>
     *
     * @param duplicatedMessage function that generates the base message from duplicated item data.
     */
    public void setDuplicatedMessage(final DuplicateMessage duplicatedMessage) {
        this.duplicatedMessage = duplicatedMessage;
    }

    /**
     * Sends a raw message to the specified player.
     *
     * @param player the player to send the message to
     * @param msg    the message string to send
     */
    public void sendMessage(Player player, String msg) {
        player.sendMessage(msg);
    }

    /**
     * Sends the blacklist message to the player regarding the specified blacklisted item.
     * The message supports color codes and placeholder replacement.
     *
     * @param player    the player to send the message to
     * @param itemStack the blacklisted item stack triggering the message
     */
    public void sendBlacklistMessage(Player player, ItemStack itemStack) {
        String message;
        if (blacklistMessage == null) {
            message = "&fThis item&6 {0}&f are blacklisted and you get the items back.";
        } else {
            message = blacklistMessage.apply(itemStack.clone());
        }

        if (message == null || message.isEmpty())
            return;

        String itemName = itemStack.getType().name().toLowerCase();
        if (notFoundTextTranslator)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, itemName))));
        else player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, itemName)));
    }

    /**
     * Sends the duplicated item message to the player, using data from the provided placeholder wrapper.
     * The message supports color codes and placeholder replacement.
     *
     * @param player          the player to send the message to
     * @param placeholderData wrapper containing duplicated item data for placeholders
     */
    public void sendDuplicatedMessage(@Nonnull final Player player, @Nonnull final DuplicatedItemWrapper placeholderData) {
        String message;
        if (duplicatedMessage == null) {
            message = "&fYou can't add more if this &6 {0} &ftype, you get back &6 {2}&f items.You have added totally &4{1}&f extra itemstacks";
        } else {
            message = duplicatedMessage.apply(placeholderData);
        }

        if (message == null || message.isEmpty())
            return;

        final PlaceholderTranslator.PlaceholderWrapper wrapper = placeholderData.getPlaceholderWrapper();
        if (!wrapper.getPlaceholders().isEmpty())
            message = PlaceholderTranslator.translateText(message, wrapper);
        else {
            message = PlaceholderTranslator.translateText(message, placeholderData.retrieveAsPlaceholderData());
        }

        if (notFoundTextTranslator)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        else
            player.sendMessage(TextTranslator.toSpigotFormat(message));
    }

    /**
     * Replaces placeholders of the form {@code {0}, {1}, ...} in the given text with
     * the string representation of the provided placeholder objects.
     *
     * @param rawText      the text containing placeholders to replace
     * @param placeholders the objects to insert into the placeholders
     * @return the text with placeholders replaced by their corresponding values
     */
    public String translatePlaceholders(String rawText, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
        }
        return rawText;
    }

    /**
     * Wrapper class holding information about duplicated items for placeholder substitution.
     */
    public static class DuplicatedItemWrapper {
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
         *     {@code
         * wrapper.put("{type}", itemStack.getType())
                   .put("{addedStacks}", size)
                   .put("{returnedItems}", itemAmount);
              }
         *
         * "&fYou can't add more if this &6 {type} &ftype, you get back &6 {returnedItems} &fitems.
         *  You have added totally &4 {addedStacks} &fextra itemstacks"
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
            return new Object[]{itemStack.getType(), size, itemAmount};
        }
    }
}
