package org.broken.arrow.library.title.update.nms.modules.v_1_12;

import org.broken.arrow.library.title.update.nms.InventoryNMS;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class InventoryPacketTwelve implements InventoryNMS {

	@Override
	public Class<?> getPacket() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Packet"));
	}

	@Override
	public Field getPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName(retrieveNMSPackage("EntityPlayer")).getField("playerConnection");
	}

	@Override
	public Class<?> getPlayerConnectionClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("PlayerConnection"));
	}

	@Override
	public Class<?> getContainersClass() throws ClassNotFoundException {
		return String.class;
	}

	@Override
	public Class<?> getContainerClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Container"));
	}

	@Override
	public Class<?> getChatSerializer() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("IChatBaseComponent$ChatSerializer"));
	}

	@Override
	public Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> iChatBaseComponent = Class.forName(retrieveNMSPackage("IChatBaseComponent"));
		return Class.forName(retrieveNMSPackage("PacketPlayOutOpenWindow")).getConstructor(int.class, this.getContainersClass(), iChatBaseComponent, int.class);
	}

	@Nonnull
	@Override
	public String getContainerField() {
		return "activeContainer";
	}

	@Nonnull
	@Override
	public String getWindowId() {
		return "windowId";
	}

	@Nonnull
	@Override
	public String getSendPacketName() {
		return "sendPacket";
	}

	@Nonnull
	@Override
	public String getUpdateInventoryMethodName() {
		return "updateInventory";
	}

	@Override
	public String getContainerFieldName(@Nonnull final Inventory currentlyOpenInventory) {
		return "minecraft:" + currentlyOpenInventory.getType().name().toLowerCase();
	}
}
