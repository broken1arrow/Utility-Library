package org.broken.arrow.prompt.library.utility;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

import javax.annotation.Nonnull;

public final class SimplePrefix implements ConversationPrefix {

	private final String prefix;

	public SimplePrefix(final String prefix) {
		this.prefix = prefix;
	}

	@Nonnull
	@Override
	public String getPrefix(@Nonnull final ConversationContext context) {
		return this.prefix;
	}
}