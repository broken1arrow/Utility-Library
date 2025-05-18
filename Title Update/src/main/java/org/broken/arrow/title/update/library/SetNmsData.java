package org.broken.arrow.title.update.library;

import org.broken.arrow.title.update.library.nms.InventoryNMS;
import org.broken.arrow.title.update.library.nms.modules.v_1_12.InventoryPacketTwelve;
import org.broken.arrow.title.update.library.nms.modules.v_1_16.InventoryPacketSixteen;
import org.broken.arrow.title.update.library.nms.modules.v_1_17.InventoryPacketSeventeen;
import org.broken.arrow.title.update.library.nms.modules.v_1_18.InventoryPacketEighteenTwo;
import org.broken.arrow.title.update.library.nms.modules.v_1_18.InventoryPacketEighteen;
import org.broken.arrow.title.update.library.nms.modules.v_1_19.InventoryPacketNineteenFour;
import org.broken.arrow.title.update.library.nms.modules.v_1_19.InventoryPacketNineteen;
import org.broken.arrow.title.update.library.nms.modules.v_1_20.InventoryPacketTwentyTwo;
import org.broken.arrow.title.update.library.nms.modules.v_1_20.InventoryPacketTwenty;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetNmsData {
	private final ContainerUtility containerUtility;
	private final float serverVersion;

	public SetNmsData() {
		serverVersion = setServerVersion();
		if (serverVersion > 20.2F) {
			containerUtility = null;
			return;
		}
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
				if (serverVersion < 14.0F) {
					inventoryNMS = new InventoryPacketTwelve();
				} else {
					if (serverVersion > 19)
						inventoryNMS = new InventoryPacketTwenty();
					else
						inventoryNMS = new InventoryPacketSixteen();
				}
		}
		return new ContainerUtility(inventoryNMS, serverVersion);
	}

}
