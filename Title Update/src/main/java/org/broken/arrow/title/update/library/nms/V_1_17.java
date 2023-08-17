package org.broken.arrow.title.update.library.nms;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class V_1_17 implements InventoryNMS {


	@Override
	public Class<?> getPacket() throws ClassNotFoundException {
		return Class.forName("net.minecraft.network.protocol.Packet");
	}

	@Override
	public Field getPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
	}

	@Override
	public Class<?> getPlayerConnectionClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.server.network.PlayerConnection");
	}

	@Override
	public Class<?> getContainersClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.world.inventory.Containers");
	}

	@Override
	public Class<?> getContainerClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.world.inventory.Container");
	}

	@Override
	public Class<?> getChatSerializer() throws ClassNotFoundException {
		return Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
	}

	@Override
	public Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		return Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(int.class, this.getContainerClass(), this.getChatSerializer());
	}

	@Override
	public String containerField() {
		return "bV";
	}

	@Override
	public String windowId() {
		return "j";
	}

	@Override
	public String sendPacket() {
		return "sendPacket";
	}

	@Override
	public String getUpdateInventoryMethodName() {
		return "initMenu";
	}

	@Override
	public String containerFieldName(final Inventory currentlyOpenInventory) {

		InventoryType inventoryType = currentlyOpenInventory.getType();
		switch (inventoryType) {
			case CHEST:
				final int inventorySize = currentlyOpenInventory.getSize();
				switch (inventorySize) {
					case 9:
						return "a";
					case 18:
						return "b";
					case 36:
						return "d";
					case 45:
						return "e";
					case 55:
						return "f";
					default:
						return "c";
				}
			case DISPENSER:
			case DROPPER:
				return "g";
		/*todo should this be implemented and can you update title? you find the field for InventoryEnderChest inside net.minecraft.world.entity.player.EntityHuman
			case ENDER_CHEST:
				break;*/
			case ANVIL:
				return "h";
			case HOPPER:
				return "p";
			case SHULKER_BOX:
				return "t";
        /*todo should this be implemented? class do you find in net.minecraft.world.level.block.entity.TileEntityBarrel.
           Check this nms code it use the field 'protected static final DataWatcherObject<NBTTagCompound> bR;':
              protected boolean a(EntityHuman entityhuman) {
                if (entityhuman.bR instanceof ContainerChest) {
                    IInventory iinventory = ((ContainerChest)entityhuman.bR).l();
                    return iinventory == TileEntityBarrel.this;
                } else {
                    return false;
                }
            }
			case BARREL:
				break;*/
		}
		return null;
	}
}
