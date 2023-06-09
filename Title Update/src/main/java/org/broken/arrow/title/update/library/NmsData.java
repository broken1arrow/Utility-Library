package org.broken.arrow.title.update.library;

import java.util.Map;

public final class NmsData {

	private final String contanerField;
	private final String windowId;
	private final String sendPacket;
	private final String updateInventory;
	private final Map<Integer, String> containerFieldnames;

	public NmsData(final String contanerField, final String windowId, final String sendPacket, final String updateInventory, final Map<Integer, String> containerFieldnames) {
		this.contanerField = contanerField;
		this.windowId = windowId;
		this.sendPacket = sendPacket;
		this.updateInventory = updateInventory;
		this.containerFieldnames = containerFieldnames;
	}

	/**
	 * inside net.minecraft.world.entity.player.EntityHuman do you have this field.
	 *
	 * @return field name.
	 */
	public String getContanerField() {
		return contanerField;
	}

	/**
	 * This is uesd to get current id of a inventory (is intriger)
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
	 * Get the name for the container type fieald name.
	 *
	 * @param inventorySize the size of the inventory.
	 * @return the right name for the field player currently open.
	 */
	public String getContainerFieldnames(final int inventorySize) {
		return containerFieldnames.get(inventorySize);
	}
}


