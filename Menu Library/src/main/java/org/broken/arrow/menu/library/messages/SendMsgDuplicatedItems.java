package org.broken.arrow.menu.library.messages;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.menu.library.utility.TriFunction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Set messages when player add duplicated items or items you have blacklisted.
 */
public class SendMsgDuplicatedItems {
    private Function<ItemStack, String> blacklistMessage;
    private TriFunction<String, ItemStack, Integer, Integer> duplicatedMessage;
    private boolean notFoundTextTranslator;

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
     * Set message for when player have added item some are duplicated.
     * Support both hex and &amp; color codes.
     *
     * <p><b>Supported color formats:</b></p>
     * <ul>
     *   <li>Hex colors: {@code <#8000ff>} or gradients like {@code <#8000ff:#ff0080>} (requires color conversion module).</li>
     *   <li>Fallback to Spigot's legacy format: {@code &x&6&6&6&6&6&6}, or for white: {@code &x&F&F&F&F&F&F}, if color conversion is not enabled.</li>
     * </ul>
     *
     *  <p><b>Available placeholders:</b></p>
     * <ul>
     *   <li>{@code {0}} – The item type (e.g., {@code DIAMOND_SWORD})</li>
     *   <li>{@code {1}} – Total number of duplicated stacks</li>
     *   <li>{@code {2}} – Total number of duplicated items</li>
     * </ul>
     *
     * @param duplicatedMessage set a message.
     */
    public void setDuplicatedMessage(String duplicatedMessage) {
        this.duplicatedMessage = (itemStack, size, itemAmount) -> duplicatedMessage;
    }

    /**
     * Sets the message format to display when a player attempts to add items and some are duplicates.
     * <p>
     * Supports both hex color codes and legacy {@code &} codes. This function allows you to handle placeholder
     * replacement yourself instead of using a predefined message format, giving you more dynamic control, particularly useful
     * if you're using a localization file or want the text to be updated externally with minimal code changes.
     * </p>
     *
     * <p><b>Supported color formats:</b></p>
     * <ul>
     *   <li>Hex colors: {@code <#8000ff>} or gradients like {@code <#8000ff:#ff0080>} (requires color conversion module).</li>
     *   <li>Fallback to Spigot's legacy format: {@code &x&6&6&6&6&6&6}, or for white: {@code &x&F&F&F&F&F&F}, if color conversion is not enabled.</li>
     * </ul>
     *
     *  <p><b>Available placeholders:</b></p>
     * <ul>
     *   <li>{@code {0}} – The item type (e.g., {@code DIAMOND_SWORD})</li>
     *   <li>{@code {1}} – Total number of duplicated stacks</li>
     *   <li>{@code {2}} – Total number of duplicated items</li>
     * </ul>
     *
     * <p>
     * The returned string will be further processed after this function is called — including color conversion
     * and placeholder replacement (e.g., {@code {0}}, {@code {1}}, {@code {2}}). You only need to provide the
     * base message with the desired format and placeholders.
     * </p>
     *
     * <p>
     * You may also choose to handle placeholder translation yourself by including resolved values directly
     * in the returned string. If you want full control over the output, including color formatting, simply return
     * an empty string or {@code null} to skip the default message generation and formatting.
     * </p>
     *
     * @param duplicatedMessage a function that takes the item type, duplicated {@link ItemStack}, and total duplicated item count,
     *                          and returns the base message string to be processed.
     */
    public void setDuplicatedMessage(final TriFunction<String, ItemStack, Integer, Integer> duplicatedMessage) {
        this.duplicatedMessage = duplicatedMessage;
    }

    public void sendMessage(Player player, String msg) {
        player.sendMessage(msg);
    }

    public void sendBlacklistMessage(Player player, ItemStack itemStack) {
        String message;
        if (blacklistMessage == null) message = "&fThis item&6 {0}&f are blacklisted and you get the items back.";
        else message = blacklistMessage.apply(itemStack.clone());
        if (message == null || message.isEmpty())
            return;

        String itemName = itemStack.getType().name().toLowerCase();
        if (notFoundTextTranslator)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, itemName))));
        else player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, itemName)));
    }


    public void sendDuplicatedMessage(@Nonnull final Player player, @Nonnull final PlaceholderHelper placeholderData) {
        String message;
        if (duplicatedMessage == null) {
            message = "&fYou can't add more if this &6 {0} &ftype, you get back &6 {2}&f items.You have added totally &4{1}&f extra itemstacks";
        } else {
            message = duplicatedMessage.apply(placeholderData.getItemStack().clone(), placeholderData.getSize(), placeholderData.getItemAmount());
        }

        if (message == null || message.isEmpty())
            return;

        if (notFoundTextTranslator)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, placeholderData.retrieveAsPlaceholderData()))));
        else
            player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholderData.retrieveAsPlaceholderData())));
    }

    public String translatePlaceholders(String rawText, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
        }
        return rawText;
    }


    public static class PlaceholderHelper {

        private final ItemStack itemStack;
        private final int size;
        private final int itemAmount;

        public PlaceholderHelper(final ItemStack itemStack, final int size, final int itemAmount) {
            this.itemStack = itemStack;
            this.size = size;
            this.itemAmount = itemAmount;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getSize() {
            return size;
        }

        public int getItemAmount() {
            return itemAmount;
        }

        public Object[] retrieveAsPlaceholderData() {
            return new Object[]{itemStack.getType(), size, itemAmount};
        }
    }
}
