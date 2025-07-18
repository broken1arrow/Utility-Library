package org.broken.arrow.library.prompt.utility;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * A simple implementation of the {@link ConversationCanceller} interface that cancels a conversation based on
 * specified cancel phrases.
 */
public final class SimpleCanceller implements ConversationCanceller {
    private final Logging logger = new Logging(SimpleCanceller.class);
    private final List<String> cancelPhrases;

    /**
     * Constructs a SimpleCanceller with the specified cancel phrases.
     *
     * @param cancelPhrases The phrases that will trigger the cancellation of the conversation.
     * @throws IllegalArgumentException If the provided list of cancel phrases is empty.
     */
    public SimpleCanceller(final String... cancelPhrases) {
        this(Arrays.asList(cancelPhrases));
    }

    /**
     * Constructs a SimpleCanceller with the specified list of cancel phrases.
     *
     * @param cancelPhrases The list of phrases that will trigger the cancellation of the conversation.
     * @throws IllegalArgumentException If the provided list of cancel phrases is empty.
     */
    public SimpleCanceller(final List<String> cancelPhrases) {
        Validate.checkBoolean(cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!");

        this.cancelPhrases = cancelPhrases;
    }

    /**
     * Sets the conversation for this canceller.
     *
     * @param conversation The conversation to be set.
     */
    @Override
    public void setConversation(@Nonnull final Conversation conversation) {
        //This method is empty, for you must override this method to use it.

    }

    /**
     * Determines whether the conversation should be canceled based on the input provided.
     *
     * @param context The conversation context.
     * @param input   The input received in the conversation.
     * @return {@code true} if the input matches any of the cancel phrases, otherwise {@code false}.
     */
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
        try {
            SimpleCanceller simpleCanceller = (SimpleCanceller) super.clone();
            simpleCanceller.cancelPhrases.clear();
            simpleCanceller.cancelPhrases.addAll(cancelPhrases);
            return simpleCanceller;
        } catch (CloneNotSupportedException e) {
            logger.log(e,()-> "Fail to clone this class " + SimpleCanceller.class.getName());
            return new SimpleCanceller("quit", "cancel", "exit");
        }
    }
}