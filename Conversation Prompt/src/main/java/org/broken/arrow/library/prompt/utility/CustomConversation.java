package org.broken.arrow.library.prompt.utility;

import org.broken.arrow.library.prompt.SimpleConversation;
import org.broken.arrow.library.prompt.SimplePrompt;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Represents a chat conversation extending the base {@link Conversation} class,
 * providing additional context and customization through a {@link SimpleConversation}.
 */
public class CustomConversation extends Conversation {
	private SimplePrompt lastSimplePrompt;
	private final SimpleConversation simpleConversation;

	/**
	 * Creates a new instance of {@code CustomConversation}.
	 *
	 * @param plugin            the plugin instance used to create and manage the conversation
	 * @param simpleConversation the {@link SimpleConversation} instance providing the conversation logic,
	 *                           including prompt flow, prefix handling, timeout, and cancellation behavior.
	 * @param forWhom           the player or conversable entity that started the conversation
	 */
	public CustomConversation(@Nonnull final Plugin plugin, @Nonnull final SimpleConversation simpleConversation, @Nonnull final Conversable forWhom) {
		super(plugin, forWhom, simpleConversation.getFirstPrompt());
		this.localEchoEnabled = false;
		this.simpleConversation = simpleConversation;
		if (simpleConversation.insertPrefix() && simpleConversation.getPrefix() != null)
			prefix = simpleConversation.getPrefix();

	}

	/**
	 * Gets the last prompt.
	 *
	 * @return The last prompt that was executed.
	 */
	public SimplePrompt getLastSimplePrompt() {
		return lastSimplePrompt;
	}

	/**
	 * Set the next prompt.
	 */
	@Override
	public void outputNextPrompt() {
		if (currentPrompt == null)
			abandon(new ConversationAbandonedEvent(this));

		else {
			// Save the time when we showed the question to the player
			// so that we only show it once per the given threshold
			final String promptClass = currentPrompt.getClass().getSimpleName();
			final String question = currentPrompt.getPromptText(context);

			try {
				final Object askedQuestions = context.getAllSessionData().getOrDefault("Asked_" + promptClass, simpleConversation.getTimeout());

				if (!askedQuestions.equals(question)) {
					context.setSessionData("Asked_" + promptClass, askedQuestions);
					context.getForWhom().sendRawMessage(prefix.getPrefix(context) + question);
				}
			} catch (final NoSuchMethodError ex) {
				// Unfortunately, old MC version was detected
			}
			// Save last prompt if it is our class
			if (currentPrompt instanceof SimplePrompt)
				lastSimplePrompt = ((SimplePrompt) currentPrompt).copy();

			if (!currentPrompt.blocksForInput(context)) {
				currentPrompt = currentPrompt.acceptInput(context, null);
				outputNextPrompt();
			}
		}
	}
}