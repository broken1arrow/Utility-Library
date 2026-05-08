package org.broken.arrow.library.menu.utility.message;

import org.broken.arrow.library.menu.messages.SendMsgDuplicatedItems;

import javax.annotation.Nonnull;
/**
 * Represents the blacklisted item message.
 */
public interface BlacklistMessage {

    /**
     * Applies this message to the given argument.
     *
     * @param blacklistItemWrapper The instance of the wrapper to retrieve the data on the item that is blacklisted.
     * @return the text set that placeholders will be translated and color codes.
     */
    String apply(@Nonnull final BlacklistItemWrapper blacklistItemWrapper);
}
