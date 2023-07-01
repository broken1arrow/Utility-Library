package org.broken.arrow.localization.library;

import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.localization.library.builders.PlaceholderText;
import org.broken.arrow.localization.library.builders.PluginMessages;
import org.broken.arrow.serialize.library.utility.converters.PlaceholderTranslator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.broken.arrow.serialize.library.utility.converters.PlaceholderTranslator.translatePlaceholders;

/**
 * Utility class for handling messages and localization.
 */
public class MessagesUtility {
	private final LocalizationCache localizationCache;
	private final Logger log;

	public MessagesUtility(LocalizationCache localizationCache, String pluginName) {
		this.localizationCache = localizationCache;
		this.log = Logger.getLogger(pluginName);
	}

	/**
	 * Sends a plain message to a player or logs it if the player is null.
	 *
	 * @param player  The player to send the message to, or null for a log message.
	 * @param message The message to send.
	 */
	public void sendPlainMessage(Player player, String message) {
		this.sendPlainMessage(null, player, message);
	}

	/**
	 * Sends a message to a player using a key from the YAML file for localization.
	 *
	 * @param player       The player to send the message to, or null for a log message.
	 * @param key          The key from the YAML file for the message value. Use only the inner key, ignoring the first key "Message".
	 * @param placeholders The placeholders to replace in the message.
	 */
	public void sendMessage(@Nullable final Player player, @Nonnull final String key, @Nullable final Object... placeholders) {
		this.sendMessage(null, player, key, placeholders);
	}

	/**
	 * Sends a message to a player using a key from the YAML file for localization or logs it if the player is null, with optional log level.
	 *
	 * @param level        The log level of the message, if sending a console message.
	 * @param player       The player to send the message to, or null for a log message.
	 * @param key          The key from the YAML file for the message value. Use only the inner key, ignoring the first key "Message".
	 * @param placeholders The placeholders to replace in the message.
	 */
	public void sendMessage(@Nullable final Level level, @Nullable final Player player, @Nonnull final String key, @Nullable final Object... placeholders) {
		PluginMessages pluginMessages = this.localizationCache.getLocalization().getPluginMessages();
		
		if (pluginMessages == null) return;
		List<String> messages = pluginMessages.getMessage(key);
		String pluginName = pluginMessages.getPluginName();
		if (pluginName == null)
			pluginName = "";
		if (!messages.isEmpty()) {
			boolean addPreSuffix = messages.size() > 1;
			if (addPreSuffix && pluginMessages.getPrefixDecor() != null) {
				String prefixMsg = PlaceholderTranslator.translatePlaceholders(pluginMessages.getPrefixDecor(), pluginName);
				sendPlainMessage(level, player, prefixMsg);
			}
			for (String message : messages) {
				if (message == null) continue;
				message = PlaceholderTranslator.translatePlaceholders((addPreSuffix ? "" : pluginName) + message, placeholders);
				sendPlainMessage(level, player, message);
			}
			if (addPreSuffix && pluginMessages.getSuffixDecor() != null) {
				String suffixMsg = PlaceholderTranslator.translatePlaceholders(pluginMessages.getSuffixDecor(), pluginName);
				sendPlainMessage(level, player, suffixMsg);
			}
		}
	}

	/**
	 * Retrieves a placeholder value from the YAML file for localization.
	 * This method is used to replace specific values, making it easy to change them in the file for different languages.
	 *
	 * @param key          The key from the YAML file for the placeholder value. Use only the inner key, ignoring the first key "Placeholders".
	 * @param placeholders The placeholders to replace in the value.
	 * @return The placeholder value with replaced placeholders.
	 */
	public String getPlaceholder(@Nonnull final String key, @Nullable Object... placeholders) {
		LocalizationCache language = this.localizationCache;
		if (language == null) return "";
		PlaceholderText pluginMessages = language.getLocalization().getPlaceholderText();
		if (pluginMessages == null) return "";
		return translatePlaceholders(pluginMessages.getPlaceholder(key), placeholders);
	}

	/**
	 * Sends a log message with the specified log level.
	 *
	 * @param logLevel The log level of the message.
	 * @param msg      The log message to send.
	 */
	public void sendLogMsg(final Level logLevel, final String msg) {
		log.log(logLevel, msg);
	}

	/**
	 * Sends a plain message to a player or logs it if the player is null, with optional log level.
	 *
	 * @param level   The log level of the message, if sending a log message.
	 * @param player  The player to send the message to, or null for a log message.
	 * @param message The message to send.
	 */
	public void sendPlainMessage(Level level, Player player, String message) {
		if (player == null) {
			if (level == null)
				this.sendLogMsg(Level.INFO, ChatColor.stripColor(message));
			else
				this.sendLogMsg(level, ChatColor.stripColor(message));
			return;
		}
		if (message != null) {
			if (!player.isConversing())
				player.sendMessage(TextTranslator.toSpigotFormat(message));
			else
				player.sendRawMessage(TextTranslator.toSpigotFormat(message));
		}
	}
}
