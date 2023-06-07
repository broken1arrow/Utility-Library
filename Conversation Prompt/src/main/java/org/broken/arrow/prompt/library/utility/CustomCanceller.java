package org.broken.arrow.prompt.library.utility;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public final class CustomCanceller extends InactivityConversationCanceller {

	/**
	 * Creates an InactivityConversationCanceller.
	 *
	 * @param plugin         The owning plugin.
	 * @param timeoutSeconds The number of seconds of inactivity to wait.
	 */
	public CustomCanceller(@Nonnull final Plugin plugin, final int timeoutSeconds) {
		super(plugin, timeoutSeconds);
	}

	@Override
	protected void cancelling(final Conversation conversation) {
		conversation.getContext().setSessionData("FLP#TIMEOUT", true);
	}
}