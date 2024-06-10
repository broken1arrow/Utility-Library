package org.broken.arrow.title.update.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.title.update.library.utility.TitleUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;


public class UpdateTitle {
	private static final Logging logger = new Logging(UpdateTitle.class);
	private static boolean hasCastEx;
	private static final float SERVER_VERSION;

	private UpdateTitle() {
	}

	@Nullable
	private static final ContainerUtility containerUtility;

	static {
		synchronized (UpdateTitle.class) {
			SetNmsData nmsData = new SetNmsData();
			SERVER_VERSION = nmsData.getServerVersion();
			containerUtility = nmsData.getContainerUtility();
		}
	}

	/**
	 * Not in use yet.
	 *
	 * @param player the player that open the inventory.
	 * @param title  the title should be showed.
	 */
	@Deprecated
	public static void update(final Player player, final JsonArray title) {
		//This method are not in use and don't know if it will ever be. update( player,  title, false);
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
	 * Note: currently this method only works in 1.20.2 and below. Due to I change to
	 * use spigot official way to update title so I have not added option to convert json to
	 * a string spigot accepts.
	 *
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
			logger.log(Level.WARNING, () -> of("There was an error while updating the title. Please contact the developer for assistance."));
			if (SERVER_VERSION < 20.2F)
				logger.log(Level.WARNING, () -> of("The set NMS values: " + containerUtility));

			return;
		}
		if (SERVER_VERSION <= 0) {
			logger.log(Level.WARNING, () -> of("The server version is 0 or below " + SERVER_VERSION));
			return;
		}
		if (showTitleNewerMinecraft(player, titleUtility)) return;

		if (player != null && containerUtility != null && SERVER_VERSION > 0)
			try {
				if (!titleUtility.isTitleSet())
					logger.log(Level.WARNING, () -> of("Title is not set, so can't update the title."));
				else
					containerUtility.updateInventory(player, titleUtility);
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception, () -> of("There was an error while updating the title. Please contact the developer for assistance."));
				hasCastEx = true;
			}
	}

	private static boolean showTitleNewerMinecraft(Player player, @Nonnull TitleUtility titleUtility) {
		if (SERVER_VERSION > 20.2F) {
			if (player == null)
				return true;
			InventoryView inventoryView = player.getOpenInventory();
			try {
				if (!titleUtility.isTitleSet())
					logger.log(Level.WARNING, () -> of("Title is not set, so can't update the title."));

				inventoryView.setTitle(titleUtility.getTitle(SERVER_VERSION) + "");
			} catch (IllegalArgumentException e) {
				logger.log(Level.INFO, () -> Logging.of("Could not render this inventory: " + inventoryView.getType()));
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception, () -> Logging.of("Something was not working when update the title: " + inventoryView.getType()));
				hasCastEx = true;
			}
			return true;
		}
		return false;
	}


	public static float getServerVersion() {
		return SERVER_VERSION;
	}

}

