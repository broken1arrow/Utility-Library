package org.broken.arrow.title.update.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.title.update.library.utility.TitleLogger;
import org.broken.arrow.title.update.library.utility.TitleUtility;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;


public class UpdateTitle {
	private static boolean hasCastEx;
	private static final float serverVersion;
	private static final TitleLogger titleLogger;

	@Nullable
	private static final ContainerUtility containerUtility;

	static {
		titleLogger = new TitleLogger(UpdateTitle.class);
		synchronized (UpdateTitle.class) {
			SetNmsData nmsData = new SetNmsData();
			serverVersion = nmsData.getServerVersion();
			containerUtility = nmsData.getContainerUtility();
		}
	}

	/**
	 * Not in use yet.
	 *
	 * @param player the player that open the inventory.
	 * @param title  the title should be showed.
	 */
	public static void update(final Player player, final JsonArray title) {

		//update( player,  title, false);
	}

	/**
	 * Sets the JSON object title and send the title update to the client. You need to format this
	 * so Minecraft can read the JSON string, and this will be converted to a string.
	 * <p>&nbsp;</p>
	 * <p>
	 * To ensure proper formatting, follow this structure:
	 * For titles with multiple colors set in the same text, use the "extra" key and
	 * an empty "text" element outside the array at the end of the JSON.
	 * </p>
	 * <p>
	 * Example with multiple components:
	 * </p>
	 * <pre>
	 * {
	 *   "extra":[
	 *      {
	 *        "color":"gold",
	 *        "text":"Test "
	 *      },
	 *      {
	 *        "color":"dark_red",
	 *        "bold":true,
	 *        "text":"this"
	 *       }
	 *    ],
	 *    "text":""
	 * }
	 * </pre>
	 * Example with a single color set:
	 * <pre>
	 * {
	 *   "color": "gold",
	 *   "text": "Test this"
	 * }
	 * </pre>
	 *
	 * @param player     the player that open the inventory.
	 * @param jsonObject The JSON object representing the title.
	 */
	public static void update(final Player player, final JsonObject jsonObject) {
		if (jsonObject == null) return;
		TitleUtility titleUtility = new TitleUtility(false);
		titleUtility.setJsonObjectTitle(jsonObject);
		update(player, titleUtility);
	}

	/**
	 * Send the title update to the client. This method will only
	 * attempt translate colors that follow spigot color formatting.
	 * <p>&nbsp;</p>
	 * As the example below:
	 * <pre>
	 *  standard -&#62; &#38;5,&#38;f
	 *
	 *  hex -&#62; &#38;x&#38;b&#38;d&#38;e&#38;a&#38;a
	 * </pre>
	 *
	 * @param player the player that open the inventory.
	 * @param title  the title should be showed.
	 */
	public static void update(final Player player, final String title) {
		if (title == null) return;
		TitleUtility titleUtility = new TitleUtility(false);
		titleUtility.setTitle(title);
		update(player, titleUtility);
	}

	/**
	 * Send the title update to the client. And if the boolean is set
	 * to false it will only use spigot color formatting. Not use that one from
	 * Color conversion module.
	 *
	 * @param player              the player that open the inventory.
	 * @param title               the title should be showed.
	 * @param defaultConvertColor set to true if it shall translate the colors, hex and gradients or you
	 *                            handle that self.
	 */
	public static void update(final Player player, final String title, boolean defaultConvertColor) {
		if (title == null) return;
		TitleUtility titleUtility = new TitleUtility(defaultConvertColor);
		titleUtility.setTitle(title);
		update(player, titleUtility);

	}

	private static void update(final Player player, @Nonnull final TitleUtility titleUtility) {
		if (hasCastEx) {
			titleLogger.sendLOG(Level.WARNING, "There was an error while updating the title. Please contact the developer for assistance.");
			titleLogger.sendLOG(Level.WARNING, "The set NMS values: " + containerUtility);
			return;
		}
		if (player != null && containerUtility != null && serverVersion > 0)
			try {
				if (!titleUtility.isTitleSet())
					titleLogger.sendLOG(Level.WARNING, "Title is not set, so can't update the title.");
				else
					containerUtility.updateInventory(player, titleUtility);
			} catch (Exception exception) {
				titleLogger.sendLOG(Level.WARNING, "There was an error while updating the title. Please contact the developer for assistance.");
				titleLogger.sendLOG(exception, Level.WARNING, "");
				hasCastEx = true;
			}
	}


	public static float getServerVersion() {
		return serverVersion;
	}

}

