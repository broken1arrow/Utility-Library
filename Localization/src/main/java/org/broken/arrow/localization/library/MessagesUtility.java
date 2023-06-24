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

public class MessagesUtility {
	private final LocalizationCache localizationCache;
	private final Logger log;

	public MessagesUtility(LocalizationCache localizationCache, String pluginName) {
		this.localizationCache = localizationCache;
		this.log = Logger.getLogger(pluginName);
	}

	public void sendPlainMessage(Player player, String message) {
		this.sendPlainMessage(null, player, message);
	}

	public void sendMessage(@Nullable final Player player, @Nonnull final String key, @Nullable final Object... placeholders) {
		this.sendMessage(null, player, key, placeholders);
	}

	public void sendMessage(@Nullable final Level level, @Nullable final Player player, @Nonnull final String key, @Nullable final Object... placeholders) {
		LocalizationCache language = localizationCache;
		PluginMessages pluginMessages = language.getLocalization().getPluginMessages();
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

	public String getPlaceholder(@Nonnull final String key, @Nullable Object... placeholders) {
		LocalizationCache language = this.localizationCache;
		if (language == null) return "";
		PlaceholderText pluginMessages = language.getLocalization().getPlaceholderText();
		if (pluginMessages == null) return "";
		return translatePlaceholders(pluginMessages.getPlaceholder(key), placeholders);
	}

	public void sendLogMsg(final Level logLevel, final String msg) {
		log.log(logLevel, msg);
	}

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
