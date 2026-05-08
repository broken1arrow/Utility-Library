package org.broken.arrow.library.menu.utility.message;

import org.broken.arrow.library.menu.messages.SendMsgDuplicatedItems;

import javax.annotation.Nonnull;

/**
 * Represents a duplicate message function.
 */
public interface DuplicateMessage {

    /**
     * Applies this message to the given argument.
     *
     * @param duplicatedWrapper The instance of the wrapper to retrieve the data on the item that is a duplicate.
     * @return the text set that placeholders will be translated and color codes.
     */
    String apply(@Nonnull final DuplicatedItemWrapper duplicatedWrapper);

}
