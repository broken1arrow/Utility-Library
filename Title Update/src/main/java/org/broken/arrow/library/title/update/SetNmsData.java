package org.broken.arrow.library.title.update;

import org.broken.arrow.library.title.update.nms.InventoryNMS;
import org.broken.arrow.library.title.update.nms.modules.v_1_12.InventoryPacketTwelve;
import org.broken.arrow.library.title.update.nms.modules.v_1_16.InventoryPacketSixteen;
import org.broken.arrow.library.title.update.nms.modules.v_1_17.InventoryPacketSeventeen;
import org.broken.arrow.library.title.update.nms.modules.v_1_18.InventoryPacketEighteenTwo;
import org.broken.arrow.library.title.update.nms.modules.v_1_18.InventoryPacketEighteen;
import org.broken.arrow.library.title.update.nms.modules.v_1_19.InventoryPacketNineteenFour;
import org.broken.arrow.library.title.update.nms.modules.v_1_19.InventoryPacketNineteen;
import org.broken.arrow.library.title.update.nms.modules.v_1_20.InventoryPacketTwentyTwo;
import org.broken.arrow.library.title.update.nms.modules.v_1_20.InventoryPacketTwenty;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class responsible for managing NMS (Net Minecraft Server) related data
 * based on the current Minecraft server version.
 * <p>
 * This class determines the server version by parsing the Bukkit version string,
 * and conditionally initializes a {@link ContainerUtility} instance depending on
 * whether the server version supports it.
 * </p>
 * <p>
 * For server versions greater than 20.2, the {@code ContainerUtility} is not initialized
 * and is set to {@code null}, reflecting potential incompatibilities or deprecated support.
 * </p>
 * <p>
 * The server version parsing logic extracts the major and minor version numbers
 * from the Bukkit version string, handling cases with suffixes such as
 * build numbers or release tags (e.g., "1.20.2-R1").
 * </p>
 */
public class SetNmsData {
	private final ContainerUtility containerUtility;
	private final float serverVersion;

	/**
	 * Creates a new instance of {@code SetNmsData}, determining the server version and
	 * initializing the {@link ContainerUtility} if applicable.
	 */
	public SetNmsData() {
		serverVersion = setServerVersion();
		if (serverVersion > 20.2F) {
			containerUtility = null;
			return;
		}
		containerUtility = setNmsData(serverVersion);
	}

	/**
	 * Gets the {@link ContainerUtility} instance if initialized for the current server version.
	 *
	 * @return the container utility, or {@code null} if unsupported for this server version
	 */
	@Nullable
	public ContainerUtility getContainerUtility() {
		return containerUtility;
	}

	/**
	 * Gets the parsed server version as a float value representing major.minor version.
	 *
	 * @return the server version, e.g., 20.4 or 19.3
	 */
	public float getServerVersion() {
		return serverVersion;
	}

	/**
	 * Parses the Bukkit server version string to extract the major and minor version numbers
	 * as a float value.
	 * <p>
	 * Handles cases where version strings include suffixes like build numbers
	 * or release candidates, e.g., "1.20.2-R1" or "1.20.2".
	 * </p>
	 *
	 * @return the parsed server version as a float (major.minor)
	 */
	private float setServerVersion() {
		final String[] versionPieces = Bukkit.getServer().getBukkitVersion().split("\\.");
		final String firstNumber;
		String secondNumber;
		final String firstString = versionPieces[1];
		if (firstString.contains("-")) {
			firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

			secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
			final int index = secondNumber.toUpperCase().indexOf("R");
			if (index >= 0) secondNumber = secondNumber.substring(index + 1);
		} else {
			final String secondString = versionPieces[2];
			firstNumber = firstString;
			secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
		}
		return Float.parseFloat(firstNumber + "." + secondNumber);
	}

	@Nonnull
	private ContainerUtility setNmsData(float serverVersion) {
		InventoryNMS inventoryNMS;
		switch ((int) Math.floor(serverVersion)) {
			case 20:
				if (serverVersion > 20.1F)
					inventoryNMS = new InventoryPacketTwentyTwo();
				else
					inventoryNMS = new InventoryPacketTwenty();
				break;
			case 19:
				if (serverVersion >= 19.4F) {
					inventoryNMS = new InventoryPacketNineteenFour();
				} else {
					inventoryNMS = new InventoryPacketNineteen();
				}
				break;
			case 18:
				if (serverVersion >= 18.2F) {
					inventoryNMS = new InventoryPacketEighteenTwo();
				} else {
					inventoryNMS = new InventoryPacketEighteen();
				}
				break;
			case 17:
				inventoryNMS = new InventoryPacketSeventeen();
				break;
			default:
				inventoryNMS = getInventoryNMS(serverVersion);
		}
		return new ContainerUtility(inventoryNMS, serverVersion);
	}

	@Nonnull
	private InventoryNMS getInventoryNMS(final float serverVersion) {
		InventoryNMS inventoryNMS;
		if (serverVersion < 14.0F) {
			inventoryNMS = new InventoryPacketTwelve();
		} else {
			if (serverVersion > 19)
				inventoryNMS = new InventoryPacketTwenty();
			else
				inventoryNMS = new InventoryPacketSixteen();
		}
		return inventoryNMS;
	}

}
