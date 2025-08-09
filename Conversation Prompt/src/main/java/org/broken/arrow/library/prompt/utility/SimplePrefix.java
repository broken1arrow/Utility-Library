package org.broken.arrow.library.prompt.utility;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

import javax.annotation.Nonnull;

/**
 * The set prefix to use in the conversion.
 */
public final class SimplePrefix implements ConversationPrefix {

	private final String prefix;

	/**
	 * Construct new instance of the prefix.
	 * @param prefix the prefix you want to tag the messages with.
	 */
	public SimplePrefix(final String prefix) {
		this.prefix = prefix;
	}

	@Nonnull
	@Override
	public String getPrefix(@Nonnull final ConversationContext context) {
		return this.prefix;
	}
}