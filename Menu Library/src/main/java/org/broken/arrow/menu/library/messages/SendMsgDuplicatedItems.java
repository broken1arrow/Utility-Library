package org.broken.arrow.menu.library.messages;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.color.library.TextTranslator;
import org.bukkit.entity.Player;

/**
 * Set messages when player add duplicated items or items you have blacklisted.
 */
public class SendMsgDuplicatedItems {
	private String blacklistMessage;
	private String dublicatedMessage;
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
	 * Suport both hex and &amp; colorcodes and have this placeholders:
	 * <p>
	 * hex format is &lt;#8000ff&gt; and gradients &lt;#8000ff:#8000ff&gt;
	 * {0} = item type
	 *
	 * @param blacklistMessage set a message.
	 */

	public void setBlacklistMessage(String blacklistMessage) {
		this.blacklistMessage = blacklistMessage;
	}

	/**
	 * Set message for when player have added item some are dublicated.
	 * Suport both hex and &amp; colorcodes and have this placeholders:
	 * <p>
	 * hex format is &lt;#8000ff&gt; and gradients &lt;#8000ff:#8000ff&gt;
	 * {0} = item type
	 * {1} = amount of stacks
	 * {2} = item amount
	 *
	 * @param dublicatedMessage set a message.
	 */

	public void setDublicatedMessage(String dublicatedMessage) {
		this.dublicatedMessage = dublicatedMessage;
	}

	public void sendMessage(Player player, String msg) {
		player.sendMessage(msg);
	}

	public void sendBlacklistMessage(Player player, Object... placeholders) {
		String message;
		if (blacklistMessage == null)
			message = "&fthis item&6 {0}&f are blacklisted";
		else
			message = blacklistMessage;
		if (notFoundTextTranslator)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, placeholders))));
		else
			player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
	}

	public void sendDublicatedMessage(Player player, Object... placeholders) {
		String message;
		if (dublicatedMessage == null)
			message = "&fYou can't add more than one &6 {0} &ftype, You have added &4{1}&f extra itemstack. You get back &6 {2}&f items.";
		else
			message = dublicatedMessage;
		if (notFoundTextTranslator)
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', (translatePlaceholders(message, placeholders))));
		else
			player.sendMessage(TextTranslator.toSpigotFormat(translatePlaceholders(message, placeholders)));
	}

	public String translatePlaceholders(String rawText, Object... placeholders) {
		for (int i = 0; i < placeholders.length; i++) {
			rawText = rawText.replace("{" + i + "}", placeholders[i] != null ? placeholders[i].toString() : "");
		}
		return rawText;
	}
}
