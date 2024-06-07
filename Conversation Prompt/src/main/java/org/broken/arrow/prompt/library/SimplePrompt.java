package org.broken.arrow.prompt.library;


import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The SimplePrompt class is an abstract class that represents a simple command prompt in-game.
 * It allows you to create a chat-based interface for executing different types of commands.
 * This class provides an easier implementation of SimpleConversation.
 */
public abstract class SimplePrompt extends ValidatingPrompt {

    private final Logging logger = new Logging(SimplePrompt.class);

    /**
     * Retrieves the prompt message to be displayed to the player.
     *
     * @param context The conversation context.
     * @return The prompt message.
     */
    protected abstract String getPrompt(ConversationContext context);

    private Player player = null;

    /**
     * Retrieves the prompt text to be displayed to the player.
     *
     * @param context The conversation context.
     * @return The prompt text.
     */
    @Nonnull
    @Override
    public final String getPromptText(@Nonnull final ConversationContext context) {
        return getPrompt(context);
    }

    /**
     * Checks if the player input is valid.
     *
     * @param context The conversation context.
     * @param input   The player input.
     * @return True if the input is valid, false otherwise.
     */
    @Override
    protected boolean isInputValid(@Nonnull final ConversationContext context, @Nullable final String input) {
        return true;
    }

    /**
     * Starts a new SimpleConversation with the specified player.
     *
     * @param player The player to start the conversation with.
     * @param plugin The plugin instance.
     * @return The started SimpleConversation instance.
     */
    public final SimpleConversation start(@Nonnull final Plugin plugin, @Nonnull final Player player) {
        this.player = player;
        Validate.checkNotNull(plugin, "Please provide a plugin instance before using this method.");
        final SimpleConversation conversation = new SimpleConversation(plugin) {
            @Override
            public Prompt getFirstPrompt() {
                return SimplePrompt.this;
            }

        };
        conversation.start(player);
        return conversation;
    }

    /**
     * Called when the whole conversation is over. This is called before {@link SimpleConversation#onConversationEnd(ConversationAbandonedEvent)}
     *
     * @param conversation the message sent when end conversation.
     * @param event        the event when conversation ends.
     */
    public void onConversationEnd(final SimpleConversation conversation, final ConversationAbandonedEvent event) {
    }

    /**
     * Converts the {@link ConversationContext} into a {@link Player}.
     *
     * @param ctx conversation context.
     * @return A player or null if the convertible is a player.
     */
    protected final Player getPlayer(@Nonnull final ConversationContext ctx) {
        if (!(ctx.getForWhom() instanceof Player))
            return null;
        return (Player) ctx.getForWhom();
    }

    /**
     * Get the player that do the conversation.
     *
     * @return the player or null if not player is set.
     */
    @Nullable
    protected final Player getPlayer() {
        return this.player;
    }

    @Nullable
    @Override
    public final Prompt acceptInput(@Nonnull final ConversationContext context, final String input) {
        if (isInputValid(context, input))
            return acceptValidatedInput(context, input);
        else {
            // Redisplay this prompt to the user to re-collect input
            return this;
        }
    }

    public SimplePrompt copy() {
        SimplePrompt simplePrompt;
        try {
            simplePrompt = (SimplePrompt) super.clone();
            simplePrompt.player = player;
        } catch (CloneNotSupportedException e) {
            logger.log(e,()-> Logging.of("Fail to clone this class " + SimplePrompt.class.getName()));
            return null;
        }
        return simplePrompt;
    }

}
