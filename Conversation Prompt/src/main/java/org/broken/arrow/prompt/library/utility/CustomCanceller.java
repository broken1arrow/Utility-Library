package org.broken.arrow.prompt.library.utility;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * A custom implementation of the InactivityConversationCanceller that cancels a conversation after a specified
 * amount of inactivity.
 */
public final class CustomCanceller extends InactivityConversationCanceller {

	/**
	 * Creates a CustomCanceller that extends the InactivityConversationCanceller to override the amount of seconds
	 * before the conversation times out due to inactivity.
	 *
	 * @param plugin         The owning plugin.
	 * @param timeoutSeconds The number of seconds of inactivity to wait before canceling the conversation.
	 */
	public CustomCanceller(@Nonnull final Plugin plugin, final int timeoutSeconds) {
		super(plugin, timeoutSeconds);
	}

	/**
	 * Sets the cancel data in the SessionData when the
	 * conversation is canceled due to inactivity.
	 *
	 * @param conversation The conversation that is being canceled.
	 */
	@Override
	protected void cancelling(final Conversation conversation) {
		conversation.getContext().setSessionData("FLP#TIMEOUT", true);
	}
}