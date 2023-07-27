package org.broken.arrow.menu.library.messages;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.color.library.TextTranslator;
import org.bukkit.entity.Player;

/**
 * Set messages when player add duplicated items or items you have blacklisted.
 */
public class SendMsgDuplicatedItems {
	private String blacklistMessage;
	private String duplicatedMessage;
	private boolean notFoundTextTranslator;

	public SendMsgDuplicatedItems() {
		try {
			TextTranslator.getInstance();
		} catch (NoClassDefFoundError ignore) {
			notFoundTextTranslator = true;
		}

	}

	/**
	 * Set message for when player have added item some are blacklisted.
	 * Support both hex and &amp; color codes. Hex formats supported:
	 * <p>
	 * hex format is &lt;#8000ff&gt; and gradients &lt;#8000ff:#8000ff&gt;
	 * </p>
	 * <p>&nbsp;</p>
	 * {@code
	 * The placeholders to use:
	 * &ndash; {0} = item type
	 * }
	 *
	 * @param blacklistMessage set a message.
	 */

	public void setBlacklistMessage(String blacklistMessage) {
		this.blacklistMessage = blacklistMessage;
	}

	/**
	 * Set message for when player have added item some are duplicated.
	 * Support both hex and &amp; color codes.Hex formats supported:
	 * <p>
	 * hex format is &lt;#8000ff&gt; and gradients &lt;#8000ff:#8000ff&gt;
	 * </p>
	 * <p>&nbsp;</p>
	 * {@code
	 * <p>The placeholders to use:</p>
	 * <p> &ndash; {0} = item type </p>
	 * <p> &ndash; {1} = total amount of stacks</p>
	 * <p>&ndash; {2} = item amount</p>
	 * }
	 *
	 * @param duplicatedMessage set a message.
	 */

	public void setDuplicatedMessage(String duplicatedMessage) {
		this.duplicatedMessage = duplicatedMessage;
	}

	public void sendMessage(Player player, String msg) {
		player.sendMessage(msg);
	}

	public void sendBlacklistMessage(Player player, Object... placeholders) {
		String message;
		if (blacklistMessage == null) message = "&fthis item&6 {0}&f are blacklisted and you get the items back";
		else message = blacklistMessage;
		if (notFoundTextTranslator)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, placeholders))));
		else player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
	}

	public void sendDuplicatedMessage(Player player, Object... placeholders) {
		String message;
		if (duplicatedMessage == null)
			message = "&fYou can't add more if this &6 {0} &ftype, you get back &6 {2}&f items.You have added totally &4{1}&f extra itemstacks";
		else message = duplicatedMessage;
		if (notFoundTextTranslator)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, placeholders))));
		else player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
	}

	public String translatePlaceholders(String rawText, Object... placeholders) {
		for (int i = 0; i < placeholders.length; i++) {
			rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
		}
		return rawText;
	}
}
