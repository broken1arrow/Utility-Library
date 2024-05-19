package org.broken.arrow.prompt.library;

import org.broken.arrow.logging.library.Validate;
import org.broken.arrow.prompt.library.utility.CustomCanceller;
import org.broken.arrow.prompt.library.utility.CustomConversation;
import org.broken.arrow.prompt.library.utility.SimpleCanceller;
import org.broken.arrow.prompt.library.utility.SimplePrefix;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * The SimpleConversation class provides a simple way to communicate with the player.
 * It isolates their chat messages and processes them as conversation input.
 */
public abstract class SimpleConversation implements ConversationAbandonedListener {

	private String prefix;
	private int timeout = 60;
	private final Plugin plugin;

	/**
	 * Creates a new simple conversation.
	 *
	 * @param plugin Your plugin instance.
	 */
	protected SimpleConversation(final Plugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Retrieves the first prompt in this conversation for the player.
	 *
	 * @return The first prompt.
	 */
	public abstract Prompt getFirstPrompt();

	/**
	 * Starts a conversation with the player.
	 *
	 * @param player The player to start the conversation with.
	 * @throws IllegalArgumentException if the player is already conversing.
	 */
	public final void start(@Nonnull final Player player) {
		Validate.checkBoolean(player.isConversing(), "Player " + player.getName() + " is already conversing!");
		// Do not allow open inventory since they cannot type anyways
		player.closeInventory();

		// Setup
		final CustomConversation conversation = new CustomConversation(this.plugin, this, player);
		final CustomCanceller canceller = new CustomCanceller(this.plugin, this.timeout);

		canceller.setConversation(conversation);

		conversation.getCancellers().add(canceller);
		conversation.getCancellers().add(getCanceller());

		conversation.addConversationAbandonedListener(this);

		conversation.begin();
	}

	/**
	 * Return the canceller that listens for certain words to exit the conversation,
	 * by default we use {@link SimpleCanceller} that listens to quit|cancel|exit
	 *
	 * @return cancel message.
	 */
	protected ConversationCanceller getCanceller() {
		return new SimpleCanceller("quit", "cancel", "exit");
	}

	@Override
	public void conversationAbandoned(@Nonnull final ConversationAbandonedEvent event) {
		final ConversationContext context = event.getContext();

		final Object source = event.getSource();
		final boolean hasTimeout = (boolean) context.getAllSessionData().getOrDefault("FLP#TIMEOUT", false);

		// Remove the session data so that they are invisible to other plugins
		context.getAllSessionData().remove("FLP#TIMEOUT");

		if (source instanceof CustomConversation) {
			final SimplePrompt lastPrompt = ((CustomConversation) source).getLastSimplePrompt();

			if (lastPrompt != null)
				lastPrompt.onConversationEnd(this, event);
		}

		onConversationEnd(event, hasTimeout);
	}

	/**
	 * Fired when the user quits this conversation (see {@link #getCanceller()}, or
	 * simply quits the game)
	 *
	 * @param event                  some get called when conversation ended.
	 * @param canceledFromInactivity true if user failed to enter input in the period set in {@link #getTimeout()}
	 */
	protected void onConversationEnd(final ConversationAbandonedEvent event, final boolean canceledFromInactivity) {
		this.onConversationEnd(event);
	}

	/**
	 * Called when the whole conversation is over. This is called before {@link SimpleConversation#onConversationEnd(ConversationAbandonedEvent)}
	 *
	 * @param conversation message send from server to player.
	 * @param event        some get called when conversation ended.
	 */
	protected void onConversationEnd(final SimpleConversation conversation, final ConversationAbandonedEvent event) {
	}

	/**
	 * Fired when the user quits this conversation (see {@link #getCanceller()}, or
	 * simply quits the game)
	 *
	 * @param event some get called when conversation ended.
	 */
	protected void onConversationEnd(final ConversationAbandonedEvent event) {
	}

	/**
	 * Get conversation prefix before each message
	 * <p>
	 * By default we use the plugins tell prefix
	 * <p>
	 * TIP: You can use {@link org.broken.arrow.prompt.library.utility.SimplePrefix}
	 *
	 * @return prefix you set or the plugin name if not set a prefix.
	 */
	public ConversationPrefix getPrefix() {
		return new SimplePrefix(this.prefix != null ? this.prefix : this.plugin.getName());
	}

	protected void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public boolean insertPrefix() {
		return this.prefix != null && !this.prefix.isEmpty();
	}

	/**
	 * Get the inactivity time when it should quit the conversion in seconds.
	 *
	 * @return The time when it should automatic close.
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set the inactivity time, before automatic ends the
	 * conversion.
	 *
	 * @param timeout time in seconds.
	 */
	protected void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

}
