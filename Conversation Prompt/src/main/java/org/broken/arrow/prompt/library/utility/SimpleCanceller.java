package org.broken.arrow.prompt.library.utility;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public final class SimpleCanceller implements ConversationCanceller {
	private final List<String> cancelPhrases;

	public SimpleCanceller(final String... cancelPhrases) {
		this(Arrays.asList(cancelPhrases));
	}

	public SimpleCanceller(final List<String> cancelPhrases) {
		Validate.checkBoolean(cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!");

		this.cancelPhrases = cancelPhrases;
	}

	@Override
	public void setConversation(@Nonnull final Conversation conversation) {

	}

	@Override
	public boolean cancelBasedOnInput(@Nonnull final ConversationContext context, @Nonnull final String input) {
		for (final String phrase : this.cancelPhrases)
			if (input.equalsIgnoreCase(phrase))
				return true;

		return false;
	}

	@Nonnull
	@Override
	public ConversationCanceller clone() {
		return new SimpleCanceller(cancelPhrases);
	}
}