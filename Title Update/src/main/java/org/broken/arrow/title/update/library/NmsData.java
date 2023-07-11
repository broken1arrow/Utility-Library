package org.broken.arrow.title.update.library;

import java.util.Map;

public final class NmsData {

	private final String containerField;
	private final String windowId;
	private final String sendPacket;
	private final String updateInventory;
	private final Map<Integer, String> containerFilenames;

	public NmsData(final String containerField, final String windowId, final String sendPacket, final String updateInventory, final Map<Integer, String> containerFilenames) {
		this.containerField = containerField;
		this.windowId = windowId;
		this.sendPacket = sendPacket;
		this.updateInventory = updateInventory;
		this.containerFilenames = containerFilenames;
	}

	/**
	 * inside net.minecraft.world.entity.player.EntityHuman do you have this field.
	 *
	 * @return field name.
	 */
	public String getContainerField() {
		return containerField;
	}

	/**
	 * This is used to get current id of a inventory (is integer)
	 * <p>
	 * The field in this class net.minecraft.world.entity.player.EntityHuman.
	 *
	 * @return the field name.
	 */
	public String getWindowId() {
		return windowId;
	}

	/**
	 * It take the method from this class net.minecraft.server.network.PlayerConnection .
	 * This method a(Packet packet) older versions is it sendPacket(Packet packet).
	 *
	 * @return method name.
	 */
	public String getSendPacket() {
		return sendPacket;
	}

	/**
	 * take the field from this class net.minecraft.world.inventory.Container .
	 * This method a(Container container) older versions is it initMenu or updateInventory.
	 *
	 * @return method name.
	 */
	public String getUpdateInventory() {
		return updateInventory;
	}

	/**
	 * Get the name for the container type field name.
	 *
	 * @param inventorySize the size of the inventory.
	 * @return the right name for the field player currently open.
	 */
	public String getContainerFilenames(final int inventorySize) {
		return containerFilenames.get(inventorySize);
	}

	@Override
	public String toString() {
		return "NmsData{" +
				"containerField='" + containerField + '\'' +
				", windowId='" + windowId + '\'' +
				", sendPacket='" + sendPacket + '\'' +
				", updateInventory='" + updateInventory + '\'' +
				", containerFilenames=" + containerFilenames +
				'}';
	}
}


