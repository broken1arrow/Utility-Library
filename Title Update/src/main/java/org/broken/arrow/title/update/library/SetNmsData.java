package org.broken.arrow.title.update.library;

import org.broken.arrow.title.update.library.nms.InventoryNMS;
import org.broken.arrow.title.update.library.nms.modules.V_1_12_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_16_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_17_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_18_2_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_18_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_19_4_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_19_Inventory;
import org.broken.arrow.title.update.library.nms.modules.V_1_20_Inventory;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetNmsData {
	private final ContainerUtility containerUtility;
	private final float serverVersion;

	public SetNmsData() {
		serverVersion = setServerVersion();
		containerUtility = setNmsData(serverVersion);
	}

	@Nullable
	public ContainerUtility getContainerUtility() {
		return containerUtility;
	}

	public float getServerVersion() {
		return serverVersion;
	}

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
				inventoryNMS = new V_1_20_Inventory();
				break;
			case 19:
				if (serverVersion >= 19.4F) {
					inventoryNMS = new V_1_19_4_Inventory();
				} else {
					inventoryNMS = new V_1_19_Inventory();
				}
				break;
			case 18:
				if (serverVersion >= 18.2F) {
					inventoryNMS = new V_1_18_2_Inventory();
				} else {
					inventoryNMS = new V_1_18_Inventory();
				}
				break;
			case 17:
				inventoryNMS = new V_1_17_Inventory();
				break;
			default:
				if (serverVersion < 14.0F) {
					inventoryNMS = new V_1_12_Inventory();
				} else {
					inventoryNMS = new V_1_16_Inventory();
				}
		}
		return new ContainerUtility(inventoryNMS, serverVersion);
	}

}
