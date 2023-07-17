package org.broken.arrow.title.update.library;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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

	@Nullable
	private ContainerUtility setNmsData(float serverVersion) {
		final Map<Integer, String> inventorySizeNames;
		NmsData nmsData = null;
		if (serverVersion >= 17.0) {
			inventorySizeNames = convertFieldNames(new FieldName(9, "a"), new FieldName(18, "b"), new FieldName(27, "c"), new FieldName(36, "d"), new FieldName(45, "e"), new FieldName(54, "f"), new FieldName(5, "p"));
			if (serverVersion >= 19.0) {
				if (serverVersion >= 19.4F) {
					if (serverVersion >= 20.0)
						nmsData = new NmsData("bR", "j", "a", "a", inventorySizeNames);
					else
						// inside net.minecraft.world.entity.player and class EntityHuman do you have the Container field.
						nmsData = new NmsData("bP", "j", "a", "a", inventorySizeNames);
				} else {
					nmsData = new NmsData("bU", "j", "a", "a", inventorySizeNames);
				}

			} else if (serverVersion >= 18.0) {
				nmsData = new NmsData(serverVersion >= 18.2F ? "bV" : "bW", "j", "a", "a", inventorySizeNames);
			} else if (serverVersion == 17.0) {
				nmsData = new NmsData("bV", "j", "sendPacket", "initMenu", inventorySizeNames);
			}
		} else if (serverVersion < 17) {
			inventorySizeNames = convertFieldNames(new FieldName(9, "1"), new FieldName(18, "2"), new FieldName(27, "3"), new FieldName(36, "4"), new FieldName(45, "5"), new FieldName(54, "6"), new FieldName(5, "HOPPER"));
			nmsData = new NmsData("activeContainer", "windowId", "sendPacket", "updateInventory", inventorySizeNames);
		}
		if (nmsData != null)
			return new ContainerUtility(nmsData, serverVersion);
		return null;
	}


	/**
	 * Use the method like this 9;a ("9" is inventory size and "a" is the field name).
	 * Is used to get the field for different inventories in the NMS class.
	 *
	 * @param fieldNames fieldNames set the name to get the right container inventory.
	 */
	static Map<Integer, String> convertFieldNames(final FieldName... fieldNames) {
		final Map<Integer, String> inventoryFieldname = new HashMap<>();
		for (final FieldName fieldName : fieldNames) {
			inventoryFieldname.put(fieldName.getInventorySize(), fieldName.getFieldName());
		}
		return inventoryFieldname;
	}
}
