package org.broken.arrow.library.menu.utility;

import org.broken.arrow.library.menu.messages.SendMsgDuplicatedItems;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents a duplicate message that takes three arguments and produces a result.
 *
 */
public interface DuplicateMessage {

    /**
     * Applies this message to the given argument.
     *
     * @param duplicatedWrapper The instance of the wrapper to retrieve the data on the item that is a duplicate.
     * @return the text set that placeholders will be translated and color codes.
     */
    String apply(@Nonnull final SendMsgDuplicatedItems.DuplicatedItemWrapper duplicatedWrapper);

}
