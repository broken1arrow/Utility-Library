package org.broken.arrow.title.update.library.nms;

import org.bukkit.inventory.Inventory;

import java.lang.reflect.Constructor;

public class V_1_12 implements InventoryNMS {

	@Override
	public Class<?> getContainerClass() {
		return String.class;
	}

	@Override
	public Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		return Class.forName(retrieveNMSPackage("PacketPlayOutOpenWindow")).getConstructor(int.class, this.getContainerClass(), this.getChatSerializer(), int.class);
	}

	@Override
	public String containerField() {
		return "activeContainer";
	}

	@Override
	public String windowId() {
		return "windowId";
	}

	@Override
	public String sendPacket() {
		return "sendPacket";
	}

	@Override
	public String getUpdateInventoryMethodName() {
		return "updateInventory";
	}

	@Override
	public String containerFieldName(final Inventory currentlyOpenInventory) {
		return "minecraft:" + currentlyOpenInventory.getType().name().toLowerCase();
	}
}
